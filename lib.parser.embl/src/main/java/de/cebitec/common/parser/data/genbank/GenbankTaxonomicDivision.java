/*
 * Copyright (C) 2014 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.common.parser.data.genbank;

import de.cebitec.common.parser.data.common.TaxonomicDivision;

/**
 * Taxonomic division for Genbank format.
 *
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public enum GenbankTaxonomicDivision implements TaxonomicDivision {
    
    Bacterial("BCT"),
    Bacteriophage("PHG"),
    EST("EST"),
    Environmental("ENV"),
    Genome_Survey_Seq("GSS"),
    High_Throughput_Genomic_Seq("HTG"),
    Invertebrate("INV"),
    Patent_Seq("PAT"),
    Plant("PLN"),
    Primate("PRI"),
    Other_Mammal("MAM"),
    Other_Rodent("ROD"),
    Other_Vertebrate("VRT"),
    Sequence_Tagged_Sits("STS"),
    Synthetic("SYN"),
    Unannotated("UNA"),
    Unfinished_High_Throughput_cDNA("HTC"),
    Viral("VRL");

    private final String code;

    private GenbankTaxonomicDivision(String code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.toString();
    }
}
