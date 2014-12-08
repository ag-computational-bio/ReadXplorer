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
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


/**
 * ReadXplorers file chooser. Contains all options and values of how to open a
 * specific file chooser for saving or opening different files.
 *
 * @author Rolf Hilker
 */
public abstract class ReadXplorerFileChooser extends JFileChooser {

    private static final long serialVersionUID = 1L;

    protected Object data;
    private String[] fileExtensions;
    private String fileDescription;
    private Preferences pref;
    private String directoryProperty;
    private String currentDirectory;


    /**
     * Creates a new readXplorer file chooser.
     *
     * @param fileExtensions  the file extensions to use. If the first entry is
     *                        the empty string, no file filter is set
     * @param fileDescription description for the files in the file filter
     */
    public ReadXplorerFileChooser( final String[] fileExtensions, String fileDescription ) {
        this( fileExtensions, fileDescription, null );
    }


    /**
     * Creates a new readXplorer file chooser.
     *
     * @param fileExtensions  the file extensions to use. If the first entry is
     *                        the empty string, no file filter is set
     * @param fileDescription description for the files in the file filter
     * @param data            the data which might be used for file choosers
     *                        storing data
     */
    public ReadXplorerFileChooser( final String[] fileExtensions, String fileDescription, final Object data ) {
        this.data = data;
        this.fileExtensions = fileExtensions;
        this.fileDescription = fileDescription;
        if( fileExtensions != null && !fileExtensions[0].isEmpty() ) {
            this.setFileFilter( new FileNameExtensionFilter( fileDescription, fileExtensions ) );
        }
        this.pref = NbPreferences.forModule( Object.class );
        directoryProperty = Properties.READXPLORER_FILECHOOSER_DIRECTORY;
    }


    /**
     * Opens a file chooser for input or output file selection/creation.
     *
     * @param option the option: readXplorerFileChooser.OPEN_DIALOG for file
     *               selection and readXplorerFileChooser.SAVE_DIALOG for storing a file.
     */
    public void openFileChooser( final int option ) {

        ////////////// open file chooser /////////////////////////
        if( currentDirectory == null || currentDirectory.isEmpty() ) {
            currentDirectory = this.pref.get( directoryProperty, "." );
        }
        if( currentDirectory.isEmpty() ) {
            currentDirectory = ".";
        }
        try {
            this.setCurrentDirectory( new File( new File( currentDirectory ).getCanonicalPath() ) );
        }
        catch( final IOException exception ) {
            this.setCurrentDirectory( null );
        }
        int result;
        if( option == ReadXplorerFileChooser.OPEN_DIALOG ) {
            result = this.showOpenDialog( this.getParent() );
        }
        else {
            if( option == ReadXplorerFileChooser.CUSTOM_DIALOG ) {
                result = this.showDialog( this.getParent(), "Select" );
            }
            else {
                result = this.showSaveDialog( null );
            }
        }
        ///////////////// store directory ////////////////////////////////////
        try {
            currentDirectory = this.getCurrentDirectory().getCanonicalPath();
            this.pref.put( directoryProperty, currentDirectory );
            this.pref.flush();
        }
        catch( BackingStoreException e ) {
            Logger.getLogger( ReadXplorerFileChooser.class.getName() ).log( Level.SEVERE, null, e );
        }
        catch( IOException ex ) {
            // do nothing, path is not stored in properties...
        }
        ////////////// handle return events /////////////////////////////////////////
        if( result == JFileChooser.CANCEL_OPTION ) {
            return;
        }
        String fileLocation = this.getSelectedFile().getAbsolutePath();
//        if (result == JFileChooser.APPROVE_OPTION) {
//        }

        if( option == ReadXplorerFileChooser.OPEN_DIALOG ) {
            this.open( fileLocation );
        }
        else if( option == ReadXplorerFileChooser.SAVE_DIALOG ) {
            fileLocation = ReadXplorerFileChooser.getSelectedFileWithExtension( this ).getAbsolutePath();
            boolean done = this.checkFileExists( fileLocation, this );
            if( !done ) {
                this.save( fileLocation );
            }
        }
        if( option == ReadXplorerFileChooser.CUSTOM_DIALOG ) {
            File file = new File( fileLocation );
            if( file.exists() ) {
                this.open( fileLocation );
            }
            else {
                this.save( fileLocation );
            }
        }
    }


    /**
     * When a file should be saved this method checks if the file already exists
     * and prompts for replacement. If it doesn't exist yet, it is created.
     *
     * @param fileLocation the file location to store the file
     * @param this         the JFileChooser
     */
    private boolean checkFileExists( final String fileLocation, final JFileChooser jfc ) {
        File file = new File( fileLocation );
        if( file.exists() ) {
            final int overwriteFile = JOptionPane.showConfirmDialog( jfc, NbBundle.getMessage( ReadXplorerFileChooser.class,
                                                                                               "readXplorerFileChooser.FileExists" ), NbBundle.getMessage( ReadXplorerFileChooser.class,
                                                                                                                                                           "readXplorerFileChooser.Dialog" ), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
            if( overwriteFile == JOptionPane.YES_OPTION ) {
                this.save( fileLocation );
            }
            else {
                this.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
            }
            return true;
        }
        return false;
    }


    /**
     * Saves the data into a file whose file extension is determined by the
     * fileExtensions variable.
     *
     * @param fileLocation the location and name of the file to create
     */
    public abstract void save( String fileLocation );


    /**
     * Opens a file from the current fileLocation and takes care of the file
     * specific handling.
     *
     * @param fileLocation the location and name of the file to create
     */
    public abstract void open( String fileLocation );


    /**
     * Set the directory property which shall be used to store the directory of
     * the selected file/s.
     *
     * @param directoryProperty
     */
    public void setDirectoryProperty( String directoryProperty ) {
        this.directoryProperty = directoryProperty;
    }


    /**
     * Sets the directory to use as starting directory for this file chooser.
     *
     * @param directory the directory to use as starting directory for this file
     *                  chooser
     */
    public void setDirectory( String directory ) {
        this.currentDirectory = directory;
    }


    /**
     * Returns the selected file from a JFileChooser, including the extension
     * from the file filter.
     *
     * @param chooser the chooser whose file is needed with its extension
     * <p>
     * @return The file including its extension.
     */
    public static File getSelectedFileWithExtension( JFileChooser chooser ) {
        File file = chooser.getSelectedFile();
        if( chooser.getFileFilter() instanceof FileNameExtensionFilter ) {
            String[] extensions = ((FileNameExtensionFilter) chooser.getFileFilter()).getExtensions();
            for( String extension : extensions ) { // check if it already has a valid extension
                if( file.getName().endsWith( '.' + extension ) ) {
                    return file;
                }
            }
            // if not, append the first extension from the selected filter
            file = new File( file.getAbsolutePath() + '.' + extensions[0] );
        }
        else { //if no appropriate filter is currently selected, the first extensions of the first appropriate extension filter is appended to the file name
            FileFilter[] filters = chooser.getChoosableFileFilters();
            if( filters.length > 0 ) {
                for( FileFilter filter : filters ) {
                    if( filter instanceof FileNameExtensionFilter ) {
                        FileNameExtensionFilter extensionFilter = (FileNameExtensionFilter) filter;
                        file = new File( file.getAbsolutePath() + '.' + extensionFilter.getExtensions()[0] );
                        break;
                    }
                }
            }
        }
        return file;
    }


}
