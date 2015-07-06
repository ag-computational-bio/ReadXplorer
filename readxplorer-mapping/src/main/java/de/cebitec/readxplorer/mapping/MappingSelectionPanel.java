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

package de.cebitec.readxplorer.mapping;


import de.cebitec.readxplorer.mapping.api.MappingApi;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;


/**
 * This is the gui part of the card for the selection of parameters of the
 * mapping process.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
class MappingSelectionPanel implements
        WizardDescriptor.FinishablePanel<WizardDescriptor> {

    MappingSelectionPanel() {
        this.getComponent().addPropertyChangeListener( MappingAction.PROP_SOURCEPATH, new PropertyChangeListener() {

            @Override
            public void propertyChange( PropertyChangeEvent evt ) {
                isValid = !((String) evt.getNewValue()).isEmpty();
                fireChangeEvent();
            }


        } );
    }


    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private MappingSelectionCard component;
    private boolean isValid;
    private final Set<ChangeListener> listeners = new HashSet<>( 1 ); // or can use ChangeSupport in NB 6.0


    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if( component == null ) {
            component = new MappingSelectionCard();
        }
        return component;
    }


    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }


    @Override
    public boolean isValid() {
        return isValid;
    }


    @Override
    public boolean isFinishPanel() {
        return isValid;
    }


    @Override
    public final void addChangeListener( ChangeListener l ) {
        synchronized( listeners ) {
            listeners.add( l );
        }
    }


    @Override
    public final void removeChangeListener( ChangeListener l ) {
        synchronized( listeners ) {
            listeners.remove( l );
        }
    }


    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized( listeners ) {
            it = new HashSet<>( listeners ).iterator();
        }
        ChangeEvent ev = new ChangeEvent( this );
        while( it.hasNext() ) {
            it.next().stateChanged( ev );
        }
    }


    @Override
    public void readSettings( WizardDescriptor data ) {
        component.setMappingParam( MappingApi.getLastMappingParams() );
    }


    @Override
    public void storeSettings( WizardDescriptor settings ) {
        settings.putProperty( MappingAction.PROP_SOURCEPATH, component.getSourcePath() );
        settings.putProperty( MappingAction.PROP_REFERENCEPATH, component.getReferencePath() );
        settings.putProperty( MappingAction.PROP_MAPPINGPARAM, component.getMappingParam() );
    }


}
