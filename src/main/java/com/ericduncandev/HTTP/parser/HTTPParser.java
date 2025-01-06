package com.ericduncandev.HTTP.parser;

import com.ericduncandev.HTTP.interfaces.IHTTPParser;
import com.ericduncandev.HTTP.model.HTTPRequest;
import com.ericduncandev.HTTP.factory.ResponseFactory;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HTTPParser implements IHTTPParser {
    private final BufferedReader br;
    private final PrintWriter pw;
    private final Logger logger = LogManager.getLogger(HTTPParser.class);

    public HTTPParser(BufferedReader in, PrintWriter out) {
        logger.trace("Initializing HTTP Reader");
        br = in;
        pw = out;
    }

    public void parseRequest() {
        try {
            logger.trace("Parsing request...");
            String requestLine = br.readLine();
            if (requestLine == null || requestLine.trim().isEmpty()) {
                logger.warn("Received an empty or null request line. Closing connection.");
                return;
            }

            String[] request = requestLine.split(" ");
            if (request.length != 3) {
                logger.warn("Invalid request line format: {}", requestLine);
                ResponseFactory.badRequest("Invalid request line format").writeTo(pw);
                return;
            }

            logger.debug("Request line: {}", requestLine);
            String method = request[0];
            String uri = request[1];
            String protocolVersion = request[2];


            // Parse headers
            Map<String, String> headers = new HashMap<>();
            String header;
            while ((header = br.readLine()) != null && !header.isEmpty()) {
                int separatorIndex = header.indexOf(": ");
                if (separatorIndex != -1) {
                    String headerTitle = header.substring(0, separatorIndex).trim();
                    String headerValue = header.substring(separatorIndex + 2).trim();
                    headers.put(headerTitle, headerValue);
                } else {
                    logger.warn("Malformed header: {}", header);
                }
            }

            // Parse body if it exists
            String body = null;
            String contentLengthStr = headers.get("Content-Length");
            if (contentLengthStr != null) {
                try {
                    int contentLength = Integer.parseInt(contentLengthStr);
                    if (contentLength > 0) {
                        char[] bodyChars = new char[contentLength];
                        int bytesRead = br.read(bodyChars, 0, contentLength);
                        if (bytesRead == contentLength) {
                            body = new String(bodyChars);
                            logger.debug("Request body: {}", body);
                        } else {
                            logger.warn("Incomplete body read. Expected {} bytes, got {}", contentLength, bytesRead);
                            ResponseFactory.badRequest("Incomplete body read").writeTo(pw);
                            return;
                        }
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Invalid Content-Length header: {}", contentLengthStr);
                    ResponseFactory.badRequest("Invalid Content-Length header").writeTo(pw);
                    return;
                }
            }

            handleDispatcher(new HTTPRequest(method, uri, protocolVersion, headers, body));

        } catch (IOException e) {
            logger.error("Error parsing request", e);
            ResponseFactory.serverError("Error parsing request: %s".formatted(e.getMessage())).writeTo(pw);
        }
    }

    private void handleDispatcher(HTTPRequest req) {
        try {
            switch (req.method()) {
                case "GET":
                    handleGet(req);
                    break;
                case "POST":
                    handlePost(req);
                    break;
                case "PUT":
                    handlePut(req);
                    break;
                case "DELETE":
                    handleDelete(req);
                    break;
                default:
                    logger.warn("Unsupported HTTP method: {}", req.method());

            }
        } catch (IOException e) {
            logger.error("Error handling {} request", req.method());
            ResponseFactory.serverError("Error handling request").writeTo(pw);
        } catch (SecurityException e) {
            logger.error("Security exception handling {} request", req.method());
            ResponseFactory.forbidden("Access denied").writeTo(pw);
        }
    }

    private String getContentType(String fileName) {
        if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
            return "text/html";
        } else if (fileName.endsWith(".xml")) {
            return "application/xml";
        } else if (fileName.endsWith(".json")) {
            return "application/json";
        } else if (fileName.endsWith(".css")) {
            return "text/css";
        } else if (fileName.endsWith(".js")) {
            return "application/javascript";
        } else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (fileName.endsWith(".png")) {
            return "image/png";
        } else {
            return "application/octet-stream"; // Default binary content type
        }
    }

    // START GET REQUEST HANDLING

    private void handleGet(HTTPRequest req) throws IOException {
        logger.info("Handling GET request for URI: {}", req.uri());
        File location = new File("." + req.uri());
        if (location.isDirectory()) {
            File indexFile = new File(location, "index.html");
            if (indexFile.exists()) {
                byte[] body = FileUtils.readFileToByteArray(indexFile);
                ResponseFactory.ok(body, "text/html").writeTo(pw);
            } else {
                ResponseFactory.notFound().writeTo(pw);
            }
            return;
        } else if (location.isFile()) {
            // Handle file
            byte[] body = FileUtils.readFileToByteArray(location);
            String contentType = getContentType(location.getName());
            ResponseFactory.ok(body, contentType).writeTo(pw);
        } else {
            ResponseFactory.notFound().writeTo(pw);
        }
    }

    private void handlePost(HTTPRequest req) throws IOException {
        logger.info("Handling POST request from {}", req.uri());
        File location = new File(".%s".formatted(req.uri()));
        if (!location.exists()) {
            ResponseFactory.notFound();
            return;
        }

        String contentType = req.headers().get("Content-Type");
        switch (contentType) {
            case "application/x-www-form-urlencoded":
                handleUrlEncodedRequest(req, location);
                break;
            case "application/json":
                handleJsonRequest(req, location);
                break;
            // rare usage of 'when' keyword in java
            case String _ when contentType.contains("multipart/form-data"): // if contentType contains "multipart/form-data"
                handleMultipartRequest(req, location, extractBoundary(contentType));
                break;
            default:
                ResponseFactory.badRequest("Unsupported POST operation").writeTo(pw);
                break;
        }
    }

    private String extractBoundary(String contentType) {
        int boundaryIndex = contentType.indexOf("boundary=");
        if (boundaryIndex != -1) {
            return contentType.substring(boundaryIndex + 9).trim();
        }
        return null;
    }

    private void handleUrlEncodedRequest(HTTPRequest req, File location) throws IOException {
        logger.info("Posting x-www-form-urlencoded request of {}", req.body());
        File formData = createUniqueFile("file", "txt");

        if (formData != null) {
            FileUtils.writeByteArrayToFile(formData, req.body().getBytes());
            JSONObject jsonForm = new JSONObject();
            for (String s : req.body().split("&")) {
                jsonForm.put(s.split("=")[0], s.split("=")[1]);
            }
            ResponseFactory.created(jsonForm.toString().getBytes(), "application/json", location.getPath()).writeTo(pw);
        } else {
            logger.error("Failed to create file for form data");
            ResponseFactory.serverError("Failed to create file for form data").writeTo(pw);
        }
    }

    private void handleJsonRequest(HTTPRequest req, File location) {
        logger.info("Posting json request of {}", req.body());
        createUniqueFile("file", "json");
    }

    private void handleMultipartRequest(HTTPRequest req, File location, String boundary) throws IOException {
        if (boundary == null) {
            logger.error("Missing boundary in multipart/form-data request");
            JSONObject errorJson = new JSONObject();
            errorJson.put("error", "Bad request");
            errorJson.put("message", "Missing boundary in multipart/form-data request");
            ResponseFactory.badRequest(errorJson.toString().getBytes(), "application/json").writeTo(pw);
            return;
        }

        logger.info("Found boundary: {}", boundary);
        Map<String, Object> processedData = processMultipartData(req.body(), boundary, location);
        JSONObject responseJson = createMultipartResponse(processedData);

        File fieldInfo = createUniqueFile("fieldInfo", "json");
        if (fieldInfo != null) {
            FileUtils.writeByteArrayToFile(fieldInfo, new JSONObject(processedData.get("formFields")).toString(2).getBytes());
            ResponseFactory.created(responseJson.toString().getBytes(), "application/json", location.getPath()).writeTo(pw);
        } else {
            logger.error("Failed to create file for form data");
            ResponseFactory.serverError("Failed to create file for form data").writeTo(pw);
        }
    }

    private JSONObject createMultipartResponse(Map<String, Object> processedData) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("message", "Files uploaded successfully");
        jsonObject.put("files", processedData.get("files"));
        jsonObject.put("formData", processedData.get("formFields"));
        return jsonObject;
    }

    private Map<String, Object> processMultipartData(String body, String boundary, File uploadDir) {
        String fullBoundary = "--%s".formatted(boundary);
        String[] parts = body.split(Pattern.quote(fullBoundary));

        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        Map<String, String> formFields = new HashMap<>();
        List<Map<String, String>> uploadedFiles = new ArrayList<>();

        // Skip the first empty part and process each subsequent part
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.trim().equals("--")) {
                continue;
            }

            String[] headerAndContent = part.split("\r\n\r\n", 2);
            if (headerAndContent.length == 2) {
                String headers = headerAndContent[0].trim();
                String content = headerAndContent[1].trim();

                String[] headerLines = headers.split("\r\n");
                for (String headerLine : headerLines) {
                    if (headerLine.startsWith("Content-Disposition:")) {
                        String name = extractFormField(headerLine, "name");
                        String filename = extractFormField(headerLine, "filename");

                        if (filename != null) {
                            Map<String, String> fileInfo = handleFileUpload(content, filename, uploadDir);
                            uploadedFiles.add(fileInfo);
                        } else {
                            handleFormField(formFields, name, content);
                        }
                    }
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("files", uploadedFiles);
        result.put("formFields", formFields);
        return result;
    }

    private String extractFormField(String header, String fieldName) {
        Pattern pattern = Pattern.compile("%s=\"([^\"]+)\"".formatted(fieldName));
        Matcher matcher = pattern.matcher(header);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    private Map<String, String> handleFileUpload(String content, String filename, File uploadDir) {
        filename = new File(filename).getName();

        File file = new File(uploadDir, filename);
        int counter = 1;
        while (file.exists()) {
            String name;
            int dotIndex = filename.lastIndexOf('.');
            if (dotIndex != -1) {
                name = MessageFormat.format("{0}_{1}{2}",
                        filename.substring(0, dotIndex), counter, filename.substring(dotIndex));
            } else {
                name = "%s_%d".formatted(filename, counter);
            }
            file = new File(uploadDir, name);
            counter++;
        }

        content = content.replaceAll("\r\n$", "");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        } catch (IOException e) {
            logger.error("Error writing to file", e);
            ResponseFactory.serverError("Internal error uploading file").writeTo(pw);
        }

        Map<String, String> fileInfo = new HashMap<>();
        fileInfo.put("originalName", filename);
        fileInfo.put("savedName", file.getName());
        fileInfo.put("path", file.getAbsolutePath());
        fileInfo.put("size", String.valueOf(content.length()));

        logger.info("File saved successfully: {}", file.getAbsolutePath());
        return fileInfo;
    }

    private void handleFormField(Map<String, String> formFields, String name, String value) {
        if (name != null && !name.trim().isEmpty()) {
            formFields.put(name, value);
            logger.info("Processed form field - {}: {}", name, value);
        }
    }

    private File createUniqueFile(String fileName, String fileType) {
        for (int count = 0; count <= 100; count++) {  // Safety limit of 100 attempts
            String suffix = count == 0 ? "" : String.valueOf(count);
            File f = new File("%s%s.%s".formatted(fileName, suffix, fileType));

            try {
                if (f.createNewFile()) {  // Returns true if file was created, false if it exists
                    return f;
                }
            } catch (IOException e) {
                logger.error("Error creating file:", e);
                return null;
            }
        }

        logger.error("Too many duplicate files");
        return null;
    }

    private void handlePut(HTTPRequest req) throws IOException, SecurityException {
        logger.trace("Handling PUT request...");
        File fileLocation = new File(".%s".formatted(req.uri()));
        logger.info("PUT at location {}", fileLocation.getPath());

        // Ensure parent directories exist
        File parent = fileLocation.getParentFile();
        if (parent != null && !parent.exists()) {
            if (!parent.mkdirs()) {
                logger.error("Failed to create parent directories for {}", fileLocation.getPath());
                ResponseFactory.serverError("Failed to create directory structure").writeTo(pw);
                return;
            }
        }

        // Write the file in one operation
        FileUtils.writeByteArrayToFile(fileLocation, req.body().getBytes());

        // Log appropriate message based on whether file existed
        if (fileLocation.length() == req.body().getBytes().length) {
            logger.info("File {} successfully {}", fileLocation.getPath(),
                    fileLocation.length() == req.body().getBytes().length ? "created" : "updated");
            ResponseFactory.noContent(fileLocation.getPath()).writeTo(pw);
        }
    }

    private void handleDelete(HTTPRequest req) throws SecurityException {
        logger.trace("Handling DELETE request");
        File fileLocation = new File(".%s".formatted(req.uri()));
        if (fileLocation.exists()) {
            if (fileLocation.isFile()) {
                if (fileLocation.delete()) {
                    logger.info("File {} successfully deleted", fileLocation.getPath());
                    ResponseFactory.noContent(fileLocation.getPath()).writeTo(pw);
                } else {
                    ResponseFactory.forbidden("Access is denied").writeTo(pw);
                }
            } else {
                ResponseFactory.badRequest("Cannot delete directory using DELETE request").writeTo(pw);
            }
        } else {
            ResponseFactory.notFound();
        }
    }

//    private void saveFormMetadata(Map<String, String> formFields, File uploadDir) {
//        File metadataFile = new File(uploadDir, "form_metadata.json");
//        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile))) {
//            // Create a simple JSON format
//            writer.write("{\n");
//            Iterator<Map.Entry<String, String>> it = formFields.entrySet().iterator();
//            while (it.hasNext()) {
//                Map.Entry<String, String> entry = it.next();
//                writer.write(String.format("  \"%s\": \"%s\"%s\n",
//                        escapeJson(entry.getKey()),
//                        escapeJson(entry.getValue()),
//                        it.hasNext() ? "," : ""));
//            }
//            writer.write("}");
//        } catch (IOException e) {
//            logger.error("Failed to save form metadata", e);
//        }
//        logger.info("Form metadata saved to: {}", metadataFile.getAbsolutePath());
//    }

    //    private String escapeJson(String input) {
//        return input.replace("\\", "\\\\")
//                .replace("\"", "\\\"")
//                .replace("\n", "\\n")
//                .replace("\r", "\\r")
//                .replace("\t", "\\t");
//    }
}
