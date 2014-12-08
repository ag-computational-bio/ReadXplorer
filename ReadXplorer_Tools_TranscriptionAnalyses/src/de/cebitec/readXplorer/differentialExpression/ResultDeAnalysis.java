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

package de.cebitec.readXplorer.differentialExpression;


import java.util.Vector;
import org.rosuda.JRI.REXP;
import org.rosuda.JRI.RFactor;
import org.rosuda.JRI.RVector;


/**
 *
 * @author kstaderm
 */
public class ResultDeAnalysis {

    private final String description;
    private RVector rawTableContents;
    private Vector<Vector> tableContents = null;
    private REXP rawColNames;
    private Vector colNames = null;
    private REXP rawRowNames;
    private Vector rowNames = null;
    private DeAnalysisData dEAdata;


    public ResultDeAnalysis( RVector tableContents, REXP colnames, REXP rownames, String description, DeAnalysisData dEAdata ) {
        rawTableContents = tableContents;
        rawColNames = colnames;
        rawRowNames = rownames;
        this.description = description;
        this.dEAdata = dEAdata;
    }


    public ResultDeAnalysis( Vector<Vector> tableContents, Vector colNames, Vector rowNames, String description ) {
        this.tableContents = tableContents;
        this.colNames = colNames;
        this.rowNames = rowNames;
        this.description = description;
    }


    public Vector<Vector> getTableContentsContainingRowNames() {
        Vector rnames = getRownames();
        Vector<Vector> data = getTableContents();
        for( int i = 0; i < rnames.size(); i++ ) {
            data.get( i ).add( 0, rnames.get( i ) );
        }
        return data;
    }


    public Vector<Vector> getTableContents() {
        if( tableContents == null ) {
            tableContents = convertRresults( rawTableContents );
        }
        return tableContents;
    }


    public Vector getColnames() {
        if( colNames == null ) {
            colNames = convertNames( rawColNames );
//            colNames.insertElementAt("Chromosome", 1);
        }
        return colNames;
    }


    public Vector getRownames() {
        if( rowNames == null ) {
            rowNames = convertNames( rawRowNames );
        }
        return rowNames;
    }


    public String getDescription() {
        return description;
    }

    /*
     * The manual array copy used in this method several times is intended!
     * This way the primitive data types are automatically converted to their
     * corresponding Object presentation.
     */

    private Vector convertNames( REXP currentValues ) {
        int currentType = currentValues.getType();
        Vector current = new Vector();
        switch( currentType ) {
            case REXP.XT_ARRAY_DOUBLE:
                double[] currentDoubleValues = currentValues.asDoubleArray();
                for( int j = 0; j < currentDoubleValues.length; j++ ) {
                    current.add( currentDoubleValues[j] );
                }
                break;
            case REXP.XT_ARRAY_INT:
                int[] currentIntValues = currentValues.asIntArray();
                for( int j = 0; j < currentIntValues.length; j++ ) {
                    current.add( currentIntValues[j] );
                }
                break;
            case REXP.XT_ARRAY_STR:
                String[] currentStringValues = currentValues.asStringArray();
                for( int j = 0; j < currentStringValues.length; j++ ) {
                    String name = currentStringValues[j];
                    if( dEAdata.existsPersistentFeatureForGNURName( name ) ) {
                        current.add( dEAdata.getPersistentFeatureByGNURName( name ) );
                    }
                    else {
                        current.add( name );
                    }
                }
                break;
            case REXP.XT_ARRAY_BOOL_INT:
                int[] currentBoolValues = currentValues.asIntArray();
                for( int j = 0; j < currentBoolValues.length; j++ ) {
                    current.add( currentBoolValues[j] == 1 );
                }
                break;
            case REXP.XT_FACTOR:
                RFactor factor = currentValues.asFactor();
                for( int j = 0; j < factor.size(); j++ ) {
                    String name = factor.at( j );
                    if( dEAdata.existsPersistentFeatureForGNURName( name ) ) {
                        current.add( dEAdata.getPersistentFeatureByGNURName( name ) );
                    }
                    else {
                        current.add( name );
                    }
                }
                break;
        }
        return current;
    }


    /**
     * Converts and RVector of data into a Vector of Vectors = table content.
     *
     * @param currentRVector The RVector to convert
     * <p>
     * @return A Vector of Vectors = table content, generated from the given
     *         RVector.
     */
    private Vector<Vector> convertRresults( RVector currentRVector ) {
        Vector<Vector> current = new Vector<>();
        for( int i = 0; i < currentRVector.size(); i++ ) {
            REXP currentValues = currentRVector.at( i );
            Vector converted = convertNames( currentValues );
            for( int j = 0; j < converted.size(); j++ ) {
                try {
                    current.get( j );
                }
                catch( ArrayIndexOutOfBoundsException e ) {
                    current.add( new Vector() );
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
