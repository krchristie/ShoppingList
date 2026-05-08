/**
 * ShoppingItem.java
 *
 * This class serves as the core data model for the application.
 * It encapsulates the attributes of a grocery item—name, quantity, and unit—and
 * provides logic for human-readable string formatting.
 */
public class ShoppingItem {

    /** The descriptive name of the grocery item. */
    private String itemName;

    /**
     * The numerical quantity of the item; uses Double to allow for fractional
     * amounts.
     */
    private Double amount;

    /** The unit of measurement for the amount (e.g., "lb", "oz", "gal", etc.). */
    private String unit;

    /**
     * Primary constructor to initialize a ShoppingItem with all attributes.
     *
     * @param itemName The name of the item.
     * @param amount   The quantity of the item (null if no amount is specified).
     * @param unit     The unit of measurement (empty string if no unit is
     *                 specified).
     */
    public ShoppingItem(String itemName, Double amount, String unit) {
        setItemName(itemName); // Use setter to ensure name normalization
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * Secondary constructor for items where only a name is known.
     * Often used when pulling items from the Master List history.
     *
     * @param itemName The name of the item.
     */
    public ShoppingItem(String itemName) {
        this(itemName, null, "");
    }

    /**
     * Retrieve the item name.
     *
     * @return The name of the item.
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Update the item name.
     * Normalizes input by trimming whitespace and converting to lowercase.
     *
     * @param itemName The new name to assign to this item.
     */
    public void setItemName(String itemName) {
        if (itemName != null) {
            this.itemName = itemName.trim().toLowerCase();
        } else {
            this.itemName = "";
        }
    }

    /**
     * Retrieve the item amount.
     *
     * @return The numerical quantity, or null if unspecified.
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Update the item amount.
     *
     * @param amount The new quantity to assign.
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * Retrieve the unit of measurement.
     *
     * @return The string representing the unit.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Update the unit of measurement.
     *
     * @param unit The new unit label (e.g., "g", "kg").
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Generate a formatted string for display in the user interface.
     *
     * This method builds a clean representation of the item. It automatically
     * strips trailing zeros from whole numbers (e.g., "1.0" becomes "1")
     * and calls formatUnit to handle singular/plural consistency.
     *
     * @return A formatted string such as "apples, 2 lbs" or "milk, 1 gal".
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(itemName);

        // Only append details if an amount exists and is greater than zero
        if (amount != null && amount > 0) {
            sb.append(", ");

            // Check if the number is a whole integer to avoid ".0" in the UI
            if (amount == amount.intValue()) {
                sb.append(amount.intValue());
            } else {
                sb.append(amount);
            }

            if (unit != null && !unit.isEmpty()) {
                sb.append(" ").append(formatUnit(amount, unit));
            }
        }
        return sb.toString();
    }

    /**
     * Adjust units for singular/plural grammatical correctness.
     *
     * Maps common abbreviations (lbs, qts, pts) back to their singular
     * form if the quantity is exactly 1.0.
     *
     * @param amt         The quantity used to determine plurality.
     * @param currentUnit The unit string to be evaluated.
     * 
     * @return The grammatically appropriate unit string.
     */
    private String formatUnit(Double amt, String currentUnit) {
        if (amt == null || amt != 1.0) {
            return currentUnit;
        }

        switch (currentUnit.toLowerCase()) {
            case "lbs":
                return "lb";
            case "qts":
                return "qt";
            case "pts":
                return "pt";
            default:
                return currentUnit;
        }
    }
}