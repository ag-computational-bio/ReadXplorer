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

package de.cebitec.readxplorer.utils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.Cigar;
import net.sf.samtools.CigarElement;
import net.sf.samtools.SAMException;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFileWriter;
import net.sf.samtools.SAMFileWriterFactory;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;
import net.sf.samtools.util.RuntimeEOFException;

/*
 * The MIT License
 *
 * Copyright (c) 2010 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

/**
 * Contains some utils for sam and bam files.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SamUtils implements Observable {

    public static final String SORT_PREFIX = "_sort";
    public static final String SORT_READSEQ_STRING = SORT_PREFIX + "_readSequence";
    public static final String SORT_READNAME_STRING = SORT_PREFIX + "_readName";
    public static final String EXTENDED_STRING = "_extended";
    public static final String COMBINED_STRING = "_combined";

    private final List<Observer> observers;


    /**
     * Contains some utils for sam and bam files.
     */
    public SamUtils() {
        this.observers = new ArrayList<>();
    }


    /**
     * Generates a BAM index file from an input BAM file.
     * <p>
     * @param reader SAMFileReader for input BAM file
     * @param output File for output index file
     * <p>
     * @return true, if the index creation succeeded, false otherwise
     * <p>
     * @author Martha Borkan, rhilker
     */
    public boolean createIndex( SAMFileReader reader, File output ) {

        boolean success = true;
        BAMIndexer indexer = new BAMIndexer( output, reader.getFileHeader() );
        reader.enableFileSource( true );
        int totalRecords = 0;

        try {
            // create and write the content
            SAMRecordIterator samItor = reader.iterator();
            SAMRecord record;
            while( samItor.hasNext() ) {
                try {
                    record = samItor.next();
                    if( ++totalRecords % 500000 == 0 ) {
                        this.notifyObservers( totalRecords + " reads indexed ..." );
                    }
                    indexer.processAlignment( record );
                }
                catch( RuntimeEOFException e ) {
                    this.notifyObservers( e );
                }
                catch( SAMFormatException e ) {
                    if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                        this.notifyObservers( e.getMessage() );
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                }
            }
            samItor.close();
            this.notifyObservers( "All " + totalRecords + " reads indexed!" );
        }
        catch( SAMException e ) {
            this.notifyObservers( "If you tried to create an index on a sam "
                                  + "file this is the reason for the exception. Indexes"
                                  + "can only be created for bam files!" );
            this.notifyObservers( e );
            success = false;
        }
        indexer.finish();
        return success;
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

    /* Creates either a sam or a bam file writer depending on the ending of the
     * oldFile.
     */

    /**
     * Creates a bam file writer. The output file of the new writer is the old
     * file name + the new
     * ending and the appropriate file extension (.sam or .bam).
     * <p>
     * @param oldFile   the old file (if data is not stored in a file, just
     *                  create
     *                  a file with a name of your choice
     * @param header    the header of the new file
     * @param presorted if true, SAMRecords must be added to the SAMFileWriter
     *                  in order that agrees with header.sortOrder.
     * @param newEnding the ending is added to the end of the file name of the
     *                  old file (this is not the file extension)
     * <p>
     * @return a pair consisting of: the sam or bam file writer ready for
     *         writing as the first element and the new file as the second element
     */
    public static Pair<SAMFileWriter, File> createSamBamWriter( File oldFile, SAMFileHeader header, boolean presorted, String newEnding ) {

// commented out part: we currently don't allow to write sam files, only bam! (more efficient)
//        String extension;
//        try {
//            extension = nameParts[nameParts.length - 1];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            extension = "bam";
//        }
        String newFileName = FileUtils.getFilePathWithoutExtension( oldFile );
        SAMFileWriterFactory factory = new SAMFileWriterFactory();
//        if (extension.toLowerCase().contains("sam")) {
//            outputFile = new File(newFileName + newEnding + ".sam");
//            return new Pair<>(factory.makeSAMWriter(header, presorted, outputFile), outputFile);
//        } else {
        File outputFile = SamUtils.getFileWithBamExtension( oldFile, newEnding );
        return new Pair<>( factory.makeBAMWriter( header, presorted, outputFile ), outputFile );
//        }
    }


    /**
     *
     * @param inputFile the input file whose extension should be changed
     * @param newEnding the ending is added to the end of the file name of the
     *                  old file (this is not the file extension)
     * <p>
     * @return a new bam file, which does not already exist with the given new
     *         ending
     */
    public static File getFileWithBamExtension( File inputFile, String newEnding ) {
        String[] nameParts = inputFile.getAbsolutePath().split( "\\." );
        String newFilePath = "";
        for( int i = 0; i < nameParts.length - 1; ++i ) { //only remove old file extension
            newFilePath = newFilePath + nameParts[i] + ".";
        }
        if( !newFilePath.isEmpty() ) {
            newFilePath = newFilePath.substring( 0, newFilePath.length() - 1 );
            newFilePath = SamUtils.removeReadXplorerFileEndings( SORT_READNAME_STRING, newFilePath );
            newFilePath = SamUtils.removeReadXplorerFileEndings( SORT_READSEQ_STRING, newFilePath );
            newFilePath = SamUtils.removeReadXplorerFileEndings( EXTENDED_STRING, newFilePath );
        }
        else {
            newFilePath = inputFile.getAbsolutePath();
        }
        File newFile = new File( newFilePath + newEnding + ".bam" );
        while( newFile.exists() ) {
            newEnding = newEnding.concat( "-readXplorer" );
            newFile = new File( newFilePath + newEnding + ".bam" );
        }
        return newFile;
    }


    /**
     * Removes a file ending used by ReadXplorer from the end of a file name.
     * Note:
     * This is not the file extension!
     * <p>
     * @param fileEnding the file ending to remove
     * @param filePath   the file path to chech for the ending
     * <p>
     * @return the new file path without the given ending
     */
    private static String removeReadXplorerFileEndings( String fileEnding, String filePath ) {
        if( filePath.endsWith( fileEnding ) ) {
            filePath = filePath.substring( 0, filePath.length() - fileEnding.length() );
        }
        return filePath;
    }


    /**
     * Checks the sort order of the fileToCheck against the sortOrderToCheck and
     * returns true, if the file is sorted according to the sort order handed
     * over as sortOrderToCheck
     * <p>
     * @param fileToCheck      the sam/bam file, whose sort order has to be
     *                         checked
     * @param sortOrderToCheck the sort order of the file, which is expected/
     *                         needed
     * <p>
     * @return true, if the sort order of the file equals the given
     *         sortOrderToCheck
     */
    public static boolean isSortedBy( File fileToCheck, SAMFileHeader.SortOrder sortOrderToCheck ) {
        try( SAMFileReader samReader = new SAMFileReader( fileToCheck ) ) {
            try {
                return samReader.getFileHeader().getSortOrder().equals( sortOrderToCheck );
            }
            catch( IllegalArgumentException e ) { //if "*" or other weird words were used as sort order we assume the file is unsorted
                return false;
            }
        }
    }


    /**
     * Returns blocks of the read sequence that have been aligned directly to
     * the
     * reference sequence. Note that clipped portions of the read and inserted
     * and
     * deleted bases (vs. the reference) are not represented in the alignment
     * blocks.
     * <p>
     * @param cigar
     * @param refStartPos
     *                    <p>
     * @return
     */
    public List<SamAlignmentBlock> getAlignmentBlocks( Cigar cigar, int refStartPos ) {

        final List<SamAlignmentBlock> alignmentBlocks = new ArrayList<>();
        int start = refStartPos;
        int stop = refStartPos - 1;
        boolean fstElement = true;

        for( final CigarElement e : cigar.getCigarElements() ) {
            switch( e.getOperator() ) {
                case H:
                    break; //ignore hard clipped bases
                case P:
                    break; //ignore padded bases
                case S:
                    break; //soft clipped read bases
                case D:
                    stop += e.getLength();
                    break; //dels
                case I:
                    break; //insertions
                case N: //skipped reference bases are excluded
                    if( !fstElement ) {
                        alignmentBlocks.add( new SamAlignmentBlock( start, stop ) );
                    }
                    stop += e.getLength();
                    start = stop + 1;
                    break;
                case M: //match or mismatch
                case EQ:
                case X: //mismatch
                    stop += e.getLength();
                    fstElement = false;
                    break;
                default:
                    throw new IllegalStateException( "Case statement encountered unknown cigar op: " + e.getOperator() );
            }
        }
        if( start < stop ) {
            alignmentBlocks.add( new SamAlignmentBlock( start, stop ) );
        }

        return alignmentBlocks;
    }


}
