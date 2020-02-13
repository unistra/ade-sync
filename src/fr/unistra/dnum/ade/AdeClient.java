package fr.unistra.dnum.ade;

import com.adesoft.beans.AdeApi6;
import com.adesoft.beans.filters.FiltersEvents;
import com.adesoft.errors.ProjectNotFoundException;
import org.jdom.Element;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.*;

public class AdeClient {
    private JSONObject adeConf;
    private AdeApi6 api;
    private static ArrayList<String> categories = new ArrayList<String>(Arrays.asList("trainee", "category5", "classroom", "instructor"));

    public static Logger logger = LoggerFactory.getLogger("ade"); //$NON-NLS-1$

    public AdeClient(JSONObject adeConf) {
        this.adeConf = adeConf;
        initAde();
    }

    private void initAde() {
        api = new AdeApi6();
        api.setServer(adeConf.getString("server"));
        api.setServerPort(adeConf.getInt("port"));
        api.setLogin(adeConf.getString("username"));
        api.setPassword(adeConf.getString("password"));
        api.setProjectId(adeConf.getInt("project_id"));
    }

    public JSONObject checkUpdates(long lastRun) throws RemoteException, ProjectNotFoundException, IOException {
        JSONObject doc = new JSONObject();
        UUID uuid = UUID.randomUUID();
        doc.put("operation_id", uuid.toString());

        // Recent changes
        FiltersEvents fe = new FiltersEvents();
        fe.addFilterUpdatedStart(lastRun);
        List<Element> events = api.getEvents(fe, 8).getChildren();
        int nb_events = events.size();
        logger.info("Found " + nb_events + " updated events");
        if(nb_events > 0){
            doc.put("events", eventsToJson(events, "update").getJSONArray("events"));
        }

        // Hunt for missing events
        fe = new FiltersEvents();
        events = api.getEvents(fe, 8).getChildren();
        JSONObject old_ones = read_cache();
        HashMap<String, Element> eventsMap = new HashMap<String, Element>();
        for (Element event : events) {
            eventsMap.put(event.getString("id"), event);
        }
        for (Object o : old_ones.getJSONArray("events")) {
            JSONObject obj = (JSONObject) o;
            if (eventsMap.get(obj.getString("id")) == null) {
                nb_events++;
                logger.info("Deleted event " + obj.getString("id"));
                obj.put("operation", "deletion");
                doc.append("events", obj);
            }
        }
        write_cache(eventsToJson(events, null));

        logger.info("Found " + nb_events + " updated or deleted events");
        if (nb_events == 0) return null;
        return doc;
    }

    private void write_cache(JSONObject data) throws IOException {
        String filePath = adeConf.getString("cache_file");
        FileOutputStream fos = new FileOutputStream(new File(filePath));
        fos.write(data.toString().getBytes());
        fos.close();
    }

    private JSONObject read_cache() throws IOException {
        String filePath = adeConf.getString("cache_file");
        File f = new File(filePath);
        if ((!f.exists()) || (!f.canRead())) {
            return new JSONObject("{\"events\": []}");
        } else {
            return new JSONObject(new String(Files.readAllBytes(Paths.get(filePath))));
        }
    }

    private JSONObject eventsToJson(List<Element> events, String operation) {
        JSONObject doc = new JSONObject();
        for (Element event : events) {
            JSONObject j_event = eventToJson(event);
            if(operation != null) j_event.put("operation", operation);
            doc.append("events", j_event);
        }
        return doc;
    }

    private JSONObject eventToJson(Element event) {
        JSONObject j_event = new JSONObject();
        j_event.put("id", event.getString("id"));
        for (Object c : event.getContent()) {
            Element content = (Element) c;
            if (content.getName().equals("resources")) {
                for (Object r : content.getContent()) {
                    Element resource = (Element) r;
                    String category = ((Element) r).getString("category");
                    // ONly selected categories
                    if (categories.contains(category))
                        j_event.append("resources", resource.getInt("id"));
                }
            }
        }
        return j_event;
    }


}
