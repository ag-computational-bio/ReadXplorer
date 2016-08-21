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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Associates all TSS within a bp window defined by the TSS analysis parameters.
 * Only the TSS with the highest number of read starts is kept. All other TSS
 * are discarded and added to the associated TSS list of the remaining TSS. This
 * behavior prevents detecting two transcription start sites for the same gene, in
 * case the transcription already starts at a low rate a few bases before the
 * actual transcription start site.
 * <p>
 * This seems to happen, when the end of the -10 region is further away from the
 * actual transcription start site than 7 bases in prokaryotes. There might
 * exist more reasons, of course.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class TssAssociater implements TssLinker {
    
    private ParameterSetTSS parametersTSS;
    private List<TranscriptionStart> detectedStarts;
    private List<TranscriptionStart> associatedTss;

    /**
     * Checks for the given TSS, if it is located within the associate Tss bp window
     * defined by the TSS analysis parameters of the current TSS on the same
     * strand. If that's the case, only the transcription start site with the
     * higher number of TOTAL read starts is kept. This method prevents
     * detecting two transcription start sites for the same gene, in case the
     * transcription already starts at a low rate a few bases before the actual
     * transcription start site.
     * <p>
     * This seems to happen, when the end of the -10 region is further away from
     * the actual transcription start site than 7 bases in prokaryotes. There
     * might exist more reasons, of course.
     * <p>
     * @param previousTss The previous TSS from the result to check against the
     * current
     * @param prevTssIdx The index of the previous TSS in the result list
     * @param tss The current TSS to compare to all previous ones
     */
    @Override
    public final void linkTss(TranscriptionStart previousTss, int prevTssIdx, TranscriptionStart tss) {
        
        while (previousTss.getPos() + parametersTSS.getAssociateTssWindow() >= tss.getPos() && prevTssIdx > 0 ) {
            if ( previousTss.isFwdStrand() == tss.isFwdStrand() && previousTss.getChromId() == tss.getChromId() ) {

                int noReadStartsLastStart = previousTss.getReadStartsAtPos();
                int noReadStartsTSS = tss.getReadStartsAtPos();
                //The alternative would be to use the coverage increase:
//                int coverageIncreaseLast = previousTss.getPercentIncrease();
//                int coverageIncrease = tss.getPercentIncrease();

                if (noReadStartsLastStart < noReadStartsTSS) {
                    tss.addAssociatedTss(previousTss, parametersTSS.getAssociateTssWindow());
                    this.associatedTss.remove(previousTss);
                } else {
                    previousTss.addAssociatedTss(tss, parametersTSS.getAssociateTssWindow());
                    this.associatedTss.remove(tss);
                }
            }
            previousTss = detectedStarts.get(--prevTssIdx);
        }
    }

    public final void setParametersTSS(ParameterSetTSS parametersTSS) {
        this.parametersTSS = parametersTSS;
    }

    public final void setDetectedStarts(List<TranscriptionStart> detectedStarts) {
        this.detectedStarts = detectedStarts;
        this.associatedTss = new ArrayList<>(this.detectedStarts);
    }

    /**
     * @return The updated copy of the original result list. All TSS associated
     * with a more significant TSS have been deleted from this list.
     */
    public List<TranscriptionStart> getAssociatedTss() {
        return Collections.unmodifiableList( associatedTss );
    }
    
}
