package com.metastrephe.app.controller;

import com.metastrephe.app.service.HarConversionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController
@RequestMapping("/api/har")
@Validated
@Tag(name = "HAR Converter", description = "Serviço para converter arquivos HAR em CSV")
public class HarConversionController {

    private final HarConversionService conversionService;

    public HarConversionController(HarConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Operation(summary = "Converte um arquivo HAR em CSV", description = "Faz upload de um arquivo HAR e retorna o CSV gerado")
    @PostMapping(value = "/convert", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ByteArrayResource> convert(@RequestParam("file") @NotNull MultipartFile file) throws IOException {
        byte[] csvBytes = conversionService.convertHarToCsv(file.getInputStream());

        String outputFileName = file.getOriginalFilename() != null
                ? file.getOriginalFilename().replaceAll("(?i)\\.har$", "") + ".csv"
                : "resultado.csv";

        ByteArrayResource resource = new ByteArrayResource(csvBytes);
        ContentDisposition contentDisposition = ContentDisposition.attachment().filename(outputFileName).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvBytes.length)
                .body(resource);
    }
}
