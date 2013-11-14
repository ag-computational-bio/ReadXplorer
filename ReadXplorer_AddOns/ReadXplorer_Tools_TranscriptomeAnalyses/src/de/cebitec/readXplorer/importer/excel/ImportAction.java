/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.importer.excel;

import de.cebitec.readXplorer.transcriptomeAnalyses.TranscriptomeAnalysesTopComponent;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileSystemView;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Window",
        id = "de.cebitec.readXplorer.importer.excel.ImportAction")
@ActionRegistration(
        displayName = "#CTL_ImportAction")
@ActionReference(path = "Menu/File", position = 1300)
@Messages("CTL_ImportAction=Import Analyses from Excel")
public final class ImportAction implements ActionListener {

    private final ReferenceViewer refViewer;
     private TranscriptomeAnalysesTopComponent transcAnalysesTopComp;
    private int referenceId;
    
    public ImportAction (ReferenceViewer reference) {
        this.refViewer = reference;
        this.referenceId = this.refViewer.getReference().getId();
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponent.PREFERRED_ID);
        if (findTopComponent != null) {
        this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponent) findTopComponent;
        } else {
            transcAnalysesTopComp = new TranscriptomeAnalysesTopComponent();
        }
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        this.transcAnalysesTopComp.open();
        ExcelImportFileChooser fc = new ExcelImportFileChooser(new String[]{"xls"}, "xls");

    }
}
