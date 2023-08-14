package com.tkdals.pdf;

import io.github.bonigarcia.wdm.WebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.Pdf;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v113.network.Network;
import org.openqa.selenium.print.PrintOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@RestController
public class PdfController {

    @GetMapping("/generate-pdf")
    public ResponseEntity<byte[]> generatePdf(@RequestParam("url") String url, String sessionId, String fileName)  {

        WebDriverManager.chromedriver().setup();

        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless");
//        options.addArguments("--disable-gpu");
//        options.addArguments("--no-sandbox");

//        options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
//        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
//        options.addArguments("--headless"); // only if you are ACTUALLY running headless
//        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
//        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
//        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
//        options.addArguments("--disable-gpu");

        options.addArguments("enable-automation");
        options.addArguments("--headless");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-extensions");
        options.addArguments("--dns-prefetch-disable");
        options.addArguments("--disable-gpu");
        options.setPageLoadTimeout(Duration.ofSeconds(600000));
        ChromeDriver driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        DevTools devTools = driver.getDevTools();
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty()));
        devTools.addListener(Network.requestWillBeSent(), requestWillBeSent -> System.out.println("요청 URL : " + requestWillBeSent.getRequest().getUrl()));

        driver.get(url);
        Cookie cookie = new Cookie("SESSION_mrs", sessionId);
        driver.manage().addCookie(cookie);
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(20));
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.navigate().to(url);

        log.info("\nChrome Driver navigate to URL : {}\nsessionId : {}\nfileName : {}", url, sessionId, fileName);
        WebDriverWait webDriverWait = new WebDriverWait(driver, Duration.ofSeconds(600));
        webDriverWait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.className("renderDone")));
        log.info("Page Rendering Complete!");
        try {
            long start = System.currentTimeMillis();
            log.info("PDF 추출 시작!");
            PrintOptions printOptions = new PrintOptions();
            Pdf pdf = driver.print(printOptions);
            Files.write(Paths.get(fileName+".pdf"), OutputType.BYTES.convertFromBase64Png(pdf.getContent()));
            log.info("PDF 추출 완료!");
            long end = System.currentTimeMillis();
            log.info("추출 시간" + (start-end)/1000);
        } catch (IOException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new RuntimeException(e);
        } catch (TimeoutException e){
            log.error(ExceptionUtils.getStackTrace(e));
        }
        finally {
            driver.quit();
        }
        return ResponseEntity.ok()
                .header("Content-Type", "application/pdf")
                .header("Content-Disposition", "attachment; filename=generated-pdf.pdf")
                .body(null);
    }
}


