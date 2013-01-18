package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.common.sequencetools.AminoAcidProperties;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import de.cebitec.vamp.util.GeneralUtils;
import de.cebitec.vamp.util.SequenceComparison;
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
public class SnpData implements ExcelExportDataI {
    
    private List<SnpI> snpList;
    private SnpResultStatistics snpStatistics;
    private Map<Integer, String> trackNames;
    private int num;
    private int percent;

    
    /**
     * New snp data object.
     * @param snpList list of snps of the analysis
     * @param trackNames hashmap of track ids to track names used in the analysis
     */
    public SnpData(List<SnpI> snpList, Map<Integer, String> trackNames) {
        this.snpList = snpList;
        this.trackNames = trackNames;
        this.snpStatistics = new SnpResultStatistics();
    }
    
    /**
     * @return the list of snps found during the analysis step
     */
    public List<SnpI> getSnpList() {
        return this.snpList;
    }

    
    /**
     * @return map of track ids to track names used in the analysis
     */
    public Map<Integer, String> getTrackNames() {
        return this.trackNames;
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
        String ids;
        
        for (SnpI snpi : this.snpList) {
            snpExport = new ArrayList<>();
            snp = (Snp) snpi; 
            
            snpExport.add(snp.getPosition());
            snpExport.add(this.trackNames.get(snp.getTrackId()));
            snpExport.add(snp.getBase().toUpperCase());
            snpExport.add(snp.getRefBase().toUpperCase());
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
            aminoAcidsSnp = "";
            aminoAcidsRef = "";
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
                    aminoSnp = codon.getAminoSnp();
                    aminoRef = codon.getAminoRef();
                    aminoAcidsSnp += aminoSnp + " (" + AminoAcidProperties.getPropertyForAA(aminoSnp) + ")\n";
                    aminoAcidsRef += aminoRef + " (" + AminoAcidProperties.getPropertyForAA(aminoRef) + ")\n";
                    effect += codon.getEffect().getType() + "\n";
                    geneId += codon.getGeneId() + "\n";
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
                        ids += codon.getGeneId() + "\n";
                    }
                    geneId = ids;
                    aminoAcidsSnp = "-";
                    aminoAcidsRef = "-";
                } else {
                    aminoAcidsSnp = "No gene";
                    aminoAcidsRef = "No gene";
                    effect = "";
                    geneId = "";
                }
            }

            snpExport.add(aminoAcidsSnp);
            snpExport.add(aminoAcidsRef);
            snpExport.add(effect);
            snpExport.add(geneId);
            
            snpExportData.add(snpExport);
        }
        
        allData.add(snpExportData);
        
        //create statistics sheet
        List<List<Object>> statisticsExportData = new ArrayList<>();
        List<Object> statisticsExport = new ArrayList<>();
        
        statisticsExport.add("SNP detection for tracks:");
        statisticsExport.add(GeneralUtils.generateConcatenatedString(trackNames));
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>(); //placeholder between title and parameters
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("SNP detection parameters:");
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum percentage of variation:");
        statisticsExport.add(this.percent);
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Minimum number of varying bases:");
        statisticsExport.add(this.num);
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("SNP effect statistics:");
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Total number of SNPs");
        statisticsExport.add(snpStatistics.getTotalNoSnps());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Intergenic SNPs");
        statisticsExport.add(snpStatistics.getNoIntergenicSnps());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Synonymous SNPs");
        statisticsExport.add(snpStatistics.getNoSynonymousSnps());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Chemically neutral SNPs");
        statisticsExport.add(snpStatistics.getNoChemicallyNeutralSnps());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Missense SNPs");
        statisticsExport.add(snpStatistics.getNoMissenseSnps());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Insertions");
        statisticsExport.add(snpStatistics.getNoAAInsertions());
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("Deletions");
        statisticsExport.add(snpStatistics.getNoAADeletions());        
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>(); //placeholder between parameters and statistics
        statisticsExport.add("");
        statisticsExportData.add(statisticsExport);
        
        statisticsExport = new ArrayList<>();
        statisticsExport.add("SNP type statistics:");
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Substitutions");
        statisticsExport.add(snpStatistics.getNoSubstitutions());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Insertions");
        statisticsExport.add(snpStatistics.getNoInsertions());
        statisticsExportData.add(statisticsExport);

        statisticsExport = new ArrayList<>();
        statisticsExport.add("Deletions");
        statisticsExport.add(snpStatistics.getNoDeletions());
        statisticsExportData.add(statisticsExport);
        
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
        dataColumnDescriptions.add("Track");
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
        dataColumnDescriptions.add("Amino Snp");
        dataColumnDescriptions.add("Amino Ref");
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
    

    /**
     * Sets the SNP dection parameters to have them connected with the search
     * results.
     * @param percent minimum deviation in percent at a position used for this
     * snp detection
     * @param num minimum number of deviating coverage at a position used for
     * this snp detection
     */
    public void setSearchParameters(int percent, int num) {
        this.percent = percent;
        this.num = num;
    }

    /**
     * @return get minimum number of deviating coverage at a position used for
     * this snp detection
     */
    public int getMinNoDeviatingCoverage() {
        return this.num;
    }

    /**
     * @return get the minimum deviation in percent at a position used for this
     * snp detection
     */
    public int getMinPercentDeviation() {
        return this.percent;
    }
    
    /**
     * @return the snp statistics of this snp analysis result
     */
    public SnpResultStatistics getSnpStatistics() {
        return snpStatistics;
    }

    /**
     * @param snpStatistics the snp statistics, which should be associated
     * with this analysis result
     */
    public void setSnpStatistics(SnpResultStatistics snpStatistics) {
        this.snpStatistics = snpStatistics;
    }
    
}
