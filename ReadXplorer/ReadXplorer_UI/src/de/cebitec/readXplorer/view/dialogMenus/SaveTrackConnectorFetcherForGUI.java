package de.cebitec.readXplorer.view.dialogMenus;

import de.cebitec.readXplorer.databackend.SamBamFileReader;
import de.cebitec.readXplorer.databackend.connector.MultiTrackConnector;
import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.StorageException;
import de.cebitec.readXplorer.databackend.connector.TrackConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantTrack;
import static de.cebitec.readXplorer.view.dialogMenus.Bundle.*;
import de.cebitec.readXplorer.view.login.LoginProperties;
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
import net.sf.samtools.util.RuntimeIOException;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;

/**
 * A class for GUI Components to safely fetch a TrackConnector.
 *
 * @author kstaderm, rhilker
 */
@Messages({ "TITLE_FileReset=Reset track file path",
            "MSG_FileReset=If you do not reset the track file location, it cannot be opened",
            "MSG_FileReset_StorageError=An error occured during storage of the new file path. Please try again" })
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
     * @throws UserCanceledTrackPathUpdateException if the no track path could be resolved.
     * @param track Track the TrackConnector should be received for.
     * @return TrackConnector for the Track handed over
     */
    public TrackConnector getTrackConnector(PersistantTrack track) throws UserCanceledTrackPathUpdateException {
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
                //If the new path is not set by the user throw exception notifying about this
                throw new UserCanceledTrackPathUpdateException();
            }
        }
        return tc;
    }

    /**
     * Returns the TrackConnector for multiple given tracks. If the tracks are
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     *
     * @throws UserCanceledTrackPathUpdateException if the no track path could be resolved.
     * @param tracks List of tracks the TrackConnector should be received for.
     * @param combineTracks boolean if the Tracks should be combined or not.
     * @return TrackConnector for the list of Tracks handed over. 
     * CAUTION: 
     * tracks are removed if their path cannot be resolved and the user refuses 
     * to set a new one.
     */
    public TrackConnector getTrackConnector(List<PersistantTrack> tracks, boolean combineTracks) throws UserCanceledTrackPathUpdateException {
        TrackConnector tc = null;
        ProjectConnector connector = ProjectConnector.getInstance();
        try {
            tc = connector.getTrackConnector(tracks, combineTracks);
        } catch (FileNotFoundException e) {
            //we keep track about the number of tracks with unresolved path errors.
            int unresolvedTracks = 0;
            for (int i = 0; i < tracks.size(); ++i) {                
                PersistantTrack track = tracks.get(i);
                if (!(new File(track.getFilePath())).exists()) {
                    PersistantTrack newTrack = getNewFilePath(track, connector);
                    //Everything is fine, path is set correctly
                    if (newTrack != null) {
                        tracks.set(i, newTrack);                   
                    } else {
                        //User canceled path update, add an unresolved track
                        unresolvedTracks++;
                        //And remove the track with wrong path from the list of processed tracks.
                        tracks.remove(i);
                    }
                }
            }
            //All track paths are tested, if no path can be resolved an exception is thrown.
            if (unresolvedTracks == tracks.size()) {
                throw new UserCanceledTrackPathUpdateException();
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
     * @author rhilker, kstaderm
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
        newTrack = this.checkFileExists(basePath, oldTrackFile, track, connector);
        if (newTrack == null) {
            prefs = Preferences.userNodeForPackage(LoginProperties.class);
            basePath = new File(prefs.get(LoginProperties.LOGIN_DATABASE_H2, ".")).getParentFile().getAbsolutePath();
            newTrack = this.checkFileExists(basePath, oldTrackFile, track, connector);
        }
        if (newTrack == null) {
            newTrack = this.openResetFilePathDialog(track, connector);
        }
        return newTrack;
    }

    /**
     * Checks if a file exists and creates a new track, if it exists.
     *
     * @author rhilker, kstaderm
     *
     * @param basePath
     * @param oldTrackFile the old track file to replace
     * @param track the old track to replace
     * @return the new track, if the file exists, null otherwise
     */
    private PersistantTrack checkFileExists(String basePath, File oldTrackFile, PersistantTrack track, ProjectConnector connector) {
        PersistantTrack newTrack = null;
        File newTrackFile = new File(basePath + "/" + oldTrackFile.getName());
        if (newTrackFile.exists()) {
            newTrack = new PersistantTrack(track.getId(),
                    newTrackFile.getAbsolutePath(), track.getDescription(), track.getTimestamp(),
                    track.getRefGenID(), track.getReadPairId());
            try {
                SamBamFileReader reader = new SamBamFileReader(newTrackFile, track.getId(), connector.getRefGenomeConnector(track.getRefGenID()).getRefGenome());
                try {
                    connector.resetTrackPath(newTrack);
                } catch (StorageException ex) {
                    String msg = MSG_FileReset_StorageError();
                    String title = TITLE_FileReset();
                    JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (RuntimeIOException e) {
                //nothing to do, we return a null track
            }
        }
        return newTrack;
    }

    /**
     * In case a direct access track was moved to another place and cannot be
     * found this method opens a dialog for resetting the file path to the
     * current location of the file.
     *
     * @author rhilker, kstaderm
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

        if (dialogDescriptor.getValue().equals(DialogDescriptor.OK_OPTION))  {
            if (resetPanel.getNewFileLocation() != null) {
                File selectedFile = new File(resetPanel.getNewFileLocation());
                if (selectedFile.exists() && selectedFile.isFile()) {
                    try {
                        newTrack = new PersistantTrack(track.getId(),
                                resetPanel.getNewFileLocation(), track.getDescription(), track.getTimestamp(),
                                track.getRefGenID(), track.getReadPairId());
                        connector.resetTrackPath(newTrack);
                        try {
                            TrackConnector trackConnector = connector.getTrackConnector(newTrack);
                        } catch (FileNotFoundException ex) {
                            String msg = MSG_FileReset();
                            String title = TITLE_FileReset();
                            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (StorageException ex) {
                        String msg = MSG_FileReset_StorageError();
                        String title = TITLE_FileReset();
                        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        } else {
            String msg = MSG_FileReset();
            String title = TITLE_FileReset();
            JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }
        return newTrack;
    }

    public MultiTrackConnector getMultiTrackConnector(PersistantTrack track) throws UserCanceledTrackPathUpdateException {
        MultiTrackConnector mtc = null;
        ProjectConnector connector = ProjectConnector.getInstance();
        try {
            mtc = connector.getMultiTrackConnector(track);
        } catch (FileNotFoundException e) {
            PersistantTrack newTrack = getNewFilePath(track, connector);
            if (newTrack != null) {
                try {
                    mtc = connector.getMultiTrackConnector(newTrack);
                } catch (FileNotFoundException ex) {
                    Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                            "{0}: Unable to open data associated with track: " + track.getId(),
                            currentTimestamp);
                }
            } else {
                //If the new path is not set by the user throw exception notifying about this
                throw new UserCanceledTrackPathUpdateException();
            }
        }
        return mtc;
    }
    
    /**
     * Returns the TrackConnector for multiple given tracks. If the tracks are
     * stored in a sam/bam file and the path to this file has changed, the
     * method will open a window and ask for the new file path.
     * @throws UserCanceledTrackPathUpdateException if the no track path could
     * be resolved.
     * @param tracks List of tracks the TrackConnector should be received for.
     * @return  
     */
    public MultiTrackConnector getMultiTrackConnector(List<PersistantTrack> tracks) throws UserCanceledTrackPathUpdateException {
        MultiTrackConnector mtc = null;
        ProjectConnector connector = ProjectConnector.getInstance();
        try {
            mtc = connector.getMultiTrackConnector(tracks);
        } catch (FileNotFoundException e) {
            //we keep track about the number of tracks with unresolved path errors.
            int unresolvedTracks = 0;
            for (int i = 0; i < tracks.size(); ++i) {
                PersistantTrack track = tracks.get(i);
                if (!(new File(track.getFilePath())).exists()) {
                    PersistantTrack newTrack = getNewFilePath(track, connector);
                    //Everything is fine, path is set correctly
                    if (newTrack != null) {
                        tracks.set(i, newTrack);
                    } else {
                        //User canceled path update, add an unresolved track
                        unresolvedTracks++;
                        //And remove the track with wrong path from the list of processed tracks.
                        tracks.remove(i);
                    }
                }
            }
            //All track paths are tested, if no path can be resolved an exception is thrown.
            if (unresolvedTracks == tracks.size()) {
                throw new UserCanceledTrackPathUpdateException();
            }
            try {
                mtc = connector.getMultiTrackConnector(tracks);
            } catch (FileNotFoundException ex) {
                Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE,
                        "{0}: Unable to open data associated with track: " + tracks.toString(),
                        currentTimestamp);
            }
        }
        return mtc;
    }

    /**
     * Exception which should be thrown if the user cancels the update of a 
     * missing track file path.
     */
    public static class UserCanceledTrackPathUpdateException extends Exception {

        private static String errorMessage = "The user canceled the track path update. Thus, no TrackConnector can be created!";
        private static final long serialVersionUID = 1L;
        
        /**
         * Exception which should be thrown if the user cancels the update of a
         * missing track file path.
         */
        public UserCanceledTrackPathUpdateException() {
            super(errorMessage);
            Date currentTimestamp = new Timestamp(Calendar.getInstance().getTime().getTime());
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, "{0}: " + errorMessage, currentTimestamp);
        }
    }
}
