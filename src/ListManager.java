import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * ListManager.java
 *
 * This class serves as the Controller/Logic layer of the application.
 * It manages two distinct datasets: the active shopping list and the
 * master history of unique items used for autocomplete and reference.
 */
public class ListManager {

    /** The current list of items actively displayed in the UI. */
    private ArrayList<ShoppingItem> activeList = new ArrayList<>();

    /**
     * A collection of all unique item names ever entered.
     *
     * Uses case-insensitive ordering to prevent duplicates like "Apple" and
     * "apple".
     */
    private TreeSet<String> masterNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

    /**
     * Create a new shopping item and add it to the active list.
     *
     * This method leverages the ShoppingItem constructor to normalize names
     * (trimming/lowercase) before adding the name to the master history and
     * sorting the active view.
     *
     * @param name   The name of the item.
     * @param amount The numerical quantity.
     * @param unit   The unit of measurement.
     */
    public void addItem(String name, Double amount, String unit) {
        ShoppingItem newItem = new ShoppingItem(name, amount, unit);

        // Record the cleaned name in master history
        masterNames.add(newItem.getItemName());

        // Add to active list and re-sort for UI consistency
        activeList.add(newItem);
        sortList();
    }

    /**
     * Modify the attributes of an existing item in the active list.
     *
     * This updates the master history by replacing the old name with the
     * new one and ensures the active list remains sorted alphabetically.
     *
     * @param item      The ShoppingItem object to be updated.
     * @param newName   The updated name.
     * @param newAmount The updated numerical quantity.
     * @param newUnit   The updated unit.
     */
    public void updateItem(ShoppingItem item, String newName, Double newAmount, String newUnit) {
        // Remove the old name from history to avoid stale entries
        masterNames.remove(item.getItemName());

        // Use setters to apply new values; name normalization happens inside
        // setItemName
        item.setItemName(newName);
        item.setAmount(newAmount);
        item.setUnit(newUnit);

        // Add the updated name back to history and refresh sort order
        masterNames.add(item.getItemName());
        sortList();
    }

    /**
     * Remove an item from the active shopping list.
     *
     * Note: This only affects the current shopping list; the item remains in the
     * master history for future autocomplete suggestions.
     *
     * @param item The ShoppingItem instance to remove.
     */
    public void removeItem(ShoppingItem item) {
        activeList.remove(item);
    }

    /**
     * Alphabetize the active list by item name.
     */
    private void sortList() {
        activeList.sort(Comparator.comparing(ShoppingItem::getItemName));
    }

    /**
     * Retrieve the current collection of active shopping items.
     *
     * @return An ArrayList of ShoppingItems.
     */
    public ArrayList<ShoppingItem> getActiveList() {
        return activeList;
    }

    /**
     * Filter the master history for autocomplete suggestions.
     * 
     * This ensures that items already on the active list do not appear in
     * the "Add Item" dropdown, preventing duplicate entries in the active list.
     *
     * @param currentlyEditing The item being modified (to allow its own name as a
     *                         suggestion).
     * @return An array of strings representing valid suggestions.
     */
    public String[] getFilteredSuggestions(ShoppingItem currentlyEditing) {
        TreeSet<String> suggestions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        suggestions.addAll(masterNames);

        for (ShoppingItem si : activeList) {
            // Only remove from suggestions if it's not the item we are currently editing
            if (currentlyEditing == null || !si.getItemName().equals(currentlyEditing.getItemName())) {
                suggestions.remove(si.getItemName());
            }
        }
        return suggestions.toArray(new String[0]);
    }

    /**
     * Retrieve the master list for editing or display.
     *
     * @return The TreeSet of all unique item names.
     */
    public TreeSet<String> getMasterList() {
        return masterNames;
    }

    /**
     * Add a name directly to the master history.
     *
     * @param name The name to be added to the reference list.
     */
    public void addMasterItem(String name) {
        if (name != null && !name.trim().isEmpty()) {
            masterNames.add(name.trim().toLowerCase());
        }
    }

    /**
     * Update an entry within the master history.
     *
     * @param oldName The name to be replaced.
     * @param newName The new name to record.
     */
    public void updateMasterItem(String oldName, String newName) {
        if (oldName != null) {
            masterNames.remove(oldName);
        }
        addMasterItem(newName);
    }

    /**
     * Permanently delete a name from the master history.
     *
     * @param name The name to remove.
     */
    public void removeMasterItem(String name) {
        masterNames.remove(name);
    }

    /**
     * Persist the active shopping list to a CSV file.
     * 
     * Uses double quotes to enclose strings, ensuring that commas within
     * item names (e.g., "beef, ground") do not break the CSV structure.
     *
     * @param filename The destination file path.
     */
    public void saveActiveList(String filename) {
        if (activeList == null)
            return;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (ShoppingItem item : activeList) {
                String name = "\"" + item.getItemName() + "\"";
                String amt = (item.getAmount() == null) ? "" : item.getAmount().toString();
                String unit = (item.getUnit() == null) ? "" : item.getUnit();

                writer.println(name + "," + amt + "," + unit);
            }
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not save shopping list to " + filename);
        }
    }

    /**
     * Load active list data from a CSV file.
     * 
     * Employs a lookahead regular expression to correctly parse commas
     * while respecting quoted strings.
     *
     * @param filename The source CSV file.
     */
    public void loadActiveList(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            return;

        activeList.clear();
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty())
                    continue;

                // Split by commas not contained within quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

                if (parts.length >= 1) {
                    String name = parts[0].replace("\"", "").trim();
                    Double amt = null;

                    if (parts.length > 1 && !parts[1].isEmpty()) {
                        try {
                            amt = Double.parseDouble(parts[1]);
                        } catch (NumberFormatException nfe) {
                            amt = null;
                        }
                    }

                    String unit = (parts.length > 2) ? parts[2].replace("\"", "").trim() : "";

                    // Populate both the list and the history
                    masterNames.add(name);
                    activeList.add(new ShoppingItem(name, amt, unit));
                }
            }
            sortList();
        } catch (Exception e) {
            System.err.println("Error loading active list: " + e.getMessage());
        }
    }

    /**
     * Persist the master history of items to a CSV file.
     *
     * @param filename The destination file path.
     */
    public void saveMasterList(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (String name : masterNames) {
                writer.println("\"" + name + "\"");
            }
        } catch (IOException e) {
            System.err.println("CRITICAL ERROR: Could not save master list to " + filename);
        }
    }

    /**
     * Load the master history from a CSV file.
     *
     * @param filename The source CSV file.
     */
    public void loadMasterList(String filename) {
        File file = new File(filename);
        if (!file.exists() || !file.isFile())
            return;

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                    continue;

                String name = line.replace("\"", "").trim();
                if (!name.isEmpty()) {
                    masterNames.add(name.toLowerCase());
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading master list: " + e.getMessage());
        }
    }
}