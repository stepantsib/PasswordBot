package org.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты логики бота
 */
public class PasswordLogicTest {

    /**
     * Экземпляр класса с логикой генерации паролей и обработки команды /password
     */
    private final PasswordLogic logic = new PasswordLogic();

    /**
     * Проверяет, что команда /password создаёт пароль с правильным форматом
     */
    @Test
    void testPasswordCommandGeneratesPasswordWithPrefix() {
        String result = logic.handleMessage(12345, "/password");
        // Пароль возвращается с префиксом "Ваш пароль: "
        Assertions.assertTrue(result.startsWith("Ваш пароль: "));
        // Сам пароль должен быть длиной 10 символов (по умолчанию)
        String password = result.substring("Ваш пароль: ".length());
        Assertions.assertEquals(10, password.length());
    }

    /**
     * Проверяет, что пароль содержит только разрешённые символы (при настройках по умолчанию)
     */
    @Test
    void testPasswordContainsOnlyAllowedCharacters() {
        String result = logic.handleMessage(12345, "/password");
        String password = result.substring("Ваш пароль: ".length());
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_-+=<>?/{}[]";

        for (char c : password.toCharArray()) {
            Assertions.assertTrue(allowed.indexOf(c) >= 0);
        }
    }

    /**
     * Проверяет, что пароли отличаются при каждом вызове
     */
    @Test
    void testPasswordsAreDifferentEachTime() {
        String p1 = logic.handleMessage(12345, "/password");
        String p2 = logic.handleMessage(12345, "/password");

        Assertions.assertNotEquals(p1, p2);
    }

    /**
     * Проверяет, что неправильная команда возвращает подсказку
     */
    @Test
    void testWrongCommandReturnsHelpMessage() {
        String result = logic.handleMessage(12345, "hello");
        Assertions.assertEquals("Неизвестная команда. Используйте /settings или /password", result);
    }

    /**
     * Проверяет, что разные пользователи получают свои настройки
     */
    @Test
    void testDifferentUsersHaveSeparateSettings() {
        // Настраиваем первого пользователя
        logic.handleMessage(11111, "/settings");
        logic.handleMessage(11111, "12"); // Длина 12
        logic.handleMessage(11111, "+"); // Цифры да
        logic.handleMessage(11111, "-"); // Прописные нет
        logic.handleMessage(11111, "+"); // Строчные да
        logic.handleMessage(11111, "-"); // Специальные нет

        // Генерируем пароль для первого пользователя
        String result1 = logic.handleMessage(11111, "/password");
        String password1 = result1.substring("Ваш пароль: ".length());

        // Проверяем, что пароль длиной 12 символов
        Assertions.assertEquals(12, password1.length());

        // Проверяем, что нет заглавных букв и специальных символов
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String special = "!@#$%^&*()_-+=<>?/{}[]";
        for (char c : password1.toCharArray()) {
            Assertions.assertFalse(upper.indexOf(c) >= 0);
            Assertions.assertFalse(special.indexOf(c) >= 0);
        }

        // Второй пользователь получает настройки по умолчанию
        String result2 = logic.handleMessage(22222, "/password");
        String password2 = result2.substring("Ваш пароль: ".length());
        Assertions.assertEquals(10, password2.length());

        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_-+=<>?/{}[]";

        for (char c : password2.toCharArray()) {
            Assertions.assertTrue(allowed.indexOf(c) >= 0);
        }
    }

    /**
     * Проверяет полный процесс настройки
     */
    @Test
    void testFullConfigurationProcess() {
        // Начинаем настройку
        String step0 = logic.handleMessage(44444, "/settings");

        Assertions.assertEquals("Введите длину пароля (6–64):", step0);
        // Устанавливаем длину
        String step1 = logic.handleMessage(44444, "8");
        Assertions.assertEquals("Использовать цифры? (+ / -)", step1);

        // Отключаем цифры
        String step2 = logic.handleMessage(44444, "-");
        Assertions.assertEquals("Использовать прописные буквы? (+ / -)", step2);

        // Включаем прописные
        String step3 = logic.handleMessage(44444, "+");
        Assertions.assertEquals("Использовать строчные буквы? (+ / -)", step3);

        // Включаем строчные
        String step4 = logic.handleMessage(44444, "+");
        Assertions.assertEquals("Использовать специальные символы? (+ / -)", step4);

        // Отключаем специальные
        String step5 = logic.handleMessage(44444, "-");
        Assertions.assertEquals("Новые параметры. Длина = 8; наличие цифр false; наличие заглавных букв true; наличие строчных букв true; наличие спецсимволов false", step5);

        // Генерируем пароль с новыми настройками
        String result = logic.handleMessage(44444, "/password");
        String password = result.substring("Ваш пароль: ".length());

        Assertions.assertEquals(8, password.length());

        // Проверяем, что нет цифр и специальных символов
        String digits = "0123456789";
        String special = "!@#$%^&*()_-+=<>?/{}[]";
        for (char c : password.toCharArray()) {
            Assertions.assertFalse(digits.indexOf(c) >= 0);
            Assertions.assertFalse(special.indexOf(c) >= 0);
        }
    }

    /**
     * Проверяет, что можно переключаться между командами без потери состояния
     */
    @Test
    void testCommandSwitching() {
        // Начинаем настройку
        logic.handleMessage(55555, "/settings");
        logic.handleMessage(55555, "15");

        // Прерываем настройку командой /password
        String password1 = logic.handleMessage(55555, "/password");
        // Пароль должен быть с длиной 15 (так как мы уже ввели ее в настройках)
        String pass1 = password1.substring("Ваш пароль: ".length());
        Assertions.assertEquals(15, pass1.length()); // Изменено с 10 на 15

        // Снова начинаем настройку - должно начаться с начала
        String start = logic.handleMessage(55555, "/settings");
        Assertions.assertEquals("Введите длину пароля (6–64):", start);

        // Проверяем, что можем продолжить настройку
        logic.handleMessage(55555, "12");
        String step1 = logic.handleMessage(55555, "+");
        Assertions.assertEquals("Использовать прописные буквы? (+ / -)", step1);
    }

    /**
     * Проверяет генерацию пароля с минимальной длиной
     */
    @Test
    void testMinimumLengthPassword() {
        logic.handleMessage(77777, "/settings");
        logic.handleMessage(77777, "6"); // Минимальная длина
        logic.handleMessage(77777, "+");
        logic.handleMessage(77777, "+");
        logic.handleMessage(77777, "+");
        logic.handleMessage(77777, "+");

        String result = logic.handleMessage(77777, "/password");
        String password = result.substring("Ваш пароль: ".length());
        Assertions.assertEquals(6, password.length());
    }

    /**
     * Проверяет, что настройки сохраняются между вызовами
     */
    @Test
    void testSettingsPersistBetweenCalls() {
        // Настраиваем пользователя
        logic.handleMessage(88888, "/settings");
        logic.handleMessage(88888, "20");
        logic.handleMessage(88888, "+");
        logic.handleMessage(88888, "+");
        logic.handleMessage(88888, "+");
        logic.handleMessage(88888, "+");

        // Генерируем несколько паролей
        String result1 = logic.handleMessage(88888, "/password");
        String password1 = result1.substring("Ваш пароль: ".length());

        String result2 = logic.handleMessage(88888, "/password");
        String password2 = result2.substring("Ваш пароль: ".length());

        // Оба пароля должны быть длиной 20 символов
        Assertions.assertEquals(20, password1.length());
        Assertions.assertEquals(20, password2.length());
        Assertions.assertNotEquals(password1, password2);
    }

    /**
     * Проверяет, что лишние пробелы вокруг команды не мешают её обработке
     */
    @Test
    void testSpacesTrimmed() {
        String result = logic.handleMessage(111,"    /password   ");
        // Проверяем, что команда с пробелами работает корректно
        Assertions.assertTrue(result.startsWith("Ваш пароль: "));

        // Проверяем длину самого пароля (должна быть 10)
        String password = result.substring("Ваш пароль: ".length());
        Assertions.assertEquals(10, password.length());
    }
}
