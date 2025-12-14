package org.example;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Класс с логикой обработки команд и генерацией пароля
 */
public class PasswordLogic {

    /** Минимальная допустимая длина пароля */
    private static final int MIN_LENGTH = 6;

    /** Максимальная допустимая длина пароля */
    private static final int MAX_LENGTH = 64;

    /** Состояние: нет активного диалога */
    private static final int STATE_NONE = 0;

    /** Состояние: ожидание ввода длины пароля */
    private static final int STATE_WAIT_LENGTH = 1;

    /** Состояние: вопрос об использовании цифр */
    private static final int STATE_ASK_DIGITS = 2;

    /** Состояние: вопрос об использовании заглавных букв */
    private static final int STATE_ASK_UPPER = 3;

    /** Состояние: вопрос об использовании строчных букв */
    private static final int STATE_ASK_LOWER = 4;

    /** Состояние: вопрос об использовании специальных символов */
    private static final int STATE_ASK_SPECIAL = 5;

    /** Строка с цифрами для генерации пароля */
    private static final String DIGITS = "0123456789";

    /** Строка с заглавными буквами для генерации пароля */
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Строка со строчными буквами для генерации пароля */
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";

    /** Строка со специальными символами для генерации пароля */
    private static final String SPECIAL = "!@#$%^&*()_-+=<>?/{}[]";

    /**
     * Генератор случайных чисел для создания паролей
     */
    private final SecureRandom random = new SecureRandom();

    /** Словарь для хранения настроек пользователей по их ID */
    private final Map<Long, UserSettings> userSettings = new HashMap<>();

    /**
     * Внутренний класс для хранения настроек одного пользователя.
     * Содержит параметры генерации пароля и состояние диалога.
     */
    private class UserSettings {
        /** Длина генерируемого пароля */
        int length = 10;

        /** Флаг использования цифр в пароле (по умолчанию true) */
        boolean digits = true;

        /** Флаг использования заглавных букв в пароле (по умолчанию true) */
        boolean upper = true;

        /** Флаг использования строчных букв в пароле (по умолчанию true) */
        boolean lower = true;

        /** Флаг использования специальных символов в пароле (по умолчанию true) */
        boolean special = true;

        /** Текущее состояние диалога с пользователем */
        int state = STATE_NONE;
    }

    /**
     Основной метод обработки сообщений от пользователя.
     Определяет команду и направляет на соответствующую обработку.
     Возвращает ответное сообщение для пользователя
     */
    public String handleMessage(long chatId, String text) {
        text = text.trim();

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

    /**
     Обрабатывает шаг настройки пароля в диалоговом режиме.
     Определяет текущее состояние и вызывает соответствующий обработчик.
     Возвращает ответное сообщение
     */
    private String handleSettingsStep(long chatId, String text, UserSettings settings) {
        switch (settings.state) {
            case STATE_WAIT_LENGTH:
                return handleLengthInput(text, settings);

            case STATE_ASK_DIGITS:
            case STATE_ASK_UPPER:
            case STATE_ASK_LOWER:
            case STATE_ASK_SPECIAL:
                return handleYesNoInput(text, settings);

            default:
                settings.state = STATE_NONE;
                return null;
        }
    }

    /**
     Обрабатывает ввод длины пароля от пользователя.
     Устанавливает длину и переходит к следующему вопросу.
     Возвращает следующий вопрос для пользователя
     */
    private String handleLengthInput(String text, UserSettings settings) {
        settings.length = Integer.parseInt(text);
        settings.state = STATE_ASK_DIGITS;
        return "Использовать цифры? (+ / -)";
    }

    /**
     Обрабатывает ответы "да/нет" на вопросы о параметрах пароля.
     Обновляет соответствующий флаг и задает следующий вопрос.
     Возвращает следующий вопрос или сообщение об окончании настройки
     */
    private String handleYesNoInput(String text, UserSettings settings) {
        Boolean value = parseYesNo(text);

        switch (settings.state) {
            case STATE_ASK_DIGITS:
                settings.digits = value;
                settings.state = STATE_ASK_UPPER;
                return "Использовать прописные буквы? (+ / -)";

            case STATE_ASK_UPPER:
                settings.upper = value;
                settings.state = STATE_ASK_LOWER;
                return "Использовать строчные буквы? (+ / -)";

            case STATE_ASK_LOWER:
                settings.lower = value;
                settings.state = STATE_ASK_SPECIAL;
                return "Использовать специальные символы? (+ / -)";

            case STATE_ASK_SPECIAL:
                settings.special = value;

                settings.state = STATE_NONE;
                return "Новые параметры. Длина = " + settings.length + "; наличие цифр "
                        + settings.digits + "; наличие заглавных букв " + settings.upper + "; наличие строчных букв "
                        + settings.lower + "; наличие спецсимволов " + settings.special;

            default:
                settings.state = STATE_NONE;
                return null;
        }
    }

    /**
     Генерирует пароль на основе текущих настроек пользователя.
     */
    private String generatePassword(UserSettings settings) {

        StringBuilder alphabet = new StringBuilder();
        if (settings.digits) alphabet.append(DIGITS);
        if (settings.upper) alphabet.append(UPPER);
        if (settings.lower) alphabet.append(LOWER);
        if (settings.special) alphabet.append(SPECIAL);


        String chars = alphabet.toString();
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < settings.length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        return "Ваш пароль: " + password;
    }

    /**
     Получает настройки пользователя по его ID.
     Возвращает объект с настройками пользователя
     */
    private UserSettings getUserSettings(long chatId) {
        return userSettings.computeIfAbsent(chatId, k -> new UserSettings());
    }

    /**
     Преобразует текстовый ответ (+/-) в логическое значение.
     Возвращает true для "+", false для "-", null для других значений
     */
    private Boolean parseYesNo(String text) {
        if (text.equals("+")) {
            return true;
        } else if (text.equals("-")) {
            return false;
        }
        return null;
    }
}
