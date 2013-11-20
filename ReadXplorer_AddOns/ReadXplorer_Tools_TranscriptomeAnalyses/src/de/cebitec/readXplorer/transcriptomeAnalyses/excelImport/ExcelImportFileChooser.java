/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.io.File;
import java.io.IOException;
import javax.swing.table.DefaultTableModel;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class ExcelImportFileChooser extends ReadXplorerFileChooser {

    DefaultTableModel model;
    ExcelToTable importer;

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
        try {
            this.importer = new ExcelToTable(new File(filelocation));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        setModel(importer.dataToDataTableImport());

    }

    public DefaultTableModel getModel() {
        return model;
    }

    public void setModel(DefaultTableModel model) {
        this.model = model;
    }
}
