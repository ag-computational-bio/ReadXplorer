package de.cebitec.readXplorer.util;

import org.openide.windows.InputOutput;

/**
 * This class implements the SimpleOutput interface for the usage 
 * with Netbeans' InputOutput-Class
 * @author Evgeny Anisiforov
 */
public class SimpleIO implements SimpleOutput {
    
    private InputOutput io;
    
    public SimpleIO(InputOutput io) {
        this.io = io;
    }
    
    @Override
    public void showMessage(String s) {
        this.io.getOut().println(s);
    }

    @Override
    public void showError(String s) {
        this.io.getErr().println(s);
    }
    
}
