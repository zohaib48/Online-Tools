package com.example.demo;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.demo.models.StatusCodeResult;

@Controller
public class statusCodeController {

    @GetMapping("/code")
    public String home() {
        return "statusCode";
    }

    @GetMapping("/check-status")
    public String checkStatus() {
        return "checkStatus";
    }

    @RestController
    public class StatusCodeController {
    
        @PostMapping("/check-status")
        public ResponseEntity<StatusCodeResult> checkStatus(@RequestParam("urls") String url) {
            try {
                System.out.println(url);
                URL websiteURL = new URL(url);
                HttpURLConnection connection = (HttpURLConnection) websiteURL.openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
                connection.setRequestMethod("GET");
                int statusCode = connection.getResponseCode();
                StatusCodeResult result = new StatusCodeResult(url, statusCode);
                return new ResponseEntity<>(result, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }
}
