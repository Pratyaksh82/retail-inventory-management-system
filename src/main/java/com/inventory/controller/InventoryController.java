package com.inventory.controller;

import com.inventory.service.InventoryService;

public class InventoryController {
    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void reduceStock(int productId, int quantity) {
        inventoryService.reduceStock(productId, quantity);
    }
}
