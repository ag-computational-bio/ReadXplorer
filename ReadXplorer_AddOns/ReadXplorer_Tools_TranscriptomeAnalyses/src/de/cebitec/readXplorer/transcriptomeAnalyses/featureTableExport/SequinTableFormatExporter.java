/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.OperonAdjacency;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import org.openide.util.Exceptions;

/**
 *
 * @author jritter
 */
public class SequinTableFormatExporter extends Thread {

    private File outputLocation;
    private ArrayList<TranscriptionStart> tssInputList;
    private ArrayList<Operon> operonInputList;
    private ArrayList<NovelTranscript> novelRegionInputList;
    private TableType tableType;
    private final String featureName;

    public SequinTableFormatExporter(File outputFile, ArrayList<TranscriptionStart> tssInputList, ArrayList<Operon> operonInputList, ArrayList<NovelTranscript> novelRegionInputList, TableType tableType, String featureName) {
        this.outputLocation = outputFile;
        this.tssInputList = tssInputList;
        this.operonInputList = operonInputList;
        this.novelRegionInputList = novelRegionInputList;
        this.tableType = tableType;
        this.featureName = featureName;
    }

    @Override
    public void run() {
        createSequinTableFormattedFile(outputLocation, tssInputList, operonInputList, novelRegionInputList, tableType, featureName);
    }

    /**
     *
     * @param outputFile
     * @param tssList
     * @param operonList
     * @param novelRegionList
     * @param tableType
     * @param featureName
     */
    public void createSequinTableFormattedFile(File outputFile, ArrayList<TranscriptionStart> tssList, ArrayList<Operon> operonList, ArrayList<NovelTranscript> novelRegionList, TableType tableType, String featureNames) {

        try {
            Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(outputFile)));
            writer.write(">Feature " + featureName + "\n");
            TranscriptionStart tss = null;

            if (tssList != null && !tssList.isEmpty() && tableType.equals(TableType.TSS_TABLE)) {
                ArrayList<ExportFeature> featuresToExport = (ArrayList<ExportFeature>) prepareTssForExport(tssList);

                for (ExportFeature feat : featuresToExport) {
                    exportTSS(feat, writer);
                }
            }
            if (novelRegionList != null && !novelRegionList.isEmpty()) {
                if (tableType.equals(TableType.NOVEL_REGION_TABLE)) {
                    NovelTranscript novel = null;
                    boolean isFwd;
                    for (Object object : novelRegionList) {
                        novel = (NovelTranscript) object;
                        isFwd = novel.isFWD();

                        // NC-RNA
                        writer.write(createSequinTableEntry(novel.getStartPosition(), novel.getDropOffPos(), FeatureKey.NC_RNA));
                        if (novel.getSite().equals("cis-antisense")) {
                            writer.write(generateSecondLine(Qualifier.NC_RNA, "antisense_RNA"));
                        } else {
                            writer.write(generateSecondLine(Qualifier.NC_RNA, "other"));
                        }
                    }
                }
            }
            if (operonList != null && !operonList.isEmpty()) {
                if (tableType.equals(TableType.OPERON_TABLE)) {
                    Operon operon = null;
                    boolean isFwd;
                    for (Object object : operonList) {
                        operon = (Operon) object;
                        isFwd = operon.isFwd();

                        int start = operon.getOperonAdjacencies().get(0).getFeature1().getStart();
                        int stop = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getStop();
                        writer.write(createSequinTableEntry(start, stop, FeatureKey.OPERON));
                        writer.write(generateSecondLine(Qualifier.OPERON, operon.toOperonString()));

                        for (Iterator<OperonAdjacency> it = operon.getOperonAdjacencies().iterator(); it.hasNext();) {
                            OperonAdjacency operonAdjacency = it.next();

                            if (it.hasNext()) {
                                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, operonAdjacency.getFeature1().getLocus()));

                            } else {
                                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, operonAdjacency.getFeature2().getLocus()));
                            }
                        }

                        writer.write(createSequinTableEntry(start, stop, FeatureKey.MRNA));
                        writer.write(generateSecondLine(Qualifier.OPERON, operon.getOperonAdjacencies().get(0).getFeature1().getLocus()));
                    }
                }
            }


            writer.close();
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }

    /**
     * The sequence identifier (SeqId) must be the same as that used on the
     * sequence. The table_name is optional. Subsequent lines of the table list
     * the features. Each feature is on a separate line. Qualifiers describing
     * that feature are on the line below. Columns are separated by tabs. Column
     * 1: Start location of feature Column 2: Stop location of feature Column 3:
     * Feature key Line2: Column 4: Qualifier key Column 5: Qualifier value
     *
     * @return a tab separated String containing the start and stop location of
     * the feature.
     */
    private String createSequinTableEntry(int startLocation, int stopLocation, FeatureKey featureKey) {
        String sequinTableEntry = "";
        if (featureKey == FeatureKey.FiveUTR) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t" + "5'UTR\n";
        } else if (featureKey == FeatureKey.MINUS_TEN_SIGNAL) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t" + "-10_signal\n";
        } else if (featureKey == FeatureKey.MINUS_THIRTYFIVE_SIGNAL) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t" + "-35_signal\n";
        } else if (featureKey == FeatureKey.CDS) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t"
                    + "CDS" + "\n";
        } else if (featureKey == FeatureKey.MRNA) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t"
                    + "mRNA" + "\n";
        } else if (featureKey == FeatureKey.RBS) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t"
                    + "RBS" + "\n";
        } else {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t"
                    + featureKey.toString().toLowerCase() + "\n";
        }
        return sequinTableEntry;
    }

    /**
     * Generates the second line of an feature entry.
     *
     * @param qualifierKey
     * @param qualifierValue
     * @return
     */
    private String generateSecondLine(Qualifier qualifierKey, String qualifierValue) {
        String sequinTableEntry = "";
        if (qualifierKey == Qualifier.EC_NUMBER) {
            sequinTableEntry = "\t\t\t"
                    + "EC_number"
                    + "\t" + qualifierValue.toString() + "\n";
        } else if (qualifierKey == Qualifier.NC_RNA) {
            sequinTableEntry = "\t\t\t"
                    + "ncRNA"
                    + "\t" + qualifierValue.toString() + "\n";
        } else if (qualifierKey == Qualifier.PCR_CONTITIONS) {
            sequinTableEntry = "\t\t\t"
                    + "PCR_conditions"
                    + "\t" + qualifierValue.toString() + "\n";
        } else if (qualifierKey == Qualifier.PCR_PRIMERS) {
            sequinTableEntry = "\t\t\t"
                    + "PCR_primers"
                    + "\t" + qualifierValue.toString() + "\n";
        } else {
            sequinTableEntry = "\t\t\t"
                    + qualifierKey.toString().toLowerCase()
                    + "\t" + qualifierValue.toString() + "\n";
        }
        return sequinTableEntry;
    }

    private void exportTSS(ExportFeature exportFeat, Writer writer) throws IOException {
        PersistantFeature feature = exportFeat.getFeature();
        boolean isFwd = feature.isFwdStrand();
        int geneStart;

        if (feature.getType() == FeatureType.CDS || feature.getType() == FeatureType.GENE) {
            // GENE
            geneStart = exportFeat.getGeneStart();
            writer.write(createSequinTableEntry(geneStart, feature.getStop(), FeatureKey.GENE));

            if (feature.hasLocus()) {
                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
            }
            if (feature.hasFeatureName() && !feature.getName().equals("")) {
                writer.write(generateSecondLine(Qualifier.STANDARD_NAME, feature.getName()));
            }
            if (!feature.getProduct().equals("")) {
                writer.write(generateSecondLine(Qualifier.PRODUCT, feature.getProduct()));
            }
//            // CDS
//            writer.write(createSequinTableEntry(isFwd, feature.getStart(), feature.getStop(), FeatureKey.CDS));
//            writer.write(generateSecondLine(Qualifier.PRODUCT, feature.getProduct()));
//            writer.write(generateSecondLine(Qualifier.CODON_START, "" + feature.getFrame())); // Frame
//            if (!feature.getEcNumber().equals("")) {
//                writer.write(generateSecondLine(Qualifier.EC_NUMBER, feature.getEcNumber()));
//            }
//            if (feature.hasLocus()) {
//                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
//            }


            for (Integer start : exportFeat.getTssPositions()) {
                // mRNA
                if (isFwd) {
                    writer.write(createSequinTableEntry(start, feature.getStop(), FeatureKey.MRNA));
                } else {
                    writer.write(createSequinTableEntry(start, feature.getStart(), FeatureKey.MRNA));
                }
                if (feature.hasLocus()) {
                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                }
                if (!feature.getProduct().equals("")) {
                    writer.write(generateSecondLine(Qualifier.PRODUCT, feature.getProduct()));
                }

                // 5'-UTR
                if (isFwd) {
                    writer.write(createSequinTableEntry(start, feature.getStart(), FeatureKey.FiveUTR));
                } else {
                    writer.write(createSequinTableEntry(start, feature.getStop(), FeatureKey.FiveUTR));
                }
                if (feature.hasLocus()) {
                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                }

                if (exportFeat.getRbsAssignments().get(start)) {
                    // RBS 
                    if (isFwd) {
                        int startMotif = exportFeat.getFeature().getStart() - exportFeat.getRbsSequenceLength() + exportFeat.getRbsPosistions().get(start);
                        int stopMotif = startMotif + exportFeat.getRbsMotifWidth();
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.RBS));
                    } else {
                        int startMotif = exportFeat.getFeature().getStop() + exportFeat.getRbsSequenceLength() - exportFeat.getRbsPosistions().get(start);
                        int stopMotif = startMotif - exportFeat.getRbsMotifWidth();
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.RBS));
                    }
                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                }

                if (exportFeat.getPromotorAssignments().get(start)) {
                    //PROMOTOR
                    if (isFwd) {
                        if (exportFeat.getMinus35Positions().containsKey(start)) {
                            writer.write(createSequinTableEntry(start - exportFeat.getPromotorSequenceLength() + exportFeat.getMinus35Positions().get(start), start, FeatureKey.PROMOTER));
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                        } else if (exportFeat.minus10Positions.containsKey(start)) {
                            writer.write(createSequinTableEntry(start - exportFeat.getPromotorSequenceLength() + exportFeat.getMinus10Positions().get(start), start, FeatureKey.PROMOTER));
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                        }
                    } else {
                        if (exportFeat.getMinus35Positions().containsKey(start)) {
                            writer.write(createSequinTableEntry(start + exportFeat.getPromotorSequenceLength() - exportFeat.getMinus35Positions().get(start), start, FeatureKey.PROMOTER));
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                        } else if (exportFeat.minus10Positions.containsKey(start)) {
                            writer.write(createSequinTableEntry(start + exportFeat.getPromotorSequenceLength() - exportFeat.getMinus10Positions().get(start), start, FeatureKey.PROMOTER));
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, feature.getLocus()));
                        }
                    }


                    // -10 signal
                    if (isFwd) {
                        int startMotif = start - exportFeat.getPromotorSequenceLength() + exportFeat.getMinus10Positions().get(start);
                        int stopMotif = startMotif + (exportFeat.getMinus10MotifWidth()-1);
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_TEN_SIGNAL));
                    } else {
                        int startMotif = start + exportFeat.getPromotorSequenceLength() - exportFeat.getMinus10Positions().get(start);
                        int stopMotif = startMotif - (exportFeat.getMinus10MotifWidth()-1);
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_TEN_SIGNAL));
                    }

                    // -35 signal
                    if (isFwd) {
                        int startMotif = start - exportFeat.getPromotorSequenceLength() + exportFeat.getMinus35Positions().get(start);
                        int stopMotif = startMotif + (exportFeat.getMinus35MotifWidth()-1);
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
                    } else {
                        int startMotif = start + exportFeat.getPromotorSequenceLength() - exportFeat.getMinus35Positions().get(start);
                        int stopMotif = startMotif - (exportFeat.getMinus35MotifWidth()-1);
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
                    }
                }
            }
        }
    }

    /**
     * This method clusters information of all transcription start sites, which
     * assigned to the same CDS into a ExportFeature. This featuer continains
     * all export information.
     *
     * @param tss List of TranscriptionStart instances.
     * @return List of ExportFeature instances.
     */
    private List<ExportFeature> prepareTssForExport(List<TranscriptionStart> tss) {
        List<ExportFeature> features = new ArrayList<>();
        TreeMap<String, ExportFeature> uniqueFeatures = new TreeMap<>();
        ExportFeature feat;
        for (TranscriptionStart ts : tss) {
            if (uniqueFeatures.containsKey(ts.getAssignedFeature().getLocus())) {
                feat = uniqueFeatures.get(ts.getAssignedFeature().getLocus());
                feat.setValues(ts.getStartPosition(), ts.getStartRbsMotif(), ts.getStartMinus10Motif(), ts.getStartMinus35Motif(), ts.hasPromotorFeaturesAssigned(), ts.hasRbsFeatureAssigned(), ts.getPromotorSequenceLength(), ts.getRbsSequenceLength());
                uniqueFeatures.put(ts.getAssignedFeature().getLocus(), feat);
            } else {
                feat = new ExportFeature(ts.getAssignedFeature(), ts.getMinus10MotifWidth(), ts.getMinus35MotifWidth(), ts.getRbsMotifWidth());
                feat.setValues(ts.getStartPosition(), ts.getStartRbsMotif(), ts.getStartMinus10Motif(), ts.getStartMinus35Motif(), ts.hasPromotorFeaturesAssigned(), ts.hasRbsFeatureAssigned(), ts.getPromotorSequenceLength(), ts.getRbsSequenceLength());
                uniqueFeatures.put(ts.getAssignedFeature().getLocus(), feat);
            }
        }

        features.addAll(uniqueFeatures.values());

        return features;
    }
}
