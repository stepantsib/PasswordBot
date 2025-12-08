package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

/**
 * Адаптер тг получает апдейты, передаёт текст в PasswordLogic
 * и отправляет текстовый ответ обратно пользователю.
 */
public class TgBot {

    /** Клиент тг api */
    private final TelegramBot bot;

    /** логика бота (генерация паролей, обработка команд)*/
    private final PasswordLogic logic;

    /**
     * Создаёт тг бота с заданным токеном
     */
    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.logic = new PasswordLogic();
    }


    /**
     * Запускает получение обновлений и обработку входящих сообщений
     */
    public void start() {
        bot.setUpdatesListener(updates -> {
            for (Update update : updates) {
                Message message = update.message();
                String messageText = message.text();
                long chatId = message.chat().id();
                String replyText = logic.handleMessage(
                        chatId,
                        messageText
                );
                if (replyText != null && !replyText.isBlank()) {
                    SendMessage response = new SendMessage(chatId, replyText);
                    bot.execute(response);
                }
            }
            return UpdatesListener.CONFIRMED_UPDATES_ALL;
        });
    }
}
