package de.cebitec.vamp.ui.dataAdministration;

import de.cebitec.vamp.databackend.connector.ProjectConnector;
import de.cebitec.vamp.databackend.dataObjects.PersistantReference;
import de.cebitec.vamp.databackend.dataObjects.PersistantTrack;
import de.cebitec.vamp.parser.Job;
import de.cebitec.vamp.parser.ReferenceJob;
import de.cebitec.vamp.parser.TrackJob;
import de.cebitec.vamp.ui.dataAdministration.actions.DataAdminWizardAction;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public class DataAdminWizardSelectionPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private SelectionCard component;
    private boolean isValid;
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1); // or can use ChangeSupport in NB 6.0

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new SelectionCard();
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }

    @Override
    public boolean isValid() {
        return isValid;
    }

    @Override
    public boolean isFinishPanel() {
        return isValid;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    protected final void fireChangeEvent() {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    private Map<String, List<? extends Job>> getDeletableReferencesAndTracks(){
        List<ReferenceJob> refJobs = new ArrayList<ReferenceJob>();
        List<TrackJob> trackJobs = new ArrayList<TrackJob>();
        HashMap<Integer, ReferenceJob> indexedGens = new HashMap<Integer, ReferenceJob>();
        
        try {

            List<PersistantReference> dbGens = ProjectConnector.getInstance().getGenomes();
            for (Iterator<PersistantReference> it = dbGens.iterator(); it.hasNext();) {
                PersistantReference dbGen = it.next();
                // File and parser parameter meaningles in this context
                ReferenceJob r = new ReferenceJob(dbGen.getId(), null, null, dbGen.getDescription(), dbGen.getName(), dbGen.getTimeStamp());
                indexedGens.put(r.getID(), r);
                refJobs.add(r);
            }

            List<PersistantTrack> dbTracks = ProjectConnector.getInstance().getTracks();
            for (Iterator<PersistantTrack> it = dbTracks.iterator(); it.hasNext();) {
                PersistantTrack dbTrack = it.next();

                // File and parser, refgenjob, runjob parameters meaningles in this context
                TrackJob t = new TrackJob(dbTrack.getId(), null, dbTrack.getDescription(),
                        indexedGens.get(dbTrack.getRefGenID()),
                        null, dbTrack.getTimestamp());

                // register dependent tracks at genome and run
                ReferenceJob gen = indexedGens.get(dbTrack.getRefGenID());
                gen.registerTrackWithoutRunJob(t);
                trackJobs.add(t);
            }
        
        } catch (OutOfMemoryError e) {
            String msg = NbBundle.getMessage(DataAdminWizardSelectionPanel.class, "OOM_Message",
                    "An out of memory error occured during fetching the references. Please restart the software with more memory.");
            String title = NbBundle.getMessage(DataAdminWizardSelectionPanel.class, "OOM_Header", "Restart Software");
            JOptionPane.showMessageDialog(this.component, msg, title, JOptionPane.INFORMATION_MESSAGE);
        }

        // fill result map
        Map<String, List<? extends Job>> deletableStuff = new HashMap<String, List<? extends Job>>();
        deletableStuff.put("references", refJobs);
        deletableStuff.put("tracks", trackJobs);
        return deletableStuff;
    }

    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    @SuppressWarnings("unchecked")
    public void readSettings(WizardDescriptor settings) {
        // get deletable references and tracks
        Map<String, List<? extends Job>> possibleJobs = getDeletableReferencesAndTracks();

        List<ReferenceJob> refJobs = (List<ReferenceJob>) possibleJobs.get("references");
        List<TrackJob> trackJobs = (List<TrackJob>) possibleJobs.get("tracks");

        component.setSelectableJobs(refJobs, trackJobs);

        component.addPropertyChangeListener(SelectionCard.PROP_HAS_CHECKED_JOBS, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                isValid = (Boolean) evt.getNewValue();
                fireChangeEvent();
            }
        });
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        settings.putProperty(DataAdminWizardAction.PROP_REFS2DEL, component.getRef2DelJobs());
        settings.putProperty(DataAdminWizardAction.PROP_TRACK2DEL, component.getTrack2DelJobs());
    }
}
