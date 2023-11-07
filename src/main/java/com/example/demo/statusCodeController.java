package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.demo.models.StatusCodeResult;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class statusCodeController {
    private List<String> liveUpdates = new ArrayList<>();
    private Map<Integer, Integer> statusCounts = new HashMap<>();

    @GetMapping("/code")
    public String home() {
        return "statusCode";
    }

    @GetMapping("/check-status")
    public String checkStatus() {
        return "checkStatus";
    }

    @PostMapping("/check-status")
    public String checkStatus(@RequestParam("sitemapUrl") String sitemapUrl) {
        try {
            liveUpdates.clear();
            statusCounts.clear();
            Document doc = Jsoup.connect(sitemapUrl).get();
            List<StatusCodeResult> results = new ArrayList<>();

            for (Element urlElement : doc.select("urlset > url > loc")) {
                String url = urlElement.text();
                int statusCode = getStatusCode(url);
                results.add(new StatusCodeResult(url, statusCode));
                updateStatusCounts(statusCode);
                addLiveUpdate("Processed URL: " + url + " - Status Code: " + statusCode);
                
               
         
            }

            return "statusCode";
        } catch (IOException e) {
            e.printStackTrace();
            // Handle the error as needed
          
        }
         return "statusCode";
    }

    private int getStatusCode(String url) throws IOException {
        URL websiteURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) websiteURL.openConnection();
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        connection.setRequestMethod("GET");
        return connection.getResponseCode();
    }

    private void updateStatusCounts(int statusCode) {
        statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);
    }

    private void addLiveUpdate(String update) {
        liveUpdates.add(update);
    }

      @GetMapping("/score")
    public ResponseEntity <Map<Integer, Integer>> getscore() {
        return new ResponseEntity<>(statusCounts, HttpStatus.OK);
    }

    @GetMapping("/live-updates")
    public ResponseEntity<List<String>> getLiveUpdates() {
        return new ResponseEntity<>(liveUpdates, HttpStatus.OK);
    }
}
