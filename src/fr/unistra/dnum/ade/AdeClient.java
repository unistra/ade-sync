package fr.unistra.dnum.ade;

import com.adesoft.beans.AdeApi6;
import com.adesoft.beans.filters.FiltersEvents;
import com.adesoft.errors.ProjectNotFoundException;
import org.jdom.Element;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

public class AdeClient {
    private JSONObject adeConf;
    private AdeApi6 api;

    public AdeClient(JSONObject adeConf) {
        this.adeConf = adeConf;
        initAde();
    }

    private void initAde(){
        api = new com.adesoft.beans.AdeApi6();
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

        FiltersEvents fe = new FiltersEvents();
        fe.addFilterUpdatedStart(lastRun);
        List<Element> events = api.getEvents(fe, 8).getChildren();
        for (Element event : events) {
            JSONObject j_event = new JSONObject();
            j_event.put("id", event.getString("id"));
            for (Object c : event.getContent()) {
                Element content = (Element) c;
                if (content.getName().equals("resources")) {
                    for (Object r : content.getContent()) {
                        Element resource = (Element) r;
                        j_event.append("resources", resource.getInt("id"));
                    }
                }
            }
            doc.append("events", j_event);
        }
        return doc;
    }
}
