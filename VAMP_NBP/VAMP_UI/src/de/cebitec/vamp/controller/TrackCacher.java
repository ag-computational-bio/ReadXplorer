package de.cebitec.vamp.controller;


import de.cebitec.vamp.databackend.CoverageAndDiffRequest;
import de.cebitec.vamp.databackend.CoverageThread;
import de.cebitec.vamp.databackend.ObjectCache;
import de.cebitec.vamp.databackend.ThreadListener;
import de.cebitec.vamp.databackend.connector.TrackConnector;
import de.cebitec.vamp.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;


/**
 *
 * @author jeff
 */


public final class TrackCacher {
    
    
    private final static int SCANFACTOR = 10;
    
    private final static RequestProcessor RP = new RequestProcessor("interruptible tasks", 1, true);
    private final static Logger LOG = Logger.getLogger(TrackCacher.class.getName());
    private RequestProcessor.Task theTask = null;

    public TrackCacher(final TrackConnector tc, final int refLength) {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Compute cache for track '" + tc.getAssociatedTrackName() +/*track.getDescription()+*/ "'", new Cancellable() {
            
            public boolean cancel() {
                return handleCancel();
            }
        });

        Runnable runnable = new Runnable() {
            
            private int currentPosition = 1;
            private int steps;
            private int currentStep = 0;
            private boolean wasCanceled = false;
            private boolean ready = false;
            private ThreadListener tl;

            private void requestNextStep() {

                //TrackConnector tc = ProjectConnector.getInstance().getTrackConnector(track);
                int t = currentStep;
                if (currentStep % 2 == 0) {
                    t = steps - currentStep;
                }

                currentPosition = t * (CoverageThread.MINIMUMINTERVALLENGTH / SCANFACTOR) + 100;
                LOG.log(Level.INFO, "Requesting track cache for position={0}", currentPosition);

                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                tc.addCoverageRequest(new CoverageAndDiffRequest(currentPosition, currentPosition + 100, tl));


                currentStep++;
                //make sure, that we do not show too many steps
                if (currentStep > steps) {
                    ph.finish();
                }// ph.progress(steps); 
                //currentStep = steps;
                else {
                    ph.progress(currentStep);
                }

            }
            
            @Override
            public void run() {
                
                tl = new ThreadListener() {
                    private void react() {
                        if ((currentStep <= steps) && (!wasCanceled)) {
                            requestNextStep();
                        } else {
                            ready = true;
                        }
                    }

                    @Override
                    public void receiveData(Object data) {
                        react();
                    }

                    @Override
                    public void notifySkipped() {
                        react();
                    }
                };

                steps = (int) Math.ceil((double) refLength / (double) CoverageThread.MINIMUMINTERVALLENGTH) * SCANFACTOR;
                ph.start(); //we must start the PH before we swith to determinate
                ph.switchToDeterminate(steps);

                ph.progress(0);
                requestNextStep();

                while ((!ready) && (!wasCanceled)) {
                    try {
                        LOG.info("Cacher not ready yet...");
                        Thread.sleep(1000); //throws InterruptedException is the task was cancelled
                    } catch (InterruptedException ex) {
                        LOG.info("Track cacher was canceled");
                        wasCanceled = true;
                        return;
                    }
                }
                ph.finish();
                  
            }

            
        };
        
        //check a boolean key to test, if the cache has allready been created 
        //for this track
        final String cacheFamily = ObjectCache.getTrackCacherFieldFamily();
        final String cacheKey = "Track." + tc.getTrackID();

        Object cachedResult = ObjectCache.getInstance().get(cacheFamily, cacheKey);
        if (cachedResult == null) {
            ObjectCache.getInstance().deleteFamily("loadCoverage." + tc.getTrackID());

            theTask = RP.create(runnable); //the task is not started yet

            theTask.addTaskListener(new TaskListener() {
                public void taskFinished(Task task) {
                    ph.finish();
                    ObjectCache.getInstance().set(cacheFamily, cacheKey, true);
                }
            });

            //start pre caching only, if the user accepted it in his prefererences
            if (NbPreferences.forModule(Object.class).getBoolean(Properties.OBJECTCACHE_AUTOSTART, true)
                    && NbPreferences.forModule(Object.class).getBoolean(Properties.OBJECTCACHE_ACTIVE, true)) {
                if (tc.isDbUsed()) {
                    theTask.schedule(5 * 1000); //start the task with a delay of 5 seconds
                }                                      //to let the first track position load 
            }
        }

    }

   

    private boolean handleCancel() {
        LOG.info("handleCancel");
        if (null == theTask) {
            return false;
        }

        return theTask.cancel();
    }
}