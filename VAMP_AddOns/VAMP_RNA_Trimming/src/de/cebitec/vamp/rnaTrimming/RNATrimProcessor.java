/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.mapping.api.MappingApi;
import de.cebitec.vamp.util.SimpleOutput;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
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
    private String sourcePath;
    private boolean canceled = false;
    private final TrimProcessResult trimProcessResult;
    
    
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
        String fastaPath = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(samfile)+"_"+method.getShortDescription()+".redo.fastq";
        
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
        this.trimProcessResult.setMappedReads(0);
        int currentline = 0;
        try (SAMFileReader samBamReader = new SAMFileReader(samfile)) { 
            SAMRecordIterator samItor = samBamReader.iterator();

            FileWriter fileWriter = new FileWriter(new File(fastaPath));
            BufferedWriter fasta = new BufferedWriter(fileWriter);
            this.trimProcessResult.setTrimmedReads(0);
            while (samItor.hasNext() && (!this.canceled)) {
                currentline++;
                this.trimProcessResult.setAllReads(currentline);
                ph.progress(currentline);
                
                //update chart after every 1000 lines
                if (currentline % 1000 == 1) this.updateChartData();
                
                try {
                    SAMRecord record = samItor.next();
                    String separator = ":os:";
                    if (record.getReadUnmappedFlag()) {
                        TrimMethodResult trimResult = method.trim(record.getReadString());
                        fasta.write(">"+record.getReadName()+separator+record.getReadString()
                          +separator+trimResult.getTrimmedCharsFromLeft()
                          +separator+trimResult.getTrimmedCharsFromRight()+"\n");
                        fasta.write(trimResult.getSequence()+"\n"); 
                        this.trimProcessResult.incrementTrimmedReads();
                    }
                    else {
                        this.trimProcessResult.incrementMappedReads();
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
        
        this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sampath));
        
        //set up the progress handle to indicate progress to the user
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sampath), 
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
            this.trimProcessResult.setTrimmedMappedReads(0);
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
                       this.trimProcessResult.incrementTrimmedMappedReads(); 
                    }
                    
                } catch(SAMFormatException e) {
                    this.showMsg("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
                }
            }
            writer.close();
            samItor.close();
            
            this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Finish", samfile.getAbsolutePath()));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Failed", samfile.getAbsolutePath()));
        }
        ph.finish();
        this.updateChartData();
        return newPath;  
    }
    
    
    

    
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
       this.panelOutput.println(msg);
    }
    
    private PrintStream panelOutput;
    
    
    
    public RNATrimProcessor(final String referencePath, final String sourcePath, 
            final int maximumTrim, final TrimMethod method, final String mappingParam) {
        NbBundle.getMessage(RNATrimProcessor.class, "RNATrimProcessor.output.name");
        //this.io = IOProvider.getDefault().getIO("RNATrimProcessor", true);
        //this.io.setOutputVisible(true);
        //this.io.getOut().println("");
        String shortFileName = FileUtils.getName(sourcePath);
        
        TrimResultTopComponent tc = TrimResultTopComponent.findInstance();
        tc.open();
        tc.requestActive();
        final TrimResultPanel resultView = tc.openResultTab(shortFileName);
        this.trimProcessResult = new TrimProcessResult();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("referencePath", referencePath);
        params.put("sourcePath", sourcePath);
        params.put("maximumTrim", maximumTrim);
        params.put("method", method);
        params.put("mappingParam", mappingParam);
        this.trimProcessResult.setAnalysisParameters(params);
        resultView.setAnalysisResult(this.trimProcessResult);
        final PrintStream output = resultView.getOutput(); 
        this.panelOutput = output;
        
        this.sourcePath = sourcePath;
        method.setMaximumTrimLength(maximumTrim);
        
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Trim RNA reads in file '"+sourcePath+"'", new Cancellable() {

            public boolean cancel() {
                return handleCancel();
            }

            
        });
        CentralLookup.getDefault().add(this);
        /*try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();*/
        
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
                    if (!canceled) sam = MappingApi.mapFastaFile(new SimpleOutput() {

                    @Override
                    public void showMessage(String s) {
                         showMsg(s);
                    }

                    @Override
                    public void showError(String s) {
                        showMsg(s);
                    }
                }, referencePath, fasta, mappingParam);
                    if (!canceled) extractedSam = extractOriginalSequencesInSamFile(sam, true);
                    if (!canceled) FileUtils.delete(sam);
                    if (!canceled) FileUtils.delete(fasta);
                    if (!canceled) showMsg("Extraction ready!");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                trimProcessResult.ready();
                resultView.ready();
                
                showMsg("trimmed reads: " + trimProcessResult.getTrimmedReads());
                showMsg("trimmed mapped reads: " + trimProcessResult.getTrimmedMappedReads());
                
            }

            
            
            
        };
        theTask = RP.create(runnable); //the task is not started yet
        theTask.schedule(1*1000); //start the task with a delay of 1 seconds
        
        
        /*SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    createStatisticsWindow();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        */
        
                 
        
        
    }
    
    private boolean handleCancel() {
         this.canceled = true;
         this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.cancel", sourcePath));
         return true;
    }

    private void updateChartData() {
        this.trimProcessResult.notifyChanged();
    }
}
