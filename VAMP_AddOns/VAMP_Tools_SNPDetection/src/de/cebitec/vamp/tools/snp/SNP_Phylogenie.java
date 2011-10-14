/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.tools.snp;

import de.cebitec.vamp.api.objects.Snp;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jhess
 */
public class SNP_Phylogenie {
    
    List<Snp> snps;
    HashMap<Integer, HashMap<String,Snp>> sortedSnps;
    
    public SNP_Phylogenie(List<Snp> snps) {
        this.snps = snps;
        createAlignment(snps);
    }
    
    private void createAlignment(List<Snp> snps) {
        
        int numberOfTracks = 0;
        HashMap<Long, HashMap<Integer,String>> bases = new HashMap<Long, HashMap<Integer, String>>();
        HashMap<Long, String> refBases = new HashMap<Long, String>();
        
        for (Snp snp : snps) {
            
            // get numberOfTracks (with snps)
            if(snp.getTrack()>numberOfTracks) {
                numberOfTracks = snp.getTrack();
            }
            
            String positionString = snp.getPosition();
            if (positionString.contains("_")) {
                positionString = positionString.substring(0, positionString.length() - 2);
            }

            Long position = Long.parseLong(positionString);
            
            // save reference base
            if(!refBases.containsKey(position)) {
                refBases.put(position, snp.getRefBase().toUpperCase());
            }
            
            // save bases per track
            if(!bases.containsKey(position)) {
                HashMap<Integer,String> track = new HashMap<Integer, String>();
                bases.put(position, track);
            }
            HashMap<Integer,String> track = bases.get(position);
            track.put(snp.getTrack(), snp.getBase());
        }

        
        
        Iterator positionIterator = bases.entrySet().iterator();
        while (positionIterator.hasNext()) {

                Map.Entry e = (Map.Entry) positionIterator.next();
                HashMap<Integer, String> positionEntry = (HashMap<Integer, String>) e.getValue();
                // fill positions without snps with reference base
                for(int i = 0; i<=numberOfTracks; i++) {
                    if(!positionEntry.containsKey(i)){
                        positionEntry.put(i, String.valueOf(refBases.get(e.getKey())));
                    }
                }
        }
        
        String[] alignment = new String[numberOfTracks+1];
        for(Long l : new TreeSet<Long>(bases.keySet())) {
            HashMap<Integer, String> entry = bases.get(l);
            for(Integer i : new TreeSet<Integer>(entry.keySet())) {
                if(alignment[i]==null){
                    alignment[i] = "";
                }
                alignment[i] = alignment[i] + entry.get(i);
            }
        }

        // create .fasta file for input in fdnaml
   
        File file;
        FileWriter writer;

        file = new File("./alignment.fasta");
        if (file.exists()) {
            file.delete();
        }
        try {
            // new FileWriter(file) - falls die Datei bereits existiert
            // wird diese überschrieben
            writer = new FileWriter(file, true);
            int track = 0;
            for (String s : alignment) {
                if (s != null) {
                    writer.write(">Track" + track);
                    writer.write(System.getProperty("line.separator"));
                    track++;

                    writer.write(s);
                    writer.write(System.getProperty("line.separator"));
                }
            }
            writer.write(System.getProperty("line.separator"));
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
  
        // start fdnaml to generate the tree
        try {
            ProcessBuilder fdnaml = new ProcessBuilder("/vol/emboss-6.2/bin/fdnaml", file.toString());
            Process fdnamlProcess = fdnaml.start();
            //System.out.println(fdnamlProcess.getOutputStream());
            OutputStreamWriter eingabe = new OutputStreamWriter(fdnamlProcess.getOutputStream());
            PrintWriter eingeben = new PrintWriter(eingabe);
            eingeben.println("\n");
            eingabe.close();
            eingabe = new OutputStreamWriter(fdnamlProcess.getOutputStream());
            eingeben = new PrintWriter(eingabe);
            eingeben.println("./alignment.fdnaml");
            eingabe.close();
            //System.out.println(fdnamlProcess.getOutputStream());
            //Runtime.getRuntime().exec("/vol/emboss-6.2/bin/fdnaml -sequence " + file + " - outfile " + file + ".fdnaml");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

    }
    
}
