package ru.flamexander.http.server.processors;

import ru.flamexander.http.server.HttpRequest;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

public interface RequestProcessor {
    void execute(HttpRequest httpRequest, OutputStream output) throws IOException;

    default String getMethod() {
        return "GET";
    }

    default Set<String> getSupportedContentTypes() {
        return Collections.singleton("text/html");
    }
}
