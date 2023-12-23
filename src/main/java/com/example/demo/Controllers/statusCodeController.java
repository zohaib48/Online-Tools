package com.example.demo.Controllers;



import org.jsoup.Jsoup;

// Import statements...

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.example.demo.TaskExecute;
import com.example.demo.UrlCheckService;
import com.example.demo.UrlCheckService2;
import com.example.demo.models.StatusCodeResult;

@Controller
public class statusCodeController {



    @GetMapping("/code")
    public String home(HttpSession session) {
       
   

        return "statusCode";
    }





   @Autowired
    private UrlCheckService urlCheckService;

    @Autowired
    private TaskExecute taskExecutor;
 

    @PostMapping("/check-status")
    public String checkStatus(@RequestParam("sitemapUrl") String sitemapUrl,
                              @RequestParam(value = "numThreads", defaultValue = "5") int numThreads,
                              Model model, HttpSession session) {
        // Reset session attributes before starting a new task
        session.setAttribute("executorService", null);
        session.setAttribute("results", null);
        session.setAttribute("statusCounts", null);

        // Reset other session attributes as needed
        session.setAttribute("InProcess", false);
        session.setAttribute("urlToCheck", 0);
        session.setAttribute("limitReached", false);
        session.setAttribute("error", null);
        System.out.println("is server busy "+taskExecutor.isServerBusy() );
    
     taskExecutor.submitTask(() -> {
    try {
        urlCheckService.checkUrls(sitemapUrl, numThreads, session, model);
    } catch (Exception e) {
        e.printStackTrace();
        // Handle the error as needed
        session.setAttribute("error", "An error occurred: " + e.getMessage());
    }
}, session);


        return "statusCode";
    }


    @Autowired
    private UrlCheckService2 urlCheckService2;
     @PostMapping("/fetch-html-and-check-status")
    public String fetchHtmlAndCheckStatus(@RequestParam("websiteUrl") String websiteUrl,
                                          @RequestParam(value = "numThreads", defaultValue = "5") int numThreads,
                                           @RequestParam("depthLevel") int depthLevel,
                                          Model model,HttpSession session) {
   ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    
        session.setAttribute("executorService", null);
        session.setAttribute("results", null);
        session.setAttribute("statusCounts", null);
    
        // Reset other session attributes as needed
        session.setAttribute("InProcess", false);
        session.setAttribute("urlToCheck", 0);
        session.setAttribute("limitReached",false);
        session.setAttribute("error",null);
                                
     
    
                                            
        CompletableFuture.runAsync(() -> {
            try {
                urlCheckService2.checkUrls(websiteUrl, numThreads, session, depthLevel);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                // Handle the error as needed
                session.setAttribute("error", "An error occurred: " + e.getMessage());
            }
        });

        return "statusCode";
    }


    
    
    private void resetSessionAttributes(HttpSession session) {
        // Stop the existing executor service if it's running
     
    }

    @PostMapping("/stop-process")
    @ResponseBody
    public Map<String, Object> stopProcess(HttpSession session) {
        Map<String, Object> response = new HashMap<>();

        ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
        if (executorService != null) {
            executorService.shutdownNow();
        }

        session.setAttribute("stopProcess", true);

        response.put("stopProcess", true);
        return response;
    }

  

    @GetMapping("/score")
    public ResponseEntity<Map<String, Object>> getscore(HttpSession session) {
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) session.getAttribute("statusCounts");
        
        Boolean limit = (Boolean) session.getAttribute("limitReached");
        
    
        boolean inProcess = session.getAttribute("InProcess") != null && (boolean) session.getAttribute("InProcess");
        String error = (String)session.getAttribute("error") ;
    
        Map<String, Object> result = new HashMap<>();
        result.put("statusCounts", statusCounts);
        System.out.println("limit value is "+limit);
        result.put("limitReached", limit);
        result.put("InProcess", inProcess);
        result.put("error", error);
    
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
    

    @GetMapping("/urls/{statusCode}")
    public ResponseEntity<List<String>> getUrlsByStatusCode(@PathVariable int statusCode, HttpSession session) {
        List<String> urls = Collections.synchronizedList(new ArrayList<>());
        List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");

        // Synchronize access to shared resource
        synchronized (results) {
            for (StatusCodeResult result : results) {
                if (result.getStatusCode() == statusCode) {
                    urls.add(result.getUrl());
                }
            }
        }

        return new ResponseEntity<>(urls, HttpStatus.OK);
    }
}
