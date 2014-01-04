/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelRegion;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class SequinTableFormatExporter extends Thread {

    private File outputLocation;
    private List<Object> inputList;
    private TableType tableType;
    private PersistantReference refGenome;
    private String tableName;

    public SequinTableFormatExporter(File outputFile, List<Object> inputList, TableType tableType, PersistantReference refGenome, String tableName) {
        this.outputLocation = outputFile;
        this.inputList = inputList;
        this.tableType = tableType;
        this.refGenome = refGenome;
        this.tableName = tableName;
    }

    @Override
    public void run() {
        createSequinTableFormattedFile(outputLocation, inputList, tableType, refGenome, tableName);
    }

    /**
     *
     * @param outputFile
     * @param list
     * @param tableType
     * @param refGenome
     * @param tableName
     */
    public void createSequinTableFormattedFile(File outputFile, List<Object> list, TableType tableType, PersistantReference refGenome, String tableName) {
        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile)));


            if (list != null && !list.isEmpty()) {
                if (tableType.equals(TableType.TSS_TABLE)) {

                    TranscriptionStart tss = null;
                    writer.write(createFirstSequinTableEntry(refGenome.getName(), tableName));
//                    writer.write(createSequinTableEntry(refGenome, MIN_PRIORITY, FeatureKey.SOURCE))
                    for (Object object : list) {
                        tss = (TranscriptionStart) object;
                        PersistantFeature feature = tss.getAssignedFeature();

                        // GENE
                        writer.write(createSequinTableEntry(tss.getStartPosition(), feature.getStop(), FeatureKey.GENE));
                        writer.write(generateSecondLine(Qualifier.GENE, feature.toString()));
                        writer.write(generateSecondLine(Qualifier.STRAIN, feature.toString()));
                        if (feature.hasLocus()) {
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                        }
                        // CDS
                        writer.write(createSequinTableEntry(feature.getStart(), feature.getStop(), FeatureKey.CDS));
                        writer.write(generateSecondLine(Qualifier.PRODUCT, feature.getProduct()));
                        writer.write(generateSecondLine(Qualifier.CODON_START, "" + feature.getFrame())); // Frame?
                        writer.write(generateSecondLine(Qualifier.EC_NUMBER, feature.getEcNumber()));
                        if (feature.hasLocus()) {
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                        }
                        // mRNA
                        writer.write(createSequinTableEntry(tss.getStartPosition(), feature.getStop(), FeatureKey.MRNA));
                        writer.write(generateSecondLine(Qualifier.GENE, feature.toString()));
                        writer.write(generateSecondLine(Qualifier.PRODUCT, feature.getProduct()));

                        // PROMOTOR
                        writer.write(createSequinTableEntry(tss.getStartPosition() - tss.getSequence().length(), tss.getStartPosition(), FeatureKey.PROMOTOR));

                        // RBS 
//                        writer.write(createSequinTableEntry(tss.getRbsStart, tss.getRbsStop, FeatureKey.RBS));
//                        writer.write(generateSecondLine(Qualifier.GENE, feature.toString()));
//
//                        // 5'-UTR
//                        writer.write(createSequinTableEntry(tss.getStartPosition(), feature.getStart(), FeatureKey.FiveUTR));
//                        writer.write(generateSecondLine(Qualifier.GENE, feature.toString()));
//                        // -10 signal
//                        writer.write(createSequinTableEntry(tss.getMinusTenSignalStart, feature.getMinusTenSignalStop, FeatureKey.MINUS_TEN_SIGNAL));
//                        writer.write(generateSecondLine(Qualifier.GENE, feature.toString()));
//
//                        // -35 signal
//                        writer.write(createSequinTableEntry(tss.getMinus35SignalStart, feature.getMinus35SignalStop, FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
//                        writer.write(generateSecondLine(Qualifier.GENE, feature.toString()));
                    }
                } else if (tableType.equals(TableType.NOVEL_REGION_TABLE)) {
                    NovelRegion novel = null;
                    writer.write(createFirstSequinTableEntry(refGenome.getName(), tableName));
                    for (Object object : list) {
                        novel = (NovelRegion) object;

                        // NC-RNA
                        writer.write(createSequinTableEntry(novel.getPos(), novel.getDropOffPos(), FeatureKey.NC_RNA));
                        if (novel.getSite().equals("cis-antisense")) {
                            writer.write(generateSecondLine(Qualifier.NC_RNA, "antisense_RNA"));
                        } else {
                            writer.write(generateSecondLine(Qualifier.NC_RNA, "other"));
                        }
                    }
                } else if (tableType.equals(TableType.OPETON_TABLE)) {
                    Operon operon = null;
                    writer.write(createFirstSequinTableEntry(refGenome.getName(), tableName));
                    for (Object object : list) {
                        operon = (Operon) object;

                        int start = operon.getOperonAdjacencies().get(0).getFeature1().getStart();
                        int stop = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getStop();
                        writer.write(createSequinTableEntry(start, stop, FeatureKey.OPERON));
                        writer.write(generateSecondLine(Qualifier.OPERON, operon.toOperonString()));
                    }
                }
            }

        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * The first line of the table contains the following basic information.
     *
     * >Feature SeqId table_name
     *
     * @param seqID
     * @return
     */
    private String createFirstSequinTableEntry(String seqID, String tableName) {
        String firstSequinTableEntry = "";

//        !!!!!!!!
//        !!TODO!!
//        !!!!!!!!
        return firstSequinTableEntry;
    }

    /**
     * The sequence identifier (SeqId) must be the same as that used on the
     * sequence. The table_name is optional. Subsequent lines of the table list
     * the features. Each feature is on a separate line. Qualifiers describing
     * that feature are on the line below. Columns are separated by tabs. Column
     * 1: Start location of feature Column 2: Stop location of feature Column 3:
     * Feature key Line2: Column 4: Qualifier key Column 5: Qualifier value
     *
     * @return
     */
    private String createSequinTableEntry(int startLocation, int stopLocation, FeatureKey featureKey) {
        String sequinTableEntry = "";



        return sequinTableEntry;
    }

    private String generateSecondLine(Qualifier qualifierKey, String qualifierValue) {
        String sequinTableEntry = "";



        return sequinTableEntry;
    }
}
