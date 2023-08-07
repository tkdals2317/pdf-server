package com.tkdals.pdf;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PdfController {

    @GetMapping("/pdf/genrate")
    public void generatePdf(@RequestParam String url) {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\lsm0506\\projects\\pdf-server\\src\\main\\resources\\static\\driver\\chromedriver.exe");

    }

}
