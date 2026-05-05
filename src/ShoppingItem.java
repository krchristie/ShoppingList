/**
 * ShoppingItem.java
 *
 * Represents a single item in the shopping list.
 * Functions as the Model in the application architecture, demonstrating
 * encapsulation through private fields and public accessors.
 */
public class ShoppingItem {

    /** The descriptive name of the grocery item. */
    private String itemName;

    /** The numerical quantity of the item (can be null if unspecified). */
    private Double amount;

    /** The unit of measurement for the amount (e.g., "lbs", "oz", "gal"). */
    private String unit;

    /**
     * Constructs a new ShoppingItem.
     *
     * @param itemName The name of the item (stored in lowercase for consistency).
     * @param amount   The quantity of the item (Double type allows for fractions).
     * @param unit     The unit of measurement.
     */
    public ShoppingItem(String itemName, Double amount, String unit) {
        this.itemName = itemName;
        this.amount = amount;
        this.unit = unit;
    }

    /**
     * Retrieves the item name.
     *
     * @return The name of the item.
     */
    public String getItemName() {
        return itemName;
    }

    /**
     * Updates the item name.
     *
     * @param itemName The new name to set.
     */
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    /**
     * Retrieves the item amount.
     *
     * @return The quantity of the item as a Double, or null if not set.
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Updates the item amount.
     *
     * @param amount The new quantity to set.
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * Retrieves the unit of measurement.
     *
     * @return The unit of measurement as a String.
     */
    public String getUnit() {
        return unit;
    }

    /**
     * Updates the unit of measurement.
     *
     * @param unit The new unit to set.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * Returns a formatted string representation of the item for UI display.
     * Formats numeric amounts by hiding the decimal if the value is a whole number,
     * and applies proper plural/singular formatting to the unit.
     *
     * @return A formatted string (e.g., "apples, 2 lbs" or "milk, 1 gal").
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(itemName);

        if (amount != null && amount > 0) {
            sb.append(", ");

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
     * Converts specified plural units to their singular form if the amount is
     * exactly 1.0.
     *
     * @param amt         The numerical quantity of the item.
     * @param currentUnit The assigned unit of measurement.
     * @return The properly formatted unit string (singular or plural).
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