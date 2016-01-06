/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.utils.errorhandling;

import de.cebitec.readxplorer.api.ErrorHandler;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;

import static de.cebitec.readxplorer.utils.errorhandling.Bundle.Msg_Header;


/**
 * Designed for handling exceptions and logging in the GUI.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class GuiErrorHandler implements ErrorHandler {


    /**
     * {@inheritDoc }
     */
    @NbBundle.Messages( { "# {0} - Exception name", "Msg_Header={0} Exception Occurred" } )
    @Override
    public void handle( Throwable t ) {
        handle( t, Msg_Header( t.getClass().getName() ) );
    }


    /**
     * {@inheritDoc }
     */
    @Override
    public void handle( Throwable t, String header ) {
        JOptionPane.showMessageDialog( JOptionPane.getRootFrame(), t, header, JOptionPane.INFORMATION_MESSAGE );
    }


}
