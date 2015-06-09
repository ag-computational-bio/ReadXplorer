/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.wizard;


import de.cebitec.readxplorer.api.enums.FeatureType;
import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.DeAnalysisHandler.Tool;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;


public class SelectTrackWizardPanel implements
        WizardDescriptor.ValidatingPanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectTrackVisualPanel component;
    private Tool tool;


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public SelectTrackVisualPanel getComponent() {
        if( component == null ) {
            component = new SelectTrackVisualPanel();
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
    public void addChangeListener( ChangeListener l ) {
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
    }


    @Override
    public void readSettings( WizardDescriptor wiz ) {
        tool = (Tool) wiz.getProperty( "tool" );
    }


    @Override
    public void storeSettings( WizardDescriptor wiz ) {
        // use wiz.putProperty to remember current panel state
        if( getComponent().selectionFinished() || tool == Tool.ExportCountTable ) {
            wiz.putProperty( "genomeID", getComponent().getSelectedReferenceGenomeID() );
            wiz.putProperty( "tracks", getComponent().getSelectedTracks() );
        }
    }


    @Override
    public void validate() throws WizardValidationException {
        if( !getComponent().selectionFinished() && tool != Tool.ExportCountTable ) {
            throw new WizardValidationException( null, "Please select a reference genome and at least two tracks.", null );
        } else {
            ReferenceConnector referenceConnector = ProjectConnector.getInstance().getRefGenomeConnector( getComponent().getSelectedReferenceGenomeID() );
            if( !referenceConnector.hasFeatures( FeatureType.ANY ) ) {
                throw new WizardValidationException( null, "The selected reference genome does not contain any annotations.", null );
            }
        }

    }


}
