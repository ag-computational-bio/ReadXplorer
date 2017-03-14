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

package de.cebitec.readxplorer.mapping;


import de.cebitec.centrallookup.CentralLookup;
import de.cebitec.readxplorer.mapping.api.MappingApi;
import de.cebitec.readxplorer.utils.SimpleIO;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * MappingProcessor allows map a fasta file to a reference sequence by using an
 * external mapping script The user will see a progress info.
 * <p>
 * @author Evgeny Anisiforov <evgeny at cebitec.uni-bielefeld.de>
 */
public class MappingProcessor {

    private static final Logger LOG = LoggerFactory.getLogger( MappingProcessor.class.getName() );

    private static final RequestProcessor RP = new RequestProcessor( "interruptible tasks", 1, true );
    private RequestProcessor.Task theTask = null;
    private InputOutput io;


    /**
     * If any message should be printed to the console, this method is used. If
     * an error occurred during the run of the parser, which does not interrupt
     * the parsing process, this method prints the error to the program console.
     * <p>
     * @param msg the msg to print
     */
    private void showMsg( String msg ) {
        this.io.getOut().println( msg );
    }


    @NbBundle.Messages( "MappingProcessor.output.name=Mapper" )
    public MappingProcessor( final String referencePath, final String sourcePath, final String mappingParam ) {
        this.io = IOProvider.getDefault().getIO( Bundle.MappingProcessor_output_name(), true );
        this.io.setOutputVisible( true );
        this.io.getOut().println( "" );

        CentralLookup.getDefault().add( this );
        try {
            io.getOut().reset();
        } catch( IOException ex ) {
            Exceptions.printStackTrace( ex );
        }
        io.select();

        Runnable runnable = new Runnable() {


            @Override
            public void run() {

                try {
                    String sam = MappingApi.mapFastaFile( new SimpleIO( io ), referencePath, sourcePath, mappingParam );
                    showMsg( "Extraction ready!" );
                } catch( IOException ex ) {
                    Exceptions.printStackTrace( ex );
                }

            }


        };

        theTask = RP.create( runnable ); //the task is not started yet
        theTask.schedule( 1 * 1000 ); //start the task with a delay of 1 seconds
    }


}
