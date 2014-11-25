package de.cebitec.readXplorer.api.objects;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * A JPanel implementing the NewJobDialog interface, which is an interface for
 * all dialogs that create new jobs and need some required info set before they
 * can finish successfully. It adds the functionality of creating a document
 * listener, which then checks if the required information for the job is set
 * for any document listener method.
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class JobPanel extends JPanel implements NewJobDialogI {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * @return <cc>true</cc>, if all required information is set, <cc>false</cc> 
     * otherwise.
     */
    @Override
    public abstract boolean isRequiredInfoSet();
    
    /**
     * @return A document listener calling {@link isRequiredInfoSet()} in all
     * its actions.
     */
    public DocumentListener createDocumentListener() {
        return new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                isRequiredInfoSet();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                isRequiredInfoSet();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                isRequiredInfoSet();
            }
        };
    }
    
    /**
     * @return a list selection listener calling {@link isRequiredInfoSet()} in 
     * its valueChanged action
     */
    public ListSelectionListener createListSelectionListener() {
        return new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                isRequiredInfoSet();
            }
        };
    }
    
    /**
     * 
     * @param table the table to which this listener is added to determine row
     * and column of the MouseEvent
     * @param column the column to which this listener listens
     * @return A MouseAdapter calling {@link isRequiredInfoSet()} on the given
     * column in its mouseClicked action.
     */
    public MouseAdapter createMouseClickListener(final JTable table, final int column) {
        return new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent evt) {
                int row = table.rowAtPoint(evt.getPoint());
                int col = table.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == column) {
                    isRequiredInfoSet();
                }
            }
        };
    }
    
//    /**
//     * @return A cell editor listener calling {@link isRequiredInfoSet()} in all
//     * its actions.
//     */
//    public TableModelListener createTableModelListener() {
//        return new TableModelListener() {
//            @Override
//            public void tableChanged(TableModelEvent e) {
//                isRequiredInfoSet();
//            }
//        };
//    }
//
//    /**
//     * @return A cell editor listener calling {@link isRequiredInfoSet()} in all
//     * its actions.
//     */
//    public CellEditorListener createCellEditorListener() {
//        return new CellEditorListener() {
//            @Override
//            public void editingStopped(ChangeEvent e) {
//                isRequiredInfoSet();
//            }
//
//            @Override
//            public void editingCanceled(ChangeEvent e) {
//                isRequiredInfoSet();
//            }
//        };
//    }
    
}
