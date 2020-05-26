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

package de.cebitec.readxplorer.rnatrimming;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.mapping.api.MappingApi;
import de.cebitec.readxplorer.utils.SimpleOutput;
import htsjdk.samtools.SAMFileHeader;
import htsjdk.samtools.SAMFileWriter;
import htsjdk.samtools.SAMFileWriterFactory;
import htsjdk.samtools.SAMFormatException;
import htsjdk.samtools.SAMRecord;
import htsjdk.samtools.SAMRecordIterator;
import htsjdk.samtools.SamReader;
import htsjdk.samtools.SamReaderFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.h2.store.fs.FileUtils;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static htsjdk.samtools.ValidationStringency.LENIENT;
import static java.util.regex.Pattern.compile;


/**
 * SamTrimmer allows to filter unmapped entries in SAM file and trim them using
 * a trim method. The user will see a progress info.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class RNATrimProcessor {

    private static final Logger LOG = LoggerFactory.getLogger( RNATrimProcessor.class.getName() );
    private static final RequestProcessor RP = new RequestProcessor( "interruptible tasks", 1, true );
    private static final Pattern OS_PATTERN = compile( ":os:" );

    private RequestProcessor.Task theTask = null;
    private String sourcePath;
    private boolean canceled = false;
    private final TrimProcessResult trimProcessResult;


    private Map<String, Integer> computeMappingHistogram( SamReader samBamReader ) {
        MapCounter<String> histogram = new MapCounter<>();
        try( SAMRecordIterator samItor = samBamReader.iterator() ) {
            while( samItor.hasNext() && (!this.canceled) ) {
                try {
                    SAMRecord record = samItor.next();
                    if( record.getReadUnmappedFlag() ) {
                        histogram.put( record.getReadName(), 0 );
                    } else {
                        histogram.incrementCount( record.getReadName() );
                    }
                } catch( SAMFormatException e ) {
                    this.showMsg( "Cought SAMFormatException for a record in your SAM file: " + e.getMessage() );
                }
            }
        }
        return histogram;
    }


    /**
     * Extracts unmapped reads from a SAM file to a FASTA file and trims them.
     * <p>
     * @param samfile the sam file containing the reads
     * @param method  the trim method to be used
     */
    private String extractUnmappedReadsAndTrim( File samfile, TrimMethod method ) {
        //set path to the fasta file to be created
        String fastaPath = de.cebitec.readxplorer.utils.FileUtils.getFilePathWithoutExtension( samfile ) + "_" + method.getShortDescription() + ".redo.fastq";

        //set up the progress handle to indicate progress to the user
        ProgressHandle ph = ProgressHandle.createHandle(
                NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Start", sourcePath ),
                new Cancellable() {
            @Override
            public boolean cancel() {
                return handleCancel();
            }


        } );
        ph.start();

        //count the number of lines in the samfile, to estimate the progress
        int lines = de.cebitec.readxplorer.utils.FileUtils.countLinesInFile( samfile );
        ph.switchToDeterminate( lines );

        //int allReads = 0;
        //int mapped = 0;
        this.trimProcessResult.setMappedReads( 0 );
        int currentline = 0;
        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        try( final SamReader samBamReader = samReaderFactory.open( samfile );
             SAMRecordIterator samItor = samBamReader.iterator(); ) {

            FileWriter fileWriter = new FileWriter( new File( fastaPath ) );
            BufferedWriter fasta = new BufferedWriter( fileWriter );
            this.trimProcessResult.setTrimmedReads( 0 );
            while( samItor.hasNext() && (!this.canceled) ) {
                currentline++;
                this.trimProcessResult.setAllReads( currentline );

                //when reading a BAM file, there could be more entries than newlines
                //in this case we would get lots of INFO messages on the console
                //workaround: do not output the progress, if the current value is bigger
                // than the whole lines count (stays at 100%)
                if( lines >= currentline ) {
                    ph.progress( currentline );
                }

                //update chart after every 1000 lines
                if( currentline % 1000 == 1 ) {
                    this.updateChartData();
                }

                try {
                    SAMRecord record = samItor.next();
                    String separator = ":os:";
                    //String fullSequence = record.getReadString();


                    if( record.getReadUnmappedFlag() ) {
                        TrimMethodResult trimResult = method.trim( record.getReadString() );
                        fasta.write( ">" + record.getReadName() + separator + trimResult.getOsField() +
                                     separator + trimResult.getTrimmedCharsFromLeft() +
                                     separator + trimResult.getTrimmedCharsFromRight() + "\n" );
                        fasta.write( trimResult.getSequence() + "\n" );
                        this.trimProcessResult.incrementTrimmedReads();
                    } else {
                        this.trimProcessResult.incrementMappedReads();
                    }

                } catch( SAMFormatException e ) {
                    this.showMsg( "Cought SAMFormatException for a record in your SAM file: " + e.getMessage() );
                }
            }
            //this.trimmedReads = this.allReads-this.mappedReads;
            fasta.close();
            fileWriter.close();
            this.showMsg( NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Finish", samfile.getAbsolutePath() ) );
        } catch( Exception e ) {
            Exceptions.printStackTrace( e );
            this.showMsg( NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractUnmapped.Failed", samfile.getAbsolutePath() ) );
        }
        ph.finish();
        this.updateChartData();
        return fastaPath;
    }


    /**
     * Extracts unmapped reads from a SAM file to a FASTA file and trims them.
     * <p>
     * @param samfile the sam file containing the reads
     * @param method  the trim method to be used
     */
    private String extractOriginalSequencesInSamFile( String sampath, boolean writeOnlyMapped ) {
        //set path to the fasta file to be created
        File samfile = new File( sampath );
        String newPath = de.cebitec.readxplorer.utils.FileUtils.getFilePathWithoutExtension( samfile ) + "_with_originals.sam";

        this.showMsg( NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sampath ) );

        //set up the progress handle to indicate progress to the user
        ProgressHandle ph = ProgressHandle.createHandle(
                NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Start", sampath ),
                new Cancellable() {
            @Override
            public boolean cancel() {
                return handleCancel();
            }


        } );
        ph.start();

        //count the number of lines in the samfile, to estimate the progress
        int lines = de.cebitec.readxplorer.utils.FileUtils.countLinesInFile( samfile );
        ph.switchToDeterminate( lines );

        int currentline = 0;
        SamReaderFactory.setDefaultValidationStringency( LENIENT );
        SamReaderFactory samReaderFactory = SamReaderFactory.make();
        try( final SamReader samBamReader = samReaderFactory.open( samfile );
             SAMRecordIterator samItor = samBamReader.iterator(); ) {

            SAMFileHeader header = samBamReader.getFileHeader();
            SAMFileWriterFactory factory = new SAMFileWriterFactory();
            File outputFile = new File( newPath );
            SAMFileWriter writer = factory.makeSAMWriter( header, false, outputFile );
            this.trimProcessResult.setTrimmedMappedReads( 0 );
            while( samItor.hasNext() && (!this.canceled) ) {
                currentline++;
                ph.progress( currentline );

                //update chart after every 1000 lines
                if( currentline % 1000 == 1 ) {
                    this.updateChartData();
                }

                try {
                    SAMRecord record = samItor.next();
                    // the readname field will have the form
                    // name:original:fullsequence
                    // so try to split it into two parts
                    String[] parts = OS_PATTERN.split( record.getReadName() );
                    if( parts.length == 4 ) {
                        record.setReadName( parts[0] );
                        record.setAttribute( "os", parts[1] ); // os = original sequence
                        try {
                            int tl = Integer.parseInt( parts[2] );
                            int tr = Integer.parseInt( parts[3] );
                            record.setAttribute( "tl", tl ); // tl = trimmed from left
                            record.setAttribute( "tr", tr ); // tr = trimmed from right
                        } catch( NumberFormatException e ) {
                            LOG.error( "RNATrimProcessor: Readname parts have wrong format - integer expected, but found: {0} and {1}",
                                       new Object[]{ parts[2], parts[3] } );
                        }

                    }
                    if( (!writeOnlyMapped) || (writeOnlyMapped && (!record.getReadUnmappedFlag())) ) {
                        writer.addAlignment( record );
                    }

                    if( !record.getReadUnmappedFlag() ) {
                        trimProcessResult.incrementTrimmedMappedReads();
                    }

                } catch( SAMFormatException e ) {
                    this.showMsg( "Cought SAMFormatException for a record in your SAM file: " + e.getMessage() );
                }
            }
            writer.close();

            this.showMsg( NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Finish", samfile.getAbsolutePath() ) );
        } catch( Exception e ) {
            Exceptions.printStackTrace( e );
            this.showMsg( NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.extractOriginalSequencesInSamFile.Failed", samfile.getAbsolutePath() ) );
        }
        ph.finish();
        this.updateChartData();
        return newPath;
    }


    /**
     * If any message should be printed to the console, this method is used. If
     * an error occurred during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * <p>
     * @param msg the msg to print
     */
    private void showMsg( String msg ) {
        this.panelOutput.println( msg );
    }


    private PrintStream panelOutput;


    public RNATrimProcessor( final String referencePath, final String sourcePath,
                             final int maximumTrim, final TrimMethod method, final String mappingParam ) {
        NbBundle.getMessage( RNATrimProcessor.class, "RNATrimProcessor.output.name" );
        String shortFileName = FileUtils.getName( sourcePath );

        TrimResultTopComponent tc = TrimResultTopComponent.findInstance();
        tc.open();
        tc.requestActive();
        final TrimResultPanel resultView = tc.openResultTab( shortFileName );
        this.trimProcessResult = new TrimProcessResult();
        HashMap<String, Object> params = new HashMap<>();
        params.put( "referencePath", referencePath );
        params.put( "sourcePath", sourcePath );
        params.put( "maximumTrim", maximumTrim );
        params.put( "method", method );
        params.put( "mappingParam", mappingParam );
        this.trimProcessResult.setAnalysisParameters( params );
        resultView.setAnalysisResult( this.trimProcessResult );
        final PrintStream output = resultView.getOutput();
        this.panelOutput = output;

        this.sourcePath = sourcePath;
        method.setMaximumTrimLength( maximumTrim );

        final ProgressHandle ph = ProgressHandle.createHandle( "Trim RNA reads in file '" + sourcePath + "'", new Cancellable() {

                                                                  @Override
                                                                  public boolean cancel() {
                                                                      return handleCancel();
                                                                  }


                                                              } );
        CentralLookup.getDefault().add( this );

        //the trim processor will:
        // 1. open source sam file
        // 2. delete all mapped reads
        // 3. delete all nonsense poly-A reads (an A-stretch)
        // 4. create a fasq file for remapping
        // 5. map fasq file against the reference genome
        // 6. add a tag to the resulting sam file, indicating the source untrimmed sequence
        // 7. add a tag to the resulting sam file, inducating match uniqueness
        Runnable runnable = new Runnable() {


            @Override
            public void run() {
                showMsg( "Extract unmapped reads to a file..." );
                String fasta = extractUnmappedReadsAndTrim( new File( sourcePath ), method );
                String sam = null;
                String extractedSam = null;
                try {
                    if( !canceled ) {
                        sam = MappingApi.mapFastaFile( new SimpleOutput() {

                            @Override
                            public void showMessage( String s ) {
                                showMsg( s );
                            }


                            @Override
                            public void showError( String s ) {
                                showMsg( s );
                            }


                        }, referencePath, fasta, mappingParam );
                    }
                    if( !canceled ) {
                        extractedSam = extractOriginalSequencesInSamFile( sam, true );
                    }
                    if( !canceled ) {
                        FileUtils.delete( sam );
                    }
                    if( !canceled ) {
                        FileUtils.delete( fasta );
                    }
                    if( !canceled ) {
                        showMsg( "Extraction ready!" );
                    }
                } catch( IOException ex ) {
                    Exceptions.printStackTrace( ex );
                }
                trimProcessResult.ready();
                resultView.ready();

                showMsg( "trimmed reads: " + trimProcessResult.getTrimmedReads() );
                showMsg( "trimmed mapped reads: " + trimProcessResult.getTrimmedMappedReads() );

            }


        };
        theTask = RP.create( runnable ); //the task is not started yet
        theTask.schedule( 1 * 1000 ); //start the task with a delay of 1 seconds

    }


    private boolean handleCancel() {
        this.canceled = true;
        this.showMsg( NbBundle.getMessage( RNATrimProcessor.class, "MSG_TrimProcessor.cancel", sourcePath ) );
        return true;
    }


    private void updateChartData() {
        this.trimProcessResult.notifyChanged();
    }


}
