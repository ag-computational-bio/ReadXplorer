/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.util;

import de.cebitec.common.sequencetools.geneticcode.GeneticCode;
import de.cebitec.common.sequencetools.geneticcode.GeneticCodeFactory;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;


/**
 * Class for utility methods working on DNA codons.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CodonUtilities {
    
    private CodonUtilities() {
    }
    
    /**
     * Parses custom codons of a line defined by the "wantedIndex" into an array
     * of Strings. Each codon is one entry in the resulting array.
     * @param wantedIndex index of the line containing the wanted codons
     * @param customCodonString string containing the codons inbetween '(' & ')'
     * and seperated by a ',', e.g. (AGT, AGG)
     * @return A pair containing two arrays with the wanted codons. The first
     * array contains the start codons, the second the stop codons.
     */
    public static Pair<String[], String[]> parseCustomCodons(int wantedIndex, String customCodonString) {
        GeneticCodeFactory genCodeFactory = GeneticCodeFactory.getDefault();
        int index = genCodeFactory.getGeneticCodes().size() + 1;
        while (index++ <= wantedIndex) {
            customCodonString = customCodonString.substring(customCodonString.indexOf('\n') + 1, customCodonString.length());
        }   
        int startIndex = customCodonString.startsWith("\n") ? 2 : 1;
        String codons = customCodonString.substring(startIndex, customCodonString.indexOf(')'));
        String[] startsAndStops = codons.split(";");
        String[] starts = splitCodons(startsAndStops[0]);
        String[] stops = {};
        if (startsAndStops.length > 1) {
            stops = splitCodons(startsAndStops[1]);
        }
        return new Pair<>(starts, stops);
    }
    
    /**
     * Retrieves the selected genetic code and parses it into a pair of start
     * and stop codons. 
     * @return A pair containing two arrays with the codons of the selected
     * genetic code. The first array contains the start codons, the second the 
     * stop codons.
     */
    public static Pair<String[], String[]> getGeneticCodeArrays() {
        String[] startCodons = new String[0];
        String[] stopCodons = new String[0];
        
        GeneticCodeFactory genCodeFactory = GeneticCodeFactory.getDefault();
        int nbGeneticCodes = genCodeFactory.getGeneticCodes().size();
        Preferences pref = NbPreferences.forModule(Object.class);
        int codeIndex = Integer.valueOf(pref.get(Properties.GENETIC_CODE_INDEX, "0"));
        
        if (codeIndex < nbGeneticCodes) {
            GeneticCode code = genCodeFactory.getGeneticCodeById(Integer.valueOf(
                    pref.get(Properties.SEL_GENETIC_CODE, Properties.STANDARD_CODE_INDEX)));
            startCodons = code.getStartCodons().toArray(startCodons);
            stopCodons = code.getStopCodons().toArray(stopCodons);
        } else {
            Pair<String[], String[]> codonPair = CodonUtilities.parseCustomCodons(codeIndex, 
                    pref.get(Properties.CUSTOM_GENETIC_CODES, Properties.STANDARD_CODE_INDEX));
            startCodons = codonPair.getFirst();
            stopCodons = codonPair.getSecond();
        }
        return new Pair<>(startCodons, stopCodons);
    }
    
    /**
     * @return Ensures that always a valid genetic code is returned. If a custom
     * genetic code is selected (which does not include a translation table), 
     * the standard genetic code is used instead. Use this method, if not only
     * the start and stop codons of a genetic code are needed, but the other
     * codons and translations as well.
     */
    public static GeneticCode getGeneticCode() {
        GeneticCodeFactory genCodeFactory = GeneticCodeFactory.getDefault();
        Preferences pref = NbPreferences.forModule(Object.class);
        Integer codeIdx;
        try {
            codeIdx = Integer.valueOf(pref.get(Properties.SEL_GENETIC_CODE, Properties.STANDARD_CODE_INDEX));
        } catch (NumberFormatException e) {
            codeIdx = Integer.valueOf(Properties.STANDARD_CODE_INDEX);
        }
        
        return genCodeFactory.getGeneticCodeById(codeIdx);
    }

    /**
     * Splits the given codon string into separate codons and adds them to an
     * array.
     * @param codonString The string of codons to split
     * @return An array of genetic codons.
     */
    private static String[] splitCodons(String codonString) {
        String[] codonArray = codonString.split(",");
        if (codonArray.length == 1 && codonArray[0].isEmpty()) {
            codonArray = new String[0];
        }
        for (int i = 0; i < codonArray.length; ++i) {
            codonArray[i] = codonArray[i].toUpperCase().trim(); //to assure correct format
        }
        return codonArray;
    }
    
}
