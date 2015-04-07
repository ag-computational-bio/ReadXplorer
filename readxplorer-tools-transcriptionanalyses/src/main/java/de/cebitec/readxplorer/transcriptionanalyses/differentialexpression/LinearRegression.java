/*
 * Copyright (C) 2015 AM
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression;

import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import java.util.HashMap;
import java.util.Map;
import java.util.Observer;
import java.util.Set;



/**
 *
 * @author Agne Matuseviciute
 */
public class LinearRegression implements LinearRegressionI {
    private Map<PersistentFeature, double[]> genesToScoreMap = new HashMap<>();

    public LinearRegression() {
    }
    
    @Override
    public void addObserver( Observer o ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }


    @Override
    public Map<PersistentFeature, double[]> getResults() throws IllegalStateException {
        return genesToScoreMap;
    }


    @Override
    public void process( Map<Integer, Map<PersistentFeature, int[]>> countData ) {
        Set<Integer> conditions = countData.keySet();
        Integer[] conditionsArray = conditions.toArray(
            new Integer[conditions.size()]);
        Map<PersistentFeature, int[]> geneSet1 =
            countData.get(conditionsArray[0]);
        Map<PersistentFeature, int[]> geneSet2 =
            countData.get(conditionsArray[1]);
        
         genesToScoreMap = this.findSimilarity(geneSet1, geneSet2);
    }


    @Override
    public void removeObserver( Observer o ) {
        throw new UnsupportedOperationException( "Not supported yet." ); //To change body of generated methods, choose Tools | Templates.
    }
    
    private  Map<PersistentFeature, double[]> findSimilarity(Map<PersistentFeature, int[]> geneSet1,
                Map<PersistentFeature, int[]> geneSet2) {
        int limit = 10;
        Map<PersistentFeature, double[]> result = new HashMap<>();
        for (Map.Entry<PersistentFeature, int[]> gene1 :
            geneSet1.entrySet() ){
            for (Map.Entry<PersistentFeature, int[]> gene2 :
                geneSet2.entrySet()) {
                if (gene1.getKey() == null ? gene2.getKey() ==
                    null : gene1.getKey().equals( gene2.getKey() )) {
                    int [] gene1Values = gene1.getValue();
                    int [] gene2Values = gene2.getValue();
                    if( coverageFilter( gene1Values, limit ) ||
                        coverageFilter( gene2Values, limit ) ) {
                        CalculatePerpendicular rSqrt = new CalculatePerpendicular();
                        double[] calculation = rSqrt.calculate( gene1Values, gene2Values );
                        result.put(gene1.getKey(), calculation);
                    }
                }                            
            }                          
        }
        return result;
    }

    private boolean coverageFilter(int[] geneValues, int limit){
        
        for( int k = 0; k < geneValues.length; k++ ){
                        if( geneValues[k] >= limit ) {
                            return true;
                        }
        }
        return true;
    }
    
}
