/*
 * Maui, Maltcms User Interface. 
 * Copyright (C) 2008-2012, The authors of Maui. All rights reserved.
 *
 * Project website: http://maltcms.sf.net
 *
 * Maui may be used under the terms of either the
 *
 * GNU Lesser General Public License (LGPL)
 * http://www.gnu.org/licenses/lgpl.html
 *
 * or the
 *
 * Eclipse Public License (EPL)
 * http://www.eclipse.org/org/documents/epl-v10.php
 *
 * As a user/recipient of Maui, you may choose which license to receive the code 
 * under. Certain files or entire directories may not be covered by this 
 * dual license, but are subject to licenses compatible to both LGPL and EPL.
 * License exceptions are explicitly declared in all relevant files or in a 
 * LICENSE file in the relevant directories.
 *
 * Maui is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. Please consult the relevant license documentation
 * for details.
 */
package de.cebitec.vamp.plotting.overlay.nodes;

import de.cebitec.vamp.plotting.api.selection.IDisplayPropertiesProvider;
import de.cebitec.vamp.plotting.api.selection.ISelection;
import java.beans.IntrospectionException;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Children;
import org.openide.util.Lookup;

/**
 *
 *  @author Nils Hoffmann
 */
public class SourceNode extends BeanNode<Object> {

    public SourceNode(Object bean, Children children, Lookup lkp) throws IntrospectionException {
        super(bean, children, lkp);
    }
    
    @Override
    public String getName() {
        IDisplayPropertiesProvider provider = getLookup().lookup(IDisplayPropertiesProvider.class);
        if(provider!=null) {
            return provider.getSourceName(getLookup().lookup(ISelection.class));
        }
        return getBean().toString();
    }

    @Override
    public String getDisplayName() {
        IDisplayPropertiesProvider provider = getLookup().lookup(IDisplayPropertiesProvider.class);
        if(provider!=null) {
            return provider.getSourceDisplayName(getLookup().lookup(ISelection.class));
        }
        return getBean().toString();
    }

    @Override
    public String getShortDescription() {
        IDisplayPropertiesProvider provider = getLookup().lookup(IDisplayPropertiesProvider.class);
        if(provider!=null) {
            return provider.getSourceShortDescription(getLookup().lookup(ISelection.class));
        }
        return getBean().toString();
    }
    
}
