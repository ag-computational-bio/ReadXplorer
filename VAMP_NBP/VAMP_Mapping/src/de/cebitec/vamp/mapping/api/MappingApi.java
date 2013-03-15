/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.mapping.api;

import de.cebitec.vamp.mapping.MappingProcessor;
import de.cebitec.vamp.util.CommandLineUtils;
import de.cebitec.vamp.util.Properties;
import java.io.File;
import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.windows.InputOutput;

/**
 * MappingAPI provides functions that can be accessed from outside this package
 * to use mapping fuctions
 * @author Evgeny Anisiforov
 */
public class MappingApi {
        public static String mapFastaFile(InputOutput io, String reference, String fasta) throws IOException {     
        ProgressHandle ph = ProgressHandleFactory.createHandle(
                NbBundle.getMessage(MappingProcessor.class, "MSG_MappingApi.mapFastaFile.Start", "mapFastaFile"));
        ph.start();
        
        String basename = de.cebitec.vamp.util.FileUtils.getFilePathWithoutExtension(fasta);
        File fastafile = new File(basename);
        basename = fastafile.getName();
        new CommandLineUtils(io).runCommandAndWaitUntilEnded(NbPreferences.forModule(Object.class).get(Properties.MAPPER_PATH, "/dev/null"), reference, fasta, basename);
        
        ph.finish();
        return fastafile.getAbsolutePath()+".sam";
    }
}
