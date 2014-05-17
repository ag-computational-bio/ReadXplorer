/* 
 * Copyright (C) 2014 Rolf Hilker
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
package de.cebitec.readXplorer.util.fileChooser;

import java.io.BufferedWriter;
import java.io.FileWriter;
import javax.swing.JOptionPane;
import org.openide.util.NbBundle;

/**
 * readXplorers fasta file chooser. Contains the save method storing a string in fasta
 * format.
 *
 * @author Rolf Hilker
 */
public class FastaFileChooser extends ReadXplorerFileChooser {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new file chooser for saving a sequence in fasta format.
     * @param fileExtension
     * @param fileDescription 
     * @param sequence
     */
    public FastaFileChooser(final String[] fileExtension, final String fileDescription, final String sequence){
        super(fileExtension, fileDescription, sequence);
        this.openFileChooser(ReadXplorerFileChooser.SAVE_DIALOG);
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
