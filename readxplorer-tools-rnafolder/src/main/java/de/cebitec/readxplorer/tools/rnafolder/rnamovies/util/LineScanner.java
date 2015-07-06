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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.util;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;


/**
 *
 * @author jkrueger
 */
public class LineScanner implements Enumeration<String> {

    private final BufferedReader br;
    private String next;


    public LineScanner( InputStream in ) {
        this.br = new BufferedReader( new InputStreamReader( in ) );
        readLine();
    }


    @Override
    public String nextElement() {
        String tmp;

        tmp = next;
        readLine();

        return tmp;
    }


    @Override
    public boolean hasMoreElements() {
        return (next != null);
    }


    private void readLine() {
        try {
            do {
                next = br.readLine();
            } while( next != null && next.trim().isEmpty() );
        } catch( IOException e ) {
            next = null;
        }
    }


}
