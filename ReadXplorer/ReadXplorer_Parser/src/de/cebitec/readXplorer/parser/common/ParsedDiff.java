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
package de.cebitec.readXplorer.parser.common;

/**
 *
 * @author ddoppmeier
 */
public class ParsedDiff {

    private long position;
    private char base;

    public ParsedDiff(long position, char c){
        this.position = position;
        this.base = c;
    }

    public char getBase() {
        return base;
    }

    public long getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ParsedDiff other = (ParsedDiff) obj;
        if (this.position != other.getPosition()) {
            return false;
        }
        if (this.base != other.getBase()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (int) (this.position ^ (this.position >>> 32));
        hash = 37 * hash + this.base;
        return hash;
    }
        
}
