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
package de.cebitec.readXplorer.parser.tables;

/**
 * Enumeration of all available table models in ReadXplorer. All tables, which
 * can be generated with ReadXplorer are listed here.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public enum TableType {
    
    /** Coverage analysis table. */
    COVERAGE_ANALYSIS("Coverage Analysis Table"),
    /** Feature coverage analysis table. */
    FEATURE_COVERAGE_ANALYSIS("Feature Coverage Analysis Table"),
    /** Operon detection table. */
    OPERON_DETECTION("Operon Detection Table"),
    /** RPKM analysis table. */
    RPKM_ANALYSIS("RPKM Analysis Table"),
    /** SNP detection table. */
    SNP_DETECTION("SNP Detection Table"),
    /** TSS detection table. */
    TSS_DETECTION("TSS Detection Table"),
    /** TSS detection table JR. */
    TSS_DETECTION_JR("TSS Detection Table JR"),
    /** Arbitrary table starting with a position column. */
    DIFF_GENE_EXPRESSION("Differential Gene Expression Table"),
    /** Arbitrary table starting with a position column. */
    POS_TABLE("Any table starting with position column"),
    /** Any arbitrary table with no synchronization in the viewers. */
    ANY_TABLE("Any other table");

    private final String name;

    private TableType(String name) {
        this.name = name;
    }

    /**
     * @return The user-readable String representation of the CsvPreference.
     */
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
    
}
