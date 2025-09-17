package com.metastrephe.app.cli;

import com.metastrephe.app.service.HarConversionService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class HarFileCommandRunner implements ApplicationRunner {

    private final HarConversionService conversionService;
    private final ConfigurableApplicationContext context;

    public HarFileCommandRunner(HarConversionService conversionService,
                                ConfigurableApplicationContext context) {
        this.conversionService = conversionService;
        this.context = context;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption("har-file")) {
            return;
        }

        String harFilePath = args.getOptionValues("har-file").get(0);
        String csvFilePath = args.containsOption("csv-file")
                ? args.getOptionValues("csv-file").get(0)
                : deriveCsvPath(harFilePath);

        Path harPath = Paths.get(harFilePath);
        Path csvPath = Paths.get(csvFilePath);

        try (InputStream inputStream = Files.newInputStream(harPath)) {
            byte[] csvContent = conversionService.convertHarToCsv(inputStream);
            Path parent = csvPath.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(csvPath, csvContent);
            System.out.printf("Arquivo CSV gerado em %s%n", csvPath.toAbsolutePath());
        } catch (IOException ex) {
            System.err.printf("Falha ao converter arquivo HAR: %s%n", ex.getMessage());
            throw ex;
        } finally {
            SpringApplication.exit(context, () -> 0);
        }
    }

    private String deriveCsvPath(String harFilePath) {
        if (harFilePath == null || harFilePath.isEmpty()) {
            return "har-convertido.csv";
        }
        if (harFilePath.toLowerCase().endsWith(".har")) {
            return harFilePath.substring(0, harFilePath.length() - 4) + ".csv";
        }
        return harFilePath + ".csv";
    }
}
