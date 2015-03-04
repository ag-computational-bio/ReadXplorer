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

package de.cebitec.readxplorer.utils;


import java.net.URL;



/**
 * Pairs a standard URL with a friendly name title.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class UrlWithTitle {

    private final String title;
    private final URL url;


    /**
     * Pairs a standard URL with a friendly name title.
     * @param title The title to display as friendly name of the URL
     * @param url The URL itself
     */
    public UrlWithTitle( String title, URL url ) {
        this.title = title;
        this.url = url;
    }


    /**
     * @return The title to display as friendly name of the URL
     */
    public String getTitle() {
        return title;
    }


    /**
     * @return The URL itself
     */
    public URL getUrl() {
        return url;
    }

    /**
     * @return The string representation of the URL.
     */
    @Override
    public String toString() {
        return url.toString();
    }

}
