package de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.FilterType;
import de.cebitec.readXplorer.transcriptomeAnalyses.main.ParameterSetFiveEnrichedAnalyses;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This Class covers all filter functions on TSS Lists.
 *
 * @author jritter
 */
public class FilterTSS {

    /**
     * This Method filter the passed List of TranscriptionStarts for the
     * selected Type and constructs a new List of TranscriptionStarts which are
     * corresponding to that type. It returns a List containing the
     * TranscriptionStarts with the selected property.
     *
     * @param type property for selection.
     * @param tss List of TranscriptionStarts.
     * @param param
     * @return a List of two other Lists.
     */
    public List<TranscriptionStart> filter(FilterType type, List<TranscriptionStart> tss, ParameterSetFiveEnrichedAnalyses params, int param) {
        List<TranscriptionStart> resultList = new ArrayList<>();
        String locus = "";
        PersistantFeature f;

        if (type == FilterType.MULTIPLE) {
            Map<String, List<TranscriptionStart>> starts = new TreeMap<>();

            for (TranscriptionStart ts : tss) {
                if (ts.isIs3PrimeUtrAntisense() == false && ts.isIntragenicAntisense() == false && ts.isIntergenicTSS() == false && (ts.getOffset() > 0 || ts.getDist2start() <= params.getLeaderlessLimit())) {
                    f = ts.getAssignedFeature();
                    if (f == null) {
                        continue;
                    }
                    locus = f.getLocus();
                    if (starts.containsKey(locus)) {
                        starts.get(locus).add(ts);
                    } else {
                        ArrayList<TranscriptionStart> list = new ArrayList<>();
                        list.add(ts);
                        starts.put(locus, list);
                    }
                }
            }

            for (String loc : starts.keySet()) {
                if (starts.get(loc).size() > 1) {
                    resultList.addAll(starts.get(loc));
                }
            }
        } else if (type == FilterType.SINGLE) {
            Map<String, List<TranscriptionStart>> starts = new TreeMap<>();

            for (TranscriptionStart ts : tss) {
                if (ts.isIs3PrimeUtrAntisense() == false && ts.isIntragenicAntisense() == false && ts.isIntergenicTSS() == false && (ts.getOffset() > 0 || ts.getDist2start() <= params.getLeaderlessLimit())) {
                    f = ts.getAssignedFeature();
                    if (f == null) {
                        continue;
                    }
                    locus = f.getLocus();
                    if (starts.containsKey(locus)) {
                        starts.get(locus).add(ts);
                    } else {
                        ArrayList<TranscriptionStart> list = new ArrayList<>();
                        list.add(ts);
                        starts.put(locus, list);
                    }
                }
            }

            for (String loc : starts.keySet()) {
                if (starts.get(loc).size() == 1) {
                    resultList.addAll(starts.get(loc));
                }
            }
        } else if (type == FilterType.READSTARTS && param != 0) {
            for (TranscriptionStart ts : tss) {
                if (ts.getReadStarts() > param) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.ONLY_ANTISENSE) {
            for (TranscriptionStart ts : tss) {
                if (ts.isPutativeAntisense()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.ONLY_INTERGENIC) {
            for (TranscriptionStart ts : tss) {
                if (ts.isIntergenicTSS()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.ONLY_INTRAGENIC) {
            for (TranscriptionStart ts : tss) {
                if (ts.isIntragenicTSS()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.ONLY_LEADERLESS) {
            for (TranscriptionStart ts : tss) {
                if (ts.isLeaderless()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.FINISHED_TAGGED) {
            for (TranscriptionStart ts : tss) {
                if (ts.isConsideredTSS()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.UPSTREMA_ANALYSIS_TAGGED) {
            for (TranscriptionStart ts : tss) {
                if (ts.isSelected()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.ONLY_FALSE_POSITIVES) {
            for (TranscriptionStart ts : tss) {
                if (ts.isFalsePositive()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.THREE_PRIME_ANTISENSE) {
            for (TranscriptionStart ts : tss) {
                if (ts.isIs3PrimeUtrAntisense()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.FIVE_PRIME_ANTISENSE) {
            for (TranscriptionStart ts : tss) {
                if (ts.isIs5PrimeUtrAntisense()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.INTRAGENIC_ANTISENSE) {
            for (TranscriptionStart ts : tss) {
                if (ts.isIntragenicAntisense()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.STABLE_RNA) {
            for (TranscriptionStart ts : tss) {
                if (ts.isAssignedToStableRNA()) {
                    resultList.add(ts);
                }
            }
        } else if (type == FilterType.ONLY_NON_STABLE_RNA) {
            for (TranscriptionStart ts : tss) {
                if (!ts.isAssignedToStableRNA()) {
                    resultList.add(ts);
                }
            }
        }

        return resultList;
    }
}
