package de.cebitec.readXplorer.view.tableVisualization;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.util.PositionUtils;
import de.cebitec.readXplorer.view.dataVisualisation.BoundsInfoManager;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JTable;

/**
 * Provides some basic table utils for ReadXplorer.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public class TableUtils {

    private TableUtils() {
        //do not instantiate
    }
    
    /**
     * Transforms the selected row index in the table view to the selected 
     * index of the underlying table model (in case the results are sorted, 
     * they can be different). If the transformation is not possible, it returns
     * -1
     * @param table the table for which the selected model row is needed
     * @return The selected model row index or -1, if the index is out of 
     * bounds or cannot be calculated
     */
    public static int getSelectedModelRow(JTable table) {
        int wantedModelIdx = -1;
        DefaultListSelectionModel model = (DefaultListSelectionModel) table.getSelectionModel();
        int selectedView = model.getLeadSelectionIndex();

        if (table.getModel().getRowCount() > selectedView && selectedView >= 0) {
            int selectedModelIdx = table.convertRowIndexToModel(selectedView);

            if (table.getModel().getRowCount() > selectedModelIdx) {
                wantedModelIdx = selectedModelIdx;
            }
        }
        return wantedModelIdx;
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
        int selectedModelRow = TableUtils.getSelectedModelRow(table);
        if (selectedModelRow > -1) {
                Object value = table.getModel().getValueAt(selectedModelRow, featureColumnIndex);
                
                if (value instanceof PersistantFeature) {
                    PersistantFeature feature = (PersistantFeature) value;
                    int pos = feature.isFwdStrand() ? feature.getStart() : feature.getStop();
                    bim.navigatorBarUpdated(pos);
                    
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
