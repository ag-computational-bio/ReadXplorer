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


import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.StorageException;
import de.cebitec.readxplorer.parser.ReferenceJob;
import de.cebitec.readxplorer.parser.common.ParsedReference;
import de.cebitec.readxplorer.parser.common.ParsingException;
import de.cebitec.readxplorer.parser.reference.BioJavaGff2Parser;
import de.cebitec.readxplorer.parser.reference.BioJavaGff3Parser;
import de.cebitec.readxplorer.parser.reference.BioJavaParser;
import de.cebitec.readxplorer.parser.reference.FastaReferenceParser;
import de.cebitec.readxplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readxplorer.parser.reference.Filter.FilterRuleSource;
import de.cebitec.readxplorer.parser.reference.ReferenceParserI;
import de.cebitec.readxplorer.utils.Benchmark;
import de.cebitec.readxplorer.utils.Properties;
import java.io.File;
import java.io.PrintStream;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.sendopts.CommandException;
import org.netbeans.spi.sendopts.Arg;
import org.netbeans.spi.sendopts.ArgsProcessor;
import org.netbeans.spi.sendopts.Description;
import org.netbeans.spi.sendopts.Env;
import org.openide.util.NbBundle;


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
public final class ImportTrackArgsProcessor implements ArgsProcessor {

    private static final Logger LOG = Logger.getLogger( ImportTrackArgsProcessor.class.getName() );


    @Arg( shortName = 'v', longName = "verbose" )
    @Description( displayName = "Verbose", shortDescription = "The H2 database file which should be connected to." )
    public boolean verboseArg;

    @Arg( shortName = 't', longName = "track-import" )
    @Description( displayName = "Track Import", shortDescription = "Import a track." )
    public boolean importTrackArg;

    @Arg( shortName = 'd', longName = "db" )
    @Description( displayName = "Database", shortDescription = "The H2 database file which should be connected to." )
    public String dbFileArg;

    @Arg( shortName = 't', longName = "file-type" )
    @Description( displayName = "File type", shortDescription = "The sequence file type. One of (embl|genbank|gff3|gff2|gtf|fasta)" )
    public String fileTypeArg;

    @Arg( shortName = 'f', longName = "files" )
    @Description( displayName = "Reference Genome Files", shortDescription = "The path to the reference files." )
    public String[] referenceFileArgs;

    @Arg( shortName = 'n', longName = "names" )
    @Description( displayName = "Reference Names", shortDescription = "The names of the sequence files." )
    public String[] nameArgs;

    @Arg( longName = "description" )
    @Description( displayName = "Description", shortDescription = "The description of the sequence files." )
    public String[] descriptionArgs;


    @Override
    public void process( final Env env ) throws CommandException {

        if( nameArgs.length != referenceFileArgs.length ) {
            CommandException ce = new CommandException( 1, "number of name arguments differ from number of sequence files!" );
            throw ce;
        }

        if( descriptionArgs.length != referenceFileArgs.length ) {
            CommandException ce = new CommandException( 1, "number of description arguments differ from number of sequence files!" );
            throw ce;
        }


        final PrintStream ps = env.getOutputStream();
        try {

            ProjectConnector.getInstance().connect( Properties.ADAPTER_H2, dbFileArg, null, null, null );

            final int l = referenceFileArgs.length;
            final ReferenceParserI refParser = selectParser( fileTypeArg );
            for( int i=0; i<l; i++ ) {

                ReferenceJob rj = new ReferenceJob( 0, new File(referenceFileArgs[i]), refParser,
                    descriptionArgs[i], nameArgs[i], new Timestamp( System.currentTimeMillis() ) );

                importRefGenome( rj, ps );

            }

            ProjectConnector.getInstance().disconnect();

        }
        catch( SQLException ex ) {

            CommandException ce = new CommandException( 1 );
                ce.initCause( ex );
            throw ce;

        }

    }


    /**
     * Processes reference genome job.
     */
    private void importRefGenome( final ReferenceJob rj, final PrintStream ps ) throws CommandException {

        ps.println( NbBundle.getMessage( ImportTrackArgsProcessor.class, "MSG_ImportThread.import.start.ref" ) + ":" );
        final long start = System.currentTimeMillis();
        try {

            // parse reference genome
            LOG.log( Level.INFO, "Start parsing reference genome from source \"{0}\"", rj.getFile().getAbsolutePath() );
            ReferenceParserI parser = rj.getParser();
            FeatureFilter filter = new FeatureFilter();
            filter.addBlacklistRule( new FilterRuleSource() );
            ParsedReference refGenome = parser.parseReference( rj, filter );
            LOG.log( Level.INFO, "Finished parsing reference genome from source \"{0}\"", rj.getFile().getAbsolutePath() );
            ps.println( "\"" + rj.getName() + "\" " + NbBundle.getMessage( ImportTrackArgsProcessor.class, "MSG_ImportThread.import.parsed" ) );


            // stores reference sequence in the db
            LOG.log( Level.INFO, "Start storing reference genome from source \"{0}\"", rj.getFile().getAbsolutePath() );
            int refGenID = ProjectConnector.getInstance().addRefGenome( refGenome );
            rj.setPersistent( refGenID );
            LOG.log( Level.INFO, "Finished storing reference genome from source \"{0}\"", rj.getFile().getAbsolutePath() );


            // print benchmarks
            if( verboseArg ) {
                final long finish = System.currentTimeMillis();
                String msg = "\"" + rj.getName() + "\" " + NbBundle.getMessage( ImportTrackArgsProcessor.class, "MSG_ImportThread.import.stored" );
                ps.println( Benchmark.calculateDuration( start, finish, msg ) );
            }

        }
        catch( ParsingException | StorageException ex ) {
            LOG.log( Level.SEVERE, null, ex );
//            ps.println( "\"" + rj.getName() + "\" " + NbBundle.getMessage( ImportThread.class, "MSG_ImportThread.import.failed" ) + "!" );
            CommandException ce = new CommandException( 1, "\"" + rj.getName() + "\" " + NbBundle.getMessage( ImportTrackArgsProcessor.class, "MSG_ImportThread.import.failed" ) );
                ce.initCause( ex );
            throw ce;
        }
        catch( OutOfMemoryError ex ) {
            LOG.log( Level.SEVERE, null, ex );
//            ps.println( "\"" + rj.getName() + "\" " + NbBundle.getMessage( ImportThread.class, "MSG_ImportThread.import.outOfMemory" ) + "!" );
            CommandException ce = new CommandException( 1, "\"" + rj.getName() + "\" " + NbBundle.getMessage( ImportTrackArgsProcessor.class, "MSG_ImportThread.import.outOfMemory" ) );
                ce.initCause( ex );
            throw ce;
        }

    }


    private static ReferenceParserI selectParser( String fileTypeArg ) {

        switch( fileTypeArg ) {
            case "embl":
                return new BioJavaParser( BioJavaParser.EMBL );
            case "genbank":
                return new BioJavaParser( BioJavaParser.GENBANK );
            case "gff3":
                return new BioJavaGff3Parser();
            case "gff2":
            case "gtf":
                return new BioJavaGff2Parser();
            case "fasta":
            default:
                return new FastaReferenceParser();
        }

    }

}
