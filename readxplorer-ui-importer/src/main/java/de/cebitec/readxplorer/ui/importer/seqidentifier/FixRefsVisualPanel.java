/*
 * Copyright (C) 2016 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.importer.seqidentifier;

import de.cebitec.readxplorer.api.objects.JobPanel;
import htsjdk.samtools.SAMSequenceDictionary;
import htsjdk.samtools.SAMSequenceRecord;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

import static de.cebitec.readxplorer.ui.importer.seqidentifier.ChangeableSeqName.LIST_FLAVOR;


/**
 * Visual wizard panel to manually fix the reference sequence ids in the mapping
 * file dictionary.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public final class FixRefsVisualPanel extends JobPanel {

    private static final long serialVersionUID = 1L;

    private List<String> chromNames;
    private final String fileNames;
    private SAMSequenceDictionary sequenceDictionary;
    private boolean isFixed = false;


    /**
     * Visual wizard panel to manually fix the reference sequence ids in the
     * mapping file dictionary.
     *
     * @param chromNames         Reference chromosome names
     * @param sequenceDictionary Mapping file sequence dictionary
     * @param fileNames          Concatenated names of the mapping files
     *                           associated to this panel
     */
    public FixRefsVisualPanel( List<String> chromNames, SAMSequenceDictionary sequenceDictionary, String fileNames ) {
        this.chromNames = chromNames;
        this.sequenceDictionary = sequenceDictionary;
        this.fileNames = fileNames;
        initComponents();
        initAdditionalComponents();
    }


    /**
     * Init data structures and both JLists.
     */
    private void initAdditionalComponents() {
        String shortFileNames = fileNames.length() > 80 ? fileNames.substring( 0, 80 ) + "..." : fileNames;
        captionLabel.setText( "<html><b>Sequence id correction for:</b> " + shortFileNames + "</html>" );

        populateData();

        refNamesList.setTransferHandler( new RefTransferHandler() );
        mappingSeqIdList.setTransferHandler( new MappingTransferHandler() );
    }


    /**
     * Add data to both JLists.
     */
    private void populateData() {

        DefaultListModel<String> defRefNamesModel = new DefaultListModel<>();
        DefaultListModel<ChangeableSeqName> defMappingSeqIdsModel = new DefaultListModel<>();
        for( String chromName : chromNames ) {
            defRefNamesModel.addElement( chromName );
        }

        for( SAMSequenceRecord seqRecord : sequenceDictionary.getSequences() ) {
            defMappingSeqIdsModel.addElement( new ChangeableSeqName( seqRecord ) );
        }

        refNamesList.setModel( defRefNamesModel );
        mappingSeqIdList.setModel( defMappingSeqIdsModel );
    }


    /**
     * Transfer handler for the mapping sequence id JList. It can import a list
     * of strings and exports a list of ChangeableSeqNames. Multi-export is
     * supported.
     */
    private class MappingTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;


        /**
         * @param c The component (JList)
         *
         * @return A list of ChangeableSeqNames
         */
        @Override
        protected Transferable createTransferable( JComponent c ) {
            return new ListTransferable( c );
        }


        @Override
        protected void exportDone( JComponent c, Transferable data, int action ) {
            ListTransferable transferable = (ListTransferable) data;
            int[] indices = transferable.getIndices();
            if( action == TransferHandler.MOVE && indices != null ) {
                @SuppressWarnings( "unchecked" )
                JList<String> source = (JList) c;
                @SuppressWarnings( "unchecked" )
                DefaultListModel<ChangeableSeqName> model = (DefaultListModel) source.getModel();
                for( int i = indices.length - 1; i >= 0; i-- ) {
                    model.get( indices[i] ).setNewSeqName( null );
                }
                source.paintAll( source.getGraphics() );
            }
            transferable.setIndices( null );
        }


        @Override
        public boolean canImport( TransferHandler.TransferSupport support ) {
            return support.isDrop() || support.isDataFlavorSupported( LIST_FLAVOR );
        }


        @Override
        public int getSourceActions( JComponent c ) {
            return TransferHandler.MOVE;
        }


        /**
         * Imports a new String into the JList with ChangeableSeqNames.
         *
         * @param support The transfer support
         *
         * @return true if the import worked, false otherwise
         */
        @Override
        @SuppressWarnings( { "unchecked" } )
        public boolean importData( TransferHandler.TransferSupport support ) {
            if( !canImport( support ) ) {
                return false;
            }

            // fetch the drop location
            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();

            int index = dl.getIndex();

            // fetch the data and bail if this fails
            List<String> refIds;
            try {
                refIds = (List<String>) support.getTransferable().getTransferData( LIST_FLAVOR );
            } catch( UnsupportedFlavorException | java.io.IOException e ) {
                return false;
            }

            //this is the receiving mapping seq ids JList
            JList<ChangeableSeqName> list = (JList<ChangeableSeqName>) support.getComponent();
            DefaultListModel<ChangeableSeqName> model = (DefaultListModel<ChangeableSeqName>) list.getModel();
            for( String refId : refIds ) {
                Enumeration<ChangeableSeqName> mappingSeqIds = model.elements();
                boolean contains = false;
                while( mappingSeqIds.hasMoreElements() && !contains ) {
                    contains = mappingSeqIds.nextElement().getNewName().equals( refId );
                }
                if( !contains ) {
                    model.get( index ).setNewSeqName( refId );

                    Rectangle rect = list.getCellBounds( index, index );
                    list.scrollRectToVisible( rect );
//                    list.setSelectedIndex( index );
                    list.requestFocusInWindow();
                    index++;
                }
            }
            list.paintAll( list.getGraphics() );

            return true;
        }


    }


    private class RefTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 1L;

        private int shuffleIndex = -1; //Location where items were added
        private int shuffleCount = 0;  //Number of items added.


        @Override
        public boolean canImport( TransferHandler.TransferSupport support ) {
            return support.isDrop() || support.isDataFlavorSupported( LIST_FLAVOR );
        }


        /**
         * Imports a new String into the JList with ChangeableSeqNames.
         *
         * @param support The transfer support
         *
         * @return true if the import worked, false otherwise
         */
        @Override
        @SuppressWarnings( { "unchecked" } )
        public boolean importData( TransferHandler.TransferSupport support ) {
            if( !canImport( support ) ) {
                return false;
            }

            // fetch the drop location
            JList.DropLocation dl = (JList.DropLocation) support.getDropLocation();

            int index = dl.getIndex();

            //this is the receiving JList
            JList<String> list = (JList<String>) support.getComponent();
            DefaultListModel<String> model = (DefaultListModel<String>) list.getModel();

            // fetch the data and bail if this fails
            List<ChangeableSeqName> mappingSeqIds;
            List<String> refIds;
            try {
                List transferData = (List) support.getTransferable().getTransferData( LIST_FLAVOR );
                if( !transferData.isEmpty() && transferData.get( 0 ) instanceof ChangeableSeqName ) {
                    mappingSeqIds = transferData;

                    for( ChangeableSeqName mappingSeqId : mappingSeqIds ) {
                        if( mappingSeqId.hasNewName() ) {
                            model.add( index, mappingSeqId.getNewName() );

                            Rectangle rect = list.getCellBounds( index, index );
                            list.scrollRectToVisible( rect );
//                list.setSelectedIndex( index );
                            list.requestFocusInWindow();
                            ++index;
                        }
                    }
                } else if( !transferData.isEmpty() && transferData.get( 0 ) instanceof String ) {
                    refIds = transferData;

                    for( String refId : refIds ) {
                        model.add( index, refId );

                        Rectangle rect = list.getCellBounds( index, index );
                        list.scrollRectToVisible( rect );
//                list.setSelectedIndex( index );
                        list.requestFocusInWindow();
                        shuffleIndex = index;
                        ++index;
                        ++shuffleCount;
                    }
                }
            } catch( UnsupportedFlavorException | java.io.IOException ex ) {
                return false;
            }

            list.paintAll( list.getGraphics() );
            return true;
        }


        @Override
        protected Transferable createTransferable( JComponent c ) {
            return new ListTransferable( c );
        }


        @Override
        public int getSourceActions( JComponent c ) {
            return TransferHandler.MOVE;
        }


        @Override
        protected void exportDone( JComponent c, Transferable data, int action ) {
            ListTransferable transferable = (ListTransferable) data;
            int[] indices = transferable.getIndices();
            if( action == TransferHandler.MOVE && indices != null ) {
                @SuppressWarnings( "unchecked" )
                JList<String> source = (JList) c;
                @SuppressWarnings( "unchecked" )
                DefaultListModel<String> model = (DefaultListModel) source.getModel();
                //If we are moving items around in the same list, we
                //need to adjust the indices accordingly, since those
                //after the insertion point have moved.
                if( shuffleCount > 0 ) {
                    for( int i = 0; i < indices.length; i++ ) {
                        if( indices[i] > shuffleIndex ) {
                            indices[i] += shuffleCount;
                        }
                    }
                }
                for( int i = indices.length - 1; i >= 0; i-- ) {
                    model.remove( indices[i] );
                }
            }
            transferable.setIndices( null );
            shuffleCount = 0;
            shuffleIndex = -1;
        }


    }


    /**
     * A transferable for lists.
     */
    private class ListTransferable implements Transferable {

        private int[] indices;
        private JComponent c;


        /**
         * A transferable for lists.
         *
         * @param indices array of treated indices
         * @param c       component whose data is transferred
         */
        public ListTransferable( JComponent c ) {
            this.indices = null;
            this.c = c;
        }


        public int[] getIndices() {
            return indices;
        }


        public void setIndices( int[] indices ) {
            this.indices = indices;
        }


        @Override
        @SuppressWarnings( "unchecked" )
        public Object getTransferData( DataFlavor flavor ) throws UnsupportedFlavorException, IOException {
            JList selectedValues = ((JList) c);
            indices = selectedValues.getSelectedIndices();
            return selectedValues.getSelectedValuesList();
        }


        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{ LIST_FLAVOR };
        }


        @Override
        public boolean isDataFlavorSupported( DataFlavor flavor ) {
            return LIST_FLAVOR.equals( flavor );
        }


    }


    @Override
    public String getName() {
        return "Fix reference sequence ids";
    }


    /** This method is called from within the constructor to initialize the
     * form. WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane3 = new javax.swing.JScrollPane();
        refNamesList = new javax.swing.JList<>();
        jScrollPane4 = new javax.swing.JScrollPane();
        mappingSeqIdList = new javax.swing.JList<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        captionLabel = new javax.swing.JLabel();

        refNamesList.setDragEnabled(true);
        refNamesList.setDropMode(javax.swing.DropMode.INSERT);
        jScrollPane3.setViewportView(refNamesList);

        mappingSeqIdList.setDragEnabled(true);
        mappingSeqIdList.setDropMode(javax.swing.DropMode.ON);
        jScrollPane4.setViewportView(mappingSeqIdList);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FixRefsVisualPanel.class, "FixRefsVisualPanel.jLabel1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FixRefsVisualPanel.class, "FixRefsVisualPanel.jLabel2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(FixRefsVisualPanel.class, "FixRefsVisualPanel.jLabel3.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(captionLabel, org.openide.util.NbBundle.getMessage(FixRefsVisualPanel.class, "FixRefsVisualPanel.captionLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)))
                .addGap(6, 6, 6)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 230, javax.swing.GroupLayout.PREFERRED_SIZE)))
            .addComponent(captionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(captionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(192, 192, 192))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 352, Short.MAX_VALUE)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING))))
        );
    }// </editor-fold>//GEN-END:initComponents


    @Override
    public boolean isRequiredInfoSet() {
        return true;
    }


    /**
     * Loads the last selected parameters into the component.
     */
    private void loadLastParameterSelection() {
        //Currently no parameters to restore
        //TODO: option to show wizard whenever at least one mapping id is not resolvable
    }


    /**
     * @return The fixed sequence dictionary.
     */
    public SAMSequenceDictionary getDictionary() {
        SAMSequenceDictionary fixedSeqDictionary = new SAMSequenceDictionary();
        ListModel<ChangeableSeqName> mappingIdList = mappingSeqIdList.getModel();
        for( int i = 0; i < mappingIdList.getSize(); i++ ) {
            ChangeableSeqName mappingId = mappingIdList.getElementAt( i );
            fixedSeqDictionary.addSequence( new SAMSequenceRecord( mappingId.getNewName(), mappingId.getSeqRecord().getSequenceLength() ) );
            if( !isFixed ) {
                isFixed = mappingId.hasNewName();
            }
        }
        return fixedSeqDictionary;
    }


    /**
     * @return <code>true</code> if at least one of the mapping sequence ids has
     *         been associated to a reference sequence id, <code>false</code>
     *         otherwise.
     */
    public boolean isFixed() {
        return isFixed;
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel captionLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JList<ChangeableSeqName> mappingSeqIdList;
    private javax.swing.JList<String> refNamesList;
    // End of variables declaration//GEN-END:variables
}
