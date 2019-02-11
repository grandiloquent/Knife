package euphoria.psycho.common.comparator;

import java.io.File;
import java.io.Serializable;
import java.util.Comparator;

import euphoria.psycho.common.FileUtils;

public class SizeFileComparator implements Comparator<File>, Serializable {

    /** Size comparator instance - directories are treated as zero size */
    public static final Comparator<File> SIZE_COMPARATOR = new SizeFileComparator();

    /** Reverse size comparator instance - directories are treated as zero size */
    public static final Comparator<File> SIZE_REVERSE = new ReverseComparator<File>(SIZE_COMPARATOR);

    /**
     * Size comparator instance which sums the size of a directory's contents
     * using {@link FileUtils#sizeOfDirectory(File)}
     */
    public static final Comparator<File> SIZE_SUMDIR_COMPARATOR = new SizeFileComparator(true);

    /**
     * Reverse size comparator instance which sums the size of a directory's contents
     * using {@link FileUtils#sizeOfDirectory(File)}
     */
    public static final Comparator<File> SIZE_SUMDIR_REVERSE = new ReverseComparator<File>(SIZE_SUMDIR_COMPARATOR);

    /** Whether the sum of the directory's contents should be calculated. */
    private final boolean sumDirectoryContents;

    /**
     * Construct a file size comparator instance (directories treated as zero size).
     */
    public SizeFileComparator() {
        this.sumDirectoryContents = false;
    }

    /**
     * Construct a file size comparator instance specifying whether the size of
     * the directory contents should be aggregated.
     * <p>
     * If the <code>sumDirectoryContents</code> is <code>true</code> The size of
     * directories is calculated using  {@link FileUtils#sizeOfDirectory(File)}.
     *
     * @param sumDirectoryContents <code>true</code> if the sum of the directoryies contents
     *  should be calculated, otherwise <code>false</code> if directories should be treated
     *  as size zero (see {@link FileUtils#sizeOfDirectory(File)}).
     */
    public SizeFileComparator(boolean sumDirectoryContents) {
        this.sumDirectoryContents = sumDirectoryContents;
    }

    /**
     * Compare the length of two files.
     *
     * @param obj1 The first file to compare
     * @param obj2 The second file to compare
     * @return a negative value if the first file's length
     * is less than the second, zero if the lengths are the
     * same and a positive value if the first files length
     * is greater than the second file.
     *
     */
    public int compare(File file1, File file2) {
        long size1 = 0;
        if (file1.isDirectory()) {
            size1 = sumDirectoryContents && file1.exists() ? FileUtils.sizeOfDirectory(file1) : 0;
        } else {
            size1 = file1.length();
        }
        long size2 = 0;
        if (file2.isDirectory()) {
            size2 = sumDirectoryContents && file2.exists() ? FileUtils.sizeOfDirectory(file2) : 0;
        } else {
            size2 = file2.length();
        }
        long result = size1 - size2;
        if (result < 0) {
            return -1;
        } else if (result > 0) {
            return 1;
        } else {
            return 0;
        }
    }
}