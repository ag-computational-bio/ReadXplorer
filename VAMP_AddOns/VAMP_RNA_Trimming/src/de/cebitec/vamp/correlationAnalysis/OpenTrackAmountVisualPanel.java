package de.cebitec.vamp.correlationAnalysis;

import de.cebitec.vamp.view.dialogMenus.ChangeListeningWizardPanel;
import de.cebitec.vamp.view.dialogMenus.OpenTracksVisualPanel;

/**
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class OpenTrackAmountVisualPanel extends OpenTracksVisualPanel {
    private static final long serialVersionUID = 1L;
    
    private Integer selectedAmount;
    private final TrackListPanel parent;

    public OpenTrackAmountVisualPanel(int referenceID, TrackListPanel parent) {
        super(referenceID);
        this.parent = parent;
        this.selectedAmount = -1;
    }
    
    @Override
    public boolean isRequiredInfoSet() {
        boolean isRequiredInfoSet = this.getSelectAmount() > -1 && 
                                    this.getSelectAmount() == this.getSelectedTracks().size();
        if (!isRequiredInfoSet) {
            this.parent.setErrorMsg("Please select " + this.getSelectAmount() + " tracks! (You selected "
                    + this.getAllMarkedNodes().size() + ")");
        }
        firePropertyChange(ChangeListeningWizardPanel.PROP_VALIDATE, null, isRequiredInfoSet);
        return isRequiredInfoSet;
    }

    /**
     * @return the maximumAmount
     */
    public int getSelectAmount() {
        return selectedAmount;
    }

    /**
     * @param maximumAmount the maximumAmount to set
     */
    public void setSelectAmount(Integer maximumAmount) {
        this.selectedAmount = maximumAmount;
    }
    
}
