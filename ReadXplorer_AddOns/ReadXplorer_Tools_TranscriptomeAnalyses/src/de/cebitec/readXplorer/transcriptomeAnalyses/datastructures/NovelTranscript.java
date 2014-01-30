package de.cebitec.readXplorer.transcriptomeAnalyses.datastructures;

import de.cebitec.readXplorer.databackend.dataObjects.TrackChromResultEntry;

/**
 * Novel Transcript defines a region on the reference, which is not determined
 * and annotated yet. This region may contain a novel transcript.
 *
 * @author jritter
 */
public class NovelTranscript extends TrackChromResultEntry {

    /**
     * Direction of novel transcript.
     */
    private boolean isFWD;
    /**
     * <True> if false positive. Set by user.
     */
    private boolean isFalsePositive;
    /**
     * <True> if selected for fasta export. Set by user.
     */
    private boolean isSelectedForFastaExport;
    /**
     * <True> if selected as finally observed in analysis process. Set by user.
     */
    private boolean isConsidered;
    /**
     * Start position of putative transcript start.
     */
    private int start;
    /**
     * Possible end of novel transcript.
     */
    private int dropoff;
    /**
     * Intergenic if novel transcript is determined inbetween two annotated gene
     * features.
     */
    private String site;
    /**
     * Possible Length of novel transcript.
     */
    private int length;
    /**
     * Sequence of novel transcript.
     */
    private String sequence;

    /**
     * Novel Transcript defines a region on the reference, which is not
     * determined and annotated yet. This region may contain a novel transcript.
     *
     * @param isFWD direction of novel transcript, <true> if forward.
     * @param start possible start of novel transcript.
     * @param dropoff possible end of novel transcript.
     * @param site <intergenic> if novel region is between to annotated
     * features. <cis-antisense> if an annotated feature is antisense on the
     * complement strand.
     * @param length possible length of novel transcript.
     * @param sequence sequence representing novel transcript.
     * @param isFP <true> if user set as a false positive determination of novel
     * transcript.
     * @param isSelected <true> if user selects novel transcript for fasta
     * export.
     * @param trackId PersistantTrack ID.
     * @param chromId PersistantChromosome ID.
     */
    public NovelTranscript(boolean isFWD, int start, Integer dropoff, String site, int length, String sequence, boolean isFP, boolean isSelected, int trackId, int chromId) {
        super(trackId, chromId);
        this.isFWD = isFWD;
        this.start = start;
        this.dropoff = dropoff;
        this.site = site;
        this.length = length;
        this.sequence = sequence;
        this.isFalsePositive = isFP;
        this.isSelectedForFastaExport = isSelected;
    }

    /**
     * Gets strand information, on which the novel transcript is located.
     *
     * @return <true> if forward strand else <false>.
     */
    public boolean isFWD() {
        return isFWD;
    }

    /**
     * Gets the possible end of novel transcript.
     *
     * @return end of novel transcript.
     */
    public int getDropOffPos() {
        return this.dropoff;
    }

    /**
     * Gets possible start position of noval transcript.
     *
     * @return
     */
    public int getStartPosition() {
        return this.start;
    }

    /**
     * Gets the site. <intergenic> if novel transcript is between two annotated
     * features. <cis-antisense> if novel transcript is cis-antisense to an
     * annotated feature.
     *
     * @return site.
     */
    public String getSite() {
        return site;
    }

    /**
     * Gets the length of novel transcript.
     *
     * @return length of novel transcript.
     */
    public int getLength() {
        return length;
    }

    /**
     * Returns the sequence, which represents this novel transcript.
     *
     * @return length of novel transcript.
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * Information about the correctness of the detection.
     *
     * @return <true> if it is a false positve detection of a novel region.
     * Default is <false>.
     */
    public boolean isFalsePositive() {
        return isFalsePositive;
    }

    /**
     * Returns <true> if user has selected this novel transcript for fasta
     * export.
     *
     * @return <true> if selected.
     */
    public boolean isSelected() {
        return isSelectedForFastaExport;
    }

    /**
     * Returns <true> if user marked novel transcript as finally considered.
     *
     * @return <true> if marked.
     */
    public boolean isConsidered() {
        return isConsidered;
    }

    /**
     * Set noval transcript as finally considered.
     *
     * @param isConsidered <true> if considered.
     */
    public void setIsConsidered(boolean isConsidered) {
        this.isConsidered = isConsidered;
    }
}
