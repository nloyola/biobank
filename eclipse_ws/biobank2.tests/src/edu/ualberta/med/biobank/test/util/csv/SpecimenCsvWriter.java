package edu.ualberta.med.biobank.test.util.csv;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.supercsv.cellprocessor.ConvertNullTo;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import edu.ualberta.med.biobank.common.action.csvimport.SpecimenCsvInfo;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;

/**
 * Used for testing the Specimen CSV file Legacy Import feature.
 * 
 * @author loyola
 * 
 */
public class SpecimenCsvWriter {

    /**
     * Generates a Patient CSV file with random patient numbers.
     * 
     * @param filename The name of the CSV file to be saved.
     * @param studyName The short name to be used for the study.
     * @param numRows The number of rows in the CSV file.
     * @throws IOException If the file could not be saved.
     */
    @SuppressWarnings("nls")
    public static void write(String filename, Set<SpecimenCsvInfo> specimenInfos)
        throws IOException {
        final String[] header = new String[] {
            "inventoryId",
            "parentInventoryID",
            "specimenType",
            "createdAt",
            "patientNumber",
            "visitNumber",
            "currentCenter",
            "originCenter",
            "sourceSpecimen",
            "worksheet",
            "palletProductBarcode",
            "rootContainerType",
            "palletLabel",
            "palletPosition"
        };

        ICsvMapWriter writer =
            new CsvMapWriter(new FileWriter(filename),
                CsvPreference.EXCEL_PREFERENCE);

        final CellProcessor[] processing = new CellProcessor[] {
            null,
            new ConvertNullTo(""),
            null,
            null,
            null,
            null,
            null,
            new ConvertNullTo(""),
            null,
            new ConvertNullTo(""),
            new ConvertNullTo(""),
            new ConvertNullTo(""),
            new ConvertNullTo(""),
            new ConvertNullTo("")
        };

        try {
            writer.writeHeader(header);

            for (SpecimenCsvInfo info : specimenInfos) {
                final HashMap<String, ? super Object> data =
                    new HashMap<String, Object>();
                data.put(header[0], info.getInventoryId());
                data.put(header[1], info.getParentInventoryID());
                data.put(header[2], info.getSpecimenType());
                data.put(header[3],
                    DateFormatter.formatAsDateTime(info.getCreatedAt()));
                data.put(header[4], info.getPatientNumber());
                data.put(header[5], info.getVisitNumber());
                data.put(header[6], info.getCurrentCenter());
                data.put(header[7], info.getOriginCenter());
                data.put(header[8], info.getSourceSpecimen());
                data.put(header[9], info.getWorksheet());
                data.put(header[10], info.getPalletProductBarcode());
                data.put(header[11], info.getRootContainerType());
                data.put(header[12], info.getPalletLabel());
                data.put(header[13], info.getPalletPosition());
                writer.write(data, header, processing);
            }
        } finally {
            writer.close();
        }
    }

}
