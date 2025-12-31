package com.crm.notification.service;

public interface SmsService {
    void sendSms(String toPhoneNumber, String messageBody);

    boolean verifyOtp(String toPhoneNumber, String otpCode);
}
