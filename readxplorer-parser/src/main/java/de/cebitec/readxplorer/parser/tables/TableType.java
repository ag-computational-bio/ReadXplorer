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

package de.cebitec.readxplorer.parser.tables;


/**
 * Enumeration of all available table models in ReadXplorer. All tables, which
 * can be generated with ReadXplorer are listed here.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public enum TableType {

    /**
     * Coverage analysis table.
     */
    COVERAGE_ANALYSIS( "Coverage Analysis Table" ),
    /**
     * Feature coverage analysis table.
     */
    FEATURE_COVERAGE_ANALYSIS( "Feature Coverage Analysis Table" ),
    /**
     * Operon detection table.
     */
    OPERON_DETECTION( "Operon Detection Table" ),
    /**
     * TPM and RPKM analysis table.
     */
    TPM_RPKM_ANALYSIS( "TPM and RPKM Analysis Table" ),
    /**
     * SNP detection table.
     */
    SNP_DETECTION( "SNP Detection Table" ),
    /**
     * TSS detection table.
     */
    TSS_DETECTION( "TSS Detection Table" ),
    /**
     * Operon detection table JR.
     */
    OPERON_DETECTION_JR( "Operon Detection Table JR" ),
    /**
     * RPKM analysis table JR.
     */
    RPKM_ANALYSIS_JR( "RPKM Analysis Table JR" ),
    /**
     * Novel transcript detection table JR.
     */
    NOVEL_TRANSCRIPT_DETECTION_JR( "Novel Transcript Detection Table JR" ),
    /**
     * TSS detection table JR.
     */
    TSS_DETECTION_JR( "TSS Detection Table JR" ),
    /**
     * Differential gene expression table starting with a position column.
     */
    DIFF_GENE_EXPRESSION( "Differential Gene Expression Table" ),
    /**
     * GASV genome rearrangement detection result table.
     */
    GASV_TABLE( "GASV Result Table" ),
    /**
     * GASV genome rearrangement detection result table.
     */
    CORRELATION_TABLE( "Correlation Analysis Table" ),
    /**
     * Arbitrary table starting with a position column.
     */
    POS_TABLE( "Any table starting with position column" ),
    /**
     * A statistics table.
     */
    STATS_TABLE( "A statistics table" ),
    /**
     * Any arbitrary table with no synchronization in the viewers.
     */
    ANY_TABLE( "Any other table" );

    private final String name;


    private TableType( String name ) {
        this.name = name;
    }


    /**
     * @return The user-readable String representation of the CsvPreference.
     */
    public String getName() {
        return name;
    }


    /**
     * @return The user-readable String representation of the CsvPreference.
     */
    @Override
    public String toString() {
        return this.getName();
    }


}
