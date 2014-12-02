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
package de.cebitec.readXplorer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * CommandLineUtils bundles tools for running external scripts on the command line
 * @author Evgeny Anisiforov
 */
public class CommandLineUtils {
    
    private final SimpleOutput out;
    
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
        this.out.showMessage(msg);
    }
    
    public CommandLineUtils(SimpleOutput output) {
        this.out = output;
    }
    
    /** 
     * run a command line tool and write the output to the console 
     * @param command
     * @throws java.io.IOException
     */
    public void runCommandAndWaitUntilEnded(String... command) throws IOException {
        this.showMsg("executing following command: "+GeneralUtils.implode(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        Process process = processBuilder.start();
        
        try (InputStream is = process.getInputStream()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            
            // And print each line
            String s;
            while ((s = reader.readLine()) != null) {
                this.showMsg(s);
            }
        }
        
        //Wait to get exit value
        try {
            int exitValue = process.waitFor();
            this.showMsg("\n\nExit Value is " + exitValue);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
