package com.example.demo.models;

import org.springframework.web.multipart.MultipartFile;

public class Image {
    private MultipartFile file;

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
