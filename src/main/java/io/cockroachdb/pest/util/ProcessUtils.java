package io.cockroachdb.pest.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StreamUtils;

public abstract class ProcessUtils {
    private ProcessUtils() {
    }

    public static final int PROCESS_TIMEOUT_SECONDS = 30;

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    public static String executeCommand(Path directory, List<String> args) {
        ByteArrayOutputStream barr = new ByteArrayOutputStream();
        int code = executeCommand(directory, args, barr);
        if (code != 0) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), code);
        }
        return StreamUtils.copyToString(barr, Charset.defaultCharset());
    }

    private static int executeCommand(Path directory,
                                     List<String> commands,
                                     ByteArrayOutputStream barr) {
        Instant start = Instant.now();

        try {
            logger.info("Starting process:\n\t%s".formatted(String.join("\n\t", commands)));

            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(directory.toFile())
                    .inheritIO()
                    .start();

            if (process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                logger.warn("Started process (terminated): %s".formatted(process.info()));
            } else {
                logger.info("Started process (waiting): %s".formatted(process.info()));
            }

            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream()) {
                IoUtils.copy(inputStream, barr);
                IoUtils.copy(errorStream, barr);
            }

            int code = process.exitValue();

            logger.info("Finished process in %s exit code %d: %s"
                    .formatted(Duration.between(start, Instant.now()), code, process.info()));

            return code;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Timeout waiting for process completion", e);
        } catch (IOException e) {
            throw new CommandException(StreamUtils.copyToString(barr, Charset.defaultCharset()), e);
        }
    }
}
