package de.cebitec.vamp.api.objects;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Rolf Hilker <rhilker at cebitec.uni-bielefeld.de>
 */
public abstract class JobPanel extends JPanel implements NewJobDialogI {
    
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

    @Override
    public abstract boolean isRequiredInfoSet();
    
}
