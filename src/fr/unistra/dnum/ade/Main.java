package fr.unistra.dnum.ade;

import com.adesoft.beans.AdeApi6;
import org.apache.commons.cli.*;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final String OPTION_CONFIG = "c"; //$NON-NLS-1$

    public static void main(String[] args) {
        CommandLineParser parser = new PosixParser();
        Options options = new Options();
        options.addOption(OPTION_CONFIG, "config", true, "path to the config file"); //$NON-NLS-1$//$NON-NLS-2$

        try {
            CommandLine line = parser.parse(options, args);

            String configPath = null;

            if (line.hasOption(OPTION_CONFIG)) {
                configPath = line.getOptionValue(OPTION_CONFIG);

                File f = new File(configPath);
                System.out.println("Using configuration from " + f.getAbsolutePath());
                if ((!f.exists()) || (!f.canRead())) {
                    throw new Error("Cannot access file " + configPath); //$NON-NLS-1$
                }

                JSONObject conf = new JSONObject(new String(Files.readAllBytes(Paths.get(f.getAbsolutePath()))));

                Sync sync = new Sync(conf);
                sync.run();

            } else {
                String header = "Syncs updates from ADE\n\n"; //$NON-NLS-1$
                String footer = "\nFor more information : https://gitlab.unistra.fr/di/ade-sync"; //$NON-NLS-1$
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar ade-sync.jar", header, options, footer, true); //$NON-NLS-1$
            }

        } catch (ParseException | IOException e2) {
            e2.printStackTrace();
        }

    }
}
