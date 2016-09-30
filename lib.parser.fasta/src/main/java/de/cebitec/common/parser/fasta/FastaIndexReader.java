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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaIndexReader {

    public List<FastaIndexEntry> read(Path p) throws IOException {
        List<String> readAllLines = Files.readAllLines(p, Charset.defaultCharset());
        List<FastaIndexEntry> list = new ArrayList<>(readAllLines.size());
        for (String string : readAllLines) {
            String[] split = string.split("\\t");
            list.add(new FastaIndexEntry(split[0],
                                         Long.parseLong(split[1]),
                                         Long.parseLong(split[2]),
                                         Long.parseLong(split[3]),
                                         Long.parseLong(split[4])));
        }
        return list;
    }
}
