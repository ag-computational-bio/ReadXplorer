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

package de.cebitec.readXplorer.ui.visualisation;


import de.cebitec.readxplorer.api.ApplicationFrameI;
import de.cebitec.readxplorer.api.cookies.CloseRefGenCookie;
import de.cebitec.readxplorer.api.cookies.CloseTrackCookie;
import de.cebitec.readxplorer.api.cookies.OpenTrackCookie;
import de.cebitec.readXplorer.util.VisualisationUtils;
import de.cebitec.readxplorer.view.TopComponentExtended;
import de.cebitec.readxplorer.view.controller.ViewController;
import de.cebitec.readxplorer.view.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.view.datavisualisation.basePanel.BasePanel;
import de.cebitec.readxplorer.view.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.view.datavisualisation.trackviewer.MultipleTrackViewer;
import de.cebitec.readxplorer.view.datavisualisation.trackviewer.TrackViewer;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Top component which displays the main work area of ReadXplorer, which
 * contains the
 * reference and track viewers.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
@ConvertAsProperties(
         dtd = "-//de.cebitec.readXplorer.ui.visualisation//AppPanel//EN",
         autostore = false
)
@TopComponent.Description(
         preferredID = "AppPanelTopComponent",
         //iconBase="SET/PATH/TO/ICON/HERE",
         persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration( mode = "editor", openAtStartup = false )
@ActionID( category = "Window", id = "de.cebitec.readXplorer.ui.visualisation.AppPanelTopComponent" )
@ActionReference( path = "Menu/Window" /*, position = 333 */ )
@TopComponent.OpenActionRegistration(
         displayName = "#CTL_AppPanelAction",
         preferredID = "AppPanelTopComponent"
)
@Messages( {
    "CTL_AppPanelAction=AppPanel",
    "CTL_AppPanelTopComponent=AppPanel Window",
    "HINT_AppPanelTopComponent=This is a AppPanel window"
} )
public final class AppPanelTopComponent extends TopComponentExtended implements
        ApplicationFrameI {

    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "AppPanelTopComponent";
    private static final long serialVersionUID = 1L;
    private static AppPanelTopComponent instance;
    private final InstanceContent content = new InstanceContent();
    private final Lookup localLookup;
    private ReferenceViewer referenceViewer;
    private final ArrayList<TrackViewer> trackViewerList;
    private JScrollPane trackScrollPane;
    private JScrollPane basePanelScrollPane;
    private final JPanel tracksPanel;
    private final JPanel basePanelPanel;


    /**
     * Top component which displays the main work area of ReadXplorer, which
     * contains
     * the reference and track viewers.
     */
    public AppPanelTopComponent() {
        initComponents();
        setName( Bundle.CTL_AppPanelTopComponent() );
        setToolTipText( Bundle.HINT_AppPanelTopComponent() );
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        localLookup = new AbstractLookup( content );
        associateLookup( localLookup );
        this.trackViewerList = new ArrayList<>();
        this.tracksPanel = new JPanel();
        this.tracksPanel.setLayout( new javax.swing.BoxLayout( tracksPanel, javax.swing.BoxLayout.PAGE_AXIS ) );
        this.basePanelPanel = new JPanel();
        this.basePanelPanel.setLayout( new javax.swing.BoxLayout( basePanelPanel, javax.swing.BoxLayout.PAGE_AXIS ) );
    }


    /**
     * Removes all cookies which are contained in TopComponent.getLookup().
     */
    private void clearLookup() {
        Collection<? extends Object> allCookies = getLookup().lookupAll( Object.class );

        for( Object cookie : allCookies ) {
            content.remove( cookie );
        }
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        visualPanel = new javax.swing.JPanel();

        visualPanel.setLayout(new javax.swing.BoxLayout(visualPanel, javax.swing.BoxLayout.PAGE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(visualPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(visualPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel visualPanel;
    // End of variables declaration//GEN-END:variables


    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_NEVER;
    }


    /**
     * Sets the view controller for this top component, which will display the
     * list of references and open the reference viewer afterwards.
     */
    @Override
    public void componentOpened() {
        ViewController vc = new ViewController( this );
        //vc.openRefGen();
        content.add( vc );

        if( vc.hasRefGen() ) {
            setName( vc.getDisplayName() );
        }
        //else {
        //    this.close();
        //}
    }


    @Override
    public void componentClosed() {
        // remove all viewers
        Component[] comps = visualPanel.getComponents();
        for( Component comp : comps ) {
            if( comp instanceof BasePanel ) {
                try {
                    ((BasePanel) comp).getViewer().close();
                }
                catch( NullPointerException e ) {
                    //do nothing, we want to close something, which is already destroyed
                }
            }
        }
        visualPanel.removeAll();

        // remove all cookies
        clearLookup();

        // if last Viewer close Navigator
        boolean lastViewer = true;
        WindowManager windowManager = WindowManager.getDefault();
        for( TopComponent tc : windowManager.getRegistry().getOpened() ) {
            if( tc instanceof ApplicationFrameI && !tc.equals( this ) ) {
                lastViewer = false;
                break;
            }
        }
        if( lastViewer ) {
            windowManager.findTopComponent( "ReferenceNavigatorTopComp" ).close();
            windowManager.findTopComponent( "ReferenceIntervalTopComp" ).close();
            windowManager.findTopComponent( "ReferenceFeatureTopComp" ).close();
            windowManager.findTopComponent( "TrackStatisticsTopComponent" ).close();
        }
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


    @Override
    public Lookup getLookup() {
        return localLookup;
    }


    // ===================== AppFrameI stuff ============================== //
    @Override
    public void showRefGenPanel( JPanel refGenPanel ) {
        visualPanel.add( refGenPanel );
        visualPanel.updateUI();

        WindowManager.getDefault().findTopComponent( "ReferenceNavigatorTopComp" ).open();
        WindowManager.getDefault().findTopComponent( "ReferenceIntervalTopComp" ).open();
        WindowManager.getDefault().findTopComponent( "ReferenceFeatureTopComp" ).open();

        // put the panels ReferenceViewer in lookup so it can be accessed
        BasePanel bp = (BasePanel) refGenPanel;
        ReferenceViewer rv = (ReferenceViewer) bp.getViewer();
        rv.setViewerLookup( localLookup );
        content.add( rv );
        this.referenceViewer = rv;

        content.add( new CloseRefGenCookie() {
            @Override
            public boolean close() {
                getLookup().lookup( ViewController.class ).closeRefGen();
                content.remove( this );
                return true;
            }


        } );
        content.add( new OpenTrackCookie() {
            @Override
            public void open() {
                getLookup().lookup( ViewController.class ).openTrack();
            }


        } );
    }


    @Override
    public void removeRefGenPanel( JPanel genomeViewer ) {
        this.close();
    }


    @Override
    public void showTrackPanel( final JPanel trackPanel ) {
        if( this.trackScrollPane == null ) {
            this.trackScrollPane = new JScrollPane( this.tracksPanel );
            trackScrollPane.setPreferredSize( new Dimension( trackScrollPane.getWidth(), 100 ) );
            this.visualPanel.add( this.trackScrollPane );
        }

        // add the trackPanel
        tracksPanel.add( trackPanel );
        visualPanel.updateUI();
        this.referenceViewer.increaseTrackCount();

        // search for opened tracks, if there are none open the track statistics window
        if( getLookup().lookupAll( TrackViewer.class ).isEmpty() ) {
            WindowManager.getDefault().findTopComponent( "TrackStatisticsTopComponent" ).open();
        }

        // put the panel's TrackViewer in lookup so it can be accessed
        BasePanel basePanel = (BasePanel) trackPanel;
        // make sure the multiple track viewers do not cause a mess in the lookup
        if( !(basePanel.getViewer() instanceof MultipleTrackViewer) ) {
            TrackViewer trackViewer = (TrackViewer) basePanel.getViewer();
            this.content.add( trackViewer );
            this.trackViewerList.add( trackViewer );
        }

        CloseTrackCookie closeTrackCookie = new CloseTrackCookie() {
            @Override
            public boolean close() {
                getLookup().lookup( ViewController.class ).closeTrackPanel( trackPanel );
                content.remove( this );
                referenceViewer.decreaseTrackCount();
                return true;
            }


            @Override
            public String getName() {
                return trackPanel.getName();
            }


        };

        content.add( closeTrackCookie );
//        all.add(new WeakReference<JPanel>(trackPanel));
    }


    @Override
    public void closeTrackPanel( JPanel trackPanel ) {
        tracksPanel.remove( trackPanel );
        visualPanel.updateUI();

        // remove the panel's TrackViewer from lookup
        BasePanel bp = (BasePanel) trackPanel;
        TrackViewer tv = (TrackViewer) bp.getViewer();
        content.remove( tv );
        trackViewerList.remove( tv );

        // if this was the last trackPanel close the track statistics window
        if( getLookup().lookupAll( TrackViewer.class ).isEmpty() ) {
            WindowManager.getDefault().findTopComponent( "TrackStatisticsTopComponent" ).close();
        }
    }


    public void showBasePanel( final BasePanel basePanel ) {
        if( this.basePanelScrollPane == null ) {
            this.basePanelScrollPane = new JScrollPane( this.basePanelPanel );
            basePanelScrollPane.setPreferredSize( new Dimension( basePanelScrollPane.getWidth(), 50 ) );
            this.visualPanel.add( this.basePanelScrollPane );
        }

        // add the basePanelPanel
        basePanelPanel.add( basePanel );

        // put the panel's Viewer in lookup so it can be accessed
        AbstractViewer viewer = basePanel.getViewer();
        this.content.add( viewer );

        //maybe use this later
//        CloseTrackCookie closeViewerCookie = new CloseTrackCookie() {
//            @Override
//            public boolean close() {
//                getLookup().lookup(ViewController.class).closeTrackPanel(basePanel);
//                content.remove(this);
//                return true;
//            }
//
//            @Override
//            public String getName() {
//                return basePanel.getName();
//            }
//        };
//
//        content.add(closeViewerCookie);
    }

//    /**
//     * Checks all components recursively for a JPanel and returns the first one
//     * found. If there is no JPanel among the components, null is returned.
//     * @param comps component array to check for a JPanel
//     * @return The first identified JPanel or null, if there is no JPanel
//     */
//    private Component getJPanel(Component[] comps) {
//        for (Component comp : comps) {
//            if (comp instanceof JPanel) {
//                return (JPanel) comp;
//            } else if (comp instanceof Container) {
//                return this.getJPanel(((Container) comp).getComponents());
//            }
//        }
//        return null;
//    }

    /*
     * Overriding these two methods ensures that only displayed components are updated
     * and thus increases performance of the viewers.
     */
    @Override
    public void componentShowing() {
        if( referenceViewer != null ) {
            this.referenceViewer.setActive( true );
            this.changeActiveTrackStatus( true );
        }
    }


    @Override
    public void componentHidden() {
        if( referenceViewer != null ) {
            this.referenceViewer.setActive( false );
            this.changeActiveTrackStatus( false );
        }
    }


    /**
     * Updates the status of all track viewers belonging to this top component.
     * <p>
     * @param isActive true, if track viewers should be active, false, if not.
     */
    private void changeActiveTrackStatus( boolean isActive ) {
        for( TrackViewer viewer : this.trackViewerList ) {
            viewer.setActive( isActive );
        }
    }


    // ==================== Experimental track closing stuff ==================== //
    private final List<Reference<JPanel>> all = Collections.synchronizedList( new ArrayList<Reference<JPanel>>() );


    public List<Action> allTrackCloseActions() {
        List<Action> result = new ArrayList<>();
        for( Iterator<Reference<JPanel>> it = all.iterator(); it.hasNext(); ) {
            Reference<JPanel> cookieRef = it.next();
            JPanel cookie = cookieRef.get();
            if( cookie == null ) {
                it.remove();
            }
            else {
                result.add( new ShowAction( cookie.getName(), cookieRef, new WeakReference<>( getLookup().lookup( ViewController.class ) ) ) );
            }
        }
        return result;
    }


    public ReferenceViewer getReferenceViewer() {
        return this.referenceViewer;
    }


    private static final class ShowAction extends AbstractAction {

        private static final long serialVersionUID = 1L;
        //Our action should not hold a strong reference to the TopComponent -
        //if it is closed, it should get garbage collected.  If a menu
        //item holds a reference to the component, then it won't be
        private final Reference<JPanel> tc;
        private final Reference<ViewController> vc;


        ShowAction( String name, Reference<JPanel> tc, Reference<ViewController> vc ) {
            this.tc = tc;
            this.vc = vc;
            putValue( NAME, name );
        }


        @Override
        public void actionPerformed( ActionEvent e ) {
            JPanel comp = tc.get();
            if( comp != null ) { //Could have been garbage collected
                vc.get().closeTrackPanel( comp );
            }
            else {
                //will almost never happen
                Toolkit.getDefaultToolkit().beep();
            }
        }


        @Override
        public boolean isEnabled() {
            JPanel comp = tc.get();
            return comp != null;
        }


    }


    @Override
    public void paint( Graphics g ) {
        try {
            super.paint( g );
        }
        catch( OutOfMemoryError e ) {
            VisualisationUtils.displayOutOfMemoryError( this );
        }
    }


}
