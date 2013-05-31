package de.cebitec.vamp.ui.visualisation.reference;

import de.cebitec.vamp.databackend.dataObjects.PersistantFeature;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dkramer
 */
public class FeatureTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1L;

    PersistantFeature[] featureData;
    private String[] columnNames = {"Feature", "Type", "Gene", "Product", "EC-Number"};
    private Object[][] data;

    public FeatureTableModel(PersistantFeature[] featureData) {
        this.featureData = featureData;
        this.data = new Object[featureData.length][columnNames.length];
        fillData();
    }

    private void fillData() {
        int counter = -1;
        this.data = new Object[featureData.length][columnNames.length];
        for (PersistantFeature feature : this.featureData) {
            counter++;
            this.data[counter][0] = feature;
            this.data[counter][1] = feature.getType();
            this.data[counter][2] = feature.getFeatureName();
            this.data[counter][3] = feature.getProduct();
            this.data[counter][4] = feature.getEcNumber();
        }
    }

    @Override
    public int getRowCount() {
        return featureData.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (this.data.length == 0){
            return null;
        }
        return this.data[row][col];
    }

    @Override
    public Class getColumnClass(int c) {
        if (getValueAt(0, c) != null) {
            if (c == 0) {
                return PersistantFeature.class;
            }
            return getValueAt(0, c).getClass();
        } else {
            return Object.class;
        }
    }
}
