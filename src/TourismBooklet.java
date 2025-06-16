import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class TourismBooklet extends JFrame {
    private JTextArea textArea;
    private JTextField placeField, cityField;
    private JTextArea descriptionArea;

    public TourismBooklet() {
        // Set title and make the window full screen
        setTitle("Tourism Booklet");
        setExtendedState(JFrame.MAXIMIZED_BOTH);  // Maximizes the window
        setUndecorated(true); // Removes window borders and title bar
        setLocationRelativeTo(null); // Center the window on the screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Display Area
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setBackground(new Color(224, 255, 255));
        textArea.setFont(new Font("Serif", Font.PLAIN, 14));
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Input Fields Panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputPanel.add(new JLabel("Place Name:"));
        placeField = createTextField();
        inputPanel.add(placeField);

        inputPanel.add(new JLabel("City:"));
        cityField = createTextField();
        inputPanel.add(cityField);

        inputPanel.add(new JLabel("Description:"));
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setBackground(new Color(255, 239, 213));
        inputPanel.add(new JScrollPane(descriptionArea));

        add(inputPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        JButton loadButton = createButton("Load Places");
        JButton addButton = createButton("Add Place");
        JButton backButton = createButton("Back to Home");

        buttonPanel.add(loadButton);
        buttonPanel.add(addButton);
        buttonPanel.add(backButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Button Actions
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadLatestPlace(); // Only show latest added place
            }
        });

        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addPlace();
            }
        });

        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new HomePage().setVisible(true);
                dispose();
            }
        });
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(new Color(255, 239, 213));
        return textField;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(30, 144, 255));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setPreferredSize(new Dimension(150, 30));
        return button;
    }

    private void loadLatestPlace() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM place ORDER BY id DESC LIMIT 1")) {

            textArea.setText("📍 **Latest Tourism Place** 📍\n\n");

            if (rs.next()) {
                textArea.append("🔹 Place: " + rs.getString("place_name") + "\n");
                textArea.append("🏙️  City: " + rs.getString("city") + "\n");
                textArea.append("📝 Description: " + rs.getString("description") + "\n");
                textArea.append("————————————————————————\n");
            } else {
                textArea.setText("ℹ️ No places found.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error loading latest place!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPlace() {
        String place = placeField.getText().trim();
        String city = cityField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (place.isEmpty() || city.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "⚠ All fields are required!", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String query = "INSERT INTO place (place_name, city, description) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, place);
            pstmt.setString(2, city);
            pstmt.setString(3, description);
            pstmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "🎉 New place added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

            // Clear fields
            placeField.setText("");
            cityField.setText("");
            descriptionArea.setText("");

            // Clear display area
            textArea.setText("");

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "❌ Error adding place!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TourismBooklet().setVisible(true));
    }
}
