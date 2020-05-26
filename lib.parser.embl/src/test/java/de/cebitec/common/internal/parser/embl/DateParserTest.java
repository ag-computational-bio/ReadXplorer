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

import de.cebitec.common.parser.data.embl.Date;
import java.text.ParseException;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class DateParserTest {

    @Test
    public void testDateParser() throws ParseException {
        DateParser dateParser = new DateParser();
        String date = "12-SEP-1991 (Rel. 29, Created)\n"
            + "13-SEP-1993 (Rel. 37, Last updated, Version 8)";

        Date parsed = dateParser.parse(date);

        assertThat(parsed, equalTo(new Date(new Date.Entry(new java.util.Date(91, 8, 12), 29, null),
                                            new Date.Entry(new java.util.Date(93, 8, 13), 37, 8))));

        assertThat(dateParser.write(parsed), equalTo(date));
    }
}
