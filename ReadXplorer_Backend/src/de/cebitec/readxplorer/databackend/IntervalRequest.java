/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.databackend;


import de.cebitec.readXplorer.util.Properties;


/**
 * An interval request can be any request for any interval data. It is
 * defined by at least three essential parameters: The left and right interval
 * borders for the interval under investigation and a ThreadListener, who wants
 * to receive the results of this request.
 * <p>
 * @author ddoppmeier, rhilker
 */
public class IntervalRequest {

    private int from;
    private int to;
    private int totalFrom;
    private int totalTo;
    private int chromId;
    private ThreadListener sender;
    private byte whichTrackNeeded;
    private byte desiredData;
    private final ParametersReadClasses readClassParams;
    private boolean diffsAndGapsNeeded;


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param chromId            id of the chromosome to querry data from
     * @param totalFrom          The total lower boundary of the request which
     *                           is used
     *                           for preloading larger data amounts for faster access.
     * @param totalTo            The total upper boundary of the request which
     *                           is used for
     *                           preloading larger data amounts for faster access.
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     * @param desiredData        Can be any byte value representing a filter
     *                           flag for
     *                           the results e.g. Properties.READ_STARTS.
     * @param whichTrackNeeded   A byte value representing one of the two flags
     *                           PersistentCoverage.TRACK1 or PersistentCoverage.TRACK2 if this is a
     *                           double track request or ParameterSetMapping.NORMAL, if this is an
     *                           ordinary track request.
     * @param readClassParams    A parameter set which contains all parameters
     *                           concerning the usage of ReadXplorer's coverage classes and if only
     *                           uniquely
     *                           mapped reads shall be used, or all reads.
     */
    public IntervalRequest( int from, int to, int totalFrom, int totalTo, int chromId, ThreadListener sender,
                            boolean diffsAndGapsNeeded, byte desiredData, byte whichTrackNeeded, ParametersReadClasses readClassParams ) {
        this.from = from;
        this.to = to;
        this.totalFrom = totalFrom;
        this.totalTo = totalTo;
        this.chromId = chromId;
        this.sender = sender;
        this.desiredData = desiredData;
        this.whichTrackNeeded = whichTrackNeeded;
        this.readClassParams = readClassParams;
        this.diffsAndGapsNeeded = diffsAndGapsNeeded;
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param chromId            id of the chromosome to querry data from
     * @param totalFrom          The total lower boundary of the request which
     *                           is used
     *                           for preloading larger data amounts for faster access.
     * @param totalTo            The total upper boundary of the request which
     *                           is used for
     *                           preloading larger data amounts for faster access.
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     * @param desiredData        Can be any byte value representing a filter
     *                           flag for
     *                           the results. Can be a byte value representing one of the two flags
     *                           PersistentCoverage.TRACK1 or PersistentCoverage.TRACK2 if this is a
     *                           double track request or Properties.NORMAL, if this is an ordinary
     *                           track request.
     * @param whichTrackNeeded   A byte value representing one of the two flags
     *                           PersistentCoverage.TRACK1 or PersistentCoverage.TRACK2 if this is a
     *                           double track request or ParameterSetMapping.NORMAL, if this is an
     *                           ordinary track request.
     */
    public IntervalRequest( int from, int to, int totalFrom, int totalTo, int chromId, ThreadListener sender,
                            boolean diffsAndGapsNeeded, byte desiredData, byte whichTrackNeeded ) {
        this( from, to, totalFrom, totalTo, chromId, sender, diffsAndGapsNeeded, desiredData, whichTrackNeeded, new ParametersReadClasses() );
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param totalFrom          The total lower boundary of the request which
     *                           is used
     *                           for preloading larger data amounts for faster access.
     * @param chromId            id of the chromosome to querry data from
     * @param totalTo            The total upper boundary of the request which
     *                           is used for
     *                           preloading larger data amounts for faster access.
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     * @param readClassParams    A parameter set which contains all parameters
     *                           concerning the usage of ReadXplorer's coverage classes and if only
     *                           uniquely
     *                           mapped reads shall be used, or all reads.
     */
    public IntervalRequest( int from, int to, int totalFrom, int totalTo, int chromId, ThreadListener sender,
                            boolean diffsAndGapsNeeded, ParametersReadClasses readClassParams ) {
        this( from, to, totalFrom, totalTo, chromId, sender, diffsAndGapsNeeded, Properties.NORMAL, Properties.NORMAL, readClassParams );
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               visible start position of the coverage request
     * @param to                 visible stop position of the coverage request
     * @param totalFrom          The total lower boundary of the request which
     *                           is used
     *                           for preloading larger data amounts for faster access.
     * @param totalTo            The total upper boundary of the request which
     *                           is used for
     *                           preloading larger data amounts for faster access.
     * @param chromId            id of the chromosome to querry data from
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     */
    public IntervalRequest( int from, int to, int totalFrom, int totalTo, int chromId, ThreadListener sender, boolean diffsAndGapsNeeded ) {
        this( from, to, totalFrom, totalTo, chromId, sender, diffsAndGapsNeeded, Properties.NORMAL, Properties.NORMAL, new ParametersReadClasses() );
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param chromId            id of the chromosome to querry data from
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     * @param desiredData        Can be any byte value representing a filter
     *                           flag for
     *                           the results.
     * @param readClassParams    A parameter set which contains all parameters
     *                           concerning the usage of ReadXplorer's coverage classes and if only
     *                           uniquely
     *                           mapped reads shall be used, or all reads.
     */
    public IntervalRequest( int from, int to, int chromId, ThreadListener sender, boolean diffsAndGapsNeeded, byte desiredData, ParametersReadClasses readClassParams ) {
        this( from, to, from, to, chromId, sender, diffsAndGapsNeeded, desiredData, Properties.NORMAL, readClassParams );
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param chromId            id of the chromosome to querry data from
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     * @param desiredData        Can be any byte value representing a filter
     *                           flag for
     *                           the results.
     */
    public IntervalRequest( int from, int to, int chromId, ThreadListener sender, boolean diffsAndGapsNeeded, byte desiredData ) {
        this( from, to, from, to, chromId, sender, diffsAndGapsNeeded, desiredData, Properties.NORMAL, new ParametersReadClasses() );
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param chromId            id of the chromosome to querry data from
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     * @param readClassParams    A parameter set which contains all parameters
     *                           concerning the usage of ReadXplorer's coverage classes and if only
     *                           uniquely
     *                           mapped reads shall be used, or all reads.
     */
    public IntervalRequest( int from, int to, int chromId, ThreadListener sender, boolean diffsAndGapsNeeded, ParametersReadClasses readClassParams ) {
        this( from, to, from, to, chromId, sender, diffsAndGapsNeeded, Properties.NORMAL, Properties.NORMAL, readClassParams );
    }


    /**
     * An interval request can be any request for any interval data. It is
     * defined by at least three essential parameters: The left and right
     * interval borders for the interval under investigation and a
     * ThreadListener, who wants to receive the results of this request.
     * <p>
     * @param from               start position of the coverage request
     * @param to                 stop position of the coverage request
     * @param chromId            id of the chromosome to querry data from
     * @param sender             the sending object, that wants to receive the
     *                           result of the
     *                           request
     * @param diffsAndGapsNeeded true, if diffs and gaps shall be included in
     *                           the result, false otherwise
     */
    public IntervalRequest( int from, int to, int chromId, ThreadListener sender, boolean diffsAndGapsNeeded ) {
        this( from, to, from, to, chromId, sender, diffsAndGapsNeeded, Properties.NORMAL, Properties.NORMAL, new ParametersReadClasses() );
    }


    /**
     * @return The visible start position of the interval under investigation
     */
    public int getFrom() {
        return this.from;
    }


    /**
     * @return The visible end position of the interval under investigation
     */
    public int getTo() {
        return this.to;
    }


    /**
     * @return The total lower boundary of the request which is used for
     *         preloading larger data amounts for faster access.
     */
    public int getTotalFrom() {
        return totalFrom;
    }


    /**
     * @return The total upper boundary of the request which is used for
     *         preloading larger data amounts for faster access.
     */
    public int getTotalTo() {
        return totalTo;
    }


    /**
     * @return the sending object, that wants to receive the result of the
     *         request
     */
    public ThreadListener getSender() {
        return this.sender;
    }


    /**
     * @return Can be any byte value representing a filter flag for the results.
     *         E.g. Properties.READ_STARTS
     */
    public byte getDesiredData() {
        return this.desiredData;
    }


    /**
     * @return A byte value representing one of the two flags
     *         PersistentCoverage.TRACK1 or PersistentCoverage.TRACK2 if this is a
     *         double track request or Properties.NORMAL, if this is a ordinary track
     *         request.
     */
    public byte getWhichTrackNeeded() {
        return whichTrackNeeded;
    }


    /**
     * @return A parameter set which contains all parameters concerning the
     *         usage of ReadXplorer's coverage classes and if only uniquely mapped reads
     *         shall
     *         be used, or all reads.
     */
    public ParametersReadClasses getReadClassParams() {
        return readClassParams;
    }


    /**
     * @return true, if diffs and gaps shall be included in the result, false
     *         otherwise
     */
    public boolean isDiffsAndGapsNeeded() {
        return this.diffsAndGapsNeeded;
    }


    /**
     * @return id of the chromosome to querry data from
     */
    public int getChromId() {
        return this.chromId;
    }


}
