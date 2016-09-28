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

package de.cebitec.readxplorer.ui.visualisation.reference;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.BoundsInfoManager;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.IThumbnailView;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.dialogmenus.ChromosomeVisualizationHelper;
import de.cebitec.readxplorer.ui.dialogmenus.ChromosomeVisualizationHelper.ChromComboObserver;
import de.cebitec.readxplorer.ui.dialogmenus.ChromosomeVisualizationHelper.ChromosomeListener;
import de.cebitec.readxplorer.ui.dialogmenus.JTextFieldPasteable;
import de.cebitec.readxplorer.ui.dialogmenus.StandardMenuEvent;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.utils.GeneralUtils;
import de.cebitec.readxplorer.utils.Observer;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.DefaultRowSorter;
import javax.swing.JOptionPane;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Utilities;


/**
 * Panel for navigating in the currently viewed chromosome sequence.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class JumpPanel extends javax.swing.JPanel implements LookupListener {

    private static final long serialVersionUID = 247246234;
    private int jumpPosition;
    private String searchPattern;
    private PersistentReference refGenome;
    private ReferenceConnector refGenCon;
    private BoundsInfoManager boundsManager;
    private ReferenceViewer curRefViewer;
    private final Lookup.Result<ReferenceViewer> res;
    private AbstractViewer viewer;
    private FeatureTableObserver featTableObserver;
    private ChromComboObserver chromObserver;
    private ChromosomeListener chromListener;
    private ReferenceFeatureTopComp refComp;


    /**
     * Creates new Panel for navigating in the currently viewed chromosome
     * sequence.
     */
    public JumpPanel() {
        this.initComponents();
        this.completeComponents();
        this.setMinimumSize( new Dimension( 50, 50 ) );
        this.setPreferredSize( new Dimension( 288, 500 ) );
        this.setSize( new Dimension( 288, 500 ) );
        jumpPosition = 1;
        featTableObserver = new FeatureTableObserver();
        filterTextfield.getDocument().addDocumentListener( new DocumentListener() {

            @Override
            public void insertUpdate( DocumentEvent e ) {
                updateFilter();
            }


            @Override
            public void removeUpdate( DocumentEvent e ) {
                updateFilter();
            }


            @Override
            public void changedUpdate( DocumentEvent e ) {
                updateFilter();

            }


        } );

        //Listener for TableSelect-Events
        this.refComp = ReferenceFeatureTopComp.findInstance();
        featureTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {

            @Override
            public void valueChanged( ListSelectionEvent e ) {
                TableUtils.showFeaturePosition( featureTable, 0, boundsManager );
                refComp.showTableFeature( featureTable, 0 );
            }


        } );

        featureTable.addMouseListener( new MouseAdapter() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                if( (e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger()) ) {
                    final IThumbnailView thumb = Lookup.getDefault().lookup( IThumbnailView.class );
                    if( thumb != null ) {
                        thumb.showTablePopUp( featureTable, curRefViewer, e );
                    }
                }
            }


            @Override
            public void mousePressed( MouseEvent e ) {
                if( (e.getButton() == MouseEvent.BUTTON3) || (e.isPopupTrigger()) ) {
                    final IThumbnailView thumb = Lookup.getDefault().lookup( IThumbnailView.class );
                    if( thumb != null ) {
                        thumb.showTablePopUp( featureTable, curRefViewer, e );
                    }
                }
            }


        } );

        //PropertyChangeListener for RevViewer
        res = Utilities.actionsGlobalContext().lookupResult( ReferenceViewer.class );
        res.addLookupListener( this );

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        featureGroundPanel = new javax.swing.JPanel();
        filterProperties = new javax.swing.JPanel();
        jumpFilterLabel = new javax.swing.JLabel();
        filterTextfield = new JTextFieldPasteable();
        filterForLabel = new javax.swing.JLabel();
        radioProduct = new javax.swing.JRadioButton();
        radioEC = new javax.swing.JRadioButton();
        radioFeatureButton = new javax.swing.JRadioButton();
        radioGene = new javax.swing.JRadioButton();
        tableScrollPane = new javax.swing.JScrollPane();
        featureTable = new javax.swing.JTable();
        searchPatternField = new JTextFieldPasteable();
        jumpTextfield = new JTextFieldPasteable();
        jumpButton = new javax.swing.JButton();
        searchPatternButton = new javax.swing.JButton();
        chromCheckBox = new javax.swing.JCheckBox();
        chromComboBox = new javax.swing.JComboBox<>();

        setBorder(javax.swing.BorderFactory.createTitledBorder("Navigation"));
        setPreferredSize(new java.awt.Dimension(190, 500));

        featureGroundPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        filterProperties.setBorder(javax.swing.BorderFactory.createTitledBorder("FilterProperties"));

        jumpFilterLabel.setText("RegEx Filter:");

        filterTextfield.setMinimumSize(jumpTextfield.getPreferredSize());

        filterForLabel.setText("Column:");

        buttonGroup1.add(radioProduct);
        radioProduct.setText("Product");
        radioProduct.setActionCommand("product");
        radioProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioProductActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioEC);
        radioEC.setText("EC-Num");
        radioEC.setActionCommand("ec");
        radioEC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioECActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioFeatureButton);
        radioFeatureButton.setSelected(true);
        radioFeatureButton.setText("Feature");
        radioFeatureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioFeatureButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(radioGene);
        radioGene.setText("Gene");
        radioGene.setActionCommand("ec");
        radioGene.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioGeneActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout filterPropertiesLayout = new javax.swing.GroupLayout(filterProperties);
        filterProperties.setLayout(filterPropertiesLayout);
        filterPropertiesLayout.setHorizontalGroup(
            filterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPropertiesLayout.createSequentialGroup()
                .addGroup(filterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(filterPropertiesLayout.createSequentialGroup()
                        .addComponent(jumpFilterLabel)
                        .addGap(10, 10, 10)
                        .addComponent(filterTextfield, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(filterPropertiesLayout.createSequentialGroup()
                        .addComponent(filterForLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(radioFeatureButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioGene)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioProduct)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioEC)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        filterPropertiesLayout.setVerticalGroup(
            filterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(filterPropertiesLayout.createSequentialGroup()
                .addGroup(filterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jumpFilterLabel)
                    .addComponent(filterTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(filterPropertiesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(filterForLabel)
                    .addComponent(radioProduct)
                    .addComponent(radioEC)
                    .addComponent(radioFeatureButton)
                    .addComponent(radioGene))
                .addContainerGap())
        );

        tableScrollPane.setViewportView(featureTable);

        searchPatternField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPatternFieldActionPerformed(evt);
            }
        });
        searchPatternField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                searchPatternFieldKeyTyped(evt);
            }
        });

        jumpTextfield.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpTextfieldActionPerformed(evt);
            }
        });
        jumpTextfield.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jumpTextfieldKeyTyped(evt);
            }
        });

        jumpButton.setText("Jump to Pos");
        jumpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jumpButtonActionPerformed(evt);
            }
        });

        searchPatternButton.setText("Search Pattern");
        searchPatternButton.setToolTipText("<html>\n<b>First click</b> with new pattern searches the pattern. <b>Second click</b> with same pattern jumps to next occurrence beyond current interval. Fwd strand is checked first, then rev strand.\n</html>");
        searchPatternButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPatternButtonActionPerformed(evt);
            }
        });

        chromCheckBox.setText("Search all Chromosomes");
        chromCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                chromCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout featureGroundPanelLayout = new javax.swing.GroupLayout(featureGroundPanel);
        featureGroundPanel.setLayout(featureGroundPanelLayout);
        featureGroundPanelLayout.setHorizontalGroup(
            featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(filterProperties, 0, 0, Short.MAX_VALUE)
            .addComponent(tableScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
            .addGroup(featureGroundPanelLayout.createSequentialGroup()
                .addGroup(featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(featureGroundPanelLayout.createSequentialGroup()
                        .addComponent(chromCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(chromComboBox, 0, 1, Short.MAX_VALUE))
                    .addGroup(featureGroundPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jumpButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(searchPatternButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jumpTextfield)
                            .addComponent(searchPatternField))))
                .addContainerGap())
        );
        featureGroundPanelLayout.setVerticalGroup(
            featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(featureGroundPanelLayout.createSequentialGroup()
                .addGroup(featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(chromCheckBox)
                    .addComponent(chromComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jumpButton)
                    .addComponent(jumpTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(featureGroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(searchPatternButton)
                    .addComponent(searchPatternField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(filterProperties, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 303, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(featureGroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(featureGroundPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jumpTextfieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jumpTextfieldKeyTyped
        //jumpList.setSelectedValue(null, false);
        //DefaultListSelectionModel model = (DefaultListSelectionModel) jumpList.getSelectionModel();
        //model.clearSelection();
}//GEN-LAST:event_jumpTextfieldKeyTyped

    private void jumpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpButtonActionPerformed
        String position = jumpTextfield.getText().replaceAll( "\\s", "" );
        jumpTextfield.setText( position );
        if( GeneralUtils.isValidPositionInput( position, refGenome.getActiveChromLength() ) ) {
            jumpPosition = Integer.parseInt( position );
            boundsManager.navigatorBarUpdated( jumpPosition );
        } else {
            JOptionPane.showMessageDialog( this, "Please enter a valid position! (1-" +
                                            refGenome.getActiveChromLength() +
                                            ")", "Invalid Position", JOptionPane.ERROR_MESSAGE );
        }
}//GEN-LAST:event_jumpButtonActionPerformed

    private void jumpTextfieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jumpTextfieldActionPerformed
        this.jumpButtonActionPerformed( evt );
    }//GEN-LAST:event_jumpTextfieldActionPerformed

    private void radioProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioProductActionPerformed
        this.clearFilter();
    }//GEN-LAST:event_radioProductActionPerformed

    private void radioECActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioECActionPerformed
        this.clearFilter();
    }//GEN-LAST:event_radioECActionPerformed

    private void radioFeatureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioFeatureButtonActionPerformed
        this.clearFilter();
    }//GEN-LAST:event_radioFeatureButtonActionPerformed

    private void searchPatternFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPatternFieldActionPerformed
        this.searchPatternButtonActionPerformed( evt );
    }//GEN-LAST:event_searchPatternFieldActionPerformed

    private void searchPatternFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchPatternFieldKeyTyped
        // add your handling code here:
    }//GEN-LAST:event_searchPatternFieldKeyTyped

    private void searchPatternButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPatternButtonActionPerformed

        String pattern = searchPatternField.getText().replaceAll( "\\s", "" );
        searchPatternField.setText( pattern );
//        if (SequenceUtils.isValidDnaString(pattern)) {
        int newPos;

        if( this.searchPattern != null && this.searchPattern.equals( pattern ) ) {
            newPos = this.viewer.getSequenceBar().findNextPatternOccurrence();
        } else {
            this.searchPattern = pattern;
            newPos = this.viewer.getSequenceBar().showPattern( this.searchPattern );
        }

        if( newPos > -1 ) {
            this.boundsManager.navigatorBarUpdated( newPos );
        }

//        } else {
//            JOptionPane.showMessageDialog(this, "Please enter a valid DNA string!", "Invalid Input", JOptionPane.ERROR_MESSAGE);
//        }
    }//GEN-LAST:event_searchPatternButtonActionPerformed

private void radioGeneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioGeneActionPerformed
    this.clearFilter();
}//GEN-LAST:event_radioGeneActionPerformed

    private void chromCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chromCheckBoxActionPerformed
        this.fillFeatureList();
    }//GEN-LAST:event_chromCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JCheckBox chromCheckBox;
    private javax.swing.JComboBox<de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome> chromComboBox;
    private javax.swing.JPanel featureGroundPanel;
    private javax.swing.JTable featureTable;
    private javax.swing.JLabel filterForLabel;
    private javax.swing.JPanel filterProperties;
    private javax.swing.JTextField filterTextfield;
    private javax.swing.JButton jumpButton;
    private javax.swing.JLabel jumpFilterLabel;
    private javax.swing.JTextField jumpTextfield;
    private javax.swing.JRadioButton radioEC;
    private javax.swing.JRadioButton radioFeatureButton;
    private javax.swing.JRadioButton radioGene;
    private javax.swing.JRadioButton radioProduct;
    private javax.swing.JButton searchPatternButton;
    private javax.swing.JTextField searchPatternField;
    private javax.swing.JScrollPane tableScrollPane;
    // End of variables declaration//GEN-END:variables


    /**
     * Set the viewer including the reference genome, for which the navigation
     * is currently available in this panel. Also updates the bounds information
     * manager to the current viewers manager and removes the old table feature
     * observer for the last reference genome.
     * <p>
     * @param viewer The viewer to navigate
     */
    public void setViewer( AbstractViewer viewer ) {
        if( viewer != this.viewer ) {
            boolean firstCall = this.viewer == null || this.chromObserver == null;
            this.updateFeatTableObserver( this.refGenome, viewer, firstCall );
            this.viewer = viewer;
            this.refGenome = viewer.getReference();
            this.boundsManager = viewer.getBoundsInformationManager();
            refGenCon = ProjectConnector.getInstance().getRefGenomeConnector( refGenome.getId() );

            if( refGenome.getNoChromosomes() > 1 ) {

                ChromosomeVisualizationHelper chromHelper = new ChromosomeVisualizationHelper();
                if( firstCall ) {
                    //Update the observer for changes to the chromosome selection anywhere else
                    this.chromObserver = chromHelper.createChromBoxWithObserver( chromComboBox, refGenome );

                    //Update the listener for changes to the chromosome selection in this box
                    chromListener = chromHelper.new ChromosomeListener( chromComboBox, viewer );
                } else {
                    this.chromObserver.setRefGenome( refGenome );
                    this.chromListener.setViewer( viewer );
                    chromHelper.updateChromBoxContent( chromComboBox, refGenome );
                }

                this.chromComboBox.setVisible( true );
                this.chromCheckBox.setVisible( true );
                this.chromComboBox.repaint();
            } else {
                this.chromComboBox.setVisible( false );
                this.chromCheckBox.setVisible( false );
            }
            this.fillFeatureList();
        }
    }


    /**
     * Updates the feature table observer, which updates the feature table
     * depending on the chromosome.
     * <p>
     * @param refGenome The OLD reference genome for which the navigation was
     *                  handled until now.
     * @param viewer    The NEW viewer, for which the navigation will be handled
     *                  from now on
     * @param firstCall true, if the object variables are not set yet, false, if
     *                  the JumpPanel had already displayed content for another
     *                  reference.
     */
    private void updateFeatTableObserver( PersistentReference refGenome, AbstractViewer viewer, boolean firstCall ) {
        if( !firstCall ) {
            refGenome.removeObserver( this.featTableObserver );
        }
        this.featTableObserver = new FeatureTableObserver();
        viewer.getReference().registerObserver( this.featTableObserver );
    }


    /**
     * An Observer for changes in the chromosome selection, which updates the
     * feature list of this JumpPanel.
     */
    private class FeatureTableObserver implements Observer {

        @Override
        public void update( Object args ) {
            fillFeatureList();
        }


    }


    /**
     * Querries all features for the currently selected reference and chromosome
     * and displays the list in the featureTable.
     */
    private void fillFeatureList() {
        List<PersistentFeature> features = new ArrayList<>( 10 );
        if( this.chromCheckBox.isSelected() ) { //TODO: improve performance or add waiting symbol somewhere
            for( PersistentChromosome chrom : refGenome.getChromosomes().values() ) {
                features.addAll( refGenCon.getFeaturesForRegion( 0, chrom.getLength(),
                                                                 FeatureType.ANY, chrom.getId() ) );
            }
        } else {
            features = refGenCon.getFeaturesForRegion( 0, refGenome.getActiveChromLength(),
                                                       FeatureType.ANY, refGenome.getActiveChromId() );
        }
        Collections.sort( features, new FeatureNameSorter() );
        PersistentFeature.Utils.addParentFeatures( features );
        PersistentFeature[] featureData = features.toArray( new PersistentFeature[features.size()] );

        //Create new Model for Table
        featureTable.setModel( new FeatureTableModel( featureData ) );
        featureTable.setRowSorter( new TableRowSorter<>( featureTable.getModel() ) );
        featureTable.getColumnModel().getColumn( 2 ).setPreferredWidth( 150 );
        updateFilter();

    }

    /**
     * Uses regular expression to filter all matching entries in Feature,
     * Product- or EC-Column.
     */
    private void updateFilter() {
        RowFilter<TableModel, Object> rf = null;
        //If current expression doesn't parse, don't update.
        try {
            String search = filterTextfield.getText().replaceAll( "\\s", "" );
            if( radioFeatureButton.isSelected() ) {
                rf = RowFilter.regexFilter( search, 0 );
            } else if( radioGene.isSelected() ) {
                rf = RowFilter.regexFilter( search, 2 );
            } else if( radioProduct.isSelected() ) {
                rf = RowFilter.regexFilter( search, 3 );
            } else if( radioEC.isSelected() ) {
                rf = RowFilter.regexFilter( search, 4 );
            }

        } catch( java.util.regex.PatternSyntaxException e ) {
            return;
        }
        ((DefaultRowSorter<TableModel, Integer>) featureTable.getRowSorter()).setRowFilter( rf );
    }


    @Override
    public void resultChanged( LookupEvent le ) {
        for( ReferenceViewer refViewer : res.allInstances() ) {
            curRefViewer = refViewer;
        }
    }


    void clearFilter() {
        this.filterTextfield.setText( "" );
        this.updateFilter();
    }


    /**
     * Loads the copy, paste, cut, select all right click menus for all text
     * fields beloning to this panel.
     */
    private void completeComponents() {
        this.jumpTextfield.addMouseListener( new StandardMenuEvent() );
        this.searchPatternField.addMouseListener( new StandardMenuEvent() );
        this.filterTextfield.addMouseListener( new StandardMenuEvent() );
    }


    private class FeatureNameSorter implements Comparator<PersistentFeature> {

        @Override
        public int compare( PersistentFeature o1, PersistentFeature o2 ) {
            String name1 = o1.getLocus();
            String name2 = o2.getLocus();

            // null string is always "bigger" than anything else
            if( name1 == null && name2 != null ) {
                return 1;
            } else if( name1 != null && name2 == null ) {
                return -1;
            } else if( name1 == null ? name2 == null : name1.equals( name2 ) ) { //== comparison desired here 4 nullcheck
                // both are null
                return 0;
            } else {
                return name1.compareTo( name2 );
            }
        }


    }

}
