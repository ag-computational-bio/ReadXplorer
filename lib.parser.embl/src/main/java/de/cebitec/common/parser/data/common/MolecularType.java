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

package de.cebitec.common.parser.data.common;

/**
 * Molecular type of the NA - like DNA, RNA, mRNA...
 */
public enum MolecularType {
    RNA("RNA"), DNA("DNA"), GenomicDNA("genomic DNA"), GenomicRNA("genomic RNA"), mRNA("mRNA"), tRNA("tRNA"), rRNA("rRNA"), OtherRNA("other RNA"), OtherDNA("other DNA"), transcribedRNA("transcribed RNA"), ViralcRNA("viral cRNA"), unassignedDNA("unassigned DNA"), unassignedRNA("unassigned RNA");
    private String text;

    private MolecularType(String text) {
        this.text = text;
    }

    /**
     * @return Textual representation of the molecular type.
     */
    public String getText() {
        return text;
    }

    public static MolecularType getByText(String text) {
        MolecularType[] values = MolecularType.values();
        for (MolecularType mt : values) {
            if (text.equals(mt.getText())) {
                return mt;
            }
        }
        return null;
    }
    
}
