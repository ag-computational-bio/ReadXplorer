/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.rnaTrimming;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.databackend.ThreadListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Logger;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
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
public class RNATrimProcessor {
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
        
        int currentline = 0;
        try (SAMFileReader samBamReader = new SAMFileReader(samfile)) { 
            SAMRecordIterator samItor = samBamReader.iterator();

            FileWriter fileWriter = new FileWriter(new File(fastaPath));
            BufferedWriter fasta = new BufferedWriter(fileWriter);
            
            while (samItor.hasNext() && (!this.canceled)) {
                currentline++;
                ph.progress(currentline);
                
                
                try {
                    SAMRecord record = samItor.next();
                    if (record.getReadUnmappedFlag()) {
                        fasta.write(">"+record.getReadName()+":original:"+record.getReadString()+"\n");
                        fasta.write(method.trim(record.getReadString())+"\n"); 
                    }
                } catch(SAMFormatException e) {
                    this.showMsg("Cought SAMFormatException for a record in your SAM file: "+e.getMessage());
                }
            }
            fasta.close();
            fileWriter.close();
            samItor.close();
            io.getOut().println(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Finish", samfile.getAbsolutePath()));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            io.getOut().println(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Failed", samfile.getAbsolutePath()));
        }
        ph.finish();
        return fastaPath;  
    }
    
    /**
     * Extracts unmapped reads from a SAM file to a FASTA file and trims them. 
     * @param samfile the sam file containing the reads
     * @param method the trim method to be used
     */
    private String extractOriginalSequencesInSamFile(String sampath) {       
        //set path to the fasta file to be created
        File samfile = new File(sampath);
        String newPath = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(samfile)+"_with_originals.sam";
        
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
    * Join array elements with a string
    * @param delim Delimiter
    * @param array Array of elements
    * @return String
    */
    public static String implode(String delim, Object[] array) {
            String AsImplodedString;
            if (array.length==0) {
                    AsImplodedString = "";
            } 
            else {
                    StringBuffer sb = new StringBuffer();
                    sb.append(array[0]);
                    for (int i=1;i<array.length;i++) {
                            sb.append(delim);
                            sb.append(array[i]);
                    }
                    AsImplodedString = sb.toString();
            }
            return AsImplodedString;
    }
    
    /* run a command line tool and write the output to the console */
    private void runCommandAndWaitUntilEnded(String... command) throws IOException {
        this.showMsg("executing following command: "+implode(" ", command));
        ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
        Process process = processBuilder.start();
        
        //OutputStream outputStream = process.getOutputStream();
        java.io.InputStream is = process.getInputStream();
        java.io.BufferedReader reader = new java.io.BufferedReader(new InputStreamReader(is));
        // And print each line
        String s = null;
        while ((s = reader.readLine()) != null) {
            this.showMsg(s);
        }
        is.close();
        
        //Wait to get exit value
        try {
            int exitValue = process.waitFor();
            this.showMsg("\n\nExit Value is " + exitValue);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private String mapFastaFile(String reference, String fasta) throws IOException {     
        String basename = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(fasta);
        File fastafile = new File(basename);
        basename = fastafile.getName();
        this.runCommandAndWaitUntilEnded("/Users/jeff/Masterarbeit/Daten/remoteMapper.sh", reference, fasta, basename);
        return fastafile.getAbsolutePath()+".sam";
    }

    
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
       
        this.io.getOut().println(msg);
    }
    
    public RNATrimProcessor(final String referencePath, final String sourcePath, final int maximumTrim, final TrimMethod method ) {
        NbBundle.getMessage(RNATrimProcessor.class, "RNATrimProcessor.output.name");
        this.io = IOProvider.getDefault().getIO("dsasfddsfdsa", true);
        this.io.setOutputVisible(true);
        this.io.getOut().println("test");
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
            
            
            /*private void processOneStep() {
                
            }*/
            
            @Override
            public void run() {
                //ph.start();
                //ph.progress(0);
                
                //processOneStep();
                showMsg("Extract unmapped reads to a file...");
                String fasta = extractUnmappedReadsAndTrim(new File(sourcePath), method);    
                String sam = null;
                String extractedSam = null;
                try {
                    if (!canceled) sam = mapFastaFile(referencePath, fasta);
                    if (!canceled) extractedSam = extractOriginalSequencesInSamFile(sam);
                    if (!canceled) showMsg("Extraction ready...");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
                
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
                ph.finish();
            }

            
            
            
        };
        theTask = RP.create(runnable); //the task is not started yet
        theTask.schedule(1*1000); //start the task with a delay of 1 seconds
    }
    
    private boolean handleCancel() {
         this.canceled = true;
         this.showMsg(NbBundle.getMessage(RNATrimProcessor.class, "MSG_TrimProcessor.cancel", sourcePath));
         return true;
    }
}
