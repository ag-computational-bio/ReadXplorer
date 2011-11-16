package de.cebitec.vamp.databackend.dataObjects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author rhilker
 * 
 * Contains all data belonging to a SNP analysis data set.
 */
public class SnpData {
    
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
    
}
