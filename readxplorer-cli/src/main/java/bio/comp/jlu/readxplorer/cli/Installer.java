/*
 * Copyright (C) 2015 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package bio.comp.jlu.readxplorer.cli;


import java.util.logging.Logger;
import org.openide.LifecycleManager;
import org.openide.modules.ModuleInstall;


/**
 *
 * @author Oliver Schwengers <oliver.Schwengers@computational.bio.uni-giessen.de>
 */
public class Installer extends ModuleInstall {

    private final static Logger LOG = Logger.getLogger( Installer.class.getName() );


    @Override
    public void restored() {

        LOG.info( "start readxplorer-cli..." );

        LifecycleManager.getDefault().exit();

        LOG.info( "stop readxplorer-cli..." );

    }


}
