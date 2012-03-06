package de.cebitec.vamp.databackend.dataObjects;

import de.cebitec.common.sequencetools.AminoAcidProperties;
import de.cebitec.vamp.exporter.excel.ExcelExportDataI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rhilker
 * 
 * Contains all data belonging to a SNP analysis data set. Also has the 
 * capabilities of transforming the SNP data into the format readable by ExcelExporters.
 */
public class SnpData implements ExcelExportDataI {
    
    private List<SnpI> snpList;
    private Map<Integer, String> trackNames;

    
    /**
     * New object.
     * @param snpList list of snps of the analysis
     * @param trackNames hashmap of track ids to track names
     */
    public SnpData(List<SnpI> snpList, Map<Integer, String> trackNames) {
        this.snpList = snpList;
        this.trackNames = trackNames;
    }

    public SnpData(List<Snp454> snps, HashMap<Integer, String> hashMap) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    
    public List<SnpI> getSnpList() {
        return snpList;
    }

    
    /**
     * @return map of track ids to track names 
     */
    public Map<Integer, String> getTrackNames() {
        return trackNames;
    }    
    
    
    @Override
    public List<List<Object>> dataToExcelExportList() {
        List<List<Object>> snpExportData = new ArrayList<List<Object>>();
        
        for (SnpI snpi : this.snpList) {
            List<Object> snpExport = new ArrayList<Object>();
            Snp snp = (Snp) snpi; 
            
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
            snpExport.add(snp.getTRate());
            snpExport.add(snp.getNRate());
            snpExport.add(snp.getGapRate());
            
            List<CodonSnp> codons = snp.getCodons();
            String noGene = "No gene";
            String aminoAcidsSnp = "";
            String aminoAcidsRef = "";
            if (codons.isEmpty()){
                aminoAcidsSnp = noGene;
                aminoAcidsRef = noGene;
            }
            String effect = "";
            String geneId = "";
            char aminoSnp;
            char aminoRef;
            for (CodonSnp codon : codons) {
                aminoSnp = codon.getAminoSnp();
                aminoRef = codon.getAminoRef();
                aminoAcidsSnp += aminoSnp + " (" + AminoAcidProperties.getPropertyForAA(aminoSnp) + ")\n";
                aminoAcidsRef += aminoRef + " (" + AminoAcidProperties.getPropertyForAA(aminoRef) + ")\n";
                effect += codon.getEffect().getType() + "\n";
                geneId += codon.getGeneId() + "\n";
            }
            
            //determine effect on amino acid sequence in case its not a substitution
            if (!aminoAcidsSnp.equals(noGene) && effect.equals("")) { //only if there is at least on gene here
                effect += snp.getType().getType(); //it will be identical to type in this case
            }
            
            snpExport.add(aminoAcidsSnp);
            snpExport.add(aminoAcidsRef);
            snpExport.add(effect);
            snpExport.add(geneId);
            
            snpExportData.add(snpExport);
        }
        
        return snpExportData;
    }

    @Override
    public List<String> dataColumnDescriptions() {
        List<String> dataColumnDescriptions = new ArrayList();
        
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
        dataColumnDescriptions.add("Features (Genes)");
        
        return dataColumnDescriptions;
    }
    
}
