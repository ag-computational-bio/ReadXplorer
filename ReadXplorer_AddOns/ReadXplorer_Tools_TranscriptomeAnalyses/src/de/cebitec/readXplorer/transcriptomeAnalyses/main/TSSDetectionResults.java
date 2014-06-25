package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.StartCodon;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.TableType;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.FivePrimeEnrichedTracksVisualPanel;
import de.cebitec.readXplorer.transcriptomeAnalyses.mainWizard.WizardPropertyStrings;
import de.cebitec.readXplorer.util.FeatureType;
import de.cebitec.readXplorer.util.GeneralUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class TSSDetectionResults extends ResultTrackAnalysis<ParameterSetFiveEnrichedAnalyses> {

    private List<TranscriptionStart> results;
    private final StatisticsOnMappingData stats;
    private Map<String, Object> statsMap;
    private List<String> promotorRegions;
    private static final TableType TABLE_TYPE = TableType.TSS_TABLE;

    /**
     *
     * @param stats
     * @param results
     * @param trackMap
     * @param refId
     */
    public TSSDetectionResults(StatisticsOnMappingData stats, List<TranscriptionStart> results, Map<Integer, PersistantTrack> trackMap, int refId) {
        super(trackMap, refId, false, 22, 0);
        this.results = results;
        this.stats = stats;
        this.statsMap = new HashMap<>();
    }

    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Transcription Analysis Table");
        sheetNames.add("Parameters and Statistics");
        return sheetNames;
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> allSheetDescriptions = new ArrayList<>();
        List<String> dataColumnDescriptions = new ArrayList<>();

        dataColumnDescriptions.add("Position");
        dataColumnDescriptions.add("Strand");
        dataColumnDescriptions.add("Comment");
        dataColumnDescriptions.add("Read Starts");
        dataColumnDescriptions.add("Rel. Count");
        dataColumnDescriptions.add("Feature Name");
        dataColumnDescriptions.add("Feature Locus");
        dataColumnDescriptions.add("Offset");
        dataColumnDescriptions.add("Dist. To Start");
        dataColumnDescriptions.add("Dist. To Stop");
        dataColumnDescriptions.add("Sequence");
        dataColumnDescriptions.add("Leaderless");
        dataColumnDescriptions.add("Putative TLS-Shift");
        dataColumnDescriptions.add("Intragenic TSS");
        dataColumnDescriptions.add("Intergenic TSS");
        dataColumnDescriptions.add("Putative Antisense");
        dataColumnDescriptions.add("Putative 5'-UTR Antisense");
        dataColumnDescriptions.add("Putative 3'-UTR Antisense");
        dataColumnDescriptions.add("Putative Intragenic Antisense");
        dataColumnDescriptions.add("Assigned To Stable RNA");
        dataColumnDescriptions.add("False Positive");
        dataColumnDescriptions.add("Selected For Upstream Region Analysis");
        dataColumnDescriptions.add("Finished");
        dataColumnDescriptions.add("Gene Start");
        dataColumnDescriptions.add("Gene Stop");
        dataColumnDescriptions.add("Gene Length In Bp");
        dataColumnDescriptions.add("Frame");
        dataColumnDescriptions.add("Gene Product");
        dataColumnDescriptions.add("Start Codon");
        dataColumnDescriptions.add("Stop Codon");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Chrom ID");
        dataColumnDescriptions.add("Track ID");

        allSheetDescriptions.add(dataColumnDescriptions);

        //add tss detection statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Transcription Start Site Detection Parameter and Statistics Table");

        allSheetDescriptions.add(statisticColumnDescriptions);

        return allSheetDescriptions;
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> tSSExport = new ArrayList<>();
        List<List<Object>> tSSResults = new ArrayList<>();

        for (int i = 0; i < results.size(); ++i) {
            TranscriptionStart tss = results.get(i);
            List<Object> tssRow = new ArrayList<>();

            tssRow.add(tss.getStartPosition());
            boolean isFwd = tss.isFwdStrand();
            if (isFwd) {
                tssRow.add("Fwd");
            } else {
                tssRow.add("Rev");
            }

            if (tss.getComment() != null) {
                tssRow.add(tss.getComment());
            } else {
                tssRow.add("-");
            }
            tssRow.add(tss.getReadStarts());
            tssRow.add(tss.getRelCount());

            PersistantFeature detectedGene = tss.getDetectedGene();
            PersistantFeature nextDownstreamGene = tss.getNextGene();

            if (detectedGene != null) {
                tssRow.add(tss.getDetectedGene().toString());
                tssRow.add(tss.getDetectedGene().getLocus());
                tssRow.add(tss.getOffset());
                tssRow.add(tss.getDist2start());
                tssRow.add(tss.getDist2stop());

            } else if (nextDownstreamGene != null) {
                tssRow.add(tss.getNextGene().toString());
                tssRow.add(tss.getNextGene().getLocus());
                tssRow.add(tss.getOffsetToNextDownstrFeature());
                tssRow.add(tss.getDist2start());
                tssRow.add(tss.getDist2stop());
            } else {
                tssRow.add("-");
                tssRow.add("-");
                tssRow.add("-");
                tssRow.add("-");
                tssRow.add("-");
            }

            String promotorSequence = promotorRegions.get(i);
            tssRow.add(promotorSequence);
            tssRow.add(tss.isLeaderless());
            tssRow.add(tss.isCdsShift());

            tssRow.add(tss.isIntragenicTSS());
            tssRow.add(tss.isIntergenicTSS());
            tssRow.add(tss.isPutativeAntisense());
            tssRow.add(tss.isIs5PrimeUtrAntisense());
            tssRow.add(tss.isIs3PrimeUtrAntisense());
            tssRow.add(tss.isIntragenicAntisense());
            tssRow.add(tss.isAssignedToStableRNA());
            tssRow.add(tss.isFalsePositive());
            tssRow.add(tss.isSelected());
            tssRow.add(tss.isConsideredTSS());

            // additionally informations about detected gene
            if (detectedGene != null) {
                tssRow.add(detectedGene.isFwdStrand() ? detectedGene.getStart() : detectedGene.getStop());
                tssRow.add(detectedGene.isFwdStrand() ? detectedGene.getStop() : detectedGene.getStart());
                tssRow.add(detectedGene.getStop() - detectedGene.getStart());

                int start = detectedGene.getStart();
                if ((start % 3) == 0) {
                    tssRow.add(3);
                } else if (start % 3 == 1) {
                    tssRow.add(1);
                } else if (start % 3 == 2) {
                    tssRow.add(2);
                }
                tssRow.add(detectedGene.getProduct());
            } else if (nextDownstreamGene != null) {
                tssRow.add(nextDownstreamGene.isFwdStrand() ? nextDownstreamGene.getStart() : nextDownstreamGene.getStop());
                tssRow.add(nextDownstreamGene.isFwdStrand() ? nextDownstreamGene.getStop() : nextDownstreamGene.getStart());
                tssRow.add(nextDownstreamGene.getStop() - nextDownstreamGene.getStart());
                int start = nextDownstreamGene.getStart();
                if ((start % 3) == 0) {
                    tssRow.add(3);
                } else if (start % 3 == 1) {
                    tssRow.add(1);
                } else if (start % 3 == 2) {
                    tssRow.add(2);
                }
                tssRow.add(nextDownstreamGene.getProduct());
            } else {
                tssRow.add("-");
                tssRow.add("-");
                tssRow.add("-");
                tssRow.add("-");
                tssRow.add("-");
            }

            tssRow.add(tss.getDetectedFeatStart());
            tssRow.add(tss.getDetectedFeatStop());
            tssRow.add(this.getChromosomeMap().get(tss.getChromId()));
            tssRow.add(tss.getChromId());
            tssRow.add(tss.getTrackId());
            tSSResults.add(tssRow);
        }

        tSSExport.add(tSSResults);

        double mappingCount = (double) this.statsMap.get(ResultPanelTranscriptionStart.MAPPINGS_COUNT);
        double meanMappingLength = (double) this.statsMap.get(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH);
        double mappingsPerMio = (double) this.statsMap.get(ResultPanelTranscriptionStart.MAPPINGS_MILLION);
        double backgroundThreshold = (double) this.statsMap.get(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE);
        //create statistics sheet
        ParameterSetFiveEnrichedAnalyses tssParameters = (ParameterSetFiveEnrichedAnalyses) this.getParameters();
        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(
                "Transcription start site detection statistics for tracks:",
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Parameters"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_RATIO,
                tssParameters.getRatio()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_FRACTION,
                tssParameters.getFraction()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_MANUALLY_SET_THRESHOLD,
                tssParameters.isThresholdManuallySet() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.BACKGROUND_THRESHOLD_MIN_STACKSIZE,
                String.valueOf(String.format("%2.2f", backgroundThreshold))));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_EXCLUSION_OF_INTERNAL_TSS,
                tssParameters.isExclusionOfAllIntragenicTSS() ? "yes" : "no"));

        statisticsExportData.add(ResultTrackAnalysis.createThreeElementTableRow(ResultPanelTranscriptionStart.TSS_KEEP_ALL_INTRAGENIC_TSS,
                tssParameters.isKeepAllIntragenicTss() ? "yes" : "no", String.valueOf(tssParameters.getKeepIntragenicTssDistanceLimit().toString())));

        statisticsExportData.add(ResultTrackAnalysis.createThreeElementTableRow(ResultPanelTranscriptionStart.TSS_KEEP_ONLY_ASSIGNED_INTRAGENIC_TSS,
                tssParameters.isKeepOnlyAssignedIntragenicTss() ? "yes" : "no", String.valueOf(tssParameters.getKeepIntragenicTssDistanceLimit())));

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(WizardPropertyStrings.PROP_INCLUDE_BEST_MATCHED_READS_TSS,
                tssParameters.isIncludeBestMatchedReads() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_RANGE_FOR_LEADERLESS_DETECTION,
                tssParameters.getLeaderlessLimit()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_OFUPSTREM_REGION,
                tssParameters.getExclusionOfTSSDistance()));
//        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_LIMITATION_FOR_DISTANCE_KEEPING_INTERNAL_TSS,
//                tssParameters.getKeepIntragenicTssDistanceLimit()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_PERCENTAGE_FOR_CDSSHIFT_ANALYSIS,
                tssParameters.getCdsShiftPercentage()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(WizardPropertyStrings.PROP_MAX_DIST_FOR_3_UTR_ANTISENSE_DETECTION,
                tssParameters.getThreeUtrLimitAntisenseDetection()));
        String validCodonsString = getValidStartCodonsAsString(tssParameters.getValidStartCodons());
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(WizardPropertyStrings.PROP_VALID_START_CODONS,
                validCodonsString));
        String fadeOutTypes = getExcludedFeatureTypesAsString(tssParameters.getExcludeFeatureTypes());
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(FivePrimeEnrichedTracksVisualPanel.PROP_SELECTED_FEAT_TYPES_FADE_OUT,
                fadeOutTypes));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("TSS statistics:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_TOTAL,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_FWD,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_FWD)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_REV,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_REV)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_LEADERLESS,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_LEADERLESS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_INTRAGENIC_TSS,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_INTRAGENIC_TSS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_INTERGENIC_TSS,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_INTERGENIC_TSS)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_IN_TOTAL,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_IN_TOTAL)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_OF_3_PRIME_UTR,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_OF_3_PRIME_UTR)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_OF_5_PRIME_UTR,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_OF_5_PRIME_UTR)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_INTRAGENIC,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_PUTATIVE_ANTISENSE_INTRAGENIC)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_ASSIGNED_TO_STABLE_RNA,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_ASSIGNED_TO_STABLE_RNA)));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.TSS_NO_PUTATIVE_CDS_SHIFTS,
                getStatsAndParametersMap().get(ResultPanelTranscriptionStart.TSS_NO_PUTATIVE_CDS_SHIFTS)));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("Mapping statistics:"));

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_COUNT,
                String.valueOf(String.format("%2.2f", mappingCount))));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.AVERAGE_MAPPINGS_LENGTH,
                String.valueOf(String.format("%2.2f", meanMappingLength))));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow(ResultPanelTranscriptionStart.MAPPINGS_MILLION,
                String.valueOf(String.format("%2.2f", mappingsPerMio))));

        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow(""));

        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Table Type", TABLE_TYPE.toString()));
        tSSExport.add(statisticsExportData);

        return tSSExport;
    }

    public List<TranscriptionStart> getResults() {
        return results;
    }

    public void setResults(List<TranscriptionStart> results) {
        this.results = results;
    }

    public StatisticsOnMappingData getStats() {
        return stats;
    }

    /**
     * @return The statistics map associated with this analysis
     */
    public Map<String, Object> getStatsAndParametersMap() {
        return this.statsMap;
    }

    /**
     * Sets the statistics map associated with this analysis.
     *
     * @param statsMap the statistics map associated with this analysis
     */
    public void setStatsAndParametersMap(Map<String, Object> statsMap) {
        this.statsMap = statsMap;
    }

    /**
     * @return Promotor regions of the detected TSS
     */
    public List<String> getPromotorRegions() {
        return promotorRegions;
    }

    /**
     * Sets the promotor regions of the detected TSS
     *
     * @param promotorRegions Promotor regions of the detected TSS
     */
    public void setPromotorRegions(List<String> promotorRegions) {
        this.promotorRegions = promotorRegions;
    }

    private String getValidStartCodonsAsString(HashMap<String, StartCodon> validStartCodons) {
        String result = "";

        for (Iterator<StartCodon> it = validStartCodons.values().iterator(); it.hasNext();) {
            StartCodon codon = it.next();

            if (it.hasNext()) {
                result = result.concat(codon.toString() + ";");
            } else {
                result = result.concat(codon.toString());
            }
        }

        return result;
    }

    private String getExcludedFeatureTypesAsString(HashSet<FeatureType> fadeOutTypes) {
        String result = "";

        if (fadeOutTypes == null) {
            return result;
        } else {
            for (Iterator<FeatureType> it = fadeOutTypes.iterator(); it.hasNext();) {
                FeatureType type = it.next();

                if (it.hasNext()) {
                    result = result.concat(type.toString() + ";");
                } else {
                    result = result.concat(type.toString());
                }
            }
        }

        return result;
    }
}
