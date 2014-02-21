package de.cebitec.readXplorer.tools.snp;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.IntervalRequest;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResultPersistant;
import de.cebitec.readXplorer.databackend.dataObjects.GapCount;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantCoverage;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantDiff;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReferenceGap;
import de.cebitec.readXplorer.databackend.dataObjects.Snp;
import de.cebitec.readXplorer.databackend.dataObjects.SnpI;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceComparison;
import de.cebitec.readXplorer.util.SequenceUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.samtools.util.RuntimeIOException;

/**
 * Carries out the logic behind the SNP and DIP detection.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
class AnalysisSNPs implements Observer, AnalysisI<List<SnpI>> {

    private static final int BASE_A = 0;
    private static final int BASE_C = 1;
    private static final int BASE_G = 2;
    private static final int BASE_T = 3;
    private static final int BASE_N = 4;
    private static final int BASE_GAP = 5;
    
    private TrackConnector trackConnector;
    private ParameterSetSNPs analysisParams;
    private List<SnpI> snps;
    
    
//    private int count = 0;
    private static final int NO_FIELDS = 6;

    /**
     * Carries out the logic behind the SNP and DIP detection.
     * @param trackConnector the track connector for this analysis
     * @param analysisParams the set of parameters for this analysis
     */
    public AnalysisSNPs(TrackConnector trackConnector, ParameterSetSNPs analysisParams) {
        this.trackConnector = trackConnector;
        this.analysisParams = analysisParams;
        this.snps = new ArrayList<>();
    }
    
    /**
     * Updates the SNP and DIP results with the new coverage result.
     * @param data the data to handle: A CoverageAndDiffResultPersistant object
     * containing coverage an diff information for the SNP and DIP analysis
     */
    @Override
    public void update(Object data) {
        CoverageAndDiffResultPersistant covAndDiffs = new CoverageAndDiffResultPersistant(new PersistantCoverage(0, 0), null, null, null);

        if (data.getClass() == covAndDiffs.getClass()) {
            covAndDiffs = (CoverageAndDiffResultPersistant) data;
            this.updateSnpResults(covAndDiffs);
        }
    }

    /**
     * @return The list of SnpIs calculated during this analysis.
     */
    @Override
    public List<SnpI> getResults() {
        return this.snps;
    }
    
    /**
     * @param typeInt value between 0 and 4
     * @return the type of a mismatch (only subs and del) as character
     */
    private SequenceComparison getType(int typeInt) {

        SequenceComparison type = SequenceComparison.UNKNOWN;

        if (typeInt >= 0 && typeInt < 5) {
            type = SequenceComparison.SUBSTITUTION;
        } else if (typeInt == 5) {
            type = SequenceComparison.DELETION;
        } else {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown diff type");
        }

        return type;

    }

    /**
     * @param index The index whose corresponding character is needed.
     * @return The character for a given base index.
     */
    private char getBase(int index) {

        char base = ' ';

        switch (index) {
            case BASE_A : base = 'A'; break;
            case BASE_C : base = 'C'; break;
            case BASE_G : base = 'G'; break;
            case BASE_T : base = 'T'; break;
            case BASE_N : base = 'N';  break;
            case BASE_GAP : base = '_'; break;
            default : 
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found unknown snp type");
        }

        return base;
    }
    
    /**
     * @param base the base whose integer value is needed
     * @return the integer value for the given base type
     */
    private int getBaseInt(char base) {

        int baseInt = 0;
        switch (base) {
            case 'A': baseInt = BASE_A; break;
            case 'C': baseInt = BASE_C; break;
            case 'G': baseInt = BASE_G; break;
            case 'T': baseInt = BASE_T; break;
            case 'N': baseInt = BASE_N; break;
            case '_': baseInt = BASE_GAP; break;
        }

        return baseInt;
    }
    
    /**
     * Creates the count arrays for the SNP/DIP positions and then checks each 
     * position, if it fulfills the given parameters. Only these positions are
     * then stored in the SNP result.
     * @param covAndDiffs the coverage and diff result to handle here
     */
    private void updateSnpResults(CoverageAndDiffResultPersistant covAndDiffs) {
        PersistantCoverage coverage = covAndDiffs.getCoverage();
        List<PersistantDiff> diffs = covAndDiffs.getDiffs();
        List<PersistantReferenceGap> gaps = covAndDiffs.getGaps();
        
        
        //in both arrays the first index is the relative position. The second index is the base index
        int[][] baseArray = new int[coverage.getRightBound() - coverage.getLeftBound() + 1][NO_FIELDS]; //+1 because sequence starts at 1 not 0
        GapCount[] gapCounts = new GapCount[coverage.getRightBound() - coverage.getLeftBound() + 1]; //right bound is excluded in PersistantCoverage
        
        char base;
        int maxBaseIdx;
        for (PersistantDiff diff : diffs) {
            if (diff.getPosition() >= coverage.getLeftBound() && diff.getPosition() < coverage.getRightBound()) {
                base = diff.isForwardStrand() ? diff.getBase() : SequenceUtils.getDnaComplement(diff.getBase());
                maxBaseIdx = this.getBaseInt(base);
                baseArray[diff.getPosition() - coverage.getLeftBound()][maxBaseIdx] += diff.getCount(); //+1 because sequence starts at 1 not 0
            }
        }
        
        int relativeGapPos;
        for (PersistantReferenceGap gap : gaps) {
            if (gap.getPosition() >= coverage.getLeftBound() && gap.getPosition() < coverage.getRightBound()) {
                relativeGapPos = gap.getPosition() - coverage.getLeftBound();
                if (gapCounts[relativeGapPos] == null) {
                    gapCounts[relativeGapPos] = new GapCount();
                }
                gapCounts[relativeGapPos].incCountFor(gap);
            }
        }
        
        IntervalRequest request = covAndDiffs.getRequest();
        String refSubSeq = trackConnector.getRefGenome().getChromSequence(request.getChromId(), request.getFrom(), request.getTo());
        int absPos;
        int[] baseCounts;
        char refBase;
        int maxCount;
        int refBaseIdx;
        int diffCount;
        int largestBaseCount;
        int cov;
        int frequency;
        SequenceComparison snpType;
        try {

            for (int i = 0; i < baseArray.length; ++i) {
                absPos = i + coverage.getLeftBound();
                baseCounts = baseArray[i];

                // i=0..5 is ACGTN_GAP (DIFFS) ...
                diffCount = 0;
                largestBaseCount = 0;
                maxCount = 0;
                maxBaseIdx = 0;
                for (int j = 0; j <= BASE_GAP; j++) {
                    if (maxCount < baseCounts[j]) {
                        maxCount = baseCounts[j];
                        maxBaseIdx = j;
                    }
                    //because only contains diffs, no matches
                    diffCount += baseCounts[j];
                    largestBaseCount = largestBaseCount < baseCounts[j] ? baseCounts[j] : largestBaseCount;
                }

                if (maxCount > 0 && !analysisParams.isUseMainBase() && diffCount >= analysisParams.getMinMismatchingBases()
                        || analysisParams.isUseMainBase() && largestBaseCount >= analysisParams.getMinMismatchingBases()) {
                    cov = coverage.getBestMatchFwdMult(absPos) + coverage.getBestMatchRevMult(absPos);
                    if (cov == 0) {
                        ++cov;
                        Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found uncovered position in diffs: {0}", absPos);
                    }
                    frequency = (diffCount * 100) / cov;


                    if (frequency >= analysisParams.getMinPercentage()) {
                        refBase = refSubSeq.charAt(i); //TODO:SEQ: Test if base is correct base
                        refBaseIdx = getBaseInt(refBase);
                        //determine SNP type, can still be match, if match coverage is largest
                        baseCounts[refBaseIdx] = cov - diffCount;
                        if (maxBaseIdx == refBaseIdx) {
                            continue;//snpType = SequenceComparison.MATCH; base = refBase;
                        } else {
                            snpType = this.getType(maxBaseIdx);
                            base = this.getBase(maxBaseIdx);
                        }

                        this.snps.add(new Snp(
                                absPos,
                                trackConnector.getTrackID(),
                                covAndDiffs.getRequest().getChromId(),
                                base,
                                refBase,
                                baseCounts[BASE_A],
                                baseCounts[BASE_C],
                                baseCounts[BASE_G],
                                baseCounts[BASE_T],
                                baseCounts[BASE_N],
                                baseCounts[BASE_GAP],
                                cov,
                                frequency,
                                snpType));
                    }
                }
            }

            List<int[]> gapOrderList;
            int[] gapCountArray;

            for (int i = 0; i < gapCounts.length; ++i) {
                if (gapCounts[i] != null) {
                    gapOrderList = gapCounts[i].getGapOrderCount();
                    absPos = i + coverage.getLeftBound();

                    for (int j = 0; j < gapOrderList.size(); ++j) {
                        gapCountArray = gapOrderList.get(j);

                        // i=0..5 is ACGTN (DIFFS) ...
                        diffCount = 0;
                        largestBaseCount = 0;
                        maxCount = 0;
                        maxBaseIdx = 0;
                        for (int k = 0; k < BASE_GAP; k++) { //here we only have bases including 'N'
                            if (maxCount < gapCountArray[k]) {
                                maxCount = gapCountArray[k];
                                maxBaseIdx = k;
                            }
                            //because only contains gaps counts, no matches
                            diffCount += gapCountArray[k];
                            largestBaseCount = largestBaseCount < gapCountArray[k] ? gapCountArray[k] : largestBaseCount;
                        }

                        if (!analysisParams.isUseMainBase() && diffCount >= analysisParams.getMinMismatchingBases()
                                || analysisParams.isUseMainBase() && largestBaseCount >= analysisParams.getMinMismatchingBases()) {

                            cov = coverage.getBestMatchFwdMult(absPos) + coverage.getBestMatchRevMult(absPos);
                            if (cov == 0) {
                                ++cov;
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found uncovered position in gaps: {0}", absPos);
                            }
                            frequency = (diffCount * 100) / cov;

                            if (frequency >= analysisParams.getMinPercentage()) {
                                base = this.getBase(maxBaseIdx);

                                this.snps.add(new Snp(
                                        absPos,
                                        trackConnector.getTrackID(),
                                        covAndDiffs.getRequest().getChromId(),
                                        base,
                                        '_',
                                        gapCountArray[BASE_A],
                                        gapCountArray[BASE_C],
                                        gapCountArray[BASE_G],
                                        gapCountArray[BASE_T],
                                        gapCountArray[BASE_N],
                                        0,
                                        cov,
                                        frequency,
                                        SequenceComparison.INSERTION,
                                        j));
                            }
                        }
                    }
                }
            }

//        System.out.println(count++ + "size diffs: " + diffs.size());
//        System.out.println(count++ + "size gaps: " + gaps.size());
        } catch (RuntimeIOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "Could not read data from track file: {0}", trackConnector.getTrackPath());
        }
    }

//    private int getNeighboringCov(int absPos, PersistantCoverage coverage) {
//        int cov1 = 0;
//        int cov2 = 0;
//        
//        cov1 = coverage.getBestMatchFwdMult(absPos) + coverage.getBestMatchRevMult(absPos);
//        if (cov1 == 0) {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found uncovered position in gaps: {0}", absPos);
//        }
//
//        cov2 = coverage.getBestMatchFwdMult(absPos + 1) + coverage.getBestMatchRevMult(absPos + 1);
//
//        int cov = (cov1 + cov2) / 2;
//        return cov == 0 ? 1 : cov;
//    }

}
