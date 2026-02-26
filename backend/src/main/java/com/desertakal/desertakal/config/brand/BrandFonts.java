package com.desertakal.desertakal.config.brand;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * Desert Akal Design Tokens - Fonts
 * PDF: Helvetica (closest to Plus Jakarta Sans)
 * Email: Plus Jakarta Sans with fallbacks
 */
public final class BrandFonts {

    private BrandFonts() {}

    // ── PDF Fonts ──
    public static final PDType1Font BOLD = PDType1Font.HELVETICA_BOLD;
    public static final PDType1Font REGULAR = PDType1Font.HELVETICA;
    public static final PDType1Font ITALIC = PDType1Font.HELVETICA_OBLIQUE;
    public static final PDType1Font BOLD_ITALIC = PDType1Font.HELVETICA_BOLD_OBLIQUE;

    // ── Email / HTML Font Stack ──
    public static final String FONT_FAMILY =
            "'Plus Jakarta Sans', 'Inter', -apple-system, BlinkMacSystemFont, " +
                    "'Segoe UI', Roboto, sans-serif";

    // ── Google Fonts Import ──
    public static final String GOOGLE_FONTS_URL =
            "https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:" +
                    "wght@400;500;600;700;800&display=swap";
}