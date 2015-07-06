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

package de.cebitec.readxplorer.ui.dialogmenus;


import de.cebitec.readxplorer.databackend.connector.ProjectConnector;
import de.cebitec.readxplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentChromosome;
import de.cebitec.readxplorer.databackend.dataobjects.PersistentReference;
import de.cebitec.readxplorer.ui.datavisualisation.abstractviewer.AbstractViewer;
import de.cebitec.readxplorer.utils.Observer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;


/**
 * Class containing helper methods for visualizing and switching between
 * chromosomes.
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
public class ChromosomeVisualizationHelper {

    /**
     * Class containing helper methods for visualizing and switching between
     * chromosomes.
     */
    public ChromosomeVisualizationHelper() {
    }


    /**
     * Update the list of chromosomes for the given chromSelectionBox with
     * the chromosomes belonging to the given reference.
     * <p>
     * @param chromSelectionBox JComboBox with the list of chromosomes belonging
     *                          to the given reference, which allows selection of the active chromosome.
     * @param refGenome         the reference whose chromosomes shall be handled
     *                          in the
     *                          selection box from now on.
     */
    public void updateChromBoxContent( JComboBox<PersistentChromosome> chromSelectionBox,
                                       PersistentReference refGenome ) {
        ReferenceConnector connector = ProjectConnector.getInstance().getRefGenomeConnector( refGenome.getId() );
        PersistentChromosome[] chroms = new PersistentChromosome[0];
        chroms = connector.getChromosomesForGenome().values().toArray( chroms );
        chromSelectionBox.setModel( new DefaultComboBoxModel<>( chroms ) );
        chromSelectionBox.setSelectedItem( refGenome.getActiveChromosome() );
    }


    /**
     * Helper method to create a new JComboBox with the list of chromosomes
     * belonging to the given reference, which allows selection of the active
     * chromosome. The viewer is needed to update the bounds information for the
     * viewers connected to the given reference. The observer is already
     * registered in the refGenome here.
     * <p>
     * @param chromSelectionBox JComboBox with the list of chromosomes belonging
     *                          to the given reference, which allows selection of the active chromosome.
     * @param refGenome         the reference for which the selection box shall
     *                          be
     *                          created.
     * <p>
     * @return A new JComboBox with the list of chromosomes belonging to the
     *         given reference, which allows selection of the active chromosome.
     */
    public ChromComboObserver createChromBoxWithObserver( JComboBox<PersistentChromosome> chromSelectionBox,
                                                          PersistentReference refGenome ) {
        this.updateChromBoxContent( chromSelectionBox, refGenome );
        ChromComboObserver chromComboObserver = new ChromComboObserver( chromSelectionBox, refGenome );
        refGenome.registerObserver( chromComboObserver );
        return chromComboObserver;
    }


    /**
     * A listener for changes in the chromosome selection.
     */
    public class ChromosomeListener implements ActionListener {

        private final JComboBox<PersistentChromosome> chromSelectionBox;
        private AbstractViewer viewer;


        /**
         * A listener for changes in the chromosome selection in the given
         * chromosome selection box.
         * <p>
         * @param chromSelectionBox the box which can switch the active
         *                          chromosome
         * @param viewer            the viewer, which shall be updated with the
         *                          new active
         *                          chromosome.
         */
        public ChromosomeListener( final JComboBox<PersistentChromosome> chromSelectionBox, final AbstractViewer viewer ) {
            this.chromSelectionBox = chromSelectionBox;
            this.chromSelectionBox.addActionListener( this );
            this.viewer = viewer;
        }


        @Override
        public void actionPerformed( ActionEvent e ) {
            PersistentChromosome activeChrom = (PersistentChromosome) chromSelectionBox.getSelectedItem();
            viewer.getBoundsInformationManager().chromosomeChanged( activeChrom.getId() );
        }


        /**
         * @param viewer Sets this viewer as the viewer to update, when the
         *               chromosome selection has changed.
         */
        public void setViewer( AbstractViewer viewer ) {
            this.viewer = viewer;
        }


    }


    /**
     * An observer, which updates a given combo box with chromosomes,
     * in case the chromosome was changed from elsewhere.
     */
    public class ChromComboObserver implements Observer {

        private final JComboBox<PersistentChromosome> chromSelectionBox;
        private PersistentReference refGenome;


        /**
         * An observer, which updates a given combo box with chromosomes, in
         * case the chromosome was changed from elsewhere.
         * <p>
         * @param chromSelectionBox The box to which the observer shall be
         *                          connected
         * @param refGenome         The reference genome, to which the observer
         *                          is added
         */
        public ChromComboObserver( final JComboBox<PersistentChromosome> chromSelectionBox, final PersistentReference refGenome ) {
            this.chromSelectionBox = chromSelectionBox;
            this.refGenome = refGenome;
        }


        @Override
        public void update( Object args ) {
            chromSelectionBox.setSelectedItem( refGenome.getActiveChromosome() );
            chromSelectionBox.repaint();
        }


        /**
         * @param refGenome Sets the reference genome, for which the connected
         *                  JComboBox is updated.
         */
        public void setRefGenome( PersistentReference refGenome ) {
            this.refGenome = refGenome;
        }


    }

}
