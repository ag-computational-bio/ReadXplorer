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
     * @param combineTracks <cc>true</cc>, if the tracks in the list are
     * combined, <cc>false</cc> otherwise
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
        String noGene;
        String aminoAcidsSnp;
        String aminoAcidsRef;
        String effect;
        String geneId;
        List<CodonSnp> codons;
        char aminoSnp;
        char aminoRef;
        String codonsSNP;
        String codonsRef;
        String ids;
        
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
            
            
            noGene = "No gene";
            aminoAcidsRef = "";
            aminoAcidsSnp = "";
            codonsRef = "";
            codonsSNP = "";
            effect = "";
            geneId = "";
            //determine amino acid substitutions among snp substitutions
            if (snp.getType() == SequenceComparison.SUBSTITUTION) {
           
                codons = snp.getCodons();
                
                if (codons.isEmpty()) {
                    aminoAcidsSnp = noGene;
                    aminoAcidsRef = noGene;
                }
               
                for (CodonSnp codon : codons) {
                    aminoRef = codon.getAminoRef();
                    aminoSnp = codon.getAminoSnp();
                    codonsRef += codon.getTripletRef() + "\n";
                    codonsSNP += codon.getTripletSnp() + "\n";
                    aminoAcidsRef += aminoRef + " (" + AminoAcidProperties.getPropertyForAA(aminoRef) + ")\n";
                    aminoAcidsSnp += aminoSnp + " (" + AminoAcidProperties.getPropertyForAA(aminoSnp) + ")\n";
                    effect += codon.getEffect().getType() + "\n";
                    geneId += codon.getFeature() + "\n";
                }
            } else {
                codons = snp.getCodons();
                ids = "";
                if (!codons.isEmpty()) {
                    if (snp.getType().equals(SequenceComparison.INSERTION)) {
                        effect = String.valueOf(SequenceComparison.INSERTION.getType());

                    } else if (snp.getType().equals(SequenceComparison.DELETION)) {
                        effect = String.valueOf(SequenceComparison.DELETION.getType());

                    } else if (snp.getType().equals(SequenceComparison.MATCH)) {
                        effect = String.valueOf(SequenceComparison.MATCH.getType());
                    }

                    for (CodonSnp codon : codons) {
                        ids += codon.getFeature() + "\n";
                    }
                    geneId = ids;
                    codonsRef = "-";
                    codonsSNP = "-";
                    aminoAcidsRef = "-";
                    aminoAcidsSnp = "-";
                } else {
                    codonsRef = "-";
                    codonsSNP = "-";
                    aminoAcidsRef = "No gene";
                    aminoAcidsSnp = "No gene";
                    effect = "";
                    geneId = "";
                }
            }

            snpExport.add(aminoAcidsRef);
            snpExport.add(aminoAcidsSnp);
            snpExport.add(codonsRef);
            snpExport.add(codonsSNP);
            snpExport.add(effect);
            snpExport.add(geneId);
            
            snpExportData.add(snpExport);
        }
        
        allData.add(snpExportData);
        
        //create statistics sheet
        ParameterSetSNPs params = (ParameterSetSNPs) this.getParameters();
        
        List<List<Object>> statisticsExportData = new ArrayList<>();
        
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("SNP detection for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("SNP detection parameters:"));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum percentage of variation:", params.getMinPercentage()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Minimum number of varying bases:", params.getMinMismatchingBases()));
        statisticsExportData.add(ResultTrackAnalysis.createTwoElementTableRow("Count only most frequent base:", params.isUseMainBase() ? "yes" : "no"));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("SNP effect statistics:"));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_TOTAL));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_INTERGENEIC));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_SYNONYMOUS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_CHEMIC_NEUTRAL));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_CHEMIC_DIFF));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_STOPS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_AA_INSERTIONS));
        statisticsExportData.add(this.createStatisticTableRow(SNP_DetectionResultPanel.SNPS_AA_DELETIONS));
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("")); //placeholder between parameters and statistics
        
        statisticsExportData.add(ResultTrackAnalysis.createSingleElementTableRow("SNP type statistics:"));
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
        dataColumnDescriptions.add("Features");
        
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
