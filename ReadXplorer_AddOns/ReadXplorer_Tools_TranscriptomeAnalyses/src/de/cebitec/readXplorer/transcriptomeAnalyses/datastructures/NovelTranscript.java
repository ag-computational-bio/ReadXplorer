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
    private final boolean isFwd;
    /**
     * <True> if false positive. Set by user.
     */
    private final boolean isFalsePositiveDetection;
    /**
     * <True> if selected for fasta export. Set by user.
     */
    private final boolean isSelectedForFastaExport;
    /**
     * <True> if selected as finally observed in analysis process. Set by user.
     */
    private boolean isConsidered;
    /**
     * Start position of putative novel transcript.
     */
    private final int start;
    /**
     * Possible end of putaive novel transcript.
     */
    private final int dropoff;
    /**
     * Intergenic if putative novel transcript is determined inbetween two
     * annotated gene features.
     */
    private final String location;
    /**
     * Possible Length of putative novel transcript.
     */
    private final int length;
    /**
     * Sequence of putative novel transcript.
     */
    private final String sequence;
    private boolean isFalsePositive;

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
        this.isFwd = isFWD;
        this.start = start;
        this.dropoff = dropoff;
        this.location = site;
        this.length = length;
        this.sequence = sequence;
        this.isFalsePositiveDetection = isFP;
        this.isSelectedForFastaExport = isSelected;
    }

    /**
     * Gets strand information, on which the novel transcript is located.
     *
     * @return <true> if forward strand else <false>.
     */
    public boolean isFwdDirection() {
        return isFwd;
    }

    /**
     * Returns the possible 3'-end of putative novel transcript.
     *
     * @return end of novel transcript.
     */
    public int getDropOffPos() {
        return this.dropoff;
    }

    /**
     * Returns possible start position of putative noval transcript.
     *
     * @return
     */
    public int getStartPosition() {
        return this.start;
    }

    /**
     * Returns the location of putative novel transcript. <intergenic> if novel
     * transcript is located between two annotated features. <cis-antisense> if
     * novel transcript is located antisense to an annotated feature.
     *
     * @return location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the length of putative novel transcript.
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
     * Information about the correctness of the detection. It can be edit by the
     * user.
     *
     * @return <true> if it is a false positve detection of a novel region.
     * Default is <false>.
     */
    public boolean isFalsePositive() {
        return isFalsePositiveDetection;
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
     * Sets noval transcript as finally considered in analysis process.
     *
     * @param isConsidered <true> if considered.
     */
    public void setIsConsidered(boolean isConsidered) {
        this.isConsidered = isConsidered;
    }

    /**
     *
     * @return <true> if this instance is selected as false positive else
     * <false>
     */
    public boolean isFalsePositiveSelected() {
        return isFalsePositive;
    }

    /**
     *
     * @param isFalsePositive <true> if selected as false positive else <false>
     */
    public void setFalsePositive(boolean isFalsePositive) {
        this.isFalsePositive = isFalsePositive;
    }

}
