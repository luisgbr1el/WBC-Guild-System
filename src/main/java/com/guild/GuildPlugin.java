package com.guild;

import com.guild.core.ServiceContainer;
import com.guild.core.config.ConfigManager;
import com.guild.core.database.DatabaseManager;
import com.guild.core.events.EventBus;
import com.guild.core.gui.GUIManager;
import com.guild.core.placeholder.PlaceholderManager;
import com.guild.core.permissions.PermissionManager;
import com.guild.commands.GuildCommand;
import com.guild.commands.GuildAdminCommand;
import com.guild.listeners.PlayerListener;
import com.guild.listeners.GuildListener;
import com.guild.services.GuildService;
import com.guild.core.utils.ServerUtils;
import com.guild.core.utils.TestUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class GuildPlugin extends JavaPlugin {
    
    private static GuildPlugin instance;
    private ServiceContainer serviceContainer;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EventBus eventBus;
    private GUIManager guiManager;
    private PlaceholderManager placeholderManager;
    private PermissionManager permissionManager;
    private GuildService guildService;
    
    @Override
    public void onEnable() {
        instance = this;
        Logger logger = getLogger();
        
        logger.info("Iniciando WBC Guild System...");
        logger.info("Tipo de servidor detectado: " + ServerUtils.getServerType());
        logger.info("Versão do servidor: " + ServerUtils.getServerVersion());
        
        // Verificar compatibilidade da versão da API
        if (!ServerUtils.supportsApiVersion("1.21")) {
            logger.severe("Este plugin requer a versão 1.21 ou superior! Versão atual: " + ServerUtils.getServerVersion());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // Executar testes de compatibilidade (usando o logger do plugin)
        TestUtils.testCompatibility(logger);
        TestUtils.testSchedulerCompatibility(logger);
        
        try {
            // Inicializar o contêiner de serviços
            serviceContainer = new ServiceContainer();
            
            // Inicializar o gerenciador de configurações
            configManager = new ConfigManager(this);
            serviceContainer.register(ConfigManager.class, configManager);
            
            // Inicializar o gerenciador de banco de dados
            databaseManager = new DatabaseManager(this);
            serviceContainer.register(DatabaseManager.class, databaseManager);
            
            // Inicializar o barramento de eventos
            eventBus = new EventBus();
            serviceContainer.register(EventBus.class, eventBus);
            
            // Inicializar o gerenciador de GUI
            guiManager = new GUIManager(this);
            serviceContainer.register(GUIManager.class, guiManager);
            
            // Inicializar o gerenciador de placeholders
            placeholderManager = new PlaceholderManager(this);
            serviceContainer.register(PlaceholderManager.class, placeholderManager);
            
            // Inicializar o gerenciador de permissões
            permissionManager = new PermissionManager(this);
            serviceContainer.register(PermissionManager.class, permissionManager);
            
            // Registrar o serviço da guilda
            guildService = new GuildService(this);
            serviceContainer.register(GuildService.class, guildService);
            
            // Definir a referência do GuildService no PlaceholderManager
            placeholderManager.setGuildService(guildService);
            
            // Registrar comandos
            registerCommands();
            
            // Registrar ouvintes
            registerListeners();
            
            // Iniciar serviços
            startServices();
            
            logger.info("Plugin de guilda iniciado com sucesso!");
            logger.info("Modo de compatibilidade: " + (ServerUtils.isFolia() ? "Folia" : "Spigot"));
            
        } catch (Exception e) {
            logger.severe("Falha ao iniciar o plugin de guilda: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    @Override
    public void onDisable() {
        Logger logger = getLogger();
        logger.info("Desligando plugin de guilda...");
        
        try {
            // Fechar todas as GUIs
            if (guiManager != null) {
                guiManager.closeAllGUIs();
            }
            
            // Desligar serviços
            if (serviceContainer != null) {
                serviceContainer.shutdown();
            }
            
            logger.info("Plugin de guilda desligado");
            
        } catch (Exception e) {
            logger.severe("Erro ao desligar o plugin de guilda: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void registerCommands() {
        GuildCommand guildCommand = new GuildCommand(this);
        GuildAdminCommand guildAdminCommand = new GuildAdminCommand(this);
        
        getCommand("guild").setExecutor(guildCommand);
        getCommand("guild").setTabCompleter(guildCommand);
        getCommand("guildadmin").setExecutor(guildAdminCommand);
        getCommand("guildadmin").setTabCompleter(guildAdminCommand);
    }
    
    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildListener(this), this);
    }
    
    private void startServices() {
        // Iniciar conexão com o banco de dados
        databaseManager.initialize();
        
        // Registrar placeholders
        placeholderManager.registerPlaceholders();
        
        // Inicializar o sistema de GUI
        guiManager.initialize();
    }
    
    public static GuildPlugin getInstance() {
        return instance;
    }
    
    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public EventBus getEventBus() {
        return eventBus;
    }
    
    public GUIManager getGuiManager() {
        return guiManager;
    }
    
    public PlaceholderManager getPlaceholderManager() {
        return placeholderManager;
    }
    
    public PermissionManager getPermissionManager() {
        return permissionManager;
    }
    
    public GuildService getGuildService() {
        return guildService;
    }
}
