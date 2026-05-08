package me.kasuki.kstaff.api.database.sqlite;

import me.kasuki.kstaff.api.database.repository.IDatabaseRepository;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Base repository implementation for sq lite operations.
 */
public abstract class AbstractSQLiteRepository<K> implements IDatabaseRepository<K> {

    /**
     * Stores connection string.
     */
    private final String connectionString;

    /**
     * Stores data label.
     */
    private final String dataLabel;

    /**
     * Stores setup table query.
     */
    private final String setupTableQuery;

    /**
     * Stores save to database query.
     */
    private final String saveToDatabaseQuery;

    /**
     * Stores get all entries query.
     */
    private final String getAllEntriesQuery;

    /**
     * Stores get from database query.
     */
    private final String getFromDatabaseQuery;

    /**
     * Stores remove from database query.
     */
    private final String removeFromDatabaseQuery;

    /**
     * Stores save batch query.
     */
    private final String saveBatchQuery;

    /**
     * Stores remove batch query.
     */
    private final String removeBatchQuery;

    /**
     * Constant for executor.
     */
    private static final Executor EXECUTOR = Executors.newSingleThreadExecutor();

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to load SQLite JDBC driver", e);
        }
    }

    /**
     * Creates a new AbstractSQLiteRepository instance.
     */
    protected AbstractSQLiteRepository(File directory, String name, String tableName) {
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        this.connectionString = "jdbc:sqlite:" + directory.getAbsolutePath() + "/" + name + ".db";
        this.dataLabel = "byte_data";

        this.setupTableQuery =
                String.format(
                        "CREATE TABLE IF NOT EXISTS `%s` (data_key VARCHAR(255) PRIMARY KEY, %s BLOB)",
                        tableName, this.dataLabel);
        this.saveToDatabaseQuery =
                String.format(
                        "INSERT OR REPLACE INTO `%s` (data_key, %s) VALUES (?, ?)", tableName, this.dataLabel);
        this.getAllEntriesQuery = String.format("SELECT %s FROM %s", this.dataLabel, tableName);
        this.getFromDatabaseQuery =
                String.format("SELECT %s FROM %s WHERE data_key = ? LIMIT 1", this.dataLabel, tableName);
        this.removeFromDatabaseQuery = String.format("DELETE FROM %s WHERE data_key = ?", tableName);
        this.saveBatchQuery =
                String.format(
                        "INSERT OR REPLACE INTO `%s` (data_key, %s) VALUES (?, ?)", tableName, this.dataLabel);
        this.removeBatchQuery = String.format("DELETE FROM %s WHERE data_key = ?", tableName);
        this.setupTable();
    }

    /**
     * Sets up table.
     */
    private void setupTable() {
        EXECUTOR.execute(
                () -> {
                    try (Connection connection = DriverManager.getConnection(this.connectionString);
                         PreparedStatement statement = connection.prepareStatement(this.setupTableQuery)) {
                        statement.executeUpdate();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
    }

    /**
     * Executes save to database.
     */
    public void saveToDatabase(K key, byte[] value) {
        EXECUTOR.execute(
                () -> {
                    try (Connection connection = DriverManager.getConnection(this.connectionString);
                         PreparedStatement statement = connection.prepareStatement(this.saveToDatabaseQuery)) {
                        statement.setString(1, key.toString());
                        statement.setBytes(2, value);
                        statement.executeUpdate();
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Gets all entries from database.
     */
    public void getAllEntriesFromDatabase(Consumer<Collection<byte[]>> consumer) {
        EXECUTOR.execute(() -> consumer.accept(this.getAllEntriesFromDatabaseSync()));
    }

    /**
     * Gets from database.
     */
    public void getFromDatabase(K key, Consumer<Optional<byte[]>> consumer) {
        EXECUTOR.execute(() -> consumer.accept(this.getFromDatabaseSync(key)));
    }

    /**
     * Executes remove from database.
     */
    public void removeFromDatabase(K key) {
        EXECUTOR.execute(
                () -> {
                    try (Connection connection = DriverManager.getConnection(this.connectionString)) {
                        try (PreparedStatement statement =
                                     connection.prepareStatement(this.removeFromDatabaseQuery)) {
                            statement.setString(1, key.toString());
                            statement.executeUpdate();
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Executes save batch.
     */
    public void saveBatch(Map<K, byte[]> batch) {
        EXECUTOR.execute(
                () -> {
                    try (Connection connection = DriverManager.getConnection(this.connectionString)) {
                        try (PreparedStatement statement = connection.prepareStatement(this.saveBatchQuery)) {
                            for (Map.Entry<K, byte[]> entry : batch.entrySet()) {
                                String key = entry.getKey().toString();
                                byte[] value = entry.getValue();

                                statement.setString(1, key);
                                statement.setBytes(2, value);
                                statement.addBatch();
                            }

                            statement.executeBatch();
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Executes delete batch.
     */
    @Override
    public void deleteBatch(Collection<K> keys) {
        EXECUTOR.execute(
                () -> {
                    try (Connection connection = DriverManager.getConnection(this.connectionString)) {
                        try (PreparedStatement statement = connection.prepareStatement(this.removeBatchQuery)) {
                            for (K key : keys) {
                                statement.setString(1, key.toString());
                                statement.addBatch();
                            }

                            statement.executeBatch();
                        }
                    } catch (SQLException exception) {
                        exception.printStackTrace();
                    }
                });
    }

    /**
     * Gets from database sync.
     */
    @Override
    public Optional<byte[]> getFromDatabaseSync(K key) {
        try (Connection connection = DriverManager.getConnection(this.connectionString)) {
            try (PreparedStatement statement = connection.prepareStatement(this.getFromDatabaseQuery)) {
                statement.setString(1, key.toString());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        byte[] data = resultSet.getBytes(this.dataLabel);
                        return Optional.ofNullable(data);
                    }
                    return Optional.empty();
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Gets all entries from database sync.
     */
    @Override
    public Collection<byte[]> getAllEntriesFromDatabaseSync() {
        List<byte[]> allValues = new ArrayList<>();

        try (Connection connection = DriverManager.getConnection(this.connectionString)) {
            try (PreparedStatement statement = connection.prepareStatement(this.getAllEntriesQuery)) {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        byte[] data = resultSet.getBytes(this.dataLabel);
                        allValues.add(data);
                    }
                }
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return allValues;
    }
}
