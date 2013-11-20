/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.transcriptomeAnalyses.main.ResultPanelTranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.TranscriptomeAnalysesTopComponentTopComponent;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

@ActionID(
        category = "Window",
        id = "de.cebitec.readXplorer.transcriptomeAnalyses.excelImporter.ImportAction")
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
        
        // hier noch einen NotifyDescriptor, damit der user die Info geben kann, welche 
        // Art von TranscriptomeAnalyse Excel er importieren möchte.
        
        
        ExcelImportFileChooser fc = new ExcelImportFileChooser(new String[]{"xls"}, "xls");
       
        DefaultTableModel model = fc.getModel();
        ResultPanelTranscriptionStart importPanel = new ResultPanelTranscriptionStart();
        importPanel.setDefaultTableModelToTable(model);
        importPanel.setReferenceViewer(refViewer);
//        ImportPanel importPanel = new ImportPanel(model);
        transcAnalysesTopComp.openAnalysisTab("Name", importPanel);
    }
}
