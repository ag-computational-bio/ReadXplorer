package de.cebitec.vamp.externalSort;

import de.cebitec.vamp.util.Benchmark;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import net.sf.samtools.*;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

/**
 * This class sorts Sam or Bam files by read sequence and returns a sorted Bam
 * file. It uses a merge sort which creates temporary files for merging to save
 * memory. Created files will be removed after sorting.
 *
 * @author jstraube, rhilker
 */
public abstract class ExternalSortBAM {

    /**
     * The sort criterion of this sorter.
     */
    public static String CRITERION = "";
// readers and writers needed for the files
    private SAMFileHeader samHeader;
    private File inputFile;
    private File sortedFile;
    private String chunkName;
    private InputOutput io;
    private final int chunkSize = 500000;
    private int workunits;
    private ProgressHandle ph;

    /**
     * This class sorts Sam or Bam files by the criterion set in the completing
     * subclasses and returns a sorted Bam file. It uses a merge sort which
     * creates temporary files for merging to save memory. Created files will be
     * removed after sorting.
     *
     * @param inputFile the input file to sort
     */
    public ExternalSortBAM(File inputFile) {
        this.inputFile = inputFile;
    }

    /**
     * Handles the sorting of the bam file set during creation of this object.
     * It uses a merge sort implementation.
     */
    public void sort() {
        this.io = IOProvider.getDefault().getIO(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.output.name"), false);
        io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.start", CRITERION));
        long start = System.currentTimeMillis();
        this.ph = ProgressHandleFactory.createHandle(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.progress.name"));
        this.externalSort(this.inputFile);
        long finish = System.currentTimeMillis();
        io.getOut().println(Benchmark.calculateDuration(start, finish, NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.finished", CRITERION)));
        ph.finish();
        //  io.getOut().close();
        //     io.closeInputOutput();
    }

    /**
     * Counts the number of records in the bam file. Counting the lines itself
     * does not work, since a bam file is a zipped archive.
     * @param file the file whose record number needs to be known
     */
    private void countLines(File file) {
        SAMFileReader samReader = new SAMFileReader(file);
        samHeader = samReader.getFileHeader();
        SAMRecordIterator itLine = samReader.iterator();
        int lines = 0;
        while (itLine.hasNext()) {
            try {
                itLine.next();
                lines++;
            } catch (SAMFormatException e) {
                ++lines;
                io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.SamFormatException", lines));
            }
        }
        workunits = lines *2;
        ph.start(workunits);
        itLine.close();
        samReader.close();
    }

    /**
     * Actually performs the sorting.
     * @param inputFile the input file to sort
     */
    private void externalSort(File inputFile) {
        this.countLines(inputFile);

        SAMFileReader samReader = new SAMFileReader(inputFile);
        SAMRecordIterator it = samReader.iterator();

        ArrayList<SAMRecord> chunkSizeRows = new ArrayList<SAMRecord>();

        int numFiles = 0;
        workunits = 0;
        File f;
        BAMFileWriter bfw;
        while (it.hasNext()) {

            try {

// get chunkSize rows
                for (int i = 0; i < chunkSize; i++) {
                    if (it.hasNext()) {
                        chunkSizeRows.add(it.next());
                    } else {
                        break;
                    }
                }
// sort the rows

                chunkSizeRows = mergeSort(chunkSizeRows);

// write to disk
                chunkName = System.getProperty("user.home") + "/chunk";
                f = new File(chunkName + numFiles);
                bfw = new BAMFileWriter(f);
                bfw.setSortOrder(SAMFileHeader.SortOrder.unsorted, true);
                bfw.setHeader(samHeader);
                for (int i = 0; i < chunkSizeRows.size(); i++) {
                    bfw.addAlignment(chunkSizeRows.get(i));
                }
                bfw.close();
                ++numFiles;

                workunits += chunkSizeRows.size();
                chunkSizeRows.clear();
                ph.progress(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.progress.chunks.creating"), workunits);

            } catch (SAMFormatException e) {
                //ignore line
                io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.SamFormatException2"));
            }
        }
        it.close();
        samReader.close();
        io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.chunks.created", numFiles));
        io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.sort.chunks.merge"));

        this.mergeFiles(inputFile, numFiles);

    }

    /**
     * Merges all sorted files into one large sorted file.
     * @param baseFile the original file to sort
     * @param numFiles the number of intermediate files which are internally
     * already sorted
     */
    private void mergeFiles(File baseFile, int numFiles) {
        try {

            List<File> files = new ArrayList<File>();
            List<SAMRecordIterator> mergeIt = new ArrayList<SAMRecordIterator>();
            List<SAMFileReader> readerList = new ArrayList<SAMFileReader>();
            List<SAMRecord> filerows = new ArrayList<SAMRecord>();
            String[] s = baseFile.getName().split("\\.");
            String name = baseFile.getParent() + "/" + s[0] + ".sort_" + CRITERION + ".bam";
            File mergedFile = new File(name);
            BAMFileWriter bfw = new BAMFileWriter(mergedFile);
            bfw.setHeader(samHeader);
            boolean someFileStillHasRows = false;
            File f;
            SAMFileReader fileReader;
            SAMRecord line;

            for (int i = 0; i < numFiles; i++) {
                f = new File(chunkName + i);
                files.add(f);
                fileReader = new SAMFileReader(f);
                readerList.add(fileReader);
                mergeIt.add(fileReader.iterator());
                // get the first row
                line = mergeIt.get(i).next();
                if (line != null) {
                    filerows.add(line);
                    someFileStillHasRows = true;
                } else {
                    filerows.add(null);
                }

            }

            SAMRecord row;
            String min;
            int minIndex;
            while (someFileStillHasRows) {
                
                row = filerows.get(0);
                if (row != null) {
                    min = row.getReadString();
                    minIndex = 0;
                } else {
                    min = null;
                    minIndex = -1;
                }

                // check which one is min
                for (int i = 1; i < filerows.size(); i++) {
                    row = filerows.get(i);
                    if (min != null) {

                        if (row != null && row.getReadString().compareTo(min) < 0) {
                            minIndex = i;
                            min = filerows.get(i).getReadString();
                        }
                    } else {
                        if (row != null) {
                            min = row.getReadString();
                            minIndex = i;
                        }
                    }
                }

                if (minIndex < 0) {
                    someFileStillHasRows = false;
                } else {
                    // write to the sorted file
                    bfw.addAlignment(filerows.get(minIndex));
                    ph.progress(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.progress.write.sortedFile"), workunits++);

                    // get another row from the file that had the min
                    line = mergeIt.get(minIndex).next();
                    if (line != null) {
                        filerows.set(minIndex, line);
                    } else {
                        filerows.set(minIndex, null);
                    }
                }
                // check if one still has rows
                for (int i = 0; i < filerows.size(); i++) {

                    someFileStillHasRows = false;
                    if (filerows.get(i) != null) {
                        if (minIndex < 0) {
                            System.out.println("mindex lt 0 and found row not null" + filerows.get(i).toString());
                            System.exit(-1);
                        }
                        someFileStillHasRows = true;
                        break;
                    }
                }

                // check the actual files one more time
                if (!someFileStillHasRows) {

                    //write the last one not covered above
                    for (int i = 0; i < filerows.size(); i++) {
                        if (filerows.get(i) == null) {
                            line = mergeIt.get(i).next();
                            if (line != null) {
                                someFileStillHasRows = true;
                                filerows.set(i, line);
                            }
                        }

                    }
                }
            }

            bfw.close();
            filerows.clear();

            // close all the files and delete them
            for (int i = 0; i < mergeIt.size(); i++) {
                mergeIt.get(i).close();
                readerList.get(i).close();
                try {
                    Files.delete(files.get(i).toPath());
                } catch (IOException e) {
                    io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.merge.FileDeletionError", files.get(i).getAbsolutePath()));
                }
            }

            for (int i = 0; i < numFiles; i++) {
                mergeIt.get(i).close();
            }
            mergeIt.clear();
            this.sortedFile = mergedFile;

        } catch (Exception ex) {
            io.getOut().println(NbBundle.getMessage(ExternalSortBAM.class, "ExternalSort.merge.GeneralError"));
        }
    }

    /**
     * Sort an arrayList of arrays based on the ith column.
     * @param records the records to merge sort
     */
    private ArrayList<SAMRecord> mergeSort(ArrayList<SAMRecord> records) {
        ArrayList<SAMRecord> left = new ArrayList<SAMRecord>();
        ArrayList<SAMRecord> right = new ArrayList<SAMRecord>();
        if (records.size() <= 1) {
            return records;
        } else {
            int middle = records.size() / 2;
            left.addAll(records.subList(0, middle));
            right.addAll(records.subList(middle, records.size()));
            left = mergeSort(left);
            right = mergeSort(right);
            
            return merge(left, right);
        }

    }

    /**
     * Merges the results of the mergeSort back together.
     */
    private ArrayList<SAMRecord> merge(ArrayList<SAMRecord> left, ArrayList<SAMRecord> right) {
        ArrayList<SAMRecord> result = new ArrayList<SAMRecord>();
        int sortValue;
        while (left.size() > 0 && right.size() > 0) {
            sortValue = this.compareTwoEntries(left.get(0), right.get(0));
            if (sortValue <= 0) {
                result.add(left.get(0));
                left.remove(0);
            } else {
                result.add(right.get(0));
                right.remove(0);
            }
        }
        if (left.size() > 0) {
            result.addAll(left);
        }
        if (right.size() > 0) {
            result.addAll(right);
        }
        return result;
    }
    

    /**
     * Needs to be implemented and decides upon the sorting criteria used for
     * the merge sort algorithm. The first record is compared to the second one.
     * If the first record comes first, the returned value is < 0, if they are
     * equal it is 0 and if the second record comes first the value is > 0.
     *
     * @param record1 first record
     * @param record2 second record
     * @return
     */
    protected abstract int compareTwoEntries(SAMRecord record1, SAMRecord record2);

    /**
     * @return The file with the sorted data.
     */
    public File getSortedFile() {
        return sortedFile;
    }
}