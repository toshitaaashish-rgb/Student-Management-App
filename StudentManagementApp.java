import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StudentManagementApp extends JFrame {
    // UI Components
    private JTextField txtId, txtName, txtAge, txtCourse, txtEmail;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnAdd, btnUpdate, btnDelete, btnSearch, btnView, btnClear;

    // --- MySQL Database Configuration ---
    // Update DB_URL, DB_USER, and DB_PASSWORD to match your local MySQL server settings
    private static final String DB_URL = "jdbc:mysql://localhost:3306/student_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";       // Replace with your MySQL username
    private static final String DB_PASSWORD = "pass1234"; // Replace with your MySQL password

    public StudentManagementApp() {
        setTitle("Student Records Management System (MySQL)");
        setSize(950, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main Layout Setup
        setLayout(new BorderLayout(10, 10));

        // --- Form Panel (Left) ---
        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 15));
        formPanel.setBorder(BorderFactory.createTitledBorder(" Student Details "));
        formPanel.setPreferredSize(new Dimension(320, 0));

        formPanel.add(new JLabel(" Student ID:"));
        txtId = new JTextField();
        txtId.setEditable(false); // Auto-incremented ID
        formPanel.add(txtId);

        formPanel.add(new JLabel(" Full Name:"));
        txtName = new JTextField();
        formPanel.add(txtName);

        formPanel.add(new JLabel(" Age:"));
        txtAge = new JTextField();
        formPanel.add(txtAge);

        formPanel.add(new JLabel(" Course:"));
        txtCourse = new JTextField();
        formPanel.add(txtCourse);

        formPanel.add(new JLabel(" Email:"));
        txtEmail = new JTextField();
        formPanel.add(txtEmail);

        add(formPanel, BorderLayout.WEST);

        // --- Table Panel (Center) ---
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Age", "Course", "Email"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder(" Student Records Database "));
        add(scrollPane, BorderLayout.CENTER);

        // Table Row Selection Listener (Populates form fields for editing)
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() != -1) {
                int row = table.getSelectedRow();
                txtId.setText(tableModel.getValueAt(row, 0).toString());
                txtName.setText(tableModel.getValueAt(row, 1).toString());
                txtAge.setText(tableModel.getValueAt(row, 2).toString());
                txtCourse.setText(tableModel.getValueAt(row, 3).toString());
                txtEmail.setText(tableModel.getValueAt(row, 4).toString());
            }
        });

        // --- Button Panel (Bottom) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 12));
        
        btnAdd = new JButton("Add Record");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnSearch = new JButton("Search");
        btnView = new JButton("View All");
        btnClear = new JButton("Clear");

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnSearch);
        buttonPanel.add(btnView);
        buttonPanel.add(btnClear);

        add(buttonPanel, BorderLayout.SOUTH);

        // --- Action Listeners ---
        btnAdd.addActionListener(e -> addStudent());
        btnUpdate.addActionListener(e -> updateStudent());
        btnDelete.addActionListener(e -> deleteStudent());
        btnSearch.addActionListener(e -> searchStudent());
        btnView.addActionListener(e -> loadStudentData());
        btnClear.addActionListener(e -> clearFields());

        // Load initial records into JTable
        loadStudentData();
    }

    // Helper Method to Get DB Connection
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Load / Refresh Table Data
    private void loadStudentData() {
        tableModel.setRowCount(0);
        String sql = "SELECT * FROM students";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getInt("age"),
                        rs.getString("course"),
                        rs.getString("email")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading records from MySQL: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add Student
    private void addStudent() {
        String name = txtName.getText().trim();
        String ageStr = txtAge.getText().trim();
        String course = txtCourse.getText().trim();
        String email = txtEmail.getText().trim();

        if (name.isEmpty() || ageStr.isEmpty() || course.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all student details.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            String sql = "INSERT INTO students (name, age, course, email) VALUES (?, ?, ?, ?)";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, course);
                pstmt.setString(4, email);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Student record added successfully!");
                loadStudentData();
                clearFields();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a valid integer.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Update Student
    private void updateStudent() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the table to update.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            String name = txtName.getText().trim();
            int age = Integer.parseInt(txtAge.getText().trim());
            String course = txtCourse.getText().trim();
            String email = txtEmail.getText().trim();

            String sql = "UPDATE students SET name = ?, age = ?, course = ?, email = ? WHERE id = ?";
            try (Connection conn = getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setInt(2, age);
                pstmt.setString(3, course);
                pstmt.setString(4, email);
                pstmt.setInt(5, id);
                
                int rows = pstmt.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Student record updated successfully!");
                    loadStudentData();
                    clearFields();
                } else {
                    JOptionPane.showMessageDialog(this, "Record not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please ensure Age is a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "MySQL Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Delete Student
    private void deleteStudent() {
        String idStr = txtId.getText().trim();
        if (idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a student record from the table to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this record?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                int id = Integer.parseInt(idStr);
                String sql = "DELETE FROM students WHERE id = ?";
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setInt(1, id);
                    int rows = pstmt.executeUpdate();
                    if (rows > 0) {
                        JOptionPane.showMessageDialog(this, "Student record deleted successfully!");
                        loadStudentData();
                        clearFields();
                    }
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "MySQL Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Search Student
    private void searchStudent() {
        String query = JOptionPane.showInputDialog(this, "Enter Student Name or ID to search:");
        if (query == null || query.trim().isEmpty()) return;

        tableModel.setRowCount(0);
        String sql = "SELECT * FROM students WHERE id LIKE ? OR name LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + query.trim() + "%");
            pstmt.setString(2, "%" + query.trim() + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    tableModel.addRow(new Object[]{
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getString("course"),
                            rs.getString("email")
                    });
                }
                if (!found) {
                    JOptionPane.showMessageDialog(this, "No matching records found.");
                    loadStudentData();
                }
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Search Error: " + e.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Clear Form Fields
    private void clearFields() {
        txtId.setText("");
        txtName.setText("");
        txtAge.setText("");
        txtCourse.setText("");
        txtEmail.setText("");
        table.clearSelection();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            new StudentManagementApp().setVisible(true);
        });
    }
}