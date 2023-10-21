package com.example.demo.models;

import org.springframework.web.multipart.MultipartFile;

public class Splitter {
    private MultipartFile pdfFile;
    private String pageRanges;

    public MultipartFile getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(MultipartFile pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getPageRanges() {
        return pageRanges;
    }

    public void setPageRanges(String pageRanges) {
        this.pageRanges = pageRanges;
    }
}
