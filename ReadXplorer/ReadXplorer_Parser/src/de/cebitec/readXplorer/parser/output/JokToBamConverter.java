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
package de.cebitec.readXplorer.parser.output;


import de.cebitec.readXplorer.parser.common.ParsingException;
import de.cebitec.readXplorer.parser.mappings.CommonsMappingParser;
import de.cebitec.readXplorer.util.Benchmark;
import de.cebitec.readXplorer.util.ErrorLimit;
import de.cebitec.readXplorer.util.MessageSenderI;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.Properties;
import de.cebitec.readXplorer.util.SamUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.BAMFileWriter;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import org.openide.util.NbBundle;


/**
 * Converts a jok file from Saruman into a bam file sorted by read start
 * positions
 * for a single reference chromosome.
 * <p>
 * @author -Rolf Hilker-
 */
public class JokToBamConverter implements ConverterI, Observable, Observer,
                                          MessageSenderI {

    private List<File> jokFiles;
    private String refSeqName;
    private Integer refSeqLength;
    private File outputFile;

    private static String name = "Jok to BAM";
    private static String[] fileExtension = new String[]{ "out", "Jok", "jok", "JOK" };
    private static String fileDescription = "Saruman Output (jok)";
    private ArrayList<Observer> observers;
    private String msg;
    private ErrorLimit errorLimit;


    /**
     * Converts a jok file from Saruman into a bam file sorted by read start
     * positions
     * for a single reference chromosome.
     */
    public JokToBamConverter() {
        this.observers = new ArrayList<>();
        this.errorLimit = new ErrorLimit( 100 );
    }


    /**
     * A JokToBamConverter needs exactly the following three parameters:
     * <br>param1 jokFiles a list of jok files to convert into BAM format.
     * <br>param2 refSeqName name of the single reference chromosome sequence
     * <br>param3 refSeqLength length of the single reference chromosome
     * sequence
     * <p>
     * @throws IllegalArgumentException
     */
    @Override
    @SuppressWarnings( "unchecked" )
    public void setDataToConvert( Object... data ) throws IllegalArgumentException {
        boolean works = true;
        if( data.length >= 3 ) {
            if( data[0] instanceof List ) {
                this.jokFiles = (List<File>) data[0];
            }
            else {
                works = false;
            }
            if( data[1] instanceof String ) {
                this.refSeqName = (String) data[1];
            }
            else {
                works = false;
            }
            if( data[2] instanceof Integer ) {
                this.refSeqLength = (Integer) data[2];
            }
            else {
                works = false;
            }
        }
        else {
            works = false;
        }
        if( !works ) {
            throw new IllegalArgumentException( NbBundle.getMessage( JokToBamConverter.class,
                                                                     "Converter.SetDataError", "Inappropriate arguments passed to the converter!" ) );
        }
    }


    @Override
    public boolean convert() throws ParsingException {
        return this.convertJokToBam();
    }


    /**
     * Actually converts the jok file into a bam file.
     */
    private boolean convertJokToBam() throws ParsingException {
        boolean success = true;
        long startTime = System.currentTimeMillis();
        long finish;
        for( File currentFile : jokFiles ) {
            String fileName = currentFile.getName();
            int noReads = 0;
            try {

                this.notifyObservers( NbBundle.getMessage( JokToBamConverter.class, "Converter.Convert.Start", fileName ) );
                String outFileName;
                try( BufferedReader br = new BufferedReader( new FileReader( currentFile ) ) ) {
                    outputFile = SamUtils.getFileWithBamExtension( currentFile, "" );
                    outFileName = outputFile.getName();
                    BAMFileWriter bamFileWriter = new BAMFileWriter( outputFile );
                    bamFileWriter.setSortOrder( SAMFileHeader.SortOrder.coordinate, false );
                    SAMFileHeader fileHeader = new SAMFileHeader();
                    List<SAMSequenceRecord> samRecords = new ArrayList<>();
                    SAMSequenceRecord seqRecord = new SAMSequenceRecord( refSeqName, refSeqLength );
                    samRecords.add( seqRecord );
                    fileHeader.setTextHeader( "@HD VN:1.4 SO:coordinate" );
                    fileHeader.setSequenceDictionary( new SAMSequenceDictionary( samRecords ) );
                    bamFileWriter.setHeader( fileHeader );
                    String[] tokens;
                    int lineno = 0;
                    String line;
                    byte direction;
                    String readSeq;
                    String refSeq;
                    String readwithoutGaps;
                    String readName;
                    int start;
                    int stop;
                    String cigar;
                    SAMRecord samRecord;
                    int counter = 0;
                    int emtpy = 0;
                    while( (line = br.readLine()) != null ) {
                        lineno++;

                        // tokenize input line
                        tokens = line.split( "\\t+", 8 );
                        if( tokens.length == 7 ) { // if the length is not correct the read is not parsed
                            // cast tokens
                            readName = tokens[0];
                            try {
                                start = Integer.parseInt( tokens[1] );
                                stop = Integer.parseInt( tokens[2] );
                                ++start;
                                ++stop; // some people (no names here...) start counting at 0, I count genome position starting with 1
                            }
                            catch( NumberFormatException e ) { //
                                if( !tokens[1].equals( "*" ) ) {
                                    this.sendMsgIfAllowed( "Value for current start position in "
                                                           + outFileName + " line " + lineno + " is not a number or *. "
                                                           + "Found start: " + tokens[1] );
                                }
                                if( !tokens[2].equals( "*" ) ) {
                                    this.sendMsgIfAllowed( "Value for current stop position in "
                                                           + outFileName + " line " + lineno + " is not a number or *. "
                                                           + "Found stop: " + tokens[2] );
                                }
                                continue; //*'s are ignored = unmapped read
                            }

                            direction = 0;
                            switch( tokens[3] ) {
                                case ">>":
                                    direction = 1;
                                    break;
                                case "<<":
                                    direction = -1;
                                    break;
                            }
                            readSeq = tokens[4];
                            refSeq = tokens[5];

                            // report empty mappings saruman should not be producing anymore
                            if( !CommonsMappingParser.checkReadJok( this, readSeq, readName, refSeq, refSeqLength, start, stop, direction, fileName, lineno ) ) {
                                emtpy++;
                                continue; //continue, and ignore read, if it contains inconsistent information
                            }

                            // parse read
                            //generate read without gaps
                            if( readSeq.contains( "_" ) ) {
                                readwithoutGaps = readSeq.replaceAll( "_+", "" );
                            }
                            else {
                                readwithoutGaps = readSeq;
                            }
                            //Saruman flips only the read string by mapping so we can get the native read direction
                            //                    readwithoutGaps = (direction == -1 ? SequenceUtils.getReverseComplement(readwithoutGaps) : readwithoutGaps);

                            cigar = this.createCigar( readSeq, refSeq );

                            samRecord = new SAMRecord( fileHeader );
                            samRecord.setReadName( readName );

                            samRecord.setReadNegativeStrandFlag( direction != 1 ); //needed or set with flags?
                            samRecord.setReadUnmappedFlag( false ); //only mapped reads in jok??

                            samRecord.setReferenceName( this.refSeqName );
                            samRecord.setAlignmentStart( start );
                            samRecord.setMappingQuality( 255 ); //255 means not available, cannot be retrieved from a jok
                            samRecord.setCigarString( cigar ); //seq of MID... match, insert, del...
                            samRecord.setMateReferenceName( "*" ); //* means that no information about pairs is available, see if you can get this for pairs somewhere
                            samRecord.setMateAlignmentStart( 0 ); //0 means that no information about pairs is available, see if you can get this for pairs somewhere
                            samRecord.setInferredInsertSize( 0 ); //TLEN is set to 0 for single-segment template
                            samRecord.setReadString( readwithoutGaps );
                            samRecord.setBaseQualityString( "*" ); //* means not available, could be retrieved from orig file, but that is expensive

                            //                    samRecord.setFlags(stop); //other fields which could be set
                            //                    samRecord.setAttribute(tag, value);
                            //                    samRecord.setReferenceIndex(lineno);
                            //                    samRecord.setBaseQualityString(name);

                            bamFileWriter.addAlignment( samRecord );
                            //                    samFileWriter.addAlignment(samRecord);

                        }
                        else {
                            this.sendMsgIfAllowed( NbBundle.getMessage( JokToBamConverter.class, "Converter.Convert.MissingData", lineno, line ) );
                        }

                        // Reads with an error already skip this part because of "continue" statements
                        if( ++noReads % 500000 == 0 ) {
                            finish = System.currentTimeMillis();
                            this.notifyObservers( Benchmark.calculateDuration( startTime, finish, lineno + " reads converted..." ) );
                        }
                        counter++;
                    }
                    bamFileWriter.close();
                }
                try( SAMFileReader samFileReader = new SAMFileReader( outputFile ) ) {
                    samFileReader.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
                    SamUtils samUtils = new SamUtils();
                    samUtils.registerObserver( this );
                    success = samUtils.createIndex( samFileReader, new File( outputFile + Properties.BAM_INDEX_EXT ) );
                }

                this.notifyObservers( "Converting track..." );
                finish = System.currentTimeMillis();
                msg = NbBundle.getMessage( JokToBamConverter.class, "Converter.Convert.Finished", outFileName );
                this.notifyObservers( Benchmark.calculateDuration( startTime, finish, msg ) );

            }
            catch( IOException ex ) {
                throw new ParsingException( ex );
            }
        }

        return success;
    }


    /**
     * Creates the cigar string (String of matches and mismatches: 8=2X3N ...)
     * for
     * a given read and reference sequence
     * <p>
     * @param readSeq read sequence
     * @param refSeq  reference sequence to which the read was mapped
     * <p>
     * @return
     */
    private String createCigar( String readSeq, String refSeq ) {
        StringBuilder cigarBuilder = new StringBuilder( readSeq.length() );

        int counter = 0;
        byte lastBase = 0;
        byte currentBase;
        char base;

        /* 0 (M) = alignment match (both, match or mismatch), 1 (I) = insertion,
         * 2 (D) = deletion, 3 (N) = skipped, 4 (S) = soft clipped, 5 (H) = hard clipped,
         * 6 (P) = padding, 7 (=) = sequene match, 8 (X) = sequence mismatch */
        for( int i = 0; i < readSeq.length(); i++ ) {
            if( readSeq.charAt( i ) == refSeq.charAt( i ) ) { //match
                currentBase = 7;
            }
            else {
                if( refSeq.charAt( i ) != '_' ) { //a base in the genome, most frequent case
                    base = readSeq.charAt( i );
                    if( base != '_' ) { //ACGT or N in the read as well
                        currentBase = 8;
                    }
                    else {//a gap in the read
                        currentBase = 2;
                    }
                }
                else {// a gap in genome
                    currentBase = 1;
                }
            }
            if( lastBase != currentBase && i > 0 ) {
                cigarBuilder.append( counter ).append( this.getCigarOpChar( lastBase ) );
                counter = 0;
            }
            ++counter;
            lastBase = currentBase;
        }
        //append cigar operations for last bases
        cigarBuilder.append( counter ).append( this.getCigarOpChar( lastBase ) );

        return cigarBuilder.toString();
    }


    /**
     * Returns the sam format character representing the alignment value at the
     * given base (cigar operation). The cigar values are as follows:
     * 0 (M) = alignment match (both, match or mismatch), 1 (I) = insertion,
     * 2 (D) = deletion, 3 (N) = skipped, 4 (S) = soft clipped, 5 (H) = hard
     * clipped,
     * 6 (P) = padding, 7 (=) = sequene match, 8 (X) = sequence mismatch.
     * If the input value is not among 0-8 an 'N' (3) is returned.
     * <p>
     * @param cigarOp value of a cigar operation, whose char representation is
     *                needed
     * <p>
     * @return the character representation of the cigar operation
     */
    private char getCigarOpChar( byte cigarOp ) {
        char alignmentValue;
        switch( cigarOp ) {
            case 0:
                alignmentValue = 'M';
                break;
            case 1:
                alignmentValue = 'I';
                break;
            case 2:
                alignmentValue = 'D';
                break;
            case 3:
                alignmentValue = 'N';
                break;
            case 4:
                alignmentValue = 'S';
                break;
            case 5:
                alignmentValue = 'H';
                break;
            case 6:
                alignmentValue = 'P';
                break;
            case 7:
                alignmentValue = '=';
                break;
            case 8:
                alignmentValue = 'X';
                break;

            default:
                alignmentValue = 'N';
        }
        return alignmentValue;
    }


    @Override
    public String getInputFileDescription() {
        return fileDescription;
    }


    @Override
    public String getName() {
        return name;
    }


    @Override
    public String[] getFileExtensions() {
        return fileExtension;
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
        }
    }


    @Override
    public void update( Object data ) {
        this.notifyObservers( data ); //until now used to send progress and error data to observers
    }


    /**
     * Method sending the msg to all observers, if the maximum message count is
     * not already reached.
     * <p>
     * @param msg the msg to send (can be an error or any other message).
     */
    @Override
    public void sendMsgIfAllowed( String msg ) {
        if( this.errorLimit.allowOutput() ) {
            this.notifyObservers( msg );
        }
    }


    @Override
    public String toString() {
        return name;
    }


    /**
     * @return The bam file, which has been written by this converter.
     */
    public File getOutputFile() {
        return outputFile;
    }


}
