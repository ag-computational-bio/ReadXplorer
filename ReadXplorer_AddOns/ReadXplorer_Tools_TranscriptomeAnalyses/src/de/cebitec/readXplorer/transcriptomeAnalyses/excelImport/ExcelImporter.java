/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import javax.swing.table.DefaultTableModel;
import org.netbeans.api.progress.ProgressHandle;

/**
 *
 * @author jritter
 */
public class ExcelImporter {

    private DefaultTableModel model;
    private ProgressHandle progressHandle;

    public ExcelImporter(ProgressHandle progressHandle) {
        this.progressHandle = progressHandle;
        this.model = new DefaultTableModel();
    }
}
