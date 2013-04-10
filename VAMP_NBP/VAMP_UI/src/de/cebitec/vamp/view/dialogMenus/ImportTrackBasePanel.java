package de.cebitec.vamp.view.dialogMenus;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.mappings.MappingParserI;
import de.cebitec.vamp.util.VisualisationUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A JPanel offering various methods useful for track import panels. E.g. it
 * allows to get the list of references present in the currently
 * opened VAMP DB. 
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
     * allows to get the list of references present in the currently opened VAMP
     * DB.
     */
    public ImportTrackBasePanel() {
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
     * vamp db. In that case the sam/bam file does not have to be extended
     * anymore, because all needed data is already stored in the file.
     */
    public boolean isAlreadyImported() {
        return isAlreadyImported;
    }

    /**
     * @param isAlreadyImported true, if this direct access track was already
     * imported in another vamp db. In that case the sam/bam file does not have
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
