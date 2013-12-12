package de.cebitec.readXplorer.view.dialogMenus;

import de.cebitec.readXplorer.databackend.connector.ProjectConnector;
import de.cebitec.readXplorer.databackend.connector.ReferenceConnector;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantChromosome;
import de.cebitec.readXplorer.databackend.dataObjects.PersistantReference;
import de.cebitec.readXplorer.util.Observer;
import de.cebitec.readXplorer.view.dataVisualisation.abstractViewer.AbstractViewer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

/**
 * Class containing helper methods for visualizing and switching between 
 * chromosomes.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
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
     * @param chromSelectionBox JComboBox with the list of chromosomes belonging
     * to the given reference, which allows selection of the active chromosome.
     * @param refGenome the reference whose chromosomes shall be handled in the 
     * selection box from now on.
     */
    public void updateChromBoxContent(JComboBox<PersistantChromosome> chromSelectionBox, 
                PersistantReference refGenome) {
        ReferenceConnector connector = ProjectConnector.getInstance().getRefGenomeConnector(refGenome.getId());
        PersistantChromosome[] chroms = new PersistantChromosome[0];
        chroms = connector.getChromosomesForGenome().values().toArray(chroms);
        chromSelectionBox.setModel(new DefaultComboBoxModel<>(chroms));
        chromSelectionBox.setSelectedItem(refGenome.getActiveChromosome());
    }
    
    /**
     * Helper method to create a new JComboBox with the list of chromosomes
     * belonging to the given reference, which allows selection of the active
     * chromosome. The viewer is needed to update the bounds information for the 
     * viewers connected to the given reference. The observer is already 
     * registered in the refGenome here.
     * @param chromSelectionBox JComboBox with the list of chromosomes belonging
     * to the given reference, which allows selection of the active chromosome.
     * @param refGenome the reference for which the selection box shall be 
     * created.
     * @return A new JComboBox with the list of chromosomes belonging to the
     * given reference, which allows selection of the active chromosome.
     */
    public ChromComboObserver createChromBoxWithObserver(JComboBox<PersistantChromosome> chromSelectionBox,
                PersistantReference refGenome) {
        this.updateChromBoxContent(chromSelectionBox, refGenome);
        ChromComboObserver chromComboObserver = new ChromComboObserver(chromSelectionBox, refGenome);
        refGenome.registerObserver(chromComboObserver);
        return chromComboObserver;
    }
    
    
    /**
     * A listener for changes in the chromosome selection.
     */
    public class ChromosomeListener implements ActionListener {

        private JComboBox<PersistantChromosome> chromSelectionBox;
        private AbstractViewer viewer;

        /**
         * A listener for changes in the chromosome selection in the given
         * chromosome selection box.
         * @param chromSelectionBox the box which can switch the active
         * chromosome
         * @param viewer the viewer, which shall be updated with the new active
         * chromosome.
         */
        public ChromosomeListener(final JComboBox<PersistantChromosome> chromSelectionBox, final AbstractViewer viewer) {
            this.chromSelectionBox = chromSelectionBox;
            this.chromSelectionBox.addActionListener(this);
            this.viewer = viewer;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            PersistantChromosome activeChrom = (PersistantChromosome) chromSelectionBox.getSelectedItem();
            viewer.getBoundsInformationManager().chromosomeChanged(activeChrom.getId());
        }

        /**
         * @param viewer Sets this viewer as the viewer to update, when the
         * chromosome selection has changed.
         */
        public void setViewer(AbstractViewer viewer) {
            this.viewer = viewer;
        }
    }
    
    
    /**
     * An observer, which updates a given combo box with chromosomes,
     * in case the chromosome was changed from elsewhere.
     */
    public class ChromComboObserver implements Observer {
        
        private JComboBox<PersistantChromosome> chromSelectionBox;
        private PersistantReference refGenome;

        /**
         * An observer, which updates a given combo box with chromosomes, in
         * case the chromosome was changed from elsewhere.
         * @param chromSelectionBox The box to which the observer shall be
         * connected
         * @param refGenome The reference genome, to which the observer is added
         */
        public ChromComboObserver(final JComboBox<PersistantChromosome> chromSelectionBox, final PersistantReference refGenome) {
            this.chromSelectionBox = chromSelectionBox;
            this.refGenome = refGenome;
        }

        @Override
        public void update(Object args) {
            chromSelectionBox.setSelectedItem(refGenome.getActiveChromosome());
            chromSelectionBox.repaint();
        }

        /**
         * @param refGenome Sets the reference genome, for which the connected
         * JComboBox is updated.
         */
        public void setRefGenome(PersistantReference refGenome) {
            this.refGenome = refGenome;
        }
    }
    
}
