import javax.swing.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import java.awt.*;

/**
 * ShoppingListGUI.java
 *
 * This class serves as the View component in the MVC architecture.
 * It manages the graphical user interface, including the dynamic switching
 * between the active shopping list and the master item history.
 * 
 * @author Karen R. Christie
 * @since April 2026
 */
public class ShoppingListGUI extends JFrame {
    private ListManager manager;
    private JPanel listContainer;
    private JLabel titleLabel;
    private JPanel bottomPanel;

    // View state toggle: true if editing Master List, false for Shopping List
    private boolean isMasterView = false;

    private final String ACTIVE_FILE_NAME = "shopping_list.csv";
    private final String MASTER_FILE_NAME = "master_list.csv";

    // Custom Color Palette
    private final Color LAVENDER = new Color(230, 230, 250);
    private final Color ELECTRIC_PURPLE = new Color(150, 0, 255);

    /** Standard units of measurement for grocery items. */
    private final String[] UNITS = { "select unit", "lbs", "oz", "gal", "qts", "pts", "kg", "g" };

    /**
     * Initialize the GUI and load persistent data.
     *
     * @param manager The ListManager instance providing the application logic.
     */
    public ShoppingListGUI(ListManager manager) {
        this.manager = manager;

        // Load existing data from CSV files upon startup
        this.manager.loadActiveList(ACTIVE_FILE_NAME);
        this.manager.loadMasterList(MASTER_FILE_NAME);

        setupUI();
        refreshDisplay();
    }

    /**
     * Build the primary application window.
     * * Configures the main layout using a GradientPanel and prepares the
     * scrollable list container and the dynamic bottom navigation panel.
     */
    private void setupUI() {
        setTitle("Karen's Shopping List");
        setSize(400, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GradientPanel root = new GradientPanel();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        // Header section
        titleLabel = new JLabel("Karen's Shopping List", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 22));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        root.add(titleLabel, BorderLayout.NORTH);

        // Main scrollable list container
        listContainer = new JPanel();
        listContainer.setLayout(new BoxLayout(listContainer, BoxLayout.Y_AXIS));
        listContainer.setOpaque(false);

        JScrollPane sp = new JScrollPane(listContainer);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        setupScrollbar(sp);

        // Footer section containing context-sensitive buttons
        bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        updateBottomPanel();

        root.add(sp, BorderLayout.CENTER);
        root.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Synchronize the footer buttons with the current view state.
     * * Swaps the "Add Item" button logic and visibility based on whether
     * the user is viewing the Active List or the Master List.
     */
    private void updateBottomPanel() {
        bottomPanel.removeAll();

        if (isMasterView) {
            titleLabel.setText("Edit Master List");

            RoundedButton backBtn = new RoundedButton("Back to List", 12, LAVENDER);
            styleNavButton(backBtn);
            backBtn.addActionListener(e -> {
                isMasterView = false;
                updateBottomPanel();
                refreshDisplay();
            });

            RoundedButton addBtn = new RoundedButton("Add Item", 12, LAVENDER);
            styleNavButton(addBtn);
            addBtn.addActionListener(e -> showMasterItemDialog(null));

            bottomPanel.add(backBtn);
            bottomPanel.add(addBtn);
        } else {
            titleLabel.setText("Karen's Shopping List");

            RoundedButton addBtn = new RoundedButton("Add Item", 12, LAVENDER);
            styleNavButton(addBtn);
            addBtn.addActionListener(e -> showItemDialog(null));

            bottomPanel.add(addBtn);
        }

        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    /**
     * Apply consistent styling to navigation buttons.
     */
    private void styleNavButton(RoundedButton btn) {
        btn.setForeground(ELECTRIC_PURPLE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setPreferredSize(new Dimension(130, 35));
    }

    /**
     * Open a dialog to add or edit items in the active shopping list.
     * * This dialog provides autocomplete suggestions for the item name
     * and includes a quick-access button to the Master List editor.
     *
     * @param itemToEdit The item to modify, or null if creating a new item.
     */
    private void showItemDialog(ShoppingItem itemToEdit) {
        boolean isEdit = (itemToEdit != null);
        String dialogTitle = isEdit ? "Edit Item" : "Add New Item";
        String buttonText = isEdit ? "Update Item" : "Save Item";

        JDialog dialog = createBaseDialog(dialogTitle);
        dialog.setSize(320, 480);
        GradientPanel dialogPanel = (GradientPanel) dialog.getContentPane().getComponent(0);

        // Autocomplete suggestions based on master history
        String[] suggestions = manager.getFilteredSuggestions(itemToEdit);
        JComboBox<String> nameBox = new JComboBox<>(suggestions);
        nameBox.setEditable(true);
        styleComboBox(nameBox);

        JTextField amountField = new JTextField();
        JComboBox<String> unitBox = new JComboBox<>(UNITS);
        styleComboBox(unitBox);

        // Pre-fill fields if editing an existing item
        if (isEdit) {
            nameBox.setSelectedItem(itemToEdit.getItemName());
            amountField.setText(itemToEdit.getAmount() != null ? itemToEdit.getAmount().toString() : "");
            if (itemToEdit.getUnit() == null || itemToEdit.getUnit().isEmpty())
                unitBox.setSelectedIndex(0);
            else
                unitBox.setSelectedItem(itemToEdit.getUnit());
        } else {
            nameBox.setSelectedItem("");
            unitBox.setSelectedIndex(0);
        }

        RoundedButton actionBtn = createDialogButton(buttonText);
        actionBtn.addActionListener(e -> {
            try {
                Object input = nameBox.getEditor().getItem();
                String name = (input == null) ? "" : input.toString().trim().toLowerCase();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Item name cannot be empty.", "Input Error",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Double amt = amountField.getText().isEmpty() ? null : Double.parseDouble(amountField.getText());
                String unit = (String) unitBox.getSelectedItem();
                if ("select unit".equals(unit))
                    unit = "";

                if (isEdit)
                    manager.updateItem(itemToEdit, name, amt, unit);
                else
                    manager.addItem(name, amt, unit);

                refreshDisplay();
                dialog.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid number (e.g., 1.5).",
                        "Numeric Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Navigation button to switch to Master List management
        RoundedButton editMasterBtn = createDialogButton("Edit Master List");
        editMasterBtn.addActionListener(e -> {
            dialog.dispose();
            isMasterView = true;
            updateBottomPanel();
            refreshDisplay();
        });

        dialogPanel.add(createFieldPanel("item name:", nameBox));
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(createFieldPanel("amount (number):", amountField));
        dialogPanel.add(Box.createVerticalStrut(10));
        dialogPanel.add(createFieldPanel("unit:", unitBox));
        dialogPanel.add(Box.createVerticalStrut(20));
        dialogPanel.add(actionBtn);
        dialogPanel.add(Box.createVerticalGlue()); // Push master list button to the bottom
        dialogPanel.add(editMasterBtn);

        dialog.setVisible(true);
    }

    /**
     * Open a dialog to manage entries in the Master List history.
     * * This simplified dialog focuses exclusively on the item name
     * for global reference and autocomplete seeding.
     *
     * @param itemToEdit The master item name to modify, or null for new entries.
     */
    private void showMasterItemDialog(String itemToEdit) {
        boolean isEdit = (itemToEdit != null);
        String dialogTitle = isEdit ? "Edit Master List Item" : "Add to Master List";
        String buttonText = isEdit ? "Update Master List" : "Add to Master List";

        JDialog dialog = createBaseDialog(dialogTitle);
        dialog.setSize(320, 250);
        GradientPanel dialogPanel = (GradientPanel) dialog.getContentPane().getComponent(0);

        JTextField nameField = new JTextField();
        if (isEdit)
            nameField.setText(itemToEdit);

        RoundedButton actionBtn = createDialogButton(buttonText);
        actionBtn.addActionListener(e -> {
            String name = nameField.getText().trim().toLowerCase();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Item name cannot be empty.", "Input Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (isEdit)
                manager.updateMasterItem(itemToEdit, name);
            else
                manager.addMasterItem(name);

            refreshDisplay();
            dialog.dispose();
        });

        dialogPanel.add(createFieldPanel("item name:", nameField));
        dialogPanel.add(Box.createVerticalStrut(20));
        dialogPanel.add(actionBtn);

        dialog.setVisible(true);
    }

    /**
     * Create a standardized JDialog with the application's gradient background.
     */
    private JDialog createBaseDialog(String title) {
        JDialog dialog = new JDialog(this, title, true);
        dialog.setSize(320, 420);
        dialog.setLocationRelativeTo(this);

        GradientPanel dialogPanel = new GradientPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(15, 25, 25, 25));

        JLabel headerLabel = new JLabel(title, SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        dialogPanel.add(headerLabel);
        dialog.add(dialogPanel);
        return dialog;
    }

    /**
     * Helper to create styled action buttons for dialog boxes.
     */
    private RoundedButton createDialogButton(String text) {
        RoundedButton btn = new RoundedButton(text, 12, LAVENDER);
        btn.setForeground(ELECTRIC_PURPLE);
        btn.setFont(new Font("Arial", Font.BOLD, 14));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return btn;
    }

    /**
     * Redraw the list container and persist changes to CSV files.
     * * This method clears the current view, iterates through the relevant
     * list (Master or Active) based on the state, and triggers an auto-save.
     */
    public void refreshDisplay() {
        listContainer.removeAll();

        if (isMasterView) {
            for (String itemName : manager.getMasterList()) {
                addMasterItemRow(itemName);
            }
        } else {
            for (ShoppingItem si : manager.getActiveList()) {
                addItemRow(si);
            }
        }

        listContainer.revalidate();
        listContainer.repaint();

        // Persist state after every change
        manager.saveActiveList(ACTIVE_FILE_NAME);
        manager.saveMasterList(MASTER_FILE_NAME);
    }

    /** Create a UI row for the active shopping list. */
    private void addItemRow(ShoppingItem si) {
        JPanel row = createBaseRowPanel();

        JCheckBox check = new JCheckBox();
        check.setOpaque(false);
        check.addActionListener(e -> {
            if (check.isSelected()) {
                manager.removeItem(si);
                refreshDisplay();
            }
        });

        JLabel label = createRowLabel(si.toString());
        RoundedButton editBtn = createRowEditButton();
        editBtn.addActionListener(e -> showItemDialog(si));

        row.add(check, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        row.add(editBtn, BorderLayout.EAST);
        listContainer.add(row);
    }

    /** Create a UI row for the master list editor. */
    private void addMasterItemRow(String itemName) {
        JPanel row = createBaseRowPanel();

        JCheckBox check = new JCheckBox();
        check.setOpaque(false);
        check.addActionListener(e -> {
            if (check.isSelected()) {
                manager.removeMasterItem(itemName);
                refreshDisplay();
            }
        });

        JLabel label = createRowLabel(itemName);
        RoundedButton editBtn = createRowEditButton();
        editBtn.addActionListener(e -> showMasterItemDialog(itemName));

        row.add(check, BorderLayout.WEST);
        row.add(label, BorderLayout.CENTER);
        row.add(editBtn, BorderLayout.EAST);
        listContainer.add(row);
    }

    // --- Layout and Styling Helpers ---

    private JPanel createBaseRowPanel() {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(400, 50));
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(255, 255, 255, 40)),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)));
        return row;
    }

    private JLabel createRowLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 16));
        return label;
    }

    private RoundedButton createRowEditButton() {
        RoundedButton editBtn = new RoundedButton("Edit", 12, LAVENDER);
        editBtn.setFont(new Font("Arial", Font.PLAIN, 11));
        editBtn.setPreferredSize(new Dimension(62, 25));
        editBtn.setForeground(ELECTRIC_PURPLE);
        return editBtn;
    }

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
 * Custom JPanel that renders a vertical sky-blue to deep-purple gradient.
 */
class GradientPanel extends JPanel {
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gp = new GradientPaint(0, 0, new Color(135, 206, 250), 0, getHeight(), new Color(120, 81, 169));
        g2d.setPaint(gp);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}

/**
 * Custom JButton that supports rounded corners and distinct background colors.
 */
class RoundedButton extends JButton {
    private Color bgColor;

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