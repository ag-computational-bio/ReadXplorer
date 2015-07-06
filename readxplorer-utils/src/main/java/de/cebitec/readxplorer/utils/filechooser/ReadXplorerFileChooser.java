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

package de.cebitec.readxplorer.utils.filechooser;


import de.cebitec.readxplorer.api.constants.Paths;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

import static java.util.logging.Level.SEVERE;


/**
 * ReadXplorers file chooser. Contains all options and values of how to open a
 * specific file chooser for saving or opening different files.
 *
 * @author Rolf Hilker
 */
public abstract class ReadXplorerFileChooser extends JFileChooser {

    private static final Logger LOG = Logger.getLogger( ReadXplorerFileChooser.class.getName() );

    private static final long serialVersionUID = 1L;

    private Preferences pref;
    private String directoryProperty;
    private String currentDirectory;

    protected Object data;


    /**
     * Creates a new readxplorer file chooser.
     *
     * @param fileExtensions The file extensions to use. If set to null or the
     * first entry is the empty string, no file filter is set
     * @param fileDescription Description for the files in the file filter
     */
    public ReadXplorerFileChooser( final String[] fileExtensions, String fileDescription ) {
        this( fileExtensions, fileDescription, null );
    }


    /**
     * Creates a new readxplorer file chooser.
     *
     * @param fileExtensions The file extensions to use. If set to null or the
     * first entry is the empty string, no file filter is set
     * @param fileDescription Description for the files in the file filter
     * @param data The data which might be used for file choosers storing data
     */
    public ReadXplorerFileChooser( final String[] fileExtensions, String fileDescription, final Object data ) {
        this.data = data;
        if( fileExtensions != null && !fileExtensions[0].isEmpty() ) {
            setFileFilter( new FileNameExtensionFilter( fileDescription, fileExtensions ) );
        }
        pref = NbPreferences.forModule( Object.class );
        directoryProperty = Paths.READXPLORER_FILECHOOSER_DIRECTORY;
    }


    /**
     * Opens a file chooser for input or output file selection/creation.
     *
     * @param option the option: readxplorerFileChooser.OPEN_DIALOG for file
     * selection and readxplorerFileChooser.SAVE_DIALOG for storing a file.
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
        } catch( final IOException exception ) {
            this.setCurrentDirectory( null );
        }
        int result;
        if( option == ReadXplorerFileChooser.OPEN_DIALOG ) {
            result = this.showOpenDialog( this.getParent() );
        } else {
            if( option == ReadXplorerFileChooser.CUSTOM_DIALOG ) {
                result = this.showDialog( this.getParent(), "Select" );
            } else {
                result = this.showSaveDialog( null );
            }
        }
        ///////////////// store directory ////////////////////////////////////
        try {
            currentDirectory = this.getCurrentDirectory().getCanonicalPath();
            pref.put( directoryProperty, currentDirectory );
            pref.flush();
        } catch( BackingStoreException e ) {
            LOG.log( SEVERE, null, e );
        } catch( IOException ioe ) {
            LOG.fine( ioe.getMessage() );
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
            open( fileLocation );
        } else if( option == ReadXplorerFileChooser.SAVE_DIALOG ) {
            fileLocation = ReadXplorerFileChooser.getSelectedFileWithExtension( this ).getAbsolutePath();
            boolean done = checkFileExists( fileLocation, this );
            if( !done ) {
                save( fileLocation );
            }
        }
        if( option == ReadXplorerFileChooser.CUSTOM_DIALOG ) {
            File file = new File( fileLocation );
            if( file.exists() ) {
                open( fileLocation );
            } else {
                save( fileLocation );
            }
        }
    }


    /**
     * When a file should be saved this method checks if the file already exists
     * and prompts for replacement. If it doesn't exist yet, it is created.
     *
     * @param fileLocation the file location to store the file
     * @param jfc the JFileChooser
     */
    @NbBundle.Messages({"ReadxplorerFileChooser_FileExists=File already exists. Do you want to overwrite the existing file?",
        "ReadXplorerFileChooser_Dialog=Overwrite File Dialog"})
    private boolean checkFileExists( final String fileLocation, final JFileChooser jfc ) {
        File file = new File( fileLocation );
        if( file.exists() ) {
            final int overwriteFile = JOptionPane.showConfirmDialog( jfc, Bundle.ReadxplorerFileChooser_FileExists(), Bundle.ReadXplorerFileChooser_Dialog(), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE );
            if( overwriteFile == JOptionPane.YES_OPTION ) {
                save( fileLocation );
            } else {
                openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
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
     * chooser
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
        } else { //if no appropriate filter is currently selected, the first extensions of the first appropriate extension filter is appended to the file name
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
