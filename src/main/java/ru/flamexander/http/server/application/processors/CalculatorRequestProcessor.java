package ru.flamexander.http.server.application.processors;

import ru.flamexander.http.server.HttpRequest;
import ru.flamexander.http.server.processors.RequestProcessor;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

public class CalculatorRequestProcessor implements RequestProcessor {
    @Override
    public void execute(HttpRequest httpRequest, OutputStream output) throws IOException {
        String acceptHeader = httpRequest.getHeader("Accept");
        if (acceptHeader != null && !getSupportedContentTypes().contains(acceptHeader)) {
            String response = "HTTP/1.1 406 Not Acceptable\r\n\r\n<html><body><h1>406 Not Acceptable</h1></body></html>";
            output.write(response.getBytes(StandardCharsets.UTF_8));
            return;
        }

        int a = Integer.parseInt(httpRequest.getParameter("a"));
        int b = Integer.parseInt(httpRequest.getParameter("b"));
        int result = a + b;
        String outMessage = a + " + " + b + " = " + result;

        String response = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n\r\n<html><body><h1>" + outMessage + "</h1></body></html>";
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getMethod() {
        return "GET";
    }

    @Override
    public Set<String> getSupportedContentTypes() {
        return Collections.singleton("text/html");
    }
}
