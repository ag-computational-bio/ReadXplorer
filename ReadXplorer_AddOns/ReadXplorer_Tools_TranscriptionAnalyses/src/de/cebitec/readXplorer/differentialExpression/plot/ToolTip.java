package de.cebitec.readXplorer.differentialExpression.plot;

import de.cebitec.readXplorer.databackend.dataObjects.PersistantFeature;
import de.cebitec.readXplorer.plotting.PlotDataItem;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;

/**
 *
 * @author kstaderm
 */
public class ToolTip implements XYToolTipGenerator {
    
    @Override
    public String generateToolTip(XYDataset xyd, int seriesIndex, int itemIndex) {
        XYSeriesCollection dataset = (XYSeriesCollection) xyd;
        PlotDataItem clickedItem = (PlotDataItem) dataset.getSeries(seriesIndex).getDataItem(itemIndex);
        PersistantFeature feature = clickedItem.getFeature();       
        StringBuilder sb = new StringBuilder("<html>");
        sb.append("Type: ").append(feature.getType()).append("<br>");
        sb.append("Locus: ").append(feature.getLocus()).append("<br>");
        sb.append("Gene: ").append(feature.toString()).append("<br>");
        sb.append("Start: ").append(feature.getStart()).append("<br>");
        sb.append("Stop: ").append(feature.getStop()).append("<br>");
        sb.append("EC number: ").append(feature.getEcNumber()).append("</html>");
        return sb.toString();
    }
}
