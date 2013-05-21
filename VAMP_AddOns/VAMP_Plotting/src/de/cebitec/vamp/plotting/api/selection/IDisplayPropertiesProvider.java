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
package de.cebitec.vamp.plotting.api.selection;

/**
 *
 *  @author Nils Hoffmann
 */
public interface IDisplayPropertiesProvider {
    public String getName(ISelection selection);
    public String getDisplayName(ISelection selection);
    public String getShortDescription(ISelection selection);
    
    public String getSourceName(ISelection selection);
    public String getSourceDisplayName(ISelection selection);
    public String getSourceShortDescription(ISelection selection);
    
    public String getTargetName(ISelection selection);
    public String getTargetDisplayName(ISelection selection);
    public String getTargetShortDescription(ISelection selection);
}
