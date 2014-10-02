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
package de.cebitec.readXplorer.tools.snp;

import de.cebitec.readXplorer.api.objects.AnalysisI;
import de.cebitec.readXplorer.databackend.IntervalRequest;
import de.cebitec.readXplorer.databackend.SamBamFileReader;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageAndDiffResult;
import de.cebitec.readXplorer.databackend.dataObjects.CoverageManager;
import de.cebitec.readXplorer.databackend.dataObjects.Difference;
import de.cebitec.readXplorer.databackend.dataObjects.GapCount;
import de.cebitec.readXplorer.databackend.dataObjects.ReferenceGap;
import de.cebitec.readXplorer.databackend.dataObjects.Snp;
import de.cebitec.readXplorer.databackend.dataObjects.SnpI;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.util.SequenceComparison;
import de.cebitec.readXplorer.util.SequenceUtils;
import de.cebitec.readXplorer.util.classification.Classification;
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
    private List<Classification> excludedClasses;
    private List<SnpI> snps;
    private boolean hasBaseQualities = true;
    private boolean hasMappingQualities = true;
    
    
//    private int count = 0;
    /** 6 = Number of supported bases: A,C,G,T,N,_. */
    private static final int NO_BASES = 6;
    /** 3 = Three values are needed: the count, the base  quality and the mapping quality. */
    private static final int NO_VALUES = 3;
    /** 0 = index for the count of a diff in all mappings at that position. */
    private static final int COUNT_IDX = 0;
    /** 1 = index for the average base quality of a diff base in all mappings for that position. */
    private static final int BASE_QUAL_IDX = 1;
    /** 2 = index for the average mapping quality of a diff base in all mappings for that position. */
    private static final int MAP_QUAL_IDX = 2;

    /**
     * Carries out the logic behind the SNP and DIP detection.
     * @param trackConnector the track connector for this analysis
     * @param analysisParams the set of parameters for this analysis
     */
    public AnalysisSNPs(TrackConnector trackConnector, ParameterSetSNPs analysisParams) {
        this.trackConnector = trackConnector;
        this.analysisParams = analysisParams;
        this.excludedClasses = analysisParams.getReadClassParams().getExcludedClasses();
        this.snps = new ArrayList<>();
    }
    
    /**
     * Updates the SNP and DIP results with the new coverage result.
     * @param data the data to handle: A CoverageAndDiffResultPersistent object
     * containing coverage an diff information for the SNP and DIP analysis
     */
    @Override
    public void update(Object data) {
        CoverageAndDiffResult covAndDiffs = new CoverageAndDiffResult(new CoverageManager(0, 0), null, null, null);

        if (data.getClass() == covAndDiffs.getClass()) {
            covAndDiffs = (CoverageAndDiffResult) data;
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
     * @return <cc>true</cc> if all mappings have base qualities, <cc>false</cc>
     * if at least one mapping does not have base qualities.
     */
    public boolean isHasBaseQualities() {
        return hasBaseQualities;
    }

    /**
     * @return <cc>true</cc> if all mappings have mapping qualities including
     * 255, <cc>false</cc> if at least one mapping does not have a mapping
     * quality.
     */
    public boolean isHasMappingQualities() {
        return hasMappingQualities;
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
            case BASE_GAP : base = '-'; break;
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
            case '-': baseInt = BASE_GAP; break;
        }

        return baseInt;
    }
    
    /**
     * Creates the count arrays for the SNP/DIP positions and then checks each 
     * position, if it fulfills the given parameters. Only these positions are
     * then stored in the SNP result.
     * @param covAndDiffs the coverage and diff result to handle here
     */
    private void updateSnpResults(CoverageAndDiffResult covAndDiffs) {
        CoverageManager coverage = covAndDiffs.getCovManager();
        List<Difference> diffs = covAndDiffs.getDiffs();
        List<ReferenceGap> gaps = covAndDiffs.getGaps();
        
        
        //in base array the first index is the relative position. The second aindex is the base index, the third contains count (0) and average base quality (1)
        int[][][] baseArray = new int[coverage.getRightBound() - coverage.getLeftBound() + 1][NO_BASES][NO_VALUES]; //+1 because sequence starts at 1 not 0
        GapCount[] gapCounts = new GapCount[coverage.getRightBound() - coverage.getLeftBound() + 1]; //right bound is excluded in CoverageManager
        
        char base;
        int maxBaseIdx;
        int pos;
        for (Difference diff : diffs) {
            if (diff.getPosition() >= coverage.getLeftBound() && diff.getPosition() < coverage.getRightBound() && 
                    (diff.getBaseQuality() > analysisParams.getMinBaseQuality() || diff.getBaseQuality() == -1)) {
                base = diff.isForwardStrand() ? diff.getBase() : SequenceUtils.getDnaComplement(diff.getBase());
                maxBaseIdx = this.getBaseInt(base);
                pos = diff.getPosition() - coverage.getLeftBound();
                baseArray[pos][maxBaseIdx][COUNT_IDX] += diff.getCount(); //+1 because sequence starts at 1 not 0
                if (diff.getBaseQuality() > -1) {
                    baseArray[pos][maxBaseIdx][BASE_QUAL_IDX] += diff.getBaseQuality(); //can be -1 if unknown
                } else {
                    hasBaseQualities = false;
                }
                if (    diff.getMappingQuality() != SamBamFileReader.UNKNOWN_MAP_QUAL
                     || diff.getMappingQuality() != SamBamFileReader.UNKNOWN_CALCULATED_MAP_QUAL) {
                        baseArray[pos][maxBaseIdx][MAP_QUAL_IDX] += diff.getMappingQuality();
                } else {
                    hasMappingQualities = false; //TODO: check before analysis for a track, if it supports base and mapping qualities - could lead to errors when stored in DB or takes a while
                }
            }
        }
        
        int relativeGapPos;
        for (ReferenceGap gap : gaps) {
            if (gap.getPosition() >= coverage.getLeftBound() && gap.getPosition() < coverage.getRightBound() &&
                    (gap.getBaseQuality() == -1 || gap.getBaseQuality() > analysisParams.getMinBaseQuality())) {
                relativeGapPos = gap.getPosition() - coverage.getLeftBound();
                if (gapCounts[relativeGapPos] == null) {
                    gapCounts[relativeGapPos] = new GapCount();
                }
                gapCounts[relativeGapPos].incCountFor(gap);
                if (gap.getBaseQuality() <= -1) {
                    hasBaseQualities = false;
                }
                if (   gap.getMappingQuality() == SamBamFileReader.UNKNOWN_MAP_QUAL
                    || gap.getMappingQuality() == SamBamFileReader.UNKNOWN_CALCULATED_MAP_QUAL) {
                    hasMappingQualities = false;
                }
            }
        }
        
        IntervalRequest request = covAndDiffs.getRequest();
        String refSubSeq = trackConnector.getRefGenome().getChromSequence(request.getChromId(), request.getFrom(), request.getTo());
        int absPos;
        int[][] baseCounts;
        char refBase;
        int maxCount;
        int refBaseIdx;
        int diffCount;
        int largestBaseCount;
        int cov;
        int frequency;
        SequenceComparison snpType;
        int averageBaseQual;
        int averageMappingQual;
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
                    if (maxCount < baseCounts[j][COUNT_IDX]) {
                        maxCount = baseCounts[j][COUNT_IDX];
                        maxBaseIdx = j;
                    }
                    //because only contains diffs, no matches
                    diffCount += baseCounts[j][COUNT_IDX];
                    largestBaseCount = largestBaseCount < baseCounts[j][COUNT_IDX] ? baseCounts[j][COUNT_IDX] : largestBaseCount;
                }

                if (maxCount > 0 && !analysisParams.isUseMainBase() && diffCount >= analysisParams.getMinMismatchingBases()
                        || analysisParams.isUseMainBase()    && largestBaseCount >= analysisParams.getMinMismatchingBases()
                        ) {
                    
                    averageBaseQual = baseCounts[maxBaseIdx][BASE_QUAL_IDX] / maxCount;
                    averageMappingQual = baseCounts[maxBaseIdx][MAP_QUAL_IDX] / maxCount;
                    
                    if (  (!this.hasBaseQualities    || averageBaseQual    >= analysisParams.getMinAverageBaseQual())
                       && (!this.hasMappingQualities || averageMappingQual >= analysisParams.getMinAverageMappingQual())
                            ) {
                        
                        cov = coverage.getTotalCoverage(excludedClasses, absPos, true) + coverage.getTotalCoverage(excludedClasses, absPos, false);
                        if (cov == 0) {
                            ++cov;
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found uncovered position in diffs: {0}", absPos);
                        }
                        frequency = (diffCount * 100) / cov;

                        if (frequency >= analysisParams.getMinPercentage()) {
                            refBase = refSubSeq.charAt(i);
                            refBaseIdx = getBaseInt(refBase);
                            //determine SNP type, can still be match, if match coverage is largest
                            baseCounts[refBaseIdx][COUNT_IDX] = cov - diffCount;
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
                                    baseCounts[BASE_A][COUNT_IDX],
                                    baseCounts[BASE_C][COUNT_IDX],
                                    baseCounts[BASE_G][COUNT_IDX],
                                    baseCounts[BASE_T][COUNT_IDX],
                                    baseCounts[BASE_N][COUNT_IDX],
                                    baseCounts[BASE_GAP][COUNT_IDX],
                                    cov,
                                    frequency,
                                    snpType,
                                    averageBaseQual,
                                    averageMappingQual));
                        }
                    }
                }
            }

            List<int[][]> gapOrderList;
            int[][] gapCountArray;

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
                            if (maxCount < gapCountArray[k][COUNT_IDX]) {
                                maxCount = gapCountArray[k][COUNT_IDX];
                                maxBaseIdx = k;
                            }
                            //because only contains gaps counts, no matches
                            diffCount += gapCountArray[k][COUNT_IDX];
                            largestBaseCount = largestBaseCount < gapCountArray[k][COUNT_IDX] ? gapCountArray[k][COUNT_IDX] : largestBaseCount;
                        }

                        if (!analysisParams.isUseMainBase() && diffCount >= analysisParams.getMinMismatchingBases()
                                || analysisParams.isUseMainBase() && largestBaseCount >= analysisParams.getMinMismatchingBases()
                        ) {

                            averageBaseQual = gapCountArray[maxBaseIdx][BASE_QUAL_IDX] / maxCount;
                            averageMappingQual = gapCountArray[maxBaseIdx][MAP_QUAL_IDX] / maxCount;

                            if (   (!this.hasBaseQualities    || averageBaseQual    >= analysisParams.getMinAverageBaseQual())
                                && (!this.hasMappingQualities || averageMappingQual >= analysisParams.getMinAverageMappingQual())
                            ) {

                                cov = coverage.getTotalCoverage(excludedClasses, absPos, true) + coverage.getTotalCoverage(excludedClasses, absPos, false);
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
                                            '-',
                                            gapCountArray[BASE_A][COUNT_IDX],
                                            gapCountArray[BASE_C][COUNT_IDX],
                                            gapCountArray[BASE_G][COUNT_IDX],
                                            gapCountArray[BASE_T][COUNT_IDX],
                                            gapCountArray[BASE_N][COUNT_IDX],
                                            0,
                                            cov,
                                            frequency,
                                            SequenceComparison.INSERTION,
                                            j,
                                            averageBaseQual,
                                            averageMappingQual));
                                }
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

//    private int getNeighboringCov(int absPos, CoverageManager coverage) {
//        int cov1 = 0;
//        int cov2 = 0;
//        
//        cov1 = coverage.getCoverage(MappingClass.BEST_MATCH).getFwdCov(absPos) + coverage.getCoverage(MappingClass.BEST_MATCH).getRevCov(absPos);
//        if (cov1 == 0) {
//            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "found uncovered position in gaps: {0}", absPos);
//        }
//
//        cov2 = coverage.getCoverage(MappingClass.BEST_MATCH).getFwdCov(absPos + 1) + coverage.getCoverage(MappingClass.BEST_MATCH).getRevCov(absPos + 1);
//
//        int cov = (cov1 + cov2) / 2;
//        return cov == 0 ? 1 : cov;
//    }

}
