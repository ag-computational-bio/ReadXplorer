package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
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
        dataColumnDescriptions.add("Correct Start Annotation");
        dataColumnDescriptions.add("Correct Start Annotation Start");
        dataColumnDescriptions.add("Correct Start Annotation Stop");
        dataColumnDescriptions.add("Next Upstream Annotation");
        dataColumnDescriptions.add("Next Upstream Annotation Start");
        dataColumnDescriptions.add("Next Upstream Annotation Stop");
        dataColumnDescriptions.add("Next Downstream Annotation");
        dataColumnDescriptions.add("Next Downstream Annotation Start");
        dataColumnDescriptions.add("Next Downstream Annotation Stop");
        
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
            
            DetectedAnnotations detAnnotations = geneStart.getDetAnnotations();
            PersistantAnnotation annotation = detAnnotations.getCorrectStartAnnotation();
            geneStartRow.add(annotation != null ? PersistantAnnotation.getAnnotationName(annotation) : "-");
            geneStartRow.add(annotation != null ? annotation.getStart() : "-");
            geneStartRow.add(annotation != null ? annotation.getStop() : "-");
            
            annotation = detAnnotations.getUpstreamAnnotation();
            geneStartRow.add(annotation != null ? PersistantAnnotation.getAnnotationName(annotation) : "-");
            geneStartRow.add(annotation != null ? annotation.getStart() : "-");
            geneStartRow.add(annotation != null ? annotation.getStop() : "-");
            
            annotation = detAnnotations.getDownstreamAnnotation();
            geneStartRow.add(annotation != null ? PersistantAnnotation.getAnnotationName(annotation) : "-");
            geneStartRow.add(annotation != null ? annotation.getStart() : "-");
            geneStartRow.add(annotation != null ? annotation.getStop() : "-");
            
            geneStartsExport.add(geneStartRow);
        }
        
        return geneStartsExport;
    }
}
