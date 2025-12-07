package org.example;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;

public class TgBot {

    private final TelegramBot bot;
    private final PasswordLogic logic;

    public TgBot(String token) {
        this.bot = new TelegramBot(token);
        this.logic = new PasswordLogic();
    }

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
