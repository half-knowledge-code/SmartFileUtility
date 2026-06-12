import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * FileService.java
 *
 * Contains all file-operation logic for SmartFileUtility.
 * This class is intentionally kept free of Scanner / System.out calls so that
 * the user interface (Main.java) and the business logic stay cleanly separated.
 *
 * Every public method:
 *   1. Validates its arguments.
 *   2. Performs the requested operation.
 *   3. Prints a success or error message using the Utility constants.
 *   4. Handles IOException in a try-catch and never lets it propagate
 *      to Main – the application should never crash on a file error.
 *
 * Regarding I/O classes used:
 *   - java.io.File          – represents a file path; used for create / delete
 *                             / metadata queries.
 *   - FileReader / FileWriter – low-level character-based streams.
 *   - BufferedReader / BufferedWriter – wrap the above to improve performance
 *     (batch reads / writes) and provide readLine() / newLine() convenience.
 *   - try-with-resources    – guarantees streams are closed even if an
 *     exception is thrown mid-operation.
 */
public class FileService {

    // ------------------------------------------------------------------
    //  1. CREATE
    // ------------------------------------------------------------------

    /**
     * Creates a new, empty text file with the given name in the working directory.
     * If the file already exists the user is informed and no data is lost.
     *
     * @param fileName Name of the file to create (e.g. "notes.txt").
     */
    public void createFile(String fileName) {
        File file = new File(fileName);

        try {
            if (file.exists()) {
                System.out.println(Utility.WARNING
                        + " File \"" + fileName + "\" already exists. No changes made.");
                return;
            }

            // createNewFile() atomically creates the file only if it does not
            // already exist – the return value tells us whether it was created.
            boolean created = file.createNewFile();
            if (created) {
                System.out.println(Utility.SUCCESS
                        + " File \"" + fileName + "\" created successfully.");
            } else {
                // Shouldn't normally happen given the exists() check above,
                // but we guard it anyway.
                System.out.println(Utility.WARNING
                        + " File \"" + fileName + "\" could not be created.");
            }

        } catch (IOException e) {
            System.out.println(Utility.ERROR
                    + " Failed to create file: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  2. WRITE  (overwrite – same implementation as modify; see method 4)
    // ------------------------------------------------------------------

    /**
     * Writes the given content to a file, replacing any existing content.
     * If the file does not exist it is created automatically by FileWriter.
     *
     * Why FileWriter(file, false)?
     *   The second argument is the "append" flag. Passing false means the file
     *   is truncated first – exactly what an overwrite should do.
     *
     * @param fileName Name of the target file.
     * @param content  Text to write (may contain multiple lines).
     */
    public void writeToFile(String fileName, String content) {
        // We re-use the shared overwrite logic; both "write" and "modify"
        // do the same thing under the hood – replace all existing content.
        overwriteFile(fileName, content, "written to");
    }

    // ------------------------------------------------------------------
    //  3. READ
    // ------------------------------------------------------------------

    /**
     * Reads the entire content of a text file and prints it to the console,
     * including line numbers for readability.
     *
     * @param fileName Name of the file to read.
     */
    public void readFile(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println(Utility.ERROR + " File \"" + fileName + "\" does not exist.");
            return;
        }
        if (file.length() == 0) {
            System.out.println(Utility.INFO + " File \"" + fileName + "\" is empty.");
            return;
        }

        System.out.println();
        Utility.printLine();
        System.out.printf("  Contents of: %s%n", fileName);
        Utility.printLine();

        // try-with-resources: FileReader → BufferedReader
        // BufferedReader wraps FileReader so that reads are buffered (fast)
        // and readLine() is available (FileReader only exposes read(char[]).
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                // Print each line with its line number, padded to 4 digits
                System.out.printf("  %4d | %s%n", lineNumber++, line);
            }
            Utility.printLine();
            System.out.println(Utility.SUCCESS + " File read successfully. ("
                    + (lineNumber - 1) + " line(s))");

        } catch (IOException e) {
            System.out.println(Utility.ERROR + " Error reading file: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  4. MODIFY  (overwrite existing content)
    // ------------------------------------------------------------------

    /**
     * Replaces the entire content of an existing file with new content.
     * Unlike writeToFile(), this method first checks that the file exists,
     * because "modify" implies something is already there.
     *
     * @param fileName Name of the file to modify.
     * @param content  New text that will replace all current content.
     */
    public void modifyFile(String fileName, String content) {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println(Utility.ERROR
                    + " File \"" + fileName + "\" does not exist. Cannot modify.");
            return;
        }
        overwriteFile(fileName, content, "modified");
    }

    // ------------------------------------------------------------------
    //  5. APPEND
    // ------------------------------------------------------------------

    /**
     * Appends new content to the end of an existing file without
     * disturbing what is already there.
     *
     * Why FileWriter(file, true)?
     *   Passing true sets append mode – the stream's write position starts
     *   at the end of the file rather than at the beginning.
     *
     * @param fileName Name of the file to append to.
     * @param content  Text to add at the end of the file.
     */
    public void appendToFile(String fileName, String content) {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println(Utility.ERROR
                    + " File \"" + fileName + "\" does not exist. Cannot append.");
            return;
        }

        // FileWriter in append mode (true) + BufferedWriter for efficiency
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(content);
            System.out.println(Utility.SUCCESS
                    + " Content appended to \"" + fileName + "\" successfully.");

        } catch (IOException e) {
            System.out.println(Utility.ERROR + " Error appending to file: " + e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    //  6. DELETE
    // ------------------------------------------------------------------

    /**
     * Permanently deletes the specified file.
     * The caller (Main.java) is responsible for asking the user to confirm
     * before invoking this method.
     *
     * @param fileName Name of the file to delete.
     */
    public void deleteFile(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println(Utility.ERROR + " File \"" + fileName + "\" does not exist.");
            return;
        }

        // File.delete() returns false if the OS refuses to delete the file
        // (e.g. because another process has it open).
        if (file.delete()) {
            System.out.println(Utility.SUCCESS
                    + " File \"" + fileName + "\" deleted successfully.");
        } else {
            System.out.println(Utility.ERROR
                    + " Could not delete \"" + fileName
                    + "\". It may be in use by another process.");
        }
    }

    // ------------------------------------------------------------------
    //  7. FILE DETAILS
    // ------------------------------------------------------------------

    /**
     * Displays metadata for the given file:
     *   - File name
     *   - Absolute path on disk
     *   - Size (formatted as B / KB / MB)
     *   - Last-modified timestamp
     *   - Read / write permissions
     *
     * @param fileName Name of the file to inspect.
     */
    public void displayFileDetails(String fileName) {
        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println(Utility.ERROR + " File \"" + fileName + "\" does not exist.");
            return;
        }

        // Format the last-modified timestamp into a readable date-time string
        String lastModified = new SimpleDateFormat("dd-MMM-yyyy  HH:mm:ss")
                .format(new Date(file.lastModified()));

        System.out.println();
        Utility.printLine();
        System.out.println("  FILE DETAILS");
        Utility.printLine();
        System.out.printf("  %-20s : %s%n",  "File Name",      file.getName());
        System.out.printf("  %-20s : %s%n",  "Absolute Path",  file.getAbsolutePath());
        System.out.printf("  %-20s : %s%n",  "File Size",      Utility.formatFileSize(file.length()));
        System.out.printf("  %-20s : %s%n",  "Last Modified",  lastModified);
        System.out.printf("  %-20s : %s%n",  "Readable",       file.canRead()  ? "Yes" : "No");
        System.out.printf("  %-20s : %s%n",  "Writable",       file.canWrite() ? "Yes" : "No");
        Utility.printLine();
    }

    // ------------------------------------------------------------------
    //  Private helper
    // ------------------------------------------------------------------

    /**
     * Core overwrite logic shared by writeToFile() and modifyFile().
     *
     * Opens a FileWriter with append=false (i.e. truncate-and-write),
     * wraps it in a BufferedWriter, and writes the supplied content.
     *
     * @param fileName   Target file.
     * @param content    Text to write.
     * @param actionWord Verb used in the success message (e.g. "written to").
     */
    private void overwriteFile(String fileName, String content, String actionWord) {
        File file = new File(fileName);

        // FileWriter(file, false) – the 'false' means: do NOT append, i.e. overwrite
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, false))) {
            writer.write(content);
            System.out.println(Utility.SUCCESS
                    + " Content " + actionWord + " \"" + fileName + "\" successfully.");

        } catch (IOException e) {
            System.out.println(Utility.ERROR + " Error writing to file: " + e.getMessage());
        }
    }
}