package de.cebitec.vamp.exporter.excel;

import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JOptionPane;
import jxl.write.WriteException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * @author rhilker
 *  
 * A file chooser for storing any kind of ExcelExportDataI data in an excel sheet.
 */
public class ExcelExportFileChooser extends VampFileChooser {
    
    private ProgressHandle progressHandle;
    private String tableName;
    
    /**
     * Creates a new file chooser for saving ExcelExportDataI data into an excel file.
     * @param fileExtensions the file extension of the excel file (typically xls)
     * @param exportData the data object to be exported (needs to implement {@link ExcelExportDataI}).
     * @param tableName the name of the table to write
     */
    public ExcelExportFileChooser(final String[] fileExtensions, String fileDescription, ExcelExportDataI exportData, String tableName) {
        super(VampFileChooser.SAVE_DIALOG, fileExtensions, fileDescription, exportData);
        
        this.tableName = tableName;
    }

    @Override
    public void save(final String fileLocation) {

        final String msg = NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.IoExceptionMsg",
                "An error occured while reading the specified file");
        final String header = NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.FailHeader", "Error");

        if (this.data instanceof ExcelExportDataI) {

            final ExcelExportDataI exportData = (ExcelExportDataI) this.data;
            this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.progress.name"));
            progressHandle.start(exportData.dataToExcelExportList().size() + 1);

            Thread exportThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    ExcelExporter exporter = new ExcelExporter(tableName, progressHandle);
                    exporter.setHeaders(exportData.dataColumnDescriptions());
                    exporter.setExportData(exportData.dataToExcelExportList());
                    if (exporter.readyToExport()) {
                        try {

                            exporter.writeFile(new File(fileLocation));

                        } catch (FileNotFoundException ex) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                        } catch (WriteException ex) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.FailMsg"),
                                    NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.FailHeader"), JOptionPane.ERROR_MESSAGE);
                        } catch (OutOfMemoryError e) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.OomMsg"),
                                    NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.FailHeader"), JOptionPane.INFORMATION_MESSAGE);
                        }
                        progressHandle.finish();
                    }
                }
            });
            exportThread.start();
        }
    }

    @Override
    public void open(String fileLocation) {
        throw new UnsupportedOperationException("Open dialog not supported!");
        //this is a save dialog, so nothing to do here
        //refactor when open option is needed and add funcationality
    }

}
