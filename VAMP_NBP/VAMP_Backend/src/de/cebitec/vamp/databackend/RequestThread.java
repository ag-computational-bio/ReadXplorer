package de.cebitec.vamp.databackend;

/**
 * Abstract class to use in thread context. Provides methods for handling genome requests
 * in connection with a thread.
 * 
 * @author -Rolf Hilker-
 */
public abstract class RequestThread extends Thread {
    
    private IntervalRequest latestRequest;

    /**
     * @return the latest request (e.g. latest request added to the request
     * queue)
     */
    protected IntervalRequest getLatestRequest() {
        return this.latestRequest;
    }

    /**
     * @param latestRequest the latest request (e.g. latest request added to the
     * request queue)
     */
    protected void setLatestRequest(IntervalRequest latestRequest) {
        this.latestRequest = latestRequest;
    }
    
    /**
     * Adds a request to the request queue.
     * @param request the request to add to the queue.
     */
    public abstract void addRequest(IntervalRequest request);

    /**
     * Checks whether the bounds of the current request match the bounds of the
     * latest request (e.g. the latest request added to the request queue. This 
     * helps to ensure that only the results of the latestRequest are returned
     * and if the VAMP user scrolled further and a new request was started already,
     * then the results of the current request are discarded, as they are not 
     * needed anymore).
     * @param request the currently handled request
     * @return true, if the request bounds are identical, false otherwise
     */
    protected boolean matchesLatestRequestBounds(IntervalRequest request) {
        int latestMiddle = calcCenterMiddle(latestRequest);
        int currentMiddle = calcCenterMiddle(request);

        // rounding error somewhere....
        if (currentMiddle - 1 <= latestMiddle && latestMiddle <= currentMiddle + 1) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Calculates the center of the given request.
     * @param request the request whose center is needed
     * @return the center of the given request
     */
    protected int calcCenterMiddle(IntervalRequest request) {
        return (request.getFrom() + request.getTo()) / 2;
    }

}
