package de.cebitec.vamp.exporter.excel;

import de.cebitec.vamp.databackend.dataObjects.SnpData;
import de.cebitec.vamp.exporter.ExportContoller;
import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author rhilker
 *  
 * A file chooser for storing snp data in an excel sheet.
 * The passed in ActionEvent needs to contain a SnpData object as its source.
 */
public class SnpExcelFileChooser extends VampFileChooser {
    
    /**
     * Creates a new file chooser for saving snp data into an excel file.
     * @param fileExtension the file extension of the excel file (typically xls)
     * @param evt the action event of the export button with the SnpData object,
     * which should be exported, as its source object (setSource(SnpData)).
     */
    public SnpExcelFileChooser(final String fileExtension, ActionEvent evt) {
        super(VampFileChooser.SAVE_DIALOG, fileExtension, evt);
    }
    
    @Override
    public void save(String fileLocation) {
        ActionEvent evt = (ActionEvent) data;
        ExportContoller e = new ExportContoller();
        e.setSnpData((SnpData) evt.getSource());

        File file = new File(fileLocation);
        e.setName(file.getName());
        e.setFile(file.getParent());
        e.actionPerformed(evt);
    }

    @Override
    public void open(String fileLocation) {
        throw new UnsupportedOperationException("Open dialog not supported!");
        //this is a save dialog, so nothing to do here
        //refactor when open option is needed and add funcationality
    }
    
}
