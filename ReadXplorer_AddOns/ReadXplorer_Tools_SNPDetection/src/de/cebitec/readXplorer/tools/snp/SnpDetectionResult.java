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

import de.cebitec.common.sequencetools.geneticcode.AminoAcidProperties;
import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.CodonSnp;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.databackend.dataObjects.Snp;
import de.cebitec.readXplorer.databackend.dataObjects.SnpI;
import de.cebitec.readXplorer.util.GeneralUtils;
import de.cebitec.readXplorer.util.SequenceComparison;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Contains all data belonging to a SNP analysis data set. Also has the
 * capabilities of transforming the SNP data into the format readable by
 * ExcelExporters.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class SnpDetectionResult extends ResultTrackAnalysis<ParameterSetSNPs> {
    
    private List<SnpI> snpList;

    
    /**
     * New snp data object.
     * @param snpList list of snps of the analysis
     * @param trackMap hashmap of track ids to the tracks used in the analysis
     * @param referenceId id of the reference genome, for which this result was
     * generated
     * @param combineTracks <code>true</code>, if the tracks in the list are
     * combined, <code>false</code> otherwise
     */
    public SnpDetectionResult(List<SnpI> snpList, Map<Integer, PersistantTrack> trackMap, int referenceId, boolean combineTracks,
            int trackColumn, int filterColumn) {
        super(trackMap, referenceId, combineTracks, trackColumn, filterColumn);
        this.snpList = snpList;
    }
    
    /**
     * @return the list of snps found during the analysis step
     */
    public List<SnpI> getSnpList() {
        return this.snpList;
    }    
    
    /**
     * @return the snp data ready to export with an {@link ExcelExporter}
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> allData = new ArrayList<>();
        List<List<Object>> snpExportData = new ArrayList<>();
        List<Object> snpExport;
        Snp snp;
        String intergenic = "Intergenic";
        String aminoAcidsSnp;
        String aminoAcidsRef;
        String effect;
        String geneId;
        String locus;
        String ecNo;
        List<CodonSnp> codons;
        char aminoSnp;
        char aminoRef;
        String codonsSNP;
        String codonsRef;
        String product;
        
        for (SnpI snpi : this.snpList) {
            snpExport = new ArrayList<>();
            snp = (Snp) snpi; 
            
            snpExport.add(snp.getPosition());
            snpExport.add(snp.getGapOrderIndex());
            snpExport.add(this.getTrackEntry(snp.getTrackId(), true));
            snpExport.add(this.getChromosomeMap().get(snp.getChromId()));
            snpExport.add(snp.getBase());
            snpExport.add(snp.getRefBase());
            snpExport.add(snp.getARate());
            snpExport.add(snp.getCRate());
            snpExport.add(snp.getGRate());
            snpExport.add(snp.getTRate());
            snpExport.add(snp.getNRate());
            snpExport.add(snp.getGapRate());
            snpExport.add(snp.getCoverage());
            snpExport.add(snp.getFrequency());
            snpExport.add(snp.getType().getType());
            
            aminoAcidsRef = "";
            aminoAcidsSnp = "";
            codonsRef = "";
            codonsSNP = "";
            effect = "";
            geneId = "";
            locus = "";
            ecNo = "";
            product = "";
            //determine amino acid substitutions among snp substitutions
            if (snp.getType() == SequenceComparison.SUBSTITUTION) {
           
                codons = snp.getCodons();
                
                if (codons.isEmpty()) {
                    aminoAcidsSnp = intergenic;
                    aminoAcidsRef = intergenic;
                }
               
                for (CodonSnp codon : codons) {
                    aminoRef = codon.getAminoRef();
                    aminoSnp = codon.getAminoSnp();
                    codonsRef += codon.getTripletRef() + "\n";
                    codonsSNP += codon.getTripletSnp() + "\n";
                    if (aminoRef != '-') {
                        aminoAcidsRef += aminoRef + " (" + AminoAcidProperties.getPropertyForAA(aminoRef) + ")\n";
                    } else {
                        aminoAcidsRef += aminoRef + "\n";
                    }
                    if (aminoSnp != '-') {
                    aminoAcidsSnp += aminoSnp + " (" + AminoAcidProperties.getPropertyForAA(aminoSnp) + ")\n";
                    } else {
                        aminoAcidsSnp += aminoSnp + "\n";
                    }
                    effect += codon.getEffect().getType() + "\n";
                    geneId += codon.getFeature() + "\n";
                    locus += codon.getFeature().getLocus() + "\n";
                    ecNo += codon.getFeature().getEcNumber() + "\n";
                    product += codon.getFeature().getProduct()+ "\n";
                }
            } else {
                codons = snp.getCodons();
                if (!codons.isEmpty()) {
                    if (snp.getType().equals(SequenceComparison.INSERTION)) {
                        effect = String.valueOf(SequenceComparison.INSERTION.getType());

                    } else if (snp.getType().equals(SequenceComparison.DELETION)) {
                        effect = String.valueOf(SequenceComparison.DELETION.getType());

                    } else if (snp.getType().equals(SequenceComparison.MATCH)) {
                        effect = String.valueOf(SequenceComparison.MATCH.getType());
                    }

                    for (CodonSnp codon : codons) {
                        geneId += codon.getFeature() + "\n";
                        locus += codon.getFeature().getLocus() + "\n";
                        ecNo += codon.getFeature().getEcNumber() + "\n";
                        ecNo += codon.getFeature().getProduct() + "\n";
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

            snpExport.add(aminoAcidsRef);
            snpExport.add(aminoAcidsSnp);
            snpExport.add(codonsRef);
            snpExport.add(codonsSNP);
            snpExport.add(snp.getAverageBaseQual());
            snpExport.add(snp.getAverageMappingQual());
            snpExport.add(effect);
            snpExport.add(geneId);
            snpExport.add(locus);
            snpExport.add(ecNo);
            snpExport.add(product);
            
            snpExportData.add(snpExport);
        }
        
        allData.add(snpExportData);
        
        //create statistics sheet
        ParameterSetSNPs params = (ParameterSetSNPs) this.getParameters();
        
        List<List<Object>> statisticsExportData = new ArrayList<>();
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("SNP detection for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("SNP detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum percentage of variation:", params.getMinPercentage()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum number of varying bases:", params.getMinMismatchingBases()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Count only most frequent base:", params.isUseMainBase() ? "yes" : "no"));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum base quality:", params.getMinBaseQuality()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum average base quality:", params.getMinAverageBaseQual()));
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Minimum average mapping quality:", params.getMinAverageMappingQual()));
        params.getReadClassParams().addReadClassParamsToStats(statisticsExportData);
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between parameters and statistics
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("SNP effect statistics:"));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_TOTAL));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_INTERGENEIC));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_SYNONYMOUS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_CHEMIC_NEUTRAL));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_CHEMIC_DIFF));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_STOPS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_AA_INSERTIONS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_AA_DELETIONS));
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between parameters and statistics
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("SNP type statistics:"));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_SUBSTITUTIONS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_INSERTIONS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_DELETIONS));
        
        allData.add(statisticsExportData);
        
        return allData;
    }

    /**
     * @return the snp data column descriptions to export with an 
     * {@link ExcelExporter}
     */
    @Override
    public List<List<String>> dataColumnDescriptions() {
        List<List<String>> dataColumnDescriptionsList = new ArrayList<>();
        
        List<String> dataColumnDescriptions = new ArrayList<>();
        dataColumnDescriptions.add("Position");
        dataColumnDescriptions.add("Gap Index");
        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Base");
        dataColumnDescriptions.add("Reference");
        dataColumnDescriptions.add("A");
        dataColumnDescriptions.add("C");
        dataColumnDescriptions.add("G");
        dataColumnDescriptions.add("T");
        dataColumnDescriptions.add("N");
        dataColumnDescriptions.add("_");
        dataColumnDescriptions.add("Coverage");
        dataColumnDescriptions.add("Frequency");
        dataColumnDescriptions.add("Type");
        dataColumnDescriptions.add("AA Ref");
        dataColumnDescriptions.add("AA SNP");
        dataColumnDescriptions.add("Codon Ref");
        dataColumnDescriptions.add("Codon SNP");
        dataColumnDescriptions.add("Effect on AA");
        dataColumnDescriptions.add("Av Base Qual");
        dataColumnDescriptions.add("Av Mapping Qual");
        dataColumnDescriptions.add("Feature Names");
        dataColumnDescriptions.add("Locus");
        dataColumnDescriptions.add("EC-Number");
        
        dataColumnDescriptionsList.add(dataColumnDescriptions);
        
        //add snp statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("SNP detection parameter and statistics table");
        dataColumnDescriptionsList.add(statisticColumnDescriptions);
        
        return dataColumnDescriptionsList;
    }
    
    /**
     * @return the snp data sheet names ready to export with an 
     * {@link ExcelExporter}
     */
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("SNP Table");
        sheetNames.add("SNP Statistics");
        return sheetNames;
    }    
}
