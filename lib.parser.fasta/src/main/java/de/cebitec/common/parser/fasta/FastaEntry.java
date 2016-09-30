/*
 * Copyright (C) 2013 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.parser.fasta;

import java.util.Objects;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaEntry {
    private String header;
    private String name;
    private String sequence;

    public FastaEntry() {
    }

    public FastaEntry(String header, String name, String sequence) {
        this.header = header;
        this.name = name;
        this.sequence = sequence;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FastaEntry other = (FastaEntry) obj;
        if (!Objects.equals(this.header, other.header)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.sequence, other.sequence)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FastaEntry{" + "header=" + header + ", name=" + name + ", sequence=" + sequence.substring(0, 100) + '}';
    }
    
    
    
}
