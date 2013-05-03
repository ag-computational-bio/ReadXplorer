/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.mapping.api.MappingApi;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import org.h2.store.fs.FileUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * SamTrimmer allows to filter unmapped entries in SAM file
 * and trim them using a trim method.
 * The user will see a progress info.
 * @author jeff
 */
public class RNATrimProcessor  {
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private final static Logger LOG = Logger.getLogger(RNATrimProcessor.class.getName());
    private RequestProcessor.Task theTask = null;
    private InputOutput io;
    private String sourcePath;
    private boolean canceled = false;
    
    
    private Map<String, Integer> computeMappingHistogram(SAMFileReader samBamReader) {
        MapCounter<String> histogram = new MapCounter<String>();
        SAMRecordIterator samItor = samBamReader.iterator();
        while (samItor.hasNext() && (!this.canceled)) {
            try {
                    SAMRecord record = samItor.next();
                    if (record.getReadUnmappedFlag()) {
                        histogram.put(record.getReadName(), 0);
                    }
                    else {
                        histogram.incrementCount(record.getReadName());
                    }
            } catch(SAMFormatException e) {
                    this.showMsg("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
            }
        }
        samItor.close();
        return histogram;
    }
    
    
    
    /**
     * Extracts unmapped reads from a SAM file to a FASTA file and trims them. 
     * @param samfile the sam file containing the reads
     * @param method the trim method to be used
     */
    private String extractUnmappedReadsAndTrim(File samfile, TrimMethod method) {       
        //set path to the fasta file to be created
        String fastaPath = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(samfile)+".redo.fastq";
        
        //set up the progress handle to indicate progress to the user
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Start", sourcePath), 
                new Cancellable() {
            public boolean cancel() {
                return handleCancel();
            }
        });
        ph.start();
        
        //count the number of lines in the samfile, to estimate the progress
        int lines = de.cebitec.vamp.util.FileUtils.countLinesInFile(samfile);
        ph.switchToDeterminate(lines);
        
        //int allReads = 0;
        //int mapped = 0;
        this.mappedReads = 0;
        int currentline = 0;
        try (SAMFileReader samBamReader = new SAMFileReader(samfile)) { 
            SAMRecordIterator samItor = samBamReader.iterator();

            FileWriter fileWriter = new FileWriter(new File(fastaPath));
            BufferedWriter fasta = new BufferedWriter(fileWriter);
            this.trimmedReads = 0;
            while (samItor.hasNext() && (!this.canceled)) {
                currentline++;
                this.allReads = currentline;
                ph.progress(currentline);
                
                //update chart after every 1000 lines
                if (currentline % 1000 == 1) this.updateChartData();
                
                try {
                    SAMRecord record = samItor.next();
                    String separator = ":os:";
                    if (record.getReadUnmappedFlag()) {
                        TrimResult trimResult = method.trim(record.getReadString());
                        fasta.write(">"+record.getReadName()+separator+record.getReadString()
                          +separator+trimResult.getTrimmedCharsFromLeft()
                          +separator+trimResult.getTrimmedCharsFromRight()+"\n");
                        fasta.write(trimResult.getSequence()+"\n"); 
                        this.trimmedReads++;
                    }
                    else {
                        this.mappedReads++;
                    }
                    
                } catch(SAMFormatException e) {
                    this.showMsg("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
                }
            }
            //this.trimmedReads = this.allReads-this.mappedReads;
            fasta.close();
            fileWriter.close();
            samItor.close();
            this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Finish", samfile.getAbsolutePath()));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Failed", samfile.getAbsolutePath()));
        }
        ph.finish();
        this.updateChartData();
        return fastaPath;  
    }
    
    /**
     * Extracts unmapped reads from a SAM file to a FASTA file and trims them. 
     * @param samfile the sam file containing the reads
     * @param method the trim method to be used
     */
    private String extractOriginalSequencesInSamFile(String sampath, boolean writeOnlyMapped) {       
        //set path to the fasta file to be created
        File samfile = new File(sampath);
        String newPath = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(samfile)+"_with_originals.sam";
        
        this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sourcePath));
        
        //set up the progress handle to indicate progress to the user
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sourcePath), 
                new Cancellable() {
            public boolean cancel() {
                return handleCancel();
            }
        });
        ph.start();
        
        //count the number of lines in the samfile, to estimate the progress
        int lines = de.cebitec.vamp.util.FileUtils.countLinesInFile(samfile);
        ph.switchToDeterminate(lines);
        
        int currentline = 0;
        try (SAMFileReader samBamReader = new SAMFileReader(samfile)) { 
            SAMRecordIterator samItor = samBamReader.iterator();
            
            SAMFileHeader header = samBamReader.getFileHeader();
            SAMFileWriterFactory factory = new SAMFileWriterFactory();
            File outputFile = new File(newPath);
            SAMFileWriter writer = factory.makeSAMWriter(header, false, outputFile);
            this.trimmedMappedReads = 0;
            while (samItor.hasNext() && (!this.canceled)) {
                currentline++;
                ph.progress(currentline);
                
                //update chart after every 1000 lines
                if (currentline % 1000 == 1) this.updateChartData();
                
                try {
                    SAMRecord record = samItor.next();
                    // the readname field will have the form
                    // name:original:fullsequence
                    // so try to split it into two parts 
                    String[] parts = record.getReadName().split(":os:");
                    if (parts.length==4) {
                        record.setReadName(parts[0]);
                        record.setAttribute("os", parts[1]); // os = original sequence
                        try {
                            int tl = Integer.parseInt(parts[2]);
                            int tr = Integer.parseInt(parts[3]);
                            record.setAttribute("tl", tl); // tl = trimmed from left
                            record.setAttribute("tr", tr); // tr = trimmed from right
                        }
                        catch(Exception e) {}
                        
                    }
                    if ((!writeOnlyMapped) || (writeOnlyMapped && (!record.getReadUnmappedFlag()))) {
                        writer.addAlignment(record);
                    }
                    
                    if (!record.getReadUnmappedFlag()) {
                       this.trimmedMappedReads++; 
                    }
                    
                } catch(SAMFormatException e) {
                    this.showMsg("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
                }
            }
            writer.close();
            samItor.close();
            
            io.getOut().println(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Finish", samfile.getAbsolutePath()));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            io.getOut().println(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Failed", samfile.getAbsolutePath()));
        }
        ph.finish();
        this.updateChartData();
        return newPath;  
    }
    
    /**
     * Compute the histogram values and write them to the SAM file
     * @param samfile the sam file containing the reads
     * @param method the trim method to be used
     */
    /*private String extractOriginalSequencesInSamFile(String sampath) {  
        //set path to the fasta file to be created
        File samfile = new File(sampath);
        String newPath = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(samfile)+"_with_histogram.sam";
        
        //set up the progress handle to indicate progress to the user
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sourcePath), 
                new Cancellable() {
            public boolean cancel() {
                return handleCancel();
            }
        });
        ph.start();
        
        //count the number of lines in the samfile, to estimate the progress
        int lines = de.cebitec.vamp.util.FileUtils.countLinesInFile(samfile);
        ph.switchToDeterminate(lines);
        
        int currentline = 0;
        try (SAMFileReader samBamReader = new SAMFileReader(samfile)) { 
            SAMRecordIterator samItor = samBamReader.iterator();
            
            SAMFileHeader header = samBamReader.getFileHeader();
            SAMFileWriterFactory factory = new SAMFileWriterFactory();
            File outputFile = new File(newPath);
            SAMFileWriter writer = factory.makeSAMWriter(header, false, outputFile);
            
            while (samItor.hasNext() && (!this.canceled)) {
                currentline++;
                ph.progress(currentline);
                
                try {
                    SAMRecord record = samItor.next();
                    // the readname field will have the form
                    // name:original:fullsequence
                    // so try to split it into two parts 
                    String[] parts = record.getReadName().split(":original:");
                    if (parts.length==2) {
                        record.setReadName(parts[0]);
                        record.setAttribute("os", parts[1]); // os = original sequence
                    }
                    writer.addAlignment(record);
                } catch(SAMFormatException e) {
                    this.showMsg("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
                }
            }
            writer.close();
            samItor.close();
            io.getOut().println(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Finish", samfile.getAbsolutePath()));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            io.getOut().println(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Failed", samfile.getAbsolutePath()));
        }
        ph.finish();
        return newPath;  
    }  */
    
    
    

    
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
       
        this.io.getOut().println(msg);
    }
    
    //private ObservableList<PieChart.Data> pieChartData = null;
    private Integer allReads = 0;
    private Integer mappedReads = 0;
    private Integer trimmedReads = 0;
    private Integer trimmedMappedReads = 0;
    //private Integer trimmedMappedReads = null;
    //private Integer unmappedReads = null;
    
    private void updateChartData() {
        if (this.statisticsWindow!=null) { 
            Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //Integer unmappedReads = allReads;
                /*if (pieChartData!=null) {
                    pieChartData.clear();
                    if (mappedReads==null) {    
                        pieChartData.add(new PieChart.Data("all reads", allReads));
                    }
                    else {
                        unmappedReads -= mappedReads;
                        pieChartData.add(new PieChart.Data("fully mapped reads", mappedReads));
                        if (trimmedMappedReads==null) { 
                            if (trimmedReads!=null) {
                                unmappedReads -= trimmedReads;
                                pieChartData.add(new PieChart.Data("trimmed reads", trimmedReads));
                            }
                        }
                        else {
                            unmappedReads -= trimmedMappedReads;
                            pieChartData.add(new PieChart.Data("trimmed mapped reads", trimmedMappedReads));
                        }
                        pieChartData.add(new PieChart.Data("unmapped reads", unmappedReads));
                    }


                }*/

                /*
                 * new XYChart.Data<String, Number>(all, mappedReads);
                series1.getData().add();
                series2.getData().add(new XYChart.Data<String, Number>(all, allReads-mappedReads));
                series1.getData().add(new XYChart.Data<String, Number>(trimmed, trimmedMappedReads));
                series2.getData().add(new XYChart.Data<String, Number>(trimmed, trimmedReads-trimmedMappedReads));
                 */
                whole_mapped_data.setYValue(mappedReads);
                whole_unmapped_data.setYValue(allReads-mappedReads);
                trimmed_mapped_data.setYValue(trimmedMappedReads);
                trimmed_unmapped_data.setYValue(trimmedReads-trimmedMappedReads);


            }});
        
            statisticsWindow.setVisible(true);
            statisticsWindow.toFront();
        }
        
    }
    
    private JFrame statisticsWindow; 
    XYChart.Series<String, Number> series1;
    XYChart.Series<String, Number> series2;
    String all = "source";
    String trimmed = "trimmed";
    
    XYChart.Data<String, Number> whole_mapped_data;
    XYChart.Data<String, Number> whole_unmapped_data;
    XYChart.Data<String, Number> trimmed_mapped_data;
    XYChart.Data<String, Number> trimmed_unmapped_data;
    StackedBarChart<String, Number> chart;
    
    public void createStatisticsWindow() throws InterruptedException {       
        //SunOS is not supported by JavaFX
        // in this case an exception will be trown. it is safe to ignore it
        // and continue without showing stats window to the user
        try {
        
            this.statisticsWindow = new JFrame("Read statistics");
            statisticsWindow.setSize(500, 500);
            final JFXPanel fxPanel = new JFXPanel();
            statisticsWindow.add(fxPanel);

            Platform.runLater(new Runnable() {
            @Override
            public void run() {
                //Stage primaryStage = new Stage();
                Group root = new Group();
                fxPanel.setScene(new Scene(root));
                 /*pieChartData = FXCollections.observableArrayList(

                 );*/
                CategoryAxis xAxis = new CategoryAxis();
                NumberAxis yAxis = new NumberAxis(); 
                yAxis.setAutoRanging(true);
                chart =
                new StackedBarChart<String, Number>(xAxis, yAxis);
                chart.setAnimated(false);
                series2 = new XYChart.Series<String, Number>();
                series1 = new XYChart.Series<String, Number>();

                series1.setName("mapped");
                series2.setName("unmapped");

                series1.getData().clear();
                series1.getData().clear();
                whole_unmapped_data = new XYChart.Data<String, Number>(all, 1);
                whole_mapped_data = new XYChart.Data<String, Number>(all, 1);
                trimmed_unmapped_data = new XYChart.Data<String, Number>(trimmed, 1);
                trimmed_mapped_data = new XYChart.Data<String, Number>(trimmed, 1);   

                series1.getData().add(trimmed_mapped_data);
                series2.getData().add(trimmed_unmapped_data);
                series1.getData().add(whole_mapped_data);
                series2.getData().add(whole_unmapped_data);

                /*series2.getData().add(new XYChart.Data<String, Number>(all, allReads-mappedReads));
                series1.getData().add(new XYChart.Data<String, Number>(trimmed, trimmedMappedReads));
                series2.getData().add(new XYChart.Data<String, Number>(trimmed, trimmedReads-trimmedMappedReads));*/

                xAxis.setLabel("Data");
                xAxis.setCategories(FXCollections.<String>observableArrayList(
                    Arrays.asList(all, trimmed)));
                yAxis.setLabel("Reads");

                chart.setCategoryGap(5);
                chart.getData().addAll(series2, series1);

                //chart.setClockwise(false);
                root.getChildren().add(chart);

            }});
        } catch(UnsupportedOperationException e) {
            this.showMsg("Could not intialize statistics window: "+e.getLocalizedMessage());
            this.statisticsWindow = null;
        }
    } 
    
    public RNATrimProcessor(final String referencePath, final String sourcePath, final int maximumTrim, final TrimMethod method ) {
        NbBundle.getMessage(RNATrimProcessor.class, "RNATrimProcessor.output.name");
        this.io = IOProvider.getDefault().getIO("RNATrimProcessor", true);
        this.io.setOutputVisible(true);
        this.io.getOut().println("");
        this.sourcePath = sourcePath;
        method.setMaximumTrimLength(maximumTrim);
        
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Trim RNA reads in file '"+sourcePath+"'", new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }

            
        });
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();
        
        //the trim processor will:
        // 1. open source sam file
        // 2. delete all mapped reads
        // 3. delete all nonsense poly-A reads (an A-stretch)
        // 4. create a fasq file for remapping
        // 5. map fasq file against the reference genome
        // 6. add a tag to the resulting sam file, indicating the source untrimmed sequence
        // 7. add a tag to the resulting sam file, inducating match uniqueness
        Runnable runnable = new Runnable() {
            private int currentPosition = 1;
            private int steps;
            private int currentStep = 0;
            private boolean wasCanceled = false;
            
            private boolean ready = false;
            
            private ThreadListener tl; //requires VAMP_BACKEND Module
            
            
            @Override
            public void run() {
                showMsg("Extract unmapped reads to a file...");
                String fasta = extractUnmappedReadsAndTrim(new File(sourcePath), method);    
                String sam = null;
                String extractedSam = null;
                try {
                    if (!canceled) sam = MappingApi.mapFastaFile(io, referencePath, fasta);
                    if (!canceled) extractedSam = extractOriginalSequencesInSamFile(sam, true);
                    if (!canceled) FileUtils.delete(sam);
                    if (!canceled) showMsg("Extraction ready!");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                showMsg("trimmed reads: " + trimmedReads);
                showMsg("trimmed mapped reads: " + trimmedMappedReads);
                
                /*while((!ready) && (!wasCanceled)) {
                    try {
                        LOG.info("Cacher not ready yet...");
                        Thread.sleep(1000); //throws InterruptedException is the task was cancelled
                    } catch (InterruptedException ex) {
                        LOG.info("Track cacher was canceled");
                        wasCanceled = true;
                        return;
                    }
                }*/
                //ph.finish();
            }

            
            
            
        };
        theTask = RP.create(runnable); //the task is not started yet
        theTask.schedule(1*1000); //start the task with a delay of 1 seconds
        
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createStatisticsWindow();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        
        
                 
        
        
    }
    
    private boolean handleCancel() {
         this.canceled = true;
         this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.cancel", sourcePath));
         return true;
    }
}
