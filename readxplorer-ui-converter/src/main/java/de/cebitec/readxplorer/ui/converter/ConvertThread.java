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

package de.cebitec.readxplorer.ui.converter;


import de.cebitec.readxplorer.parser.output.ConverterI;
import de.cebitec.readxplorer.utils.Observer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;


/**
 * Thread carrying out the conversion of one file into another format.
 * <p>
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class ConvertThread extends Thread implements Observer {

    private final InputOutput io;
    private final ConverterI converter;
    private final ProgressHandle progressHandle;


    /**
     * Thread carrying out the conversion of one file into another format.
     */
    public ConvertThread( ConverterI converter ) {
        this.io = IOProvider.getDefault().getIO( NbBundle.getMessage( ConvertThread.class, "ConvertThread.output.name" ), false );
        this.converter = converter;
        this.progressHandle = ProgressHandleFactory.createHandle( NbBundle.getMessage( ConvertThread.class, "ConvertThread.progress.name" ) );
        this.progressHandle.start();

    }


    @Override
    public void run() {
        try {
            converter.convert();
            this.progressHandle.finish();
        } catch( Exception ex ) {
            this.io.getOut().println( ex.toString() );
        }
    }


    @Override
    public void update( Object data ) {
        if( data instanceof String && ((String) data).contains( "..." ) ) {
            this.progressHandle.progress( String.valueOf( data ) );
        } else {
            this.io.getOut().println( data.toString() );
            System.out.println( data.toString() );
        }
    }


}
