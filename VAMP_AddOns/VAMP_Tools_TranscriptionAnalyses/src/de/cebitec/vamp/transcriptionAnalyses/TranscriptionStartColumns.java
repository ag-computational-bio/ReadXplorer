package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.DetectedAnnotations;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;
import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * @author -Rolf Hilker-
 * 
 * Converts a List of TranscriptionStarts into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 */
public class TranscriptionStartColumns implements ExcelExportDataI {
    
    List<TranscriptionStart> tSS;
    private final List<String> promotorRegions;

    /** 
     * Converts a List of TranscriptionStarts into the format readable for the ExcelExporter.
     * Generates both, the header and the data to write.
     * @param tSS the list of TranscriptionStarts to convert.
     */
    public TranscriptionStartColumns(List<TranscriptionStart> tSS, List<String> promotorRegions) {
        this.tSS = tSS;
        this.promotorRegions = promotorRegions;
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
        dataColumnDescriptions.add("70bp Upstream of Start");
        
        return dataColumnDescriptions;
    }

    
    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> tSSExport = new ArrayList<List<Object>>();
        
        for (int i = 0; i < this.tSS.size(); ++i) {      
            TranscriptionStart geneStart = this.tSS.get(i);
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
           
            geneStartRow.add(this.promotorRegions.get(i));
            
            tSSExport.add(geneStartRow);
        }
        
        return tSSExport;
    }
}
