/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMFormatException;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.SAMRecordIterator;


/**
 * Estimates the read length of the first x mappings of a SAM/BAM file.
 * <p>
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class SamReadLengthEstimator implements Observable {

    private final List<Observer> observers;

    /**
     * Estimates the read length of the first x mappings of a SAM/BAM file.
     * <p>
     */
    public SamReadLengthEstimator() {
        this.observers = new ArrayList<>();
    }


    /**
     * Estimates the read length of the first x mappings of a SAM/BAM file.
     * <p>
     * @param file        SAM/BAM file to peek into
     * @param numMappings Number of mappings to check from the given file at
     *                    maximum
     * <p>
     * @return true, if the sorting was successful, false otherwise
     */
    public int estimateReadLength( final File file, int numMappings ) {

        int meanReadLength = -1;

        try( SAMFileReader samBamReader = new SAMFileReader( file ) ) {
            SAMRecordIterator samItor = samBamReader.iterator();
            samBamReader.setValidationStringency( SAMFileReader.ValidationStringency.LENIENT );
            int count = 1;
            int readLengthSum = 0;
            while( samItor.hasNext() && count <= numMappings ) {
                try {
                    SAMRecord record = samItor.next();
                    readLengthSum += record.getReadLength();
                    count++;
                } catch( SAMFormatException e ) {
                    if( !e.getMessage().contains( "MAPQ should be 0" ) ) {
                        this.notifyObservers( e.getMessage() );
                    } //all reads with the "MAPQ should be 0" error are just ordinary unmapped reads and thus ignored
                }
            }
            samItor.close();

            meanReadLength = readLengthSum / count;

        } catch( Exception e ) {
            this.notifyObservers( e.getMessage() );
        }

        return meanReadLength;

    }


    @Override
    public void registerObserver( final Observer observer ) {
        this.observers.add( observer );
    }


    @Override
    public void removeObserver( final Observer observer ) {
        this.observers.remove( observer );
    }


    @Override
    public void notifyObservers( final Object data ) {
        for( Observer observer : this.observers ) {
            observer.update( data );
        }
    }


}
