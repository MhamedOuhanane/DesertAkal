package com.desertakal.desertakal.config.brand;

import java.awt.*;

/**
 * Desert Akal Design Tokens - Colors
 * Synced with frontend styles.scss
 */
public final class BrandColors {

    private BrandColors() {}

    // ═══════════════════════════════════════
    //  LOGO
    // ═══════════════════════════════════════
    public static final Color LOGO_BLUE = new Color(30, 64, 175);      // #1e40af
    public static final Color LOGO_GOLD = new Color(245, 158, 11);     // #f59e0b

    public static final String LOGO_BLUE_HEX = "#1e40af";
    public static final String LOGO_GOLD_HEX = "#f59e0b";

    // ═══════════════════════════════════════
    //  PRIMARY
    // ═══════════════════════════════════════
    public static final Color PRIMARY = new Color(244, 157, 37);       // #f49d25
    public static final Color PRIMARY_HOVER = new Color(224, 141, 21); // #e08d15
    public static final Color PRIMARY_DARK = new Color(217, 119, 6);   // #d97706

    public static final String PRIMARY_HEX = "#f49d25";
    public static final String PRIMARY_HOVER_HEX = "#e08d15";
    public static final String PRIMARY_DARK_HEX = "#d97706";

    // ═══════════════════════════════════════
    //  SECONDARY
    // ═══════════════════════════════════════
    public static final Color SECONDARY = new Color(224, 122, 95);     // #e07a5f
    public static final String SECONDARY_HEX = "#e07a5f";

    // ═══════════════════════════════════════
    //  BACKGROUNDS
    // ═══════════════════════════════════════
    public static final Color BG = new Color(248, 247, 245);           // #f8f7f5
    public static final Color SURFACE = Color.WHITE;
    public static final Color SECTION_BG = new Color(253, 250, 245);
    public static final Color CREAM = new Color(255, 238, 214);

    public static final String BG_HEX = "#f8f7f5";
    public static final String SURFACE_HEX = "#ffffff";
    public static final String SECTION_BG_HEX = "#fdfaf5";

    // ═══════════════════════════════════════
    //  BORDERS
    // ═══════════════════════════════════════
    public static final Color BORDER = new Color(229, 224, 216);       // #e5e0d8
    public static final Color DIVIDER = new Color(214, 211, 209);      // #d6d3d1

    public static final String BORDER_HEX = "#e5e0d8";
    public static final String DIVIDER_HEX = "#d6d3d1";

    // ═══════════════════════════════════════
    //  TEXT
    // ═══════════════════════════════════════
    public static final Color TEXT_PRIMARY = new Color(28, 22, 13);    // #1c160d
    public static final Color TEXT_SECONDARY = new Color(107, 100, 86);// #6b6456
    public static final Color TEXT_TERTIARY = new Color(156, 122, 73); // #9c7a49
    public static final Color TEXT_DISABLED = new Color(168, 162, 158);// #a8a29e

    public static final String TEXT_PRIMARY_HEX = "#1c160d";
    public static final String TEXT_SECONDARY_HEX = "#6b6456";
    public static final String TEXT_TERTIARY_HEX = "#9c7a49";
    public static final String TEXT_DISABLED_HEX = "#a8a29e";

    // ═══════════════════════════════════════
    //  STATUS
    // ═══════════════════════════════════════
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color SUCCESS_BG = new Color(209, 250, 229);
    public static final Color ERROR = new Color(239, 68, 68);
    public static final Color ERROR_BG = new Color(254, 226, 226);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color WARNING_BG = new Color(254, 243, 199);
    public static final Color INFO = new Color(59, 130, 246);
    public static final Color INFO_BG = new Color(219, 234, 254);

    public static final String SUCCESS_HEX = "#10b981";
    public static final String SUCCESS_BG_HEX = "#d1fae5";
    public static final String ERROR_HEX = "#ef4444";
    public static final String ERROR_BG_HEX = "#fee2e2";
    public static final String WARNING_HEX = "#f59e0b";
    public static final String WARNING_BG_HEX = "#fef3c7";
    public static final String INFO_HEX = "#3b82f6";
    public static final String INFO_BG_HEX = "#dbeafe";

    // ═══════════════════════════════════════
    //  HEADER
    // ═══════════════════════════════════════
    public static final Color HEADER_BG = Color.WHITE;
    public static final Color HEADER_TEXT_MAIN = TEXT_PRIMARY;
    public static final Color HEADER_TEXT_DIM = TEXT_TERTIARY;

    public static final String HEADER_BG_HEX = SURFACE_HEX;

    // ═══════════════════════════════════════
    //  SHADOW
    // ═══════════════════════════════════════
    public static final Color SHADOW = new Color(200, 195, 188);

    // ═══════════════════════════════════════
    //  STATUS COLOR HELPER
    // ═══════════════════════════════════════
    public static Color[] statusColors(String status) {
        if (status == null) return new Color[]{new Color(245, 245, 245), TEXT_SECONDARY};
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> new Color[]{SUCCESS_BG, SUCCESS};
            case "PENDING" -> new Color[]{WARNING_BG, WARNING};
            case "CANCELLED" -> new Color[]{ERROR_BG, ERROR};
            case "COMPLETED" -> new Color[]{INFO_BG, INFO};
            default -> new Color[]{new Color(245, 245, 245), TEXT_SECONDARY};
        };
    }

    public static String[] statusColorsHex(String status) {
        if (status == null) return new String[]{"#f5f5f5", TEXT_SECONDARY_HEX};
        return switch (status.toUpperCase()) {
            case "CONFIRMED" -> new String[]{SUCCESS_BG_HEX, SUCCESS_HEX};
            case "PENDING" -> new String[]{WARNING_BG_HEX, WARNING_HEX};
            case "CANCELLED" -> new String[]{ERROR_BG_HEX, ERROR_HEX};
            case "COMPLETED" -> new String[]{INFO_BG_HEX, INFO_HEX};
            default -> new String[]{"#f5f5f5", TEXT_SECONDARY_HEX};
        };
    }
}