package com.inventory.dao;

import com.inventory.model.Inventory;

public interface InventoryDAO {
    Inventory findByProductId(int productId);
    void decreaseQuantity(int productId, int quantity);
    void increaseQuantity(int productId, int quantity);
}
