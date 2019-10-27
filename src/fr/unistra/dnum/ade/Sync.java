package fr.unistra.dnum.ade;

import com.adesoft.beans.AdeApi6;
import com.adesoft.beans.filters.FiltersEvents;
import com.adesoft.errors.ProjectNotFoundException;
import org.jdom.Element;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;


public class Sync {
    public static JSONObject run(AdeApi6 api) {
        JSONObject doc = new JSONObject();
        UUID uuid = UUID.randomUUID();

        doc.put("operation_id", uuid.toString());

        try {
            Calendar start = Calendar.getInstance();
            FiltersEvents fe = new FiltersEvents();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 18);
            cal.set(Calendar.MINUTE, 0);
            fe.addFilterUpdatedStart(cal.getTimeInMillis());
            List<Element> events = api.getEvents(fe, 8).getChildren();
            for(Element event : events) {
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

            FileOutputStream fos = new FileOutputStream("/tmp/ade-sync.txt");
            PrintWriter pw = new PrintWriter("ade-sync.txt");
            pw.print("" + start.getTimeInMillis());
            pw.close();
        } catch (RemoteException | ProjectNotFoundException | FileNotFoundException e) {
            e.printStackTrace();
        }

        return doc;
    }


}
