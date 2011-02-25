package de.cebitec.vamp.tools.referenceEditor;

import de.cebitec.vamp.view.dataVisualisation.referenceViewer.ReferenceViewer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public final class OpenReferenceEditorAction implements ActionListener {

    private final ReferenceViewer context;

    public OpenReferenceEditorAction(ReferenceViewer context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ReferenceEditor referenceEditor = new ReferenceEditor(context.getReference());
    }
}
