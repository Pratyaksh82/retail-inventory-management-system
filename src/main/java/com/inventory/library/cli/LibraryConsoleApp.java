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
 * Implements full CRUD functionality, input validation, robust exception handling,
 * and clear user feedback.
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
     * Preloads initial sample books to demonstrate functionality immediately.
     */
    public void preloadSampleData() {
        try {
            bookService.addBook(new Book("978-0134685991", "Effective Java", "Joshua Bloch", 2018));
            bookService.addBook(new Book("978-0596009205", "Head First Java", "Kathy Sierra", 2022));
            bookService.addBook(new Book("978-0132350884", "Clean Code", "Robert C. Martin", 2008));
            bookService.addBook(new Book("978-0201633610", "Design Patterns (GoF)", "Erich Gamma et al.", 1994));
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
            System.out.print("👉 Enter your choice [1-8]: ");
            String input = scanner.nextLine().trim();

            System.out.println();
            switch (input) {
                case "1" -> handleAddBook();
                case "2" -> handleListBooks();
                case "3" -> handleSearchByIsbn();
                case "4" -> handleSearchByKeyword();
                case "5" -> handleUpdateBook();
                case "6" -> handleDeleteBook();
                case "7" -> handleDisplayStatistics();
                case "8" -> {
                    running = false;
                    System.out.println("======================================================");
                    System.out.println("  Thank you for using Library Book Inventory System!  ");
                    System.out.println("======================================================");
                }
                default -> System.out.println("⚠️  Invalid choice! Please select an option between 1 and 8.");
            }
            System.out.println();
        }
    }

    private void printHeader() {
        System.out.println("======================================================");
        System.out.println("      📚 LIBRARY BOOK INVENTORY MANAGEMENT SYSTEM      ");
        System.out.println("      Core Functionality - Java Application (Week 2)   ");
        System.out.println("======================================================");
    }

    private void printMenu() {
        System.out.println("------------------------------------------------------");
        System.out.println(" MAIN MENU");
        System.out.println("------------------------------------------------------");
        System.out.println(" 1. ➕ Add New Book (Create)");
        System.out.println(" 2. 📋 View All Books (Read / List)");
        System.out.println(" 3. 🔍 Search Book by ISBN");
        System.out.println(" 4. 🔎 Search Books by Title or Author");
        System.out.println(" 5. ✏️  Update Book Information (Update)");
        System.out.println(" 6. 🗑️  Delete Book (Delete)");
        System.out.println(" 7. 📊 View Inventory Statistics");
        System.out.println(" 8. 🚪 Exit Application");
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

            Book newBook = new Book(isbn, title, author, year);
            bookService.addBook(newBook);

            System.out.println("✅ Book added successfully!");
            System.out.println("   " + newBook);
        } catch (DuplicateBookException | InvalidBookDataException e) {
            System.out.println("❌ Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ Unexpected error occurred while adding book: " + e.getMessage());
        }
    }

    private void handleListBooks() {
        System.out.println("--- 📋 ALL BOOKS IN INVENTORY ---");
        List<Book> books = bookService.getAllBooks();
        displayBookTable(books);
    }

    private void handleSearchByIsbn() {
        System.out.println("--- 🔍 SEARCH BOOK BY ISBN ---");
        System.out.print("Enter ISBN to search: ");
        String isbn = scanner.nextLine().trim();

        try {
            Book book = bookService.getBookByIsbn(isbn);
            System.out.println("✅ Book found:");
            System.out.println("   " + book);
        } catch (BookNotFoundException | InvalidBookDataException e) {
            System.out.println("❌ " + e.getMessage());
        }
    }

    private void handleSearchByKeyword() {
        System.out.println("--- 🔎 SEARCH BOOKS BY TITLE / AUTHOR ---");
        System.out.print("Enter search keyword: ");
        String keyword = scanner.nextLine().trim();

        List<Book> matches = bookService.searchBooks(keyword);
        if (matches.isEmpty()) {
            System.out.println("ℹ️  No books found matching keyword '" + keyword + "'.");
        } else {
            System.out.println("✅ Found " + matches.size() + " matching book(s):");
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
