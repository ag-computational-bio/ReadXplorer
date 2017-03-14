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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.actions;


import de.cebitec.readxplorer.tools.rnafolder.rnamovies.MoviePane;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

//import de.cebitec.readxplorer.tools.rnaFolder.rnamovies.RNAMovies;

public abstract class MovieAction extends AbstractAction {

    //protected RNAMovies movies;
    protected MoviePane movies;


    public MovieAction( String name, String iconName ) {
        super( name, loadIcon( iconName, name ) );
    }


    private static ImageIcon loadIcon( String name, String description ) {
        URL imageURL = MovieAction.class.getResource( "icons/" + name.replaceAll( " ", "_" ) + ".png" );
        ImageIcon icon = null;
        if( imageURL != null ) {
            icon = new ImageIcon( imageURL, description );
        }

        return icon;
    }


}
