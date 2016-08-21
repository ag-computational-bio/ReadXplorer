/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readxplorer.transcriptionanalyses.logic;

import de.cebitec.readxplorer.transcriptionanalyses.ParameterSetTSS;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TranscriptionStart;
import java.util.List;

/**
 * Checks all previous TSS in the current merge TSS window against a current
 * TSS. Updates the <code>isPrimaryTss()</code> flag of each TSS in a current
 * merge TSS window. The most prominent TSS receives the primary TSS flag, while
 * all others receive the secondary TSS flag. Recursively checks all TSS within
 * the current merge TSS window.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class PrimaryTssFlagger implements TssLinker {

    private ParameterSetTSS parametersTSS;
    private List<TranscriptionStart> detectedStarts;
    
    /**
     * Check all previous TSS in the current merge TSS window against the
     * current TSS. Update the <code>isPrimaryTss()</code> flag of each TSS in
     * the current merge TSS window. The most prominent TSS receives the primary
     * TSS flag, while all others receive the secondary TSS flag. Recursively
     * checks all TSS within the current merge TSS window.
     *
     * @param previousTss The previous TSS from the result to check against the
     * current
     * @param prevTssIdx The index of the previous TSS in the result list
     * @param tss The current TSS to compare to all previous ones
     */
    @Override
    public final void linkTss(TranscriptionStart previousTss, int prevTssIdx, TranscriptionStart tss) {
        if (previousTss.getChromId() == tss.getChromId() 
                && previousTss.getPos() + parametersTSS.getMaxFeatureDistance() >= tss.getPos()
                && previousTss.isFwdStrand() == tss.isFwdStrand()) {

//            int coverageIncreaseLast = lastDetectedStart.getPercentIncrease();
//            int coverageIncrease = tss.getPercentIncrease();
            int noReadStartsLastStart = previousTss.getReadStartsAtPos();
            int noReadStartsTSS = tss.getReadStartsAtPos();
            if (previousTss.isPrimaryTss()) {
                if (noReadStartsLastStart < noReadStartsTSS) {
                    previousTss.setIsPrimary(false);
                    previousTss.setPrimaryTss(tss);
                    tss.setIsPrimary(true);
                    if (prevTssIdx > 0) {
                        furtherCheckForSecondaryTss(prevTssIdx, tss);
                    }
                } else {
                    tss.setIsPrimary(false);
                    tss.setPrimaryTss(previousTss);
                }
            } else { //previous TSS is secondary -> there is another primary TSS
                linkTss(previousTss.getPrimaryTss(), prevTssIdx, tss);
            }
        } else {
            tss.setIsPrimary(true);
        }
    }
    

    /**
     * Check if there are more secondary TSS in the merge window reach of the
     * current TSS. For all those, the primary TSS flag has to be updated to the
     * current one.
     *
     * @param prevTssIdx The index of the previous primary TSS in the current
     * merge window
     * @param tss The current TSS for which the data is updated
     */
    private void furtherCheckForSecondaryTss(int prevTssIdx, TranscriptionStart tss) {
        TranscriptionStart previousTss = detectedStarts.get(--prevTssIdx);
        while (previousTss.getPos() + parametersTSS.getMaxFeatureDistance() >= tss.getPos() && prevTssIdx > 0) {
            if (previousTss.isFwdStrand() == tss.isFwdStrand()) {
                previousTss.setPrimaryTss(tss);
                /*
                 * Keep in mind: by doing this, all secondary TSS belonging to 
                 * another real primaryTSS directly before the current merge TSS 
                 * window will be reassigned to the current TSS, even if the other 
                 * one is stronger. But such cases should only occur when futile 
                 * parameters are chosen (large merge TSS window). Further, its
                 * highly unlikely to observe 2 real primary TSS within a window of 
                 * e.g. 5bp.
                 */
            }
            previousTss = detectedStarts.get(--prevTssIdx);
        }
    }

    
    public final void setParametersTSS(ParameterSetTSS parametersTSS) {
        this.parametersTSS = parametersTSS;
    }

    
    public final void setDetectedStarts(List<TranscriptionStart> detectedStarts) {
        this.detectedStarts = detectedStarts;
    }

}
