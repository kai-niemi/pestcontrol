package io.cockroachdb.pest;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

import org.eclipse.jetty.io.RuntimeIOException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
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

import io.cockroachdb.pest.shell.support.AnsiConsole;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = {
        JdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
public class Application {
    private static void printHelpAndExit(Consumer<AnsiConsole> message) {
        try (Terminal terminal = TerminalBuilder.terminal()) {
            AnsiConsole console = new AnsiConsole(terminal);
            console.green("Usage: java -jar pestcontrol.jar [options] [args...]").nl().nl();
            console.yellow("Options include:").nl();
            {
                console.cyan("--cluster-id [id]         set default cluster id to use in shell commands").nl();
                console.cyan("--profiles [profile,..]   override spring profiles to activate").nl();
                console.cyan("--secure                  enables the 'ssl' profile for secure clusters").nl();
                console.cyan("--verbose                 enables the 'verbose' profile for extensive logging").nl();
                console.cyan("--verbose-http            enables the 'verbose-http' profile for HTTP trace logging")
                        .nl();
                console.cyan("--verbose-sql             enables the 'verbose-sql' profile for SQL trace logging").nl();
                console.cyan("--help                    this help").nl();
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
                printHelpAndExit(ansiConsole -> {
                });
            } else if (arg.equals("--secure") | arg.equals("--ssl")) {
                profiles.add("ssl");
            } else if (arg.equals("--verbose")) {
                profiles.add("verbose");
            } else if (arg.equals("--verbose-http")) {
                profiles.add("verbose-http");
            } else if (arg.equals("--verbose-sql")) {
                profiles.add("verbose-sql");
            } else if (arg.equals("--profiles")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole -> {
                        ansiConsole.red("Expected list of profile names");
                    });
                }
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(argsList.pop()));
            } else if (arg.equals("--cluster-id")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole -> {
                        ansiConsole.red("Expected cluster ID");
                    });
                }
                System.setProperty("application.defaultClusterId", argsList.pop());
            } else {
                if (arg.startsWith("--") || arg.startsWith("@")) {
                    passThroughArgs.add(arg);
                } else {
                    printHelpAndExit(ansiConsole -> {
                    });
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
}
