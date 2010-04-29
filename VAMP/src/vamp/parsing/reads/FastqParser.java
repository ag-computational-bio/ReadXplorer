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

package vamp.parsing.reads;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vamp.importer.RunJob;
import vamp.parsing.common.ParsedRun;
import vamp.parsing.common.ParsingException;

/**
 *
 * @author jstraube
 */
public class FastqParser implements RunParserI{
    private static String name = "Fastq Parser";
    private static String [] fileExtension = new String[]{"fastq"};
    private static String fileDescription = "Fastq File";


/**
 * fastq files contains reads of e.g solexa, sanger or 454 sequencing.
 * Every read should be described in 4 lines.
 * first line: read descrption should start with and @
 * second line: sequence
 * third line: starts with and +, its optional to write the description again
 * fourth line: contains the phred values of the base calling encoded in ASCII
 * should be as long as the sequence itself
 * e.g.
 * @SLXA-B3_649_FC8437_R1_1_1_610_79
 * GATGTGCAATACCTTTGTAGAGGAA
 * +SLXA-B3_649_FC8437_R1_1_1_610_79
 * YYYYYYYYYYYYYYYYYYWYWYYSU
 *
 * @param runJob
 * @return run  returns the parsed description and the sequence
 * @throws ParsingException
 */

        @Override
    public ParsedRun parseRun(RunJob runJob) throws ParsingException{
       
        File file = runJob.getFile();
        String description = runJob.getDescription();

        ParsedRun run = new ParsedRun(description);
        run.setTimestamp(runJob.getTimestamp());
        HashMap<String,String> errorList = new HashMap<String, String>();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Lesen der Read Datei "+file);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = null;
            String readname = null;
            String sequence = null;
            int lineNumber = 1;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("@") && lineNumber == 1) {
                    if(line.length() >=100){
                    readname = line.substring(1, 100);
                    }else{
                    readname = line.substring(1);
                    }
                    System.out.println("Name "  + readname);
                    lineNumber ++;
                } else if (line.matches("\\w*\\n*\\.*~*") && lineNumber == 2) {
                    sequence = line;
                    System.out.println("Sequence " + sequence);
                     lineNumber ++;
                }else if(line.startsWith("+") && lineNumber == 3){
                  if(line.length()>1 && line.substring(1).equals(readname)){
                      lineNumber++;
                       System.out.println("optional column: " + line);
                  } else if (line.length() ==1 ){
                      lineNumber++;
                       System.out.println("no optional column");
                  }
                }else if(line.matches("\\p{ASCII}*") && lineNumber == 4 && line.length() == sequence.length()){
                    System.out.println("Quality:" + line);
                     run.addReadData(sequence, readname);
                     lineNumber =1;
                }else{
                   Logger.getLogger(this.getClass().getName()).log(Level.WARNING,"Coundt read read: " + readname);
                   String error = "Fehler in Zeile" + lineNumber;
                   errorList.put(readname,error);
                   lineNumber = 1;

                }
                
            }
            br.close();
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Lesen der Datei "+file+" beendet mit Folgenden fehlern " + errorList.toString());

        } catch (Exception ex) {
            throw new ParsingException(ex);
        }
        run.addErrorList(errorList);
        return run;

    }


    @Override
    public String getParserName() {
        return name;
    }

    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }

    @Override
    public String[] getFileExtensions() {
        return fileExtension;
    }

}
