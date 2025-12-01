package org.example;

import java.security.SecureRandom;

/**
 * Класс с логикой обработки команд и генерацией пароля
 */
public class PasswordLogic {

    /**
     * Генератор случайных чисел для создания паролей
     */
    private final SecureRandom random = new SecureRandom();

    /**
     * Обрабатывает сообщение пользователя и выполняет команду /password
     */
    public String handleMessage(String text) {
        text = text.trim();

        if (!text.equals("/password")) {
            return "Используй команду /password для генерации пароля";
        }

        return generatePassword(10);
    }

    /**
     * Создаёт пароль указанной длины из случайных символов
     */
    private String generatePassword(int length) {
        String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_-+=<>?/{}~";

        String pwd = "";

        for (int i = 0; i < length; i++) {
            int idx = random.nextInt(ALPHABET.length());
            pwd = pwd + ALPHABET.charAt(idx);
        }

        return pwd;
    }
}