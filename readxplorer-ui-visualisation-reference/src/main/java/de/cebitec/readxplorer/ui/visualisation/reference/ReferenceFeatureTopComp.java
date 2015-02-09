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


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.JFeature;
import de.cebitec.readxplorer.ui.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.ui.tablevisualization.TableUtils;
import de.cebitec.readxplorer.utils.classification.FeatureType;
import de.cebitec.readxplorer.utils.polytree.Node;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;
import javax.swing.JTable;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Top component which displays the details of the currently selected
 * reference feature.
 */
@ConvertAsProperties( dtd = "-//de.cebitec.readxplorer.ui.visualisation.reference//ReferenceFeature//EN", autostore = false )
public final class ReferenceFeatureTopComp extends TopComponentExtended
        implements LookupListener {

    private final static Logger LOG = Logger.getLogger( ReferenceFeatureTopComp.class.getName() );

    private static ReferenceFeatureTopComp instance;
    private static final long serialVersionUID = 1L;
    private Result<ReferenceViewer> result;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ReferenceFeatureTopComp";


    /**
     * Top component which displays the details of the currently selected
     * reference feature.
     */
    public ReferenceFeatureTopComp() {
        this.initComponents();
        this.setName( NbBundle.getMessage( ReferenceFeatureTopComp.class, "CTL_ReferenceFeatureTopComp" ) );
        this.setToolTipText( NbBundle.getMessage( ReferenceFeatureTopComp.class, "HINT_ReferenceFeatureTopComp" ) );
//        this.setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        this.putClientProperty( TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE );
        this.putClientProperty( TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE );
        this.putClientProperty( TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE );

    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        typeLabel = new javax.swing.JLabel();
        strandLabel = new javax.swing.JLabel();
        typeText = new javax.swing.JTextField();
        productLabel = new javax.swing.JLabel();
        strandText = new javax.swing.JTextField();
        ecNumLabel = new javax.swing.JLabel();
        stopLabel = new javax.swing.JLabel();
        ecNumField = new javax.swing.JTextField();
        stopField = new javax.swing.JTextField();
        locusField = new javax.swing.JTextField();
        locusLabel = new javax.swing.JLabel();
        startField = new javax.swing.JTextField();
        startLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        productText = new javax.swing.JTextArea();
        geneField = new javax.swing.JTextField();
        geneLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        parentList = new javax.swing.JList<>();
        parentLabel = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        subfeatureList = new javax.swing.JList<>();
        subfeatureLabel = new javax.swing.JLabel();

        typeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(typeLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.typeLabel.text")); // NOI18N
        typeLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.typeLabel.toolTipText")); // NOI18N

        strandLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(strandLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.strandLabel.text")); // NOI18N
        strandLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.strandLabel.toolTipText")); // NOI18N

        typeText.setEditable(false);
        typeText.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.typeText.toolTipText")); // NOI18N
        typeText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeTextActionPerformed(evt);
            }
        });

        productLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(productLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.productLabel.text")); // NOI18N
        productLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.productLabel.toolTipText")); // NOI18N

        strandText.setEditable(false);
        strandText.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.strandText.toolTipText")); // NOI18N

        ecNumLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(ecNumLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.ecNumLabel.text")); // NOI18N
        ecNumLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.ecNumLabel.toolTipText")); // NOI18N

        stopLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(stopLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.stopLabel.text")); // NOI18N
        stopLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.stopLabel.toolTipText")); // NOI18N

        ecNumField.setEditable(false);
        ecNumField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.ecNumField.toolTipText")); // NOI18N

        stopField.setEditable(false);
        stopField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.stopField.toolTipText")); // NOI18N

        locusField.setEditable(false);
        locusField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.locusField.toolTipText")); // NOI18N

        locusLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(locusLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.locusLabel.text")); // NOI18N
        locusLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.locusLabel.toolTipText")); // NOI18N

        startField.setEditable(false);
        startField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.startField.toolTipText")); // NOI18N

        startLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(startLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.startLabel.text")); // NOI18N
        startLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.startLabel.toolTipText")); // NOI18N

        productText.setEditable(false);
        productText.setColumns(20);
        productText.setLineWrap(true);
        productText.setRows(5);
        productText.setWrapStyleWord(true);
        productText.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        jScrollPane1.setViewportView(productText);

        geneField.setEditable(false);
        geneField.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.geneField.toolTipText")); // NOI18N

        geneLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(geneLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.geneLabel.text")); // NOI18N
        geneLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.geneLabel.toolTipText")); // NOI18N

        jScrollPane2.setViewportView(parentList);

        parentLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(parentLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.parentLabel.text")); // NOI18N
        parentLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.parentLabel.toolTipText")); // NOI18N

        jScrollPane3.setViewportView(subfeatureList);

        subfeatureLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        org.openide.awt.Mnemonics.setLocalizedText(subfeatureLabel, org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.subfeatureLabel.text")); // NOI18N
        subfeatureLabel.setToolTipText(org.openide.util.NbBundle.getMessage(ReferenceFeatureTopComp.class, "ReferenceFeatureTopComp.subfeatureLabel.toolTipText")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(strandLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(productLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(geneLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(stopLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(ecNumLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(locusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(startLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(parentLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(subfeatureLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(4, 4, 4)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(locusField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(geneField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(startField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(stopField, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ecNumField)
                    .addComponent(jScrollPane1)
                    .addComponent(strandText, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(typeText, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(typeText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(typeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(locusField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(locusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(geneField)
                    .addComponent(geneLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(startLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(startField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(stopField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(stopLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ecNumField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(ecNumLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(productLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(strandText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(strandLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(parentLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(subfeatureLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void typeTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeTextActionPerformed

    }//GEN-LAST:event_typeTextActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField ecNumField;
    private javax.swing.JLabel ecNumLabel;
    private javax.swing.JTextField geneField;
    private javax.swing.JLabel geneLabel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField locusField;
    private javax.swing.JLabel locusLabel;
    private javax.swing.JLabel parentLabel;
    private javax.swing.JList<de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature> parentList;
    private javax.swing.JLabel productLabel;
    private javax.swing.JTextArea productText;
    private javax.swing.JTextField startField;
    private javax.swing.JLabel startLabel;
    private javax.swing.JTextField stopField;
    private javax.swing.JLabel stopLabel;
    private javax.swing.JLabel strandLabel;
    private javax.swing.JTextField strandText;
    private javax.swing.JLabel subfeatureLabel;
    private javax.swing.JList<de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature> subfeatureList;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JTextField typeText;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized
     * instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     * <p>
     * @return the ReferenceFeatureTopComp
     */
    public static synchronized ReferenceFeatureTopComp getDefault() {
        if( instance == null ) {
            instance = new ReferenceFeatureTopComp();
        }
        return instance;
    }


    /**
     * Obtain the ReferenceFeatureTopComp instance. Never call
     * {@link #getDefault} directly!
     * <p>
     * @return the ReferenceFeatureTopComp
     */
    public static synchronized ReferenceFeatureTopComp findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent( PREFERRED_ID );
        if( win == null ) {
            LOG.warning( "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system." );
            return getDefault();
        }
        if( win instanceof ReferenceFeatureTopComp ) {
            return (ReferenceFeatureTopComp) win;
        }
        LOG.warning( "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior." );
        return getDefault();
    }


    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }


    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult( ReferenceViewer.class );
        result.addLookupListener( this );
        resultChanged( new LookupEvent( result ) );
    }


    @Override
    public void resultChanged( LookupEvent ev ) {
        for( ReferenceViewer refViewer : result.allInstances() ) {
            JFeature feature = refViewer.getCurrentlySelectedFeature();
            this.showFeatureDetails( feature != null ? feature.getPersistentFeature() : null );

            refViewer.addPropertyChangeListener( ReferenceViewer.PROP_FEATURE_SELECTED, new PropertyChangeListener() {

                @Override
                public void propertyChange( PropertyChangeEvent evt ) {
                    JFeature feature = (JFeature) evt.getNewValue();
                    showFeatureDetails( feature.getPersistentFeature() );
                }


            } );
        }
    }


    @Override
    public void componentClosed() {
        result.removeLookupListener( this );
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // store your settings
    }


    Object readProperties( java.util.Properties p ) {
        if( instance == null ) {
            instance = this;
        }
        instance.readPropertiesImpl( p );
        return instance;
    }


    private void readPropertiesImpl( java.util.Properties p ) {
        String version = p.getProperty( "version" );
        // read your settings according to their version
    }


    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }


    /**
     * Displays the feature details in their belonging visual components.
     * <p>
     * @param feat the feature whose details should be shown
     */
    public void showFeatureDetails( PersistentFeature feat ) {

        String strand = "";
        if( feat != null ) {
            Vector<PersistentFeature> parentVect = this.getFeatureVector( feat.getParents(), "Root feature" );
            Vector<PersistentFeature> childrenVect = this.getFeatureVector( feat.getNodeChildren(), "No children" );

            this.ecNumField.setText( feat.getEcNumber() );
            this.startField.setText( String.valueOf( feat.getStart() ) );
            this.stopField.setText( String.valueOf( feat.getStop() ) );
            this.productText.setText( feat.getProduct() );
            this.productText.setToolTipText( feat.getProduct() );
            this.locusField.setText( feat.getLocus() );
            this.geneField.setText( feat.getName() );
            this.typeText.setText( feat.getType().getTypeString() );
            this.parentList.setListData( parentVect );
            this.subfeatureList.setListData( childrenVect );

            strand = feat.isFwdStrand() ? "forward" : "reverse";
        }
        else {
            this.ecNumField.setText( "" );
            this.startField.setText( "" );
            this.stopField.setText( "" );
            this.productText.setText( "" );
            this.productText.setToolTipText( "" );
            this.locusField.setText( "" );
            this.geneField.setText( "" );
            this.typeText.setText( "" );
        }
        this.strandText.setText( strand );
        this.productText.setCaretPosition( 0 );
    }


    /**
     * Transforms the given node list into a vector of persistent features.
     * The vector only contains elements, if the nodes are instances of
     * <tt>PersistentFeature</tt>.
     * <p>
     * @param featureList The list of features to convert
     * @param replacement The replacement string in case the list is empty
     * <p>
     * @return The vector of features
     */
    private Vector<PersistentFeature> getFeatureVector( List<Node> featureList, String replacement ) {
        Vector<PersistentFeature> featureVect = new Vector<>();
        for( Node parentNode : featureList ) {
            if( parentNode instanceof PersistentFeature ) {
                PersistentFeature feature = (PersistentFeature) parentNode;
                featureVect.add( feature );
            }
        }
        if( featureVect.isEmpty() ) {
            featureVect.add( new PersistentFeature( 0, 0, "", "", replacement, "", 0, 0, true, FeatureType.UNDEFINED, "" ) );
        }
        return featureVect;
    }


    /**
     * Displays the feature associated with the currently selected table row in
     * this feature window.
     * <p>
     * @param table              the table whose selected feature shall be shown
     * @param featureColumnIndex the index of the feature column in the table
     */
    public void showTableFeature( JTable table, int featureColumnIndex ) {
        int selectedModelRow = TableUtils.getSelectedModelRow( table );
        if( selectedModelRow > -1 ) {
            Object value = table.getModel().getValueAt( selectedModelRow, featureColumnIndex );

            if( value instanceof PersistentFeature ) {
                this.showFeatureDetails( (PersistentFeature) value );
            }
        }
    }


}
