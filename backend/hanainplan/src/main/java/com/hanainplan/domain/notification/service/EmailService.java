package com.hanainplan.domain.notification.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@hanainplan.com}")
    private String fromEmail;

    @Value("${app.name:하나인플랜}")
    private String appName;

    public boolean sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Simple email sent to: {}", to);
            return true;
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            return false;
        }
    }

    public boolean sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent to: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            return false;
        }
    }

    public boolean sendConsultationAcceptedEmail(
            String customerEmail,
            String customerName,
            String consultantName,
            String consultationType,
            String roomId,
            String consultationUrl
    ) {
        String subject = "[" + appName + "] 상담이 수락되었습니다";

        String htmlContent = buildConsultationAcceptedEmailHtml(
                customerName,
                consultantName,
                consultationType,
                roomId,
                consultationUrl
        );

        return sendHtmlEmail(customerEmail, subject, htmlContent);
    }

    private String buildConsultationAcceptedEmailHtml(
            String customerName,
            String consultantName,
            String consultationType,
            String roomId,
            String consultationUrl
    ) {
        return """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>상담 수락 알림</title>
                    <style>
                        body {
                            font-family: 'Malgun Gothic', '맑은 고딕', sans-serif;
                            line-height: 1.6;
                            color: #333;
                            max-width: 600px;
                            margin: 0 auto;
                            padding: 20px;
                        }
                        .header {
                            background: linear-gradient(135deg, #008485 0%, #00A091 100%);
                            color: white;
                            padding: 30px;
                            text-align: center;
                            border-radius: 10px 10px 0 0;
                        }
                        .header h1 {
                            margin: 0;
                            font-size: 24px;
                        }
                        .content {
                            background: #f9f9f9;
                            padding: 30px;
                            border: 1px solid #ddd;
                            border-top: none;
                        }
                        .info-box {
                            background: white;
                            padding: 20px;
                            margin: 20px 0;
                            border-left: 4px solid #008485;
                            border-radius: 5px;
                        }
                        .info-row {
                            margin: 10px 0;
                        }
                        .label {
                            font-weight: bold;
                            color: #008485;
                            display: inline-block;
                            width: 120px;
                        }
                        .value {
                            color: #333;
                        }
                        .button {
                            display: inline-block;
                            background: #008485;
                            color: white !important;
                            padding: 15px 40px;
                            text-decoration: none;
                            border-radius: 5px;
                            margin: 20px 0;
                            font-weight: bold;
                            text-align: center;
                        }
                        .button:hover {
                            background: #00A091;
                        }
                        .footer {
                            text-align: center;
                            padding: 20px;
                            color: #666;
                            font-size: 12px;
                            border-top: 1px solid #ddd;
                        }
                        .warning {
                            background: #fff3cd;
                            border: 1px solid #ffc107;
                            padding: 15px;
                            border-radius: 5px;
                            margin: 20px 0;
                        }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>🎉 상담이 수락되었습니다!</h1>
                    </div>
                    <div class="content">
                        <p><strong>%s</strong>님, 안녕하세요!</p>
                        <p>요청하신 상담이 수락되었습니다. 상담원과의 상담이 곧 시작됩니다.</p>

                        <div class="info-box">
                            <div class="info-row">
                                <span class="label">상담 유형:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">담당 상담원:</span>
                                <span class="value">%s</span>
                            </div>
                            <div class="info-row">
                                <span class="label">상담실 ID:</span>
                                <span class="value">%s</span>
                            </div>
                        </div>

                        <div style="text-align: center;">
                            <a href="%s" class="button">상담 참여하기</a>
                        </div>

                        <div class="warning">
                            <strong>⚠️ 안내사항</strong>
                            <ul style="margin: 10px 0; padding-left: 20px;">
                                <li>상담 시작 전 카메라와 마이크 권한을 허용해주세요.</li>
                                <li>안정적인 인터넷 연결 환경에서 이용하시기 바랍니다.</li>
                                <li>상담 중 개인정보 보호를 위해 화면 캡처가 제한될 수 있습니다.</li>
                            </ul>
                        </div>

                        <p style="color: #666; font-size: 14px;">
                            상담 시작 시간이 지연될 경우 상담원이 별도로 연락드릴 예정입니다.<br>
                            문의사항이 있으시면 고객센터(1588-1111)로 연락주시기 바랍니다.
                        </p>
                    </div>
                    <div class="footer">
                        <p>본 메일은 발신 전용입니다. 문의사항은 고객센터를 이용해주세요.</p>
                        <p>© 2025 %s. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(
                customerName,
                consultationType,
                consultantName,
                roomId,
                consultationUrl,
                appName
        );
    }

    public boolean sendConsultationCompletedEmail(
            String customerEmail,
            String customerName,
            String consultationType,
            int durationMinutes
    ) {
        String subject = "[" + appName + "] 상담이 완료되었습니다";

        String htmlContent = """
                <!DOCTYPE html>
                <html lang="ko">
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: 'Malgun Gothic', sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; }
                        .header { background: #008485; color: white; padding: 30px; text-align: center; border-radius: 10px 10px 0 0; }
                        .content { background: #f9f9f9; padding: 30px; border: 1px solid #ddd; }
                        .info-box { background: white; padding: 20px; margin: 20px 0; border-left: 4px solid #008485; }
                        .footer { text-align: center; padding: 20px; color: #666; font-size: 12px; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>✅ 상담이 완료되었습니다</h1>
                    </div>
                    <div class="content">
                        <p><strong>%s</strong>님, 상담을 이용해주셔서 감사합니다.</p>
                        <div class="info-box">
                            <p><strong>상담 유형:</strong> %s</p>
                            <p><strong>상담 시간:</strong> %d분</p>
                        </div>
                        <p>앞으로도 %s를 이용해주시기 바랍니다.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 %s. All rights reserved.</p>
                    </div>
                </body>
                </html>
                """.formatted(customerName, consultationType, durationMinutes, appName, appName);

        return sendHtmlEmail(customerEmail, subject, htmlContent);
    }
}