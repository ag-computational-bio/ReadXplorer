package de.cebitec.vamp.view.tableVisualization.tableFilter;

import de.cebitec.vamp.util.GenerateRowSorter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

/**
 * A MouseAdapter, which offers a filter for the columns of a table. An
 * instance of this class must be added as a listener to the TableHeader of the
 * table that should be filtered. Only tables using a model extending
 * DefaultTableModel can be used!
 * @param <E> the table model, which has to extend the DefaultTableModel.
 * @author kstaderm
 */
public class TableRightClickFilter<E extends DefaultTableModel> extends MouseAdapter {

    private JPopupMenu popup = new JPopupMenu();
    private JTable lastTable;
    /**
     * Stores the original TableModel.
     */
    private E originalTableModel = null;
    private int lastSelectedColumn;
    private JMenuItem numberColumnLowerItem;
    private JMenuItem numberColumnHigherItem;
    private JMenuItem stringColumnItem;
    private JMenuItem resetItem;
    private Class<E> classType;

    /**
     * A MouseAdapter, which offers a filter for the columns of a table. An
     * instance of this class must be added as a listener to the TableHeader of
     * the table that should be filtered. Only tables using a model extending
     * DefaultTableModel can be used!
     * @param classType the type of the table model, which has to extend the DefaultTableModel.
     */
    public TableRightClickFilter(Class<E> classType) {
        this.classType = classType;
        init();
    }

    /**
     * If a filtered table is changed externally this method must be called. If
     * not the filter will not know that there is a new original TableModel.
     */
    public void resetOriginalTableModel() {
        resetItem.setEnabled(false);
        originalTableModel = null;
    }

    /**
     * Initializes the filter.
     */
    private void init() {
        numberColumnLowerItem = new JMenuItem("Remove values smaller than...");
        numberColumnLowerItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = openPopUp("Remove values smaller than: ");
                if (input != null) {
                    input = input.replace(",", ".");
                    try {
                        Double cutoff = Double.parseDouble(input);
                        E newModel = filterValuesSmallerThan(
                                (E) lastTable.getModel(), cutoff, lastSelectedColumn);
                        setNewTableModel(newModel);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please insert a valid number value.");
                    }
                }
            }
        });
        popup.add(numberColumnLowerItem);

        numberColumnHigherItem = new JMenuItem("Remove values larger than...");
        numberColumnHigherItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = openPopUp("Remove values larger than: ");
                if (input != null) {
                    input = input.replace(",", ".");
                    try {
                        Double cutoff = Double.parseDouble(input);
                        E newModel = filterValuesLargerThan(
                                (E) lastTable.getModel(), cutoff, lastSelectedColumn);
                        setNewTableModel(newModel);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please insert a valid value.");
                    }
                }
            }
        });
        popup.add(numberColumnHigherItem);

        stringColumnItem = new JMenuItem("Set pattern filter");
        stringColumnItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = openPopUp("Only show rows with this pattern: ");
                if (input != null) {
                    try {
                        Pattern.compile(input);
                        E newModel = filterRegExp((E) lastTable.getModel(), input, lastSelectedColumn);
                        setNewTableModel(newModel);
                    } catch (PatternSyntaxException pse) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid pattern.");
                    }
                }
            }
        });
        popup.add(stringColumnItem);

        resetItem = new JMenuItem("Reset all filters");
        resetItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNewTableModel(originalTableModel);
                resetOriginalTableModel();
            }
        });
        popup.add(resetItem);

        numberColumnLowerItem.setEnabled(false);
        numberColumnHigherItem.setEnabled(false);
        stringColumnItem.setEnabled(false);
        resetItem.setEnabled(false);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            lastTable = ((JTableHeader) e.getSource()).getTable();
            lastSelectedColumn = lastTable.columnAtPoint(e.getPoint());
            if (lastTable.getModel().getRowCount() > 0) {
                Object testValue = lastTable.getModel().getValueAt(0, lastSelectedColumn);
                if (testValue instanceof Number) {
                    numberColumnLowerItem.setEnabled(true);
                    numberColumnHigherItem.setEnabled(true);
                    stringColumnItem.setEnabled(false);
                }
                if (testValue instanceof String) {
                    numberColumnLowerItem.setEnabled(false);
                    numberColumnHigherItem.setEnabled(false);
                    stringColumnItem.setEnabled(true);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void setNewTableModel(E newTableModel) {
        E tableModel = (E) lastTable.getModel();
        if (originalTableModel == null) {
            TableFilterUtils<E> utils = new TableFilterUtils<>(classType);
            E tmpModel = utils.prepareNewTableModel(tableModel);
            for (Iterator<Vector> it = tableModel.getDataVector().iterator(); it.hasNext();) {
                Vector row = it.next();
                tmpModel.addRow(row);
            }
            this.originalTableModel = tmpModel;
            resetItem.setEnabled(true);
        }
        lastTable.setModel(newTableModel);
        lastTable.setRowSorter(GenerateRowSorter.createRowSorter(newTableModel));
    }

    private String openPopUp(String message) {
        String input = JOptionPane.showInputDialog(null, message,
                "Value selection",
                JOptionPane.PLAIN_MESSAGE);
        return input;
    }

    private E filterValuesLargerThan(E tableModel, Double cutoff, int column) {
        FilterDoubleValues<E> doubleFilter = new FilterDoubleValues<>(new FilterLargeValues(), classType);
        E filteredTableModel = doubleFilter.filterTable(tableModel, column, cutoff);
        return filteredTableModel;
    }

    private E filterValuesSmallerThan(E tableModel, Double cutoff, int column) {
        FilterDoubleValues<E> doubleFilter = new FilterDoubleValues<>(new FilterSmallValues(), classType);
        E filteredTableModel = doubleFilter.filterTable(tableModel, column, cutoff);
        return filteredTableModel;
    }

    private E filterRegExp(E tm, String pattern, int column) {
        FilterStrings<E> patternFilter = new FilterStrings<>(classType);
        E filteredTableModel = patternFilter.filterTable(tm, column, pattern);
        return filteredTableModel;
    }
}
