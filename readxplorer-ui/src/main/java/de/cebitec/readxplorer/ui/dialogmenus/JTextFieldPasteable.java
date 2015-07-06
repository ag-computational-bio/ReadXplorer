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


import de.cebitec.readxplorer.utils.GeneralUtils;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;


/**
 * @author -Rolf Hilker-
 * <p>
 * Extends the ordinary JTextField by overwriting the CTRL+V shortcut to paste
 * the current system clipboard content.
 */
public class JTextFieldPasteable extends JTextField {

    /**
     * Extends the ordinary JTextField by overwriting the CTRL+V shortcut to
     * paste the current system clipboard content.
     */
    public JTextFieldPasteable() {
        this.setPasteBehaviour();
    }


    /**
     * Updates the paste behaviour of the CTRL+V shortcut to paste the current
     * system clipboard content.
     */
    private void setPasteBehaviour() {
        Action action = createAction();
        JTextFieldPasteable.this.getInputMap().put( KeyStroke.getKeyStroke( KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK ), "Paste" );
        JTextFieldPasteable.this.getActionMap().put( "Paste", action );
    }


    /**
     * @return Creates and returns the action that updates the paste behaviour
     *         of the CTRL+V shortcut to paste the current system clipboard content.
     */
    private Action createAction() {

        Action action = new Action() {

            @Override
            public void actionPerformed( final ActionEvent event ) {
                JTextFieldPasteable.this.setText( GeneralUtils.getClipboardContents( JTextFieldPasteable.this ) );
            }


            /**
             * Method not implemented yet, returns null.
             */
            @Override
            public Object getValue( String key ) {
                return null;
            }


            /**
             * Method not implemented yet, does not add anything.
             */
            @Override
            public void putValue( String key, Object value ) {
            }


            /**
             * Method not implemented yet, does not set anything.
             */
            @Override
            public void setEnabled( boolean b ) {
            }


            /**
             * Method not implemented yet, always returns true.
             */
            @Override
            public boolean isEnabled() {
                return true;
            }


            /**
             * Method not implemented yet, does not set anything.
             */
            @Override
            public void addPropertyChangeListener( PropertyChangeListener listener ) {
            }


            /**
             * Method not implemented yet, does not delete anything.
             */
            @Override
            public void removePropertyChangeListener( PropertyChangeListener listener ) {
            }


        };

        return action;
    }


}
