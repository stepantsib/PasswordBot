package org.example;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Класс с логикой обработки команд тг бота.
 * Реализует:
 * настройку генерации паролей через диалог (/settings)
 * генерацию паролей (/password)
 * менеджер паролей пользователя (/add, /list, /get, /delete, /change)
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

    /** Состояние: нет активного диалога менеджера паролей */
    private static final int PM_NONE = 0;

    /** Состояние: ожидание ввода названия сервиса при добавлении */
    private static final int PM_ADD_WAIT_SERVICE = 1;

    /** Состояние: ожидание ввода логина/email при добавлении */
    private static final int PM_ADD_WAIT_LOGIN = 2;

    /** Состояние: выбор способа создания пароля при добавлении */
    private static final int PM_ADD_WAIT_METHOD = 3;

    /** Состояние: ожидание ручного ввода пароля при добавлении */
    private static final int PM_ADD_WAIT_PASSWORD = 4;

    /** Состояние: подтверждение удаления записи */
    private static final int PM_DELETE_CONFIRM = 5;

    /** Состояние: выбор способа изменения пароля */
    private static final int PM_CHANGE_WAIT_METHOD = 6;

    /** Состояние: ожидание ручного ввода нового пароля */
    private static final int PM_CHANGE_WAIT_PASSWORD = 7;

    /** Строка с цифрами для генерации пароля */
    private static final String DIGITS = "0123456789";

    /** Строка с заглавными буквами для генерации пароля */
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    /** Строка со строчными буквами для генерации пароля */
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";

    /** Строка со специальными символами для генерации пароля */
    private static final String SPECIAL = "!@#$%^&*()_-+=<>?/{}[]";

    /** Генератор случайных чисел */
    private final SecureRandom random = new SecureRandom();

    /** Настройки пользователей и состояния диалогов */
    private final Map<Long, UserSettings> userSettings = new HashMap<>();

    /** База данных для хранения сервисов, логинов и паролей */
    private final PasswordDatabase database = new PasswordDatabase();


    /**
     * Настройки одного пользователя и состояния диалогов.
     */
    private class UserSettings {
        int length = 10;
        boolean digits = true;
        boolean upper = true;
        boolean lower = true;
        boolean special = true;

        int state = STATE_NONE;
        int pmState = PM_NONE;

        String tmpService;
        String tmpLogin;
    }

    /**
     * Основной метод обработки сообщений пользователя.
     */
    public String handleMessage(long chatId, String text) {
        text = text.trim();
        UserSettings settings = getUserSettings(chatId);

        if (settings.pmState != PM_NONE && !text.equals("/settings") && !text.equals("/password")) {
            return handleManagerStep(chatId, text, settings);
        }

        if (settings.state != STATE_NONE && !text.equals("/settings") && !text.equals("/password")) {
            return handleSettingsStep(text, settings);
        }

        String cmd = first(text);
        String arg = second(text);

        switch (cmd) {
            case "/start":
                return """
                        Команды:
                        /settings — настройки генерации
                        /password — сгенерировать пароль

                        /add — добавить запись
                        /list — список сервисов
                        /get <сервис> — логин и пароль
                        /delete <сервис> — удалить (+/-)
                        /change <сервис> — изменить пароль
                        """;

            case "/settings":
                settings.pmState = PM_NONE;
                settings.state = STATE_WAIT_LENGTH;
                return "Введите длину пароля (" + MIN_LENGTH + "–" + MAX_LENGTH + "):";

            case "/password":
                settings.pmState = PM_NONE;
                return "Ваш пароль: " + generatePasswordRaw(settings);

            case "/add":
                settings.pmState = PM_ADD_WAIT_SERVICE;
                return "Введите название сервиса:";

            case "/list":
                return listServices(chatId);

            case "/get":
                return handleGet(chatId, arg);

            case "/delete":
                return handleDelete(chatId, arg, settings);

            case "/change":
                return handleChange(chatId, arg, settings);

            default:
                return "Неизвестная команда. Напишите /start";
        }
    }

    /**
     * Обрабатывает шаги диалога менеджера паролей.
     */
    private String handleManagerStep(long chatId, String text, UserSettings settings) {
        switch (settings.pmState) {
            case PM_ADD_WAIT_SERVICE:
                settings.tmpService = text;
                settings.pmState = PM_ADD_WAIT_LOGIN;
                return "Введите логин/email:";

            case PM_ADD_WAIT_LOGIN:
                settings.tmpLogin = text;
                settings.pmState = PM_ADD_WAIT_METHOD;
                return """
                        Выберите способ создания пароля:
                        1. Автоматическая генерация
                        2. Ввод вручную
                        """;

            case PM_ADD_WAIT_METHOD:
                if (text.equals("1")) {
                    String pass = generatePasswordRaw(settings);
                    database.save(chatId, settings.tmpService, settings.tmpLogin, pass);
                    String service = settings.tmpService;
                    resetManager(settings);
                    return "Пароль для " + service + ": " + pass + "\nДанные сохранены";
                }
                if (text.equals("2")) {
                    settings.pmState = PM_ADD_WAIT_PASSWORD;
                    return "Введите пароль:";
                }
                return "Введите 1 или 2";

            case PM_ADD_WAIT_PASSWORD:
                database.save(chatId, settings.tmpService, settings.tmpLogin, text);
                resetManager(settings);
                return "Данные сохранены";

            case PM_DELETE_CONFIRM: {
                Boolean ok = parseYesNo(text);
                if (ok == null) return "Ответьте + или -";

                if (ok) {
                    String serviceToDelete = settings.tmpService;
                    database.delete(chatId, serviceToDelete);
                    resetManager(settings);
                    return "Данные для \"" + serviceToDelete + "\" удалены";
                }

                resetManager(settings);
                return "Удаление отменено";
            }

            case PM_CHANGE_WAIT_METHOD:
                if (text.equals("1")) {
                    PasswordDatabase.Entry e = database.find(chatId, settings.tmpService);
                    if (e == null) {
                        resetManager(settings);
                        return "Сервис \"" + settings.tmpService + "\" не найден.\nИспользуйте /list.";
                    }
                    String newPass = generatePasswordRaw(settings);
                    database.save(chatId, e.getService(), e.getLogin(), newPass);
                    String service = e.getService();
                    resetManager(settings);
                    return "Новый пароль для " + service + ": " + newPass + "\nПароль изменён";
                }
                if (text.equals("2")) {
                    settings.pmState = PM_CHANGE_WAIT_PASSWORD;
                    return "Введите новый пароль:";
                }
                return "Введите 1 или 2";

            case PM_CHANGE_WAIT_PASSWORD: {
                PasswordDatabase.Entry e = database.find(chatId, settings.tmpService);
                if (e == null) {
                    resetManager(settings);
                    return "Сервис \"" + settings.tmpService + "\" не найден.\nИспользуйте /list.";
                }
                database.save(chatId, e.getService(), e.getLogin(), text);
                resetManager(settings);
                return "Пароль для " + e.getService() + " изменён";
            }

            default:
                resetManager(settings);
                return null;
        }
    }

    /**
     * Обрабатывает шаг настройки генерации пароля.
     */
    private String handleSettingsStep(String text, UserSettings settings) {
        Boolean v;

        switch (settings.state) {
            case STATE_WAIT_LENGTH:
                settings.length = Integer.parseInt(text);
                settings.state = STATE_ASK_DIGITS;
                return "Использовать цифры? (+ / -)";

            case STATE_ASK_DIGITS:
                v = parseYesNo(text);
                if (v == null) return "Ответьте + или -";
                settings.digits = v;
                settings.state = STATE_ASK_UPPER;
                return "Использовать заглавные буквы? (+ / -)";

            case STATE_ASK_UPPER:
                v = parseYesNo(text);
                if (v == null) return "Ответьте + или -";
                settings.upper = v;
                settings.state = STATE_ASK_LOWER;
                return "Использовать строчные буквы? (+ / -)";

            case STATE_ASK_LOWER:
                v = parseYesNo(text);
                if (v == null) return "Ответьте + или -";
                settings.lower = v;
                settings.state = STATE_ASK_SPECIAL;
                return "Использовать специальные символы? (+ / -)";

            case STATE_ASK_SPECIAL:
                v = parseYesNo(text);
                if (v == null) return "Ответьте + или -";
                settings.special = v;
                settings.state = STATE_NONE;
                return "Новые параметры. Длина = " + settings.length
                        + "; наличие цифр " + settings.digits
                        + "; наличие заглавных букв " + settings.upper
                        + "; наличие строчных букв " + settings.lower
                        + "; наличие спецсимволов " + settings.special;

            default:
                settings.state = STATE_NONE;
                return null;
        }
    }

    /** Команда /get — показать логин и пароль сервиса. */
    private String handleGet(long chatId, String service) {
        if (service == null) return "Использование: /get <сервис>";
        PasswordDatabase.Entry e = database.find(chatId, service);
        if (e == null) return "Сервис не найден";
        return e.getService() + ":\nЛогин: " + e.getLogin() + "\nПароль: " + e.getPassword();
    }

    /** Команда /delete — запрос подтверждения удаления. */
    private String handleDelete(long chatId, String service, UserSettings settings) {
        if (service == null) return "Использование: /delete <сервис>";
        PasswordDatabase.Entry e = database.find(chatId, service);
        if (e == null) return "Сервис не найден";
        settings.tmpService = e.getService();
        settings.pmState = PM_DELETE_CONFIRM;
        return "Удалить данные для \"" + e.getService() + "\"? (+ / -)";
    }

    /** Команда /change — запуск процесса изменения пароля. */
    private String handleChange(long chatId, String service, UserSettings settings) {
        if (service == null) return "Использование: /change <сервис>";

        PasswordDatabase.Entry e = database.find(chatId, service);
        if (e == null) {
            return "Сервис \"" + service + "\" не найден.\nИспользуйте /list.";
        }

        settings.tmpService = e.getService();
        settings.pmState = PM_CHANGE_WAIT_METHOD;

        return "Текущий логин для " + e.getService() + ": " + e.getLogin() + "\n"
                + "Выберите способ создания нового пароля:\n"
                + "1. Автоматическая генерация\n"
                + "2. Ввод вручную";
    }

    /** Генерация пароля по текущим настройкам. */
    private String generatePasswordRaw(UserSettings settings) {
        StringBuilder alphabet = new StringBuilder();
        if (settings.digits) alphabet.append(DIGITS);
        if (settings.upper) alphabet.append(UPPER);
        if (settings.lower) alphabet.append(LOWER);
        if (settings.special) alphabet.append(SPECIAL);

        String chars = alphabet.toString();
        StringBuilder p = new StringBuilder();
        for (int i = 0; i < settings.length; i++) {
            p.append(chars.charAt(random.nextInt(chars.length())));
        }
        return p.toString();
    }

    /** Список сервисов пользователя. */
    private String listServices(long chatId) {
        List<String> list = database.listServices(chatId);
        if (list.isEmpty()) return "У вас пока нет сервисов";

        StringBuilder sb = new StringBuilder("Ваши сервисы (всего: " + list.size() + "):\n");
        for (int i = 0; i < list.size(); i++) {
            sb.append(i + 1).append(". ").append(list.get(i)).append("\n");
        }
        return sb.toString().trim();
    }

    /** Сброс состояния менеджера паролей. */
    private void resetManager(UserSettings settings) {
        settings.pmState = PM_NONE;
        settings.tmpService = null;
        settings.tmpLogin = null;
    }

    /** Получение настроек пользователя. */
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
    /** Возвращает команду. */
    private String first(String t) {
        int i = t.indexOf(' ');
        return i == -1 ? t : t.substring(0, i);
    }

    /** Возвращает аргумент команды. */
    private String second(String t) {
        int i = t.indexOf(' ');
        if (i == -1) return null;
        String s = t.substring(i + 1).trim();
        return s.isEmpty() ? null : s;
    }
}
