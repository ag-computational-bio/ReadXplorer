
package bio.comp.jlu.readxplorer.cli;


import java.util.concurrent.ThreadFactory;



/**
 *
 * @author Oliver Schwengers <oschweng@cebitec.uni-bielefeld.de>
 */
public class ReadXplorerCliThreadFactory implements ThreadFactory {

    private int threadCount = 0;


    @Override
    public Thread newThread( Runnable r ) {

        Thread t = new Thread( r, "readxplorer-cli-worker-" + threadCount );
            t.setDaemon( true );
            t.setPriority( Thread.MIN_PRIORITY );

        threadCount++;

        return t;

    }



}
