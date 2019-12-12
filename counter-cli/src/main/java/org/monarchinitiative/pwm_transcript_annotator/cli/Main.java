package org.monarchinitiative.pwm_transcript_annotator.cli;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparsers;
import org.monarchinitiative.pwm_transcript_annotator.cli.cmd.Command;
import org.monarchinitiative.pwm_transcript_annotator.cli.cmd.score_genes.ScoreGenesCommand;
import org.monarchinitiative.threes.autoconfigure.EnableThrees;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@SpringBootApplication
@EnableThrees
public class Main {

    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String EPILOG = "       くまくま━━━━━━ヽ（ That's it ）ノ━━━━━━ !!!";

    public static void main(String[] args) throws Exception {
        // 1. define CLI interface
        ArgumentParser parser = ArgumentParsers.newFor("java -jar counter-cli.jar").build();
        parser.description("Counter cli");

        // - we require threes configuration/properties
        parser.addArgument("-c", "--config")
                .required(true)
                .metavar("/path/to/application.properties")
                .help("path to Spring configuration file");

        Subparsers subparsers = parser.addSubparsers();
        ScoreGenesCommand.setupSubparsers(subparsers);

        parser.defaultHelp(true);
        parser.epilog(EPILOG);

        Namespace namespace = null;
        try {
            namespace = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(Paths.get(namespace.getString("config")))) {
            properties.load(is);
        }

        //  2. bootstrap the app
        try (ConfigurableApplicationContext appContext = new SpringApplicationBuilder(Main.class)
                .properties(properties)
                .run()) {

            // 3. get the selected command and run it
            Command command;
            String cmdName = namespace.get("cmd");
            switch (cmdName) {
                case "score-genes":
                    command = appContext.getBean(ScoreGenesCommand.class);
                    break;
                default:
                    LOGGER.warn("Unknown command '{}'", cmdName);
                    System.exit(1);
                    return; // unreachable, but still required
            }

            command.run(namespace);
            LOGGER.info("Done!");
        }
    }
}
