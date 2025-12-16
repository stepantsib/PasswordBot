package org.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

/**
 * Тесты логики бота
 */
public class PasswordLogicTest {

    /**
     * Экземпляр класса с логикой генерации паролей и обработки команды /password
     */
    private PasswordLogic logic;

    /**
     * Инициализирует новый экземпляр PasswordLogic перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        logic = new PasswordLogic();
    }

    /**
     * Проверяет, что команда /password создаёт пароль с правильным форматом
     */
    @Test
    void testPasswordCommandGeneratesPassword() {
        // 1. Сначала сбрасываем настройки для пользователя
        logic.handleMessage(12345, "/settings");
        logic.handleMessage(12345, "10"); // длина 10
        logic.handleMessage(12345, "+"); // цифры
        logic.handleMessage(12345, "+"); // заглавные
        logic.handleMessage(12345, "+"); // строчные
        logic.handleMessage(12345, "+"); // спецсимволы

        // 2. Теперь генерируем пароль
        String result = logic.handleMessage(12345, "/password");

        // 3. Проверяем
        Assertions.assertTrue(result.startsWith("Ваш пароль: "));
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
        Assertions.assertEquals("Неизвестная команда. Напишите /start", result);
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

    /**
     * Проверяет команду /start
     */
    @Test
    void testStartCommand() {
        String result = logic.handleMessage(33333, "/start");
        String expected = """
                        Команды:
                        /settings — настройки генерации
                        /password — сгенерировать пароль

                        /add — добавить запись
                        /list — список сервисов
                        /get <сервис> — логин и пароль
                        /delete <сервис> — удалить (+/-)
                        /change <сервис> — изменить пароль
                        """;
        Assertions.assertEquals(expected, result);
    }

    /**
     * Проверяет добавление записи с автоматической генерацией
     */
    @Test
    void testAddEntryAutoGenerated() {
        logic.handleMessage(99999, "/settings");
        logic.handleMessage(99999, "12");
        logic.handleMessage(99999, "+");
        logic.handleMessage(99999, "+");
        logic.handleMessage(99999, "+");
        logic.handleMessage(99999, "+");

        logic.handleMessage(99999, "/add");
        logic.handleMessage(99999, "TestService");
        logic.handleMessage(99999, "test@email.com");
        String result = logic.handleMessage(99999, "1");

        String[] resultLines = result.split("\n");
        Assertions.assertTrue(resultLines[0].startsWith("Пароль для TestService: "));
        Assertions.assertEquals("Данные сохранены", resultLines[1]);
    }

    /**
     * Проверяет добавление записи с ручным вводом
     */
    @Test
    void testAddEntryManual() {
        logic.handleMessage(88889, "/add");
        logic.handleMessage(88889, "ManualService");
        logic.handleMessage(88889, "user123");
        logic.handleMessage(88889, "2");
        String result = logic.handleMessage(88889, "MyPassword123!");

        Assertions.assertEquals("Данные сохранены", result);
    }

    /**
     * Проверяет список сервисов при отсутствии записей
     */
    @Test
    void testListEmpty() {
        String result = logic.handleMessage(55555, "/list");
        Assertions.assertEquals("У вас пока нет сервисов", result);
    }

    /**
     * Проверяет список сервисов с несколькими записями
     */
    @Test
    void testListWithMultipleEntries() {
        logic.handleMessage(66666, "/add");
        logic.handleMessage(66666, "Service1");
        logic.handleMessage(66666, "login1");
        logic.handleMessage(66666, "1");

        logic.handleMessage(66666, "/add");
        logic.handleMessage(66666, "Service2");
        logic.handleMessage(66666, "login2");
        logic.handleMessage(66666, "2");
        logic.handleMessage(66666, "pass2");

        String result = logic.handleMessage(66666, "/list");
        String[] lines = result.split("\n");
        Assertions.assertEquals("Ваши сервисы (всего: 2):", lines[0]);
        Assertions.assertEquals("1. Service1", lines[1]);
        Assertions.assertEquals("2. Service2", lines[2]);
    }

    /**
     * Проверяет получение несуществующего сервиса
     */
    @Test
    void testGetNonExistentService() {
        String result = logic.handleMessage(11112, "/get NonExistent");
        Assertions.assertEquals("Сервис не найден", result);
    }

    /**
     * Проверяет получение существующего сервиса
     */
    @Test
    void testGetExistingService() {
        logic.handleMessage(99991, "/add");
        logic.handleMessage(99991, "MyService");
        logic.handleMessage(99991, "myuser");
        logic.handleMessage(99991, "2");
        logic.handleMessage(99991, "mypass");

        String result = logic.handleMessage(99991, "/get MyService");
        String[] lines = result.split("\n");
        Assertions.assertEquals("MyService:", lines[0]);
        Assertions.assertEquals("Логин: myuser", lines[1]);
        Assertions.assertEquals("Пароль: mypass", lines[2]);
    }

    /**
     * Проверяет отмену удаления
     */
    @Test
    void testDeleteCancel() {
        logic.handleMessage(88890, "/add");
        logic.handleMessage(88890, "ToKeep");
        logic.handleMessage(88890, "login");
        logic.handleMessage(88890, "1");

        logic.handleMessage(88890, "/delete ToKeep");
        String result = logic.handleMessage(88890, "-");

        Assertions.assertEquals("Удаление отменено", result);
    }

    /**
     * Проверяет удаление несуществующего сервиса
     */
    @Test
    void testDeleteNonExistentService() {
        String result = logic.handleMessage(11113, "/delete NonExistent");
        Assertions.assertEquals("Сервис не найден", result);
    }

    /**
     * Проверяет изменение пароля автоматической генерацией
     */
    @Test
    void testChangePasswordAuto() {
        logic.handleMessage(99991, "/add");
        logic.handleMessage(99991, "ChangeService");
        logic.handleMessage(99991, "oldlogin");
        logic.handleMessage(99991, "2");
        logic.handleMessage(99991, "oldpassword");

        String changePrompt = logic.handleMessage(99991, "/change ChangeService");
        String[] promptLines = changePrompt.split("\n");
        Assertions.assertEquals("Текущий логин для ChangeService: oldlogin", promptLines[0]);
        Assertions.assertEquals("Выберите способ создания нового пароля:", promptLines[1]);
        Assertions.assertEquals("1. Автоматическая генерация", promptLines[2]);
        Assertions.assertEquals("2. Ввод вручную", promptLines[3]);

        String result = logic.handleMessage(99991, "1");
        String[] resultLines = result.split("\n");
        Assertions.assertTrue(resultLines[0].startsWith("Новый пароль для ChangeService: "));
        Assertions.assertEquals("Пароль изменён", resultLines[1]);
    }

    /**
     * Проверяет изменение пароля ручным вводом
     */
    @Test
    void testChangePasswordManual() {
        logic.handleMessage(99992, "/add");
        logic.handleMessage(99992, "ChangeManual");
        logic.handleMessage(99992, "login");
        logic.handleMessage(99992, "2");
        logic.handleMessage(99992, "oldpass");

        logic.handleMessage(99992, "/change ChangeManual");
        logic.handleMessage(99992, "2");

        String result = logic.handleMessage(99992, "newpassword123");
        Assertions.assertEquals("Пароль для ChangeManual изменён", result);
    }

    /**
     * Проверяет изменение несуществующего сервиса
     */
    @Test
    void testChangeNonExistentService() {
        String result = logic.handleMessage(11114, "/change NonExistent");
        String[] lines = result.split("\n");
        Assertions.assertEquals("Сервис \"NonExistent\" не найден.", lines[0]);
        Assertions.assertEquals("Используйте /list.", lines[1]);
    }

    /**
     * Проверяет неверный ответ в диалоге настроек
     */
    @Test
    void testInvalidYesNoResponse() {
        logic.handleMessage(11116, "/settings");
        logic.handleMessage(11116, "10");

        String result = logic.handleMessage(11116, "да");
        Assertions.assertEquals("Ответьте + или -", result);

        result = logic.handleMessage(11116, "+");
        Assertions.assertEquals("Использовать заглавные буквы? (+ / -)", result);
    }

    /**
     * Проверяет неверный выбор метода
     */
    @Test
    void testInvalidMethodChoice() {
        logic.handleMessage(11117, "/add");
        logic.handleMessage(11117, "TestService");
        logic.handleMessage(11117, "test@test.com");

        String result = logic.handleMessage(11117, "3");
        Assertions.assertEquals("Введите 1 или 2", result);
    }

    /**
     * Проверяет прерывание добавления командой /settings
     */
    @Test
    void testInterruptAddWithSettings() {
        logic.handleMessage(22224, "/add");
        logic.handleMessage(22224, "Service");

        String result = logic.handleMessage(22224, "/settings");
        Assertions.assertEquals("Введите длину пароля (6–64):", result);

        result = logic.handleMessage(22224, "15");
        Assertions.assertEquals("Использовать цифры? (+ / -)", result);
    }

    /**
     * Проверяет диалог настроек полностью
     */
    @Test
    void testFullSettingsDialog() {
        logic.handleMessage(33334, "/settings");

        String result = logic.handleMessage(33334, "15");
        Assertions.assertEquals("Использовать цифры? (+ / -)", result);

        result = logic.handleMessage(33334, "+");
        Assertions.assertEquals("Использовать заглавные буквы? (+ / -)", result);

        result = logic.handleMessage(33334, "-");
        Assertions.assertEquals("Использовать строчные буквы? (+ / -)", result);

        result = logic.handleMessage(33334, "+");
        Assertions.assertEquals("Использовать специальные символы? (+ / -)", result);

        result = logic.handleMessage(33334, "-");
        String[] lines = result.split("; ");
        Assertions.assertEquals("Новые параметры. Длина = 15", lines[0]);
        Assertions.assertEquals("наличие цифр true", lines[1]);
        Assertions.assertEquals("наличие заглавных букв false", lines[2]);
        Assertions.assertEquals("наличие строчных букв true", lines[3]);
        Assertions.assertEquals("наличие спецсимволов false", lines[4]);
    }

    /**
     * Проверяет использование команды /get без аргумента
     */
    @Test
    void testGetWithoutArgument() {
        String result = logic.handleMessage(44444, "/get");
        Assertions.assertEquals("Использование: /get <сервис>", result);
    }

    /**
     * Проверяет использование команды /delete без аргумента
     */
    @Test
    void testDeleteWithoutArgument() {
        String result = logic.handleMessage(44445, "/delete");
        Assertions.assertEquals("Использование: /delete <сервис>", result);
    }

    /**
     * Проверяет использование команды /change без аргумента
     */
    @Test
    void testChangeWithoutArgument() {
        String result = logic.handleMessage(44446, "/change");
        Assertions.assertEquals("Использование: /change <сервис>", result);
    }

    /**
     * Проверяет удаление с подтверждением.
     */
    @Test
    void testDeleteWithConfirmation() {
        // Используем уникальный chatId для изоляции теста
        long chatId = 77779;

        // Имитируем последовательность команд для добавления записи
        logic.handleMessage(chatId, "/add");
        logic.handleMessage(chatId, "ToDelete");
        logic.handleMessage(chatId, "login");
        logic.handleMessage(chatId, "2");
        String addResult = logic.handleMessage(chatId, "mypassword");

        // Проверяем успешность добавления записи
        Assertions.assertEquals("Данные сохранены", addResult);

        // Удаляем запись - запрос подтверждения
        String confirm = logic.handleMessage(chatId, "/delete ToDelete");
        Assertions.assertEquals("Удалить данные для \"ToDelete\"? (+ / -)", confirm);

        // Подтверждаем удаление
        String result = logic.handleMessage(chatId, "+");
        Assertions.assertEquals("Данные для \"ToDelete\" удалены", result);

        // Проверяем, что запись действительно удалена
        String getResult = logic.handleMessage(chatId, "/get ToDelete");
        Assertions.assertEquals("Сервис не найден", getResult);
    }
}