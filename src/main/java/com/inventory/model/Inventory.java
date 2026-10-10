package com.inventory.model;

public class Inventory {
    private int productId;
    private int quantity;
    private int minimumStock;

    public Inventory(int productId, int quantity, int minimumStock) {
        this.productId = productId;
        this.quantity = quantity;
        this.minimumStock = minimumStock;
    }

    public int getProductId() { return productId; }
    public int getQuantity() { return quantity; }
    public int getMinimumStock() { return minimumStock; }
}
