/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.util;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * CommandLineUtils bundles tools for running external scripts on the command line
 * @author Evgeny Anisiforov
 */
public class CommandLineUtils {
    
    private SimpleOutput out;
    
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
     */
    public void runCommandAndWaitUntilEnded(String... command) throws IOException {
        this.showMsg("executing following command: "+GeneralUtils.implode(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        Process process = processBuilder.start();
        
        java.io.InputStream is = process.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
        
        // And print each line
        String s = null;
        while ((s = reader.readLine()) != null) {
            this.showMsg(s);
        }
        is.close();
        
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
