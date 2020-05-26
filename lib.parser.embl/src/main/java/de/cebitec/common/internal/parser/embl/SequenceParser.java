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
package de.cebitec.common.internal.parser.embl;

import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.internal.parser.common.AbstractStringWriter;
import de.cebitec.common.parser.data.embl.Sequence;
import com.google.common.base.Splitter;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class SequenceParser extends AbstractStringWriter<Sequence> implements Parser<Sequence> {

    @Override
    public Sequence parse(CharSequence data) {
        Sequence sq = new Sequence();
        StringBuilder sb = new StringBuilder();
        for (String line : Splitter.on("\n").split(data)) {
            // header
            if (line.startsWith("Sequence")) {
                // not needed here. Implement if needed.
                // not needed here. Implement if needed.
            } // sequence
            // sequence
            else {
                String seq = line.replaceAll("\\s+", "").replaceAll("\\d+", "");
                sb.append(seq);
            }
        }
        sq.setSequence(sb.toString());
        return sq;
    }

    @Override
    public String write(Sequence data) {
        StringBuilder sb = new StringBuilder();
        sb.append(createHeader(data)).append("\n");
        sb.append(createSequence(data));
        return sb.toString();
    }

    private String createSequence(Sequence data) {
        int blockSize = 10;
        int lineSize = 60;
        String leadingWhitespaces = String.format("%5s", "");
        int blocksPerLine = lineSize / blockSize;
        StringBuilder sb = new StringBuilder(leadingWhitespaces);
        String seq = data.getSequence();
        int length = seq.length();
        for (int i = 0; i < seq.length(); i += blockSize) {
            int end = i + blockSize;
            if (end < seq.length()) {
                sb.append(seq.substring(i, end)).append(" ");
                if (end % lineSize == 0) {
                    sb.append(String.format("%9d", end)).append("\n").append(leadingWhitespaces);
                }
            } else {
                sb.append(seq.substring(i));
                // find block number
                int blockNumber = i / blockSize;
                // compute delta block to linesize
                int blockLineDelta = blocksPerLine - (blockNumber % blocksPerLine);
                // compute delta to block end
                int posBlockDelta = blockNumber * blockSize - length;
                int whitespaces = posBlockDelta + blockLineDelta * (blockSize + 1);
                sb.append(String.format("%" + whitespaces + "s", ""));
                sb.append(String.format("%9d", length));
            }
        }
        return sb.toString();
    }

    private String createHeader(Sequence data) {
        StringBuilder sb = new StringBuilder();
        int length = data.getSequence().length();
        int as = 0;
        int ts = 0;
        int gs = 0;
        int cs = 0;
        int other = 0;
        for (int i = 0; i < data.getSequence().length(); i++) {
            switch (data.getSequence().charAt(i)) {
                case 'a':
                case 'A':
                    as++;
                    break;
                case 'g':
                case 'G':
                    gs++;
                    break;
                case 't':
                case 'T':
                    ts++;
                    break;
                case 'c':
                case 'C':
                    cs++;
                    break;
                default:
                    other++;
            }
        }
        sb.append("Sequence ").append(length).append(" BP; ").append(as).append(" A; ").append(cs).append(" C; ").append(gs).append(" G; ").append(ts).append(" T; ").append(other).append(" other;");
        return sb.toString();
    }

}
