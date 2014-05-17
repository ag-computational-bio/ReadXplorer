/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.differentialExpression.wizard;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.differentialExpression.DeAnalysisHandler.Tool;
import de.cebitec.readXplorer.util.FeatureType;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;

public class GeneralSettingsWizardPanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private GeneralSettingsVisualPanel component;
    private Integer genomeID;
    private Tool tool;

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public GeneralSettingsVisualPanel getComponent() {
        if (component == null) {
            component = new GeneralSettingsVisualPanel();
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
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return true;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    @Override
    public void readSettings(WizardDescriptor wiz) {
        genomeID = (Integer) wiz.getProperty("genomeID");
        tool = (Tool) wiz.getProperty("tool");
        if (tool == Tool.ExpressTest) {
            getComponent().enableSaveOptions(false);
        } else {
            getComponent().enableSaveOptions(true);
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        if (getComponent().verifyInput()) {
            wiz.putProperty("startOffset", getComponent().getStartOffset());
            wiz.putProperty("stopOffset", getComponent().getStopOffset());
            wiz.putProperty("regardReadOrientation", getComponent().regaredReadOrientation());
        }
        if (getComponent().isSaveBoxChecked()) {
            //TODO: Input validation
            String path = getComponent().getSavePath();
            File file = new File(path);
            wiz.putProperty("saveFile", file);
        }

        List<FeatureType> usedFeatures = getComponent().getSelectedFeatureTypes();
        //If all possible features are selected, we use the ANY feature type
        if (usedFeatures.size() == FeatureType.SELECTABLE_FEATURE_TYPES.length) {
            usedFeatures = new ArrayList<>();
            usedFeatures.add(FeatureType.ANY);
        }
        wiz.putProperty("featureType", new HashSet<>(usedFeatures));
    }

    @Override
    public void validate() throws WizardValidationException {
        if (!getComponent().verifyInput()) {
            throw new WizardValidationException(null, "Please enter a number greater or equal to zero as start/stop offset.", null);
        }
        List<FeatureType> usedFeatures = getComponent().getSelectedFeatureTypes();
        if (usedFeatures.isEmpty()) {
            throw new WizardValidationException(null, "Please select at least one type of annotation.", null);
        } else {
            if (usedFeatures.size() < FeatureType.SELECTABLE_FEATURE_TYPES.length) {
                ReferenceConnector referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector(genomeID);
                if (!referenceConnector.hasFeatures(usedFeatures)) {
                    throw new WizardValidationException(null, "The selected reference genome does not contain annotations of the selected type(s).", null);
                }
            }
        }
    }
}
