package easypayments.gradle.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.gradle.api.GradleException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@UtilityClass
public class Downloader {

    private static final Gson GSON = new GsonBuilder().create();
    private static final HttpClient HTTP_CLIENT = initializeHttpClient();
    private static final Lock HTTP_CLIENT_LOCK = new ReentrantLock();

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
            Files.createDirectories(filePath.getParent());
            try (var stream = Files.newOutputStream(filePath, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                dataStream.transferTo(stream);
                stream.flush();
                return null;
            }
        });
    }

    public <T> T read(String url, ReadFunction<T> readFunction) throws IOException {
        var request = HttpRequest.newBuilder(URI.create(url)).GET()
                .timeout(Duration.ofSeconds(5L))
                .build();

        try {
            HTTP_CLIENT_LOCK.lock();

            int retryAttempt = 0;
            while (retryAttempt < 5) {
                try {
                    var response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofInputStream());
                    if (response.statusCode() / 100 != 2)
                        throw new IOException("couldn't execute %s request to '%s': response code is %d".formatted(
                                request.method(),
                                request.uri().toString(),
                                response.statusCode()
                        ));

                    try (var body = response.body()) {
                        return readFunction.read(body);
                    }
                } catch (HttpTimeoutException ex) {
                    log.error("Request timed out, url: '{}'", request.uri());
                    retryAttempt++;
                } catch (InterruptedException ignored) {
                    return null;
                }
            }

            throw new GradleException("request timed out, url '%s' (no attempts left)".formatted(request.uri()));
        } finally {
            HTTP_CLIENT_LOCK.unlock();
        }
    }

    private HttpClient initializeHttpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10L))
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .proxy(resolveProxySelector())
                .build();
    }

    private ProxySelector resolveProxySelector() {
        var proxyHost = System.getProperty("https.proxyHost", System.getProperty("http.proxyHost"));
        var proxyPort = System.getProperty("https.proxyPort", System.getProperty("http.proxyPort"));
        if (proxyHost == null && proxyPort == null)
            return ProxySelector.getDefault();

        try {
            return ProxySelector.of(new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort)));
        } catch (Exception ignored) {
            return ProxySelector.getDefault();
        }
    }

}
