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


import de.cebitec.readxplorer.api.objects.AnalysisI;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.connector.TrackConnector;
import de.cebitec.readxplorer.databackend.dataObjects.Mapping;
import de.cebitec.readxplorer.databackend.dataObjects.MappingResult;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentFeature;
import de.cebitec.readxplorer.transcriptionanalyses.dataStructures.RPKMvalue;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.polyTree.Node;
import de.cebitec.readxplorer.utils.polyTree.NodeVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


/**
 * Carries out the logic behind the RPKM anaylsis.
 * <p>
 * @author Martin Tötsches, Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class AnalysisRPKM implements Observer, AnalysisI<List<RPKMvalue>> {

    private final TrackConnector trackConnector;
    private final List<RPKMvalue> rpkmValues;
    private final List<PersistentFeature> genomeFeatures;
    private final HashMap<Integer, RPKMvalue> featureReadCount;
    private double totalMappedReads = 0;

    private int lastMappingIdx;
    private int currentCount;
    private double geneExonLength;
    private double noFeatureReads;
    private final ParameterSetRPKM parametersRPKM;
//    private Map<FeatureType, Integer> featureCountMap; //can be used, if counts for single feature types are needed
    private int noSelectedFeatures;


    /**
     * Carries out the logic behind the RPKM anaylsis.
     * <p>
     * @param trackConnector The trackConnector of the track for this analysis
     * @param parametersRPKM
     */
    public AnalysisRPKM( TrackConnector trackConnector, ParameterSetRPKM parametersRPKM ) {
        this.trackConnector = trackConnector;
        this.rpkmValues = new ArrayList<>();
        this.featureReadCount = new HashMap<>();
        this.lastMappingIdx = 0;
        this.parametersRPKM = parametersRPKM;
        this.genomeFeatures = new ArrayList<>();
        this.initDatastructures();
    }


    /**
     * Initializes the genome features and all corresponding data structures.
     */
    private void initDatastructures() {
        ReferenceConnector refConnector = ProjectConnector.getInstance().getRefGenomeConnector( trackConnector.getRefGenome().getId() );

        for( PersistentChromosome chrom : refConnector.getChromosomesForGenome().values() ) {
            int chromLength = chrom.getLength();
//            this.modifySelFeatureTypes();
            List<PersistentFeature> chromFeatures = refConnector.getFeaturesForRegionInclParents( 0, chromLength, parametersRPKM.getSelFeatureTypes(), chrom.getId() );

//        this.featureCountMap = this.fillInFeatureTypes();

            for( PersistentFeature feature : chromFeatures ) {
                this.featureReadCount.put( feature.getId(), new RPKMvalue( feature, 0, 0, trackConnector.getTrackID() ) );
                this.genomeFeatures.add( feature );
//                featureCountMap.put(feature.getType(), featureCountMap.get(feature.getType()) + 1);
            }
            this.noSelectedFeatures = this.genomeFeatures.size();
        }
    }


    @Override
    public void update( Object data ) {
        MappingResult mappingResult = new MappingResult( null, null );

        if( data.getClass() == mappingResult.getClass() ) {
            MappingResult mappings = (MappingResult) data;
            this.updateReadCountForFeatures( mappings );
        }
        else if( data instanceof Byte && ((Byte) data) == 2 ) { //2 means mapping analysis is finished
            this.calculateRPKMvalues();
        }
    }


    /**
     * @return The list of RPKM values for all selected feature types.
     */
    @Override
    public List<RPKMvalue> getResults() {
        return this.rpkmValues;
    }


    /**
     * Updates the read count for all features in the genomeFeatures list by
     * all mappings in the mappings list.
     * <p>
     * @param mappingResult the result containing all mappings to add to the
     *                      feature count
     */
    public void updateReadCountForFeatures( MappingResult mappingResult ) {
        List<Mapping> mappings = mappingResult.getMappings();
        PersistentFeature feature;
        boolean fstFittingMapping;
        int currentChromId = mappingResult.getRequest().getChromId();
        boolean isStrandBothOption = parametersRPKM.getReadClassParams().isStrandBothOption();
        boolean isFeatureStrand = parametersRPKM.getReadClassParams().isStrandFeatureOption();
        boolean analysisStrand;

        for( int i = 0; i < this.genomeFeatures.size(); ++i ) {
            feature = this.genomeFeatures.get( i );
            if( feature.getChromId() == currentChromId ) {

                int featStart = feature.getStart();
                int featStop = feature.getStop();
                analysisStrand = isFeatureStrand ? feature.isFwdStrand() : !feature.isFwdStrand();
                fstFittingMapping = true;

                for( int j = this.lastMappingIdx; j < mappings.size(); ++j ) {
                    Mapping mapping = mappings.get( j );

                    //mappings identified within a feature
                    if( mapping.getStop() > featStart && mapping.getStart() < featStop ) {

                        if( fstFittingMapping ) {
                            this.lastMappingIdx = j;
                            fstFittingMapping = false;
                        }
                        if( isStrandBothOption || analysisStrand == mapping.isFwdStrand() ) {
                            ++this.currentCount;
                        }

                        //still mappings left, but need next feature
                    }
                    else if( mapping.getStart() > featStop ) {
                        if( fstFittingMapping ) { //until now no mapping was found for current feature
                            lastMappingIdx = j; //even if next feature starts at same start position no mapping will be found until mapping index j
                        }
                        break;
                    }
                }

                //store read count in feature
                this.featureReadCount.get( feature.getId() ).setReadCount( this.featureReadCount.get( feature.getId() ).getReadCount() + this.currentCount );
                this.totalMappedReads += this.currentCount;
                this.currentCount = 0;
            }
        }

        this.lastMappingIdx = 0;
        //TODO: solution for more than one feature overlapping mapping request boundaries
    }


    /**
     * Calculates the RPKM value for a given feature/gene according to the
     * formula given in Mortazavi et al. 2008, Mapping and quantifying mammalian
     * transcriptomes by RNA-Seq:
     *
     * <br>R = 10^9 * C / (N * L) where
     * <br>C = number of mappable reads for gene
     * <br>N = total number of mappable reads for experiment/data set
     * <br>L = sum of gene base pairs
     */
    public void calculateRPKMvalues() {
        /*
         * Hierarchy to use for nested features:
         * 1. Gene - use gene, if no other subfeatures are present
         * - mRNA - complete mRNA, this shall be used for RPKM calculation, if no exons are given
         * - Exon - use them for RPKM, if they are available (mostly only in eukaryotes)
         * - CDS - real coding region. They do not cover the whole exons, so this level can be excluded,
         * except when CDS is selected for output
         * In general: We calculate all RPKM values for all feature classes given.
         * BUT: If we analyze a gene, we check for the subfeatures and output their RPKM. Same for mRNA or Exon
         * When e.g. only Exon or CDS is selected, we only calculate the separate RPKM values for the single
         * exons and CDS and DO NOT combine them, as we do for mRNA and Gene!
         * tRNA and rRNA are same level than mRNA
         */
        Set<FeatureType> selFeatureTypes = parametersRPKM.getSelFeatureTypes();
        PersistentFeature feature;
        double rpkm;
        int readCount;
        for( Integer id : this.featureReadCount.keySet() ) {
            feature = this.featureReadCount.get( id ).getFeature();
            readCount = this.featureReadCount.get( id ).getReadCount();
            if( selFeatureTypes.contains( feature.getType() )
                && readCount >= parametersRPKM.getMinReadCount()
                && readCount <= parametersRPKM.getMaxReadCount() ) {

                geneExonLength = 0; //gene length or sum of the exon length of a gene in bp
                noFeatureReads = 0; //no read for the feature itself or for gene/mRNA of the corresponding exons

                //special handling of gene/mRNA/tRNA/rRNA - if they have exons, only the exon reads are counted
                if( feature.getType() == FeatureType.GENE || feature.getType() == FeatureType.MRNA
                    || feature.getType() == FeatureType.RRNA || feature.getType() == FeatureType.TRNA ) {
                    this.calcFeatureData( feature, FeatureType.EXON );
                }
                if( geneExonLength == 0 && feature.getType() == FeatureType.GENE ) {
                    this.calcFeatureData( feature, FeatureType.MRNA );
                    if( geneExonLength == 0 ) { //if the gene has a rRNA or tRNA instead of an mRNA, we have to check this, too
                        this.calcFeatureData( feature, FeatureType.RRNA );
                    }
                    if( geneExonLength == 0 ) {
                        this.calcFeatureData( feature, FeatureType.TRNA );
                    }
                }

                /* calc gene/mRNA length/ sum of exon length for gene/mRNA for prokaryotes or
                 * gene where no exons are given and all other features */
                if( geneExonLength <= 0 ) {
                    geneExonLength = feature.getLength();
                    noFeatureReads = this.featureReadCount.get( id ).getReadCount();
                }

                rpkm = 0;
                if( noFeatureReads > 0 ) {
                    rpkm = noFeatureReads * 1000000000 / (this.totalMappedReads * geneExonLength);
                }//1000000000 = 1000000 -> normalization factor * 1000 -> factor for KB of exon length
                this.rpkmValues.add( new RPKMvalue( feature, rpkm, (int) noFeatureReads, trackConnector.getTrackID() ) );
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
     *                count
     *                is needed
     */
    private void calcFeatureData( PersistentFeature feature, final FeatureType type ) {
        //calc length of all exons of the mRNA and number of reads mapped to exon regions
        feature.topDown( new NodeVisitor() {
            @Override
            public void visit( Node node ) {
                PersistentFeature subFeature = (PersistentFeature) node;
                if( subFeature.getType() == type ) {
                    geneExonLength += subFeature.getLength();
                    try {
                        noFeatureReads += featureReadCount.get( subFeature.getId() ).getReadCount();
                    }
                    catch( NullPointerException e ) {
                        //continue
                    }
                }
            }


        } );
    }


    /**
     * @return the number of selected genome features of the analyzed reference
     *         genome.
     */
    public int getNoGenomeFeatures() {
        return this.noSelectedFeatures;
    }


    private void modifySelFeatureTypes() {
        if( parametersRPKM.getSelFeatureTypes().contains( FeatureType.GENE ) ) {
            parametersRPKM.getSelFeatureTypes().add( FeatureType.EXON );
        }
    }


}
