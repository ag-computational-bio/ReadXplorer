/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.mapping.api;

import de.cebitec.vamp.mapping.MappingProcessor;
import de.cebitec.vamp.util.CommandLineUtils;
import de.cebitec.vamp.util.FileUtils;
import de.cebitec.vamp.util.Properties;
import de.cebitec.vamp.util.SimpleOutput;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 * MappingAPI provides functions that can be accessed from outside this package
 * to use mapping fuctions
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class MappingApi {
    
    /**
     * Load the path to the mapper script from Netbeans preferences.
     * If there is no preference available, try to use the Mapper-Script from 
     * the release-Directory, which will be available in the installation directory
     * @return the full path to the mapping script
     */
    public static String getMapperPath() {
        String path = NbPreferences.forModule(Object.class).get(Properties.MAPPER_PATH, "");
        
        //try to locate bwa_mapper.sh, if we are not on windows (bwa_mapper.sh works only on unix systems)
        if ((path.equals("")) && (!(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0))  ) {
            File result = InstalledFileLocator.getDefault().locate("Mapper/bwa_mapper.sh", "VAMP_Mapping", false);
            if (result!=null) {
                //try to set executable permission, if we are the owner
                result.setExecutable(true, false);
                
                path = result.getAbsolutePath();
            }
        }
        
        return path;
    }
    
    /**
     * get last mapping params
     * @return 
     */
    public static String getLastMappingParams() {
        return NbPreferences.forModule(Object.class).get(Properties.MAPPER_PARAMS, "");
    }
    
    /**
     * set last mapping params
     * @param params 
     */
    public static void setLastMappingParams(String params) { 
        NbPreferences.forModule(Object.class).put(Properties.MAPPER_PARAMS, params);
    }
    
    /**
     * start a mapping of a fasta file 
     * @param out a SimpleOutput to be used for the output of the mapping script
     * @param reference the reference sequence
     * @param fasta the reads in fasta format
     * @param mappingParameters additional mapping paramters
     * @return
     * @throws IOException 
     */
    public static String mapFastaFile(SimpleOutput out, String reference, String fasta, String mappingParameters) throws IOException {     
        if (MappingApi.checkMapperConfig()) {
            //remember mapping params for future executions
            setLastMappingParams(mappingParameters);
            
            ProgressHandle ph = ProgressHandleFactory.createHandle(
                    NbBundle.getMessage(MappingProcessor.class, "MSG_MappingApi.mapFastaFile.Start", "mapFastaFile"));
            ph.start();

            String basename = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(fasta);
            File fastafile = new File(basename);
            basename = fastafile.getName();
            new CommandLineUtils(out).runCommandAndWaitUntilEnded(MappingApi.getMapperPath(), reference, fasta, basename, mappingParameters);

            ph.finish();
            return fastafile.getAbsolutePath()+".sam";
        }
        else {
            throw new RuntimeException("Can not continue due to previous errors!");
        }
    }
    
    /**
     * check that the mapping script is configured properly
     * @return true, if everything is fine
     * @throws a message dialog, if a problem in the config is detected
     */
    public static boolean checkMapperConfig() {
        if (!FileUtils.fileExistsAndIsExecutable(MappingApi.getMapperPath())) {
            if (!FileUtils.fileExists(MappingApi.getMapperPath())) {
                JOptionPane.showMessageDialog( null, 
                        "Please check your mapper configuration and provide a correct path to a mapping script!", 
                        "Warning", JOptionPane.INFORMATION_MESSAGE);
                return false;
            } else {
                JOptionPane.showMessageDialog( null, 
                        "Please check your mapper configuration and permissions on the mapping script!\n"
                        +"For Unix systems: try executing chmod u+rx,g+rx,a+rx bwa_mapper.sh to set execution permissions!", 
                        "Warning", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }   
        else return true;
        
    }
}
