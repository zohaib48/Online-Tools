package com.example.demo;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

@Controller
public class scraperController {

    @GetMapping("/sc")
    public String index() {
        return "scraper";
    }

    @PostMapping("/download")
    public ResponseEntity<FileSystemResource> downloadHTML(@RequestParam String url, Model model) {
        try {
            Document document = Jsoup.connect(url).get();
            String htmlContent = document.html();

            File file = new File("downloaded_html.html");
            FileWriter writer = new FileWriter(file);
            writer.write(htmlContent);
            writer.close();

            // Prepare the response to trigger file download
            FileSystemResource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=downloaded_html.html");

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (IOException e) {
            model.addAttribute("error", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/download-concise")
    public ResponseEntity<FileSystemResource> downloadConciseHTML(
            @RequestParam String url,
            @RequestParam(required = false, defaultValue = "false") boolean removeEmptyTags,
            Model model) {
        try {
            Document document = Jsoup.connect(url).get();

            // Remove CSS styles from the document
            document.select("style, link[rel=stylesheet]").remove();

            // Clean up unnecessary attributes to create a concise structure
            document.select("[class]").removeAttr("class");
            document.select("[id]").removeAttr("id");

            if (removeEmptyTags) {
                // Remove empty tags from the document
                document.select(":empty").remove();
            }

            String conciseHtml = document.html();

            File file = new File("concise_html.html");
            FileWriter writer = new FileWriter(file);
            writer.write(conciseHtml);
            writer.close();

            // Prepare the response to trigger file download
            FileSystemResource resource = new FileSystemResource(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=concise_html.html");

            return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
        } catch (IOException e) {
            model.addAttribute("conciseError", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
