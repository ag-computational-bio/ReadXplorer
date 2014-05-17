/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.correlationAnalysis;

import de.cebitec.readXplorer.databackend.ResultTrackAnalysis;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import de.cebitec.readXplorer.util.GeneralUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Contains all data belonging to a correlation analysis data set. Also has the
 * capabilities of transforming the result data into the format readable by
 * ExcelExporters.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class CorrelationResult extends ResultTrackAnalysis<CorrelationResult> {
    
    private List<CorrelatedInterval> correlationsList;
    private Map<String, Object> params;

    
    /**
     * New CorrelationResult data object.
     * @param correlationsList list of found correlations
     * @param trackMap hashmap of track ids to tracks used in the analysis
     * @param referenceId id of the reference genome, for which this result was
     * generated
     * @param combineTracks <cc>true</cc>, if the tracks in the list are
     * combined, <cc>false</cc> otherwise
     */
    public CorrelationResult(List<CorrelatedInterval> correlationsList, Map<Integer, PersistantTrack> trackMap, 
            int referenceId, boolean combineTracks, int trackColumn, int filterColumn) {
        super(trackMap, referenceId, combineTracks, trackColumn, filterColumn);
        this.correlationsList = correlationsList;
    }
    
    /**
     * @return the list of correlations found during the analysis step
     */
    public List<CorrelatedInterval> getCorrelationsList() {
        return this.correlationsList;
    }    
    
    /**
     * @return the correlation result data ready to export with an {@link ExcelExporter}
     */
    @Override
    public List<List<List<Object>>> dataToExcelExportList() {
        List<List<List<Object>>> allData = new ArrayList<>();
        List<List<Object>> exportData = new ArrayList<>();
        List<Object> exportLine;
        
        
        for (CorrelatedInterval correlation : this.correlationsList) {
            exportLine = new ArrayList<>(); 
            
            exportLine.add(correlation.getChromId());
            exportLine.add(correlation.getDirection());
            exportLine.add(correlation.getFrom());
            exportLine.add(correlation.getTo());
            exportLine.add(correlation.getCorrelation());
            exportLine.add(correlation.getMinPeakCoverage());
   
            exportData.add(exportLine);
        }
        
        allData.add(exportData);
        
        //create statistics sheet
        List<List<Object>> statisticsExportData = new ArrayList<>();
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Correlation analysis for tracks:", 
                GeneralUtils.generateConcatenatedString(this.getTrackNameList(), 0)));
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("")); //placeholder between title and parameters
        
        statisticsExportData.add(ResultTrackAnalysis.createTableRow("Analysis parameters:"));
        for(Entry<String, Object> entry : this.params.entrySet()) {
            statisticsExportData.add(ResultTrackAnalysis.createTableRow(entry.getKey()+":", entry.getValue()));
        }
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
//        dataColumnDescriptions.add("Track");
        dataColumnDescriptions.add("Chromosome");
        dataColumnDescriptions.add("Strand Direction");
        dataColumnDescriptions.add("Pos From");
        dataColumnDescriptions.add("Pos To");
        dataColumnDescriptions.add("Correlation");
        dataColumnDescriptions.add("Minimum Peak Coverage");
        
        dataColumnDescriptionsList.add(dataColumnDescriptions);
        
        //add correlation result statistic sheet header
        List<String> statisticColumnDescriptions = new ArrayList<>();
        statisticColumnDescriptions.add("Analysis parameter and statistics table");
        dataColumnDescriptionsList.add(statisticColumnDescriptions);
        
        return dataColumnDescriptionsList;
    }
    
    /**
     * @return the correlation result sheet names ready to export with an 
     * {@link ExcelExporter}
     */
    @Override
    public List<String> dataSheetNames() {
        List<String> sheetNames = new ArrayList<>();
        sheetNames.add("Correlations");
        sheetNames.add("Analysis Statistics");
        return sheetNames;
    }
    

    /**
     * Sets used analysis parameters to have them connected with the search
     * results.
     * @param params 
     */
    public void setAnalysisParameters(Map<String, Object> params) {
        this.params = params;
    }
    
    /**
     * returns the used analysis parameters
     * @return 
     */
    public Map<String, Object> getAnalysisParameters() {
        return this.params;
    }

}
