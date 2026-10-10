package com.inventory.library.cli;

import com.inventory.library.exception.BookNotFoundException;
import com.inventory.library.exception.DuplicateBookException;
import com.inventory.library.exception.InvalidBookDataException;
import com.inventory.library.model.Book;
import com.inventory.library.repository.InMemoryBookRepository;
import com.inventory.library.service.BookService;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

/**
 * Menu-driven Command-Line Interface (CLI) application for managing
 * a library's book inventory.
 *
 * Refactored in Week 4:
 * - Employs Book.builder() for cleaner entity construction.
 * - Adds dynamic sorting (by Title, Author, Year, ISBN) and pagination.
 * - Formatted tabular layout with performance latency metrics.
 */
public class LibraryConsoleApp {

    private final BookService bookService;
    private final Scanner scanner;

    public LibraryConsoleApp(BookService bookService, Scanner scanner) {
        this.bookService = bookService;
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        BookService service = new BookService(new InMemoryBookRepository());
        Scanner sc = new Scanner(System.in);
        LibraryConsoleApp app = new LibraryConsoleApp(service, sc);

        // Preload sample data for immediate testing and presentation
        app.preloadSampleData();

        // Start interactive main loop
        app.run();
    }

    /**
     * Preloads initial sample books using the Builder pattern.
     */
    public void preloadSampleData() {
        try {
            bookService.addBook(Book.builder()
                    .isbn("978-0134685991").title("Effective Java").author("Joshua Bloch").publicationYear(2018).build());
            bookService.addBook(Book.builder()
                    .isbn("978-0596009205").title("Head First Java").author("Kathy Sierra").publicationYear(2022).build());
            bookService.addBook(Book.builder()
                    .isbn("978-0132350884").title("Clean Code").author("Robert C. Martin").publicationYear(2008).build());
            bookService.addBook(Book.builder()
                    .isbn("978-0201633610").title("Design Patterns (GoF)").author("Erich Gamma et al.").publicationYear(1994).build());
            bookService.addBook(Book.builder()
                    .isbn("978-0131103627").title("The C Programming Language").author("Brian W. Kernighan").publicationYear(1988).build());
        } catch (Exception ignored) {
            // Safe fallback during boot
        }
    }

    /**
     * Main application loop displaying menu and processing commands.
     */
    public void run() {
        boolean running = true;
        printHeader();

        while (running) {
            printMenu();
            System.out.print("👉 Enter your choice [1-9]: ");
            String input = scanner.nextLine().trim();

            System.out.println();
            switch (input) {
                case "1" -> handleAddBook();
                case "2" -> handleListBooks();
                case "3" -> handleListBooksSortedOrPaged();
                case "4" -> handleSearchByIsbn();
                case "5" -> handleSearchByKeyword();
                case "6" -> handleUpdateBook();
                case "7" -> handleDeleteBook();
                case "8" -> handleDisplayStatistics();
                case "9" -> {
                    running = false;
                    System.out.println("======================================================");
                    System.out.println("  Thank you for using Library Book Inventory System!  ");
                    System.out.println("======================================================");
                }
                default -> System.out.println("⚠️  Invalid choice! Please select an option between 1 and 9.");
            }
            System.out.println();
        }
    }

    private void printHeader() {
        System.out.println("======================================================");
        System.out.println("      📚 LIBRARY BOOK INVENTORY MANAGEMENT SYSTEM      ");
        System.out.println("      Week 4: Refactored & Optimized Java Architecture ");
        System.out.println("======================================================");
    }

    private void printMenu() {
        System.out.println("------------------------------------------------------");
        System.out.println(" MAIN MENU");
        System.out.println("------------------------------------------------------");
        System.out.println(" 1. ➕ Add New Book (Create)");
        System.out.println(" 2. 📋 View All Books (Default Order)");
        System.out.println(" 3. 🔢 View Books (Sorted / Paginated)");
        System.out.println(" 4. 🔍 Search Book by ISBN (O(1) Lookup)");
        System.out.println(" 5. 🔎 Search Books by Title / Author (Token Indexed)");
        System.out.println(" 6. ✏️  Update Book Information");
        System.out.println(" 7. 🗑️  Delete Book");
        System.out.println(" 8. 📊 View Inventory Statistics");
        System.out.println(" 9. 🚪 Exit Application");
        System.out.println("------------------------------------------------------");
    }

    // --- CRUD Handlers ---

    private void handleAddBook() {
        System.out.println("--- ➕ ADD NEW BOOK ---");
        try {
            System.out.print("Enter ISBN (e.g., 978-0134685991): ");
            String isbn = scanner.nextLine().trim();

            System.out.print("Enter Book Title: ");
            String title = scanner.nextLine().trim();

            System.out.print("Enter Author Name: ");
            String author = scanner.nextLine().trim();

            System.out.print("Enter Publication Year: ");
            int year = readIntegerInput();

            Book newBook = Book.builder()
                    .isbn(isbn)
                    .title(title)
                    .author(author)
                    .publicationYear(year)
                    .build();

            long start = System.nanoTime();
            bookService.addBook(newBook);
            long elapsedMicros = (System.nanoTime() - start) / 1000;

            System.out.println("✅ Book added successfully! [Operation time: " + elapsedMicros + " µs]");
            System.out.println("   " + newBook);
        } catch (DuplicateBookException | InvalidBookDataException e) {
            System.out.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error occurred while adding book: " + e.getMessage());
        }
    }

    private void handleListBooks() {
        System.out.println("--- 📋 ALL BOOKS IN INVENTORY ---");
        long start = System.nanoTime();
        List<Book> books = bookService.getAllBooks();
        long elapsedMicros = (System.nanoTime() - start) / 1000;
        displayBookTable(books);
        System.out.println("⏱️  Query latency: " + elapsedMicros + " µs");
    }

    private void handleListBooksSortedOrPaged() {
        System.out.println("--- 🔢 SORTED & PAGINATED CATALOG ---");
        System.out.println("Sort by: 1. Title | 2. Author | 3. Publication Year | 4. ISBN");
        System.out.print("Select sort field [1-4, Enter for default]: ");
        String sortChoice = scanner.nextLine().trim();

        Comparator<Book> comparator = switch (sortChoice) {
            case "1" -> Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER);
            case "2" -> Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER);
            case "3" -> Comparator.comparingInt(Book::getPublicationYear);
            case "4" -> Comparator.comparing(Book::getIsbn);
            default -> null;
        };

        System.out.print("Enter Page Number (default 1): ");
        String pStr = scanner.nextLine().trim();
        int page = pStr.isEmpty() ? 1 : Math.max(1, Integer.parseInt(pStr));

        System.out.print("Enter Page Size (default 5): ");
        String psStr = scanner.nextLine().trim();
        int pageSize = psStr.isEmpty() ? 5 : Math.max(1, Integer.parseInt(psStr));

        List<Book> pagedBooks = bookService.getBooksPaged(page, pageSize, comparator);
        System.out.println("\nPage " + page + " (Page size: " + pageSize + "):");
        displayBookTable(pagedBooks);
    }

    private void handleSearchByIsbn() {
        System.out.println("--- 🔍 SEARCH BOOK BY ISBN ---");
        System.out.print("Enter ISBN to search: ");
        String isbn = scanner.nextLine().trim();

        long start = System.nanoTime();
        try {
            Book book = bookService.getBookByIsbn(isbn);
            long elapsedMicros = (System.nanoTime() - start) / 1000;
            System.out.println("✅ Book found [Indexed lookup: " + elapsedMicros + " µs]:");
            System.out.println("   " + book);
        } catch (BookNotFoundException | InvalidBookDataException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void handleSearchByKeyword() {
        System.out.println("--- 🔎 SEARCH BOOKS BY TITLE / AUTHOR ---");
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim();

        long start = System.nanoTime();
        List<Book> matches = bookService.searchBooks(keyword);
        long elapsedMicros = (System.nanoTime() - start) / 1000;

        if (matches.isEmpty()) {
            System.out.println("ℹ️  No books found matching keyword '" + keyword + "'.");
        } else {
            System.out.println("✅ Found " + matches.size() + " matching book(s) [Search time: " + elapsedMicros + " µs]:");
            displayBookTable(matches);
        }
    }

    private void handleUpdateBook() {
        System.out.println("--- ✏️ UPDATE BOOK INFORMATION ---");
        System.out.print("Enter ISBN of the book to update: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book current = bookService.getBookByIsbn(isbn);
            System.out.println("Current details: " + current);

            System.out.print("Enter New Title (leave blank to keep '" + current.getTitle() + "'): ");
            String newTitle = scanner.nextLine().trim();
            if (newTitle.isEmpty()) {
                newTitle = current.getTitle();
            }

            System.out.print("Enter New Author (leave blank to keep '" + current.getAuthor() + "'): ");
            String newAuthor = scanner.nextLine().trim();
            if (newAuthor.isEmpty()) {
                newAuthor = current.getAuthor();
            }

            System.out.print("Enter New Publication Year (leave blank to keep " + current.getPublicationYear() + "): ");
            String yearInput = scanner.nextLine().trim();
            int newYear = current.getPublicationYear();
            if (!yearInput.isEmpty()) {
                try {
                    newYear = Integer.parseInt(yearInput);
                } catch (NumberFormatException e) {
                    System.out.println("❌ Invalid year format. Keeping current year: " + newYear);
                }
            }

            Book updated = bookService.updateBook(isbn, newTitle, newAuthor, newYear);
            System.out.println("✅ Book updated successfully!");
            System.out.println("   " + updated);
        } catch (BookNotFoundException | InvalidBookDataException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void handleDeleteBook() {
        System.out.println("--- 🗑️ DELETE BOOK ---");
        System.out.print("Enter ISBN of the book to delete: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book existing = bookService.getBookByIsbn(isbn);
            System.out.print("⚠️ Are you sure you want to delete '" + existing.getTitle() + "'? (y/N): ");
            String confirmation = scanner.nextLine().trim();
            if (confirmation.equalsIgnoreCase("y") || confirmation.equalsIgnoreCase("yes")) {
                Book deleted = bookService.deleteBook(isbn);
                System.out.println("✅ Book deleted successfully: " + deleted.getTitle() + " [ISBN: " + deleted.getIsbn() + "]");
            } else {
                System.out.println("ℹ️ Deletion cancelled by user.");
            }
        } catch (BookNotFoundException | InvalidBookDataException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void handleDisplayStatistics() {
        System.out.println("--- 📊 INVENTORY STATISTICS ---");
        List<Book> books = bookService.getAllBooks();
        int total = books.size();
        System.out.println("Total Books in Catalog: " + total);

        if (total > 0) {
            Book oldest = books.stream().min(Comparator.comparingInt(Book::getPublicationYear)).orElse(null);
            Book newest = books.stream().max(Comparator.comparingInt(Book::getPublicationYear)).orElse(null);
            long uniqueAuthors = books.stream().map(Book::getAuthor).distinct().count();

            System.out.println("Distinct Authors:       " + uniqueAuthors);
            if (oldest != null) {
                System.out.println("Oldest Publication:     " + oldest.getTitle() + " (" + oldest.getPublicationYear() + ")");
            }
            if (newest != null) {
                System.out.println("Newest Publication:     " + newest.getTitle() + " (" + newest.getPublicationYear() + ")");
            }
        }
    }

    // --- Helper Utilities ---

    private void displayBookTable(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("ℹ️  No books found in inventory.");
            return;
        }

        System.out.println("+-----+--------------------+--------------------------------+----------------------+------+");
        System.out.printf("| %-3s | %-18s | %-30s | %-20s | %-4s |%n", "#", "ISBN", "Title", "Author", "Year");
        System.out.println("+-----+--------------------+--------------------------------+----------------------+------+");
        int idx = 1;
        for (Book b : books) {
            String title = b.getTitle().length() > 28 ? b.getTitle().substring(0, 25) + "..." : b.getTitle();
            String author = b.getAuthor().length() > 18 ? b.getAuthor().substring(0, 15) + "..." : b.getAuthor();
            System.out.printf("| %-3d | %-18s | %-30s | %-20s | %-4d |%n",
                    idx++, b.getIsbn(), title, author, b.getPublicationYear());
        }
        System.out.println("+-----+--------------------+--------------------------------+----------------------+------+");
        System.out.println("Total displayed: " + books.size() + " book(s).");
    }

    private int readIntegerInput() {
        while (true) {
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.print("⚠️  Invalid number! Please enter a valid integer year: ");
            }
        }
    }
}
