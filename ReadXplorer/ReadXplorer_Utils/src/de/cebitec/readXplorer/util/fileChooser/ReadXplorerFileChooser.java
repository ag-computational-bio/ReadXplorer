package de.cebitec.readXplorer.util.fileChooser;

import de.cebitec.readXplorer.util.Properties;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * readXplorers file chooser. Contains all options and values of how to open a specific
 * file chooser for saving or opening different files.
 * 
 * @author Rolf Hilker
 */
public abstract class ReadXplorerFileChooser  extends JFileChooser {

    protected Object data;
    private String[] fileExtensions;
    private String fileDescription;
    private Preferences pref;
    private String directoryProperty;
    private String currentDirectory;

    /**
     * Creates a new readXplorer file chooser.
     * @param fileExtensions the file extensions to use. If the first entry is the empty string, no file filter is set
     * @param fileDescription description for the files in the file filter
     */
    public ReadXplorerFileChooser(final String[] fileExtensions, String fileDescription){
        this(fileExtensions, fileDescription, null);
    }

    /**
     * Creates a new readXplorer file chooser.
     * @param fileExtensions the file extensions to use. If the first entry is the empty string, no file filter is set
     * @param fileDescription description for the files in the file filter
     * @param data the data which might be used for file choosers storing data
     */
    public ReadXplorerFileChooser(final String[] fileExtensions, String fileDescription, final Object data) {
        this.data = data;
        this.fileExtensions = fileExtensions;
        this.fileDescription = fileDescription;
        if (fileExtensions[0] != null && !fileExtensions[0].isEmpty()) {
            this.setFileFilter(new FileNameExtensionFilter(fileDescription, fileExtensions));
        }
        this.pref = NbPreferences.forModule(Object.class);
        directoryProperty = Properties.READXPLORER_FILECHOOSER_DIRECTORY;
    }

    /**
     * Opens a file chooser for input or output file selection/creation.
     * @param option the option: readXplorerFileChooser.OPEN_DIALOG for file selection and
     * readXplorerFileChooser.SAVE_DIALOG for storing a file.
     */
    public void openFileChooser(final int option) {

        ////////////// open file chooser /////////////////////////
        if (currentDirectory == null || currentDirectory.isEmpty()) {
            currentDirectory = this.pref.get(directoryProperty, ".");
        }
        if (currentDirectory.isEmpty()){
            currentDirectory = ".";
        }
        try {
            this.setCurrentDirectory(new File(new File(currentDirectory).getCanonicalPath()));
        } catch (final IOException exception) {
            this.setCurrentDirectory(null);
        }
        int result;
        if (option == ReadXplorerFileChooser.OPEN_DIALOG) {
            result = this.showOpenDialog(this.getParent());
        } else {
            result = this.showSaveDialog(null);
        }
        ///////////////// store directory ////////////////////////////////////
        try {
            currentDirectory = this.getCurrentDirectory().getCanonicalPath();
            this.pref.put(directoryProperty, currentDirectory);
            this.pref.flush();
        } catch (BackingStoreException e) {
            Logger.getLogger(ReadXplorerFileChooser.class.getName()).log(Level.SEVERE, null, e);
        } catch (IOException ex) {
            // do nothing, path is not stored in properties...
        }
        ////////////// handle return events /////////////////////////////////////////
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String fileLocation = this.getSelectedFile().getAbsolutePath();
//        if (result == JFileChooser.APPROVE_OPTION) {
//        }

        if (option == ReadXplorerFileChooser.OPEN_DIALOG) {
            this.open(fileLocation);
        } else
        if (option == ReadXplorerFileChooser.SAVE_DIALOG) {
            if (!fileExtensions[0].isEmpty() && !fileLocation.endsWith(".".concat(fileExtensions[0]))
                    && !fileLocation.endsWith(".".concat(fileExtensions[0].toUpperCase()))) {
                fileLocation = fileLocation.concat(".".concat(fileExtensions[0]));
            }
            boolean done = this.checkFileExists(fileLocation, this);
            if (!done) {
                this.save(fileLocation);
            }
        }
    }


    /**
     * When a file should be saved this method checks if the file already exists
     * and prompts for replacement. If it doesn't exist yet, it is created.
     * @param fileLocation the file location to store the file
     * @param this the JFileChooser
     */
    private boolean checkFileExists(final String fileLocation, final JFileChooser jfc) {
        File file = new File(fileLocation);
        if (file.exists()) {
            final int overwriteFile = JOptionPane.showConfirmDialog(jfc, NbBundle.getMessage(ReadXplorerFileChooser.class,
                        "readXplorerFileChooser.FileExists"), NbBundle.getMessage(ReadXplorerFileChooser.class,
                        "readXplorerFileChooser.Dialog"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (overwriteFile == JOptionPane.YES_OPTION) {
                this.save(fileLocation);
            } else {
                this.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
            }
            return true;
        }
        return false;
    }
    
    
    /**
     * Saves the data into a file whose file extension is determined
     * by the fileExtensions variable.
     * @param fileLocation the location and name of the file to create
     */
    public abstract void save(String fileLocation);


    /**
     * Opens a file from the current fileLocation and takes care of the file
     * specific handling.
     * @param fileLocation the location and name of the file to create
     */
    public abstract void open(String fileLocation);

    /**
     * Set the directory property which shall be used to store the directory
     * of the selected file/s.
     * @param directoryProperty 
     */
    public void setDirectoryProperty(String directoryProperty) {
        this.directoryProperty = directoryProperty;
    }
    
    /**
     * Sets the directory to use as starting directory for this file chooser.
     * @param directory the directory to use as starting directory for this 
     * file chooser
     */
    public void setDirectory(String directory) {
        this.currentDirectory = directory;
    }
}
