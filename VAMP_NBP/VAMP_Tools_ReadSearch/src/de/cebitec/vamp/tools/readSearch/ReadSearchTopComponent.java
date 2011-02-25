package de.cebitec.vamp.tools.readSearch;

import de.cebitec.vamp.objects.Read;
import de.cebitec.vamp.view.dataVisualisation.trackViewer.TrackViewer;
import java.awt.CardLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.logging.Logger;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
//import org.openide.util.ImageUtilities;
import org.netbeans.api.settings.ConvertAsProperties;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//de.cebitec.vamp.tools.readSearch//ReadSearch//EN", autostore = false)
public final class ReadSearchTopComponent extends TopComponent {

    private static ReadSearchTopComponent instance;
    private static final long serialVersionUID = 1L;
    /** path to the icon used by the component and its open action */
//    static final String ICON_PATH = "SET/PATH/TO/ICON/HERE";
    private static final String PREFERRED_ID = "ReadSearchTopComponent";

    public ReadSearchTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(ReadSearchTopComponent.class, "CTL_ReadSearchTopComponent"));
        setToolTipText(NbBundle.getMessage(ReadSearchTopComponent.class, "HINT_ReadSearchTopComponent"));
//        setIcon(ImageUtilities.loadImage(ICON_PATH, true));

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        readSearchSetup = new de.cebitec.vamp.tools.readSearch.ReadSearchSetup();
        readSearchResults = new de.cebitec.vamp.tools.readSearch.ReadSearchResults();

        setLayout(new java.awt.CardLayout());
        add(readSearchSetup, "setup");
        add(readSearchResults, "results");
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private de.cebitec.vamp.tools.readSearch.ReadSearchResults readSearchResults;
    private de.cebitec.vamp.tools.readSearch.ReadSearchSetup readSearchSetup;
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files only,
     * i.e. deserialization routines; otherwise you could get a non-deserialized instance.
     * To obtain the singleton instance, use {@link #findInstance}.
     */
    public static synchronized ReadSearchTopComponent getDefault() {
        if (instance == null) {
            instance = new ReadSearchTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the ReadSearchTopComponent instance. Never call {@link #getDefault} directly!
     */
    public static synchronized ReadSearchTopComponent findInstance() {
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);
        if (win == null) {
            Logger.getLogger(ReadSearchTopComponent.class.getName()).warning(
                    "Cannot find " + PREFERRED_ID + " component. It will not be located properly in the window system.");
            return getDefault();
        }
        if (win instanceof ReadSearchTopComponent) {
            return (ReadSearchTopComponent) win;
        }
        Logger.getLogger(ReadSearchTopComponent.class.getName()).warning(
                "There seem to be multiple components with the '" + PREFERRED_ID
                + "' ID. That is a potential source of errors and unexpected behavior.");
        return getDefault();
    }

    @Override
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ALWAYS;
    }

    @Override
    public void componentOpened() {
        readSearchSetup.addPropertyChangeListener("readSearchFinished", new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                readSearchResults.addReads((List<Read>) evt.getNewValue());
                ((CardLayout) getLayout()).show(instance, "results");
            }
        });
    }

    @Override
    public void componentClosed() {
        ((CardLayout) getLayout()).show(instance, "setup");
        readSearchSetup.setTrackCon(null);
        readSearchResults.setBoundsInformationManager(null);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    Object readProperties(java.util.Properties p) {
        if (instance == null) {
            instance = this;
        }
        instance.readPropertiesImpl(p);
        return instance;
    }

    private void readPropertiesImpl(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    @Override
    protected String preferredID() {
        return PREFERRED_ID;
    }

    public void setTrackViewer(TrackViewer trackViewer){
        readSearchSetup.setTrackCon(trackViewer.getTrackCon());
        readSearchResults.setBoundsInformationManager(trackViewer.getBoundsInformationManager());
    }
}
