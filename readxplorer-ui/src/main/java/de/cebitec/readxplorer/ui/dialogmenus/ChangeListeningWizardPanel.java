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

package de.cebitec.readxplorer.ui.dialogmenus;


import de.cebitec.readxplorer.api.objects.JobPanel;
import de.cebitec.readxplorer.api.objects.NewJobDialogI;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;


/**
 * A wizard panel which is able to listen to changes with a property change
 * listener, which is added in the readSettings method. When firering the
 * ChangeListeningWizardPanel.PROP_VALIDATE property change with either true
 * or false, the listener either displays the given error message, or nothing.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class ChangeListeningWizardPanel implements
        WizardDescriptor.Panel<WizardDescriptor> {

    public static final String PROP_VALIDATE = "validated";

    private final ChangeSupport changeSupport;
    private boolean isValidated = true;
    private String errorMsg;


    /**
     * A wizard panel which is able to listen to changes with a property change
     * listener, which is added in the readSettings method. When firering the
     * ChangeListeningWizardPanel.PROP_VALIDATE property change with either true
     * or false, the listener either displays the given error message, or
     * nothing.
     * <p>
     * @param errorMsg The error message to display, in case the required
     *                 information for this wizard panel is not set correctly.
     */
    public ChangeListeningWizardPanel( String errorMsg ) {
        this.changeSupport = new ChangeSupport( this );
        this.errorMsg = errorMsg;
    }


    /**
     * @return The visual component of this panel.
     */
    @Override
    public abstract Component getComponent();


    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx("help.key.here");
    }


    /**
     * @return true, if the data is validated, false otherwise
     */
    @Override
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        return this.isValidated;
        // If it depends on some condition (form filled out...) and
        // this condition changes (last form field filled in...) then
        // use ChangeSupport to implement add/removeChangeListener below.
        // WizardDescriptor.ERROR/WARNING/INFORMATION_MESSAGE will also be useful.
    }


    @Override
    public void addChangeListener( ChangeListener l ) {
        this.changeSupport.addChangeListener( l );
    }


    @Override
    public void removeChangeListener( ChangeListener l ) {
        this.changeSupport.removeChangeListener( l );
    }


    /**
     * Adds a property change listener to the visual component of this wizard
     * panel, which checks whether to display an error message or not. It also
     * sets the flag if this panel contains valid information to continue.
     * <p>
     * @param wiz the wizard descriptor to which this wizard panel belongs
     */
    @Override
    public void readSettings( final WizardDescriptor wiz ) {
        this.getComponent().addPropertyChangeListener( ChangeListeningWizardPanel.PROP_VALIDATE, new PropertyChangeListener() {
            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                isValidated = (boolean) evt.getNewValue();
                if( isValidated ) {
                    wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, null );
                } else {
                    wiz.putProperty( WizardDescriptor.PROP_ERROR_MESSAGE, errorMsg );
                }
                changeSupport.fireChange();
            }


        } );
        if( this.getComponent() instanceof JobPanel ) {
            isValidated = ((NewJobDialogI) this.getComponent()).isRequiredInfoSet();
        }
    }


    @Override
    public abstract void storeSettings( WizardDescriptor settings );


    /**
     * @param errorMsg The new error message to display, if something is wrong.
     */
    public void setErrorMsg( String errorMsg ) {
        this.errorMsg = errorMsg;
    }


}
