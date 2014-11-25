/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.filterWizard;

import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.TranscriptionStart;
import de.cebitec.readXplorer.transcriptomeAnalyses.enums.FilterType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jritter
 */
public class FilterTSS {

    public FilterTSS() {
    }

    public List<TranscriptionStart> filter(FilterType type, List<TranscriptionStart> tss, int param) {
        List<TranscriptionStart> resultList = new ArrayList<>();
        String locus = "";

        if (type == FilterType.MULTIPLE) {
            Map<String, List<TranscriptionStart>> starts = new TreeMap<>();

            for (TranscriptionStart ts : tss) {
                if (ts.isIntergenicAntisense() == false) {
                    locus = ts.getAssignedFeature().getLocus();
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
                if (ts.isIntergenicAntisense() == false) {
                    locus = ts.getAssignedFeature().getLocus();
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
        } else if (type == FilterType.SHIFTS_IN_TSS_POS && param != 0) {
            for (TranscriptionStart ts : tss) {

            }
        } else if (type == FilterType.READSTARTS && param != 0) {
            for (TranscriptionStart ts : tss) {
                if (ts.getReadStarts() > param) {
                    resultList.add(ts);
                }
            }
        }

        return resultList;
    }
}
