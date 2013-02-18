package de.cebitec.vamp.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

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
}
