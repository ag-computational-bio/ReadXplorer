package de.cebitec.readXplorer.transcriptomeAnalyses.excelImport;

import de.cebitec.readXplorer.transcriptomeAnalyses.main.TranscriptomeAnalysesTopComponentTopComponent;
import de.cebitec.readXplorer.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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
    private ProgressHandle progressHandle;
    private TranscriptomeAnalysesTopComponentTopComponent transcAnalysesTopComp;

    public ImportAction(ReferenceViewer reference) {
        this.refViewer = reference;
        TopComponent findTopComponent = WindowManager.getDefault().findTopComponent(TranscriptomeAnalysesTopComponentTopComponent.PREFERRED_ID);
        if (findTopComponent != null) {
            this.transcAnalysesTopComp = (TranscriptomeAnalysesTopComponentTopComponent) findTopComponent;
        } else {
            transcAnalysesTopComp = new TranscriptomeAnalysesTopComponentTopComponent();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final ExcelImportFileChooser fc = new ExcelImportFileChooser(new String[]{"xls"}, "xls");
        if (fc.getSelectedFile() != null) {
            this.progressHandle = ProgressHandleFactory.createHandle("Import progress ...");
            this.transcAnalysesTopComp.open();
            Thread exportThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    progressHandle.start(30);
                    ExcelImporter importer = new ExcelImporter(progressHandle);
                    importer.startExcelToTableConverter(fc.getSelectedFile(), refViewer, transcAnalysesTopComp);
                    progressHandle.progress(30);
                }
            });
            exportThread.start();
        }
    }
}
