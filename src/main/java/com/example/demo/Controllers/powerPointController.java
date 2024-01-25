package com.example.demo.Controllers;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletResponse;

import java.util.ArrayList;
import java.util.List;

@Controller
public class powerPointController {

    @GetMapping("/Powerpoint")
    public String uploadFileForm(Model model) {
        model.addAttribute("successMessage", null); // Initialize success message to null
        return "powerPointImage"; // Display the upload form initially on the "upload.html" page
    }

    @PostMapping("/powerPointImages")
    public String handleFileUpload(@RequestParam("file") MultipartFile file, Model model ,HttpServletResponse response) {
        List<byte[]> images = new ArrayList<>();
        String successMessage = null;

        try {
            byte[] fileBytes = file.getBytes();
            images = extractImagesFromFile(fileBytes);

            // Create a zip file containing all the images
            String zipFileName = "images.zip";
          File zipFile =  createZipFile(images, zipFileName);

                 response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=PPIT_images.zip");

           
                try (OutputStream out = response.getOutputStream();
                     FileInputStream zipInputStream = new FileInputStream(zipFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

            
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the error as needed
            }

        model.addAttribute("images", images);
        model.addAttribute("successMessage", successMessage);



        return "powerPointImage";
    }

    private List<byte[]> extractImagesFromFile(byte[] fileBytes) {
        List<byte[]> images = new ArrayList<>();
        try (XMLSlideShow ppt = new XMLSlideShow(new ByteArrayInputStream(fileBytes))) {
            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFPictureShape) {
                        XSLFPictureShape picture = (XSLFPictureShape) shape;
                        byte[] imageData = picture.getPictureData().getData();
                        images.add(imageData);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return images;
    }
 private File createZipFile(List<byte[]> images, String zipFileName) throws IOException {
    File zipFile = new File(zipFileName);

    try (FileOutputStream fos = new FileOutputStream(zipFile);
         ZipOutputStream zos = new ZipOutputStream(fos)) {

        int imageIndex = 0;
        for (byte[] imageData : images) {
            String entryName = "image" + imageIndex + ".png";
            ZipEntry zipEntry = new ZipEntry(entryName);
            zos.putNextEntry(zipEntry);
            zos.write(imageData);
            zos.closeEntry();
            imageIndex++;
        }
    } catch (IOException e) {
        e.printStackTrace();
    }

    return zipFile;
}
    

}