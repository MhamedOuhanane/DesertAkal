package com.desertakal.desertakal.service.impl;

import com.desertakal.desertakal.config.brand.BrandColors;
import com.desertakal.desertakal.config.brand.BrandConfig;
import com.desertakal.desertakal.config.brand.BrandFonts;
import com.desertakal.desertakal.config.brand.BrandInfo;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final BrandInfo brand;
    private final BrandConfig brandConfig;

    @Async
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = brand.getApiUrl() + "/api/auth/verify-email?token=" + token;

        String html = buildEmail(
                "Verify Your Email",
                "Welcome to " + BrandInfo.COMPANY_NAME + "!",
                "Thank you for joining us. To complete your registration " +
                        "and activate your account, please click the button below:",
                "Verify Email Address",
                verificationUrl,
                "This link is valid for 24 hours. " +
                        "If you did not create an account, you can safely ignore this email."
        );

        sendHtml(to, "Verify your email - " + BrandInfo.COMPANY_NAME, html);
    }

    @Async
    public void sendGuideWelcomeEmail(String to, String rawPassword) {
        String loginUrl = brand.getFrontendUrl() + "/login";

        String html = buildEmail(
                "Welcome Aboard!",
                "Your Guide Account is Ready",
                "Welcome to the " + BrandInfo.COMPANY_NAME + " family! " +
                        "Your account as a Guide has been created successfully." +
                        "<br><br>" +
                        "<div style='background:" + BrandColors.SECTION_BG_HEX + ";" +
                        "padding:16px;border-radius:10px;" +
                        "border:1px solid " + BrandColors.BORDER_HEX + ";'>" +
                        "<strong>Your credentials:</strong><br>" +
                        "Email: <code>" + to + "</code><br>" +
                        "Password: <code>" + rawPassword + "</code>" +
                        "</div>" +
                        "<br>For security reasons, please change your password after first login.",
                "Login Now",
                loginUrl,
                "If you have any questions, contact us at " + BrandInfo.EMAIL
        );

        sendHtml(to, "Welcome to " + BrandInfo.COMPANY_NAME + " - Guide Account", html);
    }

    @Async
    public void sendReservationConfirmation(String to, String touristName,
                                            String tourName, String ref) {
        String html = buildEmail(
                "Booking Confirmed!",
                "Hello " + touristName + "!",
                "Your reservation for <strong>" + tourName + "</strong> " +
                        "has been received successfully." +
                        "<br><br>" +
                        "<div style='background:" + BrandColors.SUCCESS_BG_HEX + ";" +
                        "padding:12px;border-radius:10px;" +
                        "border-left:4px solid " + BrandColors.SUCCESS_HEX + ";'>" +
                        "Reference: <strong>#" + ref + "</strong>" +
                        "</div>",
                "View Reservation",
                brand.getFrontendUrl() + "/reservations",
                "You will receive a confirmation email with your ticket once approved."
        );

        sendHtml(to, "Booking Confirmation - " + BrandInfo.COMPANY_NAME, html);
    }


    // ═══════════════════════════════════════════════════
    //  HTML EMAIL BUILDER
    // ═══════════════════════════════════════════════════

    private String buildEmail(String preheader, String title, String body,
                              String btnText, String btnUrl, String footer) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <link href="%s" rel="stylesheet">
                <style>
                    body {
                        margin: 0;
                        padding: 0;
                        background-color: %s;
                        font-family: %s;
                        color: %s;
                    }
                </style>
            </head>
            <body>
                <!-- Preheader -->
                <span style="display:none;font-size:1px;color:%s;">%s</span>
            
                <table width="100%%" cellpadding="0" cellspacing="0" style="padding:40px 20px;">
                <tr><td align="center">
                <table width="600" cellpadding="0" cellspacing="0"
                       style="background:%s;border-radius:16px;
                              border:1px solid %s;overflow:hidden;">
            
                    <!-- Header -->
                    <tr>
                        <td style="background:%s;padding:24px 32px;
                                   border-bottom:3px solid %s;">
                            <table width="100%%">
                            <tr>
                                <td width="70" style="vertical-align:middle; text-align:center;">
                                    <img src="cid:logo"
                                         alt="DesertAkal"
                                         width="70"\s
                                         height="60"
                                         style="display:block; border:0; border-radius:8px;\s
                                                width:70px; height:60px;\s
                                                object-fit:contain; margin:0 auto;">
                                </td>
                                <td>
                                    <span style="font-size:20px;font-weight:800;
                                                 color:%s;">Desert</span>
                                    <span style="font-size:20px;font-weight:800;
                                                 color:%s;">Akal</span>
                                    <br>
                                    <span style="font-size:12px;color:%s;">%s</span>
                                </td>
                            </tr>
                            </table>
                        </td>
                    </tr>
            
                    <!-- Body -->
                    <tr>
                        <td style="padding:40px 32px;">
                            <h1 style="margin:0 0 16px;font-size:24px;font-weight:800;
                                       color:%s;">%s</h1>
                            <p style="margin:0 0 24px;font-size:15px;line-height:1.6;
                                      color:%s;">%s</p>
            
                            <!-- Button -->
                            <table cellpadding="0" cellspacing="0" style="margin:24px 0;">
                            <tr>
                                <td style="background:%s;border-radius:10px;
                                           padding:14px 28px;">
                                    <a href="%s"
                                       style="color:white;text-decoration:none;
                                              font-weight:700;font-size:15px;">%s</a>
                                </td>
                            </tr>
                            </table>
            
                            <!-- Footer Text -->
                            <p style="margin:24px 0 0;font-size:13px;
                                      color:%s;">%s</p>
                        </td>
                    </tr>
            
                    <!-- Footer -->
                    <tr>
                        <td style="padding:20px 32px;border-top:1px solid %s;
                                   background:%s;">
                            <p style="margin:0;font-size:12px;color:%s;">
                                %s | %s<br>
                                <span style="color:%s;">%s</span>
                            </p>
                        </td>
                    </tr>
            
                </table>
                </td></tr>
                </table>
            </body>
            </html>
            """.formatted(
                BrandFonts.GOOGLE_FONTS_URL,
                BrandColors.BG_HEX,
                BrandFonts.FONT_FAMILY,
                BrandColors.TEXT_PRIMARY_HEX,
                BrandColors.BG_HEX, preheader,
                BrandColors.SURFACE_HEX,
                BrandColors.BORDER_HEX,
                BrandColors.SURFACE_HEX,
                BrandColors.PRIMARY_HEX,
                BrandColors.LOGO_BLUE_HEX,
                BrandColors.LOGO_GOLD_HEX,
                BrandColors.TEXT_TERTIARY_HEX,
                BrandInfo.TAGLINE,
                BrandColors.TEXT_PRIMARY_HEX, title,
                BrandColors.TEXT_SECONDARY_HEX, body,
                BrandColors.PRIMARY_HEX,
                btnUrl, btnText,
                BrandColors.TEXT_DISABLED_HEX, footer,
                BrandColors.BORDER_HEX,
                BrandColors.SECTION_BG_HEX,
                BrandColors.TEXT_SECONDARY_HEX,
                BrandInfo.EMAIL, BrandInfo.DOMAIN,
                BrandColors.TEXT_DISABLED_HEX,
                BrandInfo.DISCLAIMER
        );
    }

    private void sendHtml(String to, String subject, String html) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (brandConfig.isLogoLoaded()) {
                helper.addInline("logo",
                        new ByteArrayResource(brandConfig.getLogoPngBytes()),
                        "image/png");
            }

            javaMailSender.send(message);
            log.info("Email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}