package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author -Rolf Hilker-
 * 
 * Converts a List of GeneStarts into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 */
public class GeneStartColumns implements ExcelExportDataI {
    
    List<GeneStart> geneStarts;

    /** 
     * Converts a List of GeneStarts into the format readable for the ExcelExporter.
     * Generates both, the header and the data to write.
     * @param geneStarts the list of GeneStarts to convert.
     */
    public GeneStartColumns(List<GeneStart> geneStarts) {
        this.geneStarts = geneStarts;
    }

    
    @Override
    public List<String> dataColumnDescriptions() {
        List<String> dataColumnDescriptions = new ArrayList();
        
        dataColumnDescriptions.add("Position");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Initial Coverage");
        dataColumnDescriptions.add("Gene Start Coverage");
        dataColumnDescriptions.add("Coverage Increase");
        dataColumnDescriptions.add("Coverage Increase %");
        dataColumnDescriptions.add("Correct Start Feature");
        dataColumnDescriptions.add("Correct Start Feature Start");
        dataColumnDescriptions.add("Correct Start Feature Stop");
        dataColumnDescriptions.add("Next Upstream Feature");
        dataColumnDescriptions.add("Next Upstream Feature Start");
        dataColumnDescriptions.add("Next Upstream Feature Stop");
        dataColumnDescriptions.add("Next Downstream Feature");
        dataColumnDescriptions.add("Next Downstream Feature Start");
        dataColumnDescriptions.add("Next Downstream Feature Stop");
        
        return dataColumnDescriptions;
    }

    
    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> geneStartsExport = new ArrayList<List<Object>>();
        
        for (GeneStart geneStart : this.geneStarts) {      
            List<Object> geneStartRow = new ArrayList<Object>();
            
            int percentageIncrease;
            if (geneStart.getInitialCoverage() > 0) {
                percentageIncrease = (int) (((double) geneStart.getStartCoverage() / (double) geneStart.getInitialCoverage()) * 100.0) - 100;
            } else {
                percentageIncrease = Integer.MAX_VALUE;
            }
            
            geneStartRow.add(geneStart.getPos());
            geneStartRow.add(geneStart.getStrand() == SequenceUtils.STRAND_FWD ? "Fwd" : "Rev");
            geneStartRow.add(geneStart.getInitialCoverage());
            geneStartRow.add(geneStart.getStartCoverage());
            geneStartRow.add(geneStart.getStartCoverage() - geneStart.getInitialCoverage());
            geneStartRow.add(percentageIncrease);
            
            DetectedFeatures detFeatures = geneStart.getDetFeatures();
            PersistantFeature feature = detFeatures.getCorrectStartFeature();
            geneStartRow.add(feature != null ? PersistantFeature.getFeatureName(feature) : "-");
            geneStartRow.add(feature != null ? feature.getStart() : "-");
            geneStartRow.add(feature != null ? feature.getStop() : "-");
            
            feature = detFeatures.getUpstreamFeature();
            geneStartRow.add(feature != null ? PersistantFeature.getFeatureName(feature) : "-");
            geneStartRow.add(feature != null ? feature.getStart() : "-");
            geneStartRow.add(feature != null ? feature.getStop() : "-");
            
            feature = detFeatures.getDownstreamFeature();
            geneStartRow.add(feature != null ? PersistantFeature.getFeatureName(feature) : "-");
            geneStartRow.add(feature != null ? feature.getStart() : "-");
            geneStartRow.add(feature != null ? feature.getStop() : "-");
            
            geneStartsExport.add(geneStartRow);
        }
        
        return geneStartsExport;
    }
}
