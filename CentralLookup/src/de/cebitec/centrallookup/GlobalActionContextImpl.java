/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package de.cebitec.centrallookup;


import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;


/**
 * An implementation of ContextGlobalProvider that not only publishes
 * the lookup from the activated TopComponent to the global lookup, but
 * also publishes the data from the CentralLookup and the ProjectLookup
 * from ProjectLookup.Provider-instances. It is allmost the same as the
 * implementation given by the Netbeans platform.
 */
@org.openide.util.lookup.ServiceProvider( service = org.openide.util.ContextGlobalProvider.class, supersedes = "org.netbeans.modules.openide.windows.GlobalActionContextImpl" )
public final class GlobalActionContextImpl extends Object
        implements ContextGlobalProvider, Lookup.Provider,
                   java.beans.PropertyChangeListener {

    /**
     * registry to work with
     */
    private TopComponent.Registry registry;


    public GlobalActionContextImpl() {
        this( TopComponent.getRegistry() );
    }


    public GlobalActionContextImpl( TopComponent.Registry r ) {
        this.registry = r;
    }


    /**
     * Let's create the proxy listener that delegates to currently
     * selected top component.
     */
    @Override
    public Lookup createGlobalContext() {
        registry.addPropertyChangeListener( this );
        return org.openide.util.lookup.Lookups.proxy( this );
    }


    /**
     * The current component lookup
     */
    @Override
    public Lookup getLookup() {
        TopComponent tc = registry.getActivated();
        return new ProxyLookup( CentralLookup.getDefault(),
                                org.openide.util.lookup.Lookups.proxy( ProjectLookup.getCurrent() ),
                                tc == null ? Lookup.EMPTY : tc.getLookup() );
    }


    /**
     * Requests refresh of our lookup everytime component is chagned.
     */
    @Override
    public void propertyChange( java.beans.PropertyChangeEvent evt ) {
        if( TopComponent.Registry.PROP_ACTIVATED.equals( evt.getPropertyName() ) ) {
            org.openide.util.Utilities.actionsGlobalContext().lookup( javax.swing.ActionMap.class );
        }
        if( TopComponent.Registry.PROP_TC_CLOSED.equals( evt.getPropertyName() ) ) {
            org.openide.util.Utilities.actionsGlobalContext().lookup( javax.swing.ActionMap.class );
        }
    }


}
