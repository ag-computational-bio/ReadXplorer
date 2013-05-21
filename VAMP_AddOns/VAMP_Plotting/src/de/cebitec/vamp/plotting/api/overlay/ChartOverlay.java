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
package de.cebitec.vamp.plotting.api.overlay;

import org.jfree.chart.panel.Overlay;

/**
 *
 * @author Nils Hoffmann
 */
public interface ChartOverlay extends Overlay {
    public void setVisible(boolean b);
    public boolean isVisible();
    public boolean isVisibilityChangeable();
    public int getLayerPosition();
    public void setLayerPosition(int pos);
    
    public String getName();
    public String getDisplayName();
    public String getShortDescription();
    
    public final int LAYER_LOWEST = Integer.MIN_VALUE;
    public final int LAYER_HIGHEST = Integer.MAX_VALUE;
    public final String PROP_VISIBLE = "visible";
    public final String PROP_LAYER_POSITION = "layerPosition";
    public final String PROP_NAME = "name";
    public final String PROP_DISPLAY_NAME = "displayName";
    public final String PROP_SHORT_DESCRIPTION = "shortDescription";
}
