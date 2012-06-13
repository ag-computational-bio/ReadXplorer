package de.cebitec.vamp.differentialExpression;

/**
 *
 * @author kstaderm
 */
public interface IprogressMonitor {
    
    public void writeLineToConsole(String line);
    
    public void setProgress(int n);
    
}
