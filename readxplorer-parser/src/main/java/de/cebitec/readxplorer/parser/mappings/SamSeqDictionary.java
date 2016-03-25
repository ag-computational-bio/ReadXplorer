/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.parser.mappings;

import de.cebitec.readxplorer.utils.sequence.RefDictionary;
import htsjdk.samtools.SAMSequenceDictionary;


/**
 * A wrapper for the SAMSequenceDictionary = the SAM reference sequence
 * dictionary. Makes it exchangeable with other RefDictionary implementations.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class SamSeqDictionary implements RefDictionary {

    private SAMSequenceDictionary samDictionary;


    /**
     * A wrapper for the SAMSequenceDictionary = the SAM reference sequence
     * dictionary. Makes it exchangeable with other RefDictionary
     * implementations.
     *
     * @param samDictionary The sam sequence dictionary for the reference ids.
     */
    public SamSeqDictionary( SAMSequenceDictionary samDictionary ) {
        this.samDictionary = samDictionary;
    }


    /**
     * An empty SAM reference sequence dictionary.
     */
    public SamSeqDictionary() {
        samDictionary = new SAMSequenceDictionary();
    }


    /**
     * @return The sam sequence dictionary for the reference ids.
     */
    public SAMSequenceDictionary getSamDictionary() {
        return samDictionary;
    }


    /**
     * @param samDictionary The sam sequence dictionary for the reference ids.
     */
    public void setSamDictionary( SAMSequenceDictionary samDictionary ) {
        this.samDictionary = samDictionary;
    }


}
