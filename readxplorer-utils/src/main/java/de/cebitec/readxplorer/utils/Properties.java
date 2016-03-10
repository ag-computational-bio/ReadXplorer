/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.utils;


/**
 * Contains non language specific global constants.
 * <p>
 * @author Rolf Hilker
 */
public final class Properties {


    private Properties() {
    }


    // properties mainly for genetic codes
    public static final String SEL_GENETIC_CODE = "selectedGeneticCode";
    public static final String STANDARD = "Standard";
    /**
     * 1 = Index of the standard genetic code.
     */
    public static final String STANDARD_CODE_INDEX = "1";
    public static final String GENETIC_CODE_INDEX = "geneticCodeIndex";
    public static final String CUSTOM_GENETIC_CODES = "customGeneticCode";


    /**
     * Protein database prefix used for creating EC-number links.
     */
    public static final String ENZYME_DB_LINK = "PROTEIN_DB_LINK";


    /**
     * '-1' For reference features, which do not have a parent.
     */
    public static final String NO_PARENT_STRING = "-1";
    public static final String MAPPER_PARAMS = "MAPPER_PARAMS";



    /**
     * Option for showing base qualities.
     */
    public static final String BASE_QUALITY_OPTION = "BASE_QUALITY_OPTION";

    /**
     * Option for adjusting the alignment block height.
     */
    public static final String BLOCK_HEIGHT_OPTION = "BLOCK_HEIGHT_OPTION";


}
