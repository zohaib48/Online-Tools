package com.example.demo.Tools;



import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.AccessPermission;
import org.apache.pdfbox.pdmodel.encryption.StandardProtectionPolicy;

import java.io.File;
public class EncryptPDF {

    public static void main(String args[]) throws Exception {

        // Loading an existing document
        File file = new File("sample.pdf");
        PDDocument document = PDDocument.load(file);

        //Creating access permission object
        AccessPermission accessPermission = new AccessPermission();
        accessPermission.setCanExtractContent(false);
        accessPermission.setCanModify(false);
        accessPermission.setCanPrint(true);
        accessPermission.setReadOnly();

        //Creating StandardProtectionPolicy object
        StandardProtectionPolicy spp = new StandardProtectionPolicy("1234", "1234", accessPermission);

        //Setting the length of the encryption key
        spp.setEncryptionKeyLength(128);

        //Setting the access permissions
        spp.setPermissions(accessPermission);

        //Protecting the document
        document.protect(spp);

        System.out.println("Document encrypted");

        //Saving the document
        document.save("sample1.pdf");
        //Closing the document
        document.close();

    }
}