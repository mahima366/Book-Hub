//Here is the code for our project. 

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.*;

public class LibraryManagementSystem extends JFrame {
    private List<Book> books;
    private JTable bookTable;
    private DefaultTableModel tableModel;
    private JTextField titleField, authorField, isbnField, yearField, searchField;
    private JComboBox<String> genreComboBox;
    private JCheckBox availabilityCheckBox;
    private Connection connection;

    public LibraryManagementSystem() {
        books = new ArrayList<>();
        setTitle("Library Management System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Connect to the database
        connectToDatabase();
        
        // Create menu bar
        createMenuBar();
        
        // Create toolbar
        createToolBar();
        
        // Create tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Book Details", createBookDetailsPanel());
        tabbedPane.addTab("Book List", createBookListPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Load books from database into the list
        loadBooksFromDatabase();
    }

    private void connectToDatabase() {
        try {
            String url = "jdbc:mysql://localhost:3306/librarydb"; // Your database URL
            String user = "root"; // database username
            String password = "root"; // database password

            connection = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
        } catch (SQLException e) {
            showErrorDialog("Database connection error: " + e.getMessage());
        }
    }

    private void loadBooksFromDatabase() {
        String sql = "SELECT * FROM Books";
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            
            while (resultSet.next()) {
                Book book = new Book(
                    resultSet.getString("title"),
                    resultSet.getString("author"),
                    resultSet.getString("isbn"),
                    resultSet.getInt("year"),
                    resultSet.getString("genre"),
                    resultSet.getBoolean("available")
                );
                books.add(book);
            }
            updateBookTable();
        } catch (SQLException e) {
            showErrorDialog("Error loading books from database: " + e.getMessage());
        }
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);
        
        JMenu editMenu = new JMenu("Edit");
        JMenuItem clearItem = new JMenuItem("Clear Fields");
        clearItem.addActionListener(e -> clearFields());
        editMenu.add(clearItem);
        
        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    private void createToolBar() {
        JToolBar toolBar = new JToolBar();
        
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> addBook());
        
        JButton removeButton = new JButton("Remove Book");
        removeButton.addActionListener(e -> removeBook());
        
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks());
        
        toolBar.add(addButton);
        toolBar.add(removeButton);
        toolBar.add(searchButton);
        
        add(toolBar, BorderLayout.NORTH);
    }
    
    private JPanel createBookDetailsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Create form fields
        titleField = new JTextField(20);
        authorField = new JTextField(20);
        isbnField = new JTextField(20);
        yearField = new JTextField(20);
        genreComboBox = new JComboBox<>(new String[]{"Fiction", "Non-Fiction", "Science", "History", "Technology"});
        availabilityCheckBox = new JCheckBox("Available");
        
        // Add components to panel
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(new JLabel("Title:"), gbc);
        gbc.gridx = 1;
        panel.add(titleField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(new JLabel("Author:"), gbc);
        gbc.gridx = 1;
        panel.add(authorField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("ISBN:"), gbc);
        gbc.gridx = 1;
        panel.add(isbnField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(new JLabel("Year:"), gbc);
        gbc.gridx = 1;
        panel.add(yearField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1;
        panel.add(genreComboBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(availabilityCheckBox, gbc);
        
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Book");
        addButton.addActionListener(e -> addBook());
        JButton updateButton = new JButton("Update Book");
        updateButton.addActionListener(e -> updateBook());
        
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        
        gbc.gridx = 0; gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(buttonPanel, gbc);
        
        return panel;
    }
    
    private JPanel createBookListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Create search panel
        JPanel searchPanel = new JPanel();
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchBooks());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        // Create table
        String[] columnNames = {"Title", "Author", "ISBN", "Year", "Genre", "Available"};
        tableModel = new DefaultTableModel(columnNames, 0);
        bookTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(bookTable);
        
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void addBook() {
        try {
            Book book = new Book(
                titleField.getText(),
                authorField.getText(),
                isbnField.getText(),
                Integer.parseInt(yearField.getText()),
                (String) genreComboBox.getSelectedItem(),
                availabilityCheckBox.isSelected()
            );

            // Insert book into the database
            insertBookIntoDatabase(book);

            books.add(book);
            updateBookTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Book added successfully!");
        } catch (NumberFormatException ex) {
            showErrorDialog("Please enter a valid year.");
        } catch (SQLException ex) {
            showErrorDialog("Error adding book to the database: " + ex.getMessage());
        } catch (Exception ex) {
            showErrorDialog("Error adding book: " + ex.getMessage());
        }
    }

    private void insertBookIntoDatabase(Book book) throws SQLException {
        String sql = "INSERT INTO Books (title, author, isbn, year, genre, available) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setString(3, book.getIsbn());
            statement.setInt(4, book.getYear());
            statement.setString(5, book.getGenre());
            statement.setBoolean(6, book.isAvailable());

            statement.executeUpdate();
        }
    }
    
    private void updateBook() {
        String isbnToUpdate = isbnField.getText();
        if (isbnToUpdate.isEmpty()) {
            showErrorDialog("Please enter the ISBN of the book to update.");
            return;
        }

        try {
            Book bookToUpdate = null;
            for (Book book : books) {
                if (book.getIsbn().equals(isbnToUpdate)) {
                    bookToUpdate = book;
                    break;
                }
            }

            if (bookToUpdate == null) {
                showErrorDialog("No book found with the specified ISBN.");
                return;
            }

            bookToUpdate.setTitle(titleField.getText());
            bookToUpdate.setAuthor(authorField.getText());
            bookToUpdate.setYear(Integer.parseInt(yearField.getText()));
            bookToUpdate.setGenre((String) genreComboBox.getSelectedItem());
            bookToUpdate.setAvailable(availabilityCheckBox.isSelected());

            // Update book in the database
            updateBookInDatabase(bookToUpdate);

            updateBookTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Book updated successfully!");
        } catch (NumberFormatException ex) {
            showErrorDialog("Please enter a valid year.");
        } catch (SQLException ex) {
            showErrorDialog("Error updating book in the database: " + ex.getMessage());
        }
    }

    private void updateBookInDatabase(Book book) throws SQLException {
        String sql = "UPDATE Books SET title = ?, author = ?, year = ?, genre = ?, available = ? WHERE isbn = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getTitle());
            statement.setString(2, book.getAuthor());
            statement.setInt(3, book.getYear());
            statement.setString(4, book.getGenre());
            statement.setBoolean(5, book.isAvailable());
            statement.setString(6, book.getIsbn());

            statement.executeUpdate();
        }
    }

    private void removeBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            showErrorDialog("Please select a book to remove.");
            return;
        }

        try {
            Book book = books.get(selectedRow);
            removeBookFromDatabase(book);

            books.remove(selectedRow);
            updateBookTable();
            clearFields();
            JOptionPane.showMessageDialog(this, "Book removed successfully!");
        } catch (SQLException ex) {
            showErrorDialog("Error removing book from database: " + ex.getMessage());
        }
    }

    private void removeBookFromDatabase(Book book) throws SQLException {
        String sql = "DELETE FROM Books WHERE isbn = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, book.getIsbn());
            statement.executeUpdate();
        }
    }

    private void searchBooks() {
        String query = searchField.getText().toLowerCase();
        List<Book> filteredBooks = new ArrayList<>();

        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(query) || 
                book.getAuthor().toLowerCase().contains(query) ||
                book.getIsbn().toLowerCase().contains(query)) {
                filteredBooks.add(book);
            }
        }
        
        updateBookTable(filteredBooks);
    }

    private void updateBookTable() {
        updateBookTable(books);
    }

    private void updateBookTable(List<Book> bookList) {
        tableModel.setRowCount(0);
        for (Book book : bookList) {
            tableModel.addRow(new Object[]{
                book.getTitle(),
                book.getAuthor(),
                book.getIsbn(),
                book.getYear(),
                book.getGenre(),
                book.isAvailable()
            });
        }
    }

    private void clearFields() {
        titleField.setText("");
        authorField.setText("");
        isbnField.setText("");
        yearField.setText("");
        genreComboBox.setSelectedIndex(0);
        availabilityCheckBox.setSelected(false);
    }

    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this, "Library Management System\nVersion 1.0");
    }

    private void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LibraryManagementSystem lms = new LibraryManagementSystem();
            lms.setVisible(true);
        });
    }
}

// Book class definition
class Book {
    private String title;
    private String author;
    private String isbn;
    private int year;
    private String genre;
    private boolean available;

    public Book(String title, String author, String isbn, int year, String genre, boolean available) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.year = year;
        this.genre = genre;
        this.available = available;
    }

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
