import java.util.Scanner;

/**
 * Main.java
 *
 * Entry point of the SmartFileUtility application.
 * Responsibilities:
 *   - Display the main menu in a loop
 *   - Read the user's choice
 *   - Delegate every operation to FileService
 *   - Exit cleanly when the user chooses option 8
 *
 * Design note: All I/O logic lives here so that FileService stays
 * focused purely on file operations (Separation of Concerns).
 */
public class Main {

    // Single Scanner instance shared for the whole application lifetime.
    // Closing it mid-program would close System.in and break further reads.
    private static final Scanner scanner = new Scanner(System.in);

    // Single FileService instance – it is stateless, so one is enough.
    private static final FileService fileService = new FileService();

    public static void main(String[] args) {

        Utility.printBanner();          // welcome banner
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readIntChoice();

            switch (choice) {
                case 1 -> handleCreateFile();
                case 2 -> handleWriteToFile();
                case 3 -> handleReadFile();
                case 4 -> handleModifyFile();
                case 5 -> handleAppendToFile();
                case 6 -> handleDeleteFile();
                case 7 -> handleFileDetails();
                case 8 -> {
                    running = false;
                    Utility.printLine();
                    System.out.println("  Thank you for using SmartFileUtility. Goodbye!");
                    Utility.printLine();
                }
                default -> System.out.println(
                        Utility.WARNING + " Invalid choice. Please enter a number between 1 and 8.");
            }
        }

        scanner.close();
    }

    // -----------------------------------------------------------------------
    //  Menu display
    // -----------------------------------------------------------------------

    /** Prints the numbered menu options to the console. */
    private static void printMenu() {
        System.out.println();
        Utility.printLine();
        System.out.println("               SMART FILE UTILITY – MAIN MENU");
        Utility.printLine();
        System.out.println("  1. Create a new text file");
        System.out.println("  2. Write content to a file");
        System.out.println("  3. Read and display file contents");
        System.out.println("  4. Modify file contents (overwrite)");
        System.out.println("  5. Append content to a file");
        System.out.println("  6. Delete a file");
        System.out.println("  7. Display file details");
        System.out.println("  8. Exit");
        Utility.printLine();
        System.out.print("  Enter your choice: ");
    }

    // -----------------------------------------------------------------------
    //  Handler methods – one per menu option
    // -----------------------------------------------------------------------

    /** Option 1: Create a new (empty) text file. */
    private static void handleCreateFile() {
        Utility.printSectionHeader("CREATE NEW FILE");
        String fileName = promptFileName("Enter file name to create (e.g., notes.txt): ");
        if (fileName == null) return;
        fileService.createFile(fileName);
    }

    /** Option 2: Write (overwrite) content to a file. */
    private static void handleWriteToFile() {
        Utility.printSectionHeader("WRITE TO FILE");
        String fileName = promptFileName("Enter file name to write to: ");
        if (fileName == null) return;
        System.out.println("  Enter content (type END on a new line to finish):");
        String content = readMultilineInput();
        fileService.writeToFile(fileName, content);
    }

    /** Option 3: Read and display the full content of a file. */
    private static void handleReadFile() {
        Utility.printSectionHeader("READ FILE");
        String fileName = promptFileName("Enter file name to read: ");
        if (fileName == null) return;
        fileService.readFile(fileName);
    }

    /** Option 4: Replace the entire content of an existing file. */
    private static void handleModifyFile() {
        Utility.printSectionHeader("MODIFY FILE CONTENTS");
        String fileName = promptFileName("Enter file name to modify: ");
        if (fileName == null) return;
        System.out.println("  Enter new content (type END on a new line to finish):");
        String content = readMultilineInput();
        fileService.modifyFile(fileName, content);
    }

    /** Option 5: Append new content to the end of a file. */
    private static void handleAppendToFile() {
        Utility.printSectionHeader("APPEND TO FILE");
        String fileName = promptFileName("Enter file name to append to: ");
        if (fileName == null) return;
        System.out.println("  Enter content to append (type END on a new line to finish):");
        String content = readMultilineInput();
        fileService.appendToFile(fileName, content);
    }

    /** Option 6: Permanently delete a file after confirmation. */
    private static void handleDeleteFile() {
        Utility.printSectionHeader("DELETE FILE");
        String fileName = promptFileName("Enter file name to delete: ");
        if (fileName == null) return;

        // Ask for confirmation before a destructive operation
        System.out.print("  Are you sure you want to delete \"" + fileName + "\"? (yes/no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();
        if (confirm.equals("yes") || confirm.equals("y")) {
            fileService.deleteFile(fileName);
        } else {
            System.out.println(Utility.INFO + " Delete operation cancelled.");
        }
    }

    /** Option 7: Show metadata about a file (name, path, size, modified date). */
    private static void handleFileDetails() {
        Utility.printSectionHeader("FILE DETAILS");
        String fileName = promptFileName("Enter file name to inspect: ");
        if (fileName == null) return;
        fileService.displayFileDetails(fileName);
    }

    // -----------------------------------------------------------------------
    //  Input helpers
    // -----------------------------------------------------------------------

    /**
     * Prompts for and validates a file name.
     *
     * @param prompt  The message shown to the user.
     * @return        A trimmed, non-empty, valid file name; or null if invalid.
     */
    private static String promptFileName(String prompt) {
        System.out.print("  " + prompt);
        String name = scanner.nextLine().trim();

        if (name.isEmpty()) {
            System.out.println(Utility.WARNING + " File name cannot be empty.");
            return null;
        }
        if (!Utility.isValidFileName(name)) {
            System.out.println(Utility.WARNING
                    + " Invalid file name. Avoid characters like  \\ / : * ? \" < > |");
            return null;
        }
        return name;
    }

    /**
     * Reads multiple lines of text until the user types "END" on its own line.
     *
     * @return The collected lines joined by system line separators.
     */
    private static String readMultilineInput() {
        StringBuilder sb = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equalsIgnoreCase("END")) {
            sb.append(line).append(System.lineSeparator());
        }
        return sb.toString();
    }

    /**
     * Reads a single integer from the user.
     * Returns -1 (which triggers the "invalid choice" message) if input is
     * not a number, so the application never crashes on bad input.
     *
     * @return The parsed integer, or -1 on non-numeric input.
     */
    private static int readIntChoice() {
        try {
            String input = scanner.nextLine().trim();
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1;  // handled gracefully in the switch-default branch
        }
    }
}