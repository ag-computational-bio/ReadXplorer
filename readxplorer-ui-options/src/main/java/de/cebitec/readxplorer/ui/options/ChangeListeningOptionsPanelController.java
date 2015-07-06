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

package de.cebitec.readxplorer.ui.options;


import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;


/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public abstract class ChangeListeningOptionsPanelController extends OptionsPanelController {

    private final PropertyChangeSupport pcs = new PropertyChangeSupport( this );
    private boolean changed;


    @Override
    public void update() {
        this.getPanel().load();
        changed = false;
    }


    @Override
    public void applyChanges() {
        this.getPanel().store();
        changed = false;
    }


    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }


    @Override
    public boolean isValid() {
        return this.getPanel().valid();
    }


    @Override
    public boolean isChanged() {
        return changed;
    }


    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }


    @Override
    public JComponent getComponent( Lookup masterLookup ) {
        return this.getPanel();
    }


    @Override
    public void addPropertyChangeListener( PropertyChangeListener l ) {
        pcs.addPropertyChangeListener( l );
    }


    @Override
    public void removePropertyChangeListener( PropertyChangeListener l ) {
        pcs.removePropertyChangeListener( l );
    }


    void changed() {
        if( !changed ) {
            changed = true;
            pcs.firePropertyChange( OptionsPanelController.PROP_CHANGED, false, true );
        }
        pcs.firePropertyChange( OptionsPanelController.PROP_VALID, null, null );
    }


    /**
     * @return The panel belonging to this component.
     */
    protected abstract OptionsPanel getPanel();


}
