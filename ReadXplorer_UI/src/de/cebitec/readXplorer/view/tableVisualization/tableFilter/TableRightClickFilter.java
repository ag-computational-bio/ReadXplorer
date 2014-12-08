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
package de.cebitec.readXplorer.view.tableVisualization.tableFilter;


import de.cebitec.readXplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readXplorer.util.GenerateRowSorter;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * A MouseAdapter, which offers a filter for the columns of a table. An
 * instance of this class must be added as a listener to the TableHeader of the
 * table that should be filtered. Only tables using a model extending
 * DefaultTableModel can be used!
 * <p>
 * @param <E> the table model, which has to extend the DefaultTableModel.
 * <p>
 * @author kstaderm, rhilker & Margarita Steinhauer
 */
public class TableRightClickFilter<E extends DefaultTableModel> extends MouseAdapter {

    private JPopupMenu popup = new JPopupMenu();
    private JTable lastTable;
    /**
     * Stores the original TableModel.
     */
    private E originalTableModel = null;
    private int lastSelectedColumn;
    private JMenuItem numberColumnLowerItem;
    private JMenuItem numberColumnHigherItem;
    private JMenuItem stringColumnItem;
    private JMenuItem resetItem;
    private JMenuItem occurrenceFilter;
    private final Class<E> classType;
    private Map<Integer, PersistentTrack> trackMap;
    private final int posColumn;
    private final int trackColumn;


    /**
     * A MouseAdapter, which offers a filter for the columns of a table. An
     * instance of this class must be added as a listener to the TableHeader of
     * the table that should be filtered. Only tables using a model extending
     * DefaultTableModel can be used!
     *
     * @param classType   the type of the table model, which has to extend the
     *                    DefaultTableModel.
     * @param posColumn   column containing the position information
     * @param trackColumn column containing the track information
     */
    public TableRightClickFilter( Class<E> classType, int posColumn, int trackColumn ) {
        this.classType = classType;
        this.posColumn = posColumn;
        this.trackColumn = trackColumn;
        init();
    }


    /**
     * If a filtered table is changed externally this method must be called. If
     * not the filter will not know that there is a new original TableModel.
     */
    public void resetOriginalTableModel() {
        resetItem.setEnabled( false );
        originalTableModel = null;
    }


    /**
     * Getter for lastTable. needed in FilterOccurrence class.
     *
     * @return lastTable
     */
    public JTable getLastTable() {
        return lastTable;
    }


    /**
     * Getter for classType. needed in FilterOccurrence class.
     * <p>
     * @return classType
     */
    public Class<E> getClassType() {
        return classType;
    }


    /**
     * Initializes the occurrence filter, which filters a table by occurrences
     * of the same event in different tracks. First an option panel is openend
     * and then the filtering is carried out for the selected options.
     */
    @NbBundle.Messages( {
        "OccurrenceItemTitle=Filter occurrence by...",
        "Occurrence_Filter=Occurrence Filter" } )
    private void init() {
        occurrenceFilter = new JMenuItem( Bundle.OccurrenceItemTitle() );
        occurrenceFilter.addActionListener( new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e ) {
                OccurrenceSelectionPanel occurrencePanel = new OccurrenceSelectionPanel();
                final JButton okButton = new JButton( "OK" );
                ActionListener okButtonListener = this.createOccurrenceFilterListener( occurrencePanel );
                DialogDescriptor dialogDescriptor = new DialogDescriptor( occurrencePanel,
                                                                          Bundle.Occurrence_Filter(), true,
                                                                          new JButton[]{ okButton }, okButton, DialogDescriptor.DEFAULT_ALIGN, null, okButtonListener );
                dialogDescriptor.setClosingOptions( null );
                Dialog openRefGenDialog = DialogDisplayer.getDefault().createDialog( dialogDescriptor );
                openRefGenDialog.setVisible( true );
            }


            /**
             * Action to filter a table by occurrences of the same event in
             * different tracks.
             * <p>
             * @param occurrencePanel the panel, which collected the options for
             *                        the filtering process
             * <p>
             * @return the action to filter a table by occurrences of the same
             *         event in different tracks.
             */
            private ActionListener createOccurrenceFilterListener( final OccurrenceSelectionPanel occurrencePanel ) {
                ActionListener listener = new ActionListener() {
                    @Override
                    public void actionPerformed( ActionEvent e ) {
                        // The OccurrenceField only accepts positive numbers
                        String occurrenceNumberString = occurrencePanel.getOccurrenceField().getText();
                        Integer occurrenceCount = 0;
                        if( !occurrenceNumberString.isEmpty() ) {
                            occurrenceCount = Integer.parseInt( occurrenceNumberString );
                        }
                        FilterOccurrence<E> filter = new FilterOccurrence<>( occurrencePanel.getSelectedButton(),
                                                                             occurrenceCount, TableRightClickFilter.this, trackColumn, posColumn );
                        filter.filterTable();
                    }


                };
                return listener;
            }


        } );
        popup.add( occurrenceFilter );

        numberColumnLowerItem = new JMenuItem( "Remove values smaller than..." );
        numberColumnLowerItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                String input = openPopUp( "Remove values smaller than: " );
                if( input != null ) {
                    input = input.replace( ",", "." );
                    try {
                        Double cutoff = Double.parseDouble( input );
                        E newModel = filterValuesSmallerThan(
                                (E) lastTable.getModel(), cutoff, lastSelectedColumn );
                        setNewTableModel( newModel );
                    }
                    catch( NumberFormatException nfe ) {
                        JOptionPane.showMessageDialog( null, "Please insert a valid number value." );
                    }
                }
            }


        } );
        popup.add( numberColumnLowerItem );

        numberColumnHigherItem = new JMenuItem( "Remove values larger than..." );
        numberColumnHigherItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                String input = openPopUp( "Remove values larger than: " );
                if( input != null ) {
                    input = input.replace( ",", "." );
                    try {
                        Double cutoff = Double.parseDouble( input );
                        E newModel = filterValuesLargerThan(
                                (E) lastTable.getModel(), cutoff, lastSelectedColumn );
                        setNewTableModel( newModel );
                    }
                    catch( NumberFormatException nfe ) {
                        JOptionPane.showMessageDialog( null, "Please insert a valid value." );
                    }
                }
            }


        } );
        popup.add( numberColumnHigherItem );

        stringColumnItem = new JMenuItem( "Set pattern filter" );
        stringColumnItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                String input = openPopUp( "Only show rows with this pattern: " );
                if( input != null ) {
                    try {
                        Pattern.compile( input );
                        E newModel = filterRegExp( (E) lastTable.getModel(), input, lastSelectedColumn );
                        setNewTableModel( newModel );
                    }
                    catch( PatternSyntaxException pse ) {
                        JOptionPane.showMessageDialog( null, "Please enter a valid pattern." );
                    }
                }
            }


        } );
        popup.add( stringColumnItem );

        resetItem = new JMenuItem( "Reset all filters" );
        resetItem.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed( ActionEvent e ) {
                setNewTableModel( originalTableModel );
                resetOriginalTableModel();
            }


        } );
        popup.add( resetItem );

        numberColumnLowerItem.setEnabled( false );
        numberColumnHigherItem.setEnabled( false );
        stringColumnItem.setEnabled( false );
        resetItem.setEnabled( false );
    }


    @Override
    public void mouseReleased( MouseEvent e ) {
        if( SwingUtilities.isRightMouseButton( e ) ) {
            lastTable = ((JTableHeader) e.getSource()).getTable();
            lastSelectedColumn = lastTable.columnAtPoint( e.getPoint() );
            if( lastTable.getModel().getRowCount() > 0 ) {
                Object testValue = lastTable.getModel().getValueAt( 0, lastSelectedColumn );
                if( testValue instanceof Number ) {
                    numberColumnLowerItem.setEnabled( true );
                    numberColumnHigherItem.setEnabled( true );
                    stringColumnItem.setEnabled( false );
                }
                if( testValue instanceof String ) {
                    numberColumnLowerItem.setEnabled( false );
                    numberColumnHigherItem.setEnabled( false );
                    stringColumnItem.setEnabled( true );
                }
                Object trackValue = lastTable.getModel().getValueAt( 0, trackColumn );
                boolean isValidTrackColumn = trackValue instanceof PersistentTrack;
                occurrenceFilter.setEnabled( isValidTrackColumn );
                popup.show( e.getComponent(), e.getX(), e.getY() );
            }
        }
    }


    /**
     * Sets the model of the lastTable. Also sets the original table model,
     * if this is the first call of the method.
     * <p>
     * @param newTableModel the new table model to set
     */
    void setNewTableModel( E newTableModel ) {
        E tableModel = (E) lastTable.getModel();
        if( originalTableModel == null ) {
            TableFilterUtils<E> utils = new TableFilterUtils<>( classType );
            E tmpModel = utils.prepareNewTableModel( tableModel );
            for( Iterator<Vector> it = tableModel.getDataVector().iterator(); it.hasNext(); ) {
                Vector row = it.next();
                tmpModel.addRow( row );
            }
            this.originalTableModel = tmpModel;
            resetItem.setEnabled( true );
        }
        lastTable.setModel( newTableModel );
        lastTable.setRowSorter( GenerateRowSorter.createRowSorter( newTableModel ) );
    }


    private String openPopUp( String message ) {
        String input = JOptionPane.showInputDialog( null, message,
                                                    "Value selection",
                                                    JOptionPane.PLAIN_MESSAGE );
        return input;
    }


    private E filterValuesLargerThan( E tableModel, Double cutoff, int column ) {
        FilterDoubleValues<E> doubleFilter = new FilterDoubleValues<>( new FilterLargeValues(), classType );
        E filteredTableModel = doubleFilter.filterTable( tableModel, column, cutoff );
        return filteredTableModel;
    }


    private E filterValuesSmallerThan( E tableModel, Double cutoff, int column ) {
        FilterDoubleValues<E> doubleFilter = new FilterDoubleValues<>( new FilterSmallValues(), classType );
        E filteredTableModel = doubleFilter.filterTable( tableModel, column, cutoff );
        return filteredTableModel;
    }


    private E filterRegExp( E tm, String pattern, int column ) {
        FilterStrings<E> patternFilter = new FilterStrings<>( classType );
        E filteredTableModel = patternFilter.filterTable( tm, column, pattern );
        return filteredTableModel;
    }


    public void setTrackMap( Map<Integer, PersistentTrack> trackMap ) {
        this.trackMap = trackMap;
    }


    public Map<Integer, PersistentTrack> getTrackMap() {
        return trackMap;
    }


}
