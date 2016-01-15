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

package de.cebitec.readxplorer.utils;


import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utils for work with files and directories
 * <p>
 * @author Evgeny Anisiforov
 */
public final class FileUtils {


    private static final Logger LOG = LoggerFactory.getLogger( FileUtils.class.getName() );


    private FileUtils() {
    }


    /**
     * extract the whole path part without extension
     * <p>
     * @param file the file
     * <p>
     * @return file path witout extension
     */
    public static String getFilePathWithoutExtension( File file ) {
        return getFilePathWithoutExtension( file.getAbsolutePath() );
    }


    /**
     * extract the whole path part without extension
     * <p>
     * @param filePath the full absolute path to the file
     * <p>
     * @return file path witout extension
     */
    public static String getFilePathWithoutExtension( String filePath ) {
        String[] nameParts = filePath.split( "\\." );
        String newFileName = nameParts[0];
        for( int i = 1; i < (nameParts.length - 1); i++ ) {
            newFileName += "." + nameParts[i];
        }

        return newFileName;
    }


    /**
     * count lines in a file equivalent of wc -l in unix
     * <p>
     * @param file <p>
     * @return number of lines or 0 if an error occurred during reading
     */
    public static int countLinesInFile( File file ) {
        int lines = 0;
        try( BufferedReader reader = new BufferedReader( new FileReader( file ) ) ) {
            while( reader.readLine() != null ) {
                lines++;
            }
        } catch( IOException ioe ) {
            lines = 0;
        }
        return lines;
    }


    public static int countLinesInFile( String filepath ) {
        return countLinesInFile( new File( filepath ) );
    }


    /**
     * displays a file open dialog and copies the result to an edit field
     * <p>
     * @param prefName
     * @param fileNameExtensionFilter
     * @param textField
     * @param forClass
     * @param parent                  <p>
     * @return
     */
    public static File showFileOpenDialogAndChangePrefs( String prefName, FileNameExtensionFilter fileNameExtensionFilter,
                                                         JTextField textField, Class<?> forClass, Component parent ) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter( fileNameExtensionFilter );
        Preferences prefs2 = Preferences.userNodeForPackage( forClass );
        String path = prefs2.get( prefName, null );
        if( path != null ) {
            fc.setCurrentDirectory( new File( path ) );
        }
        int result = fc.showOpenDialog( parent );

        if( result == 0 ) {
            // file chosen
            File file = fc.getSelectedFile();

            if( file.canRead() ) {
                Preferences prefs = Preferences.userNodeForPackage( forClass );
                prefs.put( prefName, file.getAbsolutePath() );
                textField.setText( file.getAbsolutePath() );
                try {
                    prefs.flush();
                } catch( BackingStoreException ex ) {
                    LOG.error( null, ex );
                }
                return file;
            } else {
                LOG.warn( "Could not read file {0}", file.getAbsolutePath() );
            }
        }
        return null;
    }


    /**
     * check that the given path exists
     * <p>
     * @param filePathString <p>
     * @return boolean true if the file exists
     */
    public static boolean fileExists( String filePathString ) {
        File f = new File( filePathString );
        boolean result = f.exists();
        return result;
    }


    /**
     * check that the given path exists and is readable.
     * <p>
     * @param filePathString <p>
     * @return boolean true if the file exists, false otherwise
     */
    public static boolean fileExistsAndIsReadable( String filePathString ) {
        File f = new File( filePathString );
        boolean result = f.exists() && f.canRead();
        return result;
    }


    /**
     * check that the given path exists, is readable and can be executed
     * <p>
     * @param filePathString <p>
     * @return boolean true if the file can be executed
     */
    public static boolean fileExistsAndIsExecutable( String filePathString ) {
        File f = new File( filePathString );
        boolean result = f.exists() && f.canRead() && f.canExecute();
        return result;
    }


}
