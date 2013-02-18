package de.cebitec.vamp.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.*;
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
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 * 
 * Contains some utils for sam and bam files.
 */
public class SamUtils implements Observable {
    
    private List<Observer> observers;

    public SamUtils() {
        this.observers = new ArrayList<>();
    }
    
    
    /**
     * Generates a BAM index file from an input BAM file
     *
     * @param reader SAMFileReader for input BAM file
     * @param output  File for output index file
     *
     * @author Martha Borkan
     */
    public void createIndex(SAMFileReader reader, File output) {

        BAMIndexer indexer = new BAMIndexer(output, reader.getFileHeader());
        reader.enableFileSource(true);
        int totalRecords = 0;

        // create and write the content
        SAMRecordIterator samItor = reader.iterator();
        SAMRecord record;
        while (samItor.hasNext()) {
            try {
                record = samItor.next();
                if (++totalRecords % 500000 == 0) {
                    this.notifyObservers(totalRecords + " reads indexed ...");
                }
                indexer.processAlignment(record);
            } catch (RuntimeEOFException e) {
                this.notifyObservers(e);
            } catch (SAMException e) {
                this.notifyObservers("If you tried to create an index on a sam "
                        + "file this is the reason for the exception. Indexes"
                        + "can only be created for bam files!");
                this.notifyObservers(e);
            }
        }
        this.notifyObservers("All " + totalRecords + " reads indexed!");
        indexer.finish();
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }
     
    /* Creates either a sam or a bam file writer depending on the ending of the 
     * oldFile.
     */
    /**
     * Creates a bam file writer. The output file of the new writer is the old file name + the new
     * ending and the appropriate file extension (.sam or .bam).
     * @param oldFile the old file (if data is not stored in a file, just create
     *      a file with a name of your choice
     * @param header the header of the new file
     * @p
     * @param presorted if true, SAMRecords must be added to the SAMFileWriter
     *      in order that agrees with header.sortOrder.
     * @param newEnding the ending is added to the end of the file name of the 
     *      old file
     * @return a pair consisting of: the sam or bam file writer ready for 
     *      writing as the first element and the new file as the second element
     */
    public static Pair<SAMFileWriter, File> createSamBamWriter(File oldFile, SAMFileHeader header, boolean presorted, String newEnding) {

// commented out part: we currently don't allow to write sam files, only bam! (more efficient)
        //String[] nameParts = oldFile.getAbsolutePath().split("\\.");
        //String newFileName;
//        String extension;
//        try {
            //newFileName = nameParts[0];
//            extension = nameParts[nameParts.length - 1];
//        } catch (ArrayIndexOutOfBoundsException e) {
//            extension = "bam";
//        }
        String newFileName = FileUtils.getFilePathWithoutExtension(oldFile);
        SAMFileWriterFactory factory = new SAMFileWriterFactory();
        File outputFile;
//        if (extension.toLowerCase().contains("sam")) {
//            outputFile = new File(newFileName + newEnding + ".sam");
//            return new Pair<>(factory.makeSAMWriter(header, presorted, outputFile), outputFile);
//        } else {
            outputFile = new File(newFileName + newEnding + ".bam");
            return new Pair<>(factory.makeBAMWriter(header, presorted, outputFile), outputFile);
//        }
    }
    
    /**
     * Checks the sort order of the fileToCheck against the sortOrderToCheck and
     * returns true, if the file is sorted according to the sort order handed 
     * over as sortOrderToCheck
     * @param fileToCheck the sam/bam file, whose sort order has to be checked
     * @param sortOrderToCheck the sort order of the file, which is expected/
     * needed
     * @return true, if the sort order of the file equals the given 
     *          sortOrderToCheck
     */
    public static boolean isSortedBy(File fileToCheck, SAMFileHeader.SortOrder sortOrderToCheck) {
        boolean hadToSortCoordinate = false;
        try (SAMFileReader samReader = new SAMFileReader(fileToCheck)) {
            try {
                hadToSortCoordinate = samReader.getFileHeader().getSortOrder().equals(sortOrderToCheck);
            } catch (IllegalArgumentException e) { //if "*" or other weird words were used as sort order we assume the file is unsorted
                hadToSortCoordinate = false;
            }
        }
        return hadToSortCoordinate;
    }
}
