package com.example.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.models.Splitter;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Controller
public class HelloController {

    @GetMapping("/")
    public String home() {
        return "hello";
    }

    @GetMapping("/splitpdf")
    public String splitPdfForm(Model model) {
        model.addAttribute("splitterForm", new Splitter());
        return "splitPdfForm";
    }

    @PostMapping("/splitPdfAndDownload")
    public void splitPdfAndDownload(@ModelAttribute Splitter splitterForm, HttpServletResponse response) throws IOException {
        MultipartFile pdfFile = splitterForm.getPdfFile();
        String pageRanges = splitterForm.getPageRanges();

        if (!pdfFile.isEmpty()) {
            try (InputStream pdfInputStream = pdfFile.getInputStream()) {
                PDDocument document = PDDocument.load(pdfInputStream);

                List<String> pageRangeStrings = splitPageRanges(pageRanges);

                File tempDir = new File(System.getProperty("java.io.tmpdir") + File.separator + "splitpdf");
                tempDir.mkdirs();

                int pageNumber = 1;
                List<File> splitPdfFiles = new ArrayList<>();

                for (String pageRange : pageRangeStrings) {
                    PDDocument splitDocument = splitDocument(document, pageRange);
                    String outputPath = tempDir.getAbsolutePath() + File.separator + "split_page_" + pageNumber + ".pdf";
                    splitDocument.save(outputPath);
                    splitDocument.close();
                    pageNumber++;

                    splitPdfFiles.add(new File(outputPath));
                }

                File zipFile = createZipFile(splitPdfFiles);

                response.setContentType("application/zip");
                response.setHeader("Content-Disposition", "attachment; filename=split_pdf.zip");

                try (OutputStream out = response.getOutputStream();
                     FileInputStream zipInputStream = new FileInputStream(zipFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                }

                for (File splitPdfFile : splitPdfFiles) {
                    splitPdfFile.delete();
                }
                zipFile.delete();
                tempDir.delete();

                document.close();
            } catch (Exception e) {
                e.printStackTrace();
                // Handle the error as needed
            }
        }
    }

    private List<String> splitPageRanges(String pageRangesParam) {
        String[] rangeStrings = pageRangesParam.split(",");
        List<String> pageRangeStrings = new ArrayList<>();
        for (String rangeString : rangeStrings) {
            pageRangeStrings.add(rangeString);
        }
        return pageRangeStrings;
    }

    private PDDocument splitDocument(PDDocument document, String pageRangeString) throws IOException {
        PDDocument splitDocument = new PDDocument();
        String[] rangeParts = pageRangeString.split("-");
        for (String rangePart : rangeParts) {
            String[] pageNumbers = rangePart.trim().split("-");
            int startPage = Integer.parseInt(pageNumbers[0].trim());
            int endPage = pageNumbers.length > 1 ? Integer.parseInt(pageNumbers[1].trim()) : startPage;
            if (startPage <= endPage && startPage >= 1 && endPage <= document.getNumberOfPages()) {
                for (int i = startPage; i <= endPage; i++) {
                    PDPage page = document.getPage(i - 1);
                    splitDocument.addPage(page);
                }
            }
        }
        return splitDocument;
    }

    private File createZipFile(List<File> splitPdfFiles) throws IOException {
        File zipFile = File.createTempFile("split_pdf", ".zip");
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (File splitPdfFile : splitPdfFiles) {
                ZipEntry entry = new ZipEntry(splitPdfFile.getName());
                zos.putNextEntry(entry);
                try (FileInputStream fis = new FileInputStream(splitPdfFile)) {
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, bytesRead);
                    }
                }
                zos.closeEntry();
            }
        }
        return zipFile;
    }
}
