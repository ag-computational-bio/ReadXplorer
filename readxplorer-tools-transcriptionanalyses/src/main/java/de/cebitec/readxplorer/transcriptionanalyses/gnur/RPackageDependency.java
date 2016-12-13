/*
 * Copyright (C) 2016 Patrick Blumenkamp <patrick.blumenkamp at computational.bio.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses.gnur;


/**
 *
 * @author Patrick Blumenkamp
 * <patrick.blumenkamp at computational.bio.uni-giessen.de>
 */
public class RPackageDependency {

    private String name;
    private Version version;


    public RPackageDependency( String name, Version version ) {
        this.name = name;
        this.version = version;
    }


    public RPackageDependency( String name ) {
        this.name = name;
        this.version = new Version( "0" );
    }


    public String getName() {
        return name;
    }


    public Version getVersion() {
        return version;
    }


    @Override
    public boolean equals( Object obj ) {
        if( this == obj ) {
            return true;
        }
        if( obj == null ) {
            return false;
        }
        if( getClass() != obj.getClass() ) {
            return false;
        }
        final RPackageDependency other = (RPackageDependency) obj;
        if( !this.getName().equals( other.getName() ) ) {
            return false;
        }
        return this.getVersion().equals( other.getVersion() );
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = hash * 17 + name.hashCode();
        hash = hash * 17 + version.hashCode();
        return hash;
    }


    @Override
    public String toString() {
        return this.getName() + " - " + this.getVersion().getVersion();
    }
    
    


}
