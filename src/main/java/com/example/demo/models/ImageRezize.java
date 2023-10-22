package com.example.demo.models;

import org.springframework.web.multipart.MultipartFile;

public class ImageRezize {

    private MultipartFile imageFile;
    private Integer width;
    private Integer height;
    private Float quality;
    private String format;
    private boolean aspectRatio;
    private Float scale;
    public MultipartFile getImageFile() {
        return imageFile;
    }
    public void setImageFile(MultipartFile imageFile) {
        this.imageFile = imageFile;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public Integer getHeight() {
        return height;
    }
    public void setHeight(Integer height) {
        this.height = height;
    }
    public Float getQuality() {
        return quality;
    }
    public void setQuality(Float quality) {
        this.quality = quality;
    }
    public String getFormat() {
        return format;
    }
    public void setFormat(String format) {
        this.format = format;
    }
    public boolean isAspectRatio() {
        return aspectRatio;
    }
    public void setAspectRatio(boolean aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    public Float getScale() {
        return scale;
    }
    public void setScale(Float scale) {
        this.scale = scale;
    }
    
   
    


}