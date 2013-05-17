package de.cebitec.vamp.api.objects;

import javax.swing.JPanel;
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
    
    /**
     * @return <cc>true</cc>, if all required information is set, <cc>false</cc> 
     * otherwise.
     */
    @Override
    public abstract boolean isRequiredInfoSet();
    
    /**
     * @return a document listener calling {@link isRequiredInfoSet()} in all
     * relevant actions
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
     * @return a lsit selection listener calling {@link isRequiredInfoSet()} in 
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
    
}
