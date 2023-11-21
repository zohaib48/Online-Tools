package com.example.demo;

// Import statements...

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.example.demo.models.StatusCodeResult;

@Controller
public class statusCodeController {

    private static final int MAX_WEBSITES_TO_CHECK = 100;

    // do not store data at instance level ... becaue it seems, Spring is using same controller object or something else.. 
    private Map<Integer, Integer> statusCounts = new HashMap<>();
    private List<StatusCodeResult> results = new ArrayList<>();
    private ExecutorService executorService;

    @GetMapping("/code")
    public String home(HttpSession session) {
          if(session.getAttribute("counter") == null ) {
            session.setAttribute("counter", Integer.valueOf(1));
        } else {
            int counter = (Integer)session.getAttribute("counter");
            session.setAttribute("counter", Integer.valueOf(counter+1));
        }
        System.out.println(session.getAttribute("counter"));     
         
        statusCounts = new HashMap<>();
        results = new ArrayList<>();
        return "statusCode";
    }

  

    @PostMapping("/check-status")
    public String checkStatus(@RequestParam("sitemapUrl") String sitemapUrl,
                              @RequestParam(value = "numThreads", defaultValue = "5") int numThreads,
                              Model model) {

        // Initialize statusCounts and results here
        statusCounts = new HashMap<>();
        results = new ArrayList<>();

      

        try {
            Document doc = Jsoup.connect(sitemapUrl).get();

            // Ensure that the number of websites to check does not exceed the maximum limit
            
            // todo - cover both cases of sitemap format e..g sitemaps, urls
            //      show result overall (currently implemented) and per sitemap (if given sitemap URLs contains list of sitemaps)
            int websitesToCheck = Math.min(MAX_WEBSITES_TO_CHECK, doc.select("urlset > url > loc").size());

            // Create a thread pool with the specified number of threads
            executorService = Executors.newFixedThreadPool(numThreads);

            for (Element urlElement : doc.select("urlset > url > loc").subList(0, websitesToCheck)) {
                String url = urlElement.text();

                // Execute the status checking task in a separate thread
                executorService.execute(
                    () -> {
                    try {
                        int statusCode = getStatusCode(url);
                        // todo make sure, the results and updateStatusCounts thread safe
                        results.add(new StatusCodeResult(url, statusCode));
                        updateStatusCounts(statusCode);
                       // System.out.println(statusCode);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle the error as needed
                    }
                });
            }

            // Shutdown the thread pool and wait for tasks to complete
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Add the results and limit status to the model for display in the frontend
            model.addAttribute("limitReached", websitesToCheck == MAX_WEBSITES_TO_CHECK);

            System.out.println("check-stats completed..");
            return "statusCode";
        } catch (IOException | InterruptedException e) {
            // todo serve page to show error in user friendly way ... "Problem in downloading... "
            // log at error level using some logger e.g. log4j (later for v2)
            e.printStackTrace();
            // Handle the error as needed
        } finally {
            // Ensure that the thread pool is shut down even if an exception occurs
            if (executorService != null) {
                executorService.shutdown();
            }
        }

        return "statusCode";
    }

    @PostMapping("/stop-process")
    public String stopProcess() {
    // Stop the processing by shutting down the thread pool
   
        executorService.shutdownNow();
    

    // Return a message or redirect to a page indicating that the process has been stopped
    return "statusCode"; // Assuming you want to redirect to the home page after stopping the process
}

    @PostMapping("/fetch-html-and-check-status")
    public String fetchHtmlAndCheckStatus(@RequestParam("websiteUrl") String websiteUrl,
                                          @RequestParam(value = "numThreads", defaultValue = "5") int numThreads,
                                           @RequestParam("depthLevel") int depthLevel,
                                          Model model) {
        // Initialize statusCounts and results here
        statusCounts = new HashMap<>();
        results = new ArrayList<>();
     
        System.out.println("depth lvl="+depthLevel);
        int counter=0;
        try {
            if(depthLevel==0){
                 int statusCode = getStatusCode(websiteUrl);
                System.out.print("url to check="+websiteUrl);
                
                results.add(new StatusCodeResult(websiteUrl, statusCode));
                updateStatusCounts(statusCode);
                 return "statusCode";
            }
            if(depthLevel==1)
            {
                    Document doc = Jsoup.connect(websiteUrl).get();
          

            // Extract all anchor tags
            List<String> urlsToCheck = new ArrayList<>();
            
            for (Element anchorElement : doc.select("a[href]")) {
                String url = anchorElement.attr("abs:href");
                urlsToCheck.add(url);
                counter++;
                if (counter >= MAX_WEBSITES_TO_CHECK)
                {
                    break;
                }
            }
        System.out.println("check ="+urlsToCheck.size());

            // Ensure that the number of websites to check does not exceed the maximum limit
            int websitesToCheck = Math.min(MAX_WEBSITES_TO_CHECK, urlsToCheck.size());

            // Create a thread pool with the specified number of threads
            executorService = Executors.newFixedThreadPool(numThreads);

            for (String url : urlsToCheck.subList(0, websitesToCheck)) {
                // Execute the status checking task in a separate thread
                executorService.execute(() -> {
                    try {
                        int statusCode = getStatusCode(url);
                        results.add(new StatusCodeResult(url, statusCode));
                        updateStatusCounts(statusCode);
                        System.out.println(statusCode);
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Handle the error as needed
                    }
                });
            }

            // Shutdown the thread pool and wait for tasks to complete
            executorService.shutdown();
            executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

            // Add the results and limit status to the model for display in the frontend
            model.addAttribute("limitReached", websitesToCheck == MAX_WEBSITES_TO_CHECK);

            System.out.println("program not ending");

            return "statusCode";

            }
        
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            // Handle the error as needed
        } finally {
            // Ensure that the thread pool is shut down even if an exception occurs
            if (executorService != null) {
                executorService.shutdown();
            }
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

    @GetMapping("/score")
    public ResponseEntity<Map<Integer, Integer>> getscore() {
        return new ResponseEntity<>(statusCounts, HttpStatus.OK);
    }

    @GetMapping("/urls/{statusCode}")
    public ResponseEntity<List<String>> getUrlsByStatusCode(@PathVariable int statusCode) {
        List<String> urls = new ArrayList<>();
        // Iterate over the results and add URLs with the specified status code
        for (StatusCodeResult result : results) {
            if (result.getStatusCode() == statusCode) {
                urls.add(result.getUrl());
            }
        }
        return new ResponseEntity<>(urls, HttpStatus.OK);
    }
}
