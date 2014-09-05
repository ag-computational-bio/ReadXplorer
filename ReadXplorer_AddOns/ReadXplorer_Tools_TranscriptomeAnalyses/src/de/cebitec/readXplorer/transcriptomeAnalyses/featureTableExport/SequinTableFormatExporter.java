/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.featureTableExport;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.NovelTranscript;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Operon;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.util.classification.FeatureType;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import org.openide.util.Exceptions;

/**
 * This model class deals with the export of detected genome features in Sequin
 * table format. This is needed for submission of annotation using the tool
 * sequin. For more informotion see:
 * http://www.ncbi.nlm.nih.gov/projects/Sequin/table.html
 *
 * @author jritter
 */
public class SequinTableFormatExporter extends Thread {

    private final File outputLocation;
    private final ArrayList<TranscriptionStart> tssInputList;
    private final ArrayList<Operon> operonInputList;
    private final ArrayList<NovelTranscript> novelRegionInputList;
    private final TableType tableType;
    private final String featureName;
    private final String separator;
    private final Integer prefixLength;
    private final boolean isParsingSelected;

    /**
     * Constructor for this class.
     *
     * @param outputFile the output location for the generated sequin table
     * @param tssInputList list of detected and for submission selected tss
     * instances
     * @param operonInputList list of detected and for submission selected
     * operon instances
     * @param novelRegionInputList list of detected and for submission selected
     * novel transcript instances
     * @param tableType table type
     * @param organismTag name of the organism
     * @param separator separator used in locus_Tags names
     * @param strainLength length of the strain in the locus tag
     * @param isParsingSelected <true> if parsing is selected else <false>
     */
    public SequinTableFormatExporter(File outputFile, ArrayList<TranscriptionStart> tssInputList, ArrayList<Operon> operonInputList, ArrayList<NovelTranscript> novelRegionInputList, TableType tableType, String organismTag, String separator, Integer strainLength, boolean isParsingSelected) {
        this.outputLocation = outputFile;
        this.tssInputList = tssInputList;
        this.operonInputList = operonInputList;
        this.novelRegionInputList = novelRegionInputList;
        this.tableType = tableType;
        this.featureName = organismTag;
        this.separator = separator;
        this.prefixLength = strainLength;
        this.isParsingSelected = isParsingSelected;
    }

    @Override
    public void run() {
        createSequinTableFormattedFile(outputLocation, tssInputList, operonInputList, novelRegionInputList, tableType);
    }

    /**
     * Creates a sequin table for the given table type.
     *
     * @param outputFile the output location for the generated sequin table
     * @param tssList ist of detected and for submission selected tss instances
     * @param operonList list of detected and for submission selected operon
     * instances
     * @param novelRegionList list of detected and for submission selected novel
     * transcript instances
     * @param tableType table type
     */
    public void createSequinTableFormattedFile(File outputFile, ArrayList<TranscriptionStart> tssList, ArrayList<Operon> operonList, ArrayList<NovelTranscript> novelRegionList, TableType tableType) {

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outputFile)))) {
            writer.write(">Feature " + featureName + "\n");

            if (tssList != null && !tssList.isEmpty() && tableType.equals(TableType.TSS_TABLE)) {
                ArrayList<ExportFeature> featuresToExport = (ArrayList<ExportFeature>) prepareTssForExport(tssList);

                for (ExportFeature feat : featuresToExport) {
                    exportTSS(feat, writer);
                }
            }
            if (novelRegionList != null && !novelRegionList.isEmpty()) {
                if (tableType.equals(TableType.NOVEL_TRANSCRIPTS_TABLE)) {
                    NovelTranscript novel = null;
                    boolean isFwd;
                    for (Object object : novelRegionList) {
                        novel = (NovelTranscript) object;
                        isFwd = novel.isFwdDirection();

                        // NC-RNA
                        if (isFwd) {
                            writer.write(createSequinTableEntry(novel.getStartPosition(), novel.getDropOffPos(), FeatureKey.NC_RNA));
                        } else {
                            writer.write(createSequinTableEntry(novel.getStartPosition(), novel.getDropOffPos(), FeatureKey.NC_RNA));
                        }
                        if (novel.getLocation().equals("cis-antisense")) {
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
                        int start = operon.getStartPositionOfOperonTranscript();
                        int stop = operon.getStopPositionOfOperonTranscript();
                        String startLocus = "";
                        if (isFwd) {
                            if (isParsingSelected) {
                                startLocus = parseLocusTag(operon.getOperonAdjacencies().get(0).getFeature1().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, 0);
                            } else {
                                startLocus = operon.getOperonAdjacencies().get(0).getFeature1().getLocus();
                            }
                        } else {
                            if (isParsingSelected) {
                                startLocus = parseLocusTag(operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, 0);
                            } else {
                                startLocus = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus();
                            }
                        }

                        // Feature Operon
                        writer.write(createSequinTableEntry(start, stop, FeatureKey.OPERON));
                        writer.write(generateSecondLine(Qualifier.OPERON, operon.toOperonString()));
                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));

                        int mRnaCnt = 0;
                        int fiveUtrCnt = 0;
                        int promotorCnt = 0;
                        int minus10SignalCnt = 0;
                        int minus35SignalCnt = 0;
                        // Feature mRNA
                        if (operon.getTsSites() != null) {
                            for (Integer tss : operon.getTsSites()) {
                                mRnaCnt++;
                                writer.write(createSequinTableEntry(tss, stop, FeatureKey.MRNA));
                                if (isFwd) {
                                    if (isParsingSelected) {
                                        startLocus = parseLocusTag(operon.getOperonAdjacencies().get(0).getFeature1().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, mRnaCnt);
                                    } else {
                                        startLocus = operon.getOperonAdjacencies().get(0).getFeature1().getLocus();
                                    }
                                } else {
                                    if (isParsingSelected) {
                                        startLocus = parseLocusTag(operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, mRnaCnt);
                                    } else {
                                        startLocus = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus();
                                    }
                                }
                                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                // Feature PROMOTOR beginnin at the begin of -35 signal
                                // (if exists)and ending at the end of the -10 signal.
                                ArrayList<Integer[]> promotor = operon.getPromotor(tss);
                                Integer[] minus10 = promotor.get(1);
                                Integer[] minus35 = promotor.get(0);
                                if (isFwd) {
                                    if (minus35[0] > 0 && minus35[1] > 0) {
                                        minus35SignalCnt++;
                                        writer.write(createSequinTableEntry(tss - minus35[0], tss - minus35[0] + minus35[1], FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
                                        if (isParsingSelected) {
                                            startLocus = parseLocusTag(operon.getOperonAdjacencies().get(0).getFeature1().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, minus35SignalCnt);
                                        } else {
                                            startLocus = operon.getOperonAdjacencies().get(0).getFeature1().getLocus();
                                        }

                                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                    }
                                    if (minus10[0] > 0 && minus10[1] > 0) {
                                        minus10SignalCnt++;
                                        writer.write(createSequinTableEntry(tss - minus10[0], tss - minus10[0] + minus10[1], FeatureKey.MINUS_TEN_SIGNAL));
                                        if (isParsingSelected) {
                                            startLocus = parseLocusTag(operon.getOperonAdjacencies().get(0).getFeature1().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, minus10SignalCnt);
                                        } else {
                                            startLocus = operon.getOperonAdjacencies().get(0).getFeature1().getLocus();
                                        }
                                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                    }
                                } else {
                                    if (minus35[0] > 0 && minus35[1] > 0) {
                                        minus35SignalCnt++;
                                        writer.write(createSequinTableEntry(tss + minus35[0], tss + minus35[0] - minus35[1], FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
                                        if (isParsingSelected) {
                                            startLocus = parseLocusTag(operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, minus35SignalCnt);
                                        } else {
                                            startLocus = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus();
                                        }
                                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                    }
                                    if (minus10[0] > 0 && minus10[1] > 0) {
                                        minus10SignalCnt++;
                                        writer.write(createSequinTableEntry(tss + minus10[0], tss + minus10[0] - minus10[1], FeatureKey.MINUS_TEN_SIGNAL));
                                        if (isParsingSelected) {
                                            startLocus = parseLocusTag(operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, minus10SignalCnt);
                                        } else {
                                            startLocus = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus();
                                        }
                                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                    }
                                }
                            }
                        }
                        // Feature FiveUTR
                        if (operon.getUtRegions() != null) {
                            int cnt = 0;
                            for (Integer utr : operon.getUtRegions()) {
                                cnt++;
                                if (isFwd) {
                                    int utrStart = operon.getOperonAdjacencies().get(0).getFeature1().getStart() - utr;
                                    int utrStop = operon.getOperonAdjacencies().get(0).getFeature1().getStart();
                                    writer.write(createSequinTableEntry(utrStart, utrStop, FeatureKey.FiveUTR));
                                    if (isParsingSelected) {
                                        startLocus = parseLocusTag(operon.getOperonAdjacencies().get(0).getFeature1().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, cnt);
                                    } else {
                                        startLocus = operon.getOperonAdjacencies().get(0).getFeature1().getLocus();
                                    }
                                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                } else {
                                    int utrStart = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getStop() + utr;
                                    int utrStop = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getStart();
                                    writer.write(createSequinTableEntry(utrStart, utrStop, FeatureKey.FiveUTR));
                                    if (isParsingSelected) {
                                        startLocus = parseLocusTag(operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, cnt);
                                    } else {
                                        startLocus = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus();
                                    }
                                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                                }
                            }
                        }
                        // Feature RBS
                        if (operon.getRbsStartStop() != null && operon.getRbsStartStop()[0] > 0 && operon.getRbsStartStop()[1] > 0) {
                            int rbsStart = operon.getRbsStartStop()[0];
                            int rbsStop = operon.getRbsStartStop()[0] + operon.getRbsStartStop()[1];
                            writer.write(createSequinTableEntry(rbsStart, rbsStop, FeatureKey.RBS));
                            if (isParsingSelected) {
                                if (isFwd) {
                                    startLocus = parseLocusTag(operon.getOperonAdjacencies().get(0).getFeature1().getLocus(), ParsingPrefisSuffix.OPERON, separator, prefixLength, 0);
                                } else {
                                    startLocus = operon.getOperonAdjacencies().get(operon.getOperonAdjacencies().size() - 1).getFeature2().getLocus();
                                }
                            }
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, startLocus));
                        }
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
        } else if (featureKey == FeatureKey.NC_RNA) {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t"
                    + "ncRNA" + "\n";
        } else {
            sequinTableEntry += startLocation + "\t" + stopLocation + "\t"
                    + featureKey.toString().toLowerCase() + "\n";
        }
        return sequinTableEntry;
    }

    /**
     * Generates the second line of an feature entry.
     *
     * @param qualifierKey Qualifier key for annotation
     * @param qualifierValue String value for qualifier key
     * @return the second line in the 2 based entry in feature annotation table
     */
    private String generateSecondLine(Qualifier qualifierKey, String qualifierValue) {
        String sequinTableEntry = "";
        if (qualifierKey == Qualifier.EC_NUMBER) {
            sequinTableEntry = "\t\t\t"
                    + "EC_number"
                    + "\t" + qualifierValue + "\n";
        } else if (qualifierKey == Qualifier.NC_RNA) {
            sequinTableEntry = "\t\t\t"
                    + "ncRNA_class" + "\t" + qualifierValue + "\n";
        } else if (qualifierKey == Qualifier.PCR_CONDITIONS) {
            sequinTableEntry = "\t\t\t"
                    + "PCR_conditions"
                    + "\t" + qualifierValue + "\n";
        } else if (qualifierKey == Qualifier.PCR_PRIMERS) {
            sequinTableEntry = "\t\t\t"
                    + "PCR_primers"
                    + "\t" + qualifierValue + "\n";
        } else {
            sequinTableEntry = "\t\t\t"
                    + qualifierKey.toString().toLowerCase()
                    + "\t" + qualifierValue + "\n";
        }
        return sequinTableEntry;
    }

    /**
     * Exports the selected tss features in sequin table format.
     *
     * @param exportFeat contains all necessary information about the feature to
     * export
     * @param writer Writer
     * @throws IOException
     */
    private void exportTSS(ExportFeature exportFeat, Writer writer) throws IOException {
        PersistentFeature feature = exportFeat.getFeature();
        boolean isFwd = feature.isFwdStrand();
        int geneStart;
        int geneStop;
        String parsedLocus;
        int mRnaCnt = 0;
        int fiveUtrCnt = 0;
        int promotorCnt = 0;
        int minus10SignalCnt = 0;
        int minus35SignalCnt = 0;

        if (feature.getType() == FeatureType.CDS || feature.getType() == FeatureType.GENE) {
            // GENE
            geneStart = exportFeat.getGeneStart();
            geneStop = exportFeat.getGeneStop();
            writer.write(createSequinTableEntry(geneStart, geneStop, FeatureKey.GENE));

            if (feature.hasLocus()) {
                if (isParsingSelected) {
                    parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.GENE, separator, prefixLength, 0);
                } else {
                    parsedLocus = feature.getLocus();
                }
                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
            }
            if (feature.hasFeatureName() && !feature.getName().equals("")) {
                writer.write(generateSecondLine(Qualifier.GENE, feature.getName()));
            }
            // CDS
            if (isFwd) {
                writer.write(createSequinTableEntry(feature.getStart(), feature.getStop(), FeatureKey.CDS));
            } else {
                writer.write(createSequinTableEntry(feature.getStop(), feature.getStart(), FeatureKey.CDS));
            }
            if (!feature.getProduct().equals("")) {
                writer.write(generateSecondLine(Qualifier.PRODUCT, feature.getProduct()));
            }
            if (!feature.getEcNumber().equals("")) {
                writer.write(generateSecondLine(Qualifier.EC_NUMBER, feature.getEcNumber()));
            }
            if (feature.hasLocus()) {
                if (isParsingSelected) {
                    parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.CDS, separator, prefixLength, 0);
                } else {
                    parsedLocus = feature.getLocus();
                }
                writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
            }

            if (exportFeat.getTssPositions() != null) {
                if (exportFeat.getTssPositions().size() > 1) {
                    mRnaCnt = 1;
                    fiveUtrCnt = 1;
                    promotorCnt = 1;
                    minus10SignalCnt = 1;
                    minus35SignalCnt = 1;
                }
            }

            for (Integer start : exportFeat.getTssPositions()) {

                // mRNA
                if (isFwd) {
                    writer.write(createSequinTableEntry(start, feature.getStop(), FeatureKey.MRNA));
                } else {
                    writer.write(createSequinTableEntry(start, feature.getStart(), FeatureKey.MRNA));
                }
                if (feature.hasLocus()) {
                    if (isParsingSelected) {
                        parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.mRNA, separator, prefixLength, mRnaCnt);
                    } else {
                        parsedLocus = feature.getLocus();
                    }
                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                }

                // 5'-UTR
                if (isFwd) {
                    if (feature.getStart() - start > 0) {
                        writer.write(createSequinTableEntry(start, feature.getStart(), FeatureKey.FiveUTR));
                        if (feature.hasLocus()) {
                            if (isParsingSelected) {
                                parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.UTR, separator, prefixLength, fiveUtrCnt);
                            } else {
                                parsedLocus = feature.getLocus();
                            }
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                        }
                    }
                } else {
                    if (start - feature.getStop() > 0) {
                        writer.write(createSequinTableEntry(start, feature.getStop(), FeatureKey.FiveUTR));
                        if (feature.hasLocus()) {
                            if (isParsingSelected) {
                                parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.UTR, separator, prefixLength, fiveUtrCnt);
                            } else {
                                parsedLocus = feature.getLocus();
                            }
                            writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                        }
                    }
                }

                if (exportFeat.getRbsAssignments().get(start)) {
                    // RBS 
                    int analysisLength = exportFeat.getRbsSequenceLength();
                    int rbsPositionShift = exportFeat.getRbsPosistions().get(start);
                    int rbsMotifWidth = exportFeat.getRbsMotifWidth();
                    if (isFwd) {
                        int startMotif = feature.getStart() - analysisLength + rbsPositionShift;
                        int stopMotif = startMotif + rbsMotifWidth;
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.RBS));
                    } else {
                        int startMotif = exportFeat.getFeature().getStop() + analysisLength - rbsPositionShift;
                        int stopMotif = startMotif - rbsMotifWidth;
                        writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.RBS));
                    }
                    if (isParsingSelected) {
                        parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.RBS, separator, prefixLength, 0);
                    } else {
                        parsedLocus = feature.getLocus();
                    }
                    writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                }

                if (exportFeat.getPromotorAssignments().get(start)) {
                    int promotorAnalysisSequenceLength = exportFeat.getPromotorSequenceLength();
                    int minus10Width = exportFeat.getMinus10MotifWidth();
                    int minus35Width = exportFeat.getMinus35MotifWidth();
                    int minus10MotifShift;
                    int minus35MotifShift;
                    //PROMOTOR
                    if (isFwd) {
                        if (exportFeat.getMinus35Positions().containsKey(start)) {
                            minus35MotifShift = exportFeat.getMinus35Positions().get(start);
                            writer.write(createSequinTableEntry(start - promotorAnalysisSequenceLength + minus35MotifShift, start, FeatureKey.PROMOTER));
                        } else if (exportFeat.getMinus10Positions().containsKey(start)) {
                            minus10MotifShift = exportFeat.getMinus10Positions().get(start);
                            writer.write(createSequinTableEntry(start - promotorAnalysisSequenceLength + minus10MotifShift, start, FeatureKey.PROMOTER));
                        }
                        if (isParsingSelected) {
                            parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.PROMOTER, separator, prefixLength, promotorCnt);
                        } else {
                            parsedLocus = feature.getLocus();
                        }
                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                    } else {
                        if (exportFeat.getMinus35Positions().containsKey(start)) {
                            minus35MotifShift = exportFeat.getMinus35Positions().get(start);
                            writer.write(createSequinTableEntry(start + promotorAnalysisSequenceLength - minus35MotifShift, start, FeatureKey.PROMOTER));
                        } else if (exportFeat.getMinus10Positions().containsKey(start)) {
                            minus10MotifShift = exportFeat.getMinus10Positions().get(start);
                            writer.write(createSequinTableEntry(start + promotorAnalysisSequenceLength - minus10MotifShift, start, FeatureKey.PROMOTER));
                        }
                        if (isParsingSelected) {
                            parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.PROMOTER, separator, prefixLength, promotorCnt);
                        } else {
                            parsedLocus = feature.getLocus();
                        }
                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                    }

                    // -10 signal
                    if (exportFeat.getMinus10Positions().containsKey(start)) {
                        minus10MotifShift = exportFeat.getMinus10Positions().get(start);
                        if (isFwd) {
                            int startMotif = start - promotorAnalysisSequenceLength + minus10MotifShift;
                            int stopMotif = startMotif + (minus10Width - 1);
                            writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_TEN_SIGNAL));
                        } else {
                            int startMotif = start + promotorAnalysisSequenceLength - minus10MotifShift;
                            int stopMotif = startMotif - (minus10Width - 1);
                            writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_TEN_SIGNAL));
                        }
                        if (isParsingSelected) {
                            parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.PROMOTER, separator, prefixLength, minus10SignalCnt);
                        } else {
                            parsedLocus = feature.getLocus();
                        }
                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                        minus10SignalCnt++;
                    }

                    // -35 signal
                    if (exportFeat.getMinus35Positions().containsKey(start)) {
                        minus35MotifShift = exportFeat.getMinus35Positions().get(start);
                        if (isFwd) {
                            int startMotif = start - promotorAnalysisSequenceLength + minus35MotifShift;
                            int stopMotif = startMotif + (minus35Width - 1);
                            writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
                        } else {
                            int startMotif = start + promotorAnalysisSequenceLength - minus35MotifShift;
                            int stopMotif = startMotif - (minus35Width - 1);
                            writer.write(createSequinTableEntry(startMotif, stopMotif, FeatureKey.MINUS_THIRTYFIVE_SIGNAL));
                        }
                        if (isParsingSelected) {
                            parsedLocus = parseLocusTag(feature.getLocus(), ParsingPrefisSuffix.PROMOTER, separator, prefixLength, minus35SignalCnt);
                        } else {
                            parsedLocus = feature.getLocus();
                        }
                        writer.write(generateSecondLine(Qualifier.LOCUS_TAG, parsedLocus));
                        minus35SignalCnt++;
                    }
                    promotorCnt++;
                }
                mRnaCnt++;
                fiveUtrCnt++;
            }
        }
    }

    /**
     * This method clusters information of all transcription start sites, which
     * assigned to the same CDS into an ExportFeature. This featuer continains
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
            if ((ts.isLeaderless()
                    && ts.getOffsetToAssignedFeature() == 0
                    && ts.getDist2start() == 0
                    && ts.getAssignedFeature() != null)
                    || (ts.isIntragenicTSS()
                    && ts.getOffsetToNextDownstrFeature() > 0
                    && ts.getNextGene() != null)
                    || (ts.getOffset() > 0
                    && ts.getAssignedFeature() != null)) {
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
        }

        features.addAll(uniqueFeatures.values());

        return features;
    }

    /**
     * Parses the locus tag of a genome feature and adds qualifier ids to the
     * start of the suffix.
     *
     * @param locusTag locus tag of a genome feature.
     * @param type ParsingPrefisSuffix
     * @param separator separator for the locus tag
     * @param strainLength length of the strain in the locus tag
     * @param count 
     * @return a new locus tag, which now contains a type (feature key)
     */
    private String parseLocusTag(String locusTag, ParsingPrefisSuffix type, String separator, Integer strainLength, int count) {

        String newLocusTag = "";
        String[] splittedTag;
        String suffix = "";
        if (count == 0) {
            suffix = "";
        } else if (count == 1) {
            suffix = "a";
        } else if (count == 2) {
            suffix = "b";
        } else if (count == 3) {
            suffix = "c";
        } else if (count == 4) {
            suffix = "d";
        } else if (count == 5) {
            suffix = "e";
        } else if (count == 6) {
            suffix = "f";
        } else if (count == 7) {
            suffix = "g";
        } else if (count == 8) {
            suffix = "h";
        } else if (count == 9) {
            suffix = "i";
        } else if (count == 10) {
            suffix = "j";
        } else if (count == 11) {
            suffix = "k";
        } else if (count == 12) {
            suffix = "l";
        } else if (count == 13) {
            suffix = "m";
        } else if (count == 14) {
            suffix = "n";
        } else if (count == 15) {
            suffix = "o";
        } else if (count == 16) {
            suffix = "p";
        } else if (count == 17) {
            suffix = "q";
        } else if (count == 18) {
            suffix = "r";
        } else if (count == 19) {
            suffix = "s";
        } else if (count == 20) {
            suffix = "t";
        } else if (count == 21) {
            suffix = "u";
        } else if (count == 22) {
            suffix = "v";
        } else if (count == 23) {
            suffix = "w";
        } else if (count == 24) {
            suffix = "x";
        } else if (count == 25) {
            suffix = "y";
        } else if (count == 26) {
            suffix = "z";
        }
        String suffString = "";
        String prefix = "";
        boolean flag = false;
        if (separator != null && !separator.equals("")) {
            splittedTag = locusTag.split(separator);
            prefix = splittedTag[0];
            if (splittedTag.length == 2) {
                flag = true;
                suffString = splittedTag[1];
            }
        }
        if (flag) {
            flag = false;
            if (type == ParsingPrefisSuffix.GENE) {
                newLocusTag = newLocusTag.concat(prefix + separator + "g" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.ATTENUATOR) {
                newLocusTag = newLocusTag.concat(prefix + separator + "a" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.CDS) {
                newLocusTag = newLocusTag.concat(prefix + separator + "c" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.tRNA) {
                newLocusTag = newLocusTag.concat(prefix + separator + "t" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.mRNA) {
                newLocusTag = newLocusTag.concat(prefix + separator + "m" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.OPERON) {
                newLocusTag = newLocusTag.concat(prefix + separator + "o" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.PROMOTER) {
                newLocusTag = newLocusTag.concat(prefix + separator + "p" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.RBS) {
                newLocusTag = newLocusTag.concat(prefix + separator + "s" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.TERMINATOR) {
                newLocusTag = newLocusTag.concat(prefix + separator + "i" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.UTR) {
                newLocusTag = newLocusTag.concat(prefix + separator + "u" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.ncRNA) {
                newLocusTag = newLocusTag.concat(prefix + separator + "n" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.rRNA) {
                newLocusTag = newLocusTag.concat(prefix + separator + "r" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.NONE) {
                newLocusTag = newLocusTag.concat(prefix + separator + "" + suffString + suffix);
            }

        } else {

            prefix = locusTag.substring(0, strainLength);
            suffString = locusTag.substring(strainLength, locusTag.length());

            if (type == ParsingPrefisSuffix.GENE) {
                newLocusTag = newLocusTag.concat(prefix + "_g" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.ATTENUATOR) {
                newLocusTag = newLocusTag.concat(prefix + "_a" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.CDS) {
                newLocusTag = newLocusTag.concat(prefix + "_c" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.tRNA) {
                newLocusTag = newLocusTag.concat(prefix + "_t" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.mRNA) {
                newLocusTag = newLocusTag.concat(prefix + "_m" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.OPERON) {
                newLocusTag = newLocusTag.concat(prefix + "_o" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.PROMOTER) {
                newLocusTag = newLocusTag.concat(prefix + "_p" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.RBS) {
                newLocusTag = newLocusTag.concat(prefix + "_s" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.TERMINATOR) {
                newLocusTag = newLocusTag.concat(prefix + "_i" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.UTR) {
                newLocusTag = newLocusTag.concat(prefix + "_u" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.ncRNA) {
                newLocusTag = newLocusTag.concat(prefix + "_n" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.rRNA) {
                newLocusTag = newLocusTag.concat(prefix + "_r" + suffString + suffix);
            } else if (type == ParsingPrefisSuffix.NONE) {
                newLocusTag = newLocusTag.concat(prefix + "" + suffString + suffix);
            }
        }

        return newLocusTag;

    }
}
