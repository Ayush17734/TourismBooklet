import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class HomePage extends JFrame {
    private JLabel welcomeLabel;
    private JLabel footerLabel;
    private AnimatedButton homeButton;
    private AnimatedButton addNewButton;
    private JPanel centerPanel;

    public HomePage() {
        setTitle("Tourism Home Page");
        setExtendedState(JFrame.MAXIMIZED_BOTH); // ✅ Full screen
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        BufferedImage backgroundImage = null;
        try {
            backgroundImage = ImageIO.read(new File("src/img.jpg"));
        } catch (IOException e) {
            System.err.println("⚠ Failed to load background image: " + e.getMessage());
        }

        final BufferedImage finalBackgroundImage = backgroundImage;
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (finalBackgroundImage != null) {
                    g.drawImage(finalBackgroundImage, 0, 0, getWidth(), getHeight(), this);
                }

                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setColor(new Color(255, 255, 255, 30));
                int gridSize = 40;
                for (int x = 0; x < getWidth(); x += gridSize) {
                    g2d.drawLine(x, 0, x, getHeight());
                }
                for (int y = 0; y < getHeight(); y += gridSize) {
                    g2d.drawLine(0, y, getWidth(), y);
                }
                g2d.dispose();
            }
        };
        backgroundPanel.setLayout(new BorderLayout());
        setContentPane(backgroundPanel);

        // Header panel for welcome and footer
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setOpaque(false);
        backgroundPanel.add(topPanel, BorderLayout.NORTH);

        welcomeLabel = new JLabel("Hello!!! Welcome to TOURISM BOOKLET", JLabel.CENTER);
        welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        welcomeLabel.setForeground(new Color(0, 0, 0, 0));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        topPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        topPanel.add(welcomeLabel);

        footerLabel = new JLabel("Manage your travel booklet: ADD, VIEW, DELETE places & EDIT IT !!!.", JLabel.CENTER);
        footerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        footerLabel.setForeground(Color.BLACK);
        footerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        footerLabel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        topPanel.add(footerLabel);

        // Left button panel
        centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 80, 0, 0));
        backgroundPanel.add(centerPanel, BorderLayout.WEST);

        homeButton = new AnimatedButton("📍 View Places");
        addNewButton = new AnimatedButton("➕ Add New Place");

        homeButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        addNewButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        centerPanel.add(homeButton);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        centerPanel.add(addNewButton);

        // Button actions
        homeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                Table tableForm = new Table();
                tableForm.setVisible(true);
                HomePage.this.dispose();
            }
        });

        addNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                TourismBooklet tourismBooklet = new TourismBooklet();
                tourismBooklet.setVisible(true);
                HomePage.this.dispose();
            }
        });

        fadeInComponent(welcomeLabel, 15, 200);
        fadeInButton(homeButton);
        fadeInButton(addNewButton);
    }

    private void fadeInComponent(final JComponent component, final int alphaStep, int delay) {
        final Timer timer = new Timer(delay, null);
        timer.addActionListener(new ActionListener() {
            int alpha = 0;

            public void actionPerformed(ActionEvent e) {
                alpha += alphaStep;
                if (alpha > 255) {
                    alpha = 255;
                    timer.stop();
                }
                component.setForeground(new Color(0, 0, 0, alpha));
                component.repaint();
            }
        });
        timer.start();
    }

    private void fadeInButton(final AnimatedButton button) {
        final Timer timer = new Timer(30, null);
        final int[] alpha = new int[]{0};

        timer.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (alpha[0] < 255) {
                    alpha[0] += 10;
                    button.setAlpha(Math.min(alpha[0], 255));
                    button.repaint();
                } else {
                    timer.stop();
                }
            }
        });
        timer.start();
    }

    class AnimatedButton extends JButton {
        private int alpha = 0;
        private Color normalBg = new Color(58, 134, 255);
        private Color hoverBg = new Color(100, 170, 255);
        private boolean hovered = false;

        public AnimatedButton(String text) {
            super(text);
            setFont(new Font("SansSerif", Font.BOLD, 22)); // Bigger text
            setForeground(new Color(255, 255, 255, alpha));
            setContentAreaFilled(false);
            setFocusPainted(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Make the button square and bigger
            setPreferredSize(new Dimension(200, 80)); // Square shape: width = height
            setMaximumSize(new Dimension(200,80));   // Optional: lock square size
            setMinimumSize(new Dimension(200, 80));
        
            setBorder(BorderFactory.createEmptyBorder());
        
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }
        
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }
        

        public void setAlpha(int alpha) {
            this.alpha = alpha;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bgColor = hovered ? hoverBg : normalBg;
            g2.setColor(new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), alpha));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

            Color textColor = hovered ? Color.BLACK : Color.WHITE;
            g2.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha));
            FontMetrics fm = g2.getFontMetrics();
            int stringWidth = fm.stringWidth(getText());
            int stringHeight = fm.getAscent();
            g2.drawString(getText(), (getWidth() - stringWidth) / 2, (getHeight() + stringHeight) / 2 - 4);
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ MySQL JDBC Driver Loaded Successfully!");
        } catch (ClassNotFoundException e) {
            System.out.println("❌ MySQL JDBC Driver Not Found!");
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HomePage().setVisible(true);
            }
        });
    }
}
