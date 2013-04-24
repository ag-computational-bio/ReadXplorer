package de.cebitec.vamp.util;

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
import org.openide.util.Exceptions;

/**
 *
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
    private JMenuItem numberColumnLower;
    private JMenuItem numberColumnHigher;
    private JMenuItem stringColumn;
    private JMenuItem reset;
    private Class<E> classType;

    /**
     * Offers a filter for the columns of a table. An instance of this class
     * must be added as an listener to the TableHeader of the Tabel that should
     * be filtered. Only Tables using a model extending DefaultTableModel can be
     * used!
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
        reset.setEnabled(false);
        originalTableModel = null;
    }

    private void init() {
        numberColumnLower = new JMenuItem("Remove values lower as...");
        numberColumnLower.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = openPopUp("Remove values lower as: ");
                if (input != null) {
                    input = input.replace(",", ".");
                    try {
                        Double cutoff = Double.parseDouble(input);
                        E newModel = filterValuesLowerAs(
                                (E) lastTable.getModel(), cutoff, lastSelectedColumn);
                        setNewTableModel(newModel);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please insert a valid value.");
                    }
                }
            }
        });
        popup.add(numberColumnLower);

        numberColumnHigher = new JMenuItem("Remove values higher as...");
        numberColumnHigher.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = openPopUp("Remove values higher as: ");
                if (input != null) {
                    input = input.replace(",", ".");
                    try {
                        Double cutoff = Double.parseDouble(input);
                        E newModel = filterValuesHigherAs(
                                (E) lastTable.getModel(), cutoff, lastSelectedColumn);
                        setNewTableModel(newModel);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Please insert a valid value.");
                    }
                }
            }
        });
        popup.add(numberColumnHigher);

        stringColumn = new JMenuItem("Set pattern filter");
        stringColumn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = openPopUp("Show only rows that contain the pattern: ");
                if (input != null) {
                    try {
                        Pattern.compile(input);
                        E newModel = filterRegExp(
                                (E) lastTable.getModel(), input, lastSelectedColumn);
                        setNewTableModel(newModel);
                    } catch (PatternSyntaxException pse) {
                        JOptionPane.showMessageDialog(null, "Please enter a valid pattern.");
                    }
                }
            }
        });
        popup.add(stringColumn);

        reset = new JMenuItem("Reset all filters");
        reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setNewTableModel(originalTableModel);
                resetOriginalTableModel();
            }
        });
        popup.add(reset);

        numberColumnLower.setEnabled(false);
        numberColumnHigher.setEnabled(false);
        stringColumn.setEnabled(false);
        reset.setEnabled(false);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            lastTable = ((JTable) ((JTableHeader) e.getSource()).getTable());
            lastSelectedColumn = lastTable.columnAtPoint(e.getPoint());
            if (lastTable.getModel().getRowCount() > 0) {
                Object testValue = lastTable.getModel().getValueAt(0, lastSelectedColumn);
                if (testValue instanceof Number) {
                    numberColumnLower.setEnabled(true);
                    numberColumnHigher.setEnabled(true);
                    stringColumn.setEnabled(false);
                }
                if (testValue instanceof String) {
                    numberColumnLower.setEnabled(false);
                    numberColumnHigher.setEnabled(false);
                    stringColumn.setEnabled(true);
                }
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }

    private void setNewTableModel(E newTableModel) {
        E tableModel = (E) lastTable.getModel();
        if (originalTableModel == null) {
            E tmpModel = prepareNewTableModel(tableModel);
            for (Iterator<Vector> it = tableModel.getDataVector().iterator(); it.hasNext();) {
                Vector row = it.next();
                tmpModel.addRow(row);
            }
            this.originalTableModel = tmpModel;
            reset.setEnabled(true);
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

    private E filterValuesHigherAs(E tm, Double cutoff, int column) {
        boolean noRow = true;
        E ret = prepareNewTableModel(tm);
        Vector dataVector = tm.getDataVector();
        for (Iterator<Vector> it = dataVector.iterator(); it.hasNext();) {
            Vector row = it.next();
            Object currentEntry = row.get(column);
            if (currentEntry instanceof Integer) {
                Integer currentEntryValue = (Integer) currentEntry;
                if (currentEntryValue < cutoff) {
                    ret.addRow(row);
                    noRow = false;
                }
            }
            if (currentEntry instanceof Double) {
                Double currentEntryValue = (Double) currentEntry;
                if (currentEntryValue < cutoff) {
                    ret.addRow(row);
                    noRow = false;
                }
            }
        }
        if (noRow) {
            ret.addRow(new Vector());
        }
        return ret;
    }

    private E filterValuesLowerAs(E tm, Double cutoff, int column) {
        boolean noRow = true;
        E ret = prepareNewTableModel(tm);
        Vector dataVector = tm.getDataVector();
        for (Iterator<Vector> it = dataVector.iterator(); it.hasNext();) {
            Vector row = it.next();
            Object currentEntry = row.get(column);
            if (currentEntry instanceof Integer) {
                Integer currentEntryValue = (Integer) currentEntry;
                if (currentEntryValue > cutoff) {
                    ret.addRow(row);
                    noRow = false;
                }
            }
            if (currentEntry instanceof Double) {
                Double currentEntryValue = (Double) currentEntry;
                if (currentEntryValue > cutoff) {
                    ret.addRow(row);
                    noRow = false;
                }
            }
        }
        if (noRow) {
            ret.addRow(new Vector());
        }
        return ret;
    }

    private E filterRegExp(E tm, String pattern, int column) {
        boolean noRow = true;
        E ret = prepareNewTableModel(tm);
        Vector dataVector = tm.getDataVector();
        for (Iterator<Vector> it = dataVector.iterator(); it.hasNext();) {
            Vector row = it.next();
            if (((String) row.get(column)).contains(pattern)) {
                ret.addRow(row);
                noRow = false;
            }
        }
        if (noRow) {
            ret.addRow(new Vector());
        }
        return ret;
    }

    private E prepareNewTableModel(E tm) {
        E ret = null;
        try {
            ret = classType.newInstance();
            String[] columnNames = new String[tm.getColumnCount()];
            for (int i = 0; i < tm.getColumnCount(); i++) {
                columnNames[i] = tm.getColumnName(i);
            }
            ret.setColumnIdentifiers(columnNames);
        } catch (InstantiationException | IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        }
        return ret;
    }
}
