package de.cebitec.vamp.exporter.excel;

import de.cebitec.vamp.util.fileChooser.VampFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import jxl.write.WriteException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * A file chooser for storing any kind of ExcelExportDataI data in an excel sheet.
 * 
 * @author rhilker
 */
public class ExcelExportFileChooser extends VampFileChooser {
    
    private static final long serialVersionUID = 1L;
    
    private ProgressHandle progressHandle;
    
    /**
     * Creates a new file chooser for saving ExcelExportDataI data into an excel file.
     * @param fileExtensions the file extension of the excel file (typically xls)
     * @param fileDescription description of the file extension
     * @param exportData the data object to be exported (needs to implement {@link ExcelExportDataI}).
     */
    public ExcelExportFileChooser(final String[] fileExtensions, String fileDescription, ExcelExportDataI exportData) {
        super(fileExtensions, fileDescription, exportData);
        this.openFileChooser(VampFileChooser.SAVE_DIALOG);
    }

    @Override
    public void save(final String fileLocation) {

        final String msg = NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.IoExceptionMsg",
                "An error occured while reading the specified file");
        final String header = NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.FailHeader", "Error");

        if (this.data instanceof ExcelExportDataI) {

            final ExcelExportDataI exportData = (ExcelExportDataI) this.data;
            int size = 0;
            for (List<List<Object>> dataList : exportData.dataToExcelExportList()) {
                size += dataList.size();
            }
            this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExcelExporter.class, "ExcelExporter.progress.name"));
            this.progressHandle.start(size + 1);

            Thread exportThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    ExcelExporter exporter = new ExcelExporter(progressHandle);
                    exporter.setHeaders(exportData.dataColumnDescriptions());
                    exporter.setExportData(exportData.dataToExcelExportList());
                    exporter.setSheetNames(exportData.dataSheetNames());
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
