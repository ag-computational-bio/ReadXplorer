package de.cebitec.readXplorer.exporter.tables;

import de.cebitec.readXplorer.util.fileChooser.ReadXplorerFileChooser;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import jxl.write.WriteException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;

/**
 * A file chooser for storing any kind of ExportDataI data in a table file.
 * 
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class TableExportFileChooser extends ReadXplorerFileChooser {
    
    private static final long serialVersionUID = 1L;
    
    private ProgressHandle progressHandle;
    
    /**
     * Creates a new file chooser for saving ExportDataI data into an excel
     * file.
     * @param fileExtensions the file extension of the excel file (typically
     * xls)
     * @param fileDescription description of the file extension
     * @param exportData the data object to be exported (needs to implement {@link ExportDataI}).
     */
    public TableExportFileChooser(final String[] fileExtensions, String fileDescription, ExportDataI exportData) {
        super(fileExtensions, fileDescription, exportData);
        this.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
    }
    
    /**
     * Creates a new file chooser for saving ExportDataI data into a table
     * file. This constructor allows to add multiple file filters for different
     * table formats.
     * @param extensionFilters the file extensions of the table file (typically
     * xls and csv)
     * @param exportData the data object to be exported (needs to implement
     * {@link ExportDataI}).
     */
    public TableExportFileChooser(FileNameExtensionFilter[] extensionFilters, ExportDataI exportData) {
        super(null, null, exportData);
        for (FileNameExtensionFilter extensionFilter : extensionFilters) {
            this.addChoosableFileFilter(extensionFilter);
            this.setFileFilter(extensionFilter);       
        }
        this.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
    }

    @Override
    @NbBundle.Messages({
        "IoExceptionMsg=An error occured while reading the specified file",
        "FailHeader=Failure", 
        "ProgressName=Table export progress", 
        "FailMsg=A write error occured during saving progress!", 
        "OomMsg=Out of Memory: Data too large for table export."})
    public void save(final String fileLocation) {

        final String msg = Bundle.IoExceptionMsg();
        final String header = Bundle.FailHeader();

        if (this.data instanceof ExportDataI) {

            final ExportDataI exportData = (ExportDataI) this.data;
            final List<List<List<Object>>> tableData = exportData.dataToExcelExportList();
            int size = 0;
            for (List<List<Object>> dataList : tableData) {
                size += dataList.size();
            }
            this.progressHandle = ProgressHandleFactory.createHandle(Bundle.ProgressName());
            this.progressHandle.start(size + 1);

            Thread exportThread = new Thread(new Runnable() {

                @Override
                public void run() {
                    TableExporterI exporter;
                    if (getFileFilter().getDescription().contains("xls")) {
                        exporter = new ExcelExporter(progressHandle);
                    } else { //file filter contains csv
                        exporter = new CsvExporter(progressHandle);
                    }
                    exporter.setHeaders(exportData.dataColumnDescriptions());
                    exporter.setExportData(tableData);
                    exporter.setSheetNames(exportData.dataSheetNames());
                    if (exporter.readyToExport()) {
                        try {

                            exporter.writeFile(new File(fileLocation));

                        } catch (FileNotFoundException ex) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), msg, header, JOptionPane.ERROR_MESSAGE);
                        } catch (WriteException ex) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), Bundle.FailMsg(),
                                    Bundle.FailHeader(), JOptionPane.ERROR_MESSAGE);
                        } catch (OutOfMemoryError e) {
                            JOptionPane.showMessageDialog(JOptionPane.getRootFrame(), Bundle.OomMsg(),
                                    Bundle.FailHeader(), JOptionPane.INFORMATION_MESSAGE);
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
    
    public static final String[] CSV_EXTENSIONS = new String[]{"csv", "CSV", "Csv"};
    public static final String[] XLS_EXTENSIONS = new String[]{"xls", "XLS", "Xls"};
    public static final String CSV_DESCRIPTION = "csv";
    public static final String XLS_DESCRIPTION = "xls";
    
    /**
     * @return All available table file extensions filters and their
     * corresponding descriptions. The first element of the pair are the filters
     * and the second element are the respective descriptions.
     */
    public static FileNameExtensionFilter[] getTableFileExtensions() {
        FileNameExtensionFilter[] filters = new FileNameExtensionFilter[]{
            new FileNameExtensionFilter(CSV_DESCRIPTION, CSV_EXTENSIONS), 
            new FileNameExtensionFilter(XLS_DESCRIPTION, XLS_EXTENSIONS)};
        return filters;
    }

}
