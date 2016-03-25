/*
 * Copyright (C) 2014 Institute for Bioinformatics and Systems Biology, University Giessen, Germany
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

package de.cebitec.readxplorer.plotting;


import de.cebitec.readxplorer.utils.Observable;
import de.cebitec.readxplorer.utils.Observer;
import java.awt.Rectangle;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;


/**
 *
 * @author kstaderm
 */
public class ChartExporter implements Runnable, Observable {

    private final Path file;
    private final JFreeChart chart;
    private final List<Observer> obs = new ArrayList<>();


    public enum ChartExportStatus {

        RUNNING, FINISHED, FAILED;

    }


    public ChartExporter( Path file, JFreeChart chart ) {
        this.chart = chart;
        this.file = file;
    }


    @Override
    public void run() {
        notifyObservers( ChartExportStatus.RUNNING );
        Rectangle bounds = new Rectangle( 1920, 1080 );
        SVGGraphics2D generator = new SVGGraphics2D( bounds.width, bounds.height );
        chart.draw( generator, bounds );
        try {
            SVGUtils.writeToSVG( file.toFile(), generator.getSVGElement() );
            notifyObservers( ChartExportStatus.FINISHED );
        } catch( IOException ex ) {
            notifyObservers( ChartExportStatus.FAILED );
        }
    }


    @Override
    public void registerObserver( Observer observer ) {
        obs.add( observer );

    }


    @Override
    public void removeObserver( Observer observer ) {
        obs.remove( observer );

    }


    @Override
    public void notifyObservers( Object data ) {
        for( Observer observer : obs ) {
            observer.update( data );
        }
    }


}
