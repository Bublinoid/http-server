package ru.flamexander.http.server.processors;

import ru.flamexander.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DefaultStaticResourcesProcessor implements RequestProcessor {
    private Map<String, byte[]> cache = new HashMap<>();

    @Override
    public void execute(HttpRequest httpRequest, OutputStream output) throws IOException {
        String filename = httpRequest.getUri().substring(1);
        byte[] fileData;

        if (cache.containsKey(filename)) {
            fileData = cache.get(filename);
        } else {
            Path filePath = Paths.get("static/", filename);
            if (Files.exists(filePath)) {
                fileData = Files.readAllBytes(filePath);
                cache.put(filename, fileData);
            } else {
                sendErrorResponse(output, 404, "Not Found");
                return;
            }
        }

        String fileType = filename.substring(filename.lastIndexOf(".") + 1);
        String contentDisposition = "";
        if (fileType.equals("pdf")) {
            contentDisposition = "Content-Disposition: attachment;filename=" + filename + "\r\n";
        }

        String response = "HTTP/1.1 200 OK\r\n" +
                "Content-Length: " + fileData.length + "\r\n" +
                contentDisposition +
                "\r\n";
        output.write(response.getBytes());
        output.write(fileData);
    }

    private void sendErrorResponse(OutputStream output, int statusCode, String message) throws IOException {
        String response = String.format("HTTP/1.1 %d %s\r\n\r\n<html><body><h1>%d %s</h1></body></html>", statusCode, message, statusCode, message);
        output.write(response.getBytes());
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
