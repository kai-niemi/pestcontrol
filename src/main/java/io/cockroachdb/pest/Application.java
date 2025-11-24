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
@EnableAspectJAutoProxy(proxyTargetClass = true)
@SpringBootApplication(exclude = {
        DataJdbcRepositoriesAutoConfiguration.class,
        DataSourceAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class
})
public class Application {
    private static void printHelpAndExit(Consumer<AnsiConsole> message) {
        try (Terminal terminal = TerminalBuilder.terminal()) {
            AnsiConsole console = new AnsiConsole(terminal)
                    .green("Usage: java -jar pestcontrol.jar [options] [arg...]").nl(2)
                    .yellow("Options include:").nl()
                    .cyan("--verbose                 enable the 'verbose' profile for detailed logging").nl()
                    .cyan("--verbose-http            enable the 'verbose-http' profile for HTTP trace logging").nl()
                    .cyan("--verbose-sql             enable the 'verbose-sql' profile for SQL trace logging").nl()
                    .cyan("--profiles [profile,..]   override spring profiles to activate").nl()
                    .cyan("--offline                 disable the REST API and web ui").nl()
                    .cyan("--cluster [id]            set default cluster id to use in shell commands").nl()
                    .cyan("--help                    this help").nl(2);
            message.accept(console);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        LinkedList<String> passThroughArgs = new LinkedList<>();

        Set<String> profiles = new HashSet<>();
        profiles.add("default");

        LinkedList<String> argsList = new LinkedList<>(Arrays.asList(args));
        while (!argsList.isEmpty()) {
            String arg = argsList.pop();
            if (arg.equals("--help")) {
                printHelpAndExit(ansiConsole -> {
                });
            } else if (arg.equals("--verbose")) {
                profiles.add(ProfileNames.verbose.name());
            } else if (arg.equals("--verbose-http")) {
                profiles.add(ProfileNames.verbose_http.name());
            } else if (arg.equals("--verbose-sql")) {
                profiles.add(ProfileNames.verbose_ssl.name());
            } else if (arg.equals("--offline")) {
                profiles.add(ProfileNames.offline.name());
            } else if (arg.equals("--profiles")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole -> ansiConsole.red("Expected comma-separated list of profile names"));
                }
                profiles.clear();
                profiles.addAll(StringUtils.commaDelimitedListToSet(argsList.pop()));
            } else if (arg.equals("--cluster")) {
                if (argsList.isEmpty()) {
                    printHelpAndExit(ansiConsole -> ansiConsole.red("Expected cluster ID"));
                }
                System.setProperty("application.defaultClusterId", argsList.pop());
            } else {
                passThroughArgs.add(arg);
            }
        }

        if (Files.exists(Path.of(".certs", "pestcontrol.p12"))) {
            System.out.println("Found certificate truststore - adding 'secure' profile");
            profiles.add(ProfileNames.secure.name());
        }

        System.setProperty("spring.profiles.active", String.join(",", profiles));

        new SpringApplicationBuilder(Application.class)
                .web(profiles.contains(ProfileNames.offline.name())
                        ? WebApplicationType.NONE : WebApplicationType.SERVLET)
                .logStartupInfo(true)
                .profiles(profiles.toArray(new String[0]))
                .run(passThroughArgs.toArray(new String[]{}));
    }
}
