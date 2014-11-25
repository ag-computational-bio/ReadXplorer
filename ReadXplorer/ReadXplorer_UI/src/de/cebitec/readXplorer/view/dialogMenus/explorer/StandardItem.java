package de.cebitec.readXplorer.view.dialogMenus.explorer;

/**
 * A standard item to use for explorers. It knows, whether this item is 
 * selected.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class StandardItem implements ItemI {

    private Boolean selected;
    
    /**
     * A standard item to use for explorers. It knows, whether this item is
     * selected.
     */
    public StandardItem() {
        this.selected = false;
    }
    
    /**
     * @return <cc>true</cc>, if this item is selected, <cc>false</cc> otherwise
     */
    public Boolean getSelected() {
        return selected;
    }

    /**
     * @param selected <cc>true</cc>, if this item is selected, <cc>false</cc>
     * otherwise
     */
    public void setSelected(Boolean selected) {
        this.selected = selected;
    }
    
    
}
