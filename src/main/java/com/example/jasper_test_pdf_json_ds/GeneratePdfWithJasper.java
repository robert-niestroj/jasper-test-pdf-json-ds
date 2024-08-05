package com.example.jasper_test_pdf_json_ds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JsonDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import net.sf.jasperreports.export.SimplePdfReportConfiguration;

@Service
public class GeneratePdfWithJasper {

    public static final Logger log = LoggerFactory.getLogger(GeneratePdfWithJasper.class);

    @EventListener(ApplicationReadyEvent.class)
    public void test() throws JRException, IOException {
        var cpr = new ClassPathResource("test.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(cpr.getInputStream());

        // JSON data as a String (ensure the string is properly formatted JSON)
        String jsonString = """
                { "data": [ {"field1": "aaa", "field2": "bbb"}, {"field1": "ccc", "field2": "ddd"} ], "title": "zzz" }""";

        // Create an InputStream from the JSON string
        ByteArrayInputStream jsonInputStream = new ByteArrayInputStream(jsonString.getBytes());

        // Create a JsonDataSource from the JSON input stream
        JsonDataSource jsonDataSource = new JsonDataSource(jsonInputStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("ReportTitle", "Sample Report");

        // Fill the report with data from the JsonDataSource
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, jsonDataSource);

        // Create a ByteArrayOutputStream to hold the PDF data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Create the PDF exporter
        JRPdfExporter exporter = new JRPdfExporter();

        // Set the exporter input and output
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));

        // Configure PDF exporter
        SimplePdfReportConfiguration reportConfig = new SimplePdfReportConfiguration();
        reportConfig.setSizePageToContent(true);
        reportConfig.setForceLineBreakPolicy(false);

        SimplePdfExporterConfiguration exportConfig = new SimplePdfExporterConfiguration();
        exportConfig.setMetadataAuthor("Author Name");

        exporter.setConfiguration(reportConfig);
        exporter.setConfiguration(exportConfig);

        // Export the report to PDF
        exporter.exportReport();
        var path = Paths.get("").toAbsolutePath().resolve("test-jasper-json.pdf");
        Files.write(path, byteArrayOutputStream.toByteArray());
    }

}
