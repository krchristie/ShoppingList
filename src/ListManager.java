import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * ListManager.java
 *
 * Handles the business logic of the application.
 * Manages the active shopping list, file persistence (CSV),
 * and the master history for autocomplete functionality.
 */
public class ListManager {

    /** The current list of items actively displayed in the UI. */
    private ArrayList<ShoppingItem> activeList = new ArrayList<>();

    /**
     * Master history of all unique item names ever entered IN SINGLE SESSION
     * !! NEED TO IMPLEMENT SAVING MASTER LIST INTO FILE
     *
     * Uses a TreeSet with case-insensitive ordering to prevent duplicates.
     */
    private TreeSet<String> masterNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Adds a new item to the active list and updates the master name history.
     * Ensures the item name is stored in lowercase for consistency and sorts
     * the list immediately after insertion.
     *
     * @param name   The name of the shopping item.
     * @param amount The numerical quantity of the item (can be null).
     * @param unit   The unit of measurement (e.g., "lbs", "gal").
     */
    public void addItem(String name, Double amount, String unit) {
        String lowerName = name.toLowerCase();
        masterNames.add(lowerName);
        activeList.add(new ShoppingItem(lowerName, amount, unit));
        sortList();
    }

    /**
     * Updates the attributes of an existing ShoppingItem.
     * Manages the master history by removing the old name and inserting
     * the new name before applying changes and sorting the list.
     *
     * @param item      The existing ShoppingItem object to update.
     * @param newName   The new name for the item.
     * @param newAmount The new numerical quantity (can be null).
     * @param newUnit   The new unit of measurement.
     */
    public void updateItem(ShoppingItem item, String newName, Double newAmount, String newUnit) {
        masterNames.remove(item.getItemName());
        String lowerName = newName.toLowerCase();
        masterNames.add(lowerName);

        item.setItemName(lowerName);
        item.setAmount(newAmount);
        item.setUnit(newUnit);
        sortList();
    }

    /**
     * Removes a specified item from the active shopping list.
     * Does not remove the item from the master history.
     *
     * @param item The ShoppingItem to remove.
     */
    public void removeItem(ShoppingItem item) {
        activeList.remove(item);
    }

    /**
     * Sorts the active list alphabetically by item name.
     */
    private void sortList() {
        activeList.sort(Comparator.comparing(ShoppingItem::getItemName));
    }

    /**
     * Retrieves the current active shopping list.
     *
     * @return An ArrayList containing the active ShoppingItem objects.
     */
    public ArrayList<ShoppingItem> getActiveList() {
        return activeList;
    }

    /**
     * Generates a list of suggested item names for autocomplete.
     * Filters the master history to exclude items that are already present
     * in the active list, except for the item currently being edited.
     *
     * @param currentlyEditing The ShoppingItem being edited, or null if adding new.
     * @return An array of String suggestions for the UI dropdown.
     */
    public String[] getFilteredSuggestions(ShoppingItem currentlyEditing) {
        TreeSet<String> suggestions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        suggestions.addAll(masterNames);
        for (ShoppingItem si : activeList) {
            if (currentlyEditing == null || !si.getItemName().equals(currentlyEditing.getItemName())) {
                suggestions.remove(si.getItemName());
            }
        }
        return suggestions.toArray(new String[0]);
    }

    /**
     * Saves the current active list to a CSV file.
     * Wraps item names in quotes to safely handle entries containing commas.
     *
     * @param filename The destination file path (e.g., "shopping_list.csv").
     */
    public void saveToFile(String filename) {
        // Safety check to ensure we aren't trying to save a non-existent list
        if (activeList == null) {
            return;
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (ShoppingItem item : activeList) {
                String name = "\"" + item.getItemName() + "\"";
                String amt = (item.getAmount() == null) ? "" : item.getAmount().toString();
                String unit = (item.getUnit() == null) ? "" : item.getUnit();

                writer.println(name + "," + amt + "," + unit);
            }
        } catch (IOException e) {
            System.err.println(
                    "CRITICAL ERROR: Could not save shopping list to " + filename + ". Check folder permissions.");
        }
    }

    /**
     * Loads the active list and master history from a CSV file.
     * Uses regular expressions to split rows by commas while ignoring
     * commas that are enclosed within quotation marks.
     *
     * @param filename The source file path to read from.
     */
    public void loadFromFile(String filename) {
        File file = new File(filename);

        // Ensure it exists AND is actually a file (not a directory)
        if (!file.exists() || !file.isFile()) {
            return;
        }

        activeList.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty())
                    continue;

                // Regex: Split by comma only if it is not inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (parts.length >= 1) {
                    String name = parts[0].replace("\"", "").trim();

                    Double amt = null;
                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        try {
                            amt = Double.parseDouble(parts[1]);
                        } catch (NumberFormatException nfe) {
                            amt = null; // Defensive: Keep the item but clear corrupted amount
                        }
                    }

                    String unit = (parts.length > 2) ? parts[2].trim() : "";

                    masterNames.add(name);
                    activeList.add(new ShoppingItem(name, amt, unit));
                }
            }
            sortList();
        } catch (FileNotFoundException e) {
            System.err.println("Load Error: File not found - " + filename);
        } catch (Exception e) {
            System.err.println("Error loading file: " + e.getMessage());
        }
    }
}