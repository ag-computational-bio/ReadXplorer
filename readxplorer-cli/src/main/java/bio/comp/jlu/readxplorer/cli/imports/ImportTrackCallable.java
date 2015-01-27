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
import de.cebitec.readxplorer.databackend.dataObjects.PersistentChromosome;
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
 * 
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public final class ImportTrackCallable implements Callable<ImportTrackResults> {

    private static final Logger LOG = Logger.getLogger( ImportTrackCallable.class.getName() );

    private final ImportReferenceResult  referenceResult;
    private final TrackJob trackJob;




    public ImportTrackCallable( ImportReferenceResult referenceResult, TrackJob trackJob ) {

        this.referenceResult = referenceResult;
        this.trackJob = trackJob;

    }




    @Override
    public ImportTrackResults call() throws CommandException {

        try {

            // create necessary objects
            final File trackFile = trackJob.getFile();
            final MappingParserI mappingParser = trackJob.getParser();
            final Map<String, Integer> chromLengthMap = new HashMap<>();
            Map<Integer, PersistentChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector( referenceResult.getParsedReference().getID() ).getRefGenome().getChromosomes();
            for( PersistentChromosome chrom : chromIdMap.values() ) {
                chromLengthMap.put( chrom.getName(), chrom.getLength() );
            }
            final StatsContainer statsContainer = new StatsContainer();
                statsContainer.prepareForTrack();


            // parse track file
            LOG.log( Level.FINE, "parse track file: {0}...", trackFile.getName() );
            trackFile.setReadOnly(); //prevents changes or deletion of original file!
            //executes any conversion before other calculations, if the parser supports any
            boolean success = mappingParser.convert( trackJob, chromLengthMap );
            File lastWorkFile = trackJob.getFile();


            //generate classification data in file sorted by read sequence
            mappingParser.setStatsContainer( statsContainer );
            mappingParser.parseInput( trackJob, chromLengthMap );
            if( success ) {
                GeneralUtils.deleteOldWorkFile( lastWorkFile );
            } //only when we reach this line without exceptions and conversion was successful
            trackFile.setWritable( true );

            //file needs to be sorted by coordinate for efficient calculation
            SamBamStatsParser statsParser = new SamBamStatsParser();
                statsParser.setStatsContainer( statsContainer );
            ParsedTrack track = statsParser.createTrackStats( trackJob, chromLengthMap );

            LOG.log( Level.FINE, "parsed track file: {0}", trackFile.getName() );
            ImportTrackCallable.ImportTrackResults result = new ImportTrackCallable.ImportTrackResults();
            result.addOutput( "parsed track file " + trackFile.getName() );
            result.setParsedTrack( track );


            return result;

        }
        catch( ParsingException | IOException | OutOfMemoryError ex ) {
            LOG.log( Level.SEVERE, null, ex );
            CommandException ce = new CommandException( 1, "import failed!" );
                ce.initCause( ex );
            throw ce;
        }

    }




    public class ImportTrackResults {

        private final List<String> output;
        private ParsedTrack pt;


        ImportTrackResults() {
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
