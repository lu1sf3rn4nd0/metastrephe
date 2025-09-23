package com.metastrephe.app.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarConversionServiceTest {

    private final HarConversionService service = new HarConversionService();

    @Test
    void convertsHarFileToCsv() throws IOException {
        byte[] harContent = Files.readAllBytes(Paths.get("src/test/resources/sample.har"));
        try (InputStream inputStream = new ByteArrayInputStream(harContent)) {
            byte[] csv = service.convertHarToCsv(inputStream);
            String csvText = new String(csv, StandardCharsets.UTF_8);

            Iterable<CSVRecord> records = CSVFormat.DEFAULT
                    .withDelimiter('|')
                    .withFirstRecordAsHeader()
                    .withQuote('"')
                    .parse(new StringReader(csvText));

            List<CSVRecord> recordList = new ArrayList<>();
            for (CSVRecord record : records) {
                recordList.add(record);
            }

            assertThat(recordList).hasSize(1);

            CSVRecord record = recordList.get(0);
            assertThat(record.get("url")).isEqualTo("https://portoapi-hml.portoseguro.com.br/oauth/v2/access-token");
            assertThat(record.get("host")).isEqualTo("portoapi-hml.portoseguro.com.br");
            assertThat(record.get("step1")).isEqualTo("oauth");
            assertThat(record.get("step2")).isEqualTo("v2");
            assertThat(record.get("step3")).isEqualTo("access-token");
            assertThat(record.get("método")).isEqualTo("POST");
            assertThat(record.get("Payload")).isEqualTo("{\"grant_type\":\"client_credentials\"}");
            assertThat(record.get("response")).contains("\"access_token\"");
        }
    }
}
