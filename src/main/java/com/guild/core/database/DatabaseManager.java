package com.guild.core.database;

import com.guild.GuildPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class DatabaseManager {
    
    private final GuildPlugin plugin;
    private final Logger logger;
    private HikariDataSource dataSource;
    private DatabaseType databaseType;
    
    public DatabaseManager(GuildPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }
    
    public void initialize() {
        FileConfiguration config = plugin.getConfigManager().getDatabaseConfig();
        String type = config.getString("type", config.getString("database.type", "sqlite")).toLowerCase();
        
        try {
            if ("mysql".equals(type)) {
                initializeMySQL(config);
            } else {
                initializeSQLite(config);
            }
            
            createTables();
            
            logger.info("Conexão com banco de dados inicializada com sucesso: " + databaseType);
            
        } catch (Exception e) {
            logger.severe("数据库连接初始化失败: " + e.getMessage());
            throw new RuntimeException("数据库连接失败", e);
        }
    }
    
    private void initializeMySQL(FileConfiguration config) {
        databaseType = DatabaseType.MYSQL;
        
        HikariConfig hikariConfig = new HikariConfig();
        String host = config.getString("mysql.host", config.getString("database.mysql.host", "localhost"));
        int port = config.getInt("mysql.port", config.getInt("database.mysql.port", 3306));
        String database = config.getString("mysql.database", config.getString("database.mysql.database", "guild"));
        String params = "?useSSL=" + (config.getBoolean("mysql.use-ssl", config.getBoolean("database.mysql.use-ssl", false)) ? "true" : "false") +
                "&serverTimezone=" + config.getString("mysql.timezone", config.getString("database.mysql.timezone", "UTC")) +
                "&characterEncoding=" + config.getString("mysql.character-encoding", config.getString("database.mysql.character-encoding", "UTF-8"));
        hikariConfig.setJdbcUrl("jdbc:mysql:
