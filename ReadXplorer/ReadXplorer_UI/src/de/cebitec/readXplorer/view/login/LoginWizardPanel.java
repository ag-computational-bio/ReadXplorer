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
package de.cebitec.readXplorer.view.login;

import java.awt.Component;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;

 /**
 * The visual component that displays this panel. If you need to access the
 * component from this class, just use getComponent().
 * 
 * @author ddoppmeier
 */
public class LoginWizardPanel implements WizardDescriptor.Panel<WizardDescriptor> {

    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
    private LoginVisualPanel component;

    public static final String PROP_ADAPTER = "adapter";
    public static final String PROP_HOST = "hostname";
    public static final String PROP_DATABASE = "database";
    public static final String PROP_USER = "user";
    public static final String PROP_PASSWORD = "password";

    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new LoginVisualPanel();
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
        return true;
    }

    @Override
    public final void addChangeListener(ChangeListener l) {
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(WizardDescriptor settings) {
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        Map<String, String> loginData = component.getLoginData();

        String adapter, hostname, database, user, password;

        adapter = loginData.get(PROP_ADAPTER);
        hostname = loginData.get(PROP_HOST);
        database = loginData.get(PROP_DATABASE);
        user = loginData.get(PROP_USER);
        password = loginData.get(PROP_PASSWORD);

        settings.putProperty(PROP_ADAPTER, adapter);
        settings.putProperty(PROP_HOST, hostname);
        settings.putProperty(PROP_DATABASE, database);
        settings.putProperty(PROP_USER, user);
        settings.putProperty(PROP_PASSWORD, password);
    }

}
