/* 
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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
package de.cebitec.readXplorer.transcriptionAnalyses.wizard;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Wizard panel allowing for selection of the transcription analyses, which
 * should be carried out and whose parameters have to be adjusted in the next
 * steps.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesSelectionWizardPanel extends ChangeListeningWizardPanel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesSelectionVisualPanel component;

    /**
     * Wizard panel allowing for selection of the transcription analyses, which
     * should be carried out and whose parameters have to be adjusted in the
     * next steps.
     */
    public TransAnalysesSelectionWizardPanel() {
        super("Please select at least one transcription analysis to continue!");
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesSelectionVisualPanel getComponent() {
        if (component == null) {
            component = new TransAnalysesSelectionVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        // use wiz.putProperty to remember current panel state
        if (this.isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_TSS_ANALYSIS, this.component.isTSSAnalysisSelected());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_OPERON_ANALYSIS, this.component.isOperonAnalysisSelected());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_RPKM_ANALYSIS, this.component.isRPKMAnalysisSelected());
            //changeSupport.fireChange();
            this.readSettings(wiz);
        }
    }
}
