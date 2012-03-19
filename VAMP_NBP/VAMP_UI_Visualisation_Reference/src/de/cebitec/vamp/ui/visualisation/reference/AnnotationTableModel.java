/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.vamp.ui.visualisation.reference;

import de.cebitec.vamp.databackend.dataObjects.PersistantAnnotation;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author dkramer
 */
class AnnotationTableModel extends AbstractTableModel {

    PersistantAnnotation[] annotationData;
    private String[] columnNames = {"Annotation", "Gene", "Product", "EC-Number"};
    private Object[][] data;

    public AnnotationTableModel(PersistantAnnotation[] annotationData) {
        this.annotationData = annotationData;
        this.data = new Object[annotationData.length][columnNames.length];
        fillData();
    }

    private void fillData() {
        int counter = -1;
        this.data = new Object[annotationData.length][columnNames.length];
        for (PersistantAnnotation annotation : this.annotationData) {
            counter++;
            this.data[counter][0] = annotation;
            this.data[counter][1] = annotation.getGeneName();
            this.data[counter][2] = annotation.getProduct();
            this.data[counter][3] = annotation.getEcNumber();
        }
    }

    @Override
    public int getRowCount() {
        return annotationData.length;
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
            if(c==0){
                return PersistantAnnotation.class;
            }
            return getValueAt(0, c).getClass();
        } else {
            return Object.class;
        }
    }
}
