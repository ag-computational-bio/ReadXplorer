/*
 * Copyright (C) 2013 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
package de.cebitec.common.parser.fasta;

import java.io.IOException;
import java.io.StringWriter;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.junit.Test;

/**
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaWriterTest {

    @Test
    public void writeSingleTestSequence() throws IOException {
        String header = "Irgendwas";
        String sequence = "fajskdfja";

        StringWriter writer = new StringWriter();
        try (FastaWriter fasta = FastaWriter.writer(writer)) {
            fasta.writeEntry(header, sequence);
        }
        String toString = writer.toString();

        assertThat(toString, is(">Irgendwas\nfajskdfja\n"));
    }

    @Test
    public void writeMultipleTestSequences() throws IOException {
        StringWriter writer = new StringWriter();
        try (FastaWriter fasta = FastaWriter.writer(writer)) {
            fasta.writeEntry("1", "atg");
            fasta.writeEntry("2", "bnlalbalsd");
        }
        String toString = writer.getBuffer().toString();

        assertThat(toString, is(">1\natg\n>2\nbnlalbalsd\n"));
    }
    
    /**
     * Test whether the char counter is resetted after each fasta entry. Fails
     * if that is not the case.
     * @throws IOException 
     */
    @Test
    public void resetCharCounterAfterEachEntry() throws IOException {
        StringWriter writer = new StringWriter();
        try (FastaWriter fasta = FastaWriter.writer(writer)) {
            fasta.setLineWidth(5);
            
            fasta.writeEntry("1", "atg");
            fasta.writeEntry("2", "bnlalbalsd");
        }
        String toString = writer.getBuffer().toString();

        assertThat(toString, is(
                ">1\n"
                + "atg\n"
                + ">2\n"
                + "bnlal\n"
                + "balsd\n"));
    }

    @Test
    public void writeSingleLongTestSequence() throws IOException {
        String header = "164757.Mjls_0001";
        String sequence = "MTADPDPPFVAVWNTVVAELNGDPAATGPRNGDGALPTLTPQQRAWLKLVKPLVITEGFA"
            + "LLSVPTPFVQNEIERHLREPIITALSRHLGQRVELGVRIATPSPEDDDPPPSPVIADIDE"
            + "VDEDTEARVSAEETWPRYFSRPPETPAAEDPNAVSLNRRYTFDTFVIGASNRFAHAATLA"
            + "IAEAPARAYNPLFIWGESGLGKTHLLHAAGNYAQRLFPGMRVKYVSTEEFTNDFINSLRD"
            + "DRKASFKRSYRDIDVLLVDDIQFIEGKEGIQEEFFHTFNTLHNANKQIVISSDRPPKQLA"
            + "TLEDRLRTRFEWGLITDVQPPELETRIAILRKKAQMDRLDVPDDVLELIASSIERNIREL"
            + "EGALIRVTAFASLNKTPIDKSLAEIVLRDLISDASTMQISTAAIMAATAEYFETTIEELR"
            + "GPGKTRALAQSRQIAMYLCRELTDLSLPKIGQAFGRDHTTVMYAEKKIRGEMAERREVFD";

        String expected = ">164757.Mjls_0001\n"
            + "MTADPDPPFVAVWNTVVAELNGDPAATGPRNGDGALPTLTPQQRAWLKLVKPLVITEGFA\n"
            + "LLSVPTPFVQNEIERHLREPIITALSRHLGQRVELGVRIATPSPEDDDPPPSPVIADIDE\n"
            + "VDEDTEARVSAEETWPRYFSRPPETPAAEDPNAVSLNRRYTFDTFVIGASNRFAHAATLA\n"
            + "IAEAPARAYNPLFIWGESGLGKTHLLHAAGNYAQRLFPGMRVKYVSTEEFTNDFINSLRD\n"
            + "DRKASFKRSYRDIDVLLVDDIQFIEGKEGIQEEFFHTFNTLHNANKQIVISSDRPPKQLA\n"
            + "TLEDRLRTRFEWGLITDVQPPELETRIAILRKKAQMDRLDVPDDVLELIASSIERNIREL\n"
            + "EGALIRVTAFASLNKTPIDKSLAEIVLRDLISDASTMQISTAAIMAATAEYFETTIEELR\n"
            + "GPGKTRALAQSRQIAMYLCRELTDLSLPKIGQAFGRDHTTVMYAEKKIRGEMAERREVFD\n";
        StringWriter writer = new StringWriter();
        try (FastaWriter fasta = FastaWriter.writer(writer)) {
            fasta.writeEntry(header, sequence);
        }
        String toString = writer.getBuffer().toString();

        assertThat(toString, is(expected));
    }
}
