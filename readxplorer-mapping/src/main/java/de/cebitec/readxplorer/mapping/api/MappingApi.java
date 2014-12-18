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

package de.cebitec.readxplorer.mapping.api;


import de.cebitec.readxplorer.utils.CommandLineUtils;
import de.cebitec.readxplorer.utils.FileUtils;
import de.cebitec.readxplorer.utils.Properties;
import de.cebitec.readxplorer.utils.SimpleOutput;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;


/**
 * MappingAPI provides functions that can be accessed from outside this package
 * to use mapping fuctions.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class MappingApi {

    /**
     * Utility class.
     */
    private MappingApi() {
    }


    /**
     * Load the path to the mapper script from Netbeans preferences.
     * If there is no preference available, try to use the Mapper-Script from
     * the release-Directory, which will be available in the installation
     * directory
     * <p>
     * @return the full path to the mapping script
     */
    public static String getMapperPath() {
        String path = NbPreferences.forModule( Object.class ).get( Properties.MAPPER_PATH, "" );

        //try to locate bwa_mapper.sh, if we are not on windows (bwa_mapper.sh works only on unix systems)
        if( (path.isEmpty()) && (!System.getProperty( "os.name" ).toLowerCase().contains( "win" )) ) {
            File result = InstalledFileLocator.getDefault().locate( "Mapper/bwa_mapper.sh", "ReadXplorer_Mapping", false );
            if( result != null ) {
                //try to set executable permission, if we are the owner
                result.setExecutable( true, false );

                path = result.getAbsolutePath();
            }
        }

        return path;
    }


    /**
     * get last mapping params
     * <p>
     * @return
     */
    public static String getLastMappingParams() {
        return NbPreferences.forModule( Object.class ).get( Properties.MAPPER_PARAMS, "" );
    }


    /**
     * set last mapping params
     * <p>
     * @param params
     */
    public static void setLastMappingParams( String params ) {
        NbPreferences.forModule( Object.class ).put( Properties.MAPPER_PARAMS, params );
    }


    /**
     * start a mapping of a fasta file
     * <p>
     * @param out               a SimpleOutput to be used for the output of the
     *                          mapping script
     * @param reference         the reference sequence
     * @param fasta             the reads in fasta format
     * @param mappingParameters additional mapping paramters
     * <p>
     * @return
     *         <p>
     * @throws IOException
     */
    @Messages( { "MSG_MappingApi.mapFastaFile.Start=Map sequencing reads with external mapper" } )
    public static String mapFastaFile( SimpleOutput out, String reference, String fasta, String mappingParameters ) throws IOException {
        if( MappingApi.checkMapperConfig() ) {
            //remember mapping params for future executions
            setLastMappingParams( mappingParameters );

            ProgressHandle ph = ProgressHandleFactory.createHandle( Bundle.MSG_MappingApi_mapFastaFile_Start() );
            ph.start();

            String basename = de.cebitec.readxplorer.utils.FileUtils.getFilePathWithoutExtension( fasta );
            File fastafile = new File( basename );
            basename = fastafile.getName();
            new CommandLineUtils( out ).runCommandAndWaitUntilEnded( MappingApi.getMapperPath(), reference, fasta, basename, mappingParameters );

            ph.finish();
            return fastafile.getAbsolutePath() + ".sam";
        }
        else {
            throw new RuntimeException( "Can not continue due to previous errors!" );
        }
    }


    /**
     * check that the mapping script is configured properly
     * <p>
     * @return true, if everything is fine
     * <p>
     * @throws a message dialog, if a problem in the config is detected
     */
    public static boolean checkMapperConfig() {
        if( !FileUtils.fileExistsAndIsExecutable( MappingApi.getMapperPath() ) ) {
            if( !FileUtils.fileExists( MappingApi.getMapperPath() ) ) {
                JOptionPane.showMessageDialog( null,
                   "Please check your mapper configuration and provide a correct path to a mapping script!",
                   "Warning", JOptionPane.INFORMATION_MESSAGE );
                return false;
            }
            else {
                JOptionPane.showMessageDialog( null,
                   "Please check your mapper configuration and permissions on the mapping script!\n"
                   + "For Unix systems: try executing chmod u+rx,g+rx,a+rx bwa_mapper.sh to set execution permissions!",
                   "Warning", JOptionPane.INFORMATION_MESSAGE );
                return false;
            }
        }
        else {
            return true;
        }

    }


}
