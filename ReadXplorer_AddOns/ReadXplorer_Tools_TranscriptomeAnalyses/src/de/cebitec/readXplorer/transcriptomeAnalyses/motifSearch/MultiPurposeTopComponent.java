/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cebitec.readXplorer.transcriptomeAnalyses.motifSearch;

import de.cebitec.readXplorer.transcriptomeAnalyses.enums.PurposeEnum;
import de.cebitec.readXplorer.view.TopComponentExtended;
import de.erichseifert.gral.ui.InteractivePanel;
import java.io.File;

/**
 *
 * @author jritter
 */
public class MultiPurposeTopComponent extends TopComponentExtended {

    PurposeEnum purpose;

    /**
     *
     * @param purpose
     */
    public MultiPurposeTopComponent(PurposeEnum purpose) {
        super();
        this.purpose = purpose;
    }

    @Override
    public void componentClosed() {

        if (purpose == PurposeEnum.CHARTS) {
            if (this.getComponent(0) instanceof InteractivePanel) {
                System.out.println("====InteractivePanel=====");
            }

        } else if (purpose == PurposeEnum.MOTIF_SEARCH) {
            if (this.getComponent(0) instanceof MotifSearchPanel) {
                System.out.println("====MotifSearch-Promotor=====");
                MotifSearchPanel msPanel = (MotifSearchPanel) this.getComponent(0);
                File parent = msPanel.getBioProspOutMinus10().getParentFile();
                msPanel.getBioProspOutMinus10().delete();
                msPanel.getBioProspOutMinus35().delete();
                msPanel.getLogoMinus10().delete();
                msPanel.getLogoMinus35().delete();
                msPanel.getMinus10Input().delete();
                msPanel.getMinus35Input().delete();
                msPanel.getInfo().delete();
                parent.delete();
            }

            if (this.getComponent(0) instanceof RbsMotifSearchPanel) {
                System.out.println("====MotifSearch-RBS=====");
                RbsMotifSearchPanel rbsPanel = (RbsMotifSearchPanel) this.getComponent(0);
                File parent = rbsPanel.getBioProspInput().getParentFile();
                rbsPanel.getBioProspInput().delete();
                rbsPanel.getBioProspOut().delete();
                rbsPanel.getSequenceLogo().delete();
                rbsPanel.getInfo().delete();
                parent.delete();
            }
        }
        super.componentClosed(); //To change body of generated methods, choose Tools | Templates.
    }
}
