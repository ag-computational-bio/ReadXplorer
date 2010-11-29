package de.cebitec.vamp.exporter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

/**
 *
 * @author jstraube
 */
public interface ExporterI {

    public boolean readyToExport();

    public File writeFile(File tempDir, String name)throws FileNotFoundException, IOException;

    public void addColumn(WritableSheet sheet, String celltype, Object cellvalue,
            int column, int row) throws WriteException;

 // public void fillSheet(WritableSheet sheet, List<Object> object) throws WriteException ;
  
}
