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

package de.cebitec.readxplorer.databackend.dataobjects;


import de.cebitec.readxplorer.api.FileException;
import de.cebitec.readxplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.errorhandling.ErrorHelper;
import htsjdk.samtools.SAMException;
import htsjdk.samtools.reference.IndexedFastaSequenceFile;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A persistent reference containing an id, name, description & timestamp of a
 * reference genome. It also garants access to the active and all other
 * chromosomes of the reference.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistentReference implements Observable {

    private static final Logger LOG = LoggerFactory.getLogger( PersistentReference.class.getName() );
    
    private int id;
    private int activeChromID;
    private String name;
    private String description;
    private int genomeLength = 0;
    private final Map<Integer, PersistentChromosome> chromosomes;
    private Timestamp timestamp;
    private int noChromosomes;
    private List<Observer> observers;
    private File fastaFile;
    private IndexedFastaSequenceFile seqFile;


    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * <p>
     * @param id          The database id of the reference.
     * @param name        The name of the reference.
     * @param description The additional description of the reference.
     * @param timestamp   The insertion timestamp of the reference.
     * @param fastaFile   Fasta file containing the reference sequences
     */
    public PersistentReference( int id, String name, String description, Timestamp timestamp, File fastaFile ) {
        this( id, -1, name, description, timestamp, fastaFile );
    }


    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * <p>
     * @param id            The database id of the reference.
     * @param activeChromId id of the currently active chromosome (>= 0)
     * @param name          The name of the reference.
     * @param description   The additional description of the reference.
     * @param timestamp     The insertion timestamp of the reference.
     * @param fastaFile     Fasta file containing the reference sequences
     */
    public PersistentReference( int id, int activeChromId, String name, String description, Timestamp timestamp, File fastaFile ) {
        this( id, -1, name, description, timestamp, fastaFile, true );
    }


    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * <p>
     * @param id            The database id of the reference.
     * @param activeChromId id of the currently active chromosome (>= 0)
     * @param name          The name of the reference.
     * @param description   The additional description of the reference.
     * @param timestamp     The insertion timestamp of the reference.
     * @param fastaFile     Fasta file containing the reference sequences
     * @param checkFile     true, if the reference file shall be checked for
     *                      validitiy, false otherwise
     */
    public PersistentReference( int id, int activeChromId, String name, String description, Timestamp timestamp,
                                File fastaFile,
                                boolean checkFile ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fastaFile = fastaFile;
        chromosomes = ProjectConnector.getInstance().getRefGenomeConnector( id ).getChromosomesForGenome();
        noChromosomes = chromosomes.size();
        this.timestamp = timestamp;
        observers = new ArrayList<>( 10 );
        if( checkFile ) {
            this.checkRef( activeChromId );
        }
    }


    /**
     * Checks if everything is fine with the reference sequence file.
     * Appropriate messages are shown, if something is wrong.
     * <p>
     * @param activeChromId id of the currently active chromosome (>= 0)
     */
    private void checkRef( int activeChromId ) {
        SaveFileFetcherForGUI fileFetcher = new SaveFileFetcherForGUI();
        try {
            this.seqFile = fileFetcher.checkRefFile( this );
            if( activeChromId < 0 ) {
                Iterator<PersistentChromosome> chromIt = chromosomes.values().iterator();
                if( chromIt.hasNext() ) {
                    activeChromID = chromIt.next().getId();
                }
            } else {
                this.activeChromID = activeChromId;
            }
            try {
                this.getChromSequence( activeChromID, 1, 1 );
            } catch( NullPointerException e ) {
                if( e.getMessage() != null && e.getMessage().contains( "Unable to find entry for contig" ) ) {
                    String msg = "The fasta file \n" + fastaFile.getAbsolutePath() +
                                 "\ndoes not contain the expected sequence:\n" + e.getMessage();
                    ErrorHelper.getHandler().handle( new NullPointerException( msg ), "Sequence missing error" );
                    LOG.error( msg, e );
                }
            }
        } catch( SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex ) {
            String msg = "If the missing fasta file is not replaced, the reference cannot be shown and analyses for this reference cannot be run.";
            ErrorHelper.getHandler().handle( new FileException( msg, ex ), "Sequence missing error" );
            LOG.error( msg, ex );
        }
    }


    /**
     * @return The additional description of the reference.
     */
    public String getDescription() {
        return description;
    }


    /**
     * @return The database id of the reference.
     */
    public int getId() {
        return id;
    }


    /**
     * @return The name of the reference.
     */
    public String getName() {
        return name;
    }


    /**
     * @param chromId the id of the chromosome of interest
     * <p>
     * @return The chromosome of interest.
     */
    public PersistentChromosome getChromosome( int chromId ) {
        return this.chromosomes.get( chromId );
    }


    /**
     * @return The currently active chromosome of this reference.
     */
    public PersistentChromosome getActiveChromosome() {
        return this.chromosomes.get( this.getActiveChromId() );
    }


    /**
     * @param chromId the chromosome id of interest
     * @param start   Start position of the desired sequence
     * @param stop    Stop position of the desired sequence
     * <p>
     * @return The wanted part of the chromosome sequence for the given
     *         chromosome id.
     */
    public String getChromSequence( int chromId, int start, int stop ) {
        String refSubSeq = new String( seqFile.getSubsequenceAt( chromosomes.get( chromId ).getName(), start, stop ).getBases(), Charset.forName( "UTF-8" ) );
        return refSubSeq.toUpperCase();
    }


    /**
     * @param start Start position of the desired sequence
     * @param stop  Stop position of the desired sequence
     * <p>
     * @return The wanted part of the active chromosome sequence for the given
     *         chromosome id.
     */
    public String getActiveChromSequence( int start, int stop ) {
        return this.getChromSequence( this.activeChromID, start, stop );
    }


    /**
     * @return The chromosome sequence for the given chromosome id.
     */
    public int getActiveChromLength() {
        return this.chromosomes.get( this.getActiveChromId() ).getLength();
    }


    /**
     * @return The map of all chromosomes of this reference genome.
     */
    public Map<Integer, PersistentChromosome> getChromosomes() {
        return Collections.unmodifiableMap( chromosomes );
    }


    /**
     * Calculates & returns the genome length = Sum of the length of all
     * chromosomes belonging to this reference. Calculation is only carried out
     * the first time this method is called on this reference.
     * <p>
     * @return Genome length
     */
    public int getGenomeLength() {
        if( genomeLength == 0 ) {
            for( PersistentChromosome chrom : chromosomes.values() ) {
                genomeLength += chrom.getLength();
            }
        }
        return genomeLength;
    }


    /**
     * @return The insertion timestamp of the reference.
     */
    public Timestamp getTimeStamp() {
        return timestamp;
    }


    /**
     * @return Fasta file containing the reference sequences.
     */
    public File getFastaFile() {
        return fastaFile;
    }


    /**
     * @return The name of the reference.
     */
    @Override
    public String toString() {
        return this.getName();
    }


    /*
     * Need this to use PersistentReference class as key for HashMap @see
     * http://stackoverflow.com/questions/27581/overriding-equals-and-hashcode-in-java
     * (non-Javadoc) @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return id;
    }


    /**
     * Checks if the given reference genome is equal to this one.
     * <p>
     * @param o object to compare to this object
     */
    @Override
    public boolean equals( Object o ) {

        if( o instanceof PersistentReference ) {
            PersistentReference ogenome = (PersistentReference) o;
            return (ogenome.getDescription().equals( this.description ) &&
                    ogenome.getName().equals( this.name ) &&
                    (ogenome.getId() == this.id) &&
                    (ogenome.getNoChromosomes() == this.getNoChromosomes()) &&
                    ogenome.getTimeStamp().equals( this.timestamp ) //for current purposes we do not need to compare the sequence,
                    //in most cases the id should be enough
                    //&& ogenome.getSequence().equals(this.sequence);
                    );
        } else {
            return super.equals( o );
        }
    }


    /**
     * @return The number of chromosomes of this reference genome.
     */
    public int getNoChromosomes() {
        return noChromosomes;
    }


    /**
     * @return The id of the currently active chromosome
     */
    public int getActiveChromId() {
        return activeChromID;
    }


    /**
     * @param chromId The id of the currently active chromosome
     */
    public void setActiveChromId( int chromId ) {
        if( activeChromID != chromId ) {
            this.activeChromID = chromId;
            this.notifyObservers( this.activeChromID );
        }
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


    /**
     * Utility method for calculating the whole genome length. It is generated
     * by adding the length of each available chromosome.
     * <p>
     * @param chromosomeMap the map containing all chromosomes, for which the
     *                      whole genome length shall be calculated.
     * <p>
     * @return The whole genome length. It is generated by adding the length of
     *         each available chromosome.
     */
    public static int calcWholeGenomeLength( Map<Integer, PersistentChromosome> chromosomeMap ) {
        int wholeGenomeLength = 0;
        for( PersistentChromosome chrom : chromosomeMap.values() ) {
            wholeGenomeLength += chrom.getLength();
        }
        return wholeGenomeLength;
    }


    /**
     * USe this method only, if the path to the fasta file, beloning to this
     * reference has to be replaced.
     * <p>
     * @param newFastaFile The new fasta file
     */
    public void resetFastaPath( File newFastaFile ) {
        this.fastaFile = newFastaFile;
    }


}
