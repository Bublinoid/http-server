package ru.flamexander.http.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.flamexander.http.server.application.processors.*;
import ru.flamexander.http.server.processors.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Dispatcher {
    private final Map<String, RequestProcessor> router;
    private final RequestProcessor unknownOperationRequestProcessor;
    private final RequestProcessor optionsRequestProcessor;
    private final RequestProcessor staticResourcesProcessor;

    private static final Logger logger = LoggerFactory.getLogger(Dispatcher.class.getName());

    public Dispatcher() {
        this.router = new HashMap<>();
        this.router.put("GET /calc", new CalculatorRequestProcessor());
        this.router.put("GET /hello", new HelloWorldRequestProcessor());
        this.router.put("GET /items", new GetAllProductsProcessor());
        this.router.put("POST /items", new CreateNewProductProcessor());
        this.router.put("GET /cached", new CachingProcessor());

        this.unknownOperationRequestProcessor = new DefaultUnknownOperationProcessor();
        this.optionsRequestProcessor = new DefaultOptionsProcessor();
        this.staticResourcesProcessor = new DefaultStaticResourcesProcessor();

        logger.info("Dispatcher initialized");
    }

    public void execute(HttpRequest httpRequest, OutputStream outputStream) throws IOException {

        if (httpRequest.getMethod() == HttpMethod.OPTIONS) {
            optionsRequestProcessor.execute(httpRequest, outputStream);
            return;
        }

        if (Files.exists(Paths.get("static/", httpRequest.getUri().substring(1)))) {
            staticResourcesProcessor.execute(httpRequest, outputStream);
            return;
        }

        String routeKey = httpRequest.getRouteKey();
        RequestProcessor processor = router.get(routeKey);

        if (processor == null) {

            String path = httpRequest.getUri();
            if (router.keySet().stream().anyMatch(key -> key.endsWith(" " + path))) {
                sendErrorResponse(outputStream, 405, "Method Not Allowed");
            } else {
                unknownOperationRequestProcessor.execute(httpRequest, outputStream);
            }
            return;
        }

        if (!processor.getMethod().equals(httpRequest.getMethod().name())) {
            sendErrorResponse(outputStream, 405, "Method Not Allowed");
            return;
        }

        String acceptHeader = httpRequest.getHeader("Accept");
        if (acceptHeader != null && !acceptHeader.contains("text/html") && !acceptHeader.contains("*/*")) {
            sendErrorResponse(outputStream, 406, "Not Acceptable");
            return;
        }

        logger.info("Session ID before setting cookie: {}", httpRequest.getSessionId());

        StringBuilder responseHeaders = new StringBuilder();
        responseHeaders.append("HTTP/1.1 200 OK\r\n");
        responseHeaders.append("Content-Type: text/html\r\n");


        String cookieHeader = httpRequest.getHeader("Cookie");
        if (cookieHeader == null || !cookieHeader.contains("SESSIONID")) {
            String setCookieHeader = "Set-Cookie: SESSIONID=" + httpRequest.getSessionId() + "; Path=/; HttpOnly\r\n";
            logger.info("Setting new SESSIONID: {}", httpRequest.getSessionId());
            responseHeaders.append(setCookieHeader);
        } else {
            logger.info("Preserving existing SESSIONID: {}", httpRequest.getSessionId());
        }

        responseHeaders.append("\r\n");
        outputStream.write(responseHeaders.toString().getBytes(StandardCharsets.UTF_8));


        logger.info("Response headers sent:\n{}", responseHeaders);
        processor.execute(httpRequest, outputStream);
        logger.info("Session ID after setting cookie: {}", httpRequest.getSessionId());
    }

    private void sendErrorResponse(OutputStream outputStream, int statusCode, String message) throws IOException {
        String response = String.format("HTTP/1.1 %d %s\r\n\r\n<html><body><h1>%d %s</h1></body></html>", statusCode, message, statusCode, message);
        outputStream.write(response.getBytes(StandardCharsets.UTF_8));
    }
}
