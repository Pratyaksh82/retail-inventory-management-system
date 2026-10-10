package com.inventory.service;

import com.inventory.dao.InventoryDAO;
import com.inventory.model.Inventory;

public class InventoryService {
    private final InventoryDAO inventoryDAO;

    public InventoryService(InventoryDAO inventoryDAO) {
        this.inventoryDAO = inventoryDAO;
    }

    public boolean hasSufficientStock(int productId, int requiredQty) {
        Inventory inventory = inventoryDAO.findByProductId(productId);
        return inventory != null && inventory.getQuantity() >= requiredQty;
    }

    public void reduceStock(int productId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (!hasSufficientStock(productId, quantity)) {
            throw new IllegalArgumentException("Insufficient stock");
        }
        inventoryDAO.decreaseQuantity(productId, quantity);
    }
}
