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

package de.cebitec.readxplorer.tools.snpdetection;


import de.cebitec.common.sequencetools.geneticcode.AminoAcidProperties;
import de.cebitec.readxplorer.api.enums.SequenceComparison;
import de.cebitec.readxplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readxplorer.databackend.dataobjects.CodonSnp;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentTrack;
import de.cebitec.readxplorer.databackend.dataobjects.Snp;
import de.cebitec.readxplorer.databackend.dataobjects.SnpI;
import de.cebitec.readxplorer.utils.GeneralUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * Contains all data belonging to a SNP analysis data set. Also has the
 * capabilities of transforming the SNP data into the format readable by
 * ExcelExporters.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SnpDetectionResult extends ResultTrackAnalysis<ParameterSetSNPs> {

    private final List<SnpI> snpList;


    /**
     * New snp data object.
     * <p>
     * @param snpList       list of snps of the analysis
     * @param trackMap      hashmap of track ids to the tracks used in the
     *                      analysis
     * @param reference     reference genome, for which this result was
     *                      generated
     * @param combineTracks <code>true</code>, if the tracks in the list are
     *                      combined, <code>false</code> otherwise
     * @param trackColumn   Track column in the result table
     * @param filterColumn  Position column in the result table
     */
    public SnpDetectionResult( List<SnpI> snpList, Map<Integer, PersistentTrack> trackMap, PersistentReference reference, boolean combineTracks,
                               int trackColumn, int filterColumn ) {
        super( reference, trackMap, combineTracks, trackColumn, filterColumn );
        this.snpList = new ArrayList<>( snpList );
        Collections.sort( this.snpList );
    }


    /**
     * @return the list of snps found during the analysis step
     */
    public List<SnpI> getSnpList() {
        return Collections.unmodifiableList( snpList );
    }


    /**
     * Adds all new SNPs to the current list of SNPs stored in this result.
     * <p>
     * @param newSnps The list of new SNPs to add
     */
    public void addAllSnps( List<SnpI> newSnps ) {
        snpList.addAll( newSnps );
    }


    /**
     * @return the snp data ready to export with an {@link ExcelExporter}
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> allData = new ArrayList<>();
        List<List<Object>> snpExportData = new ArrayList<>();

        String intergenic = "Intergenic";

        for( SnpI snpi : this.snpList ) {
            List<Object> snpExport = new ArrayList<>();
            Snp snp = (Snp) snpi;

            snpExport.add( snp.getPosition() );
            snpExport.add( snp.getGapOrderIndex() );
            snpExport.add( getTrackEntry( snp.getTrackId(), true ) );
            snpExport.add( getChromosomeMap().get( snp.getChromId() ) );
            snpExport.add( snp.getBase() );
            snpExport.add( snp.getRefBase() );
            snpExport.add( snp.getARate() );
            snpExport.add( snp.getCRate() );
            snpExport.add( snp.getGRate() );
            snpExport.add( snp.getTRate() );
            snpExport.add( snp.getNRate() );
            snpExport.add( snp.getGapRate() );
            snpExport.add( snp.getCoverage() );
            snpExport.add( snp.getFrequency() );
            snpExport.add( snp.getType().getType() );

            String aminoAcidsRef = "";
            String aminoAcidsSnp = "";
            String codonsSNP = "";
            String codonsRef = "";
            String effect = "";
            String geneId = "";
            String locus = "";
            String ecNo = "";
            String product = "";
            List<CodonSnp> codons;
            //determine amino acid substitutions among snp substitutions
            if( snp.getType() == SequenceComparison.SUBSTITUTION ) {

                codons = snp.getCodons();

                if( codons.isEmpty() ) {
                    aminoAcidsRef = intergenic;
                    aminoAcidsSnp = intergenic;
                }

                for( CodonSnp codon : codons ) {
                    char aminoRef = codon.getAminoRef();
                    char aminoSnp = codon.getAminoSnp();
                    codonsRef += codon.getTripletRef() + "\n";
                    codonsSNP += codon.getTripletSnp() + "\n";
                    if( aminoRef != '-' ) {
                        aminoAcidsRef += aminoRef + " (" + AminoAcidProperties.getPropertyForAA( aminoRef ) + ")\n";
                    } else {
                        aminoAcidsRef += aminoRef + "\n";
                    }
                    if( aminoSnp != '-' ) {
                        aminoAcidsSnp += aminoSnp + " (" + AminoAcidProperties.getPropertyForAA( aminoSnp ) + ")\n";
                    } else {
                        aminoAcidsSnp += aminoSnp + "\n";
                    }
                    effect += codon.getEffect().getType() + "\n";
                    geneId += codon.getFeature() + "\n";
                    locus += codon.getFeature().getLocus() + "\n";
                    ecNo += codon.getFeature().getEcNumber() + "\n";
                    product += codon.getFeature().getProduct() + "\n";
                }

            } else {
                codons = snp.getCodons();
                if( !codons.isEmpty() ) {
                    if( snp.getType().equals( SequenceComparison.INSERTION ) ) {
                        effect = String.valueOf( SequenceComparison.INSERTION.getType() );

                    } else if( snp.getType().equals( SequenceComparison.DELETION ) ) {
                        effect = String.valueOf( SequenceComparison.DELETION.getType() );

                    } else if( snp.getType().equals( SequenceComparison.MATCH ) ) {
                        effect = String.valueOf( SequenceComparison.MATCH.getType() );
                    }

                    for( CodonSnp codon : codons ) {
                        geneId += codon.getFeature() + "\n";
                        locus += codon.getFeature().getLocus() + "\n";
                        ecNo += codon.getFeature().getEcNumber() + "\n";
                        product += codon.getFeature().getProduct() + "\n";
                    }
                    codonsRef = "-";
                    codonsSNP = "-";
                    aminoAcidsRef = "-";
                    aminoAcidsSnp = "-";

                } else {
                    codonsRef = "-";
                    codonsSNP = "-";
                    aminoAcidsRef = intergenic;
                    aminoAcidsSnp = intergenic;
                    effect = "";
                    geneId = "";
                    locus = "";
                    ecNo = "";
                    product = "";
                }
            }

            snpExport.add( aminoAcidsRef );
            snpExport.add( aminoAcidsSnp );
            snpExport.add( codonsRef );
            snpExport.add( codonsSNP );
            snpExport.add( effect );
            snpExport.add( snp.getAverageBaseQual() );
            snpExport.add( snp.getAverageMappingQual() );
            snpExport.add( geneId );
            snpExport.add( locus );
            snpExport.add( ecNo );
            snpExport.add( product );

            snpExportData.add( snpExport );
        }

        allData.add( snpExportData );

        //create statistics sheet
        ParameterSetSNPs params = (ParameterSetSNPs) getParameters();

        List<List<Object>> statisticsExportData = new ArrayList<>();

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "SNP detection for tracks:",
                                                                      GeneralUtils.generateConcatenatedString( this.getTrackNameList(), 0 ) ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between title and parameters

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "SNP detection parameters:" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum percentage of variation:", params.getMinPercentage() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum number of varying bases:", params.getMinMismatchingBases() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Count only most frequent base:", params.isUseMainBase() ? "yes" : "no" ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum base quality:", params.getMinBaseQuality() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum average base quality:", params.getMinAverageBaseQual() ) );
        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "Minimum average mapping quality:", params.getMinAverageMappingQual() ) );
        params.getReadClassParams().addReadClassParamsToStats( statisticsExportData );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "SNP effect statistics:" ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_TOTAL ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_INTERGENEIC ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_SYNONYMOUS ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_CHEMIC_NEUTRAL ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_CHEMIC_DIFF ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_STOPS ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_AA_INSERTIONS ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_AA_DELETIONS ) );

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "" ) ); //placeholder between parameters and statistics

        statisticsExportData.add( ResultTrackAnalysis.createTableRow( "SNP type statistics:" ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_SUBSTITUTIONS ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_INSERTIONS ) );
        statisticsExportData.add( createStatisticTableRow( SNPDetectionResultPanel.SNPS_DELETIONS ) );

        allData.add( statisticsExportData );

        return allData;
    }


    /**
     * @return the snp data column descriptions to export with an
     *         {@link ExcelExporter}
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptionsList = new ArrayList<>();

        List<String> dataColumnDescriptions = new ArrayList<>();
        dataColumnDescriptions.add( "Position" );
        dataColumnDescriptions.add( "Gap Index" );
        dataColumnDescriptions.add( "Track" );
        dataColumnDescriptions.add( "Chromosome" );
        dataColumnDescriptions.add( "Base" );
        dataColumnDescriptions.add( "Reference" );
        dataColumnDescriptions.add( "A" );
        dataColumnDescriptions.add( "C" );
        dataColumnDescriptions.add( "G" );
        dataColumnDescriptions.add( "T" );
        dataColumnDescriptions.add( "N" );
        dataColumnDescriptions.add( "_" );
        dataColumnDescriptions.add( "Coverage" );
        dataColumnDescriptions.add( "Frequency" );
        dataColumnDescriptions.add( "Type" );
        dataColumnDescriptions.add( "AA Ref" );
        dataColumnDescriptions.add( "AA SNP" );
        dataColumnDescriptions.add( "Codon Ref" );
        dataColumnDescriptions.add( "Codon SNP" );
        dataColumnDescriptions.add( "Effect on AA" );
        dataColumnDescriptions.add( "Av Base Qual" );
        dataColumnDescriptions.add( "Av Mapping Qual" );
        dataColumnDescriptions.add( "Feature Names" );
        dataColumnDescriptions.add( "Locus" );
        dataColumnDescriptions.add( "EC-Number" );
        dataColumnDescriptions.add( "Product" );

        dataColumnDescriptionsList.add( dataColumnDescriptions );

        //add snp statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add( "SNP detection parameter and statistics table" );
        dataColumnDescriptionsList.add( statisticColumnDescriptions );

        return dataColumnDescriptionsList;
    }


    /**
     * @return the snp data sheet names ready to export with an
     *         {@link ExcelExporter}
     */
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add( "SNP Table" );
        sheetNames.add( "SNP Statistics" );
        return sheetNames;
    }


}
