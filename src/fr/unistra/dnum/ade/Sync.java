package fr.unistra.dnum.ade;

import com.adesoft.errors.ProjectNotFoundException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;


public class Sync {
    private long start;
    private long getLastRun;
    private JSONObject config;
    private SimpleDateFormat sdf;

    public Sync(JSONObject config) {
        this.config = config;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    public void run() {
        try {
            start = new GregorianCalendar().getTimeInMillis();
            AdeClient ade = new AdeClient(config.getJSONObject("ade"));
            JSONObject doc = ade.checkUpdates(getLastRun());
            setLastRun();
            System.out.println(doc);
        } catch (RemoteException | ProjectNotFoundException e) {
            System.out.println("Error with ADE");
            e.printStackTrace();
        } catch (IOException e) {
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
        if (getLastRun != 0) return getLastRun;
        String filePath = getOutputFile();
        File f = new File(filePath);
        GregorianCalendar gc = new GregorianCalendar();
        if ((!f.exists()) || (!f.canRead())) {
            // Arbitrary run last 72 hours changes
            gc.add(GregorianCalendar.HOUR, -72);
            System.out.println("No previous run found, will fetch updates since " +
                    sdf.format(gc.getTime()));
            getLastRun = gc.getTimeInMillis();
            return getLastRun;
        }
        getLastRun = Long.parseLong(new String(Files.readAllBytes(Paths.get(filePath))));
        gc.setTimeInMillis(getLastRun);
        System.out.println("Will fetch updates since " +
                sdf.format(gc.getTime()));
        return getLastRun;
    }


    private String getOutputFile() {
        return config.getJSONObject("sync").getString("output_file");
    }


}
