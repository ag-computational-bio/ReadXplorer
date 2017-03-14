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

package de.cebitec.readxplorer.tools.rnafolder.rnamovies.thirdparty;

// Ported to Java 12/00 K Weiner

public class NeuQuant {

    protected static final int netsize = 256; /* number of colours used */

    /* four primes near 500 - assume no image has a length so large */
    /* that it is divisible by all four primes */
    protected static final int prime1 = 499;
    protected static final int prime2 = 491;
    protected static final int prime3 = 487;
    protected static final int prime4 = 503;

    protected static final int minpicturebytes = (3 * prime4);
    /* minimum size for input image */

    /* Program Skeleton ---------------- [select samplefac in range 1..30] [read
     * image from input file] pic = (unsigned char*) malloc(3*width*height);
     * initnet(pic,3*width*height,samplefac); learn(); unbiasnet(); [write
     * output image header, using writecolourmap(f)] inxbuild(); write output
     * image using inxsearch(b,g,r) */

    /* Network Definitions ------------------- */
    protected static final int maxnetpos = (netsize - 1);
    protected static final int netbiasshift = 4; /* bias for colour values */

    protected static final int ncycles = 100; /* no. of learning cycles */

    /* defs for freq and bias */
    protected static final int intbiasshift = 16; /* bias for fractions */

    protected static final int intbias = 1 << intbiasshift;
    protected static final int gammashift = 10; /* gamma = 1024 */

    protected static final int gamma = 1 << gammashift;
    protected static final int betashift = 10;
    protected static final int beta = (intbias >> betashift); /* beta = 1/1024 */

    protected static final int betagamma
            = (intbias << (gammashift - betashift));

    /* defs for decreasing radius factor */
    protected static final int initrad = (netsize >> 3); /* for 256 cols, radius
     * starts */

    protected static final int radiusbiasshift = 6; /* at 32.0 biased by 6 bits */

    protected static final int radiusbias = 1 << radiusbiasshift;
    protected static final int initradius = (initrad * radiusbias); /* and
     * decreases by a */

    protected static final int radiusdec = 30; /* factor of 1/30 each cycle */

    /* defs for decreasing alpha factor */
    protected static final int alphabiasshift = 10; /* alpha starts at 1.0 */

    protected static final int initalpha = 1 << alphabiasshift;

    protected int alphadec; /* biased by 10 bits */

    /* radbias and alpharadbias used for radpower calculation */
    protected static final int radbiasshift = 8;
    protected static final int radbias = 1 << radbiasshift;
    protected static final int alpharadbshift = (alphabiasshift + radbiasshift);
    protected static final int alpharadbias = 1 << alpharadbshift;

    /* Types and Global Variables -------------------------- */

    protected byte[] thepicture; /* the input image itself */

    protected int lengthcount; /* lengthcount = H*W*3 */

    protected int samplefac; /* sampling factor 1..30 */

    //   typedef int pixel[4];                /* BGRc */
    protected int[][] network; /* the network itself - [netsize][4] */

    protected int[] netindex = new int[256];
    /* for network lookup - really 256 */

    protected int[] bias = new int[netsize];
    /* bias and freq arrays for learning */
    protected int[] freq = new int[netsize];
    protected int[] radpower = new int[initrad];
    /* radpower for precomputation */

    /* Initialise network in range (0,0,0) to (255,255,255) and set parameters
     * ----------------------------------------------------------------------- */

    public NeuQuant( byte[] thepic, int len, int sample ) {

        thepicture = thepic;
        lengthcount = len;
        samplefac = sample;

        network = new int[netsize][];
        for( int i = 0; i < netsize; i++ ) {
            network[i] = new int[4];
            int[] p = network[i];
            p[0] = p[1] = p[2] = (i << (netbiasshift + 8)) / netsize;
            freq[i] = intbias / netsize; /* 1/netsize */

            bias[i] = 0;
        }
    }


    public byte[] colorMap() {
        byte[] map = new byte[3 * netsize];
        int[] index = new int[netsize];
        for( int i = 0; i < netsize; i++ ) {
            index[network[i][3]] = i;
        }
        int k = 0;
        for( int i = 0; i < netsize; i++ ) {
            int j = index[i];
            map[k++] = (byte) (network[j][0]);
            map[k++] = (byte) (network[j][1]);
            map[k++] = (byte) (network[j][2]);
        }
        return map;
    }

    /* Insertion sort of network and building of netindex[0..255] (to do after
     * unbias)
     * ------------------------------------------------------------------------------- */

    public void inxbuild() {

        int previouscol = 0;
        int startpos = 0;
        for( int i = 0; i < netsize; i++ ) {
            int[] p = network[i];
            int smallpos = i;
            int smallval = p[1]; /* index on g */
            /* find smallest in i..netsize-1 */

            for( int j = i + 1; j < netsize; j++ ) {
                int[] q = network[j];
                if( q[1] < smallval ) { /* index on g */

                    smallpos = j;
                    smallval = q[1]; /* index on g */

                }
            }
            int[] q = network[smallpos];
            /* swap p (i) and q (smallpos) entries */
            if( i != smallpos ) {
                int j = q[0];
                q[0] = p[0];
                p[0] = j;
                j = q[1];
                q[1] = p[1];
                p[1] = j;
                j = q[2];
                q[2] = p[2];
                p[2] = j;
                j = q[3];
                q[3] = p[3];
                p[3] = j;
            }
            /* smallval entry is now in position i */
            if( smallval != previouscol ) {
                netindex[previouscol] = (startpos + i) >> 1;
                for( int j = previouscol + 1; j < smallval; j++ ) {
                    netindex[j] = i;
                }
                previouscol = smallval;
                startpos = i;
            }
        }
        netindex[previouscol] = (startpos + maxnetpos) >> 1;
        for( int j = previouscol + 1; j < 256; j++ ) {
            netindex[j] = maxnetpos; /* really 256 */

        }
    }

    /* Main Learning Loop ------------------ */

    public void learn() {

        int radius, rad, alpha, step, delta, samplepixels;
        byte[] p;
        int pix, lim;

        if( lengthcount < minpicturebytes ) {
            samplefac = 1;
        }
        alphadec = 30 + ((samplefac - 1) / 3);
        p = thepicture;
        pix = 0;
        lim = lengthcount;
        samplepixels = lengthcount / (3 * samplefac);
        delta = samplepixels / ncycles;
        alpha = initalpha;
        radius = initradius;

        rad = radius >> radiusbiasshift;
        if( rad <= 1 ) {
            rad = 0;
        }
        for( int i = 0; i < rad; i++ ) {
            radpower[i]
                    = alpha * (((rad * rad - i * i) * radbias) / (rad * rad));
        }

        //fprintf(stderr,"beginning 1D learning: initial radius=%d\n", rad);

        if( lengthcount < minpicturebytes ) {
            step = 3;
        } else if( (lengthcount % prime1) != 0 ) {
            step = 3 * prime1;
        } else {
            if( (lengthcount % prime2) != 0 ) {
                step = 3 * prime2;
            } else {
                if( (lengthcount % prime3) != 0 ) {
                    step = 3 * prime3;
                } else {
                    step = 3 * prime4;
                }
            }
        }

        int i = 0;
        while( i < samplepixels ) {
            final int b = (p[pix + 0] & 0xff) << netbiasshift;
            final int g = (p[pix + 1] & 0xff) << netbiasshift;
            final int r = (p[pix + 2] & 0xff) << netbiasshift;
            final int q = contest( b, g, r );

            altersingle( alpha, q, b, g, r );
            if( rad != 0 ) {
                alterneigh( rad, q, b, g, r ); /* alter neighbours */
            }

            pix += step;
            if( pix >= lim ) {
                pix -= lengthcount;
            }

            i++;
            if( delta == 0 ) {
                delta = 1;
            }
            if( i % delta == 0 ) {
                alpha -= alpha / alphadec;
                radius -= radius / radiusdec;
                rad = radius >> radiusbiasshift;
                if( rad <= 1 ) {
                    rad = 0;
                }
                for( int j = 0; j < rad; j++ ) {
                    radpower[j]
                            = alpha * (((rad * rad - j * j) * radbias) / (rad * rad));
                }
            }
        }
        //fprintf(stderr,"finished 1D learning: final alpha=%f !\n",((float)alpha)/initalpha);
    }

    /* Search for BGR values 0..255 (after net is unbiased) and return colour
     * index
     * ---------------------------------------------------------------------------- */

    public int map( int b, int g, int r ) {

        int best = -1;
        int bestd = 1000; /* biggest possible dist is 256*3 */

        int i = netindex[g]; /* index on g */

        int j = i - 1; /* start at netindex[g] and work outwards */

        while( (i < netsize) || (j >= 0) ) {
            if( i < netsize ) {
                int[] p = network[i];
                int dist = p[1] - g; /* inx key */

                if( dist >= bestd ) {
                    i = netsize; /* stop iter */
                } else {
                    i++;
                    if( dist < 0 ) {
                        dist = -dist;
                    }
                    int a = p[0] - b;
                    if( a < 0 ) {
                        a = -a;
                    }
                    dist += a;
                    if( dist < bestd ) {
                        a = p[2] - r;
                        if( a < 0 ) {
                            a = -a;
                        }
                        dist += a;
                        if( dist < bestd ) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
            if( j >= 0 ) {
                int[] p = network[j];
                int dist = g - p[1]; /* inx key - reverse dif */

                if( dist >= bestd ) {
                    j = -1; /* stop iter */
                } else {
                    j--;
                    if( dist < 0 ) {
                        dist = -dist;
                    }
                    int a = p[0] - b;
                    if( a < 0 ) {
                        a = -a;
                    }
                    dist += a;
                    if( dist < bestd ) {
                        a = p[2] - r;
                        if( a < 0 ) {
                            a = -a;
                        }
                        dist += a;
                        if( dist < bestd ) {
                            bestd = dist;
                            best = p[3];
                        }
                    }
                }
            }
        }

        return best;

    }


    public byte[] process() {
        learn();
        unbiasnet();
        inxbuild();
        return colorMap();
    }

    /* Unbias network to give byte values 0..255 and record position i to
     * prepare for sort
     * ----------------------------------------------------------------------------------- */

    public void unbiasnet() {

        for( int i = 0; i < netsize; i++ ) {
            network[i][0] >>= netbiasshift;
            network[i][1] >>= netbiasshift;
            network[i][2] >>= netbiasshift;
            network[i][3] = i; /* record colour no */

        }
    }

    /* Move adjacent neurons by precomputed alpha*(1-((i-j)^2/[r]^2)) in
     * radpower[|i-j|]
     * --------------------------------------------------------------------------------- */

    protected void alterneigh( final int rad, final int i, final int b, final int g, final int r ) {

        int tmp = i - rad;
        final int lo = tmp < -1 ? -1 : tmp;

        tmp = i + rad;
        final int hi = tmp > netsize ? netsize : tmp;

        int j = i + 1;
        int k = i - 1;
        int m = 1;
        while( (j < hi) || (k > lo) ) {
            final int a = radpower[m++];
            if( j < hi ) {
                int[] p = network[j++];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch( Exception e ) {
                } // prevents 1.3 miscompilation
            }
            if( k > lo ) {
                int[] p = network[k--];
                try {
                    p[0] -= (a * (p[0] - b)) / alpharadbias;
                    p[1] -= (a * (p[1] - g)) / alpharadbias;
                    p[2] -= (a * (p[2] - r)) / alpharadbias;
                } catch( Exception e ) {
                }
            }
        }
    }

    /* Move neuron i towards biased (b,g,r) by factor alpha
     * ---------------------------------------------------- */

    protected void altersingle( int alpha, int i, int b, int g, int r ) {

        /* alter hit neuron */
        int[] n = network[i];
        n[0] -= (alpha * (n[0] - b)) / initalpha;
        n[1] -= (alpha * (n[1] - g)) / initalpha;
        n[2] -= (alpha * (n[2] - r)) / initalpha;
    }

    /* Search for biased BGR values ---------------------------- */

    protected int contest( int b, int g, int r ) {

        /* finds closest neuron (min dist) and updates freq */
        /* finds best neuron (min dist-bias) and returns position */
        /* for frequently chosen neurons, freq[i] is high and bias[i] is
         * negative */
        /* bias[i] = gamma*((1/netsize)-freq[i]) */

        int bestd = ~(1 << 31);
        int bestbiasd = bestd;
        int bestpos = -1;
        int bestbiaspos = bestpos;

        for( int i = 0; i < netsize; i++ ) {

            int[] n = network[i];
            int dist = n[0] - b;
            if( dist < 0 ) {
                dist = -dist;
            }
            int a = n[1] - g;
            if( a < 0 ) {
                a = -a;
            }
            dist += a;
            a = n[2] - r;
            if( a < 0 ) {
                a = -a;
            }
            dist += a;
            if( dist < bestd ) {
                bestd = dist;
                bestpos = i;
            }
            int biasdist = dist - ((bias[i]) >> (intbiasshift - netbiasshift));
            if( biasdist < bestbiasd ) {
                bestbiasd = biasdist;
                bestbiaspos = i;
            }
            int betafreq = (freq[i] >> betashift);
            freq[i] -= betafreq;
            bias[i] += (betafreq << gammashift);
        }

        freq[bestpos] += beta;
        bias[bestpos] -= betagamma;

        return (bestbiaspos);

    }


}
