/*
 * Copyright (C) 2015 Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
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

package de.cebitec.readxplorer.transcriptionanalyses;


import java.awt.Desktop;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import org.openide.awt.HtmlBrowser;
import org.openide.util.lookup.ServiceProvider;



/**
 *
 * @author Rolf Hilker <rolf.hilker at mikrobio.med.uni-giessen.de>
 */
@ServiceProvider (service = HtmlBrowser.URLDisplayer.class, position = 0)
public class ExternalURLDisplayer extends HtmlBrowser.URLDisplayer {

    @Override
    public void showURL( URL link ) {
        try {
            Desktop.getDesktop().browse( link.toURI() );
        } catch( URISyntaxException | IOException ex ) {
            JOptionPane.showMessageDialog( new JPanel(), "The link caused an error: " +
                                            ex.getMessage() + "!", "URL Error", JOptionPane.ERROR_MESSAGE );
        }
    }


}
