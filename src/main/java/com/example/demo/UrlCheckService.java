package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.apache.commons.validator.UrlValidator;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//fix the issue if i stop the process it show me that limit has been reached 
import javax.servlet.http.HttpSession;

import com.example.demo.models.StatusCodeResult;

@Service
public class UrlCheckService {
    private static final int MAX_WEBSITES_TO_CHECK = 50;

    @Async
    public String checkUrls(String sitemapUrl, int numThreads, HttpSession session, Model model)
            throws IOException, InterruptedException {

              
                boolean isValidUrl = validateUrl(sitemapUrl);
                if (!isValidUrl) {
                    System.out.println("invalid url");
                    session.setAttribute("error", "URL IS INVALID PLEASE ENTER AGAIN");
                    return "statusCode";
                }

        session.setAttribute("executorService", Executors.newFixedThreadPool(numThreads));
        session.setAttribute("results", new CopyOnWriteArrayList<>());
        session.setAttribute("statusCounts", new ConcurrentHashMap<>());
        session.setAttribute("urlToCheck",0);
        session.setAttribute("error", null);
        

        

        ExecutorService executorService = (ExecutorService) session.getAttribute("executorService");
        List<StatusCodeResult> results = (List<StatusCodeResult>) session.getAttribute("results");
        Map<Integer, Integer> statusCounts = (Map<Integer, Integer>) session.getAttribute("statusCounts");
      
        int websitesToCheck =(int) session.getAttribute("urlToCheck");
      
        int websitesToChecks= processSitemapUrls(sitemapUrl, numThreads, executorService, results, statusCounts, session,websitesToCheck);
        
        


        // Wait for all tasks to complete
        executorService.shutdown();
        executorService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        System.out.println("program ended");
        System.out.println("web to check inside"+websitesToChecks);
        session.setAttribute("InProcess", false);
        int count = results.size();
        System.out.println("result list size "+ count);
       if(count==MAX_WEBSITES_TO_CHECK){
        System.out.print("i am");
        session.setAttribute("limitReached",true);
       }
       
   

        return "statusCode";
    }
    private boolean validateUrl(String url) {
        UrlValidator urlValidator = new UrlValidator();
        return urlValidator.isValid(url);
    }

    private int processSitemapUrls(String sitemapUrl, int numThreads, ExecutorService executorService,
    List<StatusCodeResult> results, Map<Integer, Integer> statusCounts, HttpSession session, int websitesToCheck)
    throws IOException {

   System.out.println("web fdfdsck");

    if (containsUrlLocTags(sitemapUrl)) {
    Document doc = Jsoup.connect(sitemapUrl).get();
    int totalWebsitesToCheck = Math.min(MAX_WEBSITES_TO_CHECK - (int)session.getAttribute("urlToCheck"), doc.select("urlset > url > loc").size());
    System.out.println("web to check" + totalWebsitesToCheck);



    for (Element urlElement : doc.select("urlset > url > loc").subList(0, totalWebsitesToCheck)) {
        String url = urlElement.text();
        session.setAttribute("InProcess", true);

        // Execute the status checking task in a separate thread
        executorService.execute(() -> {
            try {
                System.out.println("Thread ID: " + Thread.currentThread().getId() + ", Processing URL: " + url);
                int statusCode = getStatusCode(url);
                // todo make sure, the results and updateStatusCounts thread safe
                results.add(new StatusCodeResult(url, statusCode));
                statusCounts.put(statusCode, statusCounts.getOrDefault(statusCode, 0) + 1);
                // System.out.println(statusCode);
            } catch (IOException e) {
                e.printStackTrace();
                // Handle the error as needed
            }
        });
    }

    websitesToCheck += totalWebsitesToCheck;
    session.setAttribute("urlToCheck", websitesToCheck);
} else {
    Document doc = Jsoup.connect(sitemapUrl).get();
    for (Element sitemapElement : doc.select("sitemap > loc")) {
        String nestedSitemapUrl = sitemapElement.text();

        processSitemapUrls(nestedSitemapUrl, numThreads, executorService, results, statusCounts,
        session, websitesToCheck);
        int  checkstatus=(int ) session.getAttribute("urlToCheck");
        if (checkstatus >= MAX_WEBSITES_TO_CHECK) {
            System.out.println("time to break");
            break;
        }
    }
}

return  (int)session.getAttribute("urlToCheck");
}


    public static boolean containsUrlLocTags(String sitemapUrl) {
        try {
            Document doc = Jsoup.connect(sitemapUrl).get();

            // Select url elements that have a loc child element
            Elements urlLocElements = doc.select("url:has(loc)");

            // Check if there's at least one matching element
            return !urlLocElements.isEmpty();
        } catch (Exception e) {
            e.printStackTrace(); // Handle the exception according to your requirements
            return false;
        }
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