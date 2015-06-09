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

package de.cebitec.readxplorer.tools.snpdetection;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.databackend.dataobjects.Snp;
import de.cebitec.readxplorer.databackend.dataobjects.SnpI;
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
import java.util.Map.Entry;
import java.util.TreeSet;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;


/**
 * NP phylogeny class.
 * <p>
 * @author jhess
 */
public class SnpPhylogeny {

    public static final String FDNAML_PATH = "PathFdnaml";

    InputOutput io;
    SnpDetectionResult snpData;
    HashMap<Integer, HashMap<String, Snp>> sortedSnps;


    /**
     * A new SNP phylogeny.
     * <p>
     * @param snpData The snp data for which the phylogeny shall be created
     */
    public SnpPhylogeny( SnpDetectionResult snpData ) {
        this.io = IOProvider.getDefault().getIO( NbBundle.getMessage( SnpPhylogeny.class, "SNP_Phylogeny.output.name" ), false );
        this.snpData = snpData;
        createAlignment( snpData );
    }


    /**
     * Create an alignment from a SNP detection result.
     * <p>
     * @param snpData The snp data for which the alignment shall be created
     */
    private void createAlignment( SnpDetectionResult snpData ) {

        int numberOfTracks;
        Map<Integer, Map<Integer, String>> bases = new HashMap<>();
        Map<Integer, String> refBases = new HashMap<>();
        List<SnpI> snps = snpData.getSnpList();
        Map<Integer, PersistentTrack> trackNames = snpData.getTrackMap();
        List<Integer> trackIdsWithSnps = new ArrayList<>();

        for( SnpI snpi : snps ) {

            Snp snp = (Snp) snpi;

            // get numberOfTracks (with snpData)
            if( !trackIdsWithSnps.contains( snp.getTrackId() ) ) {
                trackIdsWithSnps.add( snp.getTrackId() );
            }

            int position = snp.getPosition();

            // save reference base
            if( !refBases.containsKey( position ) ) {
                refBases.put( position, snp.getRefBase() );
            }

            // save bases per track in a hashmap again hashed to their position
            if( !bases.containsKey( position ) ) {
                HashMap<Integer, String> track = new HashMap<>();
                bases.put( position, track );
            }
            Map<Integer, String> track = bases.get( position );
            track.put( snp.getTrackId(), snp.getBase() );
        }

        numberOfTracks = trackIdsWithSnps.size();


        // add reference base to all position maps, where it is missing for at least one track
        Iterator<Entry<Integer, Map<Integer, String>>> positionIterator = bases.entrySet().iterator();
        while( positionIterator.hasNext() ) {

            Map.Entry<Integer, Map<Integer, String>> posToBaseMap = positionIterator.next();
            Map<Integer, String> positionEntry = posToBaseMap.getValue();
            // fill positions without snpData with reference base

            for( int trackId : trackIdsWithSnps ) {
                if( !positionEntry.containsKey( trackId ) ) {
                    positionEntry.put( trackId, String.valueOf( refBases.get( posToBaseMap.getKey() ) ) );
                }
            }
        }

        Map<Integer, Integer> trackIdToIndex = this.getTrackIdToIndexMap( trackIdsWithSnps );
        String[] alignment = new String[numberOfTracks + 1];
        for( Integer l : new TreeSet<>( bases.keySet() ) ) {
            Map<Integer, String> snpsAtPosMap = bases.get( l );
            for( Integer trackId : new TreeSet<>( snpsAtPosMap.keySet() ) ) {
                if( alignment[trackIdToIndex.get( trackId )] == null ) {
                    alignment[trackIdToIndex.get( trackId )] = ">" + trackNames.get( trackId ).getDescription() + System.getProperty( "line.separator" );
                }
                alignment[trackIdToIndex.get( trackId )] += snpsAtPosMap.get( trackId );
            }
        }

        File alignmentFile = this.createMultipleFastaFile( alignment );
        this.runFdnaml( alignmentFile );

    }


    /**
     * Creates the multiple .fasta file for input in fdnaml.
     * <p>
     * @param alignment alignment of snpData to write as multiple fasta
     * <p>
     * @return The multiple fasta file.
     */
    private File createMultipleFastaFile( String[] alignment ) {

        File file;
        FileWriter writer;

        file = new File( "./alignment.fasta" ); //todo: delete again?
        if( file.exists() ) {
            file.delete();
        }
        try {
            // new FileWriter(file) - falls die Datei bereits existiert
            // wird diese überschrieben
            writer = new FileWriter( file, true );
//            int track = 0;
            for( String s : alignment ) {
                if( s != null ) {
//                    writer.write(">" + track);
//                    writer.write(System.getProperty("line.separator"));
//                    track++;

                    writer.write( s );
                    writer.write( System.getProperty( "line.separator" ) );
                }
            }
            writer.write( System.getProperty( "line.separator" ) );
            writer.flush();
            writer.close();
        } catch( IOException e ) {
            JOptionPane.showMessageDialog( new JPanel(), NbBundle.getMessage( SnpPhylogeny.class, "SNP_Phylogeny.ERROR.Write_Alignment" ),
                                           NbBundle.getMessage( SnpPhylogeny.class, "SNP_Phylogeny.ERROR.Write_File" ), JOptionPane.ERROR_MESSAGE );
        }

        return file;
    }


    /**
     * Runs fdnaml to generate the phylogenetic tree from the alignment file
     * <p>
     * @param alignmentFile multiple fasta file to generate phylogenetic tree
     */
    private void runFdnaml( File alignmentFile ) {
        // start fdnaml to generate the tree
        try {
            String fdnamlPath = NbPreferences.forModule( SnpPhylogeny.class ).get( SnpPhylogeny.FDNAML_PATH, "/vol/emboss-6.2/bin/fdnaml" );
            ProcessBuilder fdnaml = new ProcessBuilder( fdnamlPath, alignmentFile.toString() );

            /*
             * TODO: find tool that prints rooted phylogenetic trees
             *
             * aminosäuren: sauer A, L alipathisch, M Amid-Gruppe, R aromatisch,
             * C basisch, H Hydroxyl-Gruppe, I Imino-Gruppe, S schwefelhaltig
             *
             * Service Name:
             * {http://soaplab.org/phylogeny_molecular_sequence/fdnaml.sa}fdnaml.saService
             * Port Name:
             * {http://soaplab.org/phylogeny_molecular_sequence/fdnaml.sa}fdnaml.saPort
             *
             * Address:
             * http://www.ebi.ac.uk:80/soaplab/typed/services/phylogeny_molecular_sequence.fdnaml.sa
             * WSDL:
             * http://www.ebi.ac.uk:80/soaplab/typed/services/phylogeny_molecular_sequence.fdnaml.sa?wsdl
             * Implementation class: org.soaplab.typedws.ServiceProvider
             */

            Process fdnamlProcess = fdnaml.start();
            OutputStreamWriter output = new OutputStreamWriter( fdnamlProcess.getOutputStream() );
            PrintWriter printOutput = new PrintWriter( output );
            printOutput.println( "\n" );
            output.close();
            output = new OutputStreamWriter( fdnamlProcess.getOutputStream() );
            printOutput = new PrintWriter( output );
            printOutput.println( "./alignment.fdnaml" );
            output.close();
            //Runtime.getRuntime().exec("/vol/emboss-6.2/bin/fdnaml -sequence " + file + " - outfile " + file + ".fdnaml");
        } catch( IOException e ) {
            JOptionPane.showMessageDialog( new JPanel(), NbBundle.getMessage( SnpPhylogeny.class, "SNP_Phylogeny.ERROR.Fdnaml" ),
                                           NbBundle.getMessage( SnpPhylogeny.class, "SNP_Phylogeny.ERROR.Software" ), JOptionPane.ERROR_MESSAGE );
        }
    }


    /**
     * Creates a mapping from each track id to a corresponding index.
     * <p>
     * @param trackIdList list of used trackIds
     * <p>
     * @return mapping from each track id to a corresponding index.
     */
    private HashMap<Integer, Integer> getTrackIdToIndexMap( List<Integer> trackIdList ) {
        HashMap<Integer, Integer> trackIdToIndexMap = new HashMap<>();
        int i = 0;
        for( int trackId : trackIdList ) {
            trackIdToIndexMap.put( trackId, i++ );
        }
        return trackIdToIndexMap;
    }


}
