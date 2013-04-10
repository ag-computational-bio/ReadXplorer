package de.cebitec.vamp.ui.converter;

import de.cebitec.vamp.parser.output.ConverterI;
import de.cebitec.vamp.util.Observer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * Thread carrying out the conversion of one file into another format.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ConvertThread extends Thread implements Observer {
    
    private final InputOutput io;
    private final ConverterI converter;
    private final ProgressHandle progressHandle;
    
    /**
     * Thread carrying out the conversion of one file into another format.
     */
    public ConvertThread(ConverterI converter) {
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ConvertThread.class, "ConvertThread.output.name"), false);
        this.converter = converter;
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ConvertThread.class, "ConvertThread.progress.name"));
        this.progressHandle.start();
        
    }
    
    @Override
    public void run() {
        try {
            converter.convert();
            this.progressHandle.finish();
        } catch (Exception ex) {
            this.io.getOut().println(ex.toString());
        }
    }

    @Override
    public void update(Object data) {
        if (data instanceof String && ((String) data).contains("...")) {
            this.progressHandle.progress(String.valueOf(data));
        } else {
            this.io.getOut().println(data.toString());
            System.out.println(data.toString());
        }
    }
    
}
