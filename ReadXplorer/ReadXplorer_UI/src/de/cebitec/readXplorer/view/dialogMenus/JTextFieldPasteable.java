package de.cebitec.readXplorer.view.dialogMenus;

import de.cebitec.readXplorer.util.GeneralUtils;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

/**
 * @author -Rolf Hilker-
 * 
 * Extends the ordinary JTextField by overwriting the CTRL+V shortcut to paste
 * the current system clipboard content. 
 */
public class JTextFieldPasteable extends JTextField {
    
    /**
     * Extends the ordinary JTextField by overwriting the CTRL+V shortcut to
     * paste the current system clipboard content.
     */
    public JTextFieldPasteable() {
        this.setPasteBehaviour();
    }
    
    /**
     * Updates the paste behaviour of the CTRL+V shortcut to paste the current 
     * system clipboard content.
     */
    private void setPasteBehaviour() {
        Action action = createAction();
        JTextFieldPasteable.this.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), "Paste");
        JTextFieldPasteable.this.getActionMap().put("Paste", action);
    }
    
    /**
     * @return Creates and returns the action that updates the paste behaviour 
     * of the CTRL+V shortcut to paste the current system clipboard content.
     */
    private Action createAction() {
        
        Action action = new Action() {

            @Override
            public void actionPerformed(final ActionEvent event) {
                JTextFieldPasteable.this.setText(GeneralUtils.getClipboardContents(JTextFieldPasteable.this));
            }

            /**
             * Method not implemented yet, returns null.
             */
            @Override
            public Object getValue(String key) {
                return null;
            }

            /**
             * Method not implemented yet, does not add anything.
             */
            @Override
            public void putValue(String key, Object value) {
            }

            /**
             * Method not implemented yet, does not set anything.
             */
            @Override
            public void setEnabled(boolean b) {
            }

            /**
             * Method not implemented yet, always returns true.
             */
            @Override
            public boolean isEnabled() {
                return true;
            }

            /**
             * Method not implemented yet, does not set anything.
             */
            @Override
            public void addPropertyChangeListener(PropertyChangeListener listener) {
            }

            /**
             * Method not implemented yet, does not delete anything.
             */
            @Override
            public void removePropertyChangeListener(PropertyChangeListener listener) {
            }
        };
        
        return action;
    }
    
}
