package easypayments.gradle.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@UtilityClass
public class Downloader {

    private static final Gson GSON = new GsonBuilder().create();

    public String readAsJson(String url) throws IOException {
        return new String(readAsBytes(url), StandardCharsets.UTF_8);
    }

    public <T> T readAsJson(String url, Class<T> modelType) throws IOException {
        return read(url, dataStream -> GSON.fromJson(new InputStreamReader(dataStream, StandardCharsets.UTF_8), modelType));
    }

    public byte[] readAsBytes(String url) throws IOException {
        return read(url, InputStream::readAllBytes);
    }

    public void readAsFile(String url, Path filePath) throws IOException {
        read(url, dataStream -> {
            if (!Files.isDirectory(filePath.getParent()))
                Files.createDirectories(filePath.getParent());

            try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                dataStream.transferTo(outputStream);
                outputStream.flush();
            }
            return null;
        });
    }

    public <T> T read(String url, ReadFunction<T> readFunction) throws IOException {
        if (URI.create(url).toURL().openConnection() instanceof HttpURLConnection connection) {
            try (InputStream dataStream = connection.getInputStream()) {
                return readFunction.read(dataStream);
            }
        } else {
            throw new UnsupportedOperationException("URL '%s' not supported!".formatted(url));
        }
    }

}
