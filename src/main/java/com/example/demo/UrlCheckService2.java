package com.example.demo;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpSession;

import com.example.demo.models.StatusCodeResult;

@Service
public class UrlCheckService2 {
    private static final int MAX_WEBSITES_TO_CHECK = 100;
    

    @Async
    public String checkUrls(String websiteUrl, int numThreads, HttpSession session, int depthLevel)
            throws IOException, InterruptedException {

                boolean isValidUrl = validateUrl(websiteUrl);
                if (!isValidUrl) {
                    System.out.println("invalid url");
                       session.setAttribute("error", "URL IS INVALID PLEASE ENTER AGAIN");
                    return "statusCode";
                }


        session.setAttribute("executorService", Executors.newFixedThreadPool(numThreads));
        session.setAttribute("results", new CopyOnWriteArrayList<>());
        session.setAttribute("statusCounts", new ConcurrentHashMap<>());
        session.setAttribute("urlToCheck",0);
        

        ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
        List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) session.getAttribute("statusCounts");
        
        int websitesToCheck =(int) session.getAttribute("urlToCheck");


        int websitesToChecks = processSitemapUrls(websiteUrl, numThreads, executorService, results, statusCounts, session,
                depthLevel,websitesToCheck);

        // Wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.println("program ended");
        System.out.println("web to check inside" + websitesToChecks);
        if (websitesToChecks == MAX_WEBSITES_TO_CHECK) {
            System.out.print("i am");
            session.setAttribute("limitReached",true);
        }
        session.setAttribute("InProcess", false);

        return "statusCode";
    }

       private boolean validateUrl(String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    private int processSitemapUrls(String websiteUrl, int numThreads, ExecutorService executorService,
            List<StatusCodeResult> results, Map<Integer, Integer> statusCounts, HttpSession session, int depthLevel,int websitesToCheck)
            throws IOException, InterruptedException {
            websitesToCheck = 0; // Declare the variable outside the if block
           

        if (depthLevel == 0) {
            int statusCode = getStatusCode(websiteUrl);
            System.out.print("url to check=" + websiteUrl);

            results.add(new StatusCodeResult(websiteUrl, statusCode));
            statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);

        }
        if (depthLevel == 1) {
            Document doc = Jsoup.connect(websiteUrl).get();

            // Extract all anchor tags
            List<String> urlsToCheck = new ArrayList<>();

            for (Element anchorElement : doc.select("a[href]")) {
                String url = anchorElement.attr("abs:href");
                urlsToCheck.add(url);
                websitesToCheck++;
                if (websitesToCheck >= MAX_WEBSITES_TO_CHECK) {
                    break;
                }
            }
            System.out.println("check =" + urlsToCheck.size());

            // Ensure that the number of websites to check does not exceed the maximum limit
           

           
            

            CountDownLatch latch = new CountDownLatch(websitesToCheck);

            for (String url : urlsToCheck) {
                // Execute the status checking task in a separate thread
                executorService.execute(() -> {
                    try {
                       
                        session.setAttribute("InProcess", true);
                        int statusCode = getStatusCode(url);
                        results.add(new StatusCodeResult(url, statusCode));
                        statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);
                     
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle the error as needed
                    } finally {
                      
                        latch.countDown(); // Decrease the latch count when the task is completed
                    }
                });
            }

            // Wait for all tasks to complete before returning
            latch.await();
        }

        return websitesToCheck;
    }

    private int getStatusCode(String url) throws IOException {
        URL websiteURL = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) websiteURL.openConnection();
        connection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36");
        connection.setRequestMethod("GET");
        return connection.getResponseCode();
    }
}
