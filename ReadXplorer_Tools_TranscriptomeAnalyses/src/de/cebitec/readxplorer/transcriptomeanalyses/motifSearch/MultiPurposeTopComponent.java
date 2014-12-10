/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package de.cebitec.readxplorer.transcriptomeanalyses.motifSearch;


import de.cebitec.readxplorer.transcriptomeanalyses.enums.PurposeEnum;
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
    public MultiPurposeTopComponent( PurposeEnum purpose ) {
        super();
        this.purpose = purpose;
    }


    @Override
    public void componentClosed() {

        if( purpose == PurposeEnum.CHARTS ) {
            if( this.getComponent( 0 ) instanceof InteractivePanel ) {
                System.out.println( "====InteractivePanel=====" );
            }

        }
        else if( purpose == PurposeEnum.MOTIF_SEARCH ) {
            if( this.getComponent( 0 ) instanceof MotifSearchPanel ) {
                System.out.println( "====MotifSearch-Promotor=====" );
                MotifSearchPanel msPanel = (MotifSearchPanel) this.getComponent( 0 );
                File parent = msPanel.getBioProspOutMinus10().getParentFile();
                if( msPanel.getBioProspOutMinus10().exists() ) {
                    msPanel.getBioProspOutMinus10().delete();
                }
                if( msPanel.getBioProspOutMinus35().exists() ) {
                    msPanel.getBioProspOutMinus35().delete();
                }
                File fileToDelete35 = new File( msPanel.getLogoMinus35().getParentFile() + "\\minusTenLogo.png" );
                fileToDelete35.delete();
                File fileToDelete10 = new File( msPanel.getLogoMinus10().getParentFile() + "\\minus35Logo.png" );
                fileToDelete10.delete();
                if( msPanel.getLogoMinus10() != null ) {
                    msPanel.getLogoMinus10().delete();
                }
                if( msPanel.getLogoMinus35() != null ) {
                    msPanel.getLogoMinus35().delete();
                }

                if( msPanel.getMinus10Input().exists() ) {
                    msPanel.getMinus10Input().delete();
                }
                if( msPanel.getMinus35Input().exists() ) {
                    msPanel.getMinus35Input().delete();
                }
                if( msPanel.getInfo().exists() ) {
                    msPanel.getInfo().delete();
                }
                if( parent.exists() ) {
                    parent.delete();
                }
            }

            if( this.getComponent( 0 ) instanceof RbsMotifSearchPanel ) {
                System.out.println( "====MotifSearch-RBS=====" );
                RbsMotifSearchPanel rbsPanel = (RbsMotifSearchPanel) this.getComponent( 0 );
                File parent = rbsPanel.getBioProspInput().getParentFile();
                if( rbsPanel.getBioProspInput().exists() ) {
                    rbsPanel.getBioProspInput().delete();
                }
                if( rbsPanel.getBioProspOut().exists() ) {
                    rbsPanel.getBioProspOut().delete();
                }
                if( rbsPanel.getSequenceLogo() != null ) {
                    File fileToDelete = new File( rbsPanel.getSequenceLogo().getParentFile() + "\\RBSLogo.png" );
                    fileToDelete.delete();
                    rbsPanel.getSequenceLogo().delete();
                }
                if( rbsPanel.getInfo().exists() ) {
                    rbsPanel.getInfo().delete();
                }
                if( parent.exists() ) {
                    parent.delete();
                }
            }
        }
        super.componentClosed(); //To change body of generated methods, choose Tools | Templates.
    }


}
