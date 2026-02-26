package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.config.brand.BrandConfig;
import com.desertakal.desertakal.config.brand.BrandInfo;
import com.desertakal.desertakal.exception.custom.DocumentGenerationException;
import com.desertakal.desertakal.model.dto.reservation.ReservationFindDTO;
import com.desertakal.desertakal.model.entity.Reservation;
import com.desertakal.desertakal.model.mapper.ReservationMapper;
import com.desertakal.desertakal.service.interfaces.DocumentGeneratorService;
import com.desertakal.desertakal.service.interfaces.FileStorageService;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static com.desertakal.desertakal.config.brand.BrandColors.*;
import static com.desertakal.desertakal.config.brand.BrandFonts.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DocumentGeneratorServiceImpl implements DocumentGeneratorService {
    private final FileStorageService fileStorageService;
    private final ReservationMapper reservationMapper;
    private final BrandInfo brand;
    private final BrandConfig brandConfig;

    // ── Formatters ──
    private static final DateTimeFormatter FMT_DATE = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter FMT_DT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    // ── Page ──
    private static final float PW = PDRectangle.A4.getWidth();
    private static final float PH = PDRectangle.A4.getHeight();
    private static final float HEADER_LOGO_SIZE = 50;


    // ═══════════════════════════════════════════════════
    //  PUBLIC METHODS
    // ═══════════════════════════════════════════════════

    @Override
    @Transactional
    public void generateConfirmationAssets(@NonNull Reservation reservation) {
        log.info("Generating assets for Reservation: {}", reservation.getUuid());

        if (reservation.getPdfUrl() != null) fileStorageService.deleteFile(reservation.getPdfUrl());
        if (reservation.getQrCode() != null) fileStorageService.deleteFile(reservation.getQrCode());

        ReservationFindDTO dto = reservationMapper.toFindDto(reservation);
        byte[] qrBytes = generateQRCodeImage(reservation.getUuid());
        byte[] pdfBytes = generateReservationPdf(dto, qrBytes);

        long ts = System.currentTimeMillis();
        String pdfPath = "reservations/vouchers/" + reservation.getUuid() + "_" + ts + ".pdf";
        String qrPath = "reservations/qrcodes/" + reservation.getUuid() + "_" + ts + ".png";

        reservation.setPdfUrl(fileStorageService.uploadBytes(pdfBytes, pdfPath, "application/pdf"));
        reservation.setQrCode(fileStorageService.uploadBytes(qrBytes, qrPath, "image/png"));
    }

    @Override
    public byte[] generateQRCodeImage(@NonNull UUID uuid) {
        try {
            String data = String.format("%s/reservations/verify/%s", brand.getFrontendUrl(), uuid);
            BitMatrix matrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new DocumentGenerationException("Failed to generate QR Code.");
        }
    }

    @Override
    public byte[] generateReservationPdf(@NonNull ReservationFindDTO dto, byte[] qrImage) {
        log.info("Generating ticket for reservation: {}", dto.getUuid());

        try (PDDocument doc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float ticketW = 550;
            float ticketH = 420;
            float ticketX = (PW - ticketW) / 2;
            float ticketY = (PH - ticketH) / 2;
            float ticketTop = ticketY + ticketH;

            float headerH = 52;
            float footerH = 32;
            float mainW = 380;
            float stubW = ticketW - mainW;
            float perfX = ticketX + mainW;

            float bodyTop = ticketTop - headerH;
            float bodyBot = ticketY + footerH;

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                fill(cs, 0, 0, PW, PH, BG);
                fill(cs, ticketX + 4, ticketY - 4, ticketW, ticketH, SHADOW);
                fill(cs, ticketX, ticketY, ticketW, ticketH, SURFACE);
                stroke(cs, ticketX, ticketY, ticketW, ticketH, BORDER);

                drawHeader(doc, cs, ticketX, ticketTop, ticketW, headerH, dto);
                drawFooter(cs, ticketX, ticketY, ticketW, footerH);
                drawPerforation(cs, perfX, bodyTop, bodyBot);
                drawBody(cs, ticketX, bodyTop, mainW, bodyBot, dto);
                drawStub(doc, cs, perfX, bodyTop, stubW, bodyBot, dto, qrImage);
            }

            doc.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new DocumentGenerationException("Failed to generate ticket PDF.");
        }
    }


    // ═══════════════════════════════════════════════════
    //  HEADER - يستعمل BrandColors + BrandInfo
    // ═══════════════════════════════════════════════════

    private void drawHeader(PDDocument doc, PDPageContentStream cs,
                            float x, float top, float w, float h,
                            ReservationFindDTO dto) throws IOException {
        float y = top - h;

        fill(cs, x, y, w, h, HEADER_BG);
        fill(cs, x, y, w, 1.5f, PRIMARY);

        drawLogo(doc, cs, x, y, h);

        float brandX = x + 14 + HEADER_LOGO_SIZE + 10;
        float brandY = y + h / 2 + 2;

        text(cs, brandX, brandY, "Desert", BOLD, 16, LOGO_BLUE);
        float desertW = BOLD.getStringWidth("Desert") / 1000 * 16;
        text(cs, brandX + desertW, brandY, "Akal", BOLD, 16, LOGO_GOLD);

        text(cs, brandX, brandY - 14, BrandInfo.TAGLINE,
                ITALIC, 6.5f, HEADER_TEXT_DIM);

        textCenter(cs, x, w, y + 12, "EXPLORATION PASS",
                BOLD, 7.5f, HEADER_TEXT_DIM);

        String ref = "#" + dto.getUuid().toString().substring(0, 8).toUpperCase();
        textRight(cs, x + w - 14, brandY + 2, ref, BOLD, 11, HEADER_TEXT_MAIN);

        if (dto.getDate() != null) {
            textRight(cs, x + w - 14, brandY - 13,
                    dto.getDate().format(FMT_DT), REGULAR, 7, TEXT_SECONDARY);
        }
    }


    // ═══════════════════════════════════════════════════
    //  BODY
    // ═══════════════════════════════════════════════════

    private void drawBody(PDPageContentStream cs, float tX, float bodyTop,
                          float mainW, float bodyBot,
                          ReservationFindDTO dto) throws IOException {

        float pad = 22;
        float x = tX + pad;
        float lineW = mainW - (pad * 2);
        float col1 = x;
        float col2 = x + 130;
        float col3 = x + 255;

        float y = bodyTop;

        y -= 14;
        text(cs, x, y, "PASSENGER", REGULAR, 6.5f, TEXT_DISABLED);
        y -= 15;
        text(cs, x, y, safe(dto.getTouristName()), BOLD, 17, TEXT_PRIMARY);

        y -= 18;
        text(cs, x, y, safe(dto.getTourTitle()), REGULAR, 11, PRIMARY);

        y -= 14;
        fill(cs, x, y, lineW, 0.6f, BORDER);

        y -= 12;
        boardingField(cs, col1, y, "DEPARTURE",
                dto.getStartDate() != null ? dto.getStartDate().format(FMT_DATE) : "N/A",
                TEXT_PRIMARY);
        boardingField(cs, col2, y, "RETURN",
                dto.getEndDate() != null ? dto.getEndDate().format(FMT_DATE) : "N/A",
                TEXT_PRIMARY);
        boardingField(cs, col3, y, "GUIDE",
                safe(dto.getGuideName()), LOGO_BLUE);

        y -= 48;
        cs.setStrokingColor(BORDER);
        cs.setLineWidth(0.4f);
        cs.setLineDashPattern(new float[]{2, 2}, 0);
        cs.moveTo(x, y);
        cs.lineTo(x + lineW, y);
        cs.stroke();
        cs.setLineDashPattern(new float[]{}, 0);

        y -= 12;
        long days = 0;
        if (dto.getStartDate() != null && dto.getEndDate() != null) {
            days = java.time.Duration.between(dto.getStartDate(), dto.getEndDate()).toDays();
        }
        boardingField(cs, col1, y, "DURATION",
                days > 0 ? days + " days" : "N/A", TEXT_PRIMARY);
        boardingField(cs, col2, y, "GUESTS",
                dto.getNumberPeople() != null ? dto.getNumberPeople() + " people" : "N/A",
                TEXT_PRIMARY);

        text(cs, col3, y, "TOTAL", REGULAR, 6.5f, TEXT_DISABLED);
        String amount = dto.getAmount() != null ? "$" + dto.getAmount().toPlainString() : "N/A";
        text(cs, col3, y - 16, amount, BOLD, 16, PRIMARY);

        y -= 50;
        fill(cs, x, y, lineW, 0.6f, BORDER);

        y -= 16;
        String status = safe(dto.getStatus());
        Color[] badge = statusColors(status);
        float statusTextW = BOLD.getStringWidth(status) / 1000 * 8;
        float badgeW = statusTextW + 16;

        fill(cs, x, y - 3, badgeW, 15, badge[0]);
        text(cs, x + 8, y, status, BOLD, 8, badge[1]);

        String bookInfo = "Booked: " + (dto.getDate() != null ? dto.getDate().format(FMT_DT) : "N/A");
        textRight(cs, x + lineW, y, bookInfo, REGULAR, 7.5f, TEXT_DISABLED);
    }


    // ═══════════════════════════════════════════════════
    //  PERFORATION
    // ═══════════════════════════════════════════════════

    private void drawPerforation(PDPageContentStream cs, float perfX,
                                 float top, float bot) throws IOException {
        float r = 11;
        circle(cs, perfX, top, r, BG);
        strokeCircle(cs, perfX, top, r, BORDER);
        circle(cs, perfX, bot, r, BG);
        strokeCircle(cs, perfX, bot, r, BORDER);

        cs.setStrokingColor(BORDER);
        cs.setLineWidth(0.8f);
        cs.setLineDashPattern(new float[]{3, 4}, 0);
        cs.moveTo(perfX, top - r);
        cs.lineTo(perfX, bot + r);
        cs.stroke();
        cs.setLineDashPattern(new float[]{}, 0);
    }


    // ═══════════════════════════════════════════════════
    //  STUB
    // ═══════════════════════════════════════════════════

    private void drawStub(PDDocument doc, PDPageContentStream cs,
                          float perfX, float bodyTop, float stubW, float bodyBot,
                          ReservationFindDTO dto, byte[] qrImage) throws IOException {

        fill(cs, perfX, bodyBot, stubW, bodyTop - bodyBot, SECTION_BG);

        float centerX = perfX + stubW / 2;
        float y = bodyTop;

        y -= 18;
        textCenter(cs, perfX, stubW, y, "SCAN TO BOARD", BOLD, 7, TEXT_TERTIARY);

        float qrSize = 100;
        y -= 10;
        float qrX = centerX - qrSize / 2;
        float qrY = y - qrSize;

        if (qrImage != null) {
            fill(cs, qrX - 6, qrY - 6, qrSize + 12, qrSize + 12, SURFACE);
            stroke(cs, qrX - 6, qrY - 6, qrSize + 12, qrSize + 12, BORDER);

            float d = 5;
            fill(cs, qrX - 6, qrY + qrSize + 6 - d, d, d, PRIMARY);
            fill(cs, qrX + qrSize + 6 - d, qrY + qrSize + 6 - d, d, d, PRIMARY);
            fill(cs, qrX - 6, qrY - 6, d, d, PRIMARY);
            fill(cs, qrX + qrSize + 6 - d, qrY - 6, d, d, PRIMARY);

            PDImageXObject qr = PDImageXObject.createFromByteArray(doc, qrImage, "qr");
            cs.drawImage(qr, qrX, qrY, qrSize, qrSize);
        }

        y = qrY - 14;
        textCenter(cs, perfX, stubW, y, "Verify your booking", ITALIC, 7, TEXT_DISABLED);

        y -= 12;
        fill(cs, perfX + 20, y, stubW - 40, 0.5f, BORDER);

        y -= 16;
        String status = safe(dto.getStatus());
        Color[] badge = statusColors(status);
        float sW = BOLD.getStringWidth(status) / 1000 * 8;
        float bW = sW + 18;
        float bX = centerX - bW / 2;
        fill(cs, bX, y - 3, bW, 15, badge[0]);
        textCenter(cs, perfX, stubW, y, status, BOLD, 8, badge[1]);

        y -= 24;
        String amt = dto.getAmount() != null ? "$" + dto.getAmount().toPlainString() : "";
        textCenter(cs, perfX, stubW, y, amt, BOLD, 18, PRIMARY);

        y -= 14;
        String guests = dto.getNumberPeople() != null ? dto.getNumberPeople() + " guests" : "";
        textCenter(cs, perfX, stubW, y, guests, REGULAR, 8, TEXT_SECONDARY);

        // ── Logo + Brand في الأسفل ──
        float brandFontSize = 8;
        float dW = BOLD.getStringWidth("Desert") / 1000 * brandFontSize;
        float aW = BOLD.getStringWidth("Akal") / 1000 * brandFontSize;
        float totalBrandW = dW + aW;

        float logoS = 50;
        float logoTextGap = 4;
        float brandTextY = bodyBot + 12;
        float logoY = brandTextY + brandFontSize + logoTextGap;
        float logoX = centerX - logoS / 2;

        if (brandConfig.isLogoLoaded() && brandConfig.getLogoPngBytes() != null) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, brandConfig.getLogoPngBytes(), "logo_stub");
            cs.drawImage(logo, logoX, logoY, logoS, logoS);
        } else {
            drawFallbackLogo(cs, centerX, logoY + logoS / 2, logoS / 2);
        }

        float startX = centerX - totalBrandW / 2;
        text(cs, startX, brandTextY, "Desert", BOLD, brandFontSize, LOGO_BLUE);
        text(cs, startX + dW, brandTextY, "Akal", BOLD, brandFontSize, LOGO_GOLD);
    }


    // ═══════════════════════════════════════════════════
    //  FOOTER - يستعمل BrandInfo
    // ═══════════════════════════════════════════════════

    private void drawFooter(PDPageContentStream cs, float x, float y,
                            float w, float h) throws IOException {
        fill(cs, x, y, w, h, new Color(250, 248, 244));
        fill(cs, x, y + h - 0.8f, w, 0.8f, BORDER);
        fill(cs, x, y, w, 2.5f, PRIMARY);

        float textY = y + h / 2 - 3;

        text(cs, x + 15, textY,
                "Thank you for choosing " + BrandInfo.COMPANY_NAME + "!",
                BOLD_ITALIC, 8, TEXT_TERTIARY);

        textRight(cs, x + w - 15, textY,
                BrandInfo.DOMAIN + "  |  " + BrandInfo.EMAIL,
                REGULAR, 7, TEXT_DISABLED);
    }


    // ═══════════════════════════════════════════════════
    //  LOGO HELPERS
    // ═══════════════════════════════════════════════════

    private void drawLogo(PDDocument doc, PDPageContentStream cs,
                          float x, float y, float h) throws IOException {
        float logoY = y + (h - HEADER_LOGO_SIZE) / 2;
        if (brandConfig.isLogoLoaded() && brandConfig.getLogoPngBytes() != null) {
            PDImageXObject logo = PDImageXObject.createFromByteArray(doc, brandConfig.getLogoPngBytes(), "logo_header");
            cs.drawImage(logo, x + 14, logoY, HEADER_LOGO_SIZE, HEADER_LOGO_SIZE);
        } else {
            drawFallbackLogo(cs, x + 14 + HEADER_LOGO_SIZE / 2,
                    logoY + HEADER_LOGO_SIZE / 2, HEADER_LOGO_SIZE / 2);
        }
    }

    private void drawFallbackLogo(PDPageContentStream cs, float cx, float cy,
                                  float r) throws IOException {
        circle(cs, cx, cy, r, LOGO_GOLD);
        float textX = cx - (BOLD.getStringWidth("D") / 1000 * 14) / 2;
        text(cs, textX, cy - 5, "D", BOLD, 14, SURFACE);
    }


    // ═══════════════════════════════════════════════════
    //  DRAWING UTILITIES
    // ═══════════════════════════════════════════════════

    private void boardingField(PDPageContentStream cs, float x, float y,
                               String label, String value,
                               Color valColor) throws IOException {
        text(cs, x, y, label, REGULAR, 6.5f, TEXT_DISABLED);
        text(cs, x, y - 16, safe(value), BOLD, 12, valColor);
    }

    private void text(PDPageContentStream cs, float x, float y,
                      String txt, PDType1Font font, float size,
                      Color c) throws IOException {
        cs.beginText();
        cs.setFont(font, size);
        cs.setNonStrokingColor(c);
        cs.newLineAtOffset(x, y);
        cs.showText(txt);
        cs.endText();
    }

    private void textCenter(PDPageContentStream cs, float areaX, float areaW,
                            float y, String txt, PDType1Font font,
                            float size, Color c) throws IOException {
        float w = font.getStringWidth(txt) / 1000 * size;
        text(cs, areaX + (areaW - w) / 2, y, txt, font, size, c);
    }

    private void textRight(PDPageContentStream cs, float rightX, float y,
                           String txt, PDType1Font font, float size,
                           Color c) throws IOException {
        float w = font.getStringWidth(txt) / 1000 * size;
        text(cs, rightX - w, y, txt, font, size, c);
    }

    private void fill(PDPageContentStream cs, float x, float y,
                      float w, float h, Color c) throws IOException {
        cs.setNonStrokingColor(c);
        cs.addRect(x, y, w, h);
        cs.fill();
    }

    private void stroke(PDPageContentStream cs, float x, float y,
                        float w, float h, Color c) throws IOException {
        cs.setStrokingColor(c);
        cs.setLineWidth(0.7f);
        cs.addRect(x, y, w, h);
        cs.stroke();
    }

    private void circle(PDPageContentStream cs, float cx, float cy,
                        float r, Color c) throws IOException {
        float k = r * 0.5522847498f;
        cs.setNonStrokingColor(c);
        cs.moveTo(cx - r, cy);
        cs.curveTo(cx - r, cy + k, cx - k, cy + r, cx, cy + r);
        cs.curveTo(cx + k, cy + r, cx + r, cy + k, cx + r, cy);
        cs.curveTo(cx + r, cy - k, cx + k, cy - r, cx, cy - r);
        cs.curveTo(cx - k, cy - r, cx - r, cy - k, cx - r, cy);
        cs.fill();
    }

    private void strokeCircle(PDPageContentStream cs, float cx, float cy,
                              float r, Color c) throws IOException {
        float k = r * 0.5522847498f;
        cs.setStrokingColor(c);
        cs.setLineWidth(0.5f);
        cs.moveTo(cx - r, cy);
        cs.curveTo(cx - r, cy + k, cx - k, cy + r, cx, cy + r);
        cs.curveTo(cx + k, cy + r, cx + r, cy + k, cx + r, cy);
        cs.curveTo(cx + r, cy - k, cx + k, cy - r, cx, cy - r);
        cs.curveTo(cx - k, cy - r, cx - r, cy - k, cx - r, cy);
        cs.stroke();
    }

    private String safe(Object v) {
        if (v == null) return "N/A";
        String s = v.toString();
        return s.isBlank() ? "N/A" : s;
    }
}