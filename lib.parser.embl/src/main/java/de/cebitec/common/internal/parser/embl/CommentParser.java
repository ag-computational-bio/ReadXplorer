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
import de.cebitec.common.parser.data.embl.Comment;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 *
 * @author Lukas Jelonek {@literal <ljelonek at cebitec.uni-bielefeld.de>}
 */
public class CommentParser extends AbstractStringWriter<Comment> implements Parser<Comment>, Writer<Comment> {

    public static DateFormat format = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

    @Override
    public Comment parse(CharSequence data) {
        return new Comment(data.toString());
    }

    @Override
    public String write(Comment instance) {
        return instance.getComment();
    }
}
