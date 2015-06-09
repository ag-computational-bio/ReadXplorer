/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.ui.tablevisualization;


import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.utils.PositionUtils;
import de.cebitec.readxplorer.utils.UneditableTableModel;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static java.util.logging.Level.INFO;


/**
 * Provides some basic table utils for ReadXplorer.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public final class TableUtils {

    private static final Logger LOG = Logger.getLogger( TableUtils.class.getName() );


    /**
     * Do not instantiate.
     */
    private TableUtils() {
    }


    /**
     * Transforms the selected row index in the table view to the selected index
     * of the underlying table model (in case the results are sorted, they can
     * be different). If the transformation is not possible, it returns -1
     * <p>
     * @param table the table for which the selected model row is needed
     * <p>
     * @return The selected model row index or -1, if the index is out of bounds
     *         or cannot be calculated
     */
    public static int getSelectedModelRow( JTable table ) {
        int wantedModelIdx = -1;
        DefaultListSelectionModel model = (DefaultListSelectionModel) table.getSelectionModel();
        int selectedView = model.getLeadSelectionIndex();

        if( table.getModel().getRowCount() > selectedView && selectedView >= 0 ) {
            try {
                int selectedModelIdx = table.convertRowIndexToModel( selectedView );

                if( table.getModel().getRowCount() > selectedModelIdx ) {
                    wantedModelIdx = selectedModelIdx;
                }
            } catch( ArrayIndexOutOfBoundsException e ) {
                LOG.log( INFO, "Selected row transformation not possible due to ArrayIndexOutOfBoundsException" );
                //do nothing, just return -1 since the transformation was not possible
            }
        }
        return wantedModelIdx;
    }


    /**
     * Updates the navigator bar of all viewers to the given position and
     * chromosome, which might be in String or Integer format or updates the
     * viewers to the start position of a selected PersistentFeature with
     * respect to the strand on which the feature is located.
     * <p>
     * @param table          the table whose selected element's position is to
     *                       be shown
     * @param posColumnIndex the index of the table model column holding the
     *                       position
     * @param chromColumnIdx The index of the table model column holding the
     *                       chromosome on which the position to show is
     *                       located. If the position column contains
     *                       PersistentFeatures, the chromColumnIdx can be set
     *                       to -1 and the chromosome information is obtained
     *                       from the selected PersistentFeature.
     * @param bim            the bounds information manager which should be
     *                       updated
     */
    public static void showPosition( JTable table, int posColumnIndex, int chromColumnIdx,
                                     BoundsInfoManager bim ) {
        TableUtils.showPosition( table, posColumnIndex, chromColumnIdx, bim, null );
    }


    /**
     * Updates the navigator bar of all viewers to the given position and
     * chromosome, which might be in String or Integer format or updates the
     * viewers to the start position of a selected PersistentFeature with
     * respect to the strand on which the feature is located.
     * <p>
     * @param table          the table whose selected element's position is to
     *                       be shown
     * @param posColumnIndex the index of the table model column holding the
     *                       position
     * @param chromColumnIdx The index of the table model column holding the
     *                       chromosome on which the position to show is
     *                       located. If the position column contains
     *                       PersistentFeatures, the chromColumnIdx can be set
     *                       to -1 and the chromosome information is obtained
     *                       from the selected PersistentFeature.
     * @param bim            the bounds information manager which should be
     *                       updated
     * @param reference      The reference belonging to this data table. NOTE:
     *                       It is only needed, if the chromosome column does
     *                       not contain the chromosome, but only its name!
     *                       Otherwise the reference can be <code>null</code>
     */
    public static void showPosition( JTable table, int posColumnIndex, int chromColumnIdx,
                                     BoundsInfoManager bim, PersistentReference reference ) {

        int selectedModelRow = TableUtils.getSelectedModelRow( table );
        if( selectedModelRow > -1 ) {
            Object posValue = table.getModel().getValueAt( selectedModelRow, posColumnIndex );

            if( posValue instanceof PersistentFeature ) {

                //switch chromosome
                PersistentFeature feature = (PersistentFeature) posValue;
                bim.chromosomeChanged( feature.getChromId() );
                //jump to position
                int pos = feature.getStartOnStrand();
                bim.navigatorBarUpdated( pos );

            } else {
                Object chromValue = table.getModel().getValueAt( selectedModelRow, chromColumnIdx );
                PersistentChromosome chrom = null;
                if( chromValue instanceof PersistentChromosome ) {
                    bim.chromosomeChanged( ((PersistentChromosome) chromValue).getId() );
                } else if( chromValue instanceof String && reference != null ) {
                    Map<String, PersistentChromosome> chromMap = PersistentChromosome.getChromNameMap( reference.getChromosomes().values() );
                    String chromName = (String) chromValue;
                    if( chromMap.containsKey( chromName ) ) {
                        chrom = chromMap.get( chromName );
                        bim.chromosomeChanged( chrom.getId() );
                    }
                }

                if( posValue instanceof Integer ) {
                    bim.navigatorBarUpdated( (Integer) posValue );

                } else if( posValue instanceof String ) {
                    String[] posArray = ((String) posValue).split( "\n" );
                    try {
                        // Get first position in the array
                        bim.navigatorBarUpdated( PositionUtils.convertPosition( posArray[0] ) );

                    } catch( NumberFormatException e ) {
                        //could be a feature locus then for imported tables
                        if( reference != null && chrom != null ) {
                            List<PersistentFeature> features = ProjectConnector.getInstance().getRefGenomeConnector( reference.getId() )
                                    .getFeaturesForClosedInterval( 0, chrom.getLength(), chrom.getId() );
                            Map<String, PersistentFeature> featMap = PersistentFeature.Utils.getFeatureLocusMap( features );
                            String featLocus = (String) posValue;
                            if( featMap.containsKey( featLocus ) ) {
                                PersistentFeature feature = featMap.get( featLocus );
                                int pos = feature.getStartOnStrand();
                                bim.navigatorBarUpdated( pos );
                            }
                        }
                    }
                }
            }
        }
    }


    /**
     * Updates the navigator bar of all viewers to the start position of the
     * selected PersistentFeature on the correct chromosome with respect to the
     * strand on which the feature is located.
     * <p>
     * @param table           the table whose selected PersistentFeature
     *                        position is to be shown
     * @param featColumnIndex the index of the table model column holding the
     *                        PersistentFeature
     * @param bim             the bounds information manager which should be
     *                        updated
     */
    public static void showFeaturePosition( JTable table, int featColumnIndex, BoundsInfoManager bim ) {
        TableUtils.showPosition( table, featColumnIndex, -1, bim, null );
    }


    /**
     * Transforms an arbitrary list of data into a table model. The first list
     * of lists should contain the headers, while the following lists should
     * contain the table rows.
     * <p>
     * @param dataToTransform The list of data to be transformed into a table
     *                        model ready for display.
     * <p>
     * @return The table model filled with the given dataToTransform.
     */
    public static UneditableTableModel transformDataToTableModel( List<List<?>> dataToTransform ) {
        UneditableTableModel newModel = null;
        if( dataToTransform.size() > 1 ) {
            Object[] headers = dataToTransform.get( 0 ).toArray();
            dataToTransform.remove( 0 );

            Object[][] newDataArray = new Object[dataToTransform.size()][];
            for( int i = 0; i < dataToTransform.size(); ++i ) {
                Object[] row = dataToTransform.get( i ).toArray();
                newDataArray[i] = row;
            }
            newModel = new UneditableTableModel( newDataArray, headers );
        }
        return newModel;
    }


    /**
     * Adds empty columns to a table row. They contain the empty string.
     * <p>
     * @param noColumns The number of empty columns to add
     * @param tableRow  The table row to update with empty rows
     */
    public static void addEmptyColumns( int noColumns, List<Object> tableRow ) {
        for( int i = 0; i < noColumns; i++ ) {
            tableRow.add( "" );
        }
    }


    /**
     * Adds a {@link ListSelectionListener} to the given table which enables
     * showing the genomic position of the currently selected table row via the
     * given {@link BoundsInfoManager}.
     * <p>
     * @param table       Add the listener to this table
     * @param posColumn   Table column containing the genomic position
     * @param chromColumn Table column containing the chromosome
     * @param bim         BoundsInfoManager whose position has to be updated by
     *                    the listener
     */
    public static void addTableListSelectionListener( JTable table, int posColumn, int chromColumn, BoundsInfoManager bim ) {
        DefaultListSelectionModel model = (DefaultListSelectionModel) table.getSelectionModel();
        model.addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showPosition( table, posColumn, chromColumn, bim );
            }


        } );
    }


}
