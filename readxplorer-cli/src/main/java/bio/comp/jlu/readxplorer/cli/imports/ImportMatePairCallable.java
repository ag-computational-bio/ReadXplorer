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


import bio.comp.jlu.readxplorer.cli.imports.ImportMatePairCallable.ImportMatePairResults;
import bio.comp.jlu.readxplorer.cli.imports.ImportReferenceCallable.ImportReferenceResult;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readxplorer.parser.TrackJob;
import de.cebitec.readxplorer.parser.common.ParsedTrack;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.mappings.JokToBamDirectParser;
import de.cebitec.readxplorer.parser.mappings.MappingParserI;
import de.cebitec.readxplorer.parser.mappings.SamBamParser;
import de.cebitec.readxplorer.parser.mappings.SamBamStatsParser;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.StatsContainer;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
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
 * The <code>ImportReferenceArgsProcessor</code> class is responsible for the
 * import of a reference genome in the cli version.
 *
 * The following options are available:
 * <p>
 * Mandatory:
 * <li>
 * <lu>-r / --ref-import</lu>
 * <lu>-d / --db: file to H2 database</lu>
 * <lu>-t / --file-type: sequence file type</lu>
 * <lu>-f / --files: reference genome files</lu>
 * <lu>-n / --names: reference genome names</lu>
 * <lu>-d / --descriptions: reference genome descriptions</lu>
 * </li>
 *
 * Optional:
 * -v / --verbose: print information during import process
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public final class ImportMatePairCallable implements Callable<ImportMatePairResults> {

    private static final Logger LOG = Logger.getLogger(ImportMatePairCallable.class.getName() );

    private final ImportReferenceResult  referenceResult;
    private final File trackAFile;
    private final File trackBFile;




    public ImportMatePairCallable( ImportReferenceResult referenceResult, File trackAFile, File trackBFile ) {

        this.referenceResult = referenceResult;
        this.trackAFile = trackAFile;
        this.trackBFile = trackBFile;

    }




    @Override
    public ImportMatePairResults call() throws CommandException {

        try {

            // create necessary (mockup) objects
            final ImportMatePairResults result = new ImportMatePairResults();
            final ProjectConnector pc = ProjectConnector.getInstance();
            final MappingParserI mappingParser = selectParser( trackAFile.getName().substring( trackAFile.getName().lastIndexOf( '.' ) ) );
            final Map<String, Integer> chromLengthMap = new HashMap<>();
            Map<Integer, PersistentChromosome> chromIdMap = pc.getRefGenomeConnector( referenceResult.getParsedReference().getID() ).getRefGenome().getChromosomes();
            for( PersistentChromosome chrom : chromIdMap.values() ) {
                chromLengthMap.put( chrom.getName(), chrom.getLength() );
            }
            final StatsContainer statsContainer = new StatsContainer();
                statsContainer.prepareForTrack();
            final TrackJob trackJob = new TrackJob( pc.getLatestTrackId(), trackAFile,
                                              trackAFile.getName(),
                                              referenceResult.getReferenceJob(), // check if this is ok
                                              mappingParser,
                                              false,
                                              new Timestamp( System.currentTimeMillis() ) );


            // parse track file
            LOG.log( Level.FINE, "parse track file: {0}...", trackAFile.getName() );
            trackAFile.setReadOnly(); //prevents changes or deletion of original file!
            //executes any conversion before other calculations, if the parser supports any
            boolean success = mappingParser.convert( trackJob, chromLengthMap );
            File lastWorkFile = trackJob.getFile();

            //generate classification data in file sorted by read sequence
            mappingParser.setStatsContainer( statsContainer );
            mappingParser.parseInput( trackJob, chromLengthMap );
            if( success ) {
                GeneralUtils.deleteOldWorkFile( lastWorkFile );
            } //only when we reach this line without exceptions and conversion was successful
            trackAFile.setWritable( true );

            //file needs to be sorted by coordinate for efficient calculation
            SamBamStatsParser statsParser = new SamBamStatsParser();
                statsParser.setStatsContainer( statsContainer );
            ParsedTrack track = statsParser.createTrackStats( trackJob, chromLengthMap );
            result.setParsedTrack( track );

            LOG.log( Level.FINE, "parsed track file: {0}", trackAFile.getName() );
            result.addOutput( "parsed track file " + trackAFile.getName() );

            return result;

        }
        catch( ParsingException | IOException | OutOfMemoryError ex ) {
            LOG.log( Level.SEVERE, null, ex );
            CommandException ce = new CommandException( 1, "import failed!" );
                ce.initCause( ex );
            throw ce;
        }

    }


    private static MappingParserI selectParser( String fileTypeArg ) {

        switch( fileTypeArg.toLowerCase() ) {
            case "out":
            case "jok":
                return new JokToBamDirectParser();
            case "sam":
            case "bam":
            default:
                return new SamBamParser();
        }

    }




    public class ImportMatePairResults {

        private final List<String> output;
        private ParsedTrack pt;


        ImportMatePairResults() {
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
