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
        for(int i=1; i<(nameParts.length-1); i++) {
            newFileName += "." + nameParts[i];
        }
        
        return newFileName;
    } 
    
    /**
     * count lines in a file 
     * equivalent of wc -l in unix
     * @param file
     * @return number of lines
     */
    public static int countLinesInFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(file));
            int lines = 0;
            while (reader.readLine() != null) lines++;
            reader.close();
            return lines;
        } catch(Exception e) {
            return 0;
        }
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
        if(path!=null){
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
