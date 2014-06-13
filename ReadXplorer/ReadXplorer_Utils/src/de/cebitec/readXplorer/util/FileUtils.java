/* 
 * Copyright (C) 2014 Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
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
package de.cebitec.readXplorer.util;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Utils for work with files and directories
 * @author Evgeny Anisiforov
 */
public class FileUtils {
    
    private FileUtils() {
    }
    
    /**
     * extract the whole path part without extension  
     * @param file the file 
     * @return file path witout extension
     */
    public static String getFilePathWithoutExtension(File file) {
        return getFilePathWithoutExtension(file.getAbsolutePath());
    } 
    
    /**
     * extract the whole path part without extension  
     * @param filePath the full absolute path to the file
     * @return file path witout extension
     */
    public static String getFilePathWithoutExtension(String filePath) {
        String[] nameParts = filePath.split("\\.");
        String newFileName = nameParts[0];
        for (int i = 1; i < (nameParts.length - 1); i++) {
            newFileName += "." + nameParts[i];
        }

        return newFileName;
    } 
    
    /**
     * count lines in a file 
     * equivalent of wc -l in unix
     * @param file
     * @return number of lines or 0 if an error occured during reading
     */
    public static int countLinesInFile(File file) {
        int lines = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            while (reader.readLine() != null) {
                lines++;
            }
        } catch (Exception e) {
            lines = 0;
        }
        return lines;
    }
    
    public static int countLinesInFile(String filepath) {
        return countLinesInFile(new File(filepath));
    }
    
    /**
     * displays a file open dialog and copies the result to an edit field
     * @param prefName
     * @param fileNameExtensionFilter
     * @param textField
     * @param forClass
     * @param parent
     * @return 
     */
    public static File showFileOpenDialogAndChangePrefs(String prefName, FileNameExtensionFilter fileNameExtensionFilter,
            JTextField textField, Class forClass, Component parent) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(fileNameExtensionFilter);
        Preferences prefs2 = Preferences.userNodeForPackage(forClass);
        String path = prefs2.get(prefName, null);
        if (path != null) {
            fc.setCurrentDirectory(new File(path));
        }
        int result = fc.showOpenDialog(parent);

        if (result == 0) {
            // file chosen
            File file = fc.getSelectedFile();

            if (file.canRead()) {
                Preferences prefs = Preferences.userNodeForPackage(forClass);
                prefs.put(prefName, file.getAbsolutePath());
                textField.setText(file.getAbsolutePath());
                try {
                    prefs.flush();
                } catch (BackingStoreException ex) {
                    Logger.getLogger(forClass.getName()).log(Level.SEVERE, null, ex);
                }
                return file;
            } else {
                Logger.getLogger(forClass.getName()).log(Level.WARNING, "Could not read file");
            }
        }
        return null;
    }
    
    /**
     * check that the given path exists
     * @param filePathString
     * @return boolean true if the file exists
     */
    public static boolean fileExists(String filePathString) {
        File f = new File(filePathString);
        boolean result = f.exists();
        return result;
    }
    
    /**
     * check that the given path exists, is readable and can be executed
     * @param filePathString
     * @return boolean true if the file can be executed
     */
    public static boolean fileExistsAndIsExecutable(String filePathString) {
        File f = new File(filePathString);
        boolean result = f.exists() && f.canRead() && f.canExecute();
        return result;
    }
}
