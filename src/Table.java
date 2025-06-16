import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Table extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public Table() {
        setTitle("Tourism Places Table");
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Full screen
        setUndecorated(true); // No borders
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel mainHeader = new JLabel("Here's what you can see", JLabel.CENTER);
        mainHeader.setFont(new Font("Serif", Font.BOLD, 18));
        mainHeader.setForeground(new Color(0, 102, 204));
        add(mainHeader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new String[]{"ID", "Place Name", "City", "Description"}, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(0, 102, 204));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Serif", Font.BOLD, 16));
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 40));
        ((DefaultTableCellRenderer) header.getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton deleteButton = new JButton("Delete");
        JButton editButton = new JButton("Edit Description");
        JButton backButton = new JButton("Back to Home");

        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadTableData();

        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteSelectedPlace();
            }
        });

        editButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                editDescription();
            }
        });

        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new HomePage().setVisible(true);
                dispose();
            }
        });
    }

    private void loadTableData() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM place ORDER BY id")) {

            tableModel.setRowCount(0);
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("place_name"),
                        rs.getString("city"),
                        rs.getString("description")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedPlace() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int placeId = (int) tableModel.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this place?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                deletePlace(placeId);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deletePlace(int placeId) {
        String deleteQuery = "DELETE FROM place WHERE id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteQuery)) {

            pstmt.setInt(1, placeId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                reassignIds(conn);
                showRedPopup("✅ Place deleted successfully!");
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "⚠ Place not found!", "Warning", JOptionPane.WARNING_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error deleting place: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reassignIds(Connection conn) throws SQLException {
        String selectQuery = "SELECT id FROM place ORDER BY id";
        String updateQuery = "UPDATE place SET id = ? WHERE id = ?";
        String resetAutoIncrement = "ALTER TABLE place AUTO_INCREMENT = 1";

        conn.setAutoCommit(false);

        try (
                Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery(selectQuery)
        ) {
            int newId = 1;
            while (rs.next()) {
                int oldId = rs.getInt("id");
                if (oldId != newId) {
                    try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                        pstmt.setInt(1, -newId); // Temporarily set to avoid conflict
                        pstmt.setInt(2, oldId);
                        pstmt.executeUpdate();
                    }
                }
                newId++;
            }

            // Convert negative IDs to correct positive ones
            try (Statement fixStmt = conn.createStatement();
                 ResultSet fixRs = fixStmt.executeQuery("SELECT id FROM place WHERE id < 0 ORDER BY id")) {
                newId = 1;
                while (fixRs.next()) {
                    int tempId = fixRs.getInt("id");
                    try (PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
                        pstmt.setInt(1, newId++);
                        pstmt.setInt(2, tempId);
                        pstmt.executeUpdate();
                    }
                }
            }

            try (Statement alterStmt = conn.createStatement()) {
                alterStmt.execute(resetAutoIncrement);
            }

            conn.commit();

        } catch (SQLException ex) {
            conn.rollback();
            throw ex;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private void editDescription() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            int placeId = (int) tableModel.getValueAt(selectedRow, 0);
            String currentDescription = (String) tableModel.getValueAt(selectedRow, 3);

            String newDescription = JOptionPane.showInputDialog(this, "Edit Description:", currentDescription);

            if (newDescription != null && !newDescription.trim().isEmpty()) {
                updateDescription(placeId, newDescription.trim());
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to edit.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateDescription(int id, String newDescription) {
        String updateQuery = "UPDATE place SET description=? WHERE id=?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {

            pstmt.setString(1, newDescription);
            pstmt.setInt(2, id);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "✅ Description updated successfully!");
                loadTableData();
            } else {
                JOptionPane.showMessageDialog(this, "⚠ Update failed!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error updating: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showRedPopup(String message) {
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setOpaque(true);
        label.setBackground(Color.RED);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 14));

        JOptionPane.showMessageDialog(this, label, "Deleted", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Table().setVisible(true);
            }
        });
    }
}
