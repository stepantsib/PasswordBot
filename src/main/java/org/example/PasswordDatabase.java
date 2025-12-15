package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * База данных на SQLite.
 * Данные сохраняются между перезапусками бота.
 */
public class PasswordDatabase {

    /**
     * URL для подключения к SQLite базе данных.
     * Файл базы данных называется passwords.db и создается в текущей директории.
     * Формат: jdbc:sqlite:passwords.db
     */
    private static final String DB_URL = "jdbc:sqlite:passwords.db";

    /**
     * Создает новый экземпляр PasswordDatabase и инициализирует базу данных.
     * При создании автоматически вызывает метод init() для создания таблицы, если она не существует.
     */
    public PasswordDatabase() {
        init();
    }

    /**
     * Внутренний класс, представляющий одну запись из базы данных.
     * Содержит информацию о сервисе, логине и пароле.
     */
    public class Entry {

        /**
         * Название сервиса, для которого сохранены учетные данные.
         */
        private final String service;

        /**
         * Логин или email пользователя для указанного сервиса.
         */
        private final String login;

        /**
         * Пароль, сохраненный для данного сервиса и пользователя.
         */
        private final String password;

        /**
         * Создает новую запись с указанными данными.
         *
         * service Название сервиса, не может быть null.
         * login Логин пользователя, не может быть null.
         * password Пароль пользователя, не может быть null.
         */
        Entry(String service, String login, String password) {
            this.service = service;
            this.login = login;
            this.password = password;
        }

        /**
         * Возвращает название сервиса, связанного с этой записью.
         * Возвращает строку с названием сервиса.
         */
        public String getService() { return service; }

        /**
         * Возвращает логин пользователя, сохраненный в этой записи.
         * Возвращает строку с логином пользователя.
         */
        public String getLogin() { return login; }

        /**
         * Возвращает пароль, сохраненный в этой записи.
         * Возвращает строку с паролем.
         */
        public String getPassword() { return password; }
    }

    /**
     * Инициализирует базу данных, создавая таблицу credentials, если она не существует.
     * Таблица имеет следующую структуру:
     * - user_id: идентификатор пользователя Telegram (целое число)
     * - service: название сервиса (текст)
     * - login: логин пользователя (текст)
     * - password: пароль пользователя (текст)
     * Первичный ключ состоит из комбинации user_id и service, что гарантирует уникальность
     * сервисов для каждого пользователя.
     * В случае ошибки SQL исключение перехватывается и бот продолжает работу.
     */
    private void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS credentials (
                user_id INTEGER NOT NULL,
                service TEXT NOT NULL,
                login TEXT NOT NULL,
                password TEXT NOT NULL,
                PRIMARY KEY (user_id, service)
            );
            """;

        try (Connection c = DriverManager.getConnection(DB_URL);
             Statement st = c.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {

        }
    }

    /**
     * Сохраняет или обновляет запись в базе данных.
     * Если запись с указанными user_id и service уже существует, она обновляется.
     * Если не существует - создается новая запись.
     *
     * userId Идентификатор пользователя Telegram (chatId).
     * service Название сервиса, для которого сохраняются учетные данные.
     * login Логин пользователя для указанного сервиса.
     * password Пароль пользователя для указанного сервиса.
     *
     * В случае ошибки SQL исключение перехватывается и бот продолжает работу.
     */
    public void save(long userId, String service, String login, String password) {
        String sql = """
            INSERT INTO credentials(user_id, service, login, password)
            VALUES (?, ?, ?, ?)
            ON CONFLICT(user_id, service)
            DO UPDATE SET login = excluded.login,
                          password = excluded.password;
            """;

        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, service);
            ps.setString(3, login);
            ps.setString(4, password);
            ps.executeUpdate();

        } catch (SQLException e) {

        }
    }

    /**
     * Ищет запись в базе данных по идентификатору пользователя и названию сервиса.
     *
     * userId Идентификатор пользователя Telegram (chatId).
     * service Название сервиса для поиска.
     *
     * Возвращает объект Entry с найденными данными или null, если запись не найдена.
     * В случае ошибки SQL исключение перехватывается, возвращает null и бот продолжает работу.
     */
    public Entry find(long userId, String service) {
        String sql = """
            SELECT service, login, password
            FROM credentials
            WHERE user_id = ? AND service = ?;
            """;

        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, service);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Entry(
                        rs.getString("service"),
                        rs.getString("login"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * Удаляет запись из базы данных по идентификатору пользователя и названию сервиса.
     *
     * userId Идентификатор пользователя Telegram (chatId).
     * service Название сервиса, запись которого нужно удалить.
     *
     * В случае ошибки SQL исключение перехватывается и бот продолжает работу.
     */
    public void delete(long userId, String service) {
        String sql = "DELETE FROM credentials WHERE user_id=? AND service=?;";

        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, userId);
            ps.setString(2, service);
            ps.executeUpdate();

        } catch (SQLException e) {

        }
    }

    /**
     * Возвращает список всех сервисов, сохраненных указанным пользователем.
     * Сервисы возвращаются в алфавитном порядке.
     * userId Идентификатор пользователя Telegram (chatId).
     * Возвращает список строк с названиями сервисов.
     * Если у пользователя нет сохраненных сервисов, возвращает пустой список.
     * В случае ошибки SQL возвращает пустой список.
     */
    public List<String> listServices(long userId) {
        String sql = "SELECT service FROM credentials WHERE user_id=? ORDER BY service;";
        List<String> out = new ArrayList<>();

        try (Connection c = DriverManager.getConnection(DB_URL);
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getString("service"));
                }
            }
        } catch (SQLException e) {
        }
        return out;
    }
}