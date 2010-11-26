package de.cebitec.vamp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public final class OpenVampAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Vamp.main(null);
    }
}
