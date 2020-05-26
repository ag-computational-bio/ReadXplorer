/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.internal.parser.embl;

import com.google.common.base.Joiner;
import de.cebitec.common.internal.parser.common.ConfigurableParserAndWriter;
import de.cebitec.common.parser.data.common.DataClass;
import de.cebitec.common.parser.data.common.MolecularType;
import de.cebitec.common.parser.data.embl.EmblTaxonomicDivision;
import de.cebitec.common.parser.data.genbank.GenbankTaxonomicDivision;
import de.cebitec.common.parser.data.common.TaxonomicDivisionUtilities;
import de.cebitec.common.parser.data.common.Topology;
import de.cebitec.common.parser.data.embl.Identification;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class EmblIdentificationParser extends ConfigurableParserAndWriter<Identification> {

    @Override
    public Identification parse(CharSequence data) {
        String[] split = data.toString().split(";");
        Identification id = new Identification();
        
        if (!this.isLaxParser()) { //strict validation parser
            id.setPrimaryAccession(split[0].trim());
            id.setSequenceVersion(Integer.parseInt(split[1].trim().replace("SV ", "")));
            id.setTopology(Topology.valueOf(split[2].trim()));
            id.setMolecular_type(MolecularType.getByText(split[3].trim()));
            id.setData_class(DataClass.valueOf(split[4].trim()));
            id.setTaxonomy_division(TaxonomicDivisionUtilities.getByCode(Arrays.asList(EmblTaxonomicDivision.values()), split[5].trim()));
            id.setSequence_length(Integer.parseInt(split[6].trim().replaceAll("BP.", "").trim()));
        } else { //lax parser mode
            this.parseIdLax(split, id);
        }
        return id;
    }

    @Override
    public String write(Identification id) {
        String idLine = "";
        if (!this.isLaxParser()) {
            idLine = Joiner.on("; ").join(id.getPrimaryAccession(), "SV " + id.getSequenceVersion(), id.getTopology(), 
                    id.getMolecular_type().getText(), id.getData_class(), id.getTaxonomy_division().getCode(), id.getSequence_length() + " BP.");
        } else {
            idLine = this.writeIdLax(id);
        }
        return idLine;
    }

    /**
     * Parse id in a mode where all fields, except primaryAccession and sequence
     * length are optional.
     * @param split Data to parse
     * @param id id to store the parsed values
     * @return an id object parsed in lax fashion
     */
    private Identification parseIdLax(String[] split, Identification id) {
        int i = 0;
        id.setPrimaryAccession(split[i++].trim());

        String sequenceVersion = split[i].trim().replace("SV ", "");
        try {
            id.setSequenceVersion(Integer.parseInt(sequenceVersion));
            ++i;
        } catch (NumberFormatException e) {
            //i is not increased
        }

        if (split.length > i) {
            try {
                Topology topology = Topology.valueOf(split[i].trim());
                id.setTopology(topology);
                ++i;
            } catch (IllegalArgumentException e) {
                //do not increase index i
            }
        }

        if (split.length > i) {
            MolecularType molecularType = MolecularType.getByText(split[i].trim());
            if (molecularType != null) {
                id.setMolecular_type(molecularType);
                ++i;
            }
        }
        
        if (split.length > i) {
            try {
                DataClass dataClass = DataClass.valueOf(split[i].trim());
                id.setData_class(dataClass);
                ++i;
            } catch (IllegalArgumentException e) {
                //do not increase index i
            }
        }
        
        if (split.length > i) {
            EmblTaxonomicDivision taxDivison = TaxonomicDivisionUtilities.getByCode(Arrays.asList(EmblTaxonomicDivision.values()), split[i].trim());
            if (taxDivison != null) {
                id.setTaxonomy_division(taxDivison);
                ++i;
            }
        }
        
        id.setSequence_length(Integer.parseInt(split[i].trim().replaceAll("BP.", "").trim()));
        return id;
    }

     /**
     * Write id in a mode where all fields, except primaryAccession and sequence
     * length are optional.
     * @param id id to write
     * @return A finished Identification
     */
    private String writeIdLax(Identification id) {
        
        Logger logger = Logger.getLogger(DateParser.class.getName());
        Joiner joiner = Joiner.on("; ");
        
        String idLine  = id.getPrimaryAccession();
        
        if (id.getSequenceVersion() != null) {
            idLine = joiner.join(idLine, "SV " + id.getSequenceVersion());
        } else {
            logger.log(Level.WARNING, null, "Sequence version of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            idLine = joiner.join(idLine, "SV 1");
        }

        if (id.getTopology() != null) {
            idLine = joiner.join(idLine, id.getTopology());
        } else {
            logger.log(Level.WARNING, null, "Topology of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            idLine = joiner.join(idLine, Topology.linear);
        }

        if (id.getMolecular_type() != null) {
            idLine = joiner.join(idLine, id.getMolecular_type().getText());
        } else {
            logger.log(Level.WARNING, null, "Molecular type of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            idLine = joiner.join(idLine, MolecularType.unassignedDNA.getText());
        }
        
        if (id.getData_class() != null) {
            idLine = joiner.join(idLine, id.getData_class());
        } else {
            logger.log(Level.WARNING, null, "Data class of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            idLine = joiner.join(idLine, DataClass.GSS);
        }
        
        if (id.getTaxonomy_division()!= null) {
            idLine = joiner.join(idLine, id.getTaxonomy_division().getCode());
        } else {
            logger.log(Level.WARNING, null, "Taxonomy id of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            idLine = joiner.join(idLine, GenbankTaxonomicDivision.Unannotated.getCode());
        }
        
        idLine = joiner.join(idLine, id.getSequence_length() + " BP.");

        return idLine;
    }

}
