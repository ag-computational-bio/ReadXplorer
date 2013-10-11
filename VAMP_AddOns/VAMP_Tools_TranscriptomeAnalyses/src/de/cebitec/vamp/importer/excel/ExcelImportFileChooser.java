/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.importer.excel;

import de.cebitec.vamp.exporter.excel.ExcelExporter;
import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import jxl.write.WriteException;
import org.openide.util.NbBundle;

/**
 *
 * @author jritter
 */
public class ExcelImportFileChooser extends VampFileChooser {



    public ExcelImportFileChooser(String[] fileExtensions, String fileDescription) {
        super(fileExtensions, fileDescription);
        this.openFileChooser(OPEN_DIALOG);
    }

    @Override
    public void save(String fileLocation) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void open(final String filelocation) {
        
        Thread exportThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    ExcelImporter importer = new ExcelImporter();
//                    exporter.setHeaders(exportData.dataColumnDescriptions());
//                    exporter.setExportData(exportData.dataToExcelExportList());
//                    exporter.setSheetNames(exportData.dataSheetNames());
//                    if (exporter.readyToExport()) {
                        try {

                            importer.readFile(filelocation);

                        } catch (FileNotFoundException ex) {
//                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                        } catch (IOException | OutOfMemoryError ex) {
//                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                        }
//                        progressHandle.finish();
//                    }
                }
            });
            exportThread.start();
    }
}
