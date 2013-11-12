/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptomeAnalyses;

import de.cebitec.vamp.databackend.ResultTrackAnalysis;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.transcriptomeAnalyses.datastructure.NovelRegion;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class NovelRegionResult extends ResultTrackAnalysis<ParameterSetWholeTranscriptAnalyses>{

    private List<NovelRegion> novelRegions;
    public NovelRegionResult(Map<Integer, PersistantTrack> trackMap, List<NovelRegion> novelRegions, boolean combineTracks) {
        super(trackMap, combineTracks);
        this.novelRegions = novelRegions;
    }

    public List<NovelRegion> getResults() {
        return this.novelRegions;
    }
    @Override
    public List<String> dataSheetNames() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<List<String>> dataColumnDescriptions() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
