package com.example.demo.utils;



import com.mortennobel.imagescaling.ResampleOp;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.ocpsoft.prettytime.PrettyTime;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Utils {

    public static final Logger logger = Logger.getLogger(Utils.class);
    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("E, dd-MM-yyyy");
    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    public static Properties props = null;
    private static final String EMAIL_PATTERN_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_PATTERN_REGEX);
    private static final String AB = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static Random random = new Random();
    private static PrettyTime prettyTime = new PrettyTime();

    public static void main(String[] args) {
        String batch = "FA12";
        String degree = "BCS";
        String rollNumber = "057";

        String newNumber = org.apache.commons.lang3.StringUtils.stripStart(rollNumber, "0");
        System.out.println(newNumber);

//      String registrationNumber = batch + "-" + degree + "-" + rollNumber;
//      System.out.println(registrationNumber);
//      System.out.println(rollNumber);
    }

    public static boolean isEmailValid(String email) {
        if (org.apache.commons.lang3.StringUtils.isBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public String getFileName(Part part) {
        final String partHeader = part.getHeader("content-disposition");
        System.out.println("***** content-disposition : " + partHeader);

        for (String content : part.getHeader("content-disposition").split(";")) {
            if (content.trim().startsWith("filename")) {
                return content.substring(content.indexOf('=') + 1).trim()
                        .replace("\"", "");
            }
        }
        return null;
    }

    public static String getSimpleDate(Date date) {
        return simpleDateFormat.format(date);
    }

    public static String getShortString(String originalString) {
        String shortString = originalString;
        if (!StringUtils.isEmpty(originalString) && originalString.length() > 30) {
            shortString = originalString.substring(0, 28) + "..";
        }
        return shortString;
    }

    public static String getShortString(String originalString, int maxLengthRequired) {
        String shortString = originalString;
        if (!StringUtils.isEmpty(originalString) && originalString.length() > maxLengthRequired) {
            shortString = originalString.substring(0, maxLengthRequired - 3) + "...";
        }
        return shortString;
    }

    public static String toSlug(String input) {
        if (input == null) return "";
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String noLatinChar = NONLATIN.matcher(normalized).replaceAll("");
        String noMultipleDashes = noLatinChar.replaceAll("[-]+", "-");
        if (noMultipleDashes.endsWith("-")) {
            noMultipleDashes = noMultipleDashes.substring(0, noMultipleDashes.length() - 1);
        }
        return noMultipleDashes.toLowerCase(Locale.ENGLISH);
    }

    public static void writeOnResponse(HttpServletResponse response, String message) {
        PrintWriter pw = null;
        try {
            pw = response.getWriter();
            pw.write(message);
            pw.close();
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public static String getPropertyValue(String propertyName) {
        if (Utils.props == null) {
            Utils.props = new Properties();
            try {
                props.load(Utils.class.getClassLoader().getResourceAsStream("application.properties"));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return Utils.props.getProperty(propertyName);
    }

    public static void sleepForMinutes(int minutes) {
        try {
            Thread.sleep(1000 * 60 * minutes);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static void sleepForSeconds(int seconds) {
        try {
            Thread.sleep(1000 * seconds);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public static String randomString(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++)
            sb.append(AB.charAt(random.nextInt(AB.length())));
        return sb.toString();
    }

    public static String getNewRandomCode() {
        SecureRandom random = new SecureRandom();
        return (new BigInteger(35, random).toString(17));
    }

    public static Date convertToDateViaInstant(LocalDateTime dateToConvert) {
        return java.util.Date.from(dateToConvert.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String getPrettyTimeDuration(LocalDateTime localDateTime) {
        return prettyTime.format(convertToDateViaInstant(localDateTime));
    }

    public static String getNewFileNameWithExt(String imageFileName) {
        String[] formFileName = imageFileName.split("[.]");
        return Utils.getNewRandomCode() + "." + formFileName[(formFileName.length - 1)];
    }

    public static void saveStandardAndThumbImageUsingImgScalr(String fileStorePath, String newFileName,
                                                              InputStream imageInputStream,
                                                              final int standardSize,
                                                              final int thumbSize) throws Exception {
        BufferedImage srcImage = ImageIO.read(imageInputStream);
        String extension = newFileName.substring(newFileName.indexOf(".") + 1, newFileName.length());

        int actualHeight = srcImage.getHeight();
        int actualWidth = srcImage.getWidth();
        double imageRatio = (double) actualWidth / (double) actualHeight;

        // SAVE Standard Size first
        int height, width;
        if (actualHeight > standardSize || actualWidth > standardSize) {
            height = width = standardSize;
            if (imageRatio > 1) // 1 is standard ratio
                height = (int) (standardSize / imageRatio);
            else
                width = (int) (standardSize * imageRatio);
        } else {
            height = actualHeight;
            width = actualWidth;
        }
        BufferedImage scaledImage = Scalr.resize(srcImage, Scalr.Method.ULTRA_QUALITY, width, height); // Scalr.OP_BRIGHTER REMOVED.
        ImageIO.write(scaledImage, extension, new File(fileStorePath, newFileName));

        /* lets make the smaller size image now */

        scaledImage = null;
        if (actualHeight > thumbSize || actualWidth > thumbSize) { // comment it ... if icon size need to fix
            height = width = thumbSize;
            // Starts here ... comment it, if icon size need to make fix
            if (imageRatio > 1)
                height = (int) (thumbSize / imageRatio);
            else
                width = (int) (thumbSize * imageRatio);
        } else {
            height = actualHeight;
            width = actualWidth;
        }

        // Above approach do not create small size png images some time.
        // ScaledImage = Scalr.resize(srcImage, Scalr.Method.BALANCED, width, height, Scalr.OP_ANTIALIAS);
        ResampleOp resampleOp = new ResampleOp(width, height);
        scaledImage = resampleOp.filter(srcImage, null);

        ImageIO.write(scaledImage, extension, new File(fileStorePath, "s-" + newFileName));
    }

    public static void zipFiles(String destinationZipFile, String... filePaths) {
        try {
            String zipFileName = destinationZipFile; // must contain .zip at end

            FileOutputStream fos = new FileOutputStream(zipFileName);
            ZipOutputStream zos = new ZipOutputStream(fos);

            for (String aFile : filePaths) {
                zos.putNextEntry(new ZipEntry(new File(aFile).getName()));

                byte[] bytes = Files.readAllBytes(Paths.get(aFile));
                zos.write(bytes, 0, bytes.length);
                zos.closeEntry();
            }
            zos.close();
        } catch (IOException exp) {
            logger.error("broken while zipping files. " + exp.getMessage());
        }
    }

    public static void writeFileAtResponseToDownload(String sourceFilePath, String targetFileName, HttpServletResponse response) throws IOException{
        Path file = Paths.get(sourceFilePath);
        if (Files.exists(file)) {
            response.setContentType("application/zip");
            response.addHeader("Content-Disposition", "attachment; filename=\"" + targetFileName + "\"");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Expires", "-1");
            Files.copy(file, response.getOutputStream());
            response.getOutputStream().flush();
            response.getOutputStream().close();
        }

    }
}