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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.api.enums.SequenceComparison;


/**
 * Contains the two amino acid triplets for the reference and the mapped genome
 * plus the identifier for the gene to which the triplet belongs to.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class CodonSnp {

    private final String tripletRef;
    private final String tripletSnp;
    private final char aminoRef;
    private final char aminoSnp;
    private final SequenceComparison effect;
    private final PersistentFeature feature;


    /**
     * Contains the two DNA triplets for the reference and the mapped genome
     * plus the identifier for the gene to which the triplet belongs.
     * <p>
     * @param tripletRef DNA triplet of the reference genome for a certain
     *                   position
     * @param tripletSnp DNA triplet of the mapped genome for the same position
     * @param aminoRef   amino acid of the reference genome for a certain
     *                   position
     * @param aminoSnp   amino acid of the mapped genome for the same position
     * @param effect     the effect type of a snp on the amino acid sequence
     *                   among SequenceUtils.SUBSTITUTION,
     *                   SequenceUtils.MATCH, SequenceUtils.DELETION, SequenceUtils.INSERTION
     * @param feature    identifier of the gene to which the triplet belongs
     */
    public CodonSnp( String tripletRef, String tripletSnp, char aminoRef, char aminoSnp, SequenceComparison effect, PersistentFeature feature ) {
        this.tripletRef = tripletRef;
        this.tripletSnp = tripletSnp;
        this.feature = feature;
        this.aminoRef = aminoRef;
        this.aminoSnp = aminoSnp;
        this.effect = effect;
    }


    /**
     * @return The reference genome triplet stored in this object.
     */
    public String getTripletRef() {
        return tripletRef;
    }


    /**
     * @return The mapped genome triplet stored in this object, which
     *         should/might differ from
     *         the reference triplet.
     */
    public String getTripletSnp() {
        return tripletSnp;
    }


    /**
     * @return The identifier of the gene or feature, this codon belongs to.
     */
    public PersistentFeature getFeature() {
        return feature;
    }


    /**
     * @return The reference genome amino acid stored in this object.
     */
    public char getAminoRef() {
        return aminoRef;
    }


    /**
     * @return The mapped genome amino acid stored in this object, which might
     *         differ from
     *         the reference amino acid.
     */
    public char getAminoSnp() {
        return aminoSnp;
    }


    /**
     * @return the effect type of a snp on the amino acid sequence among
     *         SequenceComparison values.
     */
    public SequenceComparison getEffect() {
        return effect;
    }


}
