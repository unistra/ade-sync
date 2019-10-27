package fr.unistra.dnum.ade;

import com.adesoft.errors.ProjectNotFoundException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeoutException;


public class Sync {
    private long start;
    private long getLastRun;
    private JSONObject config;
    private SimpleDateFormat sdf;

    public static Logger logger = LoggerFactory.getLogger("sync"); //$NON-NLS-1$

    public Sync(JSONObject config) {
        this.config = config;
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

    public void run() {
        try {
            start = new GregorianCalendar().getTimeInMillis();
            AdeClient ade = new AdeClient(config.getJSONObject("ade"));
            JSONObject doc = ade.checkUpdates(getLastRun());
            RabbitClient rabbit = new RabbitClient(config.getJSONObject("rabbitmq"));
            rabbit.send(doc);
            rabbit.close();
            setLastRun();
            logger.info("Successful check");
        } catch (RemoteException | ProjectNotFoundException e) {
            AdeClient.logger.error("Error with ADE");
            e.printStackTrace();
        } catch (IOException e) {
            logger.error("Error withs run log");
            e.printStackTrace();
        } catch (TimeoutException e) {
            RabbitClient.logger.error("Error withs run log");
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
            logger.warn("No previous run found, will fetch updates since " +
                    sdf.format(gc.getTime()));
            getLastRun = gc.getTimeInMillis();
            return getLastRun;
        }
        getLastRun = Long.parseLong(new String(Files.readAllBytes(Paths.get(filePath))));
        gc.setTimeInMillis(getLastRun);
        logger.info("Will fetch updates since " +
                sdf.format(gc.getTime()));
        return getLastRun;
    }


    private String getOutputFile() {
        return config.getJSONObject("sync").getString("output_file");
    }


}
