package org.example;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

public class PasswordLogic {

    private static final int MIN_LENGTH = 6;
    private static final int MAX_LENGTH = 64;
    private static final int STATE_NONE = 0;
    private static final int STATE_WAIT_LENGTH = 1;
    private static final int STATE_ASK_DIGITS = 2;
    private static final int STATE_ASK_UPPER = 3;
    private static final int STATE_ASK_LOWER = 4;
    private static final int STATE_ASK_SPECIAL = 5;
    private static final String DIGITS = "0123456789";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String SPECIAL = "!@#$%^&*()_-+=<>?/{}[]";

    private final SecureRandom random = new SecureRandom();
    private final Map<Long, UserSettings> userSettings = new HashMap<>();

    private static class UserSettings {
        int state = STATE_NONE;
    }

    public String handleMessage(long chatId, String text) {
        UserSettings settings = getUserSettings(chatId);
        int state = settings.state;

        if (state != STATE_NONE && !text.equals("/settings") && !text.equals("/password")) {
            return handleSettingsStep(chatId, text, settings);
        }

        switch (text) {
            case "/settings":
                settings.state = STATE_WAIT_LENGTH;
                return "Введите длину пароля (" + MIN_LENGTH + "–" + MAX_LENGTH + "):";

            case "/password":
                return generatePassword(settings);

            default:
                return "Неизвестная команда. Используйте /settings или /password";
        }
    }

    private String handleSettingsStep(long chatId, String text, UserSettings settings) {
        return null;
    }

    private String generatePassword(UserSettings settings) {
        return null;
    }

    private UserSettings getUserSettings(long chatId) {
        return userSettings.computeIfAbsent(chatId, k -> new UserSettings());
    }

}