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

import de.cebitec.common.internal.parser.common.Writer;
import de.cebitec.common.internal.parser.common.AbstractStringWriter;
import de.cebitec.common.internal.parser.common.Parser;
import de.cebitec.common.parser.data.embl.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class DateParser extends AbstractStringWriter<Date> implements Parser<Date>, Writer<Date> {

    public static DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

    @Override
    public Date parse(CharSequence data) {
        String toString = data.toString();
        String[] split = toString.split("\n");
        String createdLine = split[0];
        String updatedLine = split[1];

        Pattern createPattern = Pattern.compile("(.*) \\(Rel. (\\d+), Created\\)");
        Matcher matcher = createPattern.matcher(createdLine);
        Date.Entry createdEntry = null;
        Date.Entry updatedEntry = null;
        if (matcher.find()) {
            try {
                String date = matcher.group(1);
                String revision = matcher.group(2);
                java.util.Date parsedDate = format.parse(date);
                createdEntry = new Date.Entry(parsedDate, Integer.parseInt(revision), null);
            } catch (ParseException ex) {
                Logger.getLogger(DateParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        Pattern updatedPattern = Pattern.compile("(.*) \\(Rel. (\\d+), Last updated, Version (\\d+)\\)");
        Matcher updatedMatcher = updatedPattern.matcher(updatedLine);
        if (updatedMatcher.find()) {
            try {
                String date = updatedMatcher.group(1);
                String revision = updatedMatcher.group(2);
                String version = updatedMatcher.group(3);
                java.util.Date parsedDate = format.parse(date);
                updatedEntry = new Date.Entry(parsedDate, Integer.parseInt(revision), Integer.parseInt(version));
            } catch (ParseException ex) {
                Logger.getLogger(DateParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new Date(createdEntry, updatedEntry);
    }

    @Override
    public String write(Date instance) {
        StringBuilder sb = new StringBuilder();

        String createdLine = String.format("%s (Rel. %d, Created)",
                                           format.format(instance.getCreateDate().getDate()).toUpperCase(),
                                           instance.getCreateDate().getRevision());
        String updatedLine = String.format("%s (Rel. %d, Last updated, Version %d)",
                                           format.format(instance.getUpdateDate().getDate()).toUpperCase(),
                                           instance.getUpdateDate().getRevision(),
                                           instance.getUpdateDate().getVersion());

        sb.append(createdLine).append("\n").append(updatedLine);
        return sb.toString();
    }
}
