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
package de.compbio.readxplorer.classificationUpdate;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.StorageException;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.mappings.SamBamParser;
import de.cebitec.readXplorer.parser.mappings.SamBamStatsParser;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.StatsContainer;
import de.cebitec.readXplorer.util.classification.MappingClass;
import de.cebitec.readXplorer.util.classification.TotalCoverage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * A thread for updating the read mapping classification of ReadXplorer to
 * version 1.9.5 or later.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class UpdateThread extends SwingWorker<Object, Object> implements Observer {

    private InputOutput io;
    private ProgressHandle ph;
    private int workunits;
    private boolean noErrors = true;

    public UpdateThread() {
        this.io = IOProvider.getDefault().getIO("Read Classification Update", false);
        this.ph = ProgressHandleFactory.createHandle("Read Classification Update");
    }
    
    @Override
    protected Object doInBackground() throws Exception {
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();
        
        //get system JVM info:
        Runtime rt = Runtime.getRuntime();
         
        this.showMsg("Notification: Your current JVM config allows up to "+GeneralUtils.formatNumber(rt.maxMemory() / 1000000)+" MB of memory to be allocated.");
        this.showMsg("Currently the plattform is using "+GeneralUtils.formatNumber((rt.totalMemory() - rt.freeMemory()) / 1000000)+" MB of memory.");
        this.showMsg("Please be aware that you might need to change the -J-d64 and -J-Xmx value of your JVM to process large updates successfully.");
        this.showMsg("The value can be configured in the ../readXplorer/etc/readXplorer.conf file in the application folder."); 
        this.showMsg("");
        
        this.updateTracks();

        return null;
    }

    /**
     * If any message should be printed to the console, this method is used. If
     * an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
        this.io.getOut().println("\"" + msg);
    }

    private void updateTracks() {
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        List<TrackJob> trackJobs = this.getTrackJobs();
        if (!trackJobs.isEmpty()) {
            workunits = trackJobs.size();
            ph.start(workunits);
            workunits = 0;

            io.getOut().println("Starting update of read mapping classification for all tracks in the current database:");
            for (TrackJob trackJob : trackJobs) {
                try {
                    ph.progress("Re-calculate classification of track " + trackJob.getName(), workunits);
                    io.getOut().println("Re-calculate classification of track " + trackJob.getName());
                    SamBamParser parser = new SamBamParser();
                    parser.registerObserver(this);
                    Map<String, Integer> chromLengthMap = this.getChromLengthMap(trackJob);
                    StatsContainer statsContainer = new StatsContainer();
                    statsContainer.prepareForTrack();
                    parser.setStatsContainer(statsContainer);
                    Boolean success = parser.parseInput(trackJob, chromLengthMap);
                    parser.removeObserver(this);
                    if (success) {
                        try {
                            projectConnector.resetTrackPath(
                                    new PersistentTrack(
                                            trackJob.getID(), 
                                            trackJob.getFile().getAbsolutePath(), 
                                            trackJob.getDescription(), 
                                            trackJob.getTimestamp(), 
                                            trackJob.getRefGen().getID(), 
                                            1));
                            
                            //file needs to be sorted by coordinate for efficient calculation
                            SamBamStatsParser statsParser = new SamBamStatsParser();
                            statsParser.setStatsContainer(statsContainer);
                            statsParser.registerObserver(this);
                            statsParser.createTrackStats(trackJob, chromLengthMap);
                            statsParser.removeObserver(this);
                            
                            List<String> statsKeysToDelete = new ArrayList<>();
                            for (MappingClass mappingClass : MappingClass.values()) {
                                statsKeysToDelete.add(mappingClass.getTypeString());
                                statsKeysToDelete.add(mappingClass.getTypeString() + StatsContainer.COVERAGE_STRING);
                            }
                            statsKeysToDelete.add(TotalCoverage.TOTAL_COVERAGE.getTypeString() + StatsContainer.COVERAGE_STRING);
                            statsKeysToDelete.add(StatsContainer.NO_MAPPINGS);
                            statsKeysToDelete.add(StatsContainer.NO_UNIQUE_SEQS);
                            statsKeysToDelete.add(StatsContainer.NO_REPEATED_SEQ);
                            statsKeysToDelete.add(StatsContainer.NO_UNIQ_MAPPINGS);
                            statsKeysToDelete.add(StatsContainer.NO_READS);
                            statsKeysToDelete.add(StatsContainer.AVERAGE_READ_LENGTH);

                            projectConnector.deleteSpecificTrackStatistics(statsKeysToDelete, trackJob.getID());
                            projectConnector.storeTrackStatistics(statsContainer, trackJob.getID());
                        } catch (StorageException ex) {
                            Exceptions.printStackTrace(ex);
                            noErrors = false;
                        }
                    }
                } catch (ParsingException ex) {
                    Exceptions.printStackTrace(ex);
                    noErrors = false;
                } catch (OutOfMemoryError ex) {
                    Exceptions.printStackTrace(ex);
                    noErrors = false;
                }
                if (noErrors) {
                    io.getOut().println("Re-calculation of classification for track " + trackJob.getName() + " finished successfully!");
                } else {
                    io.getOut().println("Re-calculation of classification for track " + trackJob.getName() + " failed!");
                }
                ph.progress("Re-calculate classification of track " + trackJob.getName(), ++workunits);
            }
        }
    }
    
    private List<TrackJob> getTrackJobs() {
        ProjectConnector projectConnector = ProjectConnector.getInstance();
        List<PersistentTrack> tracks = projectConnector.getTracks();
        List<TrackJob> trackJobs = new ArrayList<>();
        Map<Integer, ReferenceJob> idToRefMap = new HashMap<>();

        List<PersistentReference> refs = ProjectConnector.getInstance().getGenomes();
        for (PersistentReference ref : refs) {
            // File and parser parameter meaningless in this context
            ReferenceJob refJob = new ReferenceJob(ref.getId(), ref.getFastaFile(), null, ref.getDescription(), ref.getName(), ref.getTimeStamp());
            idToRefMap.put(refJob.getID(), refJob);
        }

        for (PersistentTrack dbTrack : tracks) {
            // File and parser, refgenjob, runjob parameters meaningless in this context
            TrackJob trackJob = new TrackJob(dbTrack.getId(), new File(dbTrack.getFilePath()),
                    dbTrack.getDescription(), idToRefMap.get(dbTrack.getRefGenID()),
                    null, false, dbTrack.getTimestamp());

            trackJobs.add(trackJob);
        }
        return trackJobs;
    }
    
    /**
     * Reads all chromosome sequences of the reference genome and puts them into
     * the chromSeqMap map and their length in the chromLengthMap map.
     * @param trackJob The track job for which the chromosome sequences and
     * lengths are needed.
     */
    private Map<String, Integer> getChromLengthMap(TrackJob trackJob) {
        Map<String, Integer> chromLengthMap = new HashMap<>();
        int id = trackJob.getRefGen().getID();
        Map<Integer, PersistentChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefGenome().getChromosomes();
        for (PersistentChromosome chrom : chromIdMap.values()) {
            chromLengthMap.put(chrom.getName(), chrom.getLength());
        }
        return chromLengthMap;
    }
    
    @Override
    public void update(Object data) {
        if (data.toString().contains("processed") || data.toString().contains("converted") || data.toString().contains("indexed")) {
            this.ph.progress(data.toString());
        } else {
            this.showMsg(data.toString());
            this.ph.progress("");
        }
    }
    
    @Override
    protected void done() {
        super.done();
        ph.finish();
        if (this.noErrors) {
            io.getOut().println("Re-calculation of classification for all tracks finished successfully! (If no error messages were printed!)");
        } else {
            io.getOut().println("At least the update of one data set failed (check earlier log messages for more info)");
        }
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove(this);
    }
}
