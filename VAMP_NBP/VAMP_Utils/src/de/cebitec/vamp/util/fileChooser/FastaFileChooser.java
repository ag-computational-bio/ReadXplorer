package de.cebitec.vamp.util.fileChooser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;

/**
 * Vamps fasta file chooser. Contains the save method storing a string in fasta
 * format.
 *
 * @author Rolf Hilker
 */
public class FastaFileChooser extends VampFileChooser {

    /**
     * Creates a new file chooser for saving a sequence in fasta format.
     * @param fileExtension
     * @param sequence
     */
    public FastaFileChooser(final String fileExtension, final String sequence){
        super(VampFileChooser.SAVE_DIALOG, fileExtension, sequence);
    }


    @Override
    public void save(String fileLocation) {
        try {
            if (this.data instanceof String){
                String output = (String) this.data;
                final FileWriter fileStream = new FileWriter(fileLocation);
                final BufferedWriter outputWriter = new BufferedWriter(fileStream);
                outputWriter.write(output);
                outputWriter.close();
            } else {
                JOptionPane.showMessageDialog(this, NbBundle.getMessage(FastaFileChooser.class,
                        "FastaFileChooser.NoStringError"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, NbBundle.getMessage(FastaFileChooser.class,
                        "FastaFileChooser.Error"));
        }
    }

    @Override
    public void open(String fileLocation) {
        //this is a save dialog, so nothing to do here
        //refactor when open option is needed and add funcationality
    }

}
