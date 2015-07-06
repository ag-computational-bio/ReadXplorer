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

package bio.comp.jlu.readxplorer.cli.analyses;


import bio.comp.jlu.readxplorer.cli.filefilter.AnalysisFileFilter;
import de.cebitec.readxplorer.databackend.AnalysesHandler;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.tools.snpdetection.AnalysisSNPs;
import de.cebitec.readxplorer.tools.snpdetection.ParameterSetSNPs;
import de.cebitec.readxplorer.tools.snpdetection.SnpDetectionResult;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import jxl.write.WriteException;
import org.netbeans.api.sendopts.CommandException;

import static bio.comp.jlu.readxplorer.cli.analyses.CLIAnalyses.SNP;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;


/**
 * Analysis Logic for CLI SNP Analysis.
 * Performs parallelised SNP analysis calculations and result output.
 * <p>
 * For each imported track a single nucleotide polymorphism analysis will be performed.
 * Analysis preferences are read from set properties file or if not specified
 * from standard property file. After all calculations have been performed
 * results will be written to an output file.
 * <p>
 * After all analyses have been performed, common result files will be merged
 * into a single result file for each type of analysis.
 *
 * @author Oliver Schwengers <oliver.schwengers@computational.bio.uni-giessen.de
 */
public final class SNPAnalysisCallable extends AnalysisCallable {

    private static final Logger LOG = Logger.getLogger( SNPAnalysisCallable.class.getName() );

    private final PersistentTrack persistentTrack;
    private final ParameterSetSNPs parameterSet;


    /**
     * SNP Analysis Callable constructor.
     *
     * @param verbosity is verbosity required?
     * @param persistentTrack imported track to analyse
     * @param parameterSetTSS set with snp parameters
     */
    public SNPAnalysisCallable( boolean verbosity, PersistentTrack persistentTrack, ParameterSetSNPs parameterSet ) {

        super( verbosity, SNP );

        this.persistentTrack = persistentTrack;
        this.parameterSet = parameterSet;

    }


    @Override
    public AnalysisResult call() throws CommandException {

        try {

            File trackFile = new File( persistentTrack.getFilePath() );
            final String trackFileName = trackFile.getName();

            LOG.log( FINE, "start SNP analysis for {0}...", trackFileName );
            result.addOutput( "start analysis..." );
            final ProjectConnector pc = ProjectConnector.getInstance();
            final TrackConnector trackConnector = pc.getTrackConnector( persistentTrack );
            final AnalysisSNPs analysisSNPs = new AnalysisSNPs( trackConnector, parameterSet );
            final ThreadingHelper threadingHelper = new ThreadingHelper(); // tricky work-around due to MVC blindness of the RX code :-)
            threadingHelper.start();
            final AnalysesHandler analysisHandler = new AnalysesHandler( trackConnector, threadingHelper, "", parameterSet.getReadClassParams() );
                analysisHandler.registerObserver( analysisSNPs );
                analysisHandler.setCoverageNeeded( true );
                analysisHandler.setDiffsAndGapsNeeded( true );
                analysisHandler.startAnalysis();

            threadingHelper.join(); // blocks until analysisHandler finishes its job
            Map<Integer, PersistentTrack> trackMap = new HashMap<>();
            trackMap.put( persistentTrack.getId(), persistentTrack );
            PersistentReference reference = pc.getRefGenomeConnector( persistentTrack.getRefGenID() ).getRefGenome();
            final SnpDetectionResult snpDetectionResult = new SnpDetectionResult( analysisSNPs.getResults(),
                                                                                  trackMap, reference, false, 2, 0 );
            snpDetectionResult.setParameters( parameterSet );


            LOG.log( FINE, "store SNP results for {0}...", trackFileName );
            result.addOutput( "store results..." );
            File resultFile = new File( "snp-" + trackFileName + '.' + AnalysisFileFilter.SUFFIX );
            writeFile( resultFile, snpDetectionResult.dataSheetNames(), snpDetectionResult.dataColumnDescriptions(), snpDetectionResult.dataToExcelExportList() );

            result.setResultFile( resultFile );

        } catch( IOException | WriteException | InterruptedException ex ) {
            LOG.log( SEVERE, ex.getMessage(), ex );
            result.addOutput( "Error: " + ex.getMessage() );
        } catch( OutOfMemoryError ome ) {
            LOG.log( SEVERE, ome.getMessage(), ome );
            CommandException ce = new CommandException( 1, "ran out of memory!" );
            ce.initCause( ome );
            throw ce;
        }

        return result;

    }


}
