package com.example.demo.Tools;



import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.blend.BlendMode;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;

import java.io.File;
import java.io.IOException;

public class AddWatermarkToPdf {
    
    public static void waterMark(String sourceFile, String destinationFile, String watermarkText) throws IOException {
        File srcFile = new File(sourceFile);
        File dstFile = new File(destinationFile);
        String text = watermarkText;
        try (PDDocument doc = PDDocument.load(srcFile)) {
            for (PDPage page : doc.getPages()) {
                PDFont font = PDType1Font.HELVETICA;
                addWatermarkText(doc, page, font, text);
            }
            doc.save(dstFile);
        }
    }

    private static void addWatermarkText(PDDocument doc, PDPage page, PDFont font, String text)
            throws IOException {
        try (PDPageContentStream cs
                     = new PDPageContentStream(doc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
            float fontHeight = 100; // arbitrary for short text
            float width = page.getMediaBox().getWidth();
            float height = page.getMediaBox().getHeight();
            float stringWidth = font.getStringWidth(text) / 1000 * fontHeight;
            float diagonalLength = (float) Math.sqrt(width * width + height * height);
            float angle = (float) Math.atan2(height, width);
            float x = (diagonalLength - stringWidth) / 2; // "horizontal" position in rotated world
            float y = -fontHeight / 4; // 4 is a trial-and-error thing, this lowers the text a bit
            cs.transform(Matrix.getRotateInstance(angle, 0, 0));
            cs.setFont(font, fontHeight);
            //cs.setRenderingMode(RenderingMode.STROKE); // for "hollow" effect

            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.2f);
            gs.setStrokingAlphaConstant(0.2f);
            gs.setBlendMode(BlendMode.MULTIPLY);
            gs.setLineWidth(3f);
            cs.setGraphicsStateParameters(gs);

            // some API weirdness here. When int, range is 0..255.
            // when float, this would be 0..1f
            cs.setNonStrokingColor(255, 0, 0);
            cs.setStrokingColor(255, 0, 0);

            cs.beginText();
            cs.newLineAtOffset(x, y);
            cs.showText(text);
            cs.endText();
        }
    }
}