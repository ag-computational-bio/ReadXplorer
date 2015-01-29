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

package de.cebitec.readxplorer.dashboard;


import de.cebitec.readxplorer.databackend.TrackStatisticsGenerator;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentReference;
import de.cebitec.readxplorer.databackend.dataObjects.PersistentTrack;
import de.cebitec.readxplorer.exporter.tables.TableExportFileChooser;
import de.cebitec.readxplorer.ui.TopComponentExtended;
import de.cebitec.readxplorer.ui.controller.ViewController;
import de.cebitec.readxplorer.ui.dialogmenus.explorer.CustomOutlineCellRenderer;
import de.cebitec.readxplorer.ui.dialogmenus.explorer.StandardItem;
import de.cebitec.readxplorer.ui.dialogmenus.explorer.StandardNode;
import de.cebitec.readxplorer.ui.login.LoginWizardAction;
import de.cebitec.readxplorer.ui.visualisation.AppPanelTopComponent;
import de.cebitec.readxplorer.utils.VisualisationUtils;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;


/**
 * DashboardWindowTopComponent, which displays the ReadXplorer Dashboard
 * displaying all references and track data sets in an explorer.
 * <p>
 * @author Evgeny Anisiforov, Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ConvertAsProperties(
         dtd = "-//de.cebitec.readxplorer.dashboard//DashboardWindow//EN",
         autostore = false )
@TopComponent.Description(
         preferredID = "DashboardWindowTopComponent",
         //iconBase="SET/PATH/TO/ICON/HERE",
         persistenceType = TopComponent.PERSISTENCE_ALWAYS )
@TopComponent.Registration( mode = "editor", openAtStartup = true )
@ActionID( category = "Tools", id = "de.cebitec.readxplorer.dashboard.DashboardWindowTopComponent" )
@ActionReference( path = "Menu/Tools", position = 1 )
@TopComponent.OpenActionRegistration(
         displayName = "#CTL_DashboardWindowAction",
         preferredID = "DashboardWindowTopComponent" )
@Messages( {
    "CTL_DashboardWindowAction=Dashboard",
    "CTL_DashboardWindowTopComponent=Dashboard",
    "HINT_DashboardWindowTopComponent=This is a DashboardWindow window",
    "DashboardWindowTopComponent_openDBButton_loggedOut=Open existing database",
    "DashboardWindowTopComponent_openDBButton_loggedIn=Close this and open another database", } )
public final class DashboardWindowTopComponent extends TopComponentExtended
        implements ExplorerManager.Provider {

    private static final long serialVersionUID = 1L;

    private final ExplorerManager em = new ExplorerManager();
    private OutlineView ov;
    private Map<Long, HashSet<Long>> genomesAndTracksToOpen;


    /**
     * DashboardWindowTopComponent, which displays the ReadXplorer Dashboard
     * displaying all references and track data sets in an explorer.
     */
    public DashboardWindowTopComponent() {
        this.genomesAndTracksToOpen = new HashMap<>();
        initComponents();
        this.initAdditionalComponents();
        setName( Bundle.CTL_DashboardWindowTopComponent() );
        setToolTipText( Bundle.HINT_DashboardWindowTopComponent() );
        putClientProperty( TopComponent.PROP_CLOSING_DISABLED, Boolean.FALSE );
        putClientProperty( TopComponent.PROP_DRAGGING_DISABLED, Boolean.FALSE );
        putClientProperty( TopComponent.PROP_UNDOCKING_DISABLED, Boolean.FALSE );

        this.refreshData();
        ProjectConnector.getInstance().addObserver( new Observer() {
            @Override
            public void update( Observable o, Object arg ) {

                //dirty fix for a bug in h2 database, that is occuring, when trying
                //to delete two or more references
                //the code waits 200ms before updating the dashboard to ensure
                //that the reference data has been fully deleted
                new Thread() {
                    @Override
                    public synchronized void run() {
                        try {
                            this.wait( 200 );
                        }
                        catch( InterruptedException ex ) {
                            Exceptions.printStackTrace( ex );
                        }
                        try {
                            //run gui updates separately in the AWT Thread
                            EventQueue.invokeLater( new Runnable() {
                                @Override
                                public void run() {
                                    refreshData();
                                }


                            } );
                        }
                        catch( Exception e ) {
                            Logger.getLogger( this.getClass().getName() ).log( Level.WARNING,
                                                                               e.getMessage() );
                        }
                    }


                }.start();

            }


        } );
    }


    /**
     * Refreshes the data shown in the explorer: The list of references and
     * tracks.
     */
    public void refreshData() {

        if( !ProjectConnector.getInstance().isConnected() ) {
            quickstartLabel.setVisible( true );
            explorerSplitPane.setVisible( false );
            openButton.setVisible( false );
            openDBButton.setText( Bundle.DashboardWindowTopComponent_openDBButton_loggedOut() );
        }
        else {
            quickstartLabel.setVisible( false );
            explorerSplitPane.setVisible( true );
            openButton.setVisible( true );
            openDBButton.setText( Bundle.DashboardWindowTopComponent_openDBButton_loggedIn() );
            try {
                final Map<PersistentReference, List<PersistentTrack>> genomesAndTracks
                        = ProjectConnector.getInstance().getGenomesAndTracks();

                Node rootNode = new AbstractNode( new Children.Keys<PersistentReference>() {
                    @Override
                    protected Node[] createNodes( PersistentReference genome ) {
                        try {
                            List<PersistentTrack> tracks = genomesAndTracks.get( genome );

                            if( tracks != null ) {
                                List<DBItem> trackItems = new ArrayList<>( tracks.size() );
                                for( PersistentTrack track : tracks ) {
                                    trackItems.add( new DBItem( track ) );
                                }
                                return new Node[]{ new DBItemNode( new DBItem( genome ), new DBItemChildren( trackItems ) ) };
                            }
                            else {
                                return new Node[]{ new DBItemNode( new DBItem( genome ) ) };
                            }
                        }
                        catch( IntrospectionException ex ) {
                            Exceptions.printStackTrace( ex );
                            return new Node[]{};
                        }
                    }


                    @Override
                    protected void addNotify() {
                        super.addNotify();
                        this.setKeys( genomesAndTracks.keySet() );
                        //this.setKeys(dbGens);
                    }


                } );
                em.setRootContext( rootNode ); //Put the Nodes into the Lookup of the TopComponent,

                //expand all nodes
                for( Node n : em.getRootContext().getChildren().getNodes() ) {
                    ov.expandNode( n );
                }
                //or like this for one special node:
                //ov.expandNode(em.getRootContext().getChildren().getNodeAt(0));

            }
            catch( OutOfMemoryError e ) {
                VisualisationUtils.displayOutOfMemoryError( this );
            }
        }
        this.repaint();
    }


    /**
     * Updates the list of genomes and tracks to open depending on the checked
     * items in the list if nodes.
     */
    private void updateOpenList() {
        genomesAndTracksToOpen = new HashMap<>();
        List<Node> selectedNodes = DBItemNode.getAllMarkedNodes( Arrays.asList( em.getRootContext().getChildren().getNodes() ) );

        //scan all selected nodes and save them to a map of the form:
        // GenomeID -> List<ReferenceID>
//        selectedNodes.addAll(Arrays.asList(em.getSelectedNodes()));

        for( Node n : selectedNodes ) {
            DBItem item = this.getItemForNode( n );
            if( item != null ) {
                if( item.getChild() == DBItem.Child.GENOME ) {
                    if( !genomesAndTracksToOpen.containsKey( item.getID() ) ) {
                        genomesAndTracksToOpen.put( item.getID(), new HashSet<Long>() );
                    }
                }
                else {
                    if( !genomesAndTracksToOpen.containsKey( item.getRefID() ) ) {
                        genomesAndTracksToOpen.put( item.getRefID(), new HashSet<Long>() );
                        DBItem parentItem = this.getItemForNode( n.getParentNode() );
                        if( parentItem != null ) {
                            parentItem.setSelected( true );
                        }
                    }
                    genomesAndTracksToOpen.get( item.getRefID() ).add( item.getID() );
                }
            }
        }
    }


    /**
     * @param n the node to check
     * <p>
     * @return The DBItem for the given node, if it contains a DBItem, if not,
     *         null is returned
     */
    private DBItem getItemForNode( Node n ) {
        DBItem item = null;
        StandardItem standardItem = StandardNode.getItemForNode( n );
        if( standardItem != null && standardItem instanceof DBItem ) {
            item = (DBItem) standardItem;
        }
        return item;
    }


    @Override
    public ExplorerManager getExplorerManager() {
        return em;
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        quickstartLabel = new javax.swing.JLabel();
        explorerSplitPane = new javax.swing.JSplitPane();
        explorerPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        openButton = new javax.swing.JButton();
        selectButtonPanel = new javax.swing.JPanel();
        selectAllButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();
        storeButtonPanel = new javax.swing.JPanel();
        storeStatsButton = new javax.swing.JButton();
        storeStatsLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        openDBButton = new javax.swing.JButton();
        createDBButton = new javax.swing.JButton();

        org.openide.awt.Mnemonics.setLocalizedText(quickstartLabel, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.quickstartLabel.text")); // NOI18N

        explorerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        explorerSplitPane.setResizeWeight(1.0);
        explorerSplitPane.setToolTipText(org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.explorerSplitPane.toolTipText")); // NOI18N

        explorerPanel.setPreferredSize(new java.awt.Dimension(500, 400));
        explorerPanel.setLayout(new java.awt.BorderLayout());
        explorerSplitPane.setLeftComponent(explorerPanel);

        buttonPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        buttonPanel.setPreferredSize(new java.awt.Dimension(1053, 51));

        org.openide.awt.Mnemonics.setLocalizedText(openButton, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.openButton.text")); // NOI18N
        openButton.setMargin(new java.awt.Insets(15, 50, 15, 50));
        openButton.setMaximumSize(new java.awt.Dimension(400, 29));
        openButton.setMinimumSize(new java.awt.Dimension(400, 29));
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        selectButtonPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(selectAllButton, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.selectAllButton.text")); // NOI18N
        selectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAllButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(deselectAllButton, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.deselectAllButton.text")); // NOI18N
        deselectAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deselectAllButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout selectButtonPanelLayout = new javax.swing.GroupLayout(selectButtonPanel);
        selectButtonPanel.setLayout(selectButtonPanelLayout);
        selectButtonPanelLayout.setHorizontalGroup(
            selectButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectButtonPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(selectButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(deselectAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(selectAllButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        selectButtonPanelLayout.setVerticalGroup(
            selectButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(selectButtonPanelLayout.createSequentialGroup()
                .addComponent(selectAllButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(deselectAllButton))
        );

        storeButtonPanel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        org.openide.awt.Mnemonics.setLocalizedText(storeStatsButton, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.storeStatsButton.text")); // NOI18N
        storeStatsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                storeStatsButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(storeStatsLabel, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.storeStatsLabel.text")); // NOI18N

        javax.swing.GroupLayout storeButtonPanelLayout = new javax.swing.GroupLayout(storeButtonPanel);
        storeButtonPanel.setLayout(storeButtonPanelLayout);
        storeButtonPanelLayout.setHorizontalGroup(
            storeButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storeButtonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(storeButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(storeStatsButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(storeButtonPanelLayout.createSequentialGroup()
                        .addComponent(storeStatsLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        storeButtonPanelLayout.setVerticalGroup(
            storeButtonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(storeButtonPanelLayout.createSequentialGroup()
                .addGap(9, 9, 9)
                .addComponent(storeStatsLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(storeStatsButton))
        );

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(storeButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 215, Short.MAX_VALUE)
                .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 217, Short.MAX_VALUE)
                .addComponent(selectButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(storeButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        explorerSplitPane.setRightComponent(buttonPanel);

        org.openide.awt.Mnemonics.setLocalizedText(openDBButton, Bundle.DashboardWindowTopComponent_openDBButton_loggedOut());
        openDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(createDBButton, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.createDBButton.text")); // NOI18N
        createDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createDBButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(openDBButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(createDBButton)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(createDBButton, javax.swing.GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                    .addComponent(openDBButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(explorerSplitPane)
            .addComponent(quickstartLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(1, 1, 1)
                .addComponent(quickstartLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(explorerSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 87, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents


    /**
     * Initializes all additional components.
     */
    private void initAdditionalComponents() {

        String sText = "<html><img src=\"" + DashboardWindowTopComponent.class.getResource( "splash.png" ) + "\" /><h2>ReadXplorer - "
                       + "Visualization and Analysis of Mapped Sequences: Quick Start</h2> <p>1. Open/Create a database (\"File -> Open/Create Database\") <br/> "
                       + "2. Import a reference genome (\"File -> Import data\") <br /> 3. Import a track (\"File -> Import data\")<br /> 4. Explore "
                       + "your reference genome and tracks (via Dashboard, toolbar buttons or \"Visualisation\" menu) <br />5. Run an analysis on your data (via "
                       + "toolbar buttons or \"Tools\" menu)</p></html>";
        quickstartLabel.setText( sText );

        Border paddingBorder = BorderFactory.createEmptyBorder( 50, 100, 100, 100 );
        quickstartLabel.setBorder( BorderFactory.createCompoundBorder( paddingBorder, paddingBorder ) );

        //Create the outline view showing the explorer
        ov = new OutlineView(); //Set the columns of the outline view
        //do not show the default property window
        //this outlineview is meant to be a read-only list
        ov.setDefaultActionAllowed( false );
        //using the name of the property
        //followed by the text to be displayed in the column header:
        ov.setPropertyColumns( "description", "Description", "timestamp", "Import Date", "selected", "Mark for action" );
        //Hide the root node, since we only care about the children:
        ov.getOutline().setRootVisible( false ); //Add the OutlineView to the TopComponent:
        ov.getOutline().setDefaultRenderer( Node.Property.class, new CustomOutlineCellRenderer() );
        ov.getOutline().addMouseListener( new MouseAdapter() {

            @Override
            public void mouseClicked( MouseEvent e ) {
                this.checkSelectedRowBoxes();
                updateOpenList();
            }


            /**
             * Checks the boxes of all currently selected nodes in the explorer.
             */
            private void checkSelectedRowBoxes() {
                Node[] selectedNodes = em.getSelectedNodes();
                for( int i = 0; i < selectedNodes.length; ++i ) {
                    DBItem dbItem = getItemForNode( selectedNodes[i] );
                    if( dbItem != null ) {
                        if( dbItem.getChild() == DBItem.Child.GENOME ) {
                            if( !genomesAndTracksToOpen.containsKey( dbItem.getID() ) ) {
                                dbItem.setSelected( true );
                            }
                        }
                        else {
                            if( !genomesAndTracksToOpen.containsKey( dbItem.getRefID() ) ) {
                                dbItem.setSelected( true );
                            }
                            else if( !genomesAndTracksToOpen.get( dbItem.getRefID() ).contains( dbItem.getID() ) ) {
                                dbItem.setSelected( true );
                            }
                        }
                    }
                }
                ov.repaint();
            }


        } );
        explorerPanel.add( ov );
    }

    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed

        //scan all selected nodes and save them to a map of the form:
        // GenomeID -> List<ReferenceID>
//        selectedNodes.addAll(Arrays.asList(em.getSelectedNodes()));

        this.updateOpenList();

        Map<PersistentReference, List<PersistentTrack>> genomesAndTracks = ProjectConnector.getInstance().getGenomesAndTracks();

        for( Long genomeId : genomesAndTracksToOpen.keySet() ) {
            Set<Long> trackIds = genomesAndTracksToOpen.get( genomeId );

            ReferenceConnector rc = ProjectConnector.getInstance().getRefGenomeConnector( genomeId.intValue() );
            PersistentReference genome = rc.getRefGenome();

            //open reference genome now
            AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.open();
            appPanelTopComponent.getLookup().lookup( ViewController.class ).openGenome( genome );
            appPanelTopComponent.setName( appPanelTopComponent.getLookup().lookup( ViewController.class ).getDisplayName() );
            appPanelTopComponent.requestActive();


            //open tracks for this genome now
            List<PersistentTrack> allTracksForThisGenome = genomesAndTracks.get( genome );
            List<PersistentTrack> tracksToShow = new ArrayList<>( allTracksForThisGenome.size() );
            for( PersistentTrack track : allTracksForThisGenome ) {
                if( trackIds.contains( (long) track.getId() ) ) {
                    tracksToShow.add( track );
                }
            }

            appPanelTopComponent.getLookup().lookup( ViewController.class ).openTracksOnCurrentGenome( tracksToShow );

        }
        //ReferenceViewer referenceViewer = AppPanelTopComponent.findInstance().getReferenceViewer();

    }//GEN-LAST:event_openButtonActionPerformed


    @Messages( { "ChooseDB=Choose database" } )
    private void openDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBButtonActionPerformed
        this.openLoginAction( Bundle.ChooseDB(), evt );
    }//GEN-LAST:event_openDBButtonActionPerformed


    @Messages( { "CreateDB=Choose new DB location" } )
    private void createDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createDBButtonActionPerformed
        this.openLoginAction( Bundle.CreateDB(), evt );
    }//GEN-LAST:event_createDBButtonActionPerformed

    private void selectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectAllButtonActionPerformed
        StandardItem.setSelectionOfAllItems( ov, em.getRootContext().getChildren().getNodes(), true );
    }//GEN-LAST:event_selectAllButtonActionPerformed

    private void deselectAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deselectAllButtonActionPerformed
        StandardItem.setSelectionOfAllItems( ov, em.getRootContext().getChildren().getNodes(), false );
    }//GEN-LAST:event_deselectAllButtonActionPerformed

    private void storeStatsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeStatsButtonActionPerformed
        storeTrackStatistics();
    }//GEN-LAST:event_storeStatsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton createDBButton;
    private javax.swing.JButton deselectAllButton;
    private javax.swing.JPanel explorerPanel;
    private javax.swing.JSplitPane explorerSplitPane;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JButton openButton;
    private javax.swing.JButton openDBButton;
    private javax.swing.JLabel quickstartLabel;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JPanel selectButtonPanel;
    private javax.swing.JPanel storeButtonPanel;
    private javax.swing.JButton storeStatsButton;
    private javax.swing.JLabel storeStatsLabel;
    // End of variables declaration//GEN-END:variables


    @Override
    public void componentOpened() {
        this.refreshData();
    }


    @Override
    public void componentClosed() {
        // add custom code on component closing
    }


    void writeProperties( java.util.Properties p ) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty( "version", "1.0" );
        // store your settings
    }


    void readProperties( java.util.Properties p ) {
        String version = p.getProperty( "version" );
        // read your settings according to their version
    }


    /**
     * Opens the login action for a ReadXplorer DB with the appropriate
     * chooseButtonText.
     * <p>
     * @param chooseButtonText the text to display on the open DB button
     * @param evt              the event which is forwarded to the login action
     */
    private void openLoginAction( String chooseButtonText, java.awt.event.ActionEvent evt ) {
        LoginWizardAction loginAction = new LoginWizardAction();
        loginAction.setChooseButtonText( chooseButtonText );
        loginAction.actionPerformed( evt );
    }

    /**
     * Opens a TableFileChooser and fetches the track statistics for all tracks 
     * stored in the DB to store their statistics in a table file.
     */
    private void storeTrackStatistics() {
        TableExportFileChooser fileChooser = new TableExportFileChooser( 
                TableExportFileChooser.getTableFileExtensions(), new TrackStatisticsGenerator() );
    }

}
