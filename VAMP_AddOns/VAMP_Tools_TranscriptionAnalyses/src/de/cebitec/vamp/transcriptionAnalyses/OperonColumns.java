package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MKD, rhilker
 */
public class OperonColumns implements ExcelExportDataI {

    private List<Operon> operonDetection;

    public OperonColumns(List<Operon> operonDetection) {
        this.operonDetection = operonDetection;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Feature 1");
        dataColumnDescriptions.add("Feature 2");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Start Anno 1");
        dataColumnDescriptions.add("Start Anno 2");
        dataColumnDescriptions.add("Reads Overlap Stop 1");
        dataColumnDescriptions.add("Reads Overlap Start 2");
        dataColumnDescriptions.add("Internal Reads");
        dataColumnDescriptions.add("Spanning Reads");
        
        allSheetDescriptions.add(dataColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> exportData = new ArrayList<>();
        List<List<Object>> operonResults = new ArrayList<>();

        for (Operon operon : operonDetection) {
            String annoName1 = "";
            String annoName2 = "";
            String strand = (operon.getOperonAdjacencies().get(0).getFeature1().isFwdStrand() ? "Fwd" : "Rev") + "\n";
            String startAnno1 = "";
            String startAnno2 = "";
            String readsAnno1 = "";
            String readsAnno2 = "";
            String internalReads = "";
            String spanningReads = "";
            
            for (OperonAdjacency opAdj : operon.getOperonAdjacencies()) {
                annoName1 += opAdj.getFeature1().getLocus() + "\n";
                annoName2 += opAdj.getFeature2().getLocus() + "\n";
                startAnno1 += opAdj.getFeature1().getStart() + "\n";
                startAnno2 += opAdj.getFeature2().getStart() + "\n";
                readsAnno1 += opAdj.getReadsFeature1() + "\n";
                readsAnno2 += opAdj.getReadsFeature2() + "\n";
                internalReads += opAdj.getInternalReads() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<>();
            operonsRow.add(annoName1);
            operonsRow.add(annoName2);
            operonsRow.add(strand);
            operonsRow.add(startAnno1);
            operonsRow.add(startAnno2);
            operonsRow.add(readsAnno1);
            operonsRow.add(readsAnno2);
            operonsRow.add(internalReads);
            operonsRow.add(spanningReads);

            operonResults.add(operonsRow);
        }
        
        exportData.add(operonResults);
        
        return exportData;
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Operon Detection Table");
        sheetNames.add("Operon Detection Parameters/Stats");
        return sheetNames;
    }
}
