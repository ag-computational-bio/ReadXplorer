/* 
 * Copyright (C) 2014 Rolf Hilker
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cebitec.readXplorer.ui.dataAdministration;

import de.cebitec.readXplorer.parser.ReferenceJob;
import de.cebitec.readXplorer.parser.TrackJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import org.openide.util.NbBundle;

/**
 *
 * @author ddoppmeier
 */
public class SelectionCard extends javax.swing.JPanel {
    
    private static final long serialVersionUID = 1L;

    public static final String PROP_HAS_CHECKED_JOBS = "hasCheckedJobs";

    /** Creates new form DataAdminPanel */
    public SelectionCard() {
        initComponents();
        refGenView.addPropertyChangeListener(PROP_HAS_CHECKED_JOBS, getHasCheckedJobsListener());
        mappingView.addPropertyChangeListener(PROP_HAS_CHECKED_JOBS, getHasCheckedJobsListener());
        mappingView.addPropertyChangeListener(TrackView.PROP_DESELECT, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refGenView.deselectRefGen((ReferenceJob) evt.getNewValue());
            }
        });
    }

    public void setSelectableJobs(List<ReferenceJob> refJobs, List<TrackJob> trackJobs){
        refGenView.setReferenceJobs(refJobs);
        mappingView.setTrackJobs(trackJobs);
    }

    public List<ReferenceJob> getRef2DelJobs(){
        return refGenView.getJobs2del();
    }

    public List<TrackJob> getTrack2DelJobs(){
        return mappingView.getJobs2Del();
    }

    private PropertyChangeListener getHasCheckedJobsListener(){
        return new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (refGenView.getJobs2del().isEmpty() && mappingView.getJobs2Del().isEmpty()){
                    firePropertyChange(evt.getPropertyName(), null, Boolean.FALSE);
                }
                else{
                    firePropertyChange(evt.getPropertyName(), null, Boolean.TRUE);
                }
            }
        };
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SelectionCard.class, "CTL_SelectionCard.name");
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        refGenView = new de.cebitec.readXplorer.ui.dataAdministration.ReferenceView();
        mappingView = new de.cebitec.readXplorer.ui.dataAdministration.TrackView();

        jTabbedPane1.addTab("References", refGenView);
        jTabbedPane1.addTab("Tracks", mappingView);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 467, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 322, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTabbedPane jTabbedPane1;
    private de.cebitec.readXplorer.ui.dataAdministration.TrackView mappingView;
    private de.cebitec.readXplorer.ui.dataAdministration.ReferenceView refGenView;
    // End of variables declaration//GEN-END:variables

}
