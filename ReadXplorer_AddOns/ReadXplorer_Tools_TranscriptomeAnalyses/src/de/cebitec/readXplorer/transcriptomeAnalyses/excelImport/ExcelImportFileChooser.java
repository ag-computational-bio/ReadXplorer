package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.swing.table.DefaultTableModel;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class ExcelImportFileChooser extends ReadXplorerFileChooser {

    DefaultTableModel model;
    ExcelToTable importer;
    HashMap<String, String> secondSheet;
    private File selectedFileForExport;

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
        this.setSelectedFile(new File(filelocation));
    }
}
