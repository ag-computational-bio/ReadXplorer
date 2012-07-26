package de.cebitec.vamp.ui.converter;

import de.cebitec.vamp.parser.output.ConverterI;
import de.cebitec.vamp.parser.output.JokToBamConverter;
import de.cebitec.vamp.util.Observer;
import java.io.File;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 * 
 * Thread carrying out the conversion of one file into another format.
 */
public class ConvertThread extends Thread implements Observer {
    
    private final InputOutput io;
    private final String filePath;
    private final String converterType;
    private final String referenceName;
    private final int referenceLength;
    private final ProgressHandle progressHandle;
    
    /**
     * Thread carrying out the conversion of one file into another format.
     */
    public ConvertThread(String filePath, String converterType, 
            String referenceName,  int referenceLength) {
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ConvertThread.class, "ConvertThread.output.name"), false);
        this.filePath = filePath;
        this.converterType = converterType;
        this.referenceName = referenceName;
        this.referenceLength = referenceLength;
        
        this.progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(ConvertThread.class, "ConvertThread.progress.name"));
        this.progressHandle.start();
        
    }
    
    @Override
    public void run() {
        ConverterI converter = this.getConverter();
        try {
            converter.convert();
            this.progressHandle.finish();
        } catch (Exception ex) {
            this.io.getOut().println(ex.toString());
        }
    }

    /**
     * @return the converter for the given data or null, if no converter could be assigned.
     */
    private ConverterI getConverter() {
        ConverterI converter = null;
        if (converterType.equals(NbBundle.getMessage(ConvertThread.class, "ConverterSetupCard.JokToBamConverter"))) {
            JokToBamConverter jokConverter = new JokToBamConverter();
            jokConverter.registerObserver(this);
            jokConverter.setDataToConvert(new File(filePath), referenceName, referenceLength);
            converter = jokConverter;
        }
        return converter;
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
