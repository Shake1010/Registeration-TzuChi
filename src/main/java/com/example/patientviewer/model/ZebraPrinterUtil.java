package com.example.patientviewer.model;

import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.io.*;

public class ZebraPrinterUtil {
    private static final String PRINTER_NAME = "ZD230"; // Change this to match your printer name

    public static void printLabel(String content) throws PrintException {
        // Create ZPL code for the label
        String zplContent = generateZPLCode(content);

        // Find the Zebra printer
        PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
        PrintService zebraPrinter = null;

        for (PrintService service : services) {
            if (service.getName().toLowerCase().contains(PRINTER_NAME.toLowerCase())) {
                zebraPrinter = service;
                break;
            }
        }

        if (zebraPrinter == null) {
            throw new PrintException("Zebra printer not found");
        }

        // Create print job
        DocPrintJob job = zebraPrinter.createPrintJob();

        // Create document
        byte[] bytes = zplContent.getBytes();
        DocFlavor flavor = DocFlavor.BYTE_ARRAY.AUTOSENSE;
        Doc doc = new SimpleDoc(bytes, flavor, null);

        // Print
        job.print(doc, null);
    }

    private static String generateZPLCode(String content) {
        // ZPL code to print a label with the content
        return "^XA" +
                "^FO50,50" +
                "^A0N,50,50" +
                "^FD" + content + "^FS" +
                "^XZ";
    }
}

