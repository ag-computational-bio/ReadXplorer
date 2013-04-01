/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.view.dialogMenus.OpenTrackPanelList;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * TrackListPanel displays a list of tracks for a certain reference
 */
public class TrackListPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    public static final String PROP_VALIDATE = "validate";
    
    private ChangeSupport changeSupport;
    
    private boolean isValidated = true;
    private int referenceId;
    private Integer selectAmount;
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private OpenTrackPanelList component;
    

    /**
     * The SNP detection wizard main panel.
     * @param referenceId reference id 
     */
    public TrackListPanel(int referenceId) {
        this.changeSupport = new ChangeSupport(this);
        this.setReferenceId(referenceId);
    }
    
    
    
    

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public OpenTrackPanelList getComponent() {
        if (component == null) {
            component = new OpenTrackPanelList(this.getReferenceId());
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }

    @Override
    public boolean isFinishPanel() {
        return this.isValidated;
    }

    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return isValidated;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public void readSettings(final WizardDescriptor wiz) {
        //final Integer amount = this.getMaximumAmount();
        component.addPropertyChangeListener(OpenTrackPanelList.PROP_SELECTED_ITEMS, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ((getSelectAmount()!=null) && (component.getSelectedTracks().size()!=getSelectAmount().intValue())) {
                    isValidated = false;
                }
                else {
                    isValidated = true;
                }
                //isValidated = (boolean) evt.getNewValue();
                if (isValidated) {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, null);
                } else {
                    wiz.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, 
                    "Please select "+getSelectAmount()+" tracks! (You selected "
                            +component.getSelectedTracks().size()+")");
                }
                changeSupport.fireChange();
            }
        });
        
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (isFinishPanel()) {
            wiz.putProperty(CorrelationAnalysisAction.PROP_SELECTED_TRACKS, this.component.getSelectedTracks());
        }
    }

    /**
     * @return the referenceId
     */
    public int getReferenceId() {
        return referenceId;
    }

    /**
     * @param referenceId the referenceId to set
     */
    private void setReferenceId(int referenceId) {
        this.referenceId = referenceId;
    }

    /**
     * @return the maximumAmount
     */
    public Integer getSelectAmount() {
        return selectAmount;
    }

    /**
     * @param maximumAmount the maximumAmount to set
     */
    public void setSelectAmount(Integer maximumAmount) {
        this.selectAmount = maximumAmount;
    }
}
