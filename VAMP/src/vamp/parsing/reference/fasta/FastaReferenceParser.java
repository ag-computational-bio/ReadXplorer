/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 *   This file is part of ProSE.
 *   Copyright (C) 2007-2010 CeBiTec, Bielefeld University
 * 
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 * 
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 * 
 *   You should have received a copy of the GNU General Public License
 *   along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package vamp.parsing.reference.fasta;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import vamp.importer.ReferenceJob;
import vamp.parsing.common.ParsedReference;
import vamp.parsing.common.ParsingException;
import vamp.parsing.reference.Filter.FeatureFilter;
import vamp.parsing.reference.ReferenceParserI;

/**
 *
 * @author jstraube
 */
public class FastaReferenceParser implements ReferenceParserI {

    private static String parsername = "Fasta Reference Parser";
    private static String[] fileExtension = new String[]{"fas", "fasta"};
    private static String fileDescription = "Fasta File";

    @Override
    public ParsedReference parseReference(ReferenceJob referenceJob, FeatureFilter filter) throws ParsingException {
        ParsedReference refGenome = new ParsedReference();
        String sequence = "";
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Start reading file  \"" + referenceJob.getFile() + "\"");
        try {

            BufferedReader in = new BufferedReader(new FileReader(referenceJob.getFile()));
            
            refGenome.setDescription(referenceJob.getDescription());
            refGenome.setName(referenceJob.getName());
            refGenome.setTimestamp(referenceJob.getTimestamp());
            String line = null;
           
            while ((line = in.readLine()) != null) {
                if (!line.startsWith(">")) {
                    sequence = sequence + line;
                }
            }

            refGenome.setSequence(sequence.toLowerCase());

        } catch (Exception ex) {
            throw new ParsingException(ex);
        }

        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Finished reading file  \"" + referenceJob.getFile() + "\"" + "genome length:" + sequence.length());
        return refGenome;

    }

    @Override
    public String getParserName() {
        return parsername;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }
}
