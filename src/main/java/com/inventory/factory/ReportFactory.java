package com.inventory.factory;

public final class ReportFactory {
    private ReportFactory() {}

    public static String createReportType(String type) {
        if (type == null) {
            throw new IllegalArgumentException("Report type is required");
        }
        return switch (type.toUpperCase()) {
            case "SALES", "INVENTORY", "PURCHASE", "LOW_STOCK" -> type.toUpperCase();
            default -> throw new IllegalArgumentException("Unsupported report type");
        };
    }
}
