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
     * Проверяет, что команда /password создаёт пароль длиной 10 символов
     */
    @Test
    void testPasswordCommandGenerates10Chars() {
        String result = logic.handleMessage("/password");
        Assertions.assertEquals(10, result.length());
    }

    /**
     * Проверяет, что пароль содержит только разрешённые символы
     */
    @Test
    void testPasswordContainsOnlyAllowedCharacters() {
        String result = logic.handleMessage("/password");
        String allowed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_-+=<>?/{}~";

        for (char c : result.toCharArray()) {
            Assertions.assertTrue(allowed.indexOf(c) >= 0, "Недопустимый символ: " + c);
        }
    }

    /**
     * Проверяет, что пароли отличаются при каждом вызове
     */
    @Test
    void testPasswordsAreDifferentEachTime() {
        String p1 = logic.handleMessage("/password");
        String p2 = logic.handleMessage("/password");

        Assertions.assertNotEquals(p1, p2);
    }

    /**
     * Проверяет, что неправильная команда возвращает подсказку
     */
    @Test
    void testWrongCommandReturnsHelpMessage() {
        String result = logic.handleMessage("hello");
        Assertions.assertEquals("Используй команду /password для генерации пароля", result);
    }

    /**
     * Проверяет, что лишние пробелы вокруг команды не мешают её обработке
     */
    @Test
    void testSpacesTrimmed() {
        String result = logic.handleMessage("    /password   ");
        Assertions.assertEquals(10, result.length());
    }
}