package de.cebitec.vamp.transcriptionAnalyses;

import de.cebitec.vamp.transcriptionAnalyses.dataStructures.Operon;
import de.cebitec.vamp.transcriptionAnalyses.dataStructures.OperonAdjacency;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author MKD
 */
public class OperonColumns implements ExcelExportDataI {

    private List<Operon> operonDetection;

    public OperonColumns(List<Operon> operonDetection) {
        this.operonDetection = operonDetection;
    }

    @Override
    public List<String> dataColumnDescriptions() {
        List<String> dataColumnDescriptions = new ArrayList();

        dataColumnDescriptions.add("Gene 1");
        dataColumnDescriptions.add("Gene 2");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Start Gene 1");
        dataColumnDescriptions.add("Start Gene 2");
        dataColumnDescriptions.add("Gene 1 Reads");
        dataColumnDescriptions.add("Gene 2 Reads");
        dataColumnDescriptions.add("Internal Reads");
        dataColumnDescriptions.add("Spanning Reads");

        return dataColumnDescriptions;
    }

    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> operonExport = new ArrayList<List<Object>>();

        for (Operon operon : operonDetection) {
            String geneName1 = "";
            String geneName2 = "";
            String strand = "";
            String pos1 = "";
            String pos2 = "";
            String coverGen1 = "";
            String coverGen2 = "";
            String coverNone = "";
            String coverGen1And2 = "";
            for (OperonAdjacency ad : operon.getOperon()) {
                geneName1 += ad.getOperonAnnotation().getLocus() + "\n";
                geneName2 += ad.getOperonAnnotation2().getLocus() + "\n";
                strand += (ad.getOperonAnnotation().getStrand() == SequenceUtils.STRAND_FWD ? "Fwd" : "Rev") + "\n";
                pos1 += ad.getOperonAnnotation().getStart() + "\n";
                pos2 += ad.getOperonAnnotation2().getStart() + "\n";
                coverGen1 += ad.getReadsGene1() + "\n";
                coverGen2 += ad.getReadsGene2() + "\n";
                coverNone += ad.getInternalReads() + "\n";
                coverGen1And2 += ad.getSpanningReads() + "\n";

            }
            List<Object> operonsRow = new ArrayList<Object>();
            operonsRow.add(geneName1);
            operonsRow.add(geneName2);
            operonsRow.add(strand);
            operonsRow.add(pos1);
            operonsRow.add(pos2);
            operonsRow.add(coverGen1);
            operonsRow.add(coverGen2);
            operonsRow.add(coverNone);
            operonsRow.add(coverGen1And2);

            operonExport.add(operonsRow);
        }
        return operonExport;
    }
}
