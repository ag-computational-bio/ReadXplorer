[![DOI:10.1093/bioinformatics/btw541](https://zenodo.org/badge/DOI/10.1093/bioinformatics/btw541.svg)](https://doi.org/10.1093/bioinformatics/btw541)
[![License: GPL v3](https://img.shields.io/badge/License-GPL%20v3-brightgreen.svg)](https://github.com/ag-computational-bio/ReadXplorer/blob/master/License-GPLv3.txt)
![Don't judge me](https://img.shields.io/badge/Language-Java-blue.svg)
![GitHub release](https://img.shields.io/github/release/ag-computational-bio/ReadXplorer.svg)

# ReadXplorer - Detailed Visualization and Analysis of Mapped Sequencing Reads

## Contents
- [Description](#description)
- [Features](#features)
- [Availability](#availability)
- [Citation](#citation)
- [FAQ](#faq)

## Description
ReadXplorer is a freely available comprehensive exploration and evaluation tool for NGS data. It extracts and adds quantity and quality measures to each alignment in order to classify the mapped reads. This classification is then taken into account for the different data views and all supported automatic analysis functions.

ReadXplorer is implemented in Java as a Netbeans rich client application. Utilizing a modular programming structure, it enables developers to create their own highly specialized software modules and easily plug them into ReadXplorer.

http://www.readxplorer.org

## Features
ReadXplorer has been developed for over decade and provides a rich feature set comprising but not limited to:
- Main Window: ReadXplorer's main window provides interactive visualizations of the reference genomes and multiple track data sets. Mapped reads are classified into three different mapping classes (green - perfect match, yellow - best match, red - common match).
- Alignment Viewer: The alignment viewer shows all read alignments including mismatches and gaps. Tooltips show details for hovered read alignments.
- Histogram Viewer: The Histogram Viewer supplies intuitive exploration of position specific coverage information as histogram. Match coverages are shown in green and all mismatches and gaps are displayed in a base specific color.
- Read Pair Viewer: The Read Pair Viewer displays the pair configuration of all aligned reads. Perfect Pairs are displayed in green, while Distorted Pairs are yellow and Single Mappings are red. The pair configurations are computed by a well-defined algorithm in ReadXplorer.
- Thumbnail Viewer: The Thumbnail Viewer allows the inspection of the coverage of selected features (e.g. genes) among selected tracks. It enables an easy comparison of different experiments (different tracks).
- Multiple Track Viewer: The Multiple Track Viewer combines the coverage of an arbitrary number of selected tracks in one data set. Combinations of tracks can also be used for any of ReadXplorer's automatic analysis functions.
- DGE: Results from differential gene expression analyses using DESeq within ReadXplorer are presented with a result table and an interactive M/A plot. Selected genes are automatically centered in the synchronized viewers.
- TSS Detection: Results of Transcription Start Site (TSS) detections include novel transcript detection capabilities.
- Read Counts: Results of RPKM and read count analyses are presented in histograms providing the distribution of RPKM values on the number of genes (log scale).
- Feature Coverage: The Feature Coverage Analysis allows the detection of reference features that show predefined characteristics in terms of their coverage.
- Operon Detection: Operons can be predicted based on the number of spanning reads of two neighboring genes.
- SNPs: Relevant information on called SNPs is shown in the genetic context. For detailed visual inspection, selected SNP positions are automatically centered in the data viewers.

## Availability

## Citation
> Hilker R, Bernd Stadermann KB, Schwengers O, Anisiforov E, Jaenicke S, Weisshaar B, Zimmermann T, Goesmann A (2016). ReadXplorer 2 - detailed read mapping analysis and visualization from one single source. Bioinformatics 32(24):3702-3708. DOI: 10.1093/bioinformatics/btw541

>Hilker R, Stadermann KB, Doppmeier D, Kalinowski J, Stoye J, Straube J, Winnebald J, Goesmann A (2014). ReadXplorer - Visualization and Analysis of Mapped Sequences. Bioinformatics 30(16):2247-54. DOI: 10.1093/bioinformatics/btu205

## FAQ
1. An extensive PDF manual is available [here](https://www.uni-giessen.de/fbz/fb08/Inst/bioinformatik/software/ReadXplorer/documentation/userManual)
2. A PDF getting started guide is available [here](https://www.uni-giessen.de/fbz/fb08/Inst/bioinformatik/software/ReadXplorer/documentation/gettingstarted)
3. Contact and support: readxplorer@computational.bio.uni-giessen.de
4. Issues: Bugs and issues can be filed [here](https://github.com/ag-computational-bio/ReadXplorer/issues)
