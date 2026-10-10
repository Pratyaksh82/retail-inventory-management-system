import docx
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import parse_xml
from docx.oxml.ns import nsdecls
import os

doc = docx.Document()

# Page setup - Standard 1 inch margins
for section in doc.sections:
    section.top_margin = Inches(1)
    section.bottom_margin = Inches(1)
    section.left_margin = Inches(1)
    section.right_margin = Inches(1)

def set_run_font(run, name='Calibri', size=11, color=None, bold=False, italic=False):
    run.font.name = name
    run.font.size = Pt(size)
    run.bold = bold
    run.italic = italic
    if color:
        run.font.color.rgb = color

def add_title(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(4)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(text)
    set_run_font(r, 'Calibri', 24, RGBColor(26, 54, 93), bold=True)
    return p

def add_subtitle(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(2)
    p.paragraph_format.space_after = Pt(18)
    p.alignment = WD_ALIGN_PARAGRAPH.CENTER
    r = p.add_run(text)
    set_run_font(r, 'Calibri', 13, RGBColor(74, 85, 104), italic=True)
    return p

def add_h1(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(16)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    set_run_font(r, 'Calibri', 16, RGBColor(31, 78, 121), bold=True)
    return p

def add_h2(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(12)
    p.paragraph_format.space_after = Pt(4)
    p.paragraph_format.keep_with_next = True
    r = p.add_run(text)
    set_run_font(r, 'Calibri', 13, RGBColor(43, 108, 176), bold=True)
    return p

def add_p(text):
    p = doc.add_paragraph()
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(6)
    p.paragraph_format.line_spacing = 1.15
    r = p.add_run(text)
    set_run_font(r, 'Calibri', 11, RGBColor(45, 55, 72))
    return p

def add_bullet(bold_prefix, text):
    p = doc.add_paragraph(style='List Bullet')
    p.paragraph_format.space_before = Pt(0)
    p.paragraph_format.space_after = Pt(3)
    p.paragraph_format.line_spacing = 1.15
    r1 = p.add_run(bold_prefix + ' ')
    set_run_font(r1, 'Calibri', 11, RGBColor(26, 32, 44), bold=True)
    r2 = p.add_run(text)
    set_run_font(r2, 'Calibri', 11, RGBColor(45, 55, 72))
    return p

def add_code_block(code_text):
    tbl = doc.add_table(rows=1, cols=1)
    tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
    cell = tbl.cell(0, 0)
    shd = parse_xml(r'<w:shd {} w:fill="F7FAFC"/>'.format(nsdecls('w')))
    cell._tc.get_or_add_tcPr().append(shd)
    borders = parse_xml(r'<w:tcBorders {}><w:top w:val="single" w:sz="4" w:space="0" w:color="CBD5E0"/><w:left w:val="single" w:sz="24" w:space="0" w:color="3182CE"/><w:bottom w:val="single" w:sz="4" w:space="0" w:color="CBD5E0"/><w:right w:val="single" w:sz="4" w:space="0" w:color="CBD5E0"/></w:tcBorders>'.format(nsdecls('w')))
    cell._tc.get_or_add_tcPr().append(borders)
    p = cell.paragraphs[0]
    p.paragraph_format.space_before = Pt(4)
    p.paragraph_format.space_after = Pt(4)
    r = p.add_run(code_text)
    set_run_font(r, 'Consolas', 9.5, RGBColor(45, 55, 72))
    doc.add_paragraph().paragraph_format.space_after = Pt(4)

# Document Header
add_title('Library Book Inventory Management System')
add_subtitle('Week 2 Task: Java Application Development - Core Functionality Implementation\nDetailed Design, Implementation, and Verification Report\nPrepared for Yuva Learn Internship Submission')

# 1. Introduction & Task Objective
add_h1('1. Introduction & Task Objectives')
add_p('The primary objective of Week 2 is to advance from the conceptual architectural design established in Week 1 into the hands-on implementation phase of Java application development. This milestone focuses on building core operational functionalities for a real-world software system by developing a robust, menu-driven command-line interface (CLI) program that manages a library\'s book inventory.')
add_p('A library inventory system demands precision, instant retrieval, reliable duplicate prevention, and transactional data integrity. Even in an in-memory application, books cannot be treated as arbitrary strings; they represent formal business entities governed by strict validation rules (such as international standard book numbers, title completeness, author attribution, and valid publication eras).')
add_p('The core goals achieved in this Week 2 implementation include:')
add_bullet('Full CRUD Implementation:', 'Developing complete Create, Read, Update, and Delete operations for book catalog entities.')
add_bullet('In-Memory Data Structures:', 'Selecting and implementing idiomatic Java Collections Framework data structures that ensure optimal algorithmic efficiency (O(1) lookups) while preserving predictable presentation ordering.')
add_bullet('Robust Validation & Custom Exceptions:', 'Guarding against corrupted, malformed, or blank data inputs via comprehensive domain validation and targeted custom exception hierarchies.')
add_bullet('Interactive Menu-Driven CLI:', 'Delivering an intuitive, resilient command-line interface with safe input parsers and clean tabular output visualization.')
add_bullet('Automated Verification:', 'Validating all business rules, edge cases, and exception paths through an automated unit test suite achieving 100% pass rate.')

# 2. System Architecture & Layered Decomposition
add_h1('2. System Architecture & Layered Decomposition')
add_p('To ensure long-term maintainability, testability, and code quality, the application applies the classic multi-tier layered architectural pattern. Rather than combining user interaction, data storage, and validation into a single script, responsibilities are clearly partitioned across five dedicated architectural layers:')
add_bullet('1. Domain Model Layer (com.inventory.library.model):', 'Defines the Book entity class, encapsulating attributes (ISBN, title, author, publication year) with private field access, defensive getters/setters, formatted string generators, and equals/hashCode implementations.')
add_bullet('2. Repository Layer (com.inventory.library.repository):', 'Defines the persistence abstraction through the BookRepository interface and provides an in-memory implementation (InMemoryBookRepository). This shields upper application layers from direct knowledge of underlying storage mechanisms.')
add_bullet('3. Business Service Layer (com.inventory.library.service):', 'Orchestrates domain validation rules, uniqueness constraints, exception throwing, and high-level query filtering (e.g., search by keyword, statistical summaries).')
add_bullet('4. Presentation / CLI Layer (com.inventory.library.cli):', 'Houses the user-facing console application (LibraryConsoleApp), managing menu display, input parsing, formatted ASCII table rendering, and user feedback.')
add_bullet('5. Domain Exception Layer (com.inventory.library.exception):', 'Supplies meaningful custom exception classes (BookNotFoundException, DuplicateBookException, InvalidBookDataException) to cleanly convey business rule violations.')

# 3. Object-Oriented Programming (OOP) Principles Applied
add_h1('3. Object-Oriented Programming (OOP) Principles Applied')
add_p('The implementation demonstrates core OOP principles that align with modern enterprise Java standards:')
add_bullet('Encapsulation:', 'All entity properties in the Book class are marked private. Access and modifications are strictly channeled through public getters and setters. Mutable state within repository collections is protected using defensive copies (new ArrayList<>(storage.values())) to prevent external modification leaks.')
add_bullet('Abstraction:', 'The BookRepository interface exposes only essential persistence operations (save, findByIsbn, findAll, update, deleteByIsbn, count). Upper service layers depend purely on this abstraction, enabling transparent substitution of the storage layer (e.g., swapping InMemoryBookRepository for a MySQL JDBC or Hibernate repository in future iterations).')
add_bullet('Polymorphism:', 'The BookService constructor accepts any implementation of BookRepository via constructor dependency injection. This allows production code to use InMemoryBookRepository while unit tests or future modules can inject mock or database-backed implementations seamlessly.')
add_bullet('Single Responsibility Principle (SRP):', 'Each class has one well-defined responsibility: Book holds data, InMemoryBookRepository stores data, BookService enforces business rules, and LibraryConsoleApp manages console interactions.')

# 4. Java Collections Framework Strategy & Complexity Analysis
add_h1('4. Java Collections Framework Strategy & Complexity Analysis')
add_p('Choosing the correct data structure within the Java Collections Framework is essential for balancing lookup speed, memory consumption, and iteration behavior. A comparative analysis of potential collection types was conducted:')
add_bullet('ArrayList<Book>:', 'Provides simple sequential indexing and low memory overhead. However, finding a book by ISBN or checking for duplicates requires an O(n) linear scan through all elements. For larger inventories, this becomes a performance bottleneck.')
add_bullet('LinkedList<Book>:', 'Offers O(1) insertion at head/tail, but random access and search by ISBN remains O(n), with higher per-element memory overhead due to bidirectional node pointers.')
add_bullet('HashSet<Book> / HashMap<String, Book>:', 'Provides exceptional O(1) constant-time lookup, insertion, and deletion by hashing the unique ISBN key. However, standard HashMaps do not maintain any predictable iteration order, causing books to appear randomly scrambled when listing inventory.')
add_bullet('LinkedHashMap<String, Book> (Selected Data Structure):', 'Combines the O(1) time complexity of a hash table with the predictable doubly-linked list iteration ordering of an insertion sequence. It ensures that looking up an ISBN, verifying duplicate presence, updating a record, or deleting a book takes O(1) constant time, while listing all books preserves natural insertion order.')

# 5. Domain Model & Validation Architecture
add_h1('5. Domain Model & Validation Architecture')
add_p('The Book entity encapsulates four fundamental attributes: ISBN, Title, Author, and Publication Year. Prior to persisting or updating any record, BookService executes rigorous domain validation:')
add_bullet('ISBN Normalization & Validation:', 'Accepts standard 10-digit or 13-digit ISBNs with or without hyphens and spaces (e.g., "978-0134685991" or "0132350884"). The service normalizes inputs by removing whitespace and hyphens, and validates the clean string against the regex pattern ^[0-9]{9}[0-9X]$|^[0-9]{13}$.')
add_bullet('Title and Author Validation:', 'Ensures that titles and author names are non-null and non-blank after trimming leading/trailing whitespace. Enforces maximum length bounds (250 characters for title, 150 characters for author) to prevent memory bloating and buffer overflows.')
add_bullet('Publication Year Range Check:', 'Restricts publication years to a logical range between 1400 (invention of movable type printing) and the current calendar year plus one (accommodating forthcoming publications). Out-of-bounds years (such as negative numbers or year 3000) are immediately rejected.')
add_bullet('Duplicate ISBN Prevention:', 'Before any addition, the repository is inspected via existsByIsbn(isbn). If a matching key already exists, a DuplicateBookException is raised immediately, preserving catalog integrity.')

# 6. Detailed Walkthrough of CRUD Operations
add_h1('6. Detailed Walkthrough of CRUD Operations')
add_p('The application provides a comprehensive suite of operations for managing inventory lifecycle:')
add_bullet('Create (Add New Book):', 'Prompts the user for ISBN, Title, Author, and Year. Data passes through BookService.validateBook(). If valid and unique, the book is saved to the LinkedHashMap and a confirmation card is displayed.')
add_bullet('Read (List All Books):', 'Retrieves all catalog items and renders them in an aligned ASCII table displaying index, ISBN, title, author, and year. If the catalog is empty, a helpful empty-state message is shown.')
add_bullet('Read (Search by ISBN):', 'Executes an immediate O(1) lookup. Returns the complete book details or displays a descriptive BookNotFoundException message.')
add_bullet('Read (Search by Keyword):', 'Performs case-insensitive substring matching across both book titles and author names using Java Streams, aggregating unique matches without duplicates.')
add_bullet('Update (Modify Details):', 'Allows the librarian to update title, author, and publication year for an existing ISBN. If the user hits enter without typing, the existing value is preserved. Changes are validated before being committed.')
add_bullet('Delete (Remove Book):', 'Locates the book by ISBN, displays the book title, and requires explicit user confirmation (y/N) before permanent removal, preventing accidental loss.')
add_bullet('Inventory Analytics:', 'Calculates total catalog size, unique author count, oldest publication, and newest acquisition dynamically from the in-memory collection.')

# 7. Menu-Driven Command-Line Interface (CLI)
add_h1('7. Menu-Driven Command-Line Interface (CLI)')
add_p('The LibraryConsoleApp class provides an ergonomic, menu-driven console loop. CLI robustness is fortified using line-oriented input reading (scanner.nextLine()) rather than scanner.nextInt(). In standard Java Scanner usage, nextInt() leaves the trailing newline in the buffer, which frequently causes subsequent text prompts to be skipped. Our line-oriented parser converts integers safely with NumberFormatException handling, preventing input skipping.')
add_p('To provide immediate testability without tedious manual data entry, the application boots with a preloaded sample dataset containing recognized computing literature: "Effective Java" by Joshua Bloch (2018), "Head First Java" by Kathy Sierra (2022), "Clean Code" by Robert C. Martin (2008), and "Design Patterns" by Erich Gamma et al. (1994).')

# 8. Automated Testing & Verification Suite
add_h1('8. Automated Testing & Verification Suite')
add_p('An automated test runner (LibraryTestRunner) was engineered to test all critical execution paths without requiring external third-party dependencies. The suite executes 20 dedicated unit test assertions:')
add_bullet('Test 1-2: Create Book Success:', 'Confirms entity persistence, correct ISBN binding, and catalog size incrementation.')
add_bullet('Test 3: Duplicate ISBN Prevention:', 'Verifies that adding an existing ISBN throws DuplicateBookException.')
add_bullet('Test 4-6: Domain Validation:', 'Verifies rejection of blank titles, future years (e.g., 3000), and malformed ISBN strings.')
add_bullet('Test 7-8: ISBN Retrieval & Missing Key:', 'Verifies O(1) retrieval for existing books and asserts BookNotFoundException for unknown ISBNs.')
add_bullet('Test 9-11: Collection Order Preservation:', 'Verifies that retrieving all books maintains exact insertion order across the LinkedHashMap.')
add_bullet('Test 12-13: Substring Search Filtering:', 'Verifies case-insensitive search by title keyword ("java") and author name ("Martin").')
add_bullet('Test 14-16: Update Integrity:', 'Verifies that title, author, and year can be updated and retrieved correctly.')
add_bullet('Test 17-20: Deletion & Boundary Verification:', 'Verifies item removal, size decrementation, and BookNotFoundException when attempting to delete non-existent books.')
add_p('Empirical Test Run Result: All 20 tests executed with 100% pass rate (20 Passed, 0 Failed, Exit Code 0).')

# 9. Source Code Architecture & Scaffolding
add_h1('9. Source Code Architecture & Scaffolding')
add_p('Below are representative code segments demonstrating the implementation:')
add_p('Core Service Validation and Insertion:')
add_code_block('''public Book addBook(Book book) {
    validateBook(book);
    if (repository.existsByIsbn(book.getIsbn())) {
        throw new DuplicateBookException(
            "A book with ISBN '" + book.getIsbn() + "' already exists in inventory.");
    }
    return repository.save(book);
}''')
add_p('In-Memory LinkedHashMap Storage Implementation:')
add_code_block('''public class InMemoryBookRepository implements BookRepository {
    private final Map<String, Book> storage = new LinkedHashMap<>();

    @Override
    public synchronized Book save(Book book) {
        storage.put(normalizeIsbn(book.getIsbn()), book);
        return book;
    }

    @Override
    public synchronized Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(storage.get(normalizeIsbn(isbn)));
    }
}''')

# 10. How to Compile and Run
add_h1('10. How to Compile and Run')
add_p('The project can be compiled and executed using standard JDK commands:')
add_bullet('Compile All Classes:', 'javac -d bin src/main/java/com/inventory/library/**/*.java')
add_bullet('Run Interactive CLI:', 'java -cp bin com.inventory.library.cli.LibraryConsoleApp')
add_bullet('Run Automated Test Suite:', 'java -cp bin com.inventory.library.LibraryTestRunner')

# 11. Conclusion
add_h1('11. Conclusion & Alignment with Evaluation Criteria')
add_p('The Week 2 deliverable satisfies every criterion outlined in the Yuva Learn evaluation matrix:')
add_bullet('Functionality & Completeness:', 'Full CRUD, search, statistics, and exit workflows implemented and verified.')
add_bullet('Adherence to Java Best Practices:', 'Decoupled layered architecture, interfaces, immutable streams, and thread-safe synchronized methods.')
add_bullet('Effective UI & Error Management:', 'Menu-driven CLI with formatted tables, custom domain exceptions, and non-crashing input validation.')
add_bullet('Documentation & Scaffolding:', 'Well-commented Java source code, clean directory structure, comprehensive Word report, and automated test harness.')

docs_dir = r'c:\Users\praty\Desktop\certification\docs'
os.makedirs(docs_dir, exist_ok=True)
out_path = os.path.join(docs_dir, 'Library_Book_Inventory_Week2_Report.docx')
doc.save(out_path)
print('Document saved to:', out_path)
