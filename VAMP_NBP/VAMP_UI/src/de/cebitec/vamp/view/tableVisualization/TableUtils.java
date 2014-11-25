package de.cebitec.vamp.view.tableVisualization;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import de.cebitec.vamp.util.PositionUtils;
import de.cebitec.vamp.view.dataVisualisation.BoundsInfoManager;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;

/**
 * Provides some basic table utils for VAMP.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TableUtils {

    private TableUtils() {
        //do not instantiate
    }
    
    /**
     * Updates the navigator bar of all viewers to the given position, which
     * might be in String or Integer format or updates the viewers to the start
     * position of a selected PersistantFeature with respect to the strand on
     * which the feature is located.
     * @param table the table whose selected element's position is to be shown
     * @param featureColumnIndex the index of the table model column holding the position
     * @param bim the bounds information manager which should be updated
     */
    public static void showPosition(JTable table, int featureColumnIndex, BoundsInfoManager bim) {
        DefaultListSelectionModel model = (DefaultListSelectionModel) table.getSelectionModel();
        int selectedView = model.getLeadSelectionIndex();

        if (table.getModel().getRowCount() > selectedView && selectedView >= 0) {
            int selectedModel = table.convertRowIndexToModel(selectedView);

            if (table.getModel().getRowCount() > selectedModel) {
                Object value = table.getModel().getValueAt(selectedModel, featureColumnIndex);

                
                if (value instanceof PersistantFeature) {
                    PersistantFeature feature = (PersistantFeature) value;

                    if (feature != null) {
                        int pos = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                        bim.navigatorBarUpdated(pos);
                    }
                    
                } else if (value instanceof Integer) {
                    bim.navigatorBarUpdated((Integer) value);
                    
                } else if (value instanceof String) {
                    String[] posArray = ((String) value).split("\n");
                    try {
                        // Get first position in the array
                        bim.navigatorBarUpdated(PositionUtils.convertPosition(posArray[0]));
                    } catch (NumberFormatException e) {
                        //do nothing if it is no valid number
                    }
                }
            }
        }
    }
    
}
