package com.tkdals.pdf;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v113.network.Network;
import org.openqa.selenium.print.PrintOptions;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

@RestController
public class PdfController {

    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestParam("url") String url, String sessionId) throws InterruptedException {
        // 성공
        //System.setProperty("webdriver.chrome.driver", "C:\\MIDAS\\pdf-server\\src\\main\\resources\\static\\driver\\104\\chromedriver104.exe");
        // 실패
        //System.setProperty("webdriver.http.factory", "jdk-http-client");

        //options.setBinary("C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu", "--no-sandbox");
        ChromeDriver driver = new ChromeDriver(options);
        driver.get(url);
        Cookie cookie = new Cookie("SESSION_mrs", sessionId);
        driver.manage().addCookie(cookie);
        driver.navigate().refresh();

        try {
            Pdf pdf = driver.print(new PrintOptions());
            Files.write(Paths.get("./generate.pdf"), OutputType.BYTES.convertFromBase64Png(pdf.getContent()));

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=generated-pdf.pdf")
                    .body(null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            driver.quit();
        }
    }
}


