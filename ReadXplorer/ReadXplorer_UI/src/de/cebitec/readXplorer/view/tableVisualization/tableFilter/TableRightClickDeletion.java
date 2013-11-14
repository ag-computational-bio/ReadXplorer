package de.cebitec.readXplorer.view.tableVisualization.tableFilter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/**
 * A MouseAdapter, which offers a row deletion option for a JTable. An instance
 * of this class must be added as a listener to the Table that should have the
 * possibility of deletion of rows. Only tables using a model extending
 * DefaultTableModel can be used!
 *
 * @param <E> the table model, which has to extend the DefaultTableModel.
 *
 * @author jritter
 */
public class TableRightClickDeletion<E extends DefaultTableModel> extends MouseAdapter {

    private JPopupMenu popup = new JPopupMenu();
    private JTable lastTable;
    /**
     * Stores the original TableModel.
     */
    private int lastSelectedRow;
    private JMenuItem removeRow;
    private JMenuItem markRow;

    /**
     * A MouseAdapter, which offers a row deletion option for a JTable. An
     * instance of this class must be added as a listener to the Table that
     * should have the possibility of deletion of rows. Only tables using a
     * model extending DefaultTableModel can be used!
     *
     * @param <E> the table model, which has to extend the DefaultTableModel.
     */
    public TableRightClickDeletion() {
        init();
    }

    /**
     * Initialize the JMenuItems, adds the actionListener with actionPerformes
     * and adds them into JPopupMenu.
     */
    private void init() {
        removeRow = new JMenuItem("remove this row");
        removeRow.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) lastTable.getModel();

                try {
                    if (lastTable.isRowSelected(lastSelectedRow)) {
                        model.removeRow(lastSelectedRow);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please select a row.");
                }
            }
        });

        popup.add(removeRow);

//        markRow = new JMenuItem("mark this row");
//        markRow.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                DefaultTableModel model = (DefaultTableModel) lastTable.getModel();
//                int row = lastSelectedRow;
//                int columsCount = lastTable.getColumnCount();
//
//                if (lastTable.isRowSelected(row)) {
//                    lastTable.setSelectionBackground(Color.yellow);
                    
//                    for (int i = 0; i < columsCount; i++) {
//                        TableCellRenderer cellRenderer = (DefaultTableCellRenderer) lastTable.getCellRenderer(row, i);
//                        
//                        JComponent comp = (JComponent) lastTable.prepareRenderer(cellRenderer, row, i);
//                            comp.setBackground(Color.yellow);
//                    } 
//                }
//            }
//        });
//
//        popup.add(markRow);

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
            lastTable = (JTable) e.getSource();
            lastSelectedRow = lastTable.getSelectedRow();
            if (lastTable.getModel().getRowCount() > 0) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        }
    }
}
