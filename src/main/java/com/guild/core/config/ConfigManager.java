package com.guild.core.config;
import com.guild.GuildPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
public class ConfigManager {
    private final GuildPlugin plugin;
    private final Logger logger;
    private final Map<String, FileConfiguration> configs = new HashMap<>();
    private final Map<String, File> configFiles = new HashMap<>();
    public ConfigManager(GuildPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        loadConfigs();
    }
    private void loadConfigs() {
        loadConfig("config.yml");
        loadConfig("messages.yml");
        loadConfig("gui.yml");
        loadConfig("database.yml");
    }
    public void loadConfig(String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        configs.put(fileName, config);
        configFiles.put(fileName, configFile);
        logger.info("Carregando arquivo de configuração: " + fileName);
    }
    public FileConfiguration getConfig(String fileName) {
        return configs.get(fileName);
    }
    public FileConfiguration getMainConfig() {
        return getConfig("config.yml");
    }
    public FileConfiguration getMessagesConfig() {
        return getConfig("messages.yml");
    }
    public FileConfiguration getGuiConfig() {
        return getConfig("gui.yml");
    }
    public FileConfiguration getDatabaseConfig() {
        return getConfig("database.yml");
    }
    public void saveMainConfig() {
        saveConfig("config.yml");
    }
    public void saveConfig(String fileName) {
        FileConfiguration config = configs.get(fileName);
        File configFile = configFiles.get(fileName);
        if (config != null && configFile != null) {
            try {
                config.save(configFile);
                logger.info("Salvando arquivo de configuração: " + fileName);
            } catch (IOException e) {
                logger.severe("Falha ao salvar arquivo de configuração: " + fileName + " - " + e.getMessage());
            }
        }
    }
    public void reloadConfig(String fileName) {
        loadConfig(fileName);
        logger.info("Recarregando arquivo de configuração: " + fileName);
    }
    public void reloadAllConfigs() {
        configs.clear();
        configFiles.clear();
        loadConfigs();
        logger.info("Recarregando todos os arquivos de configuração");
    }
    public String getString(String fileName, String path, String defaultValue) {
        FileConfiguration config = getConfig(fileName);
        if (config == null) return defaultValue;
        String value = config.getString(path, defaultValue);
        return value != null ? value.replace("&", "§") : defaultValue;
    }
    public int getInt(String fileName, String path, int defaultValue) {
        FileConfiguration config = getConfig(fileName);
        if (config == null) return defaultValue;
        return config.getInt(path, defaultValue);
    }
    public boolean getBoolean(String fileName, String path, boolean defaultValue) {
        FileConfiguration config = getConfig(fileName);
        if (config == null) return defaultValue;
        return config.getBoolean(path, defaultValue);
    }
}