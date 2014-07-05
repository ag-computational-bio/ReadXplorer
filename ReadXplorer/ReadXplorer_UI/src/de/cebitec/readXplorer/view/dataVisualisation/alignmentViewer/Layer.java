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
package de.cebitec.readXplorer.view.dataVisualisation.alignmentViewer;

import de.cebitec.readXplorer.view.dataVisualisation.GenomeGapManager;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author ddoppmei
 */
public class Layer implements LayerI{

    private ArrayList<BlockI> blocks;
    private int absStart;
    private int absStop;
    private GenomeGapManager gapManager;

    public Layer(int absStart, int absStop, GenomeGapManager gapManager){
        this.absStart = absStart;
        this.absStop = absStop;
        this.gapManager = gapManager;
        blocks = new ArrayList<>();
    }

    @Override
    public void addBlock(BlockI block) {
        blocks.add(block);
    }

    @Override
    public String toString(){

        StringBuilder sb = new StringBuilder();
        int from = absStart;
        for (BlockI b : blocks) {
            int to = b.getAbsStart() - 1;
            this.fillWithGaps(from, to, sb);
            sb.append(b.toString());
            from = b.getAbsStop() + 1;
        }
        if (from < absStop) {

            this.fillWithGaps(from, absStop, sb);
        }
        return sb.toString();
    }

    private void fillWithGaps(int from, int to, StringBuilder sb) {
        for (int i = from; i <= to; i++) {
            if (gapManager.hasGapAt(i)) {
                for (int x = 0; x < gapManager.getNumOfGapsAt(i); x++) {
                    sb.append("-");
                }
                sb.append(".");
            } else {
                sb.append(".");
            }
        }
    }

    @Override
    public Iterator<BlockI> getBlockIterator() {
        return blocks.iterator();
    }

}
