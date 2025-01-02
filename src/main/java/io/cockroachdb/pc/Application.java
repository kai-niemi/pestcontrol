package io.cockroachdb.pc;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jetty.io.RuntimeIOException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.jdbc.JdbcRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StringUtils;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
public class Application implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    @Value("${server.port}")
    private String serverPort;

    private static void printHelpAndExit(Consumer<AnsiConsole> message) {
        try (Terminal terminal = TerminalBuilder.terminal()) {
            AnsiConsole console = new AnsiConsole(terminal);
            console.green("Usage: java -jar pestcontrol.jar [options] <profile> [args...]").nl().nl();
            console.cyan("Unrecognized options and arguments following <profile> "
                         + "are passed as arguments to the spring boot container.").nl().nl();
            console.cyan("Options include:").nl();
            {
                console.cyan("--help                    this help").nl();
                console.cyan("--verbose                 enable verbose SQL trace logging").nl();
                console.cyan("--profiles [profile,..]   override spring profiles to activate").nl();
            }
            console.nl();
            message.accept(console);
        } catch (IOException e) {
            throw new RuntimeIOException(e);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        LinkedList<String> passThroughArgs = new LinkedList<>();

        Set<String> profiles =
                StringUtils.commaDelimitedListToSet(System.getProperty("spring.profiles.active"));

        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.equals("--help")) {
                printHelpAndExit(ansiConsole -> {});
            } else if (arg.equals("--profiles")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole -> {
                        ansiConsole.red("Expected list of profile names");
                    });
                }
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(argsList.pop()));
            } else {
                if (arg.startsWith("--") || arg.startsWith("@")) {
                    passThroughArgs.add(arg);
                } else {
                    profiles.add(arg);
                }
            }
        }

        if (!profiles.isEmpty()) {
            System.setProperty("spring.profiles.active", String.join(",", profiles));
        }

        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET)
                .logStartupInfo(true)
                .profiles(profiles.toArray(new String[0]))
                .run(passThroughArgs.toArray(new String[] {}));
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info(
                "Welcome to Pest Control, see http://localhost:%s".formatted(serverPort));
    }
}
