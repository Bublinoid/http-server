package ru.flamexander.http.server.processors;

import ru.flamexander.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CachingProcessor implements RequestProcessor {
    private static final String CACHED_RESPONSE = "<html><body><h1>Cached response for /cached</h1></body></html>";

    @Override
    public void execute(HttpRequest httpRequest, OutputStream output) throws IOException {
        output.write(CACHED_RESPONSE.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getMethod() {
        return "GET";
    }
}
