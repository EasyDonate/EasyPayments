package ru.easydonate.easypayments.core.logging;

import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public final class DebugLogger {

    static final DateTimeFormatter LOG_TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yy HH:mm:ss");

    private static final char LEVEL_DEBUG = 'D';
    private static final char LEVEL_INFO = 'I';
    private static final char LEVEL_WARN = 'W';
    private static final char LEVEL_ERROR = 'E';

    private final Path pluginDir;
    private final Path logsDir;
    private final Path logFile;
    private volatile boolean working;

    public DebugLogger(Plugin plugin) {
        this.pluginDir = plugin.getDataFolder().toPath();
        this.logsDir = pluginDir.resolve("logs");
        this.logFile = findFreeFileName(logsDir);
        this.working = true;

        compressLogFiles();
        DebugEnvironmentLookup.writeEnvironmentInfo(plugin, this);
    }

    public void compressLogFiles() {
        if (!Files.isDirectory(logsDir))
            return;

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(logsDir, "debug-*.log")) {
            dirStream.forEach(this::compressLogFile);
        } catch (IOException ignored) {
        }
    }

    public void shutdown() {
        this.working = false;
        compressLogFile(logFile);
    }

    public DebugLogger debug(String message, Object... args) {
        return logMessage(LEVEL_DEBUG, message, args);
    }

    public DebugLogger debug(String[] messages) {
        return log(LEVEL_DEBUG, messages);
    }

    public DebugLogger info(String message, Object... args) {
        return logMessage(LEVEL_INFO, message, args);
    }

    public DebugLogger info(String[] messages) {
        return log(LEVEL_INFO, messages);
    }

    public DebugLogger warn(String message, Object... args) {
        return logMessage(LEVEL_WARN, message, args);
    }

    public DebugLogger warn(String[] messages) {
        return log(LEVEL_WARN, messages);
    }

    public DebugLogger error(String message, Object... args) {
        return logMessage(LEVEL_ERROR, message, args);
    }

    public DebugLogger error(String[] messages) {
        return log(LEVEL_ERROR, messages);
    }

    public DebugLogger debug(Throwable throwable) {
        return logStacktrace(LEVEL_DEBUG, throwable);
    }

    public DebugLogger info(Throwable throwable) {
        return logStacktrace(LEVEL_INFO, throwable);
    }

    public DebugLogger warn(Throwable throwable) {
        return logStacktrace(LEVEL_WARN, throwable);
    }

    public DebugLogger error(Throwable throwable) {
        return logStacktrace(LEVEL_ERROR, throwable);
    }

    private DebugLogger logMessage(char level, String message, Object... args) {
        if (!working)
            return this;

        message = args != null && args.length != 0 ? MessageFormat.format(message.replace("'", "''"), args) : message;
        return log(level, message);
    }

    private DebugLogger logStacktrace(char level, Throwable throwable) {
        if (!working)
            return this;

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter, true);
        throwable.printStackTrace(printWriter);
        return log(level, stringWriter.toString().split("\n"));
    }

    private synchronized DebugLogger log(char level, String... content) {
        if (!working)
            return this;

        String timestamp = LOG_TIMESTAMP_FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC));
        List<String> lines = new ArrayList<>();

        for (String line : content) {
            if (line == null || line.isEmpty())
                continue;

            lines.add('[' + timestamp + "] [" + level + "] " + line);
        }

        if (!lines.isEmpty()) {
            writeToFile(lines);
        }

        return this;
    }

    synchronized void writeToFile(List<String> content) {
        if (!working)
            return;

        if (!Files.isDirectory(logFile.getParent())) {
            try {
                Files.createDirectories(logFile.getParent());
            } catch (IOException ignored) {
                return;
            }
        }

        try {
            Files.write(logFile, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }

    private void compressLogFile(Path filePath) {
        if (working && filePath.getFileName().equals(logFile.getFileName()))
            return;

        if (!Files.isRegularFile(filePath))
            return;

        Path compressedFile = filePath.getParent().resolve(filePath.getFileName() + ".gz");
        try (
                InputStream sourceStream = Files.newInputStream(filePath);
                OutputStream outputStream = Files.newOutputStream(compressedFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream)
        ) {
            byte[] buffer = new byte[1024];
            int read;

            do {
                read = sourceStream.read(buffer);
                if (read > 0) {
                    gzipOutputStream.write(buffer, 0, read);
                }
            } while (read > 0);

            gzipOutputStream.flush();
        } catch (IOException ignored) {
            return;
        }

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }
    }

    private static Path findFreeFileName(Path directory) {
        String timestamp = DateTimeFormatter.ISO_LOCAL_DATE.format(LocalDate.now(ZoneOffset.UTC));
        String baseName = String.format("debug-%s-%%d.log", timestamp);

        Path result;
        int logNumber = 1;

        do {
            result = directory.resolve(String.format(baseName, logNumber++));
        } while (Files.exists(result) || Files.exists(directory.resolve(result.getFileName() + ".gz")));

        return result;
    }

}
