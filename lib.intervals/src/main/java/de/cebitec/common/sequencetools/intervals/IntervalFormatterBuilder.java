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

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class IntervalFormatterBuilder<T extends Number> {

    private List<IntervalFormatter<T>> list = new LinkedList<>();
    private Interval.Type intervaltype;

    public IntervalFormatterBuilder<T> outputType(Interval.Type type) {
        this.intervaltype = type;
        return this;
    }

    public IntervalFormatterBuilder<T> startPosition() {
        list.add(new IntervalFormatter<T>() {
            @Override
            public String format(Interval<T> interval) {
                T start = interval.getStart();
                return "" + start;
            }
        });
        return this;
    }

    public IntervalFormatterBuilder<T> text(final String text) {
        list.add(new IntervalFormatter<T>() {
            @Override
            public String format(Interval<T> interval) {
                return text;
            }
        });
        return this;
    }

    public IntervalFormatterBuilder<T> endPosition() {
        list.add(new IntervalFormatter<T>() {
            @Override
            public String format(Interval<T> interval) {
                T start = interval.getEnd();
                return "" + start;
            }
        });
        return this;
    }

    public IntervalFormatter create() {
        return new IntervalFormatterImpl(list, intervaltype);
    }
}
