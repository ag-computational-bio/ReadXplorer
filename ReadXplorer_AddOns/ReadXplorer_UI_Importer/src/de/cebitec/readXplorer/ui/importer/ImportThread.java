/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.ui.importer;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.StorageException;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.parser.ReadPairJobContainer;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.TrackJob;
import de.cebitec.readXplorer.parser.common.ParsedClassification;
import de.cebitec.readXplorer.parser.common.ParsedReference;
import de.cebitec.readXplorer.parser.common.ParsedTrack;
import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.mappings.MappingParserI;
import de.cebitec.readXplorer.parser.mappings.SamBamStatsParser;
import de.cebitec.readXplorer.parser.output.SamBamCombiner;
import de.cebitec.readXplorer.parser.output.SamBamExtender;
import de.cebitec.readXplorer.parser.reference.Filter.FeatureFilter;
import de.cebitec.readXplorer.parser.reference.Filter.FilterRuleSource;
import de.cebitec.readXplorer.parser.reference.ReferenceParserI;
import de.cebitec.readXplorer.readPairClassifier.SamBamReadPairClassifier;
import de.cebitec.readXplorer.readPairClassifier.SamBamReadPairStatsParser;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.StatsContainer;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingWorker;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * THE thread in ReadXplorer for handling the import of data.
 * 
 * @author ddoppmeier, rhilker
 */
public class ImportThread extends SwingWorker<Object, Object> implements Observer {

    private InputOutput io;
    private List<ReferenceJob> referenceJobs;
    private List<TrackJob> tracksJobs;
    private List<ReadPairJobContainer> readPairJobs;
    private ProgressHandle ph;
    private int workunits;
    private boolean noErrors = true;
    private Map<String, Integer> chromLengthMap;

    /**
     * THE thread in ReadXplorer for handling the import of data.
     * @param refJobs reference jobs to import
     * @param trackJobs track jobs to import
     * @param readPairJobs read pair jobs to import
     */
    public ImportThread(List<ReferenceJob> refJobs, List<TrackJob> trackJobs, List<ReadPairJobContainer> readPairJobs) {
        super();
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ImportThread.class, "ImportThread.output.name"), false);
        this.tracksJobs = trackJobs;
        this.referenceJobs = refJobs;
        this.readPairJobs = readPairJobs;
        this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.name"));
        
        this.workunits = refJobs.size();
        for (TrackJob trackJob : trackJobs) {
            workunits += trackJob.isAlreadyImported() ? 1 : 2; //one or two steps are needed
        }
        for (ReadPairJobContainer readPairJob : readPairJobs) {
            workunits += readPairJob.getTrackJob1().isAlreadyImported() ? 1 : 3; //one or three steps are needed
        }
    }

    private ParsedReference parseRefJob(ReferenceJob refGenJob) throws ParsingException, OutOfMemoryError {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        ReferenceParserI parser = refGenJob.getParser();
        parser.registerObserver(this);
        FeatureFilter filter = new FeatureFilter();
        filter.addBlacklistRule(new FilterRuleSource());
        ParsedReference refGenome = parser.parseReference(refGenJob, filter);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished parsing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
        return refGenome;
    }

    /**
     * Stores a reference sequence in the DB.
     * @param refGenome the reference sequence to store
     * @param refGenJob the corresponding reference job, whose id will be updated
     * @throws StorageException 
     */
    private void storeRefGenome(ParsedReference refGenome, ReferenceJob refGenJob) throws StorageException {
        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Start storing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());

        int refGenID = ProjectConnector.getInstance().addRefGenome(refGenome);
        refGenJob.setPersistant(refGenID);

        Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, "Finished storing reference genome from source \"{0}\"", refGenJob.getFile().getAbsolutePath());
    }

    /**
     * Processes all reference genome jobs of this import process.
     */
    private void processRefGenomeJobs() {
        if (!referenceJobs.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.ref") + ":");
            long start;
            long finish;
            String msg;
            
            for (Iterator<ReferenceJob> it = referenceJobs.iterator(); it.hasNext();) {
                start = System.currentTimeMillis();
                ReferenceJob r = it.next();
                ph.progress(workunits++);

                try {
                    // parsing
                    ParsedReference refGen = this.parseRefJob(r);
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.parsed"));

                    // storing
                    try {
                        storeRefGenome(refGen, r);
                        finish = System.currentTimeMillis();
                        msg = "\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.stored");
                        io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
                    } catch (StorageException ex) {
                        // if something went wrong, mark all dependent track jobs
                        io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                        this.noErrors = false;
                        Logger.getLogger(ImportThread.class.getName()).log(Level.SEVERE, null, ex);
                    }

                } catch (ParsingException ex) {
                    // if something went wrong, mark all dependent track jobs
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.failed") + "!");
                    this.noErrors = false;
                    Logger.getLogger(ImportThread.class.getName()).log(Level.INFO, null, ex);
                } catch (OutOfMemoryError ex) {
                    io.getOut().println("\"" + r.getName() + "\" " + NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.outOfMemory") + "!");
                }

                it.remove();
            }

            io.getOut().println("");
        }
    }

    /**
     * Reads all chromosome sequences of the reference genome and puts them
     * into the chromSeqMap map and their length in the chromLengthMap map. 
     * @param trackJob The track job for which the chromosome sequences and
     * lengths are needed.
     */
    private void setChromLengthMap(TrackJob trackJob) {
        chromLengthMap = new HashMap<>();
        int id = trackJob.getRefGen().getID();
        Map<Integer, PersistantChromosome> chromIdMap = ProjectConnector.getInstance().getRefGenomeConnector(id).getRefGenome().getChromosomes();
        for (PersistantChromosome chrom : chromIdMap.values()) {
            chromLengthMap.put(chrom.getName(), chrom.getLength());
        }
    }
    
    /**
     * Processes track jobs (parsing and storing) of the current import.
     */
    private void processTrackJobs() {
        if (!tracksJobs.isEmpty()) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.track") + ":");
            
            for (Iterator<TrackJob> it = tracksJobs.iterator(); it.hasNext();) {
                TrackJob trackJob = it.next();

                this.parseBamTrack(trackJob);

                ph.progress(workunits++);
                it.remove();
            }
        }
    }
    
    private void processReadPairJobs() {
        if (!readPairJobs.isEmpty()) {

            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.readPairs") + ":");

            long start;
            long finish;
            String msg;

            for (Iterator<ReadPairJobContainer> it = readPairJobs.iterator(); it.hasNext();) {
                start = System.currentTimeMillis();
                ReadPairJobContainer readPairJobContainer = it.next();

                int distance = readPairJobContainer.getDistance();
                if (distance > 0) {

                    int trackId1;
                    int trackId2 = -1;

                        /*
                         * Algorithm:
                         * start file
                         * if (track not yet imported) {
                         *      convert file 1 to sam/bam, if necessary
                         *      if (isTwoTracks) { 
                         *          convert file 2 to sam/bam, if necessary
                         *          combine them unsorted (NEW FILE) 
                         *      }
                         *      sort by readseq (NEW FILE) - if isTwoTracks: deleteOldFile
                         *      parse mappings 
                         *      sort by read name (NEW FILE) - deleteOldFile
                         *      read pair classification, extension & sorting by coordinate - deleteOldFile
                         * }
                         * create position table (advantage: is already sorted by coordinate & classification in file)
                         */

                        TrackJob trackJob1 = readPairJobContainer.getTrackJob1();
                        TrackJob trackJob2 = readPairJobContainer.getTrackJob2();
                        Map<String, ParsedClassification> classificationMap;
                        this.setChromLengthMap(trackJob1);
                        File inputFile1 = trackJob1.getFile();
                        inputFile1.setReadOnly(); //prevents changes or deletion of original file!
                        boolean success;
                        StatsContainer statsContainer = new StatsContainer();
                        statsContainer.prepareForTrack();
                        statsContainer.prepareForReadPairTrack();

                        if (!trackJob1.isAlreadyImported()) {

                            try {
                                //executes any conversion before other calculations, if the parser supports any
                                trackJob1.getParser().registerObserver(this);
                                success = (boolean) trackJob1.getParser().convert(trackJob1, chromLengthMap);
                                trackJob1.getParser().removeObserver(this);
                                if (!success) {
                                    this.noErrors = false;
                                    this.showMsg("Conversion of " + trackJob1.getName() + " failed!");
                                    continue;
                                }
                                File lastWorkFile = trackJob1.getFile(); //file which was created in the last step of the import process
                                
                                boolean isTwoTracks = trackJob2 != null;
                                if (isTwoTracks) { //only combine, if data is not already combined
                                    File inputFile2 = trackJob2.getFile();
                                    inputFile2.setReadOnly();
                                    trackJob2.getParser().registerObserver(this);
                                    success = (boolean) trackJob2.getParser().convert(trackJob2, chromLengthMap);
                                    trackJob2.getParser().removeObserver(this);
                                    File lastWorkFile2 = trackJob2.getFile();
                                    if (!success) {
                                        this.noErrors = false;
                                        this.showMsg("Conversion of " + trackJob2.getName() + " failed!");
                                        continue;
                                    }

                                    //combine both tracks and continue with trackJob1, they are unsorted now
                                    SamBamCombiner combiner = new SamBamCombiner(trackJob1, trackJob2, false);
                                    combiner.registerObserver(this);
                                    success = combiner.combineData();
                                    if (!success) {
                                        this.noErrors = false;
                                        this.showMsg("Combination of " + trackJob1.getName() + " and " + trackJob2.getName() + " failed!");
                                        continue;
                                    }
                                    GeneralUtils.deleteOldWorkFile(lastWorkFile); //either were converted or are write protected
                                    GeneralUtils.deleteOldWorkFile(lastWorkFile2);
                                    lastWorkFile = trackJob1.getFile(); //the combined file
                                    inputFile2.setWritable(true);
                                }

                                //extension for both classification and read pair info
                                SamBamReadPairClassifier samBamDirectReadPairClassifier = new SamBamReadPairClassifier(
                                        readPairJobContainer, chromLengthMap);
                                samBamDirectReadPairClassifier.registerObserver(this);
                                samBamDirectReadPairClassifier.setStatsContainer(statsContainer);
                                samBamDirectReadPairClassifier.classifyReadPairs();
                                
                                //delete the combined file, if it was combined, otherwise the orig. file cannot be deleted
                                GeneralUtils.deleteOldWorkFile(lastWorkFile);

                            } catch (OutOfMemoryError ex) {
                                this.showMsg("Out of Memory error during parsing of bam track: " + ex.getMessage());
                                this.noErrors = false;
                                continue;
                            } catch (Exception ex) {
                                this.showMsg("Error during parsing of bam track: " + ex.getMessage());
                                Exceptions.printStackTrace(ex);
                                this.noErrors = false;
                                continue;
                            }
                        } else { //else case with 2 already imported tracks is prohibited
                            //we have to calculate the stats
                            ph.progress(workunits++);
                            SamBamReadPairStatsParser statsParser = new SamBamReadPairStatsParser(readPairJobContainer, chromLengthMap, null);
                            statsParser.setStatsContainer(statsContainer);
                            try {
                                statsParser.registerObserver(this);
                                statsParser.classifyReadPairs();
                            } catch (OutOfMemoryError ex) {
                                this.showMsg("Out of Memory error during parsing of bam track: " + ex.getMessage());
                                this.noErrors = false;
                                continue;
                            } catch (Exception ex) {
                                this.showMsg("Error during parsing of bam track: " + ex.getMessage());
                                Exceptions.printStackTrace(ex);
                                this.noErrors = false;
                                continue;
                            }
                        }

                        ph.progress(workunits++);
                        //create general track stats
                        SamBamStatsParser statsParser = new SamBamStatsParser();
                        statsParser.setStatsContainer(statsContainer);
                        statsParser.registerObserver(this);
                        ParsedTrack track = statsParser.createTrackStats(trackJob1, chromLengthMap);
                        statsParser.removeObserver(this);

                        this.storeBamTrack(track, true); // store track entry in db
                        trackId1 = trackJob1.getID();
                        inputFile1.setWritable(true);
//                    }

                    //read pair ids have to be set in track entry
                    ProjectConnector.getInstance().setReadPairIdsForTrackIds(trackId1, trackId2);

                } else { //if (distance <= 0)
                    this.showMsg(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.error"));
                    this.noErrors = false;
                }

                ph.progress(workunits++);
                it.remove();
            }
        }
    }
    
    /**
     * Parses a bam track and calls the method for storing the
     * track relevant data in the db.
     * @param trackJob the trackjob to import as bam track
     */
    private void parseBamTrack(TrackJob trackJob) {
        
        /*
         * Algorithm:
         * if (track not yet imported) {
         *      convert to sam/bam, if necessary (NEW FILE)
         *      parse mappings 
         *      extend bam file (NEW FILE) - deleteOldFile
         * }
         * create statistics (advantage: is already sorted by coordinate & classification in file)
         */   

        this.setChromLengthMap(trackJob);
        boolean success;
        StatsContainer statsContainer = new StatsContainer();
        statsContainer.prepareForTrack();
        
        //only extend, if data is not already stored in it
        if (!trackJob.isAlreadyImported()) {
            File inputFile = trackJob.getFile();
            MappingParserI mappingParser = trackJob.getParser();
            inputFile.setReadOnly(); //prevents changes or deletion of original file!
            try {
                //executes any conversion before other calculations, if the parser supports any
                success = (boolean) trackJob.getParser().convert(trackJob, chromLengthMap);
                File lastWorkFile = trackJob.getFile();

                //generate classification data in file sorted by read sequence
                mappingParser.registerObserver(this);
                mappingParser.setStatsContainer(statsContainer);
                mappingParser.parseInput(trackJob, chromLengthMap);
                mappingParser.removeObserver(this);
                ph.progress(workunits++);
                noErrors = noErrors ? success : noErrors;
                if (success) { GeneralUtils.deleteOldWorkFile(lastWorkFile); } //only when we reach this line without exceptions and conversion was successful

            } catch (OutOfMemoryError ex) {
                this.showMsg("Out of memory error during parsing of bam track: " + ex.getMessage());
                this.noErrors = false;
                return;
            } catch (Exception ex) {
                this.showMsg("Error during parsing of bam track: " + ex.getMessage());
                Exceptions.printStackTrace(ex); //TODO: remove this error handling
                this.noErrors = false;
                return;
            }
            inputFile.setWritable(true);
            mappingParser.removeObserver(this);
        }

        //file needs to be sorted by coordinate for efficient calculation
        SamBamStatsParser statsParser = new SamBamStatsParser();
        statsParser.setStatsContainer(statsContainer);
        statsParser.registerObserver(this);
        ParsedTrack track = statsParser.createTrackStats(trackJob, chromLengthMap);
        statsParser.removeObserver(this);

        this.storeBamTrack(track, false);
    }

    @Override
    protected Object doInBackground() {
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();

        ph.start(workunits);
        workunits = 0;

        ph.progress(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.ref") + "...", workunits);
        this.processRefGenomeJobs();

        // track jobs have to be imported last, because they may depend upon previously imported genomes, runs
        ph.progress(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.progress.track") + "...", workunits);
        
        //get system JVM info:
        Runtime rt = Runtime.getRuntime();
         
        this.showMsg("Your current JVM config allows up to "+GeneralUtils.formatNumber(rt.maxMemory())+" bytes of memory to be allocated.");
        this.showMsg("Currently the plattform is using "+GeneralUtils.formatNumber(rt.totalMemory() - rt.freeMemory())+" bytes of memory.");
        this.showMsg("Please be aware that you might need to change the -J-d64 and -J-Xmx value of your JVM to process large imports successfully.");
        this.showMsg("The value can be configured in the ../readXplorer/etc/readXplorer.conf file in the application folder."); 
        this.showMsg("");
        
        this.processTrackJobs();
        this.processReadPairJobs();

        return null;
    }

    @Override
    protected void done() {
        super.done();
        ph.progress(workunits);
        if (this.noErrors) {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.finished"));
        } else {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.partFailed"));
        }
        io.getOut().close();
        ph.finish();

        CentralLookup.getDefault().remove(this);
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

    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
        this.io.getOut().println("\"" + msg);
    }

    /**
     * Stores a bam track in the database and gives appropriate status messages.
     * @param trackJob the information about the track to store
     * @param readPairs true, if this is a readuence pair import, false otherwise
     */
    private void storeBamTrack(ParsedTrack track, boolean readPairs) {
        try {
            io.getOut().println(track.getTrackName() + ": " + this.getBundleString("MSG_ImportThread.import.start.trackdirect"));
            ProjectConnector.getInstance().storeBamTrack(track);
            ProjectConnector.getInstance().storeTrackStatistics(track);
            if (readPairs) {
                ProjectConnector.getInstance().storeReadPairTrackStatistics(track.getStatsContainer(), track.getID());
            }
            io.getOut().println(this.getBundleString("MSG_ImportThread.import.success.trackdirect"));
            
        } catch(OutOfMemoryError e) {
            io.getOut().println(this.getBundleString("MSG_ImportThread.import.outOfMemory") + "!");
        }
    }
    
    /**
     * @param name the name of the bundle string to return (found in Bundle.properties)
     * @return the string associated in the Bundle.properties with the given name.
     */
    private String getBundleString(String name) {
        return NbBundle.getMessage(ImportThread.class, name);
    }

    /**
     * Extends a sam or bam file with ReadXplorers classification data.
     * @param classificationMap the classification map of classification data
     * @param trackJob the track job containing the file to extend
     * @param chromLengthMap the mapping of chromosome names to chromosome length
     * @return true, if the extension was successful, false otherwise
     */
    private boolean extendSamBamFile(Map<String, ParsedClassification> classificationMap, TrackJob trackJob, Map<String, Integer> chromLengthMap) {
        boolean success;
        try {
            io.getOut().println(NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.start.extension", trackJob.getFile().getName()));
            long start = System.currentTimeMillis();
            
            //sorts file again by genome coordinate (position) & stores classification data
            SamBamExtender bamExtender = new SamBamExtender(classificationMap);
            bamExtender.setDataToConvert(trackJob, chromLengthMap);
            bamExtender.registerObserver(this);
            success = bamExtender.convert();
            
            long finish = System.currentTimeMillis();
            String msg = NbBundle.getMessage(ImportThread.class, "MSG_ImportThread.import.finish.extension", trackJob.getFile().getName());
            io.getOut().println(Benchmark.calculateDuration(start, finish, msg));
            
        } catch (ParsingException ex) {
            this.showMsg(ex.toString());
            success = false;
        }
        
        return success;
    }
}
