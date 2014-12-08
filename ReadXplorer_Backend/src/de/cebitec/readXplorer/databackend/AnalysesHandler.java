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

package de.cebitec.readXplorer.databackend;


import de.cebitec.readXplorer.api.objects.JobI;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.DataVisualisationI;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Pair;
import de.cebitec.readXplorer.util.Properties;
import java.util.ArrayList;
import java.util.Map;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;


/**
 * Class for handling the data threads for one of the currently started
 * analyses. CAUTION: You cannot query coverage and mapping tables with the same
 * AnalysisHandler at the same time. You have to use two separate handlers.
 */
public class AnalysesHandler implements ThreadListener, Observable, JobI {

    public static final String DATA_TYPE_COVERAGE = "Coverage";
    public static final String DATA_TYPE_MAPPINGS = "Mappings";
    /**
     * Returns 1 and means that all coverage querries are finished.
     */
    public static final byte COVERAGE_QUERRIES_FINISHED = 1;
    /**
     * Returns 2 and means that all mapping querries are finished.
     */
    public static final byte MAPPING_QUERRIES_FINISHED = 2;
    private final ProgressHandle progressHandle;
    private final DataVisualisationI parent;
    private final TrackConnector trackConnector;
    private int nbCovRequests;
    private int nbMappingRequests;
    private int nbRequests;
    private int nbCarriedOutRequests;
    private String queryType;
    private final ArrayList<Observer> observers;
    private boolean diffsAndGapsNeeded;
    private boolean coverageNeeded;
    private boolean mappingsNeeded;
    private byte desiredData = Properties.NORMAL;
    private final ParametersReadClasses readClassParams;
    private long start;


    /**
     * Creates a new analysis handler, ready for extracting mapping and coverage
     * information for the given track. CAUTION: You cannot query coverage and
     * mapping tables with the same AnalysisHandler at the same time. You have
     * to use two separate handlers.
     * <p>
     * @param trackConnector  the track connector for which the analyses are
     *                        carried out
     * @param parent          the parent for visualization of the results
     * @param handlerTitle    title of the analysis handler
     * @param readClassParams The parameter set which contains all parameters
     *                        concerning the usage of ReadXplorer's coverage classes and if only
     *                        uniquely
     *                        mapped reads shall be used, or all reads.
     */
    public AnalysesHandler( TrackConnector trackConnector, DataVisualisationI parent,
                            String handlerTitle, ParametersReadClasses readClassParams ) {
        this.progressHandle = ProgressHandleFactory.createHandle( handlerTitle );
        this.observers = new ArrayList<>();
        this.parent = parent;
        this.trackConnector = trackConnector;
        this.diffsAndGapsNeeded = false;
        this.coverageNeeded = false;
        this.mappingsNeeded = false;
        this.nbCovRequests = 0;
        this.nbMappingRequests = 0;
        this.readClassParams = readClassParams;
    }


    /**
     * Needs to be called in order to start the transcription analyses. Creates
     * the needed database requests and carries them out. The parent has to be a
     * ThreadListener in order to receive the coverage or mapping data.
     * Afterwards the results are returned to the observers of this analyses
     * handler by the {@link receiveData()} method.
     */
    public void startAnalysis() {

        this.queryType = this.coverageNeeded ? DATA_TYPE_COVERAGE : DATA_TYPE_MAPPINGS;
        this.nbRequests = 0;
        this.progressHandle.start();
        this.start = System.currentTimeMillis();
        Map<Integer, PersistentChromosome> chroms = trackConnector.getRefGenome().getChromosomes();

        if( this.coverageNeeded ) {

            //decide upon stepSize of a single request and analyse coverage of whole genome
            final int stepSize = 200000;

            for( PersistentChromosome chrom : chroms.values() ) {

                int chromLength = chrom.getLength();
                int from = 1;
                int to = this.calcRightBoundary( chromLength, stepSize, COVERAGE_QUERRIES_FINISHED );

                while( to < chromLength ) {
                    trackConnector.addCoverageAnalysisRequest( new IntervalRequest( from, to, chrom.getId(), this, diffsAndGapsNeeded, desiredData, readClassParams ) );

                    from = to + 1;
                    to += stepSize;
                }

                //calc last interval until genomeSize
                to = chromLength;
                trackConnector.addCoverageAnalysisRequest( new IntervalRequest( from, to, chrom.getId(), this, diffsAndGapsNeeded, desiredData, readClassParams ) );
            }


        }
        else if( this.mappingsNeeded ) {

            int stepSize = 150000;
            for( PersistentChromosome chrom : chroms.values() ) {

                int chromLength = chrom.getLength();
                int from = 1;
                int to = this.calcRightBoundary( chromLength, stepSize, MAPPING_QUERRIES_FINISHED );
                while( to < chromLength ) {
                    trackConnector.addMappingAnalysisRequest( new IntervalRequest( from, to, chrom.getId(), this, false, desiredData, readClassParams ) );
                    from = to + 1;
                    to += stepSize;
                }

                //calc last interval until genomeSize
                to = chromLength;
                trackConnector.addMappingAnalysisRequest( new IntervalRequest( from, to, chrom.getId(), this, false, desiredData, readClassParams ) );
            }
        }
        else {
            this.progressHandle.finish();
        }
    }


    /**
     * ThreadListener method for updating information in this analysis handler.
     * Updates of GUI componentes are delegated to the Swing dispatch thread.
     * <p>
     * @param data the data to add to this analysis handler
     */
    @Override
    public void receiveData( Object data ) {
        long finish = System.currentTimeMillis();
        String benchmark = Benchmark.calculateDuration( start, finish, ". Elapsed time: " );
        this.progressHandle.progress( this.queryType + " request "
                                      + (nbCarriedOutRequests + 1) + " of " + nbRequests + benchmark, ++nbCarriedOutRequests );
        this.notifyObservers( data );

        //when the last request is finished signalize the parent to collect the data
        if( this.nbCarriedOutRequests >= this.nbRequests ) {
            this.progressHandle.finish();
            this.parent.showData( new Pair<>( trackConnector.getTrackID(), this.queryType ) );
            this.nbCarriedOutRequests = 0;
        }
    }


    /**
     * @return True, if the analysis works with coverage, false otherwise.
     */
    public boolean isCoverageNeeded() {
        return this.coverageNeeded;
    }


    /**
     * Set before an anylsis is is started. True, if the analysis works with
     * coverage, false otherwise. By default it is false.
     *
     * @param coverageNeeded True, if the analysis works with coverage, false
     *                       otherwise.
     */
    public void setCoverageNeeded( boolean coverageNeeded ) {
        this.coverageNeeded = coverageNeeded;
    }


    /**
     * @return True, if the analysis works with mappings, false otherwise.
     */
    public boolean isMappingsNeeded() {
        return this.mappingsNeeded;
    }


    /**
     * Set before an anylsis is is started. True, if the analysis works with
     * mappings, false otherwise. By default it is false.
     * <p>
     * @param mappingsNeeded True, if the analysis works with mappings, false
     *                       otherwise.
     */
    public void setMappingsNeeded( boolean mappingsNeeded ) {
        this.mappingsNeeded = mappingsNeeded;
    }


    /**
     * @return True, if the analysis works with diffs and gaps, false otherwise.
     */
    public boolean isDiffsAndGapsNeeded() {
        return diffsAndGapsNeeded;
    }


    /**
     * Set before an anylsis is is started. True, if the analysis works with
     * diffs and gaps, false otherwise. By default it is false.
     * <p>
     * @param diffsAndGapsNeeded True, if the analysis works with diffs and
     *                           gaps, false
     *                           otherwise.
     */
    public void setDiffsAndGapsNeeded( boolean diffsAndGapsNeeded ) {
        this.diffsAndGapsNeeded = diffsAndGapsNeeded;
    }


    /**
     * Sets the desired data for this analysis handler to
     * Properties.REDUCED_MAPPINGS or back to Properties.NORMAL.
     * <p>
     * @param desiredData the byte value of the desired data. Currently among
     *                    Properties.REDUCED_MAPPINGS, Properties.NORMAL or Properties.READ_STARTS.
     */
    public void setDesiredData( byte desiredData ) {
        this.desiredData = desiredData;
    }


    @Override
    public int getNbCarriedOutRequests() {
        return this.nbCarriedOutRequests;
    }


    @Override
    public int getNbTotalRequests() {
        return this.nbRequests;
    }


    @Override
    public void registerObserver( Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
            if( this.nbCarriedOutRequests == this.nbCovRequests ) {
                observer.update( COVERAGE_QUERRIES_FINISHED );
            }
            else if( this.nbCarriedOutRequests == this.nbMappingRequests ) {
                observer.update( MAPPING_QUERRIES_FINISHED );
            }
        }
    }


    @Override
    public void notifySkipped() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }


    /**
     * Calculates the right interval boundary for a request, claculates the
     * number of needed total requests and updates the progress handle of this
     * analysis handler.
     * <p>
     * @param chromLength length of the chromosome under investigation
     * @param stepSize    size of each step
     * @param neededData  The data type. Determines, which requests are counted.
     * <p>
     * @return the right interval boundary for a request
     */
    private int calcRightBoundary( int chromLength, int stepSize, byte neededData ) {
        int to = chromLength > stepSize ? stepSize : chromLength;
        int additionalRequest = chromLength % stepSize == 0 ? 0 : 1;
        if( neededData == COVERAGE_QUERRIES_FINISHED ) {
            this.nbCovRequests += chromLength / stepSize + additionalRequest;
            this.nbRequests = this.nbCovRequests;
        }
        else if( neededData == MAPPING_QUERRIES_FINISHED ) {
            this.nbMappingRequests += chromLength / stepSize + additionalRequest;
            this.nbRequests = this.nbMappingRequests;
        }
        this.progressHandle.switchToDeterminate( this.nbRequests );
        this.progressHandle.progress( "Request " + (nbCarriedOutRequests + 1) + " of " + nbRequests, nbCarriedOutRequests );
        return to;
    }


}
