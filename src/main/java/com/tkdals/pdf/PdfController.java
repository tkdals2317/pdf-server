package com.tkdals.pdf;

import org.openqa.selenium.Pdf;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.print.PrintOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Files;

@RestController
public class PdfController {

    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestParam("url") String url) {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-debugging-port=9222");
        options.addArguments("print-to-pdf");

        ChromeDriver driver = new ChromeDriver(options);

        try {
            driver.get(url);
            Pdf pdf = driver.print(new PrintOptions());

            String content = pdf.getContent();
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=generated-pdf.pdf")
                    .body(null);
        } finally {
            driver.quit();
        }
    }
}


