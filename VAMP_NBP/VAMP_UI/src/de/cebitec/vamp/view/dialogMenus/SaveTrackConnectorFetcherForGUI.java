package de.cebitec.vamp.view.dialogMenus;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.connector.StorageException;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.view.dataVisualisation.basePanel.BasePanelFactory;
import de.cebitec.vamp.view.login.LoginProperties;
import java.awt.Dialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JOptionPane;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * A class for GUI Components to safely fetch a TrackConnector.
 * @author kstaderm
 */
public class SaveTrackConnectorFetcherForGUI {

    /**
     * Blank constructor of the class.
     */
    public SaveTrackConnectorFetcherForGUI() {
    }

    /**
     * Returns the TrackConnector for the given track. If the track is stored in
     * a sam/bam file and the path to this file has changed, the method will
     * open a window and ask for the new file path.
     *
     * @param track Track the TrackConnector should be received for.
     * @return TrackConnector for the Track handed over
     */
    public TrackConnector getTrackConnector(PersistantTrack track) {
        TrackConnector tc = null;
        ProjectConnector connector = ProjectConnector.getInstance();
        try {
            tc = connector.getTrackConnector(track);
        } catch (FileNotFoundException e) {
            PersistantTrack newTrack = getNewFilePath(track, connector);
            if (newTrack != null) {
                try {
                    tc = connector.getTrackConnector(newTrack);
                } catch (FileNotFoundException ex) {
                    Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                            "{0}: Unable to open data associated with track: " + track.getId(),
                            currentTimestamp);
                }
            } else {
                return null;
            }
        }
        return tc;
    }

    /**
     * Returns the TrackConnector for multiple given tracks. If the tracks are
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     *
     * @param tracks List of tracks the TrackConnector should be received for.
     * @param combineTracks boolean if the Tracks should be combined or not.
     * @return TrackConnector for the list of Tracks handed over.
     */
    public TrackConnector getTrackConnector(List<PersistantTrack> tracks, boolean combineTracks) {
        TrackConnector tc = null;
        ProjectConnector connector = ProjectConnector.getInstance();
        try {
            tc = connector.getTrackConnector(tracks, combineTracks);
        } catch (FileNotFoundException e) {
            for (int i = 0; i < tracks.size(); ++i) {
                PersistantTrack track = tracks.get(i);
                if (!(new File(track.getFilePath())).exists()) {
                    PersistantTrack newTrack = getNewFilePath(track, connector);
                    tracks.set(i, newTrack);
                }
            }
            try {
                tc = connector.getTrackConnector(tracks, combineTracks);
            } catch (FileNotFoundException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                        "{0}: Unable to open data associated with track: " + tracks.toString(),
                        currentTimestamp);
            }
        }
        return tc;
    }

    /**
     * In case a direct access track was moved to another place this method
     * first tries to find the track in the current directory used for resetting
     * track file paths and if it cannot be found it calls the
     * <tt>openResetFilePathDialog</tt> method to open a dialog for resetting
     * the file path to the current location of the file.
     *
     * @author ddoppmeier, rhiler, kstaderm
     * @param track the track whose path has to be reseted
     * @param connector the connector
     * @return the track connector for the updated track or null, if it did not
     * work
     */
    private PersistantTrack getNewFilePath(PersistantTrack track, ProjectConnector connector) {
        PersistantTrack newTrack;
        Preferences prefs = NbPreferences.forModule(Object.class);
        File oldTrackFile = new File(track.getFilePath());
        String basePath = prefs.get("ResetTrack.Filepath", ".");
        newTrack = this.checkFileExists(basePath, oldTrackFile, track);
        if (newTrack == null) {
            prefs = Preferences.userNodeForPackage(LoginProperties.class);
            basePath = new File(prefs.get(LoginProperties.LOGIN_DATABASE_H2, ".")).getParentFile().getAbsolutePath();
            newTrack = this.checkFileExists(basePath, oldTrackFile, track);
        }
        if (newTrack == null) {
            newTrack = this.openResetFilePathDialog(track, connector);
        }
        return newTrack;
    }

    /**
     * Checks if a file exists and creates a new track, if it exists.
     *
     * @author ddoppmeier, rhiler, kstaderm
     *
     * @param basePath
     * @param oldTrackFile the old track file to replace
     * @param track the old track to replace
     * @return the new track, if the file exists, null otherwise
     */
    private PersistantTrack checkFileExists(String basePath, File oldTrackFile, PersistantTrack track) {
        PersistantTrack newTrack = null;
        File newTrackFile = new File(basePath + "/" + oldTrackFile.getName());
        if (newTrackFile.exists()) {
            newTrack = new PersistantTrack(track.getId(),
                    newTrackFile.getAbsolutePath(), track.getDescription(), track.getTimestamp(),
                    track.getRefGenID(), track.getSeqPairId());
        }
        return newTrack;
    }

    /**
     * In case a direct access track was moved to another place and cannot be
     * found this method opens a dialog for resetting the file path to the
     * current location of the file.
     *
     * @author ddoppmeier, rhiler, kstaderm
     *
     * @param track the track whose path has to be resetted
     * @param connector the connector
     * @return the track connector for the updated track or null, if it did not
     * work
     */
    private PersistantTrack openResetFilePathDialog(PersistantTrack track, ProjectConnector connector) {
        PersistantTrack newTrack = null;
        ResetTrackFilePanel resetPanel = new ResetTrackFilePanel(track.getFilePath());
        DialogDescriptor dialogDescriptor = new DialogDescriptor(resetPanel, "Reset File Path");
        Dialog resetFileDialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        resetFileDialog.setVisible(true);

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            try {
                newTrack = new PersistantTrack(track.getId(),
                        resetPanel.getNewFileLocation(), track.getDescription(), track.getTimestamp(),
                        track.getRefGenID(), track.getSeqPairId());
                connector.resetTrackPath(newTrack);
                try {
                    TrackConnector trackConnector = connector.getTrackConnector(newTrack);
                } catch (FileNotFoundException ex) {
                    String msg = NbBundle.getMessage(BasePanelFactory.class, "MSG_BasePanelFactory_FileReset.Error");
                    String title = NbBundle.getMessage(BasePanelFactory.class, "TITLE_BasePanelFactory_FileReset");
                    JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (StorageException ex) {
                String msg = NbBundle.getMessage(BasePanelFactory.class, "MSG_BasePanelFactory_FileReset.StorageError");
                String title = NbBundle.getMessage(BasePanelFactory.class, "TITLE_BasePanelFactory_FileReset");
                JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
            }
        } else {
            String msg = NbBundle.getMessage(BasePanelFactory.class, "MSG_BasePanelFactory_FileReset");
            String title = NbBundle.getMessage(BasePanelFactory.class, "TITLE_BasePanelFactory_FileReset");
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
        return newTrack;
    }
}
