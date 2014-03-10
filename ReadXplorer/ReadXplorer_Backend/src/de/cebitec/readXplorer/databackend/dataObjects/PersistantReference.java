package de.cebitec.readXplorer.databackend.dataObjects;

import de.cebitec.readXplorer.databackend.SaveFileFetcherForGUI;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.util.Observable;
import de.cebitec.readXplorer.util.Observer;
import java.io.File;
import java.nio.charset.Charset;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sf.picard.PicardException;
import net.sf.picard.reference.IndexedFastaSequenceFile;

/**
 * A persistant reference containing an id, name, description & timestamp of a 
 * reference genome. It also garants access to the active and all other 
 * chromosomes of the reference.
 *
 * @author ddoppmeier, rhilker
 */
public class PersistantReference implements Observable {

    private int id;
    private int activeChromID;
    private String name;
    private String description;
//    private int refLength;
    private Map<Integer, PersistantChromosome> chromosomes;
    private Timestamp timestamp;
    private int noChromosomes;
    private List<Observer> observers;
    private File fastaFile;
    private IndexedFastaSequenceFile seqFile;

    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * @param id The database id of the reference.
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param timestamp The insertion timestamp of the reference.
     * @param fastaFile Fasta file containing the reference sequences
     */
    public PersistantReference(int id, String name, String description, Timestamp timestamp, File fastaFile) {
        this(id, -1, name, description, timestamp, fastaFile);
    }
    
    /**
     * Data holder for a reference genome containing an id, name, description &
     * timestamp of a reference genome. It also garants access to the active and
     * all other chromosomes of the reference.
     * @param id The database id of the reference.
     * @param activeChromId id of the currently active chromosome (>= 0)
     * @param name The name of the reference.
     * @param description The additional description of the reference.
     * @param timestamp The insertion timestamp of the reference.
     * @param fastaFile Fasta file containing the reference sequences
     */
    public PersistantReference(int id, int activeChromId, String name, String description, Timestamp timestamp, File fastaFile) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fastaFile = fastaFile;
        this.chromosomes = ProjectConnector.getInstance().getRefGenomeConnector(id).getChromosomesForGenome();
        this.noChromosomes = this.chromosomes.size();
        this.timestamp = timestamp;
        this.observers = new ArrayList<>();
        this.checkRef(activeChromId);
    }
    
    /**
     * Checks if everything is fine with the reference sequence file. 
     * Appropriate messages are shown, if something is wrong.
     * @param activeChromId id of the currently active chromosome (>= 0)
     */
    private void checkRef(int activeChromId) {
        SaveFileFetcherForGUI fileFetcher = new SaveFileFetcherForGUI();
        try {
            this.seqFile = fileFetcher.checkRefFile(this);
            if (activeChromId < 0) {
                Iterator<PersistantChromosome> chromIt = chromosomes.values().iterator();
                if (chromIt.hasNext()) {
                    activeChromID = chromIt.next().getId();
                }
            } else {
                this.activeChromID = activeChromId;
            }
            try {
                this.getChromSequence(activeChromID, 1, 1);
            } catch (PicardException | NullPointerException e) {
                if (e.getMessage() != null && e.getMessage().contains("Unable to find entry for contig")) {
                    String msg = "The fasta file \n" + fastaFile.getAbsolutePath()
                            + "\ndoes not contain the expected sequence:\n" + e.getMessage();
                    JOptionPane.showMessageDialog(new JPanel(), msg, "Sequence missing error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SaveFileFetcherForGUI.UserCanceledTrackPathUpdateException ex) {
            String msg = "If the missing fasta file is not replaced, the reference cannot be shown and analyses for this reference cannot be run.";
            JOptionPane.showMessageDialog(new JPanel(), msg, "Sequence missing error", JOptionPane.ERROR_MESSAGE);
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
     * @return The chromosome of interest.
     */
    public PersistantChromosome getChromosome(int chromId) {
        return this.chromosomes.get(chromId);
        }
    
    /**
     * @return The currently active chromosome of this reference.
     */
    public PersistantChromosome getActiveChromosome() {
        return this.chromosomes.get(this.getActiveChromId());
    }

    /**
     * @param chromId the chromosome id of interest
     * @param start Start position of the desired sequence
     * @param stop Stop position of the desired sequence
     * @return The wanted part of the chromosome sequence for the given 
     * chromosome id.
     */
    public String getChromSequence(int chromId, int start, int stop) {
        String refSubSeq = new String(seqFile.getSubsequenceAt(chromosomes.get(chromId).getName(), start, stop).getBases(), Charset.forName("UTF-8"));
        return refSubSeq.toUpperCase();
    }
    
    /**
     * @param start Start position of the desired sequence
     * @param stop Stop position of the desired sequence
     * @return The wanted part of the active chromosome sequence for the given
     * chromosome id.
     */
    public String getActiveChromSequence(int start, int stop) {
        return this.getChromSequence(this.activeChromID, start, stop);
    }

    /**
     * @return The chromosome sequence for the given chromosome id.
     */
    public int getActiveChromLength() {
        return this.chromosomes.get(this.getActiveChromId()).getLength();
    }
    
    /**
     * @return The map of all chromosomes of this reference genome.
     */
    public Map<Integer, PersistantChromosome> getChromosomes() {
        return this.chromosomes;
    }

    /**
     * @return The insertion timestamp of the reference.
     */
    public Timestamp getTimeStamp(){
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
    public String toString(){
        return this.getName();
    }
    
    /* 
     * Need this to use PersistantReference class as key for HashMap 
     * @see http://stackoverflow.com/questions/27581/overriding-equals-and-hashcode-in-java
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
       return id;
   }
    
    /**
     * Checks if the given reference genome is equal to this one.
     * @param o object to compare to this object
     */
    @Override
    public boolean equals(Object o) {

        if (o instanceof PersistantReference) {
            PersistantReference ogenome = (PersistantReference) o;
            return (ogenome.getDescription().equals(this.description)
                    && ogenome.getName().equals(this.name)
                    && (ogenome.getId() == this.id)
                    && (ogenome.getNoChromosomes() == this.getNoChromosomes())
                    && ogenome.getTimeStamp().equals(this.timestamp) //for current purposes we do not need to compare the sequence, 
                    //in most cases the id should be enough
                    //&& ogenome.getSequence().equals(this.sequence); 
                    );
        } else {
            return super.equals(o);
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
    public void setActiveChromId(int chromId) {
        if (activeChromID != chromId) {
            this.activeChromID = chromId;
            this.notifyObservers(this.activeChromID);
        }
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
    
    /**
     * Utility method for calculating the whole genome length. It is generated
     * by adding the length of each available chromosome.
     * @param chromosomeMap the map containing all chromosomes, for which the
     * whole genome length shall be calculated.
     * @return The whole genome length. It is generated by adding the length of
     * each available chromosome.
     */
    public static int calcWholeGenomeLength(Map<Integer, PersistantChromosome> chromosomeMap) {
        int wholeGenomeLength = 0;
        for (PersistantChromosome chrom : chromosomeMap.values()) {
            wholeGenomeLength += chrom.getLength();
        }
        return wholeGenomeLength;
    }

    /**
     * USe this method only, if the path to the fasta file, beloning to this
     * reference has to be replaced.
     * @param newFastaFile The new fasta file
     */
    public void resetFastaPath(File newFastaFile) {
        this.fastaFile = newFastaFile;
    }

}
