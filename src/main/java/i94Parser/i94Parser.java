package i94Parser;

import com.opencsv.CSVWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Chris on 3/3/2019.
 */
public class i94Parser {
    private static final String I94HEADER = "Date Type Location";
    private static final String[] CSVHEADER = { "Entry", "Date", "Type", "Location" };

    public static void main(String args[]) {
        String inputPdf = args[0];
        String outputCsv = args[1];

        // Perform ETL
        ArrayList<String> extractedi94Data = extractTextFromPdf(inputPdf);
        ArrayList<String[]> borderActivities = transformi94Data(extractedi94Data);
        loadToCsv(borderActivities, outputCsv);
    }

    /**
     * @param pathToPdf Full path to i94 PDF downloaded from the USCIS website
     * @return List of lines extracted from i94
     */
    private static ArrayList<String> extractTextFromPdf(String pathToPdf) {
        String parsedText = null;
        try {
            parsedText = new PDFTextStripper().getText(PDDocument.load(new File(pathToPdf)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] lines = parsedText.split(System.getProperty("line.separator"));
        ArrayList<String> entries = new ArrayList<>();
        entries.addAll(Arrays.asList(lines));
        return entries;
    }

    /**
     * @param extractedi94Data List of lines from i94 PDF
     * @return i94 data represented as a format to be inserted into a CSV
     */
    private static ArrayList<String[]> transformi94Data(ArrayList<String> extractedi94Data) {
        ArrayList<String[]> result = new ArrayList<>();
        boolean isTrip = false;
        for (String line : extractedi94Data) {
            // Until the first instance of the I94HEADER is found, we skip over the data
            if (line.equals(I94HEADER)) {
                isTrip = true;
            }
            // Once we find the first instance of the I94HEADER, we filter subsequent instances
            // of it but parse the rest.
            if (isTrip) {
                if (!line.equals(I94HEADER)) {
                    result.add(line.split(" "));
                }
            }
        }
        return result;
    }

    /**
     * @param borderActivities All activities from the border, ready to be inserted into the CSV
     * @param outputCsvPath Full path of the CSV to insert the data into
     */
    private static void loadToCsv(ArrayList<String[]> borderActivities, String outputCsvPath) {
        // Write to file
        FileWriter outputfile = null;
        try {
            outputfile = new FileWriter(outputCsvPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        CSVWriter writer = new CSVWriter(outputfile);
        writer.writeNext(CSVHEADER);
        for (String[] borderActivity : borderActivities) {
            writer.writeNext(borderActivity);
        }
        try {
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
