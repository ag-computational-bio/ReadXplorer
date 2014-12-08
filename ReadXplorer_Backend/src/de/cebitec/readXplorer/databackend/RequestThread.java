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

package de.cebitec.readXplorer.databackend;


import de.cebitec.readXplorer.util.classification.Classification;
import java.util.ArrayList;
import java.util.List;


/**
 * Abstract class to use in thread context. Provides methods for handling genome
 * requests in connection with a thread.
 * <p>
 * @author -Rolf Hilker-
 */
public abstract class RequestThread extends Thread {

    private IntervalRequest latestRequest;
    private IntervalRequest lastRequest = new IntervalRequest( 0, 0, -1, null, false );


    /**
     * @return the latest request (e.g. latest request added to the request
     *         queue)
     */
    protected IntervalRequest getLatestRequest() {
        return this.latestRequest;
    }


    /**
     * @param latestRequest the latest request (e.g. latest request added to the
     *                      request queue)
     */
    protected void setLatestRequest( IntervalRequest latestRequest ) {
        this.latestRequest = latestRequest;
    }


    /**
     * @return the last request (the last request which returned a result)
     */
    protected IntervalRequest getLastRequest() {
        return lastRequest;
    }


    /**
     * @param lastRequest the last request (the last request which returned a
     *                    result)
     */
    protected void setLastRequest( IntervalRequest lastRequest ) {
        this.lastRequest = lastRequest;
    }


    /**
     * Adds a request to the request queue.
     * <p>
     * @param request the request to add to the queue.
     */
    public abstract void addRequest( IntervalRequest request );


    /**
     * @param request the request to compare to the last request
     * <p>
     * @return true, if the read class parameters are completely the same, false
     *         otherwise.
     */
    public boolean readClassParamsFulfilled( IntervalRequest request ) {
        List<Classification> lastExcludedClasses = lastRequest.getReadClassParams().getExcludedClasses();
        List<Classification> excludedClasses = new ArrayList<>( request.getReadClassParams().getExcludedClasses() );
        return excludedClasses.containsAll( lastExcludedClasses ) && lastExcludedClasses.containsAll( excludedClasses );
    }


    /**
     * Checks whether the bounds of the current request match the bounds of the
     * latest request (e.g. the latest request added to the request queue. This
     * helps to ensure that only the results of the latestRequest are returned
     * and if the ReadXplorer user scrolled further and a new request was
     * started already,
     * then the results of the current request are discarded, as they are not
     * needed anymore).
     * <p>
     * @param request the currently handled request
     * <p>
     * @return true, if the request bounds are different, false otherwise
     */
    protected boolean doesNotMatchLatestRequestBounds( IntervalRequest request ) {
        int latestMiddle = calcCenterMiddle( latestRequest );
        int currentMiddle = calcCenterMiddle( request );

        // rounding error somewhere....
        return currentMiddle - 1 <= latestMiddle && latestMiddle <= currentMiddle + 1
               || request.getDesiredData() != latestRequest.getDesiredData()
               || request.getSender() != latestRequest.getSender()
               || request.getChromId() != latestRequest.getChromId();
    }


    /**
     * Calculates the center of the given request.
     * <p>
     * @param request the request whose center is needed
     * <p>
     * @return the center of the given request
     */
    protected int calcCenterMiddle( IntervalRequest request ) {
        return (request.getFrom() + request.getTo()) / 2;
    }


}
