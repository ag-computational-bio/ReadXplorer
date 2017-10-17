/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.ui.options;


import de.cebitec.readxplorer.api.constants.Paths;
import de.cebitec.readxplorer.api.constants.RServe;
import de.cebitec.readxplorer.utils.Downloader;
import de.cebitec.readxplorer.utils.Observer;
import de.cebitec.readxplorer.utils.OsUtils;
import de.cebitec.readxplorer.utils.Unzip;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


final class GnuRPanel extends OptionsPanel implements Observer {

    private static final Logger LOG = LoggerFactory.getLogger( GnuRPanel.class.getName() );

    private static final long serialVersionUID = 1L;

    private final GnuROptionsPanelController controller;
    private final Preferences pref;
    private Downloader downloader;
    private Unzip unzip;
    private GnuRAutoPanel autoPanel;
    private GnuRStartupScriptPanel scriptPanel;
    private GnuRRemotePanel remotePanel;
//    private File zipFile;
//    private boolean passwordChanged = false;
//    private final File userDir = Places.getUserDirectory();
//    private final File rDir = new File( userDir.getAbsolutePath() + File.separator + "R" );
//    private final File versionIndicator = new File( rDir.getAbsolutePath() + File.separator + "rx_minimal_version_2_1" );
    private static final String SOURCE_URI = "R-3.2.0.tar";
//    private static final String R_ZIP = "R-3.2.0.zip";
    private static final String DEFAULT_R_DOWNLOAD_MIRROR = "ftp://ftp.cebitec.uni-bielefeld.de/pub/readxplorer_repo/R/";
    private static final String DEFAULT_RSERVE_HOST = "localhost";
    private static final int DEFAULT_RSERVE_PORT = 6311;


    GnuRPanel( GnuROptionsPanelController controller ) {
        this.controller = controller;
        this.pref = NbPreferences.forModule( Object.class );
        initComponents();
        autoPanel = new GnuRAutoPanel( pref.get( Paths.CRAN_MIRROR, DEFAULT_R_DOWNLOAD_MIRROR ) + SOURCE_URI );
        scriptPanel = new GnuRStartupScriptPanel();
        remotePanel = new GnuRRemotePanel();
        //        warningMessage.setText( "" );
        //        String sourceUri = pref.get( Paths.CRAN_MIRROR, DEFAULT_R_DOWNLOAD_MIRROR ) + SOURCE_URI;
        //        sourceFileTextField.setText( sourceUri );
        //        jProgressBar1.setMaximum( 100 );
        setUpListener();
        if( OsUtils.isWindows() ) {
            autoButton.setSelected( true );
            autoButtonSelected();
            messages.setText( "'Startup Script' is only supported under Linux." );
            startupScriptButton.setEnabled( false );
//            if( !versionIndicator.exists() ) {
//                installButton.setEnabled( true );
//                jProgressBar1.setEnabled( true );
//                messages.setText( "" );
//            }
        } else if( OsUtils.isMac() ) {
            messages.setText( "Startup script and auto installation are not supported under OS X." );
            remoteButton.setSelected( true );
            autoButton.setEnabled( false );
            startupScriptButton.setEnabled( false );
            remoteButtonSelected();
        } else if( new File( "/vol/readxplorer/R/CeBiTecMode" ).exists() ) {
            messages.setText( "Rserve is already configured correctly for use in CeBiTec" );
            autoButton.setEnabled( false );
            startupScriptButton.setEnabled( false );
            remoteButton.setEnabled( false );
//            cranMirror.setEnabled( false );
        } else {
            messages.setText( "Auto installation is only supported under Windows 7, 8 & 10." );
            autoButton.setEnabled( false );
        }
//        rServePort.setInputVerifier( new PortInputVerifier() );
//        rServeStartupScript.setInputVerifier( new ScriptInputVerifier() );
//        usernameTextField.setInputVerifier( new UsernameInputVerifier() );
//        passwordTextField.setInputVerifier( new PasswordInputVerifier() );
    }


    private void setUpListener() {
        autoPanel.addCRANMirrorListener( new KeyListener() {
            @Override
            public void keyTyped( KeyEvent e ) {
                controller.changed();
            }


            @Override
            public void keyPressed( KeyEvent e ) {
            }


            @Override
            public void keyReleased( KeyEvent e ) {
            }


        } );
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        gnuRSettings = new javax.swing.ButtonGroup();
        autoButton = new javax.swing.JRadioButton();
        remoteButton = new javax.swing.JRadioButton();
        startupScriptButton = new javax.swing.JRadioButton();
        jLabel4 = new javax.swing.JLabel();
        jSeparator1 = new javax.swing.JSeparator();
        messages = new javax.swing.JLabel();
        gnuRSettingsPanel = new javax.swing.JPanel();

        gnuRSettings.add(autoButton);
        org.openide.awt.Mnemonics.setLocalizedText(autoButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.autoButton.text")); // NOI18N
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        gnuRSettings.add(remoteButton);
        org.openide.awt.Mnemonics.setLocalizedText(remoteButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.remoteButton.text")); // NOI18N
        remoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                remoteButtonActionPerformed(evt);
            }
        });

        gnuRSettings.add(startupScriptButton);
        org.openide.awt.Mnemonics.setLocalizedText(startupScriptButton, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.startupScriptButton.text")); // NOI18N
        startupScriptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startupScriptButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.jLabel4.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(messages, org.openide.util.NbBundle.getMessage(GnuRPanel.class, "GnuRPanel.messages.text")); // NOI18N

        gnuRSettingsPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gnuRSettingsPanel.setMinimumSize(new java.awt.Dimension(523, 479));
        gnuRSettingsPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(autoButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(startupScriptButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(remoteButton)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(gnuRSettingsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(messages)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jSeparator1)))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoButton)
                    .addComponent(remoteButton)
                    .addComponent(startupScriptButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(messages))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(gnuRSettingsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 467, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void remoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_remoteButtonActionPerformed
        remoteButtonSelected();
    }//GEN-LAST:event_remoteButtonActionPerformed


    private void remoteButtonSelected() {
        remoteButton.setSelected( true );
        gnuRSettingsPanel.removeAll();
        gnuRSettingsPanel.add( remotePanel );
        revalidate();
        this.repaint();
    }

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
        autoButtonSelected();
    }//GEN-LAST:event_autoButtonActionPerformed


    private void autoButtonSelected() {
        autoButton.setSelected( true );
        gnuRSettingsPanel.removeAll();
        gnuRSettingsPanel.add( autoPanel );
        revalidate();
        this.repaint();
    }

    private void startupScriptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startupScriptButtonActionPerformed
        startupScriptButtonSelected();
    }//GEN-LAST:event_startupScriptButtonActionPerformed


    private void startupScriptButtonSelected() {
        startupScriptButton.setSelected( true );
        gnuRSettingsPanel.removeAll();
        gnuRSettingsPanel.add( scriptPanel );
        revalidate();
        this.repaint();
    }


//    private void useAuthCheckboxSelected() {
//        usernameTextField.setEditable( useAuthCheckBox.isSelected() );
//        passwordTextField.setEditable( useAuthCheckBox.isSelected() );
//    }
    @Override
    void load() {
        autoPanel.load( pref, DEFAULT_R_DOWNLOAD_MIRROR );
        scriptPanel.load( pref, DEFAULT_RSERVE_PORT );
        remotePanel.load( pref, DEFAULT_RSERVE_HOST, DEFAULT_RSERVE_PORT );
        boolean remoteButtonSelected = pref.getBoolean( RServe.RSERVE_USE_REMOTE_SETUP, false );
        boolean startupScriptButtonSelected = pref.getBoolean( RServe.RSERVE_USE_STARTUP_SCRIPT_SETUP, false );
        boolean autoButtonSelected = pref.getBoolean( RServe.RSERVE_USE_AUTO_SETUP, false );
        if( remoteButtonSelected ) {
            gnuRSettings.setSelected( remoteButton.getModel(), true );
            remoteButtonSelected();
        } else if( autoButtonSelected && OsUtils.isWindows() ) {
            gnuRSettings.setSelected( autoButton.getModel(), true );
            autoButtonSelected();
        } else {
            // default is startupscript
            gnuRSettings.setSelected( startupScriptButton.getModel(), true );
            startupScriptButtonSelected();
        }


    }


    @Override
    void store() {
        autoPanel.store( pref );
        scriptPanel.store( pref );
        remotePanel.store( pref );
        boolean remoteButtonSelected = remoteButton.isSelected();
        boolean startupScriptButtonSelected = startupScriptButton.isSelected();
        boolean autoButtonSelected = autoButton.isSelected();
        pref.putBoolean( RServe.RSERVE_USE_REMOTE_SETUP, remoteButtonSelected );
        pref.putBoolean( RServe.RSERVE_USE_STARTUP_SCRIPT_SETUP, startupScriptButtonSelected );
        pref.putBoolean( RServe.RSERVE_USE_AUTO_SETUP, autoButtonSelected );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton autoButton;
    private javax.swing.ButtonGroup gnuRSettings;
    private javax.swing.JPanel gnuRSettingsPanel;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel messages;
    private javax.swing.JRadioButton remoteButton;
    private javax.swing.JRadioButton startupScriptButton;
    // End of variables declaration//GEN-END:variables


//    private void unzipGNUR() {
//        if( !versionIndicator.exists() && rDir.exists() ) {
//            rDir.delete();
//        }
//        rDir.mkdir();
//        try {
//            unzip = new Unzip( zipFile, rDir );
//            unzip.registerObserver( this );
//        } catch( Unzip.NoDirectoryException ex ) {
//            Exceptions.printStackTrace( ex );
//        }
//        Thread th = new Thread( unzip );
//        th.start();
//    }
    @Override
    public void update( Object args ) {
//        if( args instanceof Downloader.Status ) {
//            Downloader.Status status = (Downloader.Status) args;
//            switch( status ) {
//                case FAILED:
//                    SwingUtilities.invokeLater( new Runnable() {
//                        @Override
//                        public void run() {
//                            jProgressBar1.setIndeterminate( false );
//                            jProgressBar1.setValue( 0 );
//                            messages.setText( "Download failed. Please try again." );
//                        }
//
//
//                    } );
//                    downloader.removeObserver( this );
//                    break;
//                case FINISHED:
//                    SwingUtilities.invokeLater( new Runnable() {
//                        @Override
//                        public void run() {
//                            jProgressBar1.setIndeterminate( false );
//                            jProgressBar1.setValue( 100 );
//                            messages.setText( "Download finished." );
//                        }
//
//
//                    } );
//                    downloader.removeObserver( this );
//                    unzipGNUR();
//                    break;
//                case RUNNING:
//                    SwingUtilities.invokeLater( new Runnable() {
//                        @Override
//                        public void run() {
//                            jProgressBar1.setIndeterminate( true );
//                            messages.setText( "Downloading GNU R." );
//                        }
//
//
//                    } );
//                    break;
//                default:
//                    LOG.info( "Encountered unknown downloader status." );
//            }
//        }
//
//        if( args instanceof Unzip.Status ) {
//            final Unzip.Status status = (Unzip.Status) args;
//            SwingUtilities.invokeLater( new Runnable() {
//                @Override
//                public void run() {
//                    switch( status ) {
//                        case FILE_NOT_FOUND:
//                            messages.setText( "The user directory does not exist." );
//                            jProgressBar1.setIndeterminate( false );
//                            jProgressBar1.setValue( 0 );
//                            break;
//                        case FINISHED:
//                            messages.setText( "Setup complete!" );
//                            jProgressBar1.setIndeterminate( false );
//                            jProgressBar1.setValue( 100 );
//                            break;
//                        case NO_RIGHTS:
//                            messages.setText( "Can not write to user dir. Please check permissions." );
//                            jProgressBar1.setIndeterminate( false );
//                            jProgressBar1.setValue( 0 );
//                            break;
//                        case RUNNING:
//                            messages.setText( "Extracting GNU R from archive." );
//                            jProgressBar1.setIndeterminate( true );
//                            break;
//                        case FAILED: //fallthrough to default
//                        default:
//                            messages.setText( "Can not unzip R archive, please try again." );
//                            jProgressBar1.setIndeterminate( false );
//                            jProgressBar1.setValue( 0 );
//
//                    }
//                }
//
//
//            } );
//        }

    }


//    class PortInputVerifier extends InputVerifier {
//
//        @Override
//        public boolean verify( JComponent input ) {
//            JTextField textField = (JTextField) input;
//            String text = textField.getText();
//            try {
//                Integer.parseInt( text );
//            } catch( NumberFormatException ex ) {
//                warningMessage.setText( "Please enter a valid port number." );
//                return false;
//            }
//            warningMessage.setText( "" );
//            return true;
//        }
//
//
//    }
//    class ScriptInputVerifier extends InputVerifier {
//
//        @Override
//        public boolean verify( JComponent input ) {
//            JTextField textField = (JTextField) input;
//            String text = textField.getText();
//            File script = new File( text );
//            if( script.exists() && script.canExecute() ) {
//                warningMessage.setText( "" );
//                return true;
//            } else if( !startupScriptButton.isSelected() ) {
//                warningMessage.setText( "" );
//                return true;
//            } else {
//                warningMessage.setText( "Please enter a valid startup script." );
//                return false;
//            }
//        }
//
//
//    }
//    class UsernameInputVerifier extends InputVerifier {
//
//        @Override
//        public boolean verify( JComponent input ) {
//            JTextField textField = (JTextField) input;
//            String username = textField.getText();
//            if( username.isEmpty() ) {
//                warningMessage.setText( "Username cannot be left empty." );
//                return false;
//            } else if( !manualButton.isSelected() ) {
//                warningMessage.setText( "" );
//                return true;
//            } else {
//                warningMessage.setText( "" );
//                return true;
//            }
//        }
//
//
//    }
//    class PasswordInputVerifier extends InputVerifier {
//
//        @Override
//        public boolean verify( JComponent input ) {
//            JPasswordField textField = (JPasswordField) input;
//            char[] password = textField.getPassword();
//            if( password.length > 0 ) {
//                warningMessage.setText( "" );
//                return true;
//            } else if( !manualButton.isSelected() ) {
//                warningMessage.setText( "" );
//                return true;
//            } else {
//                warningMessage.setText( "Password cannot be left empty." );
//                return false;
//            }
//        }
//
//
//    }
}
