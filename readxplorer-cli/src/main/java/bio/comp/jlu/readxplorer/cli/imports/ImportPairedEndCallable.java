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

package bio.comp.jlu.readxplorer.cli.imports;


import bio.comp.jlu.readxplorer.cli.imports.ImportPairedEndCallable.ImportPairedEndResults;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable.ImportReferenceResult;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.parser.ReadPairJobContainer;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.SamBamStatsParser;
import de.cebitec.readxplorer.parser.output.SamBamCombiner;
import de.cebitec.readxplorer.readpairclassifier.SamBamReadPairClassifier;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;

import static java.util.logging.Level.SEVERE;


/**
 *
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public final class ImportPairedEndCallable implements Callable<ImportPairedEndResults> {

    private static final Logger LOG = Logger.getLogger( ImportPairedEndCallable.class.getName() );

    private final ImportReferenceResult referenceResult;
    private final ReadPairJobContainer rpjc;


    public ImportPairedEndCallable( ImportReferenceResult referenceResult, ReadPairJobContainer rpjc ) {

        this.referenceResult = referenceResult;
        this.rpjc = rpjc;

    }


    @Override
    public ImportPairedEndResults call() throws CommandException {

        try {

            /**
             * Algorithm: start file if (PersistentTrack not yet imported) {
             * convert file 1 to sam/bam, if necessary if (isTwoTracks) {
             * convert file 2 to sam/bam, if necessary combine them unsorted
             * (NEW FILE) } sort by readseq (NEW FILE) - if isTwoTracks:
             * deleteOldFile parse mappings sort by read name (NEW FILE) -
             * deleteOldFile read pair classification, extension & sorting by
             * coordinate - deleteOldFile } create position table (advantage: is
             * already sorted by coordinate & classification in file)
             */

            final ImportPairedEndResults iper = new ImportPairedEndResults();
            final TrackJob trackJob1 = rpjc.getTrackJob1();
            final TrackJob trackJob2 = rpjc.getTrackJob2();
            final Map<String, Integer> chromLengthMap = new HashMap<>();
            Map<Integer, PersistentChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector( referenceResult.getParsedReference().getID() ).getRefGenome().getChromosomes();
            for( PersistentChromosome chrom : chromIdMap.values() ) {
                chromLengthMap.put( chrom.getName(), chrom.getLength() );
            }

            final File inputFile1 = trackJob1.getFile();
            inputFile1.setReadOnly(); // prevents changes or deletion of original file!
            final StatsContainer statsContainer = new StatsContainer();
            statsContainer.prepareForTrack();
            statsContainer.prepareForReadPairTrack();

            try {
                // executes any conversion before other calculations, if the parser supports any
                if( !trackJob1.getParser().convert( trackJob1, chromLengthMap ) ) {
                    LOG.log( SEVERE, "Conversion of {0} failed!", trackJob1.getName() );
                    return null;
                }

                File lastWorkFile = trackJob1.getFile(); // file which was created in the last step of the import process
                if( trackJob2 != null ) { // only combine, if data is not already combined
                    File inputFile2 = trackJob2.getFile();
                    inputFile2.setReadOnly();
                    boolean success = trackJob2.getParser().convert( trackJob2, chromLengthMap );
                    File lastWorkFile2 = trackJob2.getFile();
                    if( !success ) {
                        LOG.log( SEVERE, "Conversion of {0} failed!", trackJob2.getName() );
                        return null;
                    }

                    // combine both tracks and continue with trackJob1, they are unsorted now
                    SamBamCombiner combiner = new SamBamCombiner( trackJob1, trackJob2, false );
                    if( !combiner.combineData() ) {
                        LOG.log( SEVERE, "Combination of {0} and {1} failed!", new Object[]{ trackJob1.getName(), trackJob2.getName() } );
                        return null;
                    }
                    GeneralUtils.deleteOldWorkFile( lastWorkFile ); // either were converted or are write protected
                    GeneralUtils.deleteOldWorkFile( lastWorkFile2 );
                    lastWorkFile = trackJob1.getFile(); // the combined file
                    inputFile2.setWritable( true );
                }

                // extension for both classification and read pair info
                SamBamReadPairClassifier samBamDirectReadPairClassifier = new SamBamReadPairClassifier( rpjc, chromLengthMap );
                samBamDirectReadPairClassifier.setStatsContainer( statsContainer );
                samBamDirectReadPairClassifier.classifyReadPairs();

                // delete the combined file, if it was combined, otherwise the orig. file cannot be deleted
                GeneralUtils.deleteOldWorkFile( lastWorkFile );

            } catch( OutOfMemoryError | ParsingException ex ) {
                LOG.log( SEVERE, "Error during parsing of bam track!", ex );
                return null;
            }
            inputFile1.setWritable( true );

            // create general track stats
            SamBamStatsParser statsParser = new SamBamStatsParser();
            statsParser.setStatsContainer( statsContainer );
            ParsedTrack track = statsParser.createTrackStats( trackJob1, chromLengthMap );
            iper.setParsedTrack( track );

            return iper;

        } catch( IOException | OutOfMemoryError ex ) {
            LOG.log( Level.SEVERE, null, ex );
            CommandException ce = new CommandException( 1, "import failed!" );
            ce.initCause( ex );
            throw ce;
        }

    }


    public class ImportPairedEndResults {

        private final List<String> output;
        private ParsedTrack pt;


        ImportPairedEndResults() {
            this.output = new ArrayList<>( 10 );
        }


        void addOutput( String msg ) {
            output.add( msg );
        }


        public List<String> getOutput() {
            return Collections.unmodifiableList( output );
        }


        void setParsedTrack( ParsedTrack pr ) {
            this.pt = pr;
        }


        public ParsedTrack getParsedTrack() {
            return pt;
        }


    }

}
