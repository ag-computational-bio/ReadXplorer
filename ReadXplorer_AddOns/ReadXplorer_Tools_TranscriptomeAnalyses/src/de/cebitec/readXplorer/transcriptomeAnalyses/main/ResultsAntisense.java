package de.cebitec.readXplorer.transcriptomeAnalyses.main;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.transcriptomeAnalyses.datastructures.Antisense;
import java.util.List;
import java.util.Map;

/**
 *
 * @author jritter
 */
public class ResultsAntisense extends ResultTrackAnalysis<ParameterSetFiveEnrichedAnalyses>{

    private List<Antisense> results;
    ParameterSetFiveEnrichedAnalyses parameters;
    
    public ResultsAntisense(Map<Integer, PersistantTrack> trackMap, List<Antisense> asResults, int refId, boolean combineTracks) {
        super(trackMap, refId, combineTracks);
        this.results = asResults;
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

    public List<Antisense> getResults() {
        return this.results;
    }
    
}
