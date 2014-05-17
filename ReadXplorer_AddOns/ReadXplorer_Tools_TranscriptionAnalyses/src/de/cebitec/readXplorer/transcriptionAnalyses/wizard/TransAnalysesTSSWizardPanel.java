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
package de.cebitec.readXplorer.transcriptionAnalyses.wizard;

import de.cebitec.readXplorer.view.dialogMenus.ChangeListeningWizardPanel;
import org.openide.WizardDescriptor;

/**
 * Panel for showing and handling all available options for the transcription
 * start site detection.
 * 
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TransAnalysesTSSWizardPanel extends ChangeListeningWizardPanel {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private TransAnalysesTSSVisualPanel component;

    /**
     * Panel for showing and handling all available options for the
     * transcription start site detection.
     */
    public TransAnalysesTSSWizardPanel() {
        super("Please enter valid parameters (only positive numbers are allowed)");
    }

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public TransAnalysesTSSVisualPanel getComponent() {
        if (component == null) {
            component = new TransAnalysesTSSVisualPanel();
        }
        return component;
    }

    @Override
    public void storeSettings(WizardDescriptor wiz) {
        if (this.isValid()) {
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_AUTO_TSS_PARAMS, this.component.isTssAutomatic());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_TOTAL_INCREASE, this.component.getMinTotalIncrease());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_PERCENT_INCREASE, this.component.getMinTotalPercentIncrease());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_LOW_COV_INIT_COUNT, this.component.getMaxLowCovInitialCount());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_LOW_COV_INC, this.component.getMinLowCovIncrease());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_UNANNOTATED_TRANSCRIPT_DET, this.component.getDetectUnannotatedTranscripts());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MIN_TRANSCRIPT_EXTENSION_COV, this.component.getMinTranscriptExtensionCov());
            wiz.putProperty(TranscriptionAnalysesWizardIterator.PROP_MAX_LEADERLESS_DISTANCE, this.component.getMaxLeaderlessDistance());
        }
    }
}
