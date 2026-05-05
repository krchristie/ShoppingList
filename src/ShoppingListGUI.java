import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/**
 * ShoppingListGUI.java
 * * The View component of the application.
 * This class handles the rendering of the shopping list, the creation of
 * custom-styled dialogs for adding/editing items, and user input validation.
 * It utilizes custom Swing components for a modern "Electric Purple" aesthetic.
 * * @author [Your Name]
 * 
 * @version 1.0
 */
public class ShoppingListGUI extends JFrame {
    private ListManager manager;
    private JPanel listContainer;
    private final String FILE_NAME = "shopping_list.csv";

    // Custom Color Palette
    private final Color LAVENDER = new Color(230, 230, 250);
    private final Color ELECTRIC_PURPLE = new Color(150, 0, 255);

    /** Standard units of measurement for the dropdown menu. */
    private final String[] UNITS = { "select unit", "lbs", "oz", "gal", "qts", "pts", "kg", "g" };

    /**
     * Constructs the GUI and initializes the data connection.
     * * @param manager The ListManager instance providing the business logic.
     */
    public ShoppingListGUI(ListManager manager) {
        this.manager = manager;

        // Load existing data from file before rendering
        this.manager.loadFromFile(FILE_NAME);

        setupUI();
        refreshDisplay();
    }

    /**
     * Initializes the main window frame, sets the content pane to a gradient,
     * and constructs the header, scrollable list, and footer.
     */
    private void setupUI() {
        setTitle("Karen's Shopping List");
        setSize(400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        JLabel titleLabel = new JLabel("Karen's Shopping List", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        root.add(titleLabel, BorderLayout.NORTH);

        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        JScrollPane sp = new JScrollPane(listContainer);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        setupScrollbar(sp);

        RoundedButton addBtn = new RoundedButton("Add Item", 12, LAVENDER);
        addBtn.setForeground(ELECTRIC_PURPLE);
        addBtn.setFont(new Font("Arial", Font.BOLD, 14));
        addBtn.setPreferredSize(new Dimension(120, 35));
        addBtn.addActionListener(e -> showItemDialog(null));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setOpaque(false);
        bottomPanel.add(addBtn);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));

        root.add(sp, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Displays a modal dialog for adding a new item or editing an existing one.
     * Implements validation for empty names and invalid numeric formats.
     * * @param itemToEdit The item to modify, or null if creating a new item.
     */
    private void showItemDialog(ShoppingItem itemToEdit) {
        boolean isEdit = (itemToEdit != null);
        String dialogTitle = isEdit ? "Edit Item" : "Add New Item";
        String buttonText = isEdit ? "Update Item" : "Save Item";

        JDialog dialog = new JDialog(this, dialogTitle, true);
        dialog.setSize(320, 420);
        dialog.setLocationRelativeTo(this);

        GradientPanel dialogPanel = new GradientPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));

        JLabel headerLabel = new JLabel(dialogTitle, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JComboBox<String> nameBox = new JComboBox<>(manager.getFilteredSuggestions(itemToEdit));
        nameBox.setEditable(true);
        styleComboBox(nameBox);

        JTextField amountField = new JTextField();
        JComboBox<String> unitBox = new JComboBox<>(UNITS);
        styleComboBox(unitBox);

        // Pre-populate fields if in Edit mode
        if (isEdit) {
            nameBox.setSelectedItem(itemToEdit.getItemName());
            amountField.setText(itemToEdit.getAmount() != null ? itemToEdit.getAmount().toString() : "");
            if (itemToEdit.getUnit() == null || itemToEdit.getUnit().isEmpty()) {
                unitBox.setSelectedIndex(0);
            } else {
                unitBox.setSelectedItem(itemToEdit.getUnit());
            }
        } else {
            nameBox.setSelectedItem("");
            unitBox.setSelectedIndex(0);
        }

        RoundedButton actionBtn = new RoundedButton(buttonText, 12, LAVENDER);
        actionBtn.setForeground(ELECTRIC_PURPLE);
        actionBtn.setFont(new Font("Arial", Font.BOLD, 14));
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        actionBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        actionBtn.addActionListener(e -> {
            try {
                Object input = nameBox.getEditor().getItem();
                String name = (input == null) ? "" : input.toString().trim().toLowerCase();

                // VALIDATION: Ensure item name is not blank
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Item name cannot be empty.", "Input Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // VALIDATION: Attempt to parse the amount; handles NumberFormatException
                Double amt = amountField.getText().isEmpty() ? null : Double.parseDouble(amountField.getText());

                String unit = (String) unitBox.getSelectedItem();
                if ("select unit".equals(unit)) {
                    unit = "";
                }

                if (isEdit)
                    manager.updateItem(itemToEdit, name, amt, unit);
                else
                    manager.addItem(name, amt, unit);

                refreshDisplay();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number (e.g., 1.5) or leave blank.",
                        "Numeric Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "An unexpected error occurred: " + ex.getMessage(),
                        "System Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        dialogPanel.add(headerLabel);
        dialogPanel.add(createFieldPanel("item name:", nameBox));
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(createFieldPanel("amount (number):", amountField));
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(createFieldPanel("unit:", unitBox));
        dialogPanel.add(Box.createVerticalStrut(20));
        dialogPanel.add(actionBtn);

        dialog.add(dialogPanel);
        dialog.setVisible(true);
    }

    /**
     * Clears the current list container and repopulates it with
     * items from the ListManager. Automatically saves data to CSV.
     */
    public void refreshDisplay() {
        listContainer.removeAll();
        for (ShoppingItem si : manager.getActiveList()) {
            addItemRow(si);
        }
        listContainer.revalidate();
        listContainer.repaint();
        manager.saveToFile(FILE_NAME);
    }

    /**
     * Creates and adds a single UI row representing a shopping item.
     * Includes a checkbox for removal and an edit button.
     * * @param si The ShoppingItem to be rendered in this row.
     */
    private void addItemRow(ShoppingItem si) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(400, 50));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 40)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JCheckBox check = new JCheckBox();
        check.setOpaque(false);
        check.addActionListener(e -> {
            if (check.isSelected()) {
                manager.removeItem(si);
                refreshDisplay();
            }
        });

        JLabel label = new JLabel(si.toString());
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 16));

        RoundedButton editBtn = new RoundedButton("Edit", 12, LAVENDER);
        editBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        editBtn.setPreferredSize(new Dimension(62, 25));
        editBtn.setForeground(ELECTRIC_PURPLE);
        editBtn.addActionListener(e -> showItemDialog(si));

        row.add(check, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        row.add(editBtn, BorderLayout.EAST);
        listContainer.add(row);
    }

    /**
     * Utility method to create a labeled input section for the entry dialog.
     * * @param text The label text.
     * 
     * @param input The input component (JTextField or JComboBox).
     * @return A JPanel containing the labeled input.
     */
    private JPanel createFieldPanel(String text, JComponent input) {
        JPanel p = new JPanel(new BorderLayout(0, 5));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Arial", Font.BOLD, 12));
        p.add(l, BorderLayout.NORTH);
        p.add(input, BorderLayout.CENTER);
        return p;
    }

    /**
     * Applies custom colors and UI delegates to JComboBoxes to match theme.
     * * @param combo The JComboBox to style.
     */
    private void styleComboBox(JComboBox<String> combo) {
        combo.setBackground(LAVENDER);
        combo.setForeground(ELECTRIC_PURPLE);
        combo.setUI(new BasicComboBoxUI() {
            @Override
            protected JButton createArrowButton() {
                JButton b = super.createArrowButton();
                b.setBackground(LAVENDER);
                return b;
            }
        });
    }

    /**
     * Customizes the JScrollPane scrollbar colors and removes arrow buttons.
     * * @param sp The JScrollPane to customize.
     */
    private void setupScrollbar(JScrollPane sp) {
        sp.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = ELECTRIC_PURPLE;
                this.trackColor = LAVENDER;
            }

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton b = new JButton();
                b.setPreferredSize(new Dimension(0, 0));
                return b;
            }
        });
    }
}

/**
 * A custom JPanel that renders a vertical sky-blue to deep-purple gradient.
 */
class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        // Sky Blue to Deep Purple gradient
        GradientPaint gp = new GradientPaint(0, 0, new Color(135, 206, 250), 0, getHeight(), new Color(120, 81, 169));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}

/**
 * A custom JButton that renders with rounded corners and anti-aliased edges.
 */
class RoundedButton extends JButton {
    private Color bgColor;

    /**
     * Creates a rounded button.
     * 
     * @param label   Button text.
     * @param radius  Corner radius.
     * @param bgColor Background color.
     */
    public RoundedButton(String label, int radius, Color bgColor) {
        super(label);
        this.bgColor = bgColor;
        setContentAreaFilled(false);
        setBorderPainted(false);
        setFocusPainted(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(bgColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
        g2d.dispose();
        super.paintComponent(g);
    }
}