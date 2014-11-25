package de.cebitec.vamp.mapping;

import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.vamp.mapping.api.MappingApi;
import de.cebitec.vamp.util.SimpleIO;
import java.io.IOException;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * MappingProcessor allows map a fasta file to a reference sequence
 * by using an external mapping script
 * The user will see a progress info.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class MappingProcessor  {
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private final static Logger LOG = Logger.getLogger(MappingProcessor.class.getName());
    private RequestProcessor.Task theTask = null;
    private InputOutput io;
    private String sourcePath;
    
    /**
     * If any message should be printed to the console, this method is used.
     * If an error occured during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * @param msg the msg to print
     */
    private void showMsg(String msg) {
        this.io.getOut().println(msg);
    }
    
    public MappingProcessor(final String referencePath, final String sourcePath, final String mappingParam) {
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(MappingProcessor.class, "MappingProcessor.output.name"), true);
        this.io.setOutputVisible(true);
        this.io.getOut().println("");
        this.sourcePath = sourcePath;
        
        CentralLookup.getDefault().add(this);
        try {
            io.getOut().reset();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        io.select();
        
        Runnable runnable = new Runnable() {
            private int currentPosition = 1;
            private int steps;
            private int currentStep = 0;
            private boolean wasCanceled = false;
            private boolean ready = false;
            
            
            @Override
            public void run() {
                String sam = null;
                String extractedSam = null;
                try {
                    sam = MappingApi.mapFastaFile(new SimpleIO(io), referencePath, sourcePath, mappingParam);
                    showMsg("Extraction ready!");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                
            }

        };
        
        theTask = RP.create(runnable); //the task is not started yet
        theTask.schedule(1*1000); //start the task with a delay of 1 seconds
    }
}
