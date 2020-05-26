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
package de.cebitec.common.sequencetools.intervals.cache;

import de.cebitec.common.sequencetools.intervals.Interval;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
class CacheCheckResult {

    private List<Interval<Integer>> list;

    CacheCheckResult(List<Interval<Integer>> list) {
        if (list == null) {
            throw new NullPointerException();
        }
        this.list = list;
    }

    List<Interval<Integer>> getMissingIntervals() {
        return list == null ? Collections.EMPTY_LIST : list;
    }

    boolean isCached() {
        return list.isEmpty();
    }

}
