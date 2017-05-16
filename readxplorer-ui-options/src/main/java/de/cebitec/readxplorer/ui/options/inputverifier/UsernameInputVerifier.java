/*
 * Copyright (C) 2017 Patrick Blumenkamp<patrick.blumenkamp@computational.bio.uni-giessen.de>
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

package de.cebitec.readxplorer.ui.options.inputverifier;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 *
 * @author Patrick
 * Blumenkamp<patrick.blumenkamp@computational.bio.uni-giessen.de>
 */
public class UsernameInputVerifier extends InputVerifier {

    private JLabel messageBox = null;


    public UsernameInputVerifier() {
    }


    public UsernameInputVerifier( JLabel messageBox ) {
        this.messageBox = messageBox;
    }


    @Override
    public boolean shouldYieldFocus( JComponent input ) {
        boolean isValid = super.shouldYieldFocus( input );
        if( !isValid && messageBox != null ) {
            messageBox.setText( "Username cannot be left empty." );
        } else {
            messageBox.setText( "" );
        }
        return isValid;
    }


    @Override
    public boolean verify( JComponent input ) {
        JTextField textField = (JTextField) input;
        String username = textField.getText();
        return !username.isEmpty();
    }


}
