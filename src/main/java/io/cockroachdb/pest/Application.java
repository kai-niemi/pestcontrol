package io.cockroachdb.pest;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.Consumer;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.data.jdbc.autoconfigure.DataJdbcRepositoriesAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StringUtils;

import io.cockroachdb.pest.shell.support.AnsiConsole;

@EnableConfigurationProperties
@ConfigurationPropertiesScan
@EnableAspectJAutoProxy
@SpringBootApplication(exclude = {
        DataJdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
public class Application implements DisposableBean {
    private static void printHelpAndExit(Consumer<AnsiConsole> message) {
        try (Terminal terminal = TerminalBuilder.terminal()) {
            AnsiConsole console = new AnsiConsole(terminal.writer())
                    .green("Usage: java -jar pestcontrol.jar [options] [arg...]")
                    .nl()
                    .nl()
                    .yellow("Profile Options:")
                    .nl()
                    .cyan("--verbose-http            enable 'verbose-http' profile for HTTP trace logging")
                    .cyan("--verbose-sql             enable 'verbose-sql' profile for SQL trace logging")
                    .cyan("--secure                  enable 'secure' profile")
                    .cyan("--offline                 disable HTML front-end")
                    .cyan("--profiles [profile,..]   override spring profiles to activate")
                    .nl()
                    .yellow("Other Options:")
                    .cyan("--cluster [id]            set default cluster id to use in shell commands")
                    .cyan("--help                    this help")
                    .nl();
            message.accept(console);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        LinkedList<String> passThroughArgs = new LinkedList<>();

        Set<String> profiles = new HashSet<>();

        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.equals("--help")) {
                printHelpAndExit(ansiConsole -> {
                });
            } else if (arg.equals("--verbose-http")) {
                profiles.add(ProfileNames.VERBOSE_HTTP);
            } else if (arg.equals("--verbose-sql")) {
                profiles.add(ProfileNames.VERBOSE_SQL);
            } else if (arg.equals("--offline")) {
                profiles.add(ProfileNames.ONLINE);
            } else if (arg.equals("--dev")) {
                profiles.add(ProfileNames.DEV);
            } else if (arg.equals("--profiles")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole ->
                            ansiConsole.red("Expected comma-separated list of profile names"));
                }
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(argsList.pop()));
            } else if (arg.equals("--cluster")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole ->
                            ansiConsole.red("Expected cluster ID"));
                }
                System.setProperty("application.defaultClusterId", argsList.pop());
            } else {
                passThroughArgs.add(arg);
            }
        }

        if (Files.exists(Path.of(".certs", "pestcontrol.p12"))) {
            if (profiles.isEmpty()) {
                profiles.add(ProfileNames.DEFAULT);
                System.out.println("Adding '%s' profile".formatted(ProfileNames.DEFAULT));
            }
            profiles.add(ProfileNames.SECURE);
            System.out.println("Adding '%s' profile".formatted(ProfileNames.SECURE));
        }

        if (!passThroughArgs.isEmpty()) {
            System.setProperty("spring.shell.interactive.enabled", "false");
        }

        if (!profiles.isEmpty()) {
            System.setProperty("spring.profiles.active", String.join(",", profiles));
        }

//        System.setProperty("spring.shell.debug.enabled", "true");
//        System.out.printf("Spring profiles: %s%n", String.join(",", profiles));
//        System.out.printf("Passthrough args: %s%n", String.join(",", passThroughArgs));

        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET)
                .logStartupInfo(true)
                .profiles(profiles.toArray(new String[0]))
                .run(passThroughArgs.toArray(new String[] {}));
    }

    @Override
    public void destroy() {
        System.exit(0);
    }
}
