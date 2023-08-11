package com.tkdals.pdf;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v113.network.Network;
import org.openqa.selenium.print.PrintOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StopWatch;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;

@RestController
public class PdfController {

    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestParam("url") String url, String sessionId, String fileName)  {

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu", "--no-sandbox");
        ChromeDriver driver = new ChromeDriver(options);
        driver.get(url);
        Cookie cookie = new Cookie("SESSION_mrs", sessionId);
        driver.manage().addCookie(cookie);
        driver.navigate().to(url);

        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofMinutes(1));
        webDriverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("accDone")));

        try {
            Pdf pdf = driver.print(new PrintOptions());
            Files.write(Paths.get(fileName+".pdf"), OutputType.BYTES.convertFromBase64Png(pdf.getContent()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=generated-pdf.pdf")
                .body(null);
    }
}


