import javax.swing.SwingUtilities;

/**
 * ShoppingListApp.java
 *
 * The main entry point for the application.
 * This class coordinates the initialization of the data layer (ListManager)
 * and the user interface (ShoppingListGUI).
 */
public class ShoppingListApp {

    /**
     * Launch the application and handle initial state setup.
     *
     * Uses SwingUtilities.invokeLater to ensure thread safety when
     * initializing the GUI on the Event Dispatch Thread (EDT).
     */
    public static void main(String[] args) {
        // Initialize the backend data manager
        ListManager manager = new ListManager();

        // Launch UI and handle first-run data population
        SwingUtilities.invokeLater(() -> {
            // Instantiate the GUI, which automatically triggers file loading
            ShoppingListGUI gui = new ShoppingListGUI(manager);

            /**
             * SEED DATA LOGIC:
             * If no master list is found (first run), populate the manager
             * with a default variety of items to showcase functionality.
             */
            if (manager.getMasterList().isEmpty()) {
                String[] demoItems = {
                        "apples", "avocados", "bananas", "beef, ground", "blueberries", "bread", "butter",
                        "carrots", "cheese", "chicken thighs", "coffee", "eggs", "flour", "garlic",
                        "honey", "lemons", "limes", "milk", "olive oil", "onions", "pasta",
                        "potatoes", "rice", "spinach", "strawberries"
                };

                for (String item : demoItems) {
                    manager.addMasterItem(item);
                }

                // Persist the seeded data immediately
                manager.saveMasterList("master_list.csv");
            }

            // Display the window
            gui.setVisible(true);
        });
    }
}