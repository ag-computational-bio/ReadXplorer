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
package de.cebitec.readXplorer.ui.visualisation.actions;

import de.cebitec.readXplorer.api.cookies.OpenRefGenCookie;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

@Deprecated
public final class OpenRefGenDialogAction implements ActionListener {

    private final OpenRefGenCookie context;

    public OpenRefGenDialogAction(OpenRefGenCookie context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.open();
    }
}
