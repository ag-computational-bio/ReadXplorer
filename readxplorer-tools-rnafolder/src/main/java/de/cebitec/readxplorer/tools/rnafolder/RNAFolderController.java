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

package de.cebitec.readxplorer.tools.rnafolder;


import de.cebitec.readxplorer.tools.rnafolder.rnamovies.MoviePane;
import de.cebitec.readxplorer.tools.rnafolder.rnamovies.RNAMovies;
import de.cebitec.readxplorer.ui.dialogmenus.RNAFolderI;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;


/**
 * Controls and creates the content of a RNAFolderTopComponent.
 * <p>
 * @author Rolf Hilker
 */
@ServiceProvider( service = RNAFolderI.class )
public class RNAFolderController implements RNAFolderI {

    /**
     * The RNAFolderTopComponent holding all tabs with folded RNAs in a
     * RNAMovie.
     */
    private RNAFolderTopComponent rnaFolderTopComp;


    public RNAFolderController() {
        //Nothing to do here.
    }


    @Override
    public void showRNAFolderView( final String sequenceToFold, final String header ) {

        rnaFolderTopComp = (RNAFolderTopComponent) WindowManager.getDefault().findTopComponent( "RNAFolderTopComponent" );
        rnaFolderTopComp.open();
        rnaFolderTopComp.requestActive();

        Thread rnaFoldThread = new Thread( new Runnable() {
            @Override
            public void run() {
                ProgressHandle progressHandle = ProgressHandleFactory.createHandle( NbBundle.getMessage( RNAFolderController.class, "RNA_Folder-Progress" ) );
                progressHandle.start();
                try { //new header because RNA movies cannot cope with spaces
                    String rnaMoviesHeader = header.replace( " ", "_" );
                    String foldedSequence = RNAFoldCaller.callRNAFolder( sequenceToFold, rnaMoviesHeader );
                    //for testing purposes in offline mode:
//            String foldedSequence = ">tRNA-like structure from turnip yellow mosaic virus\n"+
//"UUAGCUCGCCAGUUAGCGAGGUCUGUCCCCACACGACAGAUAAUCGGGUGCAACUCCCGCCCCUUUUCCGAGGGUCAUCGGAACCA\n"+
//"....(((((......)))))(((((((.......)))))))....(((.((.......)))))..((((((......))))))... (-27.80)";
                    //String foldingEnergy = foldedSequence.substring(foldedSequence.indexOf(" ")+1);
                    foldedSequence = foldedSequence.substring( 0, foldedSequence.lastIndexOf( '(' ) - 1 );
                    RNAMovies rnaMovies = new RNAMovies();
                    rnaMovies.setData( foldedSequence );
                    MoviePane rnaMovie = (MoviePane) rnaMovies.getMovie();

                    rnaFolderTopComp.openRNAMovie( rnaMovie, header );

                } catch( RNAFoldException ex ) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message( ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE );
                    DialogDisplayer.getDefault().notify( nd );
                }
                progressHandle.finish();
            }


        } );
        rnaFoldThread.start();

    }


}
