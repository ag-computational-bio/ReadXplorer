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

import de.cebitec.readxplorer.transcriptionanalyses.datastructures.TranscriptionStart;

/**
 * Performs some linking action for Transcription start sites (TSS).
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public interface TssLinker {
    
    /**
     * Performs some linking action for the given TSS.
     * @param previousTss Some previous TSS to the current TSS
     * @param prevTssIdx The index of the previous TSS
     * @param tss The current TSS
     */
    public void linkTss(TranscriptionStart previousTss, int prevTssIdx, TranscriptionStart tss);
}
