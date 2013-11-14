package de.cebitec.readXplorer.view.dialogMenus;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.mappings.MappingParserI;
import de.cebitec.readXplorer.util.VisualisationUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.samtools.SAMFileHeader;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMSequenceDictionary;
import net.sf.samtools.SAMSequenceRecord;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * A JPanel offering various methods useful for track import panels. E.g. it
 * allows to get the list of references present in the currently
 * opened ReadXplorer DB. 
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class ImportTrackBasePanel extends FileSelectionPanel {
    
    private static final long serialVersionUID = 1L;
    
    private boolean isAlreadyImported = false;
    private boolean isDbUsed = false;
    private MappingParserI currentParser;

    /**
     * A JPanel offering various methods useful for track import panels. E.g. it
     * allows to get the list of references present in the currently opened ReadXplorer
     * DB.
     */
    public ImportTrackBasePanel() {
    }
    
    /**
     * Checks a given sam/bam file for the currently selected reference and 
     * displays an error message in case the selected reference does not occur
     * in the sequence dictionary of the sam/bam file.
     * @param smaBamFile the sam/bam file to check for the reference 
     */
    public void checkSeqDictonary(File smaBamFile) {
        try (SAMFileReader samReader = new SAMFileReader(smaBamFile)) {
            SAMFileHeader header = samReader.getFileHeader();
            SAMSequenceRecord refSeq = header.getSequenceDictionary().getSequence(this.getReferenceJob().getName());
            if (refSeq == null) {
                String msg = NbBundle.getMessage(ImportTrackBasePanel.class, "MSG_ErrorReference",
                        this.getReferenceJob().getName(),
                        smaBamFile.getAbsolutePath(),
                        this.createRefDictionaryString(header.getSequenceDictionary()));
                NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                nd.setTitle(NbBundle.getMessage(ImportTrackBasePanel.class, "TITLE_ErrorReference"));
                DialogDisplayer.getDefault().notify(nd); //TODO: add to is required info set!
            }
        }
    }

    /**
     * Creates a concatenated string of all reference names contained in the 
     * given sequence dictionary. All names are separated by a new line (\n).
     * @param sequenceDictionary The sequence dictionary whose reference names
     * should be concatenated
     * @return The concatenated reference names
     */
    private String createRefDictionaryString(SAMSequenceDictionary sequenceDictionary) {
        StringBuilder concatenatedDictionary = new StringBuilder(100);
        concatenatedDictionary.append("\n");
        for (SAMSequenceRecord ref : sequenceDictionary.getSequences()) {
            concatenatedDictionary.append(ref.getSequenceName()).append("\n");
        }
        return concatenatedDictionary.toString();
    }
    
    /**
     * @param jobs list of reference jobs which shall be imported now and thus
     * have to be available for the import of new tracks too.
     * @return Complete list of reference jobs in the db and which are imported now
     */
    public ReferenceJob[] getReferenceJobs(List<ReferenceJob> jobs) {
        List<ReferenceJob> refJobList = this.getRefJobList();

        refJobList.addAll(jobs);

        ReferenceJob[] references = new ReferenceJob[1];
        references = refJobList.toArray(references);
        
        return references;
    }

    /**
     * @return all reference genomes which are stored in the db until now.
     */
    public ReferenceJob[] getReferenceJobs() {
        List<ReferenceJob> refJobList = this.getRefJobList();
        
        ReferenceJob[] references = new ReferenceJob[1];
        references = refJobList.toArray(references);

        return references;
    }
    
    /**
     * @return the list of reference jobs stored in the current db
     */
    private List<ReferenceJob> getRefJobList() {
        List<ReferenceJob> refJobList = new ArrayList<>();

        try {
            List<PersistantReference> dbGens = ProjectConnector.getInstance().getGenomes();
            for (Iterator<PersistantReference> it = dbGens.iterator(); it.hasNext();) {
                PersistantReference r = it.next();
                refJobList.add(new ReferenceJob(r.getId(), null, null, r.getDescription(), r.getName(), r.getTimeStamp()));
            }
        } catch (OutOfMemoryError e) {
            VisualisationUtils.displayOutOfMemoryError(this);
        }
        return refJobList;
    }

    /**
     * @return true, if this direct access track was already imported in another
     * readXplorer db. In that case the sam/bam file does not have to be extended
     * anymore, because all needed data is already stored in the file.
     */
    public boolean isAlreadyImported() {
        return isAlreadyImported;
    }

    /**
     * @param isAlreadyImported true, if this direct access track was already
     * imported in another readXplorer db. In that case the sam/bam file does not have
     * to be extended anymore, because all needed data is already stored in the
     * file.
     */
    protected void setIsAlreadyImported(boolean isAlreadyImported) {
        this.isAlreadyImported = isAlreadyImported;
    }
    

    /**
     * @return The parser, which shall be used for parsing this track job.
     */
    public MappingParserI getCurrentParser() {
        return currentParser;
    }

    /**
     * @param currentParser  The parser, which shall be used for parsing this 
     * track job.
     */
    protected void setCurrentParser(MappingParserI currentParser) {
        this.currentParser = currentParser;
    }

    /**
     * @return true, if the track should be stored into the database and false,
     * if direct file access is desired
     */
    public boolean isDbUsed() {
        return isDbUsed;
    }

    protected void setIsDbUsed(boolean isDbUsed) {
        this.isDbUsed = isDbUsed;
    }
    
    /**
     * @return The reference job selected as reference for this track.
     */
    public abstract ReferenceJob getReferenceJob();
    
    /**
     * @return The track name for this track.
     */
    public abstract String getTrackName();
    
}
