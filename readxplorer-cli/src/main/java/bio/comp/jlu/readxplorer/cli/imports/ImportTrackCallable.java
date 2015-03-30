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


import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable.ImportReferenceResult;
import bio.comp.jlu.readxplorer.cli.imports.ImportTrackCallable.ImportTrackResults;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.MappingParserI;
import de.cebitec.readxplorer.parser.mappings.SamBamStatsParser;
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


/**
 * Reads CLI Importer.
 * The <code>ImportTrackCallable</code> class is responsible for the parallelised
 * import of read files in the CLI version.
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public final class ImportTrackCallable implements Callable<ImportTrackResults> {

    private static final Logger LOG = Logger.getLogger( ImportTrackCallable.class.getName() );

    private final ImportReferenceResult referenceResult;
    private final TrackJob trackJob;


    /**
     * Reads CLI Importer
     *
     * @param referenceResult imported reference genome as mapping target
     * @param rpjc <code>TrackJob</code> with import information
     */
    public ImportTrackCallable( ImportReferenceResult referenceResult, TrackJob trackJob ) {

        this.referenceResult = referenceResult;
        this.trackJob = trackJob;

    }


    @Override
    public ImportTrackResults call() throws CommandException {

        final ImportTrackResults result = new ImportTrackResults( trackJob.getFile().getName() );
        try {

            // create necessary objects
            LOG.log( Level.FINE, "create import objects..." );
            result.addOutput( "create import objects..." );
            final File trackFile = trackJob.getFile();
            final MappingParserI mappingParser = trackJob.getParser();
            final Map<String, Integer> chromLengthMap = new HashMap<>();
            int refId = referenceResult.getParsedReference().getID();
            Map<Integer, PersistentChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector( refId ).getRefGenome().getChromosomes();
            for( PersistentChromosome chrom : chromIdMap.values() ) {
                chromLengthMap.put( chrom.getName(), chrom.getLength() );
            }
            final StatsContainer statsContainer = new StatsContainer();
            statsContainer.prepareForTrack();


            // executes any conversion before other calculations, if the parser supports any
            LOG.log( Level.FINE, "convert read file: {0}...", trackFile.getName() );
            result.addOutput( "convert file..." );
            trackFile.setReadOnly(); // prevents changes or deletion of original file!
            boolean success = mappingParser.convert( trackJob, chromLengthMap );
            File lastWorkFile = trackJob.getFile();


            // generate classification data in file sorted by read sequence
            LOG.log( Level.FINE, "parse read file: {0}...", trackFile.getName() );
            result.addOutput( "parse..." );
            mappingParser.setStatsContainer( statsContainer );
            mappingParser.parseInput( trackJob, chromLengthMap );
            if( success ) {
                GeneralUtils.deleteOldWorkFile( lastWorkFile );
            } // only when we reach this line without exceptions and conversion was successful
            trackFile.setWritable( true );

            // file needs to be sorted by coordinate for efficient calculation
            LOG.log( Level.FINE, "create classification statistics..." );
            result.addOutput( "create statistics..." );
            SamBamStatsParser statsParser = new SamBamStatsParser();
            statsParser.setStatsContainer( statsContainer );
            ParsedTrack track = statsParser.createTrackStats( trackJob, chromLengthMap );

            LOG.log( Level.FINE, "parsed read file: {0}", trackFile.getName() );
            result.addOutput( "parsed read file " + trackFile.getName() );
            result.setParsedTrack( track );
            result.setSuccessful( true );

        } catch( ParsingException | IOException ex ) {
            LOG.log( Level.SEVERE, ex.getMessage(), ex );
            result.addOutput( "Error: " + ex.getMessage() );
        } catch( OutOfMemoryError ex ) {
            LOG.log( Level.SEVERE, ex.getMessage(), ex );
            CommandException ce = new CommandException( 1, "ran out of memory!" );
            ce.initCause( ex );
            throw ce;
        }

        return result;

    }


    /**
     * Result Class.
     * Contains all available result and import information.
     */
    public final class ImportTrackResults {

        private final List<String> output;
        private boolean successful;
        private String fileName;
        private ParsedTrack pt;


        ImportTrackResults( String fileName ) {
            this.successful = false;
            this.fileName = fileName;
            this.output = new ArrayList<>( 10 );
        }


        public String getFileName() {
            return fileName;
        }


        void setSuccessful( boolean success ) {
            this.successful = true;
        }


        public boolean isSuccessful() {
            return successful;
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
