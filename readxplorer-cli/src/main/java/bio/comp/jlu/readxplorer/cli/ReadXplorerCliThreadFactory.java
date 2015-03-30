/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.cli;


import java.util.concurrent.ThreadFactory;


/**
 * ReadXplorer CLI Worker Thread Factory.
 * This implementation of <code>ThreadFactory</code> creates
 * daemon low priority worker threads for the CLI ReadXplorer version.
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
