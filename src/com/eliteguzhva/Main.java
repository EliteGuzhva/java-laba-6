package com.eliteguzhva;

import java.awt.*;

public class Main {

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Invalid number of arguments");
            System.exit(0);
        }

        String originalFile = args[0];
        String stampFile = args[1];
        String outputFile = args[2];

        PdfStamp pdfStamp = new PdfStamp();
        try {
            pdfStamp.init(originalFile);
            pdfStamp.putStamp(stampFile, "Stamp",
                    50, 50, 15, 15, false,
                    new Color(255, 255, 255), new Color(0, 0, 0));
            pdfStamp.save(outputFile);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }

}
