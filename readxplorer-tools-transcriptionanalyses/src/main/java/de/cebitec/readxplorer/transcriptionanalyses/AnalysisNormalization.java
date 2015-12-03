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

package de.cebitec.readxplorer.transcriptionanalyses;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.api.objects.AnalysisI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataobjects.Mapping;
import de.cebitec.readxplorer.databackend.dataobjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.AssignedMapping;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.NormalizedReadCount;
import de.cebitec.readxplorer.transcriptionanalyses.datastructures.UnionFractionMapping;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.polytree.Node;
import de.cebitec.readxplorer.utils.polytree.NodeVisitor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static de.cebitec.readxplorer.api.enums.FeatureType.CDS;
import static de.cebitec.readxplorer.api.enums.FeatureType.EXON;
import static de.cebitec.readxplorer.api.enums.FeatureType.GENE;
import static de.cebitec.readxplorer.api.enums.FeatureType.MRNA;
import static de.cebitec.readxplorer.api.enums.FeatureType.RRNA;
import static de.cebitec.readxplorer.api.enums.FeatureType.TRNA;


/**
 * Carries out the logic behind the normalization (TPM and RPKM) anaylsis.
 * <p>
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class AnalysisNormalization implements Observer, AnalysisI<List<NormalizedReadCount>> {

    private static final Logger LOG = Logger.getLogger( AnalysisNormalization.class.getName() );

    private final TrackConnector trackConnector;
    private final List<NormalizedReadCount> normValues;
    private final List<PersistentFeature> genomeFeatures;
    private final Map<Integer, NormalizedReadCount> featureReadCount;
    private double totalMappedReads = 0;
    private boolean notInitialized;

    private double geneExonLength;
    private int noFeatureReads;
    private int readLengthSum;
    private final ParameterSetNormalization paramsNormalization;
    /** Contains the normalization sum for each genomic feature type involved in
     * the analysis. */
    private Map<FeatureType, Double> normalizationSumMap;
    private int noSelectedFeatures;
    private Set<FeatureType> usedFeatureTypes;


    /**
     * Carries out the logic behind the normalization (TPM and RPKM) anaylsis.
     * <p>
     * @param trackConnector      The trackConnector of the track for this
     *                            analysis
     * @param paramsNormalization The set of selected parameters
     */
    public AnalysisNormalization( TrackConnector trackConnector, ParameterSetNormalization paramsNormalization ) {
        this.trackConnector = trackConnector;
        this.paramsNormalization = paramsNormalization;
        normValues = new ArrayList<>();
        featureReadCount = new HashMap<>();
        normalizationSumMap = new HashMap<>();
        genomeFeatures = new ArrayList<>();
        usedFeatureTypes = new HashSet<>( paramsNormalization.getSelFeatureTypes() );
        notInitialized = true;
    }


    @Override
    public void update( Object data ) {
        MappingResult mappingResult = new MappingResult( null, null );

        if( data.getClass() == mappingResult.getClass() ) {
            if( notInitialized ) {
                initDatastructures();
                notInitialized = false;
            }
            MappingResult mappings = (MappingResult) data;
            updateReadCountForFeatures( mappings );

        } else if( data instanceof Byte && ((Byte) data) == 2 ) { //2 means mapping analysis is finished
            calculateFeatHierarchyData();
            NormalizationFormulas.calculateNormalizationSums( featureReadCount, normalizationSumMap );
            calculateNormalizedValues();
        }
    }


    /**
     * Initializes the genome features and all corresponding data structures.
     */
    private void initDatastructures() {
        usedFeatureTypes.addAll( Arrays.asList( new FeatureType[]{ GENE, CDS, MRNA, RRNA, TRNA, EXON } ) );
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector( trackConnector.getRefGenome().getId() );
        fillInFeatureTypes(); //feature types are the same for all chromosomes
        for( PersistentChromosome chrom : refConnector.getChromosomesForGenome().values() ) {
            int chromLength = chrom.getLength();
            List<PersistentFeature> chromFeatures = refConnector.getFeaturesForRegionInclParents( 0, chromLength, usedFeatureTypes, chrom.getId() );

            for( PersistentFeature feature : chromFeatures ) {
                featureReadCount.put( feature.getId(), new NormalizedReadCount( feature, 0, 0, 0, trackConnector.getTrackID(), paramsNormalization ) );
            }
            genomeFeatures.addAll( chromFeatures );
            Collections.sort( genomeFeatures );
            noSelectedFeatures = genomeFeatures.size();
        }
    }


    /**
     * Initializes the map with all feature types selected for the analysis and
     * a value of 0.
     */
    private void fillInFeatureTypes() {
        for( FeatureType type : usedFeatureTypes ) {
            normalizationSumMap.put( type, 0.0 );
        }
    }


    /**
     * @return The list of normalization (TPM and RPKM) and read count values
     *         for all selected feature types.
     */
    @Override
    public List<NormalizedReadCount> getResults() {
        return Collections.unmodifiableList( normValues );
    }


    /**
     * Updates the read count for all features in the genomeFeatures list by all
     * mappings in the mappings list.
     * <p>
     * @param mappingResult the result containing all mappings to add to the
     *                      feature count
     */
    public void updateReadCountForFeatures( MappingResult mappingResult ) {
        List<Mapping> resultMappings = mappingResult.getMappings();
        int currentChromId = mappingResult.getRequest().getChromId();
        boolean isStrandBothOption = paramsNormalization.getReadClassParams().isStrandBothOption();
        boolean isFeatureStrand = paramsNormalization.getReadClassParams().isStrandFeatureOption();
        List<AssignedMapping> mappings = createAssignedMappings( resultMappings );
        int lastMappingIdx = 0;

        //every mapping is associated to overlapping feature via lastMappingIdx according to strategy in AssignedMapping
        for( PersistentFeature feature : genomeFeatures ) {
            if( feature.getChromId() == currentChromId ) {

                int featStart = paramsNormalization.calcFeatureStartOffset( feature );
                int featStop = paramsNormalization.calcFeatureStopOffset( feature );
                boolean analysisStrand = isFeatureStrand ? feature.isFwdStrand() : !feature.isFwdStrand();
                boolean fstFittingMapping = true;
                int readLengthSum = 0; //sum of all mappings of a single feature
                int currentCount = 0; //count for one genomic feature

                for( int j = lastMappingIdx; j < mappings.size(); ++j ) {
//TODO: generally, when results arrive unordered, the first mapping pos can be used to determine feature position to start at
                    AssignedMapping assignedMapping = mappings.get( j );
                    Mapping mapping = assignedMapping.getMapping();

                    //overlapping mappings identified within a feature
                    if( mapping.getStop() >= featStart && mapping.getStart() <= featStop ) {

                        if( fstFittingMapping ) {
                            lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        if( isStrandBothOption || analysisStrand == mapping.isFwdStrand() ) {
                            boolean countIt = assignedMapping.checkAssignment( featStart, featStop, feature );
                            if( countIt ) {
                                assignedMapping.checkCountDecrease( featureReadCount );
                                ++currentCount;
                                readLengthSum += mapping.getLength();
                            }
                        }

                        //still mappings left, but need next feature
                    } else if( mapping.getStart() > featStop ) {
                        if( fstFittingMapping ) { //until now no mapping was found for current feature
                            lastMappingIdx = j; //even if next feature starts at same start position no mapping will be found until mapping index j
                        }
                        break;
//                    } else if ( mapping.getStart() < featStart ) {
//                        //Comment for information purpose: do nothing in this case
                    }
                }

                //store read count in feature
                featureReadCount.get( feature.getId() ).setReadCount( featureReadCount.get( feature.getId() ).getReadCount() + currentCount );
                featureReadCount.get( feature.getId() ).addReadLength( readLengthSum );
            }
        }

        for( AssignedMapping assignedMapping : mappings ) {
            assignedMapping.fractionAssignmentCheck( featureReadCount );
        }

        lastMappingIdx = 0;
        //TODO: solution for more than one feature overlapping mapping request boundaries
    }


    /**
     * Create an {@link AssignedMapping} for each mapping.
     * <p>
     * @param mappings The list of mappings to process
     * <p>
     * @return The created list of {@link AssignedMapping}s
     */
    private List<AssignedMapping> createAssignedMappings( List<Mapping> mappings ) {
        List<AssignedMapping> assignedMappings = new ArrayList<>();
        for( Mapping mapping : mappings ) { //when other read count models are supported, they can be initialized here!
            assignedMappings.add( new UnionFractionMapping( mapping ) );
        }
        return assignedMappings;
    }


    /**
     * For features involved in a more complex hierarchy (not just 1 gene = 1CDS
     * as in prokaryotes), it calculates the read count, read length sum and
     * effective feature length for each of the selected feature types based on
     * the coding subfeatures (first {@link FeatureType.CDS}s, then
     * {@link FeatureType.EXON}s are checked for {@link FeatureType}s included
     * in the analysis among {@link FeatureType.GENE}s,
     * {@link FeatureType.MRNA}s, {@link FeatureType.RRNA}s and
     * {@link FeatureType.TRNA}s).
     */
    private void calculateFeatHierarchyData() {

//        Hierarchy to use for nested features:
//        1. Gene - use gene, if no other subfeatures are present
//        - mRNA - complete mRNA, this shall be used for normalization
//        calculation, if no exons are given
//        - Exon - use them for normalization if they are available (mostly
//        only in eukaryotes)
//        - CDS - real coding region. They do not cover the whole exons, so
//        this level can be excluded, except when CDS is selected for
//        output.
//        In general: We calculate all normalization values for all feature
//        classes given. BUT: If we analyze a gene, we check for the
//        subfeatures and output their normalization values. Same for mRNA or
//        Exon When e.g. only Exon or CDS is selected, we only calculate the
//        separate normalization values for the single exons and CDS and DO NOT
//        combine them, as we do for mRNA and Gene! tRNA and rRNA are same
//        level than mRNA.

        for( Integer id : featureReadCount.keySet() ) {
            NormalizedReadCount countObject = featureReadCount.get( id );
            PersistentFeature feature = countObject.getFeature();
            double readCount = countObject.getReadCount();
            //undesired feature types are not treated
            if( paramsNormalization.getSelFeatureTypes().contains( feature.getType() ) ) {
                if( readCount >= paramsNormalization.getMinReadCount() && readCount <= paramsNormalization.getMaxReadCount() ) {

                    geneExonLength = 0; //gene length or sum of the exon length of a gene in bp
                    readLengthSum = 0;
                    noFeatureReads = 0; //no read for the feature itself or for gene/mRNA of the corresponding exons

                    //special handling of gene/mRNA/tRNA/rRNA - if they have exons, only the exon reads are counted
                    if( feature.getType() == FeatureType.GENE || feature.getType() == FeatureType.MRNA ||
                        feature.getType() == FeatureType.RRNA || feature.getType() == FeatureType.TRNA ) {
                        calcFeatureData( feature, FeatureType.CDS ); //first we try to find CDS
                        if( geneExonLength == 0 ) { //if no CDS are there, try exons
                            calcFeatureData( feature, FeatureType.EXON );
                        }
                    }
                    if( geneExonLength == 0 && feature.getType() == FeatureType.GENE ) {
                        calcFeatureData( feature, FeatureType.MRNA );
                        if( geneExonLength == 0 ) { //if the gene has a rRNA or tRNA instead of an mRNA, we have to check this, too
                            calcFeatureData( feature, FeatureType.RRNA );
                        }
                        if( geneExonLength == 0 ) {
                            calcFeatureData( feature, FeatureType.TRNA );
                        }
                    }

                    if( geneExonLength > 0 ) { //we have multi exon/cds genes only in this case
                        countObject.setReadCount( noFeatureReads ); //not needed for most prokaryotes
                        countObject.setReadLengthSum( readLengthSum );
                        double length;
                        if( paramsNormalization.isUseEffectiveLength() ) {
                            length = NormalizedReadCount.Utils.calcEffectiveFeatureLength( geneExonLength, countObject.getReadLengthMean() );
                        } else {
                            length = geneExonLength;
                        }
                        countObject.storeEffectiveFeatureLength( length );
                    }
                }
                //sum all read counts assigned to any features
                totalMappedReads += readCount;
            }
        }
    }


    /**
     * Calculates the feature type length of the given feature type for all
     * features downward in the feature tree hierarchy (top-down fashion).
     * <p>
     * @param feature the feature whose subfeature length and readcount is
     *                needed
     * @param type    the feature type for which the length sum and total read
     *                count is needed
     */
    private void calcFeatureData( PersistentFeature feature, final FeatureType type ) {
        //calc length of all exons of the mRNA and number of reads mapped to exon regions
        feature.topDown( new NodeVisitor() {
            @Override
            public void visit( Node node ) {
                PersistentFeature subFeature = (PersistentFeature) node;
                if( subFeature.getType() == type ) {
                    geneExonLength += subFeature.getLength();
                    try { //CDS of gene (subfeature) is not in the list: no read count for CDS -> calc all read counts and dismiss later!
                        noFeatureReads += featureReadCount.get( subFeature.getId() ).getReadCount();
                        readLengthSum += featureReadCount.get( subFeature.getId() ).getReadLengthSum();
                    } catch( NullPointerException e ) {
                        LOG.info( "Queried subfeature not contained in list." );
                    }
                }
            }


        } );
    }


    /**
     * Calculates the normalized read count values (RPKM and TPM)e for all
     * features/genes according to the formulas given in Mortazavi et al. 2008,
     * Mapping and quantifying mammalian transcriptomes by RNA-Seq for RPKM:
     * <br>
     * <br><b>RPKM = 10^9 * C / (N * L)</b> where
     * <br>C = number of mappable reads for gene
     * <br>N = total number of mappable reads for experiment/data set
     * <br>L = sum of gene base pairs
     * <p>
     * and Li et al. 2010: RNA-Seq gene expression estimation with read mapping
     * uncertainty for TPM:
     * <br>
     * <br><b>TPM = 10^6 * (c / l) * (1 / (sum_i (c_i / l_i)))</b> where
     * <br>c = number of mappable reads for gene (or genomic feature)
     * <br>l = effective length (or length) of gene (or genomic feature)
     * <br>i = 1 - #genes (or genomic features of the same type)
     */
    public void calculateNormalizedValues() {

        for( Integer id : featureReadCount.keySet() ) {
            NormalizedReadCount countObject = featureReadCount.get( id );
            PersistentFeature feature = countObject.getFeature();
            double readCount = countObject.getReadCount();
            //undesired feature types have to be removed now
            if( readCount >= paramsNormalization.getMinReadCount() && readCount <= paramsNormalization.getMaxReadCount() &&
                paramsNormalization.getSelFeatureTypes().contains( feature.getType() ) ) {

                double featureLength = countObject.getFeatureLength();
                double rpkm = 0;
                double tpm = 0;
                if( readCount > 0 ) {
                    rpkm = NormalizationFormulas.calculateRpkm( readCount, totalMappedReads, featureLength );
                    double normSum = normalizationSumMap.get( feature.getType() );
                    tpm = NormalizationFormulas.calculateTpm( readCount, featureLength, normSum );
                }
                normValues.add( new NormalizedReadCount( feature, rpkm, tpm, readCount, featureLength, trackConnector.getTrackID(), paramsNormalization ) );
            }
        }
    }


    /**
     * @return the number of selected genome features of the analyzed reference
     *         genome.
     */
    public int getNoGenomeFeatures() {
        return this.noSelectedFeatures;
    }


    /**
     * @return The total number of read mappings compatible with genomic
     *         features.
     */
    public double getTotalMappings() {
        return totalMappedReads;
    }


}
