package com.metastrephe.app.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class HarConversionService {

    private static final String[] HEADER = new String[]{
            "url", "host", "step1", "step2", "step3", "step4", "step5", "método", "Payload", "response"
    };

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] convertHarToCsv(InputStream inputStream) throws IOException {
        JsonNode root = objectMapper.readTree(inputStream);
        JsonNode entries = root.path("log").path("entries");

        try (StringWriter writer = new StringWriter();
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT
                     .withHeader(HEADER)
                     .withDelimiter('|')
                     .withQuote('"')
                     .withRecordSeparator("\n"))) {

            if (entries.isArray()) {
                Iterator<JsonNode> iterator = entries.elements();
                while (iterator.hasNext()) {
                    JsonNode entry = iterator.next();
                    JsonNode requestNode = entry.path("request");
                    String url = requestNode.path("url").asText("");
                    if (shouldSkipUrl(url)) {
                        continue;
                    }
                    List<String> record = createRecord(entry, requestNode, url);
                    printer.printRecord(record);
                }
            }

            printer.flush();
            return writer.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

    private List<String> createRecord(JsonNode entry, JsonNode requestNode, String url) {
        List<String> record = new ArrayList<>();
        record.add(url);

        URI uri = parseUri(url);
        record.add(uri != null ? nullToEmpty(uri.getHost()) : "");

        String[] steps = extractSteps(uri);
        for (String step : steps) {
            record.add(step);
        }

        record.add(requestNode.path("method").asText("").toUpperCase());
        record.add(extractPayload(requestNode));
        record.add(extractResponse(entry.path("response")));

        return record;
    }

    private boolean shouldSkipUrl(String url) {
        if (url == null) {
            return false;
        }
        String lowerUrl = url.toLowerCase();
        return lowerUrl.contains(".html")
                || lowerUrl.contains(".js")
                || lowerUrl.contains(".css")
                || lowerUrl.contains(".json");
    }

    private URI parseUri(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        try {
            return new URI(url);
        } catch (URISyntaxException e) {
            return null;
        }
    }

    private String[] extractSteps(URI uri) {
        String[] steps = new String[]{"", "", "", "", ""};
        if (uri == null) {
            return steps;
        }
        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            return steps;
        }
        String[] segments = path.split("/+");
        int index = 0;
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }
            steps[index] = segment;
            index++;
            if (index >= steps.length) {
                break;
            }
        }
        return steps;
    }

    private String extractPayload(JsonNode requestNode) {
        JsonNode postData = requestNode.path("postData");
        if (postData.isMissingNode()) {
            return "";
        }
        JsonNode textNode = postData.path("text");
        if (!textNode.isMissingNode() && !textNode.isNull()) {
            return textNode.asText("");
        }
        JsonNode paramsNode = postData.path("params");
        if (paramsNode.isArray()) {
            List<String> params = new ArrayList<>();
            for (JsonNode param : paramsNode) {
                String name = param.path("name").asText("");
                String value = param.path("value").asText("");
                params.add(name + "=" + value);
            }
            return String.join("&", params);
        }
        return "";
    }

    private String extractResponse(JsonNode responseNode) {
        if (responseNode.isMissingNode()) {
            return "";
        }
        JsonNode content = responseNode.path("content");
        if (content.isMissingNode()) {
            return "";
        }
        JsonNode textNode = content.path("text");
        if (textNode.isMissingNode() || textNode.isNull()) {
            return "";
        }
        return textNode.asText("");
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
