/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.exporter;

import de.cebitec.vamp.api.objects.Snp454;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.WritableSheet;
import jxl.write.WriteException;

/**
 *
 * @author msmith
 */
public class Snp454Exporter implements ExporterI{
    
    private List<Snp454> snps;
    
    public Snp454Exporter(List<Snp454> snps) {
        this.snps = snps;
    }

    @Override
    public boolean readyToExport() {
        if (snps.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public File writeFile(File tempDir, String name) throws FileNotFoundException{
        File tempFile = null;
        try {
            tempFile = new File(tempDir, name+".csv");
 
            FileWriter writer = new FileWriter(tempFile); 
            fillSheet(writer);
            writer.flush();
            writer.close();
            
        } catch (IOException ex) {
            Logger.getLogger(Snp454Exporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tempFile;
    }

    @Override
    public void addColumn(WritableSheet sheet, String celltype, Object cellvalue, int column, int row) throws WriteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private void fillSheet(FileWriter writer) {
        try {
            writer.append("Position");
            writer.append("\t");
            writer.append("Base");
            writer.append("\t");
            writer.append("Reference Base");
            writer.append("\t");
            writer.append("Count");
            writer.append("\t");
            writer.append("%");
            writer.append("\t");
            writer.append("% variation at position");
            for(Snp454 snp : this.snps) {
                writer.append("\n");
                writer.append(Integer.toString(snp.getPosition()));
                writer.append("\t");
                writer.append(snp.getBase());
                writer.append("\t");
                writer.append(snp.getRefBase());
                writer.append("\t");
                writer.append(Integer.toString(snp.getCount()));
                writer.append("\t");
                writer.append(Integer.toString(snp.getPercentage()));
                writer.append("\t");
                writer.append(Integer.toString(snp.getVariationPercentag()));
        }
         } catch (IOException ex) {
            Logger.getLogger(Snp454Exporter.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
