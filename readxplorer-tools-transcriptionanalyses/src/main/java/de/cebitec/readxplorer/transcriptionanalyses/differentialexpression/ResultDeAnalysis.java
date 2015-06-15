/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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


import java.util.ArrayList;
import java.util.List;
import org.openide.util.Exceptions;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REXPVector;


/**
 *
 * @author kstaderm
 */
public class ResultDeAnalysis {

    private final String description;
    private List<REXPVector> rawTableContents;
    private List<List<Object>> tableContents = null;
    private REXP rawColNames;
    private List<Object> colNames = null;
    private REXP rawRowNames;
    private List<Object> rowNames = null;
    private DeAnalysisData dEAdata;


    public ResultDeAnalysis( List<List<Object>> tableContents, List<Object> colNames, List<Object> rowNames, String description ) {
        this.tableContents = tableContents;
        this.colNames = colNames;
        this.rowNames = rowNames;
        this.description = description;
    }


    public ResultDeAnalysis( List<REXPVector> rawTableContents, REXP rawColNames, REXP rawRowNames, String description, DeAnalysisData dEAdata ) {
        this.rawTableContents = rawTableContents;
        this.rawColNames = rawColNames;
        this.rawRowNames = rawRowNames;
        this.description = description;
        this.dEAdata = dEAdata;
    }


    public List<List<Object>> getTableContentsContainingRowNames() {
        List<Object> rnames = getRownames();
        List<List<Object>> data = getTableContents();
        for( int i = 0; i < rnames.size(); i++ ) {
            data.get( i ).add( 0, rnames.get( i ) );
        }
        return data;
    }


    public List<List<Object>> getTableContents() {
        if( tableContents == null ) {
            tableContents = convertRresults( rawTableContents );
        }
        return new ArrayList<>( tableContents );
    }


    public List<Object> getColnames() {
        if( colNames == null ) {
            colNames = convertNames( rawColNames );
        }
        return new ArrayList<>( colNames );
    }


    public List<Object> getRownames() {
        if( rowNames == null ) {
            rowNames = convertNames( rawRowNames );
        }
        return new ArrayList<>( rowNames );
    }


    public String getDescription() {
        return description;
    }

    /*
     * The manual array copy used in this method several times is intended! This
     * way the primitive data types are automatically converted to their
     * corresponding Object presentation.
     */

    private List<Object> convertNames( REXP currentValues ) {

        List<Object> current = new ArrayList<>();
        try {
            if( currentValues.isString() ) {
                String[] currentStringValues = currentValues.asStrings();
                for( String name : currentStringValues ) {
                    if( dEAdata.existsPersistentFeatureForGNURName( name ) ) {
                        current.add( dEAdata.getPersistentFeatureByGNURName( name ) );
                    } else {
                        current.add( name );
                    }
                }
            } else if( currentValues.isNumeric() ) {
                Object currentValuesAsObject = currentValues.asNativeJavaObject();
                if( currentValuesAsObject instanceof double[] ) {
                    double[] tmp = (double[]) currentValuesAsObject;
                    for( int i = 0; i < tmp.length; i++ ) {
                        current.add( tmp[i] );
                    }
                }
                if( currentValuesAsObject instanceof int[] ) {
                    int[] tmp = (int[]) currentValuesAsObject;
                    for( int i = 0; i < tmp.length; i++ ) {
                        current.add( tmp[i] );
                    }
                }
                if( currentValuesAsObject instanceof float[] ) {
                    float[] tmp = (float[]) currentValuesAsObject;
                    for( int i = 0; i < tmp.length; i++ ) {
                        current.add( tmp[i] );
                    }
                } else if( currentValuesAsObject instanceof String[] ) {
                    String[] currentStringValues = (String[]) currentValuesAsObject;
                    for( String name : currentStringValues ) {
                        if( dEAdata.existsPersistentFeatureForGNURName( name ) ) {
                            current.add( dEAdata.getPersistentFeatureByGNURName( name ) );
                        } else {
                            current.add( name );
                        }
                    }
                }
            }
        } catch( REXPMismatchException ex ) {
            Exceptions.printStackTrace( ex );
        }
        return current;
    }


    /**
     * Converts and RVector of data into a Vector of Vectors = table content.
     * <p>
     * @param currentRVector The RVector to convert
     * <p>
     * @return A Vector of Vectors = table content, generated from the given
     *         RVector.
     */
    private List<List<Object>> convertRresults( final List<REXPVector> currentRVector ) {

        List<List<Object>> current = new ArrayList<>();
        for( REXPVector currentRVector1 : currentRVector ) {
            List<Object> converted = convertNames( currentRVector1 );
            for( int j = 0; j < converted.size(); j++ ) {

//                if( j>=current.size() )
//                    current.add( new ArrayList<>() );
                try {
                    current.get( j );
                } catch( IndexOutOfBoundsException e ) {
                    current.add( new ArrayList<>() );
                }
                current.get( j ).add( converted.get( j ) );
            }
        }

        // assign chromosomes to the column next to the PersistentFeature column
        // TODO: This makes this converter methode to specific. It was
        // intended to convert any GNU R table but the following code assumes
        // that the first column always contains a PersistentFeature which is not
        // always true (e.g. DESeq2). I have to finde a better solution.
//        for (int i = 0; i < current.size(); i++) {
//            current.get(i).insertElementAt(chromMap.get(((PersistentFeature) current.get(i).get(0)).getChromId()), 1);
//        }
        return current;
    }


}
