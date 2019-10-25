package fr.unistra.dnum.ade;

import com.adesoft.beans.AdeApi6;
import com.adesoft.beans.filters.FiltersEvents;
import com.adesoft.errors.ProjectNotFoundException;
import org.jdom.Element;
import org.json.JSONObject;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;


public class Sync {
    public static JSONObject run(AdeApi6 api) {
        JSONObject doc = new JSONObject();
        UUID uuid = UUID.randomUUID();

        doc.put("operation_id", uuid.toString());

        try {
            FiltersEvents fe = new FiltersEvents();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 16);
            cal.set(Calendar.MINUTE, 0); // date à partir de laquelle tu veux les modifs
            fe.addFilterUpdatedStart(cal.getTimeInMillis());
            Element events = api.getEvents(fe, 8); // niveau de détail 1 doit suffire si besoin que de l'id.
            List<Element> liste = events.getChildren();
            for(Element event : liste) {
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
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (ProjectNotFoundException e) {
            e.printStackTrace();
        }

        return doc;
    }


}
