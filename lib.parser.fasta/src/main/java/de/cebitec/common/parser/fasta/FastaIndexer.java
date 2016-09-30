/*
 * Copyright (C) 2014 Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

/**
 * Calculates the faidx for a fasta file. This class is not thread safe.
 *
 * @author Lukas Jelonek <ljelonek at cebitec.uni-bielefeld.de>
 */
public class FastaIndexer {

    private final int DEFAULT_BUFFER_SIZE = 4 * 1024 * 1024;
    private int bufferSize = DEFAULT_BUFFER_SIZE;

    private final HeaderStrategy header = new HeaderStrategy();
    private final SequenceStrategy sequence = new SequenceStrategy();
    private int linenumber = 0;

    /**
     * The parsing of the file is accomplished via a strategy and state pattern. There are two different parsers: one
     * for the header and one for the sequence. They will be switched when the header or the sequence ends.
     */
    private interface Strategy {

        /**
         * Processes one character of the input file.
         *
         * @param c The read character.
         * @return The strategy that will be used next.
         */
        Strategy process(char c);

        /**
         * Reset the internal variables such that the parser can be reused for the next entry.
         */
        void reset();
    }

    private class HeaderStrategy implements Strategy {

        private final StringBuilder idBuffer = new StringBuilder();
        private boolean skip_attributes = false;

        @Override
        public Strategy process(char c) {
            if (c == '\n') {
                linenumber++;
                // change strategy
                return sequence;
            }
            if (c != '>' && !Character.isWhitespace(c) && !skip_attributes) {
                idBuffer.append(c);
            }
            if (c == ' ') {
                skip_attributes = true;
            }
            return this;
        }

        @Override
        public void reset() {
            idBuffer.delete(0, idBuffer.length());
            skip_attributes = false;
        }
    }

    private class SequenceStrategy implements Strategy {

        private int lineBaseLength = 0;
        private int lineByteLength = 0;
        private int currentLineBaseLength = 0;
        private int currentLineByteLength = 0;
        private int sequenceLength = 0;
        private long sequenceStartOffset = 0;
        private boolean shorterline = false;
        private int nonbaseLength = 0;

        @Override
        public Strategy process(char c) {
            if (c == '>') {
                // change strategy
                return header;
            }
            if (Character.isAlphabetic(c) && shorterline) {
                throw new IllegalStateException("Line " + linenumber + " has not the same length as the previous line. " + currentLineByteLength + " " + lineByteLength);
            }
            if (Character.isAlphabetic(c)) {
                currentLineBaseLength++;
                sequenceLength++;
            } else {
                nonbaseLength++;
                if (c == '\n' || c == '\0') {
                    linenumber++;
                    currentLineByteLength = currentLineBaseLength + nonbaseLength;
                    if (lineBaseLength == 0) {
                        lineBaseLength = currentLineBaseLength;
                        lineByteLength = currentLineByteLength;
                    } else {
                        if (lineBaseLength != currentLineBaseLength || lineByteLength != currentLineByteLength) {
                            // check if next element is a header and fail if not
                            shorterline = true;
                        }
                    }
                    currentLineBaseLength = 0;
                    currentLineByteLength = 0;
                    nonbaseLength = 0;
                }
            }

            return this;
        }

        @Override
        public void reset() {
            currentLineBaseLength = 0;
            currentLineByteLength = 0;
            nonbaseLength = 0;
            sequenceLength = 0;
            lineBaseLength = 0;
            shorterline = false;
        }
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    private FastaIndexEntry createIndexEntry() {
        FastaIndexEntry indexEntry = new FastaIndexEntry(
            header.idBuffer.toString(),
            sequence.sequenceLength,
            sequence.sequenceStartOffset,
            sequence.lineBaseLength,
            sequence.lineByteLength
        );
        return indexEntry;
    }

    /**
     * Parses the fasta file and creates index entries for all sequences. Does not check if a sequence identifier is
     * unique. Does not check if the fasta file is a valid file.
     *
     * @param fastaFilePath A fasta file.
     * @throws java.io.IOException
     * @return A list of all indexentries for all sequences in the fasta file.
     */
    public List<FastaIndexEntry> createIndex(Path fastaFilePath) throws IOException {
        List<FastaIndexEntry> list = new LinkedList<>();
        linenumber = 0;
        try (FileChannel fileChannel = (FileChannel) Files.newByteChannel(fastaFilePath, StandardOpenOption.READ)) {
            long filesize = fileChannel.size();
            long offset = 0;
            Strategy strategy = header;

            ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
            CharBuffer charBuffer;
            CharsetDecoder decoder = StandardCharsets.ISO_8859_1.newDecoder();
            while (fileChannel.position() < filesize) {
                byteBuffer.clear();
                fileChannel.read(byteBuffer);
                byteBuffer.flip();
                charBuffer = decoder.decode(byteBuffer);
                while (charBuffer.hasRemaining()) {
                    char get = charBuffer.get();
                    Strategy process = strategy.process(get);
                    if (!process.equals(strategy)) {
                        if (process == header) {
                            list.add(createIndexEntry());
                        }
                        if (process == sequence) {
                            sequence.sequenceStartOffset = charBuffer.position() + offset;
                        }
                        strategy = process;
                        strategy.reset();
                    }
                }
                offset += charBuffer.position();
            }
            list.add(createIndexEntry());
        }
        return list;
    }
}
