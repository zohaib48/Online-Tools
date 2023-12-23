package com.example.demo.Tools;



import com.example.demo.utils.*;
import org.apache.log4j.Logger;
import org.apache.pdfbox.contentstream.PDFStreamEngine;
import org.apache.pdfbox.contentstream.operator.Operator;
import org.apache.pdfbox.cos.COSBase;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class ExtractImagesFromPdf extends PDFStreamEngine {
    public static final Logger logger = Logger.getLogger(Utils.class);

    public int imageNumber = 1;
    public String destinationFolder;

    public static void main(String[] args) throws IOException {
        extractImages("E:\\test2.pdf", "E:\\temp\\");
    }

    public static void extractImages(String sourceFileNameWithPath, String destinationFolder) throws IOException {
        PDDocument document = null;
        try {
            document = PDDocument.load(new File(sourceFileNameWithPath));

            ExtractImagesFromPdf printer = new ExtractImagesFromPdf();
            printer.destinationFolder = destinationFolder;

            int pageNumber = 0;
            for (PDPage page : document.getPages()) {
                pageNumber++;
                printer.processPage(page);
            }
            logger.debug("all pages processed of uploaded file to extract images");
        } finally {
            if (document != null) {
                document.close();
            }
        }
    }

    @Override
    protected void processOperator(Operator operator, List<COSBase> operands) throws IOException {
        String operation = operator.getName();
        if ("Do".equals(operation)) {
            COSName objectName = (COSName) operands.get(0);
            PDXObject xObject = getResources().getXObject(objectName);
            if (xObject instanceof PDImageXObject) {
                PDImageXObject imageObject = (PDImageXObject) xObject;
                BufferedImage bufferedImage = imageObject.getImage();
                ImageIO.write(bufferedImage, "PNG", new File(destinationFolder + "image_" + imageNumber + ".png"));
                imageNumber++;
            }
        }
    }
}