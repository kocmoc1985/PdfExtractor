/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static main.PdfExtractorSriLanka.EXCEPTION_1;
import static main.PdfExtractorSriLanka.EXCEPTION_2;
import static main.PdfExtractorSriLanka.STEP;
import static main.PdfExtractorSriLanka.STEP_ACTION_PATTERN;
import static main.PdfExtractorSriLanka.one_time_flag;
import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;
import sec.SimpleLoggerLight;

/**
 *
 * @author KOCMOC
 */
public class PdfExtStaticMethodsLib {
     public static void extractFromPdfAndWriteToFile(String path) {
        one_time_flag = true;
        String rawString = extractFromPdfFile(path);
        String[] lines = rawString.split("\r");
        //
        String[] arr = path.split("\\.");
        write(arr[0] + ".txt", lines);
    }
    
      public static void extractFromPdfAndWriteToFile(File file, String destPath) {
        one_time_flag = true;
        //
        String path = file.getAbsolutePath();
        String fileNameWithExt = file.getAbsoluteFile().getName();
        String fileNameNoExt = fileNameWithExt.split("\\.")[0];
        //
        String rawString = extractFromPdfFile(path);
        String[] lines = rawString.split("\r");
        //
        String p_ath = destPath + "/" + fileNameNoExt + ".txt";
        //
        write(p_ath, lines);
    }

    private synchronized static String extractFromPdfFile(String path) {
        PDFTextStripper pdfStripper;
        PDDocument pdDoc;
        COSDocument cosDoc;
        File file = new File(path);
        try {
            PDFParser parser = new PDFParser(new FileInputStream(file));
            parser.parse();
            cosDoc = parser.getDocument();
            pdfStripper = new PDFTextStripper();
            pdDoc = new PDDocument(cosDoc);
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(5);
            String parsedText = pdfStripper.getText(pdDoc);
//            System.out.println(parsedText);
            parser.clearResources();
            cosDoc.close();
            pdDoc.close();
            return parsedText;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }

    public static boolean isPdfFile(String path) {
        String[] arr = path.split("\\.");
        //
        if (arr.length < 2) {
            return false;
        }
        //
        if (arr[1].contains("pdf")) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean verifyIfSequencePdfFile(String path) {
        String rawString = extractFromPdfFile(path);
        String[] lines = rawString.split("\r");
        //
        if (lines.length < 10) {
            return false;
        }
        //
        if (lines[0].contains("Recipe Steps")) {
            return true;
        } else {
            return false;
        }
        //
    }

    private static void write(String fileToWriteTO, String[] linesToWrite) {
        try {
            // Create file
            FileWriter fstream = new FileWriter(fileToWriteTO, false);
            BufferedWriter out = new BufferedWriter(fstream);
            for (int i = 0; i < linesToWrite.length; i++) {
                //
                String line = linesToWrite[i];
                //
                if (line != null) {
                    //
                    if (line.contains("Ram Pressure") && line.contains("Rotations")) {
                        continue;
                    }
                    //
                    if (line.contains("MF") || line.contains("SV") || line.contains("TR")) {
                        line = line.replaceAll("MF", " MF");
                        line = line.replaceAll("SV", " SV");
                        line = line.replaceAll("TR", " TR");
                    }
                    //
                    if (line.contains("Recipe") && line.contains("Ver.:") && one_time_flag) {
                        out.write(line);
                        out.newLine();
                        one_time_flag = false;
                    }
                    //
                    if (line.contains("MaxTemp:")) {
                        line = "\n" + line;
                        out.write(line);
                        out.newLine();
                    }
                    //
                    if (checkIfDate(line)) {
                        line = "Last change: " + line.replaceAll("\n", "") + "\n";
                        out.write(line);
                        out.newLine();
                    }
                    //
                    if (line.contains(STEP) && line.contains(EXCEPTION_1) == false) {
                        line = line.trim();
                        line = line.substring(0, 2);
                        line = line.replaceAll("S", "");
                        line = "\nStep: " + line;
                        out.write(line);
                        out.newLine();
                    }
                    //
                    //
                    if (line.contains(STEP_ACTION_PATTERN) && line.contains(EXCEPTION_2) == false) {
                        //
                        out.write(line);
                        //
                    }
                    //
                    //
                }

                out.flush();
                //Close the output stream
            }
            out.close();
        } catch (Exception e) {//Catch exception if any
            Logger.getLogger(PdfExtractorSriLanka.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private static boolean checkIfDate(String value) {
        if (value.contains("ADVISE")) {
            return false;
        }

        //
        try {
            value = value.substring(1, 11);
        } catch (Exception ex) {
            return false;
        }
        //
        if (value.matches("\\d{2}.\\d{2}.\\d{4}")) {
            return true;
        }
        return false;
    }
}
