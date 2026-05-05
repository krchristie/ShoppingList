import javax.swing.SwingUtilities;

/**
 * ShoppingListApp.java
 *
 * Driver class for the Shopping List application.
 * Orchestrates the initialization of the application logic (ListManager)
 * and the graphical user interface (ShoppingListGUI).
 */
public class ShoppingListApp {

    /**
     * The main entry point for the application.
     * Populates the manager with initial demo data and launches the GUI
     * on the Event Dispatch Thread (EDT).
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        ListManager manager = new ListManager();

        /**
         * Demo data used to seed the application's autocomplete history
         * and populate the initial list if no CSV file exists.
         */
        String[] items = {
                "apples", "avocados", "bananas", "beef, ground", "blueberries", "bread", "butter",
                "carrots", "cheese", "chicken thighs", "coffee", "eggs", "flour", "garlic",
                "honey", "lemons", "limes", "milk", "olive oil", "onions", "pasta",
                "potatoes", "rice", "spinach", "strawberries"
        };

        for (String s : items) {
            // Adds items with null amount and empty unit to satisfy method signature
            manager.addItem(s, null, "");
        }

        // Ensure Swing components are created and updated on the EDT
        SwingUtilities.invokeLater(() -> {
            new ShoppingListGUI(manager).setVisible(true);
        });
    }
}