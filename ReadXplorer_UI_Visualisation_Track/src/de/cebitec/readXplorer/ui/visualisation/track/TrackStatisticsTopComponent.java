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

package de.cebitec.readXplorer.ui.visualisation.track;


import de.cebitec.readxplorer.view.TopComponentExtended;
import de.cebitec.readxplorer.view.datavisualisation.referenceviewer.ReferenceViewer;
import de.cebitec.readxplorer.view.datavisualisation.trackviewer.TrackViewer;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Logger;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;


/**
 * Top component which displays something.
 */
@ConvertAsProperties( dtd = "-//de.cebitec.readXplorer.ui.visualisation.track//TrackStatistics//EN", autostore = false )
public final class TrackStatisticsTopComponent extends TopComponentExtended
        implements LookupListener {

    private static final long serialVersionUID = 1L;

    private static TrackStatisticsTopComponent instance;
    private Result<TrackViewer> result;
    /**
     * path to the icon used by the component and its open action
     */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "TrackStatisticsTopComponent";


    public TrackStatisticsTopComponent() {
        initComponents();
        setName( NbBundle.getMessage( TrackStatisticsTopComponent.class, "CTL_TrackStatisticsTopComponent" ) );
        setToolTipText( NbBundle.getMessage( TrackStatisticsTopComponent.class, "HINT_TrackStatisticsTopComponent" ) );
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));
        putClientProperty( TopComponent.PROP_CLOSING_DISABLED, Boolean.TRUE );
        putClientProperty( TopComponent.PROP_MAXIMIZATION_DISABLED, Boolean.TRUE );
        putClientProperty( TopComponent.PROP_KEEP_PREFERRED_SIZE_WHEN_SLIDED_IN, Boolean.TRUE );

    }


    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        trackStatisticsPanel = new de.cebitec.readXplorer.ui.visualisation.track.TrackStatisticsPanel();

        setLayout(new java.awt.BorderLayout());

        trackStatisticsPanel.setMinimumSize(new java.awt.Dimension(157, 200));
        add(trackStatisticsPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cebitec.readXplorer.ui.visualisation.track.TrackStatisticsPanel trackStatisticsPanel;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized
     * instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     * <p>
     * @return the track statistics top component
     */
    public static synchronized TrackStatisticsTopComponent getDefault() {
        if( instance == null ) {
            instance = new TrackStatisticsTopComponent();
        }
        return instance;
    }


    /**
     * Obtain the TrackStatisticsTopComponent instance. Never call
     * {@link #getDefault} directly!
     * <p>
     * @return the track statistics top component
     */
    public static synchronized TrackStatisticsTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent( PREFERRED_ID );
        if( win == null ) {
            Logger.getLogger( TrackStatisticsTopComponent.class.getName() ).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system." );
            return getDefault();
        }
        if( win instanceof TrackStatisticsTopComponent ) {
            return (TrackStatisticsTopComponent) win;
        }
        Logger.getLogger( TrackStatisticsTopComponent.class.getName() ).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior." );
        return getDefault();
    }


    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }


    @Override
    public void componentOpened() {
        result = Utilities.actionsGlobalContext().lookupResult( TrackViewer.class );
        result.addLookupListener( this );
        resultChanged( new LookupEvent( result ) );
    }


    @Override
    public void resultChanged( LookupEvent ev ) {
        if( result.allInstances().isEmpty() && !Utilities.actionsGlobalContext().lookupAll( ReferenceViewer.class ).isEmpty() ) {
            setVisible( false );
        }
        else {
            if( !isVisible() ) {
                setVisible( true );
            }
            for( TrackViewer trackViewer : result.allInstances() ) {
                trackStatisticsPanel.setTrackConnector( trackViewer.getTrackCon() );

                trackViewer.addMouseListener( new MouseListener() {

                    @Override
                    public void mouseClicked( MouseEvent e ) {
                        trackStatisticsPanel.setTrackConnector( ((TrackViewer) e.getSource()).getTrackCon() );
                    }


                    @Override
                    public void mousePressed( MouseEvent e ) {
                    }


                    @Override
                    public void mouseReleased( MouseEvent e ) {
                    }


                    @Override
                    public void mouseEntered( MouseEvent e ) {
                    }


                    @Override
                    public void mouseExited( MouseEvent e ) {
                    }


                } );
            }
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


}
