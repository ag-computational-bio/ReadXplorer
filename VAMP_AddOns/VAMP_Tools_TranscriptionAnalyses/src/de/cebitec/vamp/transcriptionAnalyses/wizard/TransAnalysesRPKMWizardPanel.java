/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.transcriptionAnalyses.wizard;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 *
 * @author Martin Tötsches
 */
public class TransAnalysesRPKMWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    private TransAnalysesRPKMVisualPanel component;
    private ChangeSupport changeSupport;
    private boolean isValidated = true;

    /**
     * Panel for showing and handling all available options for the operon
     * detection.
     */
    public TransAnalysesRPKMWizardPanel() {
        this.changeSupport = new ChangeSupport(this);
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new TransAnalysesRPKMVisualPanel();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    public void readSettings(final WizardDescriptor wiz) {
        component.addPropertyChangeListener(TranscriptionAnalysesWizardIterator.PROP_TSS_ANALYSIS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                isValidated = (boolean) evt.getNewValue();
                if (isValidated) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                } else {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, "Please enter valid parameters (only positive numbers are allowed)");
                }
                changeSupport.fireChange();
            }
        });
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_NUMBER_READS, this.component.getMinNumberOfReads());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_NUMBER_READS, this.component.getMaxNumberOfReads());
        }
    }

    @Override
    public boolean isValid() {
        return this.isValidated;
    }

    @Override
    public void addChangeListener(ChangeListener cl) {
        this.changeSupport.addChangeListener(cl);
    }

    @Override
    public void removeChangeListener(ChangeListener cl) {
        this.changeSupport.removeChangeListener(cl);
    }
    
}
