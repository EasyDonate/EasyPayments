package ru.soknight.easypayments.sdk.http;

import ru.soknight.easypayments.EasyPaymentsPlugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class HttpRequest {

    private static final int CONNECT_TIMEOUT = 5000;
    private static final int READ_TIMEOUT = 10000;

    private static final String CONTENT_TYPE;
    private static final String USER_AGENT;

    static {
        CONTENT_TYPE = "application/json";
        USER_AGENT = "EasyPayments " + EasyPaymentsPlugin.getVersion();
    }

    public static String get(String targetUrl) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            InputStream inputStream = connection.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            StringBuilder response = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            bufferedReader.close();
            return response.toString();
        } finally {
            if(connection != null)
                connection.disconnect();
        }
    }

    public static String post(String targetUrl, String content) throws IOException {
        HttpURLConnection connection = null;

        try {
            URL url = new URL(targetUrl);
            connection = (HttpURLConnection) url.openConnection();

            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Length", String.valueOf(content.getBytes().length));
            connection.setRequestProperty("Content-Type", CONTENT_TYPE);
            connection.setRequestProperty("User-Agent", USER_AGENT);

            connection.setConnectTimeout(CONNECT_TIMEOUT);
            connection.setReadTimeout(READ_TIMEOUT);

            OutputStream outputStream = connection.getOutputStream();
            OutputStreamWriter streamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
            streamWriter.write(content);
            streamWriter.flush();
            streamWriter.close();

            InputStream inputStream = connection.getInputStream();
            InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(streamReader);

            StringBuilder response = new StringBuilder();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }

            bufferedReader.close();
            return response.toString();
        } finally {
            if(connection != null)
                connection.disconnect();
        }
    }

}
