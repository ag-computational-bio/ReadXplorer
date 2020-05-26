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
import java.lang.ref.WeakReference;
import java.util.Objects;
import static de.cebitec.common.sequencetools.intervals.Intervals.*;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
class IntervalCacheItem<T> implements Interval<Integer> {

    private Interval<Integer> interval;
    private WeakReference<T> data;
    private T lock;
    private final IntervalCache.MergeCallback<T> mergeCallback;

    enum Status {

        Cached,
        GarbageCollected
    }

    IntervalCacheItem(IntervalCache.MergeCallback<T> mergeCallback, Interval<Integer> interval, T data) {
        if (data == null) {
            throw new NullPointerException();
        }
        this.mergeCallback = mergeCallback;
        this.interval = interval;
        this.data = new WeakReference<>(data);
    }

    Status getStatus() {
        return data.get() == null ? Status.GarbageCollected : Status.Cached;
    }

    Interval<Integer> getInterval() {
        return interval;
    }

    T getData() {
        return data.get();
    }

    @Override
    public Type getType() {
        return interval.getType();
    }

    @Override
    public Integer getLength() {
        return interval.getLength();
    }

    @Override
    public Integer getStart() {
        return interval.getStart();
    }

    @Override
    public Integer getEnd() {
        return interval.getEnd();
    }

    @Override
    public boolean isEmpty() {
        return interval.isEmpty();
    }

    /**
     * Creates a hard reference to the weakly referenced string and thus locks it from garbage collection.
     */
    void lock() {
        this.lock = data.get();
        if (lock == null) {
            throw new IllegalStateException("Locking of cache item not possible as it already got garbage collected.");
        }
    }

    /**
     * Removes the hard reference created by lock.
     */
    void unlock() {
        this.lock = null;
    }

    /**
     * Merges the content of the next item to this item.
     *
     * @param item
     */
    void merge(IntervalCacheItem<T> item) {
        if (operations().leftOf(this, item) && operations().adjacent(this, item)) {
            lock();
            Interval<Integer> union = operations().union(this, item);
            T odata = item.getData();
            T merge = mergeCallback.merge(data.get(), odata);
            data = new WeakReference<>(merge);
            interval = union;
            unlock();
        } else {
            throw new UnsupportedOperationException("Merging an string cache item that is not "
                + "adjacent and right of the given item is not allowed.");
        }
    }

    @Override
    public Interval<Integer> as(Type newType) {
        return interval.as(newType);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.interval);
        hash = 59 * hash + Objects.hashCode(this.data);
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
        final IntervalCacheItem other = (IntervalCacheItem) obj;
        if (!Objects.equals(this.interval, other.interval)) {
            return false;
        }
        if (!Objects.equals(getData(), getData())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StringCacheItem{" + "interval=" + interval + ", data=" + data.get() + '}';
    }
}
