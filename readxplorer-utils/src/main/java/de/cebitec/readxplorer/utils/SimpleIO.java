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

package de.cebitec.readxplorer.utils;


import org.openide.windows.InputOutput;


/**
 * This class implements the SimpleOutput interface for the usage with Netbeans'
 * InputOutput-Class
 * <p>
 * @author Evgeny Anisiforov
 */
public class SimpleIO implements SimpleOutput {

    private final InputOutput io;


    public SimpleIO( InputOutput io ) {
        this.io = io;
    }


    @Override
    public void showMessage( String s ) {
        this.io.getOut().println( s );
    }


    @Override
    public void showError( String s ) {
        this.io.getErr().println( s );
    }


}
