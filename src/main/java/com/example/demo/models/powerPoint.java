package com.example.demo.models;

import org.springframework.web.multipart.MultipartFile;

public class powerPoint {
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}