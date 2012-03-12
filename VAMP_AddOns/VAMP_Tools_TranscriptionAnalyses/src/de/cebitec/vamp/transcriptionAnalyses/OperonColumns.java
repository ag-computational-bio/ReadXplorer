/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptionAnalyses;

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

        dataColumnDescriptions.add("Gen1");
        dataColumnDescriptions.add("Gen2");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Position of Gen1");
        dataColumnDescriptions.add("Position of Gen2");
        dataColumnDescriptions.add("read cover Gen1");
        dataColumnDescriptions.add("read cover Gen2");
        dataColumnDescriptions.add("read cover none");
        dataColumnDescriptions.add("read cover Gen1 and Gen2");

        return dataColumnDescriptions;
    }

    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> operonExport = new ArrayList<List<Object>>();

        for (Operon operon : operonDetection) {
            String genName1 = "";
            String genName2 = "";
            String strand = "";
            String pos1 = "";
            String pos2 = "";
            String coverGen1 = "";
            String coverGen2 = "";
            String coverNone = "";
            String coverGen1And2 = "";
            for (OperonAdjacency ad : operon.getOperon()) {
                genName1 += ad.getOperonFeature().getLocus() + "\n";
                genName2 += ad.getOperonFeature2().getLocus() + "\n";
                strand += (ad.getOperonFeature().getStrand() == SequenceUtils.STRAND_FWD ? "Fwd" : "Rev") + "\n";
                pos1 += ad.getOperonFeature().getStart() + "\n";
                pos2 += ad.getOperonFeature2().getStart() + "\n";
                coverGen1 += ad.getRead_cover_Gen1() + "\n";
                coverGen2 += ad.getRead_cover_Gen2() + "\n";
                coverNone += ad.getRead_cover_none() + "\n";
                coverGen1And2 += ad.getRead_cover_Gen1_and_Gen2() + "\n";

            }
            List<Object> operonsRow = new ArrayList<Object>();
            operonsRow.add(genName1);
            operonsRow.add(genName2);
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
