package de.cebitec.vamp.tools.rnaFolder.rnamovies.util;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;

/**
 * ExportAccessory define some extra Components in Panel to integrate
 * into a Open/Export Dialog.
 *
 * @author Alexander Kaiser <akaiser@techfak.uni-bielefeld.de>
 *         Jan Krueger <jkrueger@techfak.uni-bielefeld.de>
 */
public class ExportAccessory extends JPanel implements PropertyChangeListener{
    
    /* create a private instance of a Logger */
    private static final Logger log = Logger.getLogger(ExportAccessory.class.getName());
    
    private JSpinner from, to, zoom, steps,fps;
    
    private JTextField w, h;
    
    private JCheckBox  trans;
    
    private int ws, hs;
    
    
    
    public ExportAccessory(JFileChooser jfc, int fromVal, int toVal, int size, int ws, int hs) {
        Border border;
        GridBagLayout gbl;
        GridBagConstraints gbc;
        JLabel label;
        
        this.ws = ws;
        this.hs = hs;
        
        border = BorderFactory.createBevelBorder(BevelBorder.LOWERED);
        setBorder(BorderFactory.createTitledBorder(border, "Export Options"));
        
        gbl = new GridBagLayout();
        setLayout(gbl);
        
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        trans = new JCheckBox("Transparent", false);
        gbl.setConstraints(trans, gbc);
        add(trans);
        
//        gbc.gridwidth = 1;
//        label = new JLabel("From:");
//        gbl.setConstraints(label, gbc);
//        add(label);
//        
//        from = new JSpinner(new SpinnerNumberModel(fromVal, 1, size, 1));
//        gbl.setConstraints(from, gbc);
//        add(from);
//        
//        label = new JLabel("To:");
//        gbl.setConstraints(label, gbc);
//        add(label);
//        
//        gbc.gridwidth = GridBagConstraints.REMAINDER;
//        to = new JSpinner(new SpinnerNumberModel(toVal, 1, size, 1));
//        gbl.setConstraints(to, gbc);
//        add(to);
//        
//        gbc.gridwidth = 1;
//        label = new JLabel("Steps:");
//        gbl.setConstraints(label, gbc);
//        add(label);
//        
//        steps = new JSpinner(new SpinnerNumberModel(10, 0, 25, 1));
//        gbl.setConstraints(steps,gbc);
//        add(steps);
//       
        label = new JLabel("Zoom[%]:");
        gbl.setConstraints(label, gbc);
        add(label);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        zoom = new JSpinner(new SpinnerNumberModel(80, 60, 90, 1));
        gbl.setConstraints(zoom, gbc);
        add(zoom);
        
        gbc.gridwidth = 1;
        label = new JLabel("Width:");
        gbl.setConstraints(label, gbc);
        add(label);
        
        w = new JTextField(ws+"",4);
        w.addFocusListener(new MyFocusListener());
        gbl.setConstraints(w, gbc);
        add(w);
         
        label = new JLabel("Height:");
        gbl.setConstraints(label, gbc);
        add(label);
        
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        h = new JTextField(hs+"",4);
        h.addFocusListener(new MyFocusListener());
        gbl.setConstraints(h, gbc);
        add(h);
        
//        gbc.gridwidth = 1;
//        label = new JLabel("FPS:");
//        gbl.setConstraints(label, gbc);
//        add(label);
//       
//        fps = new JSpinner(new SpinnerNumberModel(10, 1, 25, 1));
//        gbl.setConstraints(fps, gbc);
//        add(fps);
        
        /* add PropertyChangeEvent Listener */
        jfc.addPropertyChangeListener(this);
       
    }
    
    public boolean getTransparent() {
        return trans.isSelected();
    }
     
//    public int getFromFrame() {
//        return (Integer)from.getValue();
//    }
//    
//    public int getToFrame() {
//        return (Integer)to.getValue();
//    }
    
    public int getZoom() {
        return (Integer)zoom.getValue();
    }
    
//    /** Returns the number of interpolation steps between two
//     *  structures.
//     *  @return Returns the number of interpolation steps.*/
//    public int getInterpolationSteps() {
//        return (Integer)steps.getValue();
//    }
    
    /** Return the width of the movie/picture.
     * @return Return the width of the movie/picture.*/
    public int getW() {
        return Integer.parseInt(w.getText());
    }
    
    /** Return the height of the movie/picture
     * @return Returns the height of the movie/picture*/
    public int getH() {
        return Integer.parseInt(h.getText());
    }
    
//    /** get the FPS(frame per second) value for the movie
//     *  @return fps */
//    public int getFPS(){
//        return (Integer)fps.getValue();
//    }

    /** implements interface java.beans.PropertyChangeListener 
     *   @see java.beans.PropertyChangeListener.propertyChange */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        
        String prop = evt.getPropertyName();
        if (prop.equals("fileFilterChanged")) {
            /* insert a  try/catch block because on MACOSX adding additional filefilters leads to
             * an NullPointerException; don't know what's exactly the problem here, JK */
            try {
//                if (evt.getNewValue().getClass().getName().equals("de.cebitec.vamp.tools.rnaFolder.rnamovies.actions.Export$GIFFilter")){
//                    fps.setEnabled(true);
//                    steps.setEnabled(true);
//                } else {
//                    fps.setEnabled(false);
//                    steps.setEnabled(false);
//                }          
                if(evt.getNewValue().getClass().getName().equals("de.cebitec.vamp.tools.rnaFolder.rnamovies.actions.Export$JPGFilter")){
                    trans.setEnabled(false);
                } else {
                    trans.setEnabled(true);
                }
            } catch (NullPointerException e){
                log.severe("A NullPointerException occurred during evaluate the PropertyChangeEvent!");
            }   
            
            
        }
        
        
    }
        
    
    
    class MyFocusListener implements FocusListener {
        
        @Override
        public void focusGained(FocusEvent e) {
            JTextComponent jt = (JTextComponent)e.getSource();
            jt.setForeground(Color.BLACK);
        }
        
        @Override
        public void focusLost(FocusEvent e) {
            JTextComponent jt = (JTextComponent)e.getSource();
            // check if result is valid
            if (isValid(jt.getText())) {
                double scale = (double)ws/(double)hs;
                if (e.getSource().equals(w)) {
                    int value = (int)Math.round(((double)Integer.parseInt(jt.getText()))/scale);
                    h.setText(value+"");
                } else {
                    int value = (int)Math.round(((double)Integer.parseInt(jt.getText()))*scale);
                    w.setText(value+"");
                }
            } else {
                jt.setForeground(Color.RED);
            }
        }
        
        
        private boolean isValid(String text){
            try {
                Integer.parseInt(text);
                return true;
            } catch (NumberFormatException e){
                return false;
            }
        }
        
    }
    
}
