/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readxplorer.transcriptomeAnalyses.importer.excel;

import de.cebitec.vamp.transcriptomeAnalyses.TranscriptomeAnalysesTopComponentTopComponent;
import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Window",
        id = "de.cebitec.vamp.importer.excel.ImportAction")
@ActionRegistration(
        displayName = "#CTL_ImportAction")
@ActionReference(path = "Menu/File", position = 1300)
@Messages("CTL_ImportAction=Import Analyses from Excel")
public final class ImportAction implements ActionListener {

    private final ReferenceViewer refViewer;
     private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;
    private int referenceId;
    
    public ImportAction (ReferenceViewer reference) {
        this.refViewer = reference;
        this.referenceId = this.refViewer.getReference().getId();
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID);
        if (findTopComponent != null) {
        this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
        } else {
            transcAnalysesTopComp = new TranscriptomeAnalysesTopComponentTopComponent();
        }
        
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        
        this.transcAnalysesTopComp.open();
        ExcelImportFileChooser fc = new ExcelImportFileChooser(new String[]{"xls"}, "xls");

    }
}
