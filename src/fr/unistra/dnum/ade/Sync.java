package fr.unistra.dnum.ade;

import com.adesoft.beans.AdeApi6;
import com.adesoft.beans.filters.FiltersEvents;
import com.adesoft.errors.ProjectNotFoundException;
import org.jdom.Element;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class Sync {
    private long start;
    private JSONObject config;
    private AdeApi6 api;

    public Sync(JSONObject config) {
        this.config = config;
        initAde();
    }

    private void initAde(){
        JSONObject adeConf = config.getJSONObject("ade");

        api = new com.adesoft.beans.AdeApi6();
        api.setServer(adeConf.getString("server"));
        api.setServerPort(adeConf.getInt("port"));
        api.setLogin(adeConf.getString("username"));
        api.setPassword(adeConf.getString("password"));
        api.setProjectId(adeConf.getInt("project_id"));
    }

    public void run() {
        try {
            start = Calendar.getInstance().getTimeInMillis();
            JSONObject doc = checkUpdates(api);
            setLastRun();
            System.out.println(doc);
        } catch (RemoteException | ProjectNotFoundException e) {
            System.out.println("Error with ADE");
            e.printStackTrace();
        } catch (FileNotFoundException e){
            System.out.println("Error writing run log");
            e.printStackTrace();
        }
    }

    private void setLastRun() throws FileNotFoundException {
            PrintWriter pw = new PrintWriter(getOutputFile());
            pw.print("" + start);
            pw.close();
    }

    private long getLastRun() throws IOException {
        String filePath = getOutputFile();
        File f = new File(filePath);
        if ((!f.exists()) || (!f.canRead())) {
            throw new Error("Cannot access run file " + filePath); //$NON-NLS-1$
        }
        return Long.parseLong(new String(Files.readAllBytes(Paths.get(filePath))));
    }

    private JSONObject checkUpdates(AdeApi6 api) throws RemoteException, ProjectNotFoundException {
        JSONObject doc = new JSONObject();
        UUID uuid = UUID.randomUUID();
        doc.put("operation_id", uuid.toString());

        FiltersEvents fe = new FiltersEvents();
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 0);
        fe.addFilterUpdatedStart(cal.getTimeInMillis());
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

    private String getOutputFile()
    {
        return config.getJSONObject("sync").getString("output_file");
    }


}
