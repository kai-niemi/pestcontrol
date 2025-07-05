package io.cockroachdb.pest.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.util.StreamUtils;

public abstract class ProcessUtils {
    private ProcessUtils() {
    }

    public static final int PROCESS_TIMEOUT_SECONDS = 30;

    private static final Logger logger = LoggerFactory.getLogger(ProcessUtils.class);

    public static Pair<String, Integer> executeCommand(Path directory, List<String> commands) {
        Instant start = Instant.now();

        try {
            logger.info("Starting process:\n\t%s".formatted(String.join("\n\t", commands)));

            Process process = new ProcessBuilder()
                    .command(commands)
                    .directory(directory.toFile())
                    .inheritIO()
                    .start();

            ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
            ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

            try (InputStream inputStream = process.getInputStream();
                 InputStream errorStream = process.getErrorStream()) {
                StreamUtils.copy(inputStream, stdOut);
                StreamUtils.copy(errorStream, stdErr);
            }

            while (process.isAlive()) {
                if (process.waitFor(PROCESS_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    logger.warn("Process terminated: %s".formatted(process.info()));
                    break;
                } else {
                    logger.info("Process running (waiting): %s".formatted(process.info()));
                }
            }

            int code = process.exitValue();

            String output = StreamUtils.copyToString(stdOut, Charset.defaultCharset());
            String error = StreamUtils.copyToString(stdErr, Charset.defaultCharset());

            logger.info("Finished process in %s with exit code: %d, output: [%s], error: [%s]"
                    .formatted(Duration.between(start, Instant.now()), code, output, error));

            if (code != 0) {
                throw new CommandException("Process error code " + code, code);
            }

            return Pair.of(output, code);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandException("Timeout waiting for process completion", e);
        } catch (IOException e) {
            throw new CommandException("Process I/O error", e);
        }
    }
}
