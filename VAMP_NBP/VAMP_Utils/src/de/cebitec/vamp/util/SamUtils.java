package de.cebitec.vamp.util;

import java.io.File;
import java.util.List;
import net.sf.samtools.BAMIndexer;
import net.sf.samtools.SAMFileReader;
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
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 * 
 * Contains some utils for sam and bam files.
 */
public class SamUtils implements Observable {
    
    private List<Observer> observers;
    
    /**
     * Generates a BAM index file from an input BAM file
     *
     * @param reader SAMFileReader for input BAM file
     * @param output  File for output index file
     *
     * @author Martha Borkan
     */
    public void createIndex(SAMFileReader reader, File output, Observer parent) {

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
                    parent.update(totalRecords + " reads indexed ...");
                }
                indexer.processAlignment(record);
            } catch (RuntimeEOFException e) {
                notifyObservers(e);
            }
        }
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
}
