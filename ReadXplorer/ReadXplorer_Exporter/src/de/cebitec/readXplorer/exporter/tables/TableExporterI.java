package de.cebitec.readXplorer.exporter.tables;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import jxl.write.WriteException;

/**
 * Interface to use for table exporters.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public interface TableExporterI {
    
    /**
     * @param headers the header list for the tables which should be exported to
     * the table file. Must be set!
     */
    public void setHeaders(List<List<String>> headers);
    
    /**
     * @param dataSheetNames the sheet name listf for the sheets which should be
     * exported to the table file. Must be set!
     */
    public void setSheetNames(List<String> dataSheetNames);

    /**
     * @param exportData The list of data which should be exported to a table 
     * file. Must be set!
     */
    public void setExportData(List<List<List<Object>>> exportData);
    
    /**
     * Carries out the whole export process to a table file.
     * @param file the file in which to write the data.
     * @return the file in which the data was written.
     * @throws FileNotFoundException
     * @throws IOException
     * @throws OutOfMemoryError
     * @throws WriteException
     */
    public File writeFile(File file) throws FileNotFoundException, IOException, OutOfMemoryError, WriteException;
    
    /**
     * @return true, if the complete export process can be started by calling
     * {@link writeFile()}.
     */
    public boolean readyToExport();
}
