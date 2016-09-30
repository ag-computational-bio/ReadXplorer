/*
 * Copyright (C) 2014 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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

import com.google.common.base.Joiner;
import java.util.Objects;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaIndexEntry {

    private String sequenceId;
    private long sequenceLength;
    private long sequenceStartOffset;
    private long lineBaseLength;
    private long lineByteLength;

    public FastaIndexEntry(String sequenceId, long sequenceLength, long sequenceStartOffset, long lineBaseLength, long lineByteLength) {
        this.sequenceId = sequenceId;
        this.sequenceLength = sequenceLength;
        this.sequenceStartOffset = sequenceStartOffset;
        this.lineBaseLength = lineBaseLength;
        this.lineByteLength = lineByteLength;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public long getSequenceStartOffset() {
        return sequenceStartOffset;
    }

    public void setSequenceStartOffset(long sequenceStart) {
        this.sequenceStartOffset = sequenceStart;
    }

    public long getLineBaseLength() {
        return lineBaseLength;
    }

    public void setLineBaseLength(long lineBaseLength) {
        this.lineBaseLength = lineBaseLength;
        this.lineByteLength = lineBaseLength + 1;
    }

    public long getLineByteLength() {
        return lineByteLength;
    }

    public long getSequenceLength() {
        return sequenceLength;
    }

    public void setSequenceLength(long sequenceLength) {
        this.sequenceLength = sequenceLength;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.sequenceId);
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
        final FastaIndexEntry other = (FastaIndexEntry) obj;
        if (!Objects.equals(this.sequenceId, other.sequenceId)) {
            return false;
        }
        if (this.sequenceStartOffset != other.sequenceStartOffset) {
            return false;
        }
        if (this.lineBaseLength != other.lineBaseLength) {
            return false;
        }
        if (this.lineByteLength != other.lineByteLength) {
            return false;
        }
        if (this.sequenceLength != other.sequenceLength) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return Joiner.on("\t").join(sequenceId, sequenceLength, sequenceStartOffset, lineBaseLength, lineByteLength);
    }

}
