/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.tools.gasv;

import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.MissingResourceException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle;


/**
 * Handles all steps necessary for calling GASV to predict genome
 * rearrangements.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class GASVCaller {


    /**
     * Handles all steps necessary for calling GASV to predict genome
     * rearrangements.
     */
    public GASVCaller() {
    }


    /**
     * Executes all necessary steps to prepare and run GASV for the detection of
     * genome rearrangements within read mapping data.
     * <p>
     * @param reference The reference whose tracks are analyzed here.
     */
    public void callGASV( PersistentReference reference ) {
//        Algorithm:
//        1. create chromosome naming file as references will most certainly contain other names than numbers
//        2. Call GASV
        createChromosomeNamingFile( reference );


    }


    /**
     * Creates a chromosome naming file of the form "chr-name chr-id". E.g.:
     * <br/>
     * <br/>PAO1 1
     * <br/>plasmidX 2
     * <p>
     * @param reference The reference whose tracks are analyzed here.
     */
    @NbBundle.Messages( { "Error=An error occured during the file saving process.",
                          "ProgressName=Storing chromosome names in file...",
                          "SuccessMsg=Chromosome names successfully stored in ",
                          "SuccessHeader=Success" } )
    private void createChromosomeNamingFile( PersistentReference reference ) {
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        String dbLocation = projectConnector.getDBLocation();
        String refName = reference.getName();
        String chromNamesFileName = new File( dbLocation ).getParent().concat( "\\" + refName.concat( "-seqNames-gasv.txt" ) );

        ProgressHandle progressHandle = ProgressHandleFactory.createHandle( Bundle.ProgressName() );
        final String chromNamesString = createChromNamesString( reference );
        progressHandle.start();

        Thread exportThread = new Thread( new Runnable() {

            @Override
            public void run() {

                //Note that file is overwritten every time!
                try( final BufferedWriter outputWriter = new BufferedWriter( new FileWriter( chromNamesFileName ) ); ) {
                    outputWriter.write( chromNamesString );
                    NotificationDisplayer.getDefault().notify( Bundle.SuccessHeader(), new ImageIcon(),
                                                               Bundle.SuccessMsg() + chromNamesFileName, null );
                } catch( IOException | MissingResourceException | HeadlessException e ) {
                    JOptionPane.showMessageDialog( new JPanel(), Bundle.Error() + e.getMessage() );
                }
                progressHandle.finish();
            }


        } );
        exportThread.start();
    }


    /**
     * Creates a chromosome names string with one chromosome per line of the
     * form "chr-name chr-id". E.g.:
     * <br/>
     * <br/>PAO1 1
     * <br/>plasmidX 2
     * <p>
     * @param reference The reference connector of the reference whose tracks
     *                     are analyzed here.
     * <p>
     * @return A String with one chromosome name and a unique id per line for
     *         all chromosomes of the given reference genome.
     */
    private String createChromNamesString( PersistentReference reference ) {
        StringBuilder chromNamesBuilder = new StringBuilder( 20 );
        Map<Integer, PersistentChromosome> chromosomes = reference.getChromosomes();
        int chromNo = 1;
        for( PersistentChromosome chrom : chromosomes.values() ) {
            chromNamesBuilder.append( chrom.getName() ).append( " " ).append( chromNo++ ).append( "\n" );
        }

        return chromNamesBuilder.toString();
    }


}
