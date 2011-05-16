/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.vamp.util.fileChooser;

import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import de.cebitec.vamp.util.Properties;

/**
 * Vamps file chooser. Contains all options and values of how to open a specific
 * file chooser for saving or opening different files
 *
 * @author Rolf Hilker
 */
public abstract class VampFileChooser  extends JFileChooser {

    /** Number representing the open file chooser event. */
    public static final int OPEN = 0;
    /** Number representing the save fasta event. */
    public static final int SAVE = 1;
    protected Object data;
    protected String fileExtension;
    protected Preferences pref;

    public VampFileChooser(final int option, final String fileExtension){
        this(option, fileExtension, null);
    }

    public VampFileChooser(final int option, final String fileExtension, final Object data) {
        this.data = data;
        this.fileExtension = fileExtension.toLowerCase();
        this.pref = NbPreferences.forModule(Object.class);
        this.openFileChooser(option);
    }

    /**
     * Opens a file chooser for input or output file selection/creation.
     * @param option the option: VampFileChooser.OPEN for file selection and
     * VampFileChooser.SAVE for storing a file.
     */
    private void openFileChooser(final int option) {

        ////////////// open file chooser /////////////////////////
        final JFileChooser jfc = new JFileChooser();
        String currentDirectory = this.pref.get(Properties.VAMP_FILECHOOSER_DIRECTORY, ".");
        if (currentDirectory.isEmpty()){
            currentDirectory = ".";
        }
        try {
            jfc.setCurrentDirectory(new File(new File(currentDirectory).getCanonicalPath()));
        } catch (final IOException exception) {
            jfc.setCurrentDirectory(null);
        }
        int result;
        if (option == VampFileChooser.OPEN) {
            result = jfc.showOpenDialog(this.getParent());
        } else {
            if (option == VampFileChooser.SAVE) {
                jfc.setFileFilter(new FileNameExtensionFilter(this.fileExtension, this.fileExtension));
            }
            result = jfc.showSaveDialog(null);
        }
        ///////////////// store directory ////////////////////////////////////
        try {
            currentDirectory = jfc.getCurrentDirectory().getCanonicalPath();
            NbPreferences.forModule(Object.class).put(Properties.VAMP_FILECHOOSER_DIRECTORY, currentDirectory);
        } catch (IOException ex) {
            // do nothing, path is not stored in properties...
        }
        ////////////// handle return events /////////////////////////////////////////
        if (result == JFileChooser.CANCEL_OPTION) {
            return;
        }
        String fileLocation = jfc.getSelectedFile().getAbsolutePath();
        if (result == JFileChooser.APPROVE_OPTION) {
            if (!fileLocation.endsWith(".".concat(this.fileExtension)) && !fileLocation.endsWith(".".concat(this.fileExtension.toUpperCase()))){
        	fileLocation = fileLocation.concat(".".concat(this.fileExtension));
            }
        }

        if (option == VampFileChooser.OPEN) {
            this.open(fileLocation);
        } else
        if (option == VampFileChooser.SAVE) {
            boolean done = this.checkFileExists(fileLocation, jfc);
            if (!done){
                this.save(fileLocation);
            }
        }
    }


    /**
     * When a file should be saved this method checks if the file already exists
     * and prompts for replacement. If it doesn't exist yet, it is created.
     * @param fileLocation the file location to store the file
     * @param jfc the JFileChooser
     */
    private boolean checkFileExists(final String fileLocation, final JFileChooser jfc) {
        File file = new File(fileLocation);
        if (file.exists()) {
            final int overwriteFile = JOptionPane.showConfirmDialog(jfc, NbBundle.getMessage(VampFileChooser.class,
                        "VampFileChooser.FileExists"), NbBundle.getMessage(VampFileChooser.class,
                        "VampFileChooser.Dialog"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (overwriteFile == JOptionPane.YES_OPTION) {
                this.save(fileLocation);
            } else {
                this.openFileChooser(VampFileChooser.SAVE);
            }
            return true;
        }
        return false;
    }
    
    
    /**
     * Saves the data into a file whose file extension is determined
     * by the fileExtension variable.
     * @param fileLocation the location and name of the file to create
     */
    public abstract void save(String fileLocation);


    /**
     * Opens a file from the current fileLocation and takes care of the file
     * specific handling.
     * @param fileLocation the location and name of the file to create
     */
    public abstract void open(String fileLocation);


}
