package com.example.demo;

import net.coobird.thumbnailator.Thumbnails;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Controller
public class DemoController {

    @GetMapping("/uploadForm")
    public String showUploadForm() {
        return "uploadForm";
    }

    @PostMapping("/heicToPdf")
    public String convertHeicToPdf(@RequestParam("file") MultipartFile file, Model model) {
        try {
            File heicFile = convertMultipartFileToFile(file);
            File jpgFile = convertHeicToJpg(heicFile);

            // Create a PDF document
            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                // Add the image to the PDF document
                try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                    BufferedImage bufferedImage = ImageIO.read(jpgFile);
                    float scale = 1f; // adjust the scale as needed
                 //   contentStream.drawImage(
                           // PDImageXObject.createFromByteArray(document, Thumbnails.of(bufferedImage).scale(1.0).asByteArray()),
                           // 20, 20, bufferedImage.getWidth() * scale, bufferedImage.getHeight() * scale
                //    );
                }

                // Save the PDF document
                String outputFilePath = "output.pdf";
                document.save(outputFilePath);
            }

            model.addAttribute("successMessage", "Conversion successful!");

        } catch (IOException e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error during conversion. Exception: " + e.getMessage());
        }

        return "uploadForm";
    }

    private File convertMultipartFileToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        file.transferTo(convFile);
        return convFile;
    }

    private File convertHeicToJpg(File heicFile) throws IOException {
        // Convert HEIC to JPG using Thumbnailator
        BufferedImage bufferedImage = Thumbnails.of(heicFile)
                .outputFormat("jpg")
                .scale(1.0) // Set the scale to 1.0
                .asBufferedImage();

        File jpgFile = new File("temp.jpg");
        ImageIO.write(bufferedImage, "jpg", jpgFile);

        return jpgFile;
    }
}
