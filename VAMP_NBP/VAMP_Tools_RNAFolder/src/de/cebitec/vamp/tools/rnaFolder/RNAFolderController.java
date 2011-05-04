package de.cebitec.vamp.tools.rnaFolder;

import de.cebitec.vamp.tools.rnaFolder.rnamovies.MoviePane;
import de.cebitec.vamp.tools.rnaFolder.rnamovies.RNAMovies;
import de.cebitec.vamp.view.dialogMenus.RNAFolderI;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * Controls and creates the content of a RNAFolderTopComponent.
 *
 * @author Rolf Hilker
 */
@ServiceProvider(service = RNAFolderI.class)
public class RNAFolderController implements RNAFolderI {

    /**The RNAFolderTopComponent holding all tabs with folded RNAs in a RNAMovie.*/
    private RNAFolderTopComponent rnaFolderTopComp;

    public RNAFolderController() {
        //Nothing to do here.
    }

    @Override
    public void showRNAFolderView(final String sequenceToFold, final String header) {

        try { //new header because RNA movies cannot cope with spaces
            String rnaMoviesHeader = header.replace(" ", "_");
            String foldedSequence = RNAFoldCaller.callRNAFolder(sequenceToFold, rnaMoviesHeader);
            //String foldingEnergy = foldedSequence.substring(foldedSequence.indexOf(" ")+1);
            foldedSequence = foldedSequence.substring(0, foldedSequence.lastIndexOf("(")-1);
            RNAMovies rnaMovies = new RNAMovies();
            rnaMovies.setData(foldedSequence);
            MoviePane rnaMovie = (MoviePane) rnaMovies.getMovie();

            this.rnaFolderTopComp = (RNAFolderTopComponent) WindowManager.getDefault().findTopComponent("RNAFolderTopComponent");
            this.rnaFolderTopComp.openRNAMovie(rnaMovie, header);
        } catch (RNAFoldException ex) {
            NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notify(nd);
        }
    }
}
