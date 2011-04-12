package de.cebitec.vamp.exporter;

import de.cebitec.vamp.exporter.excel.ExcelExporter;
import de.cebitec.vamp.api.objects.Snp;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jstraube
 */
public class ExportContoller implements ActionListener {

    private File exportFile;
    private File tempFile;
    private String filename;
    private String contentType;
    private List<Snp> snps = new ArrayList<Snp>();
    private ExcelExporter exporter = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        String actionCommand = e.getActionCommand();
        if (actionCommand.startsWith("export_")) {

            String exportTypeName = actionCommand.substring(7).toUpperCase();
            try {
                EnumExportType exportType = EnumExportType.getExporters(exportTypeName);
                notifyExportEvent(exportType, "text/plain");
                setContentType("text/plain");

            } catch (IllegalArgumentException iae) {
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Type does not match any exporter!");
            }
        }
    }

    public enum EnumExportType {

        EXCEL;

        /**
         * Get the EnumExportType of a given name
         * @param exportTypeName
         * @return enumExportType
         */
        public static EnumExportType getExporters(String exportTypeName) {
            return EnumExportType.valueOf(exportTypeName);
        }
    }

    /**
     * Notifies an export event and starts the appropriate exporter.
     * @param exportType The enum for the wanted exporter.
     * @param contentType The content type of the output file.
     */
    public void notifyExportEvent(
            EnumExportType exportType,
            final String contentType) {
        try {
            /* initialise correct exporter */
            switch (exportType) {
                case EXCEL:
                    exporter = new ExcelExporter();
                    exporter.setSNPs(snps);
                    break;
            }
        } catch (NoClassDefFoundError def) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Unknown exporter! Export failed!");
        }

        /* start exporter */
        if (exporter != null) {
            if (exporter.readyToExport()) {
                try {
                     exportFile = exporter.writeFile(tempFile, filename);

                } catch (FileNotFoundException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, " Export failed!", ex);
                } catch (IOException ex) {
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, " Export failed!", ex);
                }
            } else {
                // Exporter is not ready!
                Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "Exporter is not ready now");
            }
        }
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getFileName() {
        return exportFile.getName();
    }

    public int getSize() {
        return (int) exportFile.length();
    }
    public void setSnpData(List<Snp> snps){
    this.snps = snps;
    }

    public void setFile(String path){
        tempFile = new File(path);
    }

    public void setName(String name){
        this.filename = name;
    }

}
