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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;


public class Sync {
    private long start;
    private long previousRun;
    private JSONObject config;
    private AdeApi6 api;
    private SimpleDateFormat sdf;

    public Sync(JSONObject config) {
        this.config = config;
        initAde();
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
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
            start = new GregorianCalendar().getTimeInMillis();
            JSONObject doc = checkUpdates(api);
            setLastRun();
            System.out.println(doc);
        } catch (RemoteException | ProjectNotFoundException e) {
            System.out.println("Error with ADE");
            e.printStackTrace();
        } catch (IOException e){
            System.out.println("Error withs run log");
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
        GregorianCalendar gc = new GregorianCalendar();
        if ((!f.exists()) || (!f.canRead())) {
            // Arbitrary run last 72 hours changes
            gc.add(GregorianCalendar.HOUR, -72);
            System.out.println("No previous run found, will fetch updates since " +
                    sdf.format(gc.getTime()));
            return gc.getTimeInMillis();
        }
        long last = Long.parseLong(new String(Files.readAllBytes(Paths.get(filePath))));
        gc.setTimeInMillis(last);
        System.out.println("Will fetch updates since " +
                sdf.format(gc.getTime()));
        return last;
    }

    private JSONObject checkUpdates(AdeApi6 api) throws RemoteException, ProjectNotFoundException, IOException {
        JSONObject doc = new JSONObject();
        UUID uuid = UUID.randomUUID();
        doc.put("operation_id", uuid.toString());

        FiltersEvents fe = new FiltersEvents();
        fe.addFilterUpdatedStart(getLastRun());
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
