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
import static de.cebitec.common.sequencetools.intervals.Intervals.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * This class implements a string cache based on a NavigableMap that contains contigous intervals of texts. The loaded
 * string parts are stored in weak references to cope with small memory footprint requirements.
 *
 * @param <T> The type of the cached sequences.
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
class IntervalCacheImpl<T> implements IntervalCache<T> {

    private final NavigableMap<Integer, IntervalCacheItem<T>> map = new TreeMap<>();
    private final T defaultValue;
    private final IntervalCache.FetchCallback<T> fetchCallback;
    private final IntervalCache.MergeCallback<T> mergeCallback;
    private final IntervalCache.ExtractCallback<T> extractCallback;
    private final IntervalCache.LengthCallback<T> lengthCallback;

    /**
     *
     * @param defaultValue The value that is returned for empty intervals. May be null.
     * @param fetchCallback The callback that fetches the needed sequences. Must not be null.
     * @param mergeCallback The callback that merges the needed sequences. Must not be null.
     * @param extractCallback The callback that extracts subsequences. Must not be null.
     * @param lengthCallback The callback that calculates the length of a sequence. May be null.
     */
    IntervalCacheImpl(T defaultValue,
                                       FetchCallback<T> fetchCallback,
                                       MergeCallback<T> mergeCallback,
                                       ExtractCallback<T> extractCallback,
                                       LengthCallback<T> lengthCallback) {
        if (fetchCallback == null || mergeCallback == null || extractCallback == null) {
            throw new NullPointerException();
        }
        this.defaultValue = defaultValue;
        this.fetchCallback = fetchCallback;
        this.mergeCallback = mergeCallback;
        this.extractCallback = extractCallback;
        this.lengthCallback = lengthCallback;
    }

    void addValue(Interval<Integer> interval, T value) {
        addValue(interval, value, false);
    }

    /**
     * Adds a string with the given interval to the underlying map.
     *
     * @param interval
     * @param string
     * @param lock
     */
    void addValue(Interval<Integer> interval, T value, boolean lock) {
        if (lengthCallback != null && interval.getLength() != lengthCallback.length(value)) {
            throw new IllegalArgumentException("Value and interval must have the same length.");
        }

        lockInterval(interval);
        SortedMap<Integer, IntervalCacheItem<T>> subMap = getSubMap(interval);
        removeGarbageCollectedItems(subMap.values());
        for (IntervalCacheItem item : subMap.values()) {
            if (operations().overlap(interval, item)) {
                throw new IllegalArgumentException("Overlapping intervals can not be added.");
            }
        }
        unlockInterval(interval);

        IntervalCacheItem<T> item = new IntervalCacheItem<>(mergeCallback, interval, value);
        if (lock) {
            item.lock();
        }
        map.put(interval.getStart(), item);
    }

    NavigableMap<Integer, IntervalCacheItem<T>> getMap() {
        return map;
    }

    /**
     * Locks all strings in the given interval from garbage collection. If an item already was garbage collected it is
     * silently removed.
     *
     * @param interval
     */
    private void lockInterval(Interval<Integer> interval) {
        SortedMap<Integer, IntervalCacheItem<T>> subMap = getSubMap(interval);
        for (Iterator<IntervalCacheItem<T>> it = subMap.values().iterator(); it.hasNext();) {
            IntervalCacheItem item = it.next();
            try {
                item.lock();
            } catch (IllegalStateException ex) {
                it.remove();
            }
        }
    }

    /**
     * Unlocks all strings in the given interval for garbage collection.
     *
     * @param interval
     */
    private void unlockInterval(Interval<Integer> interval) {
        SortedMap<Integer, IntervalCacheItem<T>> subMap = getSubMap(interval);
        for (IntervalCacheItem<T> item : subMap.values()) {
            item.unlock();
        }
    }

    /**
     * Removes all items from the map that were garbage collected.
     *
     * @param items
     */
    private void removeGarbageCollectedItems(Iterable<? extends IntervalCacheItem> items) {
        for (Iterator<? extends IntervalCacheItem> it = items.iterator(); it.hasNext();) {
            IntervalCacheItem item = it.next();
            if (item.getStatus() == IntervalCacheItem.Status.GarbageCollected) {
                it.remove();
            }
        }
    }

    @Override
    public T get(Interval<Integer> interval) {
        // entry point to the interval cache api, force zeroopen intervals.
        interval = interval.as(Interval.Type.ZeroOpen);

        if (interval.isEmpty()) {
            return defaultValue;
        }
        CacheCheckResult cached = isCached(interval);
        if (!cached.isCached()) {
            loadMissingIntervals(cached.getMissingIntervals());
        }
        return _get(interval);
    }

    /**
     * Uses the callback to load the missing intervals to the cache.
     *
     * @param missing
     */
    final void loadMissingIntervals(List<Interval<Integer>> missing) {
        Interval<Integer> enclose = operations().enclose(missing);
        lockInterval(enclose);

        for (Interval<Integer> interval : missing) {
            T value = fetchCallback.fetch(interval);
            addValue(interval, value, true);
        }

        enclose = createInterval(enclose.getStart() - 1, enclose.getEnd() + 1);
        mergeAdjacentIntervals(enclose);
        unlockInterval(enclose);
    }

    /**
     * Merges all adjacent string items in the given interval.
     *
     * @param interval
     */
    final void mergeAdjacentIntervals(Interval<Integer> interval) {
        SortedMap<Integer, IntervalCacheItem<T>> subMap = getSubMap(interval);

        IntervalCacheItem previous = null;
        for (Iterator<IntervalCacheItem<T>> it = subMap.values().iterator(); it.hasNext();) {
            IntervalCacheItem<T> current = it.next();
            if (previous == null) {
                previous = current;
            } else {
                if (operations().adjacent(previous, current)) {
                    previous.merge(current);
                    it.remove();
                } else {
                    previous = current;
                }
            }
        }
    }

    private IntervalCacheItem<T> getItem(Interval<Integer> interval) {
        SortedMap<Integer, IntervalCacheItem<T>> subMap = getSubMap(interval);
        if (subMap.isEmpty()) {
            return null;
        } else {
            IntervalCacheItem<T> item = subMap.get(subMap.firstKey());
            if (item.getStatus() == IntervalCacheItem.Status.GarbageCollected) {
                return null;
            }
            return item;
        }
    }

    private T _get(Interval<Integer> interval) {
        lockInterval(interval);

        IntervalCacheItem<T> item = getItem(interval);
        T output;
        if (item != null) {
            Interval<Integer> shift = operations().shift(interval, item.getStart());
            T data = item.getData();
            output = extractCallback.extract(shift, data);
        } else {
            loadMissingIntervals(Collections.singletonList(interval));
            output = _get(interval);
        }

        unlockInterval(interval);
        return output;
    }

    /**
     * Retrieves a submap that contains all items within the interval or a larger interval if the explicit values are
     * not present in the internal map, i.e. for [5,10] it may return [5,10] but also [min,10] or [5,max] or [min,max]
     * or other values.
     *
     * @param interval
     * @return
     */
    private SortedMap<Integer, IntervalCacheItem<T>> getSubMap(Interval<Integer> interval) {
        interval = interval.as(Interval.Type.ZeroOpen);
        Integer floorKey = map.floorKey(interval.getStart());
        Integer ceilingKey = map.ceilingKey(interval.getEnd());
        SortedMap<Integer, IntervalCacheItem<T>> subMap;
        if (floorKey == null && ceilingKey == null) {
            subMap = map;
        } else if (floorKey == null) {
            subMap = map.headMap(ceilingKey);
        } else if (ceilingKey == null) {
            subMap = map.tailMap(floorKey);
        } else {
            subMap = map.subMap(floorKey, ceilingKey);
        }
        return subMap;
    }

    CacheCheckResult isCached(Interval<Integer> interval) {
        List<Interval<Integer>> missingparts = operations().complement(interval, getSubMap(interval).values());
        return new CacheCheckResult(missingparts);
    }

    @Override
    public void invalidate() {
        map.clear();
    }

    
}
