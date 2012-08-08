package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
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
    public List<String> dataColumnDescriptions() {
        List<String> dataColumnDescriptions = new ArrayList();

        dataColumnDescriptions.add("Annotation 1");
        dataColumnDescriptions.add("Annotation 2");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Start Anno 1");
        dataColumnDescriptions.add("Start Anno 2");
        dataColumnDescriptions.add("Reads Overlap Stop 1");
        dataColumnDescriptions.add("Reads Overlap Start 2");
        dataColumnDescriptions.add("Internal Reads");
        dataColumnDescriptions.add("Spanning Reads");

        return dataColumnDescriptions;
    }

    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> operonExport = new ArrayList<List<Object>>();

        for (Operon operon : operonDetection) {
            String annoName1 = "";
            String annoName2 = "";
            String strand = (operon.getOperonAdjacencies().get(0).getAnnotation1().isFwdStrand() ? "Fwd" : "Rev") + "\n";
            String startAnno1 = "";
            String startAnno2 = "";
            String readsAnno1 = "";
            String readsAnno2 = "";
            String internalReads = "";
            String spanningReads = "";
            
            for (OperonAdjacency opAdj : operon.getOperonAdjacencies()) {
                annoName1 += opAdj.getAnnotation1().getLocus() + "\n";
                annoName2 += opAdj.getAnnotation2().getLocus() + "\n";
                startAnno1 += opAdj.getAnnotation1().getStart() + "\n";
                startAnno2 += opAdj.getAnnotation2().getStart() + "\n";
                readsAnno1 += opAdj.getReadsAnnotation1() + "\n";
                readsAnno2 += opAdj.getReadsAnnotation2() + "\n";
                internalReads += opAdj.getInternalReads() + "\n";
                spanningReads += opAdj.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<Object>();
            operonsRow.add(annoName1);
            operonsRow.add(annoName2);
            operonsRow.add(strand);
            operonsRow.add(startAnno1);
            operonsRow.add(startAnno2);
            operonsRow.add(readsAnno1);
            operonsRow.add(readsAnno2);
            operonsRow.add(internalReads);
            operonsRow.add(spanningReads);

            operonExport.add(operonsRow);
        }
        return operonExport;
    }
}
