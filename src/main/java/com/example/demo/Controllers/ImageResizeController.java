package com.example.demo.Controllers;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import com.example.demo.models.ImageRezize;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;


//backe
@Controller
public class ImageResizeController {

    private String resizedImageBase64; // Store the resized image data

    @GetMapping("/resize")
    public String home(Model model) {
        model.addAttribute("imageResizeForm", new ImageRezize());
        return "resizeForm";
    }

    @PostMapping("/resizeImage")
    public String resizeImage(ImageRezize imageResizeForm, Model model) {
        MultipartFile imageFile = imageResizeForm.getImageFile();
        Integer width = imageResizeForm.getWidth();
        Integer height = imageResizeForm.getHeight();
        Float quality = imageResizeForm.getQuality();
        String format = imageResizeForm.getFormat();
        boolean aspectRatio = imageResizeForm.isAspectRatio();
        Float scale = imageResizeForm.getScale();

        if (width == null) {
            width = -1; // Set 0 as the default width
        }
        if (height == null) {
            height = -1; // Set 0 as the default height
        }
    

    
        if (imageFile.isEmpty()) {
            model.addAttribute("error", "No image file uploaded.");
            return "resizeForm";
        }

        try {
            // Create a temporary file to store the resized image
            File tempFile = File.createTempFile("resized", "." + format);

            // Resize the image using Thumbnails library
            if (width == -1 && height == -1) {
                Thumbnails.of(imageFile.getInputStream())
                        .scale(scale)
                        .outputFormat(format)
                        .outputQuality(quality)
                        .toFile(tempFile);
            } else {
                Thumbnails.of(imageFile.getInputStream())
                        .size(width, height)
                        .outputFormat(format)
                        .outputQuality(quality)
                        .keepAspectRatio(aspectRatio)
                        .toFile(tempFile);
            }

            // Store the resized image data in the controller
            resizedImageBase64 = convertImageToBase64(tempFile);

            // Add the resized image and format to the model
            model.addAttribute("resizedImageBase64", resizedImageBase64);
            model.addAttribute("format", format);

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("error", "Error occurred during image resizing.");
        }

        return "resizeForm";
    }

    @GetMapping("/downloadImage")
    public ResponseEntity<FileSystemResource> downloadImage(ImageRezize imageResizeForm) throws IOException {
        if (resizedImageBase64 == null) {
            // Handle the case where resizedImageBase64 is not available
            // You can return an error page or take appropriate action
        }
        String format = imageResizeForm.getFormat();
        File resizedImage = File.createTempFile("resized", "." + format);

        // Write the Base64 decoded image data to the temporary file
        try (FileOutputStream fos = new FileOutputStream(resizedImage)) {
            byte[] imageBytes = Base64.getDecoder().decode(resizedImageBase64);
            fos.write(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("image/" + format));
        headers.setContentDispositionFormData("attachment", "resized_image." + format);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resizedImage.length())
                .body(new FileSystemResource(resizedImage));
    }

    private String convertImageToBase64(File imageFile) {
        try {
            byte[] imageBytes = Files.readAllBytes(imageFile.toPath());
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
