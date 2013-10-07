package de.cebitec.vamp.dashboard;
  
import de.cebitec.vamp.controller.ViewController;
import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.ReferenceConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference; 
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.ui.visualisation.AppPanelTopComponent;
import de.cebitec.vamp.util.VisualisationUtils;
import de.cebitec.vamp.view.TopComponentExtended;
import java.awt.EventQueue;
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
 * 
 * @author Evgeny Anisiforov, Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ConvertAsProperties(
    dtd = "-//de.cebitec.vamp.dashboard//DashboardWindow//EN",
autostore = false)
@TopComponent.Description(
    preferredID = "DashboardWindowTopComponent",
//iconBase="SET/PATH/TO/ICON/HERE", 
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "editor", openAtStartup = true)
@ActionID(category = "Tools", id = "de.cebitec.vamp.dashboard.DashboardWindowTopComponent")
@ActionReference(path = "Menu/Tools", position = 1)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_DashboardWindowAction",
preferredID = "DashboardWindowTopComponent")
@Messages({
    "CTL_DashboardWindowAction=Dashboard",
    "CTL_DashboardWindowTopComponent=Dashboard",
    "HINT_DashboardWindowTopComponent=This is a DashboardWindow window"
})
public final class DashboardWindowTopComponent extends TopComponentExtended implements ExplorerManager.Provider {
    
    private static final long serialVersionUID = 1L;
    
    private ExplorerManager em = new ExplorerManager();
    private OutlineView ov;
    
    /**
     * DashboardWindowTopComponent, which displays the ReadXplorer Dashboard
     * displaying all references and track data sets in an explorer.
     */
    public DashboardWindowTopComponent() {
        initComponents();
        this.initAdditionalComponents();
        setName(Bundle.CTL_DashboardWindowTopComponent());
        setToolTipText(Bundle.HINT_DashboardWindowTopComponent());
        putClientProperty(TopComponent.PROP_CLOSING_DISABLED, Boolean.FALSE);
        putClientProperty(TopComponent.PROP_DRAGGING_DISABLED, Boolean.FALSE);
        putClientProperty(TopComponent.PROP_UNDOCKING_DISABLED, Boolean.FALSE);

        this.refreshData();
        ProjectConnector.getInstance().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {

                //dirty fix for a bug in h2 database, that is occuring, when trying
                //to delete two or more references
                //the code waits 200ms before updating the dashboard to ensure 
                //that the reference data has been fully deleted
                new Thread() {
                    @Override
                    public synchronized void run() {
                        try {
                            this.wait(200);
                        } catch (InterruptedException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        try {
                            //run gui updates separately in the AWT Thread
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    refreshData();
                                }
                            });
                        } catch (Exception e) {
                            Logger.getLogger(this.getClass().getName()).log(Level.WARNING,
                                    e.getMessage());
                        }
                    }
                }.start();

            }
        });
    }
    
    /**
     * Refreshes the data shown in the explorer: The list of references and tracks.
     */
    public void refreshData() {
        
        if (!ProjectConnector.getInstance().isConnected()) {
            quickstartLabel.setVisible(true);
            explorerSplitPane.setVisible(false);
            openButton.setVisible(false);
        } else {
            quickstartLabel.setVisible(false);
            explorerSplitPane.setVisible(true);
            openButton.setVisible(true);
            try {
                final Map<PersistantReference, List<PersistantTrack>> genomesAndTracks =
                        ProjectConnector.getInstance().getGenomesAndTracks();

                Node rootNode = new AbstractNode(new Children.Keys() {
                    @Override
                    protected Node[] createNodes(Object t) {
                        PersistantReference genome = (PersistantReference) t;
                        try {
                            List<PersistantTrack> tracks = genomesAndTracks.get(genome);

                            if (tracks != null) {
                                List<Item> trackItems = new ArrayList<>();
                                for (PersistantTrack track : tracks) {
                                    trackItems.add(new Item(track));
                                }
                                return new Node[]{new ItemNode(new Item(genome), new ItemChildren(trackItems))};
                            } else {
                                return new Node[]{new ItemNode(new Item(genome))};
                            }
                        } catch (IntrospectionException ex) {
                            Exceptions.printStackTrace(ex);
                            return new Node[]{};
                        }
                    }

                    @Override
                    protected void addNotify() {
                        super.addNotify();
                        this.setKeys(genomesAndTracks.keySet());
                        //this.setKeys(dbGens);
                    }
                });
                em.setRootContext(rootNode); //Put the Nodes into the Lookup of the TopComponent, 

                //expand all nodes
                for (Node n : em.getRootContext().getChildren().getNodes()) {
                    ov.expandNode(n);
                }
                //or like this for one special node: 
                //ov.expandNode(em.getRootContext().getChildren().getNodeAt(0));

            } catch (OutOfMemoryError e) {
                VisualisationUtils.displayOutOfMemoryError(this);
            }
        }
        this.repaint();
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

        org.openide.awt.Mnemonics.setLocalizedText(quickstartLabel, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.quickstartLabel.text")); // NOI18N

        explorerSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        explorerPanel.setLayout(new java.awt.BorderLayout());
        explorerSplitPane.setLeftComponent(explorerPanel);

        org.openide.awt.Mnemonics.setLocalizedText(openButton, org.openide.util.NbBundle.getMessage(DashboardWindowTopComponent.class, "DashboardWindowTopComponent.openButton.text")); // NOI18N
        openButton.setMargin(new java.awt.Insets(15, 50, 15, 50));
        openButton.setMaximumSize(new java.awt.Dimension(400, 29));
        openButton.setMinimumSize(new java.awt.Dimension(400, 29));
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addContainerGap(102, Short.MAX_VALUE)
                .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(103, Short.MAX_VALUE))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(openButton, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 130, Short.MAX_VALUE))
        );

        explorerSplitPane.setRightComponent(buttonPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(quickstartLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(explorerSplitPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(quickstartLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(explorerSplitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Initializes all additional components.
     */
    private void initAdditionalComponents() {

        String sText = "<html><img src=\"" + DashboardWindowTopComponent.class.getResource("splash.png") + "\" /><h2>ReadXplorer - "
                + "Visualization and Analysis of Mapped Sequences: Quick Start</h2> <p>1. Open/Create a database (\"File -> Open\") <br/> "
                + "2. Import a reference genome (\"File -> Import data\") <br /> 3. Import a track (\"File -> Import data\")<br /> 4. Explore "
                + "your reference genome and tracks (via Dashboard, toolbar buttons or \"Visualisation\" menu) <br />5. Run an analysis on your data (via "
                + "toolbar buttons or \"Tools\" menu)</p></html>";
        quickstartLabel.setText(sText);

        Border paddingBorder = BorderFactory.createEmptyBorder(100, 100, 100, 100);
        quickstartLabel.setBorder(BorderFactory.createCompoundBorder(paddingBorder, paddingBorder));

        //Create the outline view showing the explorer
        ov = new OutlineView(); //Set the columns of the outline view
        //do not show the default property window
        //this outlineview is meant to be a read-only list
        ov.setDefaultActionAllowed(false);
        //using the name of the property 
        //followed by the text to be displayed in the column header: 
        ov.setPropertyColumns("description", "Description", "timestamp", "Import Date", "mark", "Mark for action");
        //Hide the root node, since we only care about the children: 
        ov.getOutline().setRootVisible(false); //Add the OutlineView to the TopComponent: 
        ov.getOutline().setDefaultRenderer(Node.Property.class, new CustomOutlineCellRenderer());
        explorerPanel.add(ov);
    }
    
    /** 
     * Iterates through all given nodes and their children and returns only 
     * those with getMark() = true.
     */
    private static List<Node> getAllMarkedNodes(List<Node> nodes) {
        ArrayList<Node> selectedNodes = new ArrayList<>(); 
        for(Node n : nodes) {
            ItemNode node = (ItemNode) n;
            Item item = node.getData();
            if (item.getMark()) {
                //Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Node "+item.getTitle()+" is selected!");
                selectedNodes.add(n);
            }
            List<Node> markedChildren = Arrays.asList(n.getChildren().getNodes());
            selectedNodes.addAll(getAllMarkedNodes(markedChildren));
        }
        return selectedNodes;
    }
    
    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        List<Node> selectedNodes = getAllMarkedNodes(Arrays.asList(em.getRootContext().getChildren().getNodes()));
            
        //scan all selected nodes and save them to a map of the form:
        // GenomeID -> List<ReferenceID>
        selectedNodes.addAll(Arrays.asList(em.getSelectedNodes()));
        
        HashMap<Long,HashSet<Long>> genomesAndTracksToOpen = new HashMap<>();
        for(Node n : selectedNodes) {
            ItemNode node = (ItemNode) n;
            Item item = node.getData();
            if (item.getKind()==Item.Kind.GENOME) {
                if (!genomesAndTracksToOpen.containsKey(item.getID())) {
                    genomesAndTracksToOpen.put(item.getID(), new HashSet<Long>()); 
                }
            } else {
                if (!genomesAndTracksToOpen.containsKey(item.getRefID())) {
                    genomesAndTracksToOpen.put(item.getRefID(), new HashSet<Long>());
                }
                genomesAndTracksToOpen.get(item.getRefID()).add(item.getID());
            }
        }
        
        
        Map<PersistantReference, List<PersistantTrack>> genomesAndTracks = ProjectConnector.getInstance().getGenomesAndTracks();
        
        for (Long genomeId : genomesAndTracksToOpen.keySet()) {
            Set<Long> trackIds = genomesAndTracksToOpen.get(genomeId);
            
            ReferenceConnector rc = ProjectConnector.getInstance().getRefGenomeConnector(genomeId.intValue());
            PersistantReference genome = rc.getRefGenome();
            
            //open reference genome now
            AppPanelTopComponent appPanelTopComponent = new AppPanelTopComponent();
            appPanelTopComponent.open();
            appPanelTopComponent.getLookup().lookup(ViewController.class).openGenome(genome);
            appPanelTopComponent.setName(appPanelTopComponent.getLookup().lookup(ViewController.class).getDisplayName());
            appPanelTopComponent.requestActive();
             
            
            //open tracks for this genome now
            List<PersistantTrack> allTracksForThisGenome = genomesAndTracks.get(genome);
            List<PersistantTrack> tracksToShow = new ArrayList<>();
            for (PersistantTrack track : allTracksForThisGenome) {
                if (trackIds.contains(new Long(track.getId()))) {
                    tracksToShow.add(track);
                }
            }
                
            appPanelTopComponent.getLookup().lookup(ViewController.class).openTracksOnCurrentGenome(tracksToShow);
            
        }
        //ReferenceViewer referenceViewer = AppPanelTopComponent.findInstance().getReferenceViewer();
        
    }//GEN-LAST:event_openButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel explorerPanel;
    private javax.swing.JSplitPane explorerSplitPane;
    private javax.swing.JButton openButton;
    private javax.swing.JLabel quickstartLabel;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void componentOpened() {
        this.refreshData();
    }

    @Override
    public void componentClosed() {
        // TODO add custom code on component closing
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }
}
