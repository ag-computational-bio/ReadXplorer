package de.cebitec.readXplorer.tools.rnaFolder;

import de.cebitec.readXplorer.tools.rnaFolder.rnamovies.MoviePane;
import de.cebitec.readXplorer.tools.rnaFolder.rnamovies.RNAMovies;
import de.cebitec.readXplorer.view.dialogMenus.RNAFolderI;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
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

        rnaFolderTopComp = (RNAFolderTopComponent) WindowManager.getDefault().findTopComponent("RNAFolderTopComponent");
        rnaFolderTopComp.open();
        rnaFolderTopComp.requestActive();
        
        Thread rnaFoldThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try { //new header because RNA movies cannot cope with spaces
                    ProgressHandle progressHandle = ProgressHandleFactory.createHandle(NbBundle.getMessage(RNAFolderController.class, "RNA_Folder-Progress"));
                    progressHandle.start();
                    String rnaMoviesHeader = header.replace(" ", "_");
                    String foldedSequence = RNAFoldCaller.callRNAFolder(sequenceToFold, rnaMoviesHeader);
                    //for testing purposes in offline mode:
//            String foldedSequence = ">tRNA-like structure from turnip yellow mosaic virus\n"+
//"UUAGCUCGCCAGUUAGCGAGGUCUGUCCCCACACGACAGAUAAUCGGGUGCAACUCCCGCCCCUUUUCCGAGGGUCAUCGGAACCA\n"+
//"....(((((......)))))(((((((.......)))))))....(((.((.......)))))..((((((......))))))... (-27.80)";
                    //String foldingEnergy = foldedSequence.substring(foldedSequence.indexOf(" ")+1);
                    foldedSequence = foldedSequence.substring(0, foldedSequence.lastIndexOf('(') - 1);
                    RNAMovies rnaMovies = new RNAMovies();
                    rnaMovies.setData(foldedSequence);
                    MoviePane rnaMovie = (MoviePane) rnaMovies.getMovie();

                    rnaFolderTopComp.openRNAMovie(rnaMovie, header);
                    progressHandle.finish();

                } catch (RNAFoldException ex) {
                    NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage(), NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            }
        });
        rnaFoldThread.start();

    }
}
