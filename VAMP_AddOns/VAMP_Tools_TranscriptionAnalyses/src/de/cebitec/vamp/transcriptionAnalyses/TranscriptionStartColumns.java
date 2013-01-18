package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.DetectedFeatures;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TransStartUnannotated;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.TranscriptionStart;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts a List of TranscriptionStarts into the format readable for the ExcelExporter.
 * Generates both, the header and the data to write.
 * 
 * @author -Rolf Hilker-
 */
public class TranscriptionStartColumns implements ExcelExportDataI {
    
    List<TranscriptionStart> tSS;
    private final List<String> promotorRegions;

    /** 
     * Converts a List of TranscriptionStarts into the format readable for the ExcelExporter.
     * Generates both, the header and the data to write.
     * @param tSS the list of TranscriptionStarts to convert.
     * @param promotorRegions  the promotor region for each TSS. Have to be in the same order as the tSS!
     */
    public TranscriptionStartColumns(List<TranscriptionStart> tSS, List<String> promotorRegions) {
        this.tSS = tSS;
        this.promotorRegions = promotorRegions;
    }

    /**
     * @return creates and returns the list of transcription start site descriptions 
     * for the columns of the table.
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();
        
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
        dataColumnDescriptions.add("Unannotated Transcript");
        dataColumnDescriptions.add("Transcript Stop");
        dataColumnDescriptions.add("70bp Upstream of Start");
        
        allSheetDescriptions.add(dataColumnDescriptions);
        
        return allSheetDescriptions;
    }

    /**
     * @return creates and returns the list of transcription start rows belonging 
     * to the transcription start site table.
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();
        
        for (int i = 0; i < this.tSS.size(); ++i) {      
            TranscriptionStart geneStart = this.tSS.get(i);
            List<Object> geneStartRow = new ArrayList<>();
            
            int percentageIncrease;
            if (geneStart.getInitialCoverage() > 0) {
                percentageIncrease = (int) (((double) geneStart.getStartCoverage() / (double) geneStart.getInitialCoverage()) * 100.0) - 100;
            } else {
                percentageIncrease = Integer.MAX_VALUE;
            }
            
            geneStartRow.add(geneStart.getPos());
            geneStartRow.add(geneStart.isFwdStrand() ? "Fwd" : "Rev");
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
            
            if (geneStart instanceof TransStartUnannotated) {
                TransStartUnannotated unannoStart = (TransStartUnannotated) geneStart;
                geneStartRow.add("yes");
                geneStartRow.add(unannoStart.getDetectedStop());
            } else {
                geneStartRow.add("-");
                geneStartRow.add("-");
            }
           
            geneStartRow.add(this.promotorRegions.get(i));
            
            tSSResults.add(geneStartRow);
        }
        
        tSSExport.add(tSSResults);
        
        return tSSExport;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Transcription Analysis Table");
        sheetNames.add("Trans Analysis Parameters/Stats");
        return sheetNames;
    }
}
