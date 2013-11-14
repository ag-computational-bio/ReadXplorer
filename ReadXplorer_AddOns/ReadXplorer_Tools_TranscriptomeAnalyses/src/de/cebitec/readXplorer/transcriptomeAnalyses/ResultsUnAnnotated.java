/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructure.NovelRegion;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class ResultsUnAnnotated extends ResultTrackAnalysis<ParameterSetFiveEnrichedAnalyses>{

    private List<NovelRegion> results;
    
    public ResultsUnAnnotated(Map<Integer, PersistantTrack> trackMap, boolean combineTracks) {
        super(trackMap, combineTracks);
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

    public List<NovelRegion> getResults() {
        return this.results;
    }
    
}
