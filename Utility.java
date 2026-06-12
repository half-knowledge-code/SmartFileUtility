/**
 * Utility.java
 *
 * A collection of static helper methods and constants used across the
 * SmartFileUtility application.
 *
 * Keeping these here means:
 *   - Main.java and FileService.java do not duplicate formatting logic.
 *   - If we ever want to change the separator line style or status icons,
 *     there is exactly one place to edit.
 *
 * All methods are static because Utility holds no state – it is purely
 * a bag of reusable functions (similar to java.util.Math).
 */
public class Utility {

    // ------------------------------------------------------------------
    //  Status prefixes – used in console messages throughout the app
    // ------------------------------------------------------------------

    /** Green-ish prefix for successful operations. */
    public static final String SUCCESS = "[OK]    ";

    /** Red-ish prefix for errors (e.g. file not found, IO failure). */
    public static final String ERROR   = "[ERROR] ";

    /** Yellow-ish prefix for warnings (e.g. file already exists). */
    public static final String WARNING = "[WARN]  ";

    /** Blue-ish prefix for neutral informational messages. */
    public static final String INFO    = "[INFO]  ";

    // Separator character count kept as a constant so every printLine()
    // call produces the exact same width without magic numbers in the code.
    private static final int LINE_WIDTH = 60;
    private static final String SEPARATOR = "-".repeat(LINE_WIDTH);

    // ------------------------------------------------------------------
    //  Constructor – private to prevent instantiation.
    //  This is a utility class; it should never be instantiated.
    // ------------------------------------------------------------------
    private Utility() {}

    // ------------------------------------------------------------------
    //  Console formatting helpers
    // ------------------------------------------------------------------

    /**
     * Prints a horizontal separator line to the console.
     * Used to visually divide sections of output.
     */
    public static void printLine() {
        System.out.println("  " + SEPARATOR);
    }

    /**
     * Prints the application welcome banner shown once at startup.
     */
    public static void printBanner() {
        System.out.println();
        printLine();
        System.out.println("           SmartFileUtility  v1.0");
        System.out.println("        A Console-Based File Manager");
        printLine();
        System.out.println("  Built with Java  |  File I/O Demonstration");
        printLine();
    }

    /**
     * Prints a clearly labelled section header before each operation.
     * This makes it easy to tell which operation is currently running
     * when scrolling through console output.
     *
     * @param title Short title for the current operation (e.g. "READ FILE").
     */
    public static void printSectionHeader(String title) {
        System.out.println();
        printLine();
        System.out.println("  >> " + title);
        printLine();
    }

    // ------------------------------------------------------------------
    //  Validation helpers
    // ------------------------------------------------------------------

    /**
     * Checks whether a given file name is acceptable for use on common
     * operating systems (Windows, Linux, macOS).
     *
     * A name is considered invalid if it:
     *   - Is null or blank.
     *   - Contains any of the characters forbidden by Windows NTFS:
     *     \  /  :  *  ?  "  <  >  |
     *   - Starts or ends with a space or period (causes issues on Windows).
     *
     * Note: This is a pragmatic, OS-compatible check for a student project.
     * A production tool would also validate against reserved device names
     * (CON, PRN, AUX, NUL, COM1–COM9, LPT1–LPT9 on Windows).
     *
     * @param fileName The file name to validate.
     * @return true if the name is acceptable; false otherwise.
     */
    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return false;
        }
        // Characters that are illegal in file names on Windows (and most OS)
        String illegalChars = "\\/:*?\"<>|";
        for (char c : illegalChars.toCharArray()) {
            if (fileName.indexOf(c) != -1) {
                return false;
            }
        }
        // Names must not start or end with a space or dot
        if (fileName.startsWith(" ") || fileName.endsWith(" ")
                || fileName.startsWith(".") || fileName.endsWith(".")) {
            return false;
        }
        return true;
    }

    // ------------------------------------------------------------------
    //  Formatting helpers
    // ------------------------------------------------------------------

    /**
     * Converts a raw byte count into a human-readable file size string.
     *
     * Examples:
     *   formatFileSize(500)       →  "500 B"
     *   formatFileSize(2048)      →  "2.00 KB"
     *   formatFileSize(1572864)   →  "1.50 MB"
     *
     * Why 1024 and not 1000?
     *   File systems measure in binary units (1 KB = 1024 bytes), so we
     *   follow the same convention that Windows Explorer and most tools use.
     *
     * @param bytes File size in bytes (from File.length()).
     * @return A formatted string with the appropriate unit.
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        }
    }
}