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

package de.cebitec.readxplorer.dashboard;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Cancellable;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * An action, which can be canceled.
 * <p>
 * @author jeff
 */
public final class MyCancelableAction implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger( MyCancelableAction.class.getName() );

    private static final RequestProcessor RP = new RequestProcessor( "interruptible tasks", 1, true );
    private RequestProcessor.Task theTask = null;


    @Override
    public void actionPerformed( ActionEvent e ) {
        final ProgressHandle ph = ProgressHandleFactory.createHandle( "task thats shows progress", new Cancellable() {

                                                                  @Override
                                                                  public boolean cancel() {
                                                                      return handleCancel();
                                                                  }


                                                              } );

        Runnable runnable = new Runnable() {

            private static final int NUM = 60000;


            @Override
            public void run() {
                try {
                    ph.start(); //we must start the PH before we swith to determinate
                    ph.switchToDeterminate( NUM );
                    for( int i = 0; i < NUM; i++ ) {
                        doSomething( i );
                        ph.progress( i );
                        Thread.sleep( 0 ); //throws InterruptedException is the task was cancelled
                    }

                } catch( InterruptedException ex ) {
                    LOG.info( "the task was CANCELLED" );
                }

            }


            private void doSomething( int i ) {
                LOG.info( "doSomething with {0}", i );
            }


        };

        theTask = RP.create( runnable ); //the task is not started yet

        theTask.addTaskListener( new TaskListener() {
            @Override
            public void taskFinished( Task task ) {
                ph.finish();
            }


        } );

        theTask.schedule( 0 ); //start the task


    }


    private boolean handleCancel() {
        LOG.info( "handleCancel" );
        if( null == theTask ) {
            return false;
        }

        return theTask.cancel();
    }


}
