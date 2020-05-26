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
package de.cebitec.common.sequencetools.intervals;

import java.io.Serializable;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class SerializableInterval implements Serializable, Interval<Integer> {

    private java.lang.Integer start;
    private java.lang.Integer end;
    private Type type;

    public SerializableInterval() {
    }

    public SerializableInterval(java.lang.Integer start, java.lang.Integer end, Type type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    @Override
    public java.lang.Integer getStart() {
        return start;
    }

    @Override
    public java.lang.Integer getEnd() {
        return end;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public java.lang.Integer getLength() {
        if (Type.ZeroOpen.equals(getType())) {
            return getEnd() - getStart();
        } else {
            return Intervals.normalize(this).getLength();
        }
    }

    @Override
    public boolean isEmpty() {
        return getLength() == 0;
    }

    @Override
    public Interval<java.lang.Integer> as(Type newType) {
        return Intervals.transfromInterval(this, newType);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + (this.start != null ? this.start.hashCode() : 0);
        hash = 89 * hash + (this.end != null ? this.end.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SerializableInterval other = (SerializableInterval) obj;
        if (this.start != other.start && (this.start == null || !this.start.equals(other.start))) {
            return false;
        }
        if (this.end != other.end && (this.end == null || !this.end.equals(other.end))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Interval{" + "type=" + type + ", start=" + start + ", end=" + end + '}';
    }
}
