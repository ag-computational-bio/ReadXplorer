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

package de.cebitec.readxplorer.utils.filechooser;


import java.awt.HeadlessException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.MissingResourceException;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.NbBundle.Messages;


/**
 * ReadXplorer's String file chooser. Contains the save method storing an
 * arbitrary string.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class StoreStringFileChooser extends ReadXplorerFileChooser {

    private static final long serialVersionUID = 1L;

    private ProgressHandle progressHandle;


    /**
     * Creates a new String file chooser. Contains the save method storing an
     * arbitrary string.
     * <p>
     * @param fileExtension   file extension to use for this file
     * @param fileDescription file description
     * @param string          string to store in the file
     */
    public StoreStringFileChooser( final String[] fileExtension, final String fileDescription, final String string ) {
        super( fileExtension, fileDescription, string );
        this.openFileChooser( ReadXplorerFileChooser.SAVE_DIALOG );
    }


    @Messages( { "NoStringError=Given data is not a string and cannot be saved as fasta.",
                 "Error=An error occured during the file saving process.",
                 "ProgressName=Storing data in file...",
                 "SuccessMsg=Data successfully stored in ",
                 "SuccessHeader=Success" } )
    @Override
    public void save( final String fileLocation ) {

        if( data instanceof String ) {
            this.progressHandle = ProgressHandleFactory.createHandle( Bundle.ProgressName() );
            final String dataString = (String) data;
            this.progressHandle.start();

            Thread exportThread = new Thread( new Runnable() {

                @Override
                public void run() {

                    try( final BufferedWriter outputWriter = new BufferedWriter( new FileWriter( fileLocation ) ); ) {
                        outputWriter.write( dataString );
                        NotificationDisplayer.getDefault().notify( Bundle.SuccessHeader(), new ImageIcon(), Bundle.SuccessMsg() + fileLocation, null );
                    }
                    catch( IOException | MissingResourceException | HeadlessException e ) {
                        JOptionPane.showMessageDialog( StoreStringFileChooser.this, Bundle.Error() );
                    }
                    progressHandle.finish();
                }


            } );
            exportThread.start();

        }
        else {
            JOptionPane.showMessageDialog( this, Bundle.NoStringError() );
        }
    }


    @Override
    public void open( String fileLocation ) {
        //this is a save dialog, so nothing to do here
        //refactor when open option is needed and add funcationality
    }


}
