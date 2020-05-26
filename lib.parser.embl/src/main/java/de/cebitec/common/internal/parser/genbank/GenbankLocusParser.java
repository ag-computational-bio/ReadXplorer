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
package de.cebitec.common.internal.parser.genbank;

import de.cebitec.common.internal.parser.common.ConfigurableParserAndWriter;
import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.parser.data.common.MolecularType;
import de.cebitec.common.parser.data.genbank.GenbankTaxonomicDivision;
import de.cebitec.common.parser.data.common.TaxonomicDivisionUtilities;
import de.cebitec.common.parser.data.common.Topology;
import de.cebitec.common.parser.data.embl.Date;
import de.cebitec.common.internal.parser.embl.DateParser;
import de.cebitec.common.parser.data.genbank.Locus;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parses the LOCUS line of a genbank file.
 * 
 * @author Rolf Hilker {@literal <rhilker at mikrobio.med.uni-giessen.de>}
 */
public class GenbankLocusParser extends ConfigurableParserAndWriter<Locus> implements Parser<Locus> {

    private final int BP_END = 40;
    private final int TAX_DIVISION_START = 64;
    
    /**
     * @param data The genbank locus line to parse
     * @return The locus line of a genbank entry. The mandatory fields are 
     * accession and sequence length. All other fields are tried to be parsed.
     * If the parsing fails, some of the other fields might be missing!
     */
    @Override
    public Locus parse(CharSequence data) {
        String[] split = data.toString().split(" ");
        List<String> nonEmptyEntries = this.removeEmptyEntries(split);
        Locus id = new Locus();
        
        if (!this.isLaxParser()) { //strict validation parser
            int i = 0;
            id.setPrimaryAccession(nonEmptyEntries.get(i++));
            id.setSequence_length(Integer.parseInt(nonEmptyEntries.get(i++)));
            ++i;
            id.setMolecular_type(MolecularType.getByText(nonEmptyEntries.get(i++)));
            if (nonEmptyEntries.get(i).equals(Topology.linear.toString())
                    || nonEmptyEntries.get(i).equals(Topology.circular.toString())) {
                id.setTopology(Topology.valueOf(nonEmptyEntries.get(i++)));
            } //optional field. If it does not exist we continue with the taxonomic division
            id.setTaxonomy_division(TaxonomicDivisionUtilities.getByCode(Arrays.asList(GenbankTaxonomicDivision.values()), nonEmptyEntries.get(i++)));
            if (nonEmptyEntries.size() > i) {
                id.setDate(nonEmptyEntries.get(i++));
            }

            //Get from VERSION line? id.setSequenceVersion(Integer.parseInt(split[1].trim().replace("SV ", "")));
            //Used in Topology id.setData_class(EntryHeader.DataClass.valueOf(split[4].trim()));
        } else { //lax parser mode
            this.parseIdLax(nonEmptyEntries, id);
        }
        
        //Get from VERSION line? id.setSequenceVersion(Integer.parseInt(split[1].trim().replace("SV ", "")));
        //Used in Topology id.setData_class(EntryHeader.DataClass.valueOf(split[4].trim()));
        return id;
    }

    /**
     * @param id
     * @return Writes a Genbank locus String. If mandatory fields are missing, 
     * an exception is thrown!
     * @throws IllegalStateException
     */
    @Override
    public String write(Locus id) {
        StringBuilder locusBuilder = new StringBuilder("LOCUS       ");
        locusBuilder.append(id.getPrimaryAccession());
        int noSpaces = BP_END - locusBuilder.length() - id.getSequence_length().toString().length();
        locusBuilder.append(this.createCongenericString(" ", noSpaces));
        locusBuilder.append(id.getSequence_length()).append(" bp    ");
        
        if (!this.isLaxParser()) {
            locusBuilder.append(id.getMolecular_type()).append("     ");
            noSpaces = TAX_DIVISION_START - locusBuilder.length() - id.getTopology().toString().length();
            locusBuilder.append(id.getTopology()).append(this.createCongenericString(" ", noSpaces));
            locusBuilder.append(id.getTaxonomy_division()).append(" ");
            locusBuilder.append(id.getDate());
        } else {
            this.writeLaxLocus(locusBuilder, id);
        }
        return locusBuilder.toString();
    }

    /**
     * Removes all empty entries from a given String array.
     * @param arrayToCleanUp The array whose entries shall be cleaned.
     * @return A new list with all non-empty elements of the array
     */
    private List<String> removeEmptyEntries(String[] arrayToCleanUp) {
        List<String> nonEmptyEntries = new ArrayList<>();
        for (String entry : arrayToCleanUp) {
            if (!entry.isEmpty()) {
                nonEmptyEntries.add(entry);
            }
        }
        return nonEmptyEntries;
    }

    /**
     * Create a congeneric string only consisting of a given number of repeats
     * of the given template string.
     * @param template the template to repeat noRepeat times 
     * @param noRepeats number of repeats for the given template string
     * @return A congeneric string only consisting of the given number of 
     * repeats of the given template string.
     */
    private String createCongenericString(String template, int noRepeats) {
        StringBuilder resultBuilder = new StringBuilder(template.length() * noRepeats);
        for (int i = 0; i < noRepeats; i++) {
            resultBuilder.append(template);
        }
        return resultBuilder.toString();
    }

    /**
     * Parse id in a mode where all fields, except primaryAccession and sequence
     * length are optional.
     * @param entries Data to parse
     * @param id locus to store the parsed values
     * @return an id object parsed in lax fashion
     */
    private void parseIdLax(List<String> entries, Locus id) {
        
        int i = 0;
        //accession and sequence length are the only mandatory parameters
        id.setPrimaryAccession(entries.get(i++));
        id.setSequence_length(Integer.parseInt(entries.get(i++)));
        ++i;
        
        if (entries.size() > i) {
            MolecularType molecularType = MolecularType.getByText(entries.get(i));
            if (molecularType != null) {
                id.setMolecular_type(molecularType);
                ++i;
            } //optional field. If it does not exist we continue with the topology
        }
        
        if (    entries.size() > i &&
               (entries.get(i).equals(Topology.linear.toString()) || 
                entries.get(i).equals(Topology.circular.toString()))) {
            id.setTopology(Topology.valueOf(entries.get(i++)));
        } //optional field. If it does not exist we continue with the taxonomic division
        
        if (entries.size() > i) {
            GenbankTaxonomicDivision taxonomy = TaxonomicDivisionUtilities.getByCode(Arrays.asList(GenbankTaxonomicDivision.values()), entries.get(i));
            if (taxonomy != null) {
                id.setTaxonomy_division(taxonomy);
                ++i;
            }
        }
        
        if (entries.size() > i) {
            id.setDate(entries.get(i));
        }
    }

    /**
     * Write a locus line in a mode where all fields, except primaryAccession 
     * and sequence length are optional.
     * @param locusBuilder builder to append data to
     * @param id id to write
     */
    private void writeLaxLocus(StringBuilder locusBuilder, Locus id) {
        Logger logger = Logger.getLogger(DateParser.class.getName());
        if (id.getMolecular_type() != null) {
            locusBuilder.append(id.getMolecular_type()).append("     ");
        } else {
            logger.log(Level.WARNING, null, "Molecular type of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            locusBuilder.append(MolecularType.unassignedDNA.getText()).append("     ");
        }

        int noSpaces = TAX_DIVISION_START - locusBuilder.length() - id.getTopology().toString().length();
        if (id.getTopology() != null) {
            locusBuilder.append(id.getTopology()).append(this.createCongenericString(" ", noSpaces));
        } else {
            logger.log(Level.WARNING, null, "Topology of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            locusBuilder.append(Topology.linear).append(this.createCongenericString(" ", noSpaces));
        }

        if (id.getTaxonomy_division() != null) {
            locusBuilder.append(id.getTaxonomy_division()).append(" ");
        } else {
            logger.log(Level.WARNING, null, "Taxonomic division of the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            locusBuilder.append(GenbankTaxonomicDivision.Unannotated).append(" ");
        }

        if (id.getDate() != null) {
            try {
                locusBuilder.append(DateParser.format.parse(id.getDate()));
            } catch (ParseException ex) {
                logger.log(Level.WARNING, null, "Date in the locus entry " + id.getPrimaryAccession() + " is defect, setting default value!");
                Date.Entry createdEntry = new Date.Entry(new java.util.Date(), 1, null);
                locusBuilder.append(createdEntry.getDate());
            }
        } else {
            logger.log(Level.WARNING, null, "Date in the locus entry " + id.getPrimaryAccession() + " is missing, setting default value!");
            Date.Entry createdEntry = new Date.Entry(new java.util.Date(), 1, null);
            locusBuilder.append(createdEntry.getDate());
        }
    }

}
