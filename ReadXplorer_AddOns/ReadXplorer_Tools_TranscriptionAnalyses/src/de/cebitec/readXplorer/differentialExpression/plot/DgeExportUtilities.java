/*
 * Copyright (C) 2014 Kai Bernd Stadermann <kstaderm at cebitec.uni-bielefeld.de>
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
package de.cebitec.readXplorer.differentialExpression.plot;


import de.cebitec.readXplorer.plotting.ChartExporter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.awt.NotificationDisplayer;


/**
 * Class containing utility methods for exporting differential gene expression
 * results.
 *
 * @author Rolf Hilker <rhilker at mikrobio.med.uni-giessen.de>
 */
public class DgeExportUtilities {

    private DgeExportUtilities() {
    }


    /**
     * Receives an updated update status and updates the corresponding
     * progress handle and GUI components.
     * <p>
     * @param svgExportProgressHandle progress handle to update
     * @param status                  new export status
     * @param saveButton
     */
    public static void updateExportStatus( final ProgressHandle svgExportProgressHandle, final ChartExporter.ChartExportStatus status, final JButton saveButton ) {
        try {
            SwingUtilities.invokeAndWait( new Runnable() {
                @Override
                public void run() {
                    switch( status ) {
                        case RUNNING:
                            saveButton.setEnabled( false );
                            svgExportProgressHandle.start();
                            svgExportProgressHandle.switchToIndeterminate();
                            break;
                        case FAILED:
                            NotificationDisplayer.getDefault().notify( "Storage Error", new ImageIcon(), "The export of the plot failed.", null );
                            break;
                        case FINISHED:
                            NotificationDisplayer.getDefault().notify( "Success", new ImageIcon(), "SVG image saved.", null );
                            svgExportProgressHandle.switchToDeterminate( 100 );
                            svgExportProgressHandle.finish();
                            break;
                    }
                }


            } );
        }
        catch( InterruptedException | InvocationTargetException ex ) {
            Date currentTimestamp = new Timestamp( Calendar.getInstance().getTime().getTime() );
            Logger.getLogger( status.getClass().getName() ).log( Level.WARNING, ex.getMessage(), currentTimestamp );
        }
    }


}
