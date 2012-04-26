package de.cebitec.vamp.view.dataVisualisation.abstractViewer;

import de.cebitec.vamp.util.ColorProperties;
import de.cebitec.vamp.util.Observable;
import de.cebitec.vamp.util.Observer;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author ddoppmeier, rhilker
 */
public class MenuLabel extends JLabel implements Observer, Observable {

    private static final long serialVersionUID = 2974452;
    
    public static final String TITLE_LEGEND = "Legend";
    public static final String TITLE_OPTIONS = "Options";
    
    private final JPanel associatedPanel;
    private boolean isShowingLabel;
    private Icon expandIcon;
    private Icon collapseIcon;
    private List<Observer> observers;
    

    public MenuLabel(JPanel associatedPanel, String title){
        super(title);
        this.observers = new ArrayList<Observer>();
        this.associatedPanel = associatedPanel;
        isShowingLabel = false;
        expandIcon = new ImageIcon(this.getClass().getClassLoader().getResource("de/cebitec/vamp/resources/expandIcon.png"));
        collapseIcon = new ImageIcon(this.getClass().getClassLoader().getResource("de/cebitec/vamp/resources/collapseIcon.png"));
        this.setIcon(expandIcon);

        this.addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (isShowingLabel) {
                    isShowingLabel = false;
                    MenuLabel.this.setIcon(expandIcon);
                } else {
                    isShowingLabel = true;
                    MenuLabel.this.setIcon(collapseIcon);
                    notifyObservers(true); //means it is true to hide other option/legend panels
                }
                MenuLabel.this.associatedPanel.setVisible(isShowingLabel);
            }
            @Override
            public void mousePressed(MouseEvent e) {
            }
            @Override
            public void mouseReleased(MouseEvent e) {
            }
            @Override
            public void mouseEntered(MouseEvent e) {
            }
            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g){
        g.setColor(ColorProperties.LEGEND_BACKGROUND);
        g.fillRect(0, 0, this.getSize().width-1, this.getSize().height-1);
        super.paintComponent(g);
    }
    
    /**
     * If this instance observes another Object, the other object can force this
     * instance to hide.
     * @param hidePanel the boolean value "true", if this MenuLabel should be hidden
     */
    @Override
    public void update(Object hidePanel) {
        if (this.isShowingLabel && hidePanel instanceof Boolean && (Boolean) hidePanel) {
            this.isShowingLabel = false;
            MenuLabel.this.setIcon(this.expandIcon);
            MenuLabel.this.associatedPanel.setVisible(this.isShowingLabel);
        }
    }

    @Override
    public void registerObserver(Observer observer) {
        this.observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        this.observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object data) {
        for (Observer observer : this.observers) {
            observer.update(data);
        }
    }
    
}
