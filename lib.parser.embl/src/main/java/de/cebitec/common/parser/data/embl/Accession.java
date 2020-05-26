/*
 * Copyright (C) 2012 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.parser.data.embl;

import java.util.List;

/**
 * Representation of the AC lines.
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class Accession {

    private List<String> numbers;

    public Accession() {
    }

    public Accession(List<String> numbers) {
        this.numbers = numbers;
    }

    public List<String> getNumbers() {
        return numbers;
    }

    public void setNumbers(List<String> numbers) {
        this.numbers = numbers;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + (this.numbers != null ? this.numbers.hashCode() : 0);
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
        final Accession other = (Accession) obj;
        if (this.numbers != other.numbers && (this.numbers == null || !this.numbers.equals(other.numbers))) {
            return false;
        }
        return true;
    }

}
