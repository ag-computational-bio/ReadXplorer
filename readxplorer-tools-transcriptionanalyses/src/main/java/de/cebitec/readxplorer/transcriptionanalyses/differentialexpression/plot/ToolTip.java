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

package de.cebitec.readxplorer.transcriptionanalyses.differentialexpression.plot;


import de.cebitec.readxplorer.databackend.dataobjects.PersistentFeature;
import de.cebitec.readxplorer.plotting.PlotDataItem;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;


/**
 *
 * @author kstaderm
 */
public class ToolTip implements XYToolTipGenerator {

    @Override
    public String generateToolTip( XYDataset xyd, int seriesIndex, int itemIndex ) {
        XYSeriesCollection dataset = (XYSeriesCollection) xyd;
        PlotDataItem clickedItem = (PlotDataItem) dataset.getSeries( seriesIndex ).getDataItem( itemIndex );
        PersistentFeature feature = clickedItem.getFeature();
        StringBuilder sb = new StringBuilder( "<html>" );
        sb.append( "Type: " ).append( feature.getType() ).append( "<br>" );
        sb.append( "Locus: " ).append( feature.getLocus() ).append( "<br>" );
        sb.append( "Gene: " ).append( feature.toString() ).append( "<br>" );
        sb.append( "Start: " ).append( feature.getStart() ).append( "<br>" );
        sb.append( "Stop: " ).append( feature.getStop() ).append( "<br>" );
        sb.append( "EC number: " ).append( feature.getEcNumber() ).append( "</html>" );
        return sb.toString();
    }


}
