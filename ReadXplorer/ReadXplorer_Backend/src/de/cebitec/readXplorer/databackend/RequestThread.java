package de.cebitec.readXplorer.databackend;

/**
 * Abstract class to use in thread context. Provides methods for handling genome
 * requests in connection with a thread.
 * 
 * @author -Rolf Hilker-
 */
public abstract class RequestThread extends Thread {
    
    private IntervalRequest latestRequest;
    private IntervalRequest lastRequest = new IntervalRequest(0, 0, -1, null, false);

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
     * @return the last request (the last request which returned a result)
     */
    protected IntervalRequest getLastRequest() {
        return lastRequest;
    }

    /**
     * @param lastRequest the last request (the last request which returned a
     * result)
     */
    protected void setLastRequest(IntervalRequest lastRequest) {
        this.lastRequest = lastRequest;
    }
    
    
    
    /**
     * Adds a request to the request queue.
     * @param request the request to add to the queue.
     */
    public abstract void addRequest(IntervalRequest request);
    
    /**
     * @param request the request to compare to the last request
     * @return true, if the read class parameters are completely the same, false
     * otherwise.
     */
    public boolean readClassParamsFulfilled(IntervalRequest request) {
        ParametersReadClasses lastParams = lastRequest.getReadClassParams();
        ParametersReadClasses params = request.getReadClassParams();
        return  lastParams.isPerfectMatchUsed() == params.isPerfectMatchUsed() &&
                lastParams.isBestMatchUsed()    == params.isBestMatchUsed()    &&
                lastParams.isCommonMatchUsed()  == params.isCommonMatchUsed()  &&
                lastParams.isOnlyUniqueReads()  == params.isOnlyUniqueReads();
    }

    /**
     * Checks whether the bounds of the current request match the bounds of the
     * latest request (e.g. the latest request added to the request queue. This 
     * helps to ensure that only the results of the latestRequest are returned
     * and if the ReadXplorer user scrolled further and a new request was started already,
     * then the results of the current request are discarded, as they are not 
     * needed anymore).
     * @param request the currently handled request
     * @return true, if the request bounds are different, false otherwise
     */
    protected boolean doesNotMatchLatestRequestBounds(IntervalRequest request) {
        int latestMiddle = calcCenterMiddle(latestRequest);
        int currentMiddle = calcCenterMiddle(request);
        
        // rounding error somewhere....
        if (currentMiddle - 1 <= latestMiddle && latestMiddle <= currentMiddle + 1
                || request.getDesiredData() != latestRequest.getDesiredData()
                || request.getSender() != latestRequest.getSender()
                || request.getChromId() != latestRequest.getChromId()
                ) {
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
