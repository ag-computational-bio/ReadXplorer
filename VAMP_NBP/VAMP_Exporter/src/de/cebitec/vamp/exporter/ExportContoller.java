package de.cebitec.vamp.exporter;

import de.cebitec.vamp.exporter.excel.SnpExcelExporter;
import de.cebitec.vamp.databackend.dataObjects.Snp454;
import de.cebitec.vamp.databackend.dataObjects.SnpData;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;

/**
 *
 * @author jstraube, rhilker
 */
public class ExportContoller implements ActionListener {

    private File exportFile;
    private File tempFile;
    private String filename;
    private String contentType;
    private SnpData snpData;
    private List<Snp454> snps454;
    private ExporterI exporter = null;

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

        EXCEL,
        SNP454;

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
    public void notifyExportEvent(EnumExportType exportType, final String contentType) {
        //often used error messages
        String msg = NbBundle.getMessage(ExportContoller.class, "ExportContoller.FailMsg",
                "An error occured while reading the specified file");
        String header = NbBundle.getMessage(ExportContoller.class, "ExportContoller.FailHeader", "Error");

        try {
            /* initialise correct exporter */
            switch (exportType) {
                case EXCEL:
                    exporter = new SnpExcelExporter();
                    ((SnpExcelExporter)exporter).setSNPs(snpData);
                    break;
                case SNP454:
                    exporter = new Snp454Exporter(this.snps454);
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
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, " Export failed!", ex);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                    Logger.getLogger(this.getClass().getName()).log(Level.WARNING, " Export failed!", ex);
                }
            } else {
                // Exporter is not ready!
                String msg2 = NbBundle.getMessage(ExportContoller.class, "ExportContoller.NotReadyMsg",
                    "The SNP data is empty, so nothing to store right now.");
                JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg2, header, JOptionPane.ERROR_MESSAGE);
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
    public void setSnpData(SnpData snpData){
    this.snpData = snpData;
    }

    public void setFile(String path){
        tempFile = new File(path);
    }

    public void setName(String name){
        this.filename = name;
    }
    
    public void setSnp454Data(List<Snp454> snps454) {
        this.snps454 = snps454;
    }

}
