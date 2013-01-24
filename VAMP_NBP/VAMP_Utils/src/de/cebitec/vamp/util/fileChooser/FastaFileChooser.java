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
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new file chooser for saving a sequence in fasta format.
     * @param fileExtension
     * @param fileDescription 
     * @param sequence
     */
    public FastaFileChooser(final String[] fileExtension, final String fileDescription, final String sequence){
        super(fileExtension, fileDescription, sequence);
        this.openFileChooser(VampFileChooser.SAVE_DIALOG);
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
