package de.cebitec.readXplorer.mapping;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

/**
 * MapperOptionsPanelController is an options panel to select the mapping script. 
 * The mapping script is a bash script that encapsulates the mapper 
 * functionality (which is often divided into multiple commands) 
 * into a single script.
 * 
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
@OptionsPanelController.TopLevelRegistration( 
        categoryName = "#OptionsCategory_Name_Mapper", 
        iconBase = "de/cebitec/readXplorer/mapping/mapper.png",
        keywords = "#AdvancedOption_Keywords_Mapper",
        keywordsCategory = "General/Mapper")
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Mapper=Mapper", "AdvancedOption_Keywords_Mapper=Mapper Mapping "})
public final class MapperOptionsPanelController extends OptionsPanelController {

    private MapperPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    public void update() {
        getPanel().load();
        changed = false;
    }

    public void applyChanges() {
        getPanel().store();
        changed = false;
    }

    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    public boolean isValid() {
        return getPanel().valid();
    }

    public boolean isChanged() {
        return changed;
    }

    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    public JComponent getComponent(Lookup masterLookup) {
        return getPanel();
    }

    public void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    private MapperPanel getPanel() {
        if (panel == null) {
            panel = new MapperPanel(this);
        }
        return panel;
    }

    void changed() {
        if (!changed) {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
