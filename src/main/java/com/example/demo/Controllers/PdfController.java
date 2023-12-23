package com.example.demo.Controllers;



import com.example.demo.Tools.AddWatermarkToPdf;
import com.example.demo.Tools.EncryptPDF;
import com.example.demo.Tools.ExtractImagesFromPdf;
import com.example.demo.Tools.PDFMerger;
import  com.example.demo.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
public class PdfController {
    public static final Logger logger = Logger.getLogger(Utils.class);

    @Autowired
    ServletContext context;

    @GetMapping("/mergepdf")
    public String mergeMultiplePdfFiles() {
        return "MergeMultiplePdfFiles";
    }

    @RequestMapping(value = "/merge-multiple-pdf-files", method = RequestMethod.POST)
    public String mergeMultiplePdfFiles(@RequestParam("files") MultipartFile[] files,
                                        @RequestParam String newFileName,
                                        HttpServletResponse response) {

        String randomFileName = System.currentTimeMillis() + "" + ".pdf";
        if (StringUtils.isEmpty(newFileName)) {
            newFileName = randomFileName;
        } else {
            if (!newFileName.endsWith(".pdf")) {
                newFileName = newFileName + ".pdf";
            }
        }

        PDFMerger pdfMerger = new PDFMerger();
        ArrayList<InputStream> inputFiles = new ArrayList<InputStream>();

        try {
            for (MultipartFile file : files) {
                inputFiles.add(file.getInputStream());
            }
            InputStream inputStream = pdfMerger.merge(inputFiles);
            String fileDirectory = context.getRealPath("") + "WEB-INF" + File.separator + "temp" + File.separator;
            FileUtils.copyInputStreamToFile(inputStream, new File(fileDirectory + randomFileName));

            // Write file to browser
            Path file = Paths.get(fileDirectory, randomFileName);
            if (Files.exists(file)) {
                response.setContentType("application/pdf");
                response.addHeader("Content-Disposition", "attachment; filename=\"" + newFileName + "\"");
                response.setHeader("Cache-Control", "no-cache");
                response.setHeader("Expires", "-1");
                Files.copy(file, response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
        } catch (IOException exp) {
            exp.printStackTrace();
        }
        return null;
    }

    @GetMapping("/watermark")
    public String addWatermarkToPdf() {
        return "AddWatermarkToPdf";
    }
    

    @RequestMapping(value = "/add-watermark-to-pdf", method = RequestMethod.POST)
    public String addWatermarkToPdf(@RequestParam("file") MultipartFile inputFile,
                                    @RequestParam String watermarkText,
                                    HttpServletResponse response) {
        String originalFileName = inputFile.getOriginalFilename();
        String sourceFileName = System.currentTimeMillis() + "abc" + ".pdf";
        String destinationFileName = System.currentTimeMillis() + "xyz" + ".pdf";

        try {
            String fileDirectory = context.getRealPath("") + "WEB-INF" + File.separator + "temp" + File.separator;
            String sourceFilePath = fileDirectory + sourceFileName;
            FileUtils.copyInputStreamToFile(inputFile.getInputStream(), new File(sourceFilePath));

            String destinationFilePath = fileDirectory + destinationFileName;
            AddWatermarkToPdf.waterMark(sourceFilePath, destinationFilePath, watermarkText);
            Utils.writeFileAtResponseToDownload(fileDirectory + destinationFileName, "Watermarked-" + originalFileName, response);
        } catch (IOException exp) {
            logger.error("error occured while processing watermark-pdf-file. " + exp.getMessage());
            exp.printStackTrace();
        }
        return null;
        // todo redirect to download file page ..
    }

    @GetMapping("/extractimage")
    public String extractImagesFromPdf() {
        return "ExtractImagesFromPdf";
    }

    @RequestMapping(value = "/extract-images-from-pdf", method = RequestMethod.POST)
    public String extractImagesFromPdf(@RequestParam("file") MultipartFile inputFile, HttpServletResponse response) {
        String randomName = Utils.randomString(20);

        String sourceFileName = System.currentTimeMillis() + Utils.randomString(5) + ".pdf";
        try {
            String imagesDirectoryStr = context.getRealPath("") + "temp" + File.separator + randomName + File.separator;
            File imagesDirectory = new File(imagesDirectoryStr);
            if (!imagesDirectory.exists()) imagesDirectory.mkdirs();

            String uploadedFilesDirectory = context.getRealPath("") + "temp" + File.separator;
            String sourceFileNameWithPath = uploadedFilesDirectory + sourceFileName;
            FileUtils.copyInputStreamToFile(inputFile.getInputStream(), new File(sourceFileNameWithPath));
            ExtractImagesFromPdf.extractImages(sourceFileNameWithPath, imagesDirectoryStr);

            // Make zip file
            List<String> results = new ArrayList<>();
            File[] files = imagesDirectory.listFiles();
            for (File file : files) if (file.isFile()) results.add(file.getAbsolutePath());
            String zipFileName = uploadedFilesDirectory + randomName + ".zip";
            Utils.zipFiles(zipFileName, results.toArray(new String[0]));

            String targetFileName = "images-from-" + inputFile.getOriginalFilename() + ".zip";
            Utils.writeFileAtResponseToDownload(zipFileName, targetFileName, response);
        } catch (IOException exp) {
            logger.error("error occured while processing extract-images-from-pdf features. " + exp.getMessage());
            exp.printStackTrace();
        }
        return "redirect:/";
        // todo
        // if the file contains no image show proper message to user ...
        // optimize paths
        // redirect to next page ... and show link to download the file.
        // write logic to remove temporary directory files and folders ... after some time.
    }
}