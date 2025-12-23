package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * GUI de Configurações do Sistema
 */
public class SystemSettingsGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Player player;
    
    public SystemSettingsGUI(GuildPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&4Configurações do Sistema");
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // Preencher borda
        fillBorder(inventory);
        
        // Configurar opções de sistema
        setupSettingsOptions(inventory);
        
        // Configurar botões de ação
        setupActionButtons(inventory);
    }
    
    private void setupSettingsOptions(Inventory inventory) {
        // Alternar exibição de informações detalhadas no console
        boolean debugMode = plugin.getConfigManager().getMainConfig().getBoolean("debug.enabled", false);
        Material debugMaterial = debugMode ? Material.LIME_WOOL : Material.RED_WOOL;
        String debugStatus = debugMode ? "&aAtivado" : "&cDesativado";
        
        ItemStack debugToggle = createItem(
            debugMaterial,
            ColorUtils.colorize("&eExibir informações detalhadas no console"),
            ColorUtils.colorize("&7Status atual: " + debugStatus),
            ColorUtils.colorize("&7Quando ativado, será exibido no console"),
            ColorUtils.colorize("&7informações detalhadas de depuração"),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(19, debugToggle);
        
        // Configurações de salvamento automático
        boolean autoSave = plugin.getConfigManager().getMainConfig().getBoolean("auto-save.enabled", true);
        Material autoSaveMaterial = autoSave ? Material.LIME_WOOL : Material.RED_WOOL;
        String autoSaveStatus = autoSave ? "&aAtivado" : "&cDesativado";
        
        ItemStack autoSaveToggle = createItem(
            autoSaveMaterial,
            ColorUtils.colorize("&eSalvar dados automaticamente"),
            ColorUtils.colorize("&7Status atual: " + autoSaveStatus),
            ColorUtils.colorize("&7Salvar dados da guilda periodicamente"),
            ColorUtils.colorize("&7Prevenir perda de dados"),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(21, autoSaveToggle);
        
        // Alternar sistema de economia
        boolean economyEnabled = plugin.getConfigManager().getMainConfig().getBoolean("economy.enabled", true);
        Material economyMaterial = economyEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String economyStatus = economyEnabled ? "&aAtivado" : "&cDesativado";
        
        ItemStack economyToggle = createItem(
            economyMaterial,
            ColorUtils.colorize("&eSistema de Economia"),
            ColorUtils.colorize("&7Status atual: " + economyStatus),
            ColorUtils.colorize("&7Alternar função de economia da guilda"),
            ColorUtils.colorize("&7Inclui depósito, retirada, transferência, etc."),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(23, economyToggle);
        
        // Alternar sistema de relações
        boolean relationEnabled = plugin.getConfigManager().getMainConfig().getBoolean("relations.enabled", true);
        Material relationMaterial = relationEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String relationStatus = relationEnabled ? "&aAtivado" : "&cDesativado";
        
        ItemStack relationToggle = createItem(
            relationMaterial,
            ColorUtils.colorize("&eSistema de Relações da Guilda"),
            ColorUtils.colorize("&7Status atual: " + relationStatus),
            ColorUtils.colorize("&7Alternar função de relações da guilda"),
            ColorUtils.colorize("&7Inclui aliado, inimigo, guerra, etc."),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(25, relationToggle);
        
        // Alternar sistema de níveis
        boolean levelEnabled = plugin.getConfigManager().getMainConfig().getBoolean("level-system.enabled", true);
        Material levelMaterial = levelEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String levelStatus = levelEnabled ? "&aAtivado" : "&cDesativado";
        
        ItemStack levelToggle = createItem(
            levelMaterial,
            ColorUtils.colorize("&eSistema de Nível da Guilda"),
            ColorUtils.colorize("&7Status atual: " + levelStatus),
            ColorUtils.colorize("&7Alternar função de nível da guilda"),
            ColorUtils.colorize("&7Inclui atualização automática, limite de membros, etc."),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(28, levelToggle);
        
        // Alternar sistema de inscrições
        boolean applicationEnabled = plugin.getConfigManager().getMainConfig().getBoolean("applications.enabled", true);
        Material applicationMaterial = applicationEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String applicationStatus = applicationEnabled ? "&aAtivado" : "&cDesativado";
        
        ItemStack applicationToggle = createItem(
            applicationMaterial,
            ColorUtils.colorize("&eSistema de Inscrição"),
            ColorUtils.colorize("&7Status atual: " + applicationStatus),
            ColorUtils.colorize("&7Alternar função de inscrição na guilda"),
            ColorUtils.colorize("&7Jogadores precisam se inscrever para entrar na guilda"),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(30, applicationToggle);
        
        // Alternar sistema de convites
        boolean inviteEnabled = plugin.getConfigManager().getMainConfig().getBoolean("invites.enabled", true);
        Material inviteMaterial = inviteEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String inviteStatus = inviteEnabled ? "&aAtivado" : "&cDesativado";
        
        ItemStack inviteToggle = createItem(
            inviteMaterial,
            ColorUtils.colorize("&eSistema de Convite"),
            ColorUtils.colorize("&7Status atual: " + inviteStatus),
            ColorUtils.colorize("&7Alternar função de convite da guilda"),
            ColorUtils.colorize("&7O líder pode convidar jogadores para a guilda"),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(32, inviteToggle);
        
        // Alternar sistema de casa da guilda
        boolean homeEnabled = plugin.getConfigManager().getMainConfig().getBoolean("guild-home.enabled", true);
        Material homeMaterial = homeEnabled ? Material.LIME_WOOL : Material.RED_WOOL;
        String homeStatus = homeEnabled ? "&aAtivado" : "&cDesativado";
        
        ItemStack homeToggle = createItem(
            homeMaterial,
            ColorUtils.colorize("&eSistema de Casa da Guilda"),
            ColorUtils.colorize("&7Status atual: " + homeStatus),
            ColorUtils.colorize("&7Alternar função de casa da guilda"),
            ColorUtils.colorize("&7Inclui definir e teleportar para a casa da guilda"),
            "",
            ColorUtils.colorize("&eClique para alternar o status")
        );
        inventory.setItem(34, homeToggle);
    }
    
    private void setupActionButtons(Inventory inventory) {
        // Botão de recarregar configurações
        ItemStack reload = createItem(
            Material.EMERALD,
            ColorUtils.colorize("&aRecarregar Configurações"),
            ColorUtils.colorize("&7Recarregar todos os arquivos de configuração"),
            ColorUtils.colorize("&7Inclui messages.yml, gui.yml, etc."),
            "",
            ColorUtils.colorize("&eClique para recarregar configurações")
        );
        inventory.setItem(37, reload);
        
        // Botão de manutenção do banco de dados
        ItemStack database = createItem(
            Material.BOOK,
            ColorUtils.colorize("&bManutenção do Banco de Dados"),
            ColorUtils.colorize("&7Manutenção e otimização do banco de dados"),
            ColorUtils.colorize("&7Limpar dados expirados, otimizar desempenho"),
            "",
            ColorUtils.colorize("&eClique para realizar manutenção")
        );
        inventory.setItem(39, database);
        
        // Botão de backup de dados
        ItemStack backup = createItem(
            Material.CHEST,
            ColorUtils.colorize("&6Backup de Dados"),
            ColorUtils.colorize("&7Fazer backup dos dados da guilda"),
            ColorUtils.colorize("&7Criar arquivo de backup de dados"),
            "",
            ColorUtils.colorize("&eClique para fazer backup")
        );
        inventory.setItem(41, backup);
        
        // Botão de voltar
        ItemStack back = createItem(
            Material.ARROW,
            ColorUtils.colorize("&cVoltar"),
            ColorUtils.colorize("&7Voltar ao menu de gerenciamento")
        );
        inventory.setItem(49, back);
        
        // Botão de salvar configurações
        ItemStack save = createItem(
            Material.GREEN_WOOL,
            ColorUtils.colorize("&aSalvar Configurações"),
            ColorUtils.colorize("&7Salvar todas as configurações atuais"),
            ColorUtils.colorize("&7Aplicar ao arquivo de configuração"),
            "",
            ColorUtils.colorize("&eClique para salvar configurações")
        );
        inventory.setItem(51, save);
    }
    
    private void fillBorder(Inventory inventory) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        
        // Preencher borda
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        switch (slot) {
            case 19: // Alternar exibição de informações detalhadas no console
                toggleDebugMode(player);
                break;
            case 21: // Alternar salvamento automático
                toggleAutoSave(player);
                break;
            case 23: // Alternar sistema de economia
                toggleEconomy(player);
                break;
            case 25: // Alternar sistema de relações
                toggleRelations(player);
                break;
            case 28: // Alternar sistema de níveis
                toggleLevelSystem(player);
                break;
            case 30: // Alternar sistema de inscrições
                toggleApplications(player);
                break;
            case 32: // Alternar sistema de convites
                toggleInvites(player);
                break;
            case 34: // Alternar sistema de casa da guilda
                toggleGuildHome(player);
                break;
            case 37: // Recarregar configurações
                reloadConfigs(player);
                break;
            case 39: // Manutenção do banco de dados
                maintainDatabase(player);
                break;
            case 41: // Backup de dados
                backupData(player);
                break;
            case 49: // Voltar
                plugin.getGuiManager().openGUI(player, new AdminGuildGUI(plugin));
                break;
            case 51: // Salvar configurações
                saveSettings(player);
                break;
        }
    }
    
    private void toggleDebugMode(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("debug.enabled", false);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("debug.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aInformações detalhadas do console ativadas!" : "&cInformações detalhadas do console desativadas!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleAutoSave(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("auto-save.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("auto-save.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSalvamento automático de dados ativado!" : "&cSalvamento automático de dados desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleEconomy(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("economy.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("economy.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSistema de economia ativado!" : "&cSistema de economia desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleRelations(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("relations.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("relations.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSistema de relações da guilda ativado!" : "&cSistema de relações da guilda desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleLevelSystem(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("level-system.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("level-system.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSistema de nível da guilda ativado!" : "&cSistema de nível da guilda desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleApplications(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("applications.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("applications.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSistema de inscrição ativado!" : "&cSistema de inscrição desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleInvites(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("invites.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("invites.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSistema de convite ativado!" : "&cSistema de convite desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void toggleGuildHome(Player player) {
        boolean current = plugin.getConfigManager().getMainConfig().getBoolean("guild-home.enabled", true);
        boolean newValue = !current;
        plugin.getConfigManager().getMainConfig().set("guild-home.enabled", newValue);
        plugin.getConfigManager().saveMainConfig();
        
        String message = newValue ? "&aSistema de casa da guilda ativado!" : "&cSistema de casa da guilda desativado!";
        player.sendMessage(ColorUtils.colorize(message));
        refresh(player);
    }
    
    private void reloadConfigs(Player player) {
        try {
            plugin.getConfigManager().reloadAllConfigs();
            player.sendMessage(ColorUtils.colorize("&aConfiguração recarregada com sucesso!"));
        } catch (Exception e) {
            player.sendMessage(ColorUtils.colorize("&cFalha ao recarregar configuração: " + e.getMessage()));
        }
    }
    
    private void maintainDatabase(Player player) {
        player.sendMessage(ColorUtils.colorize("&eFunção de manutenção do banco de dados em desenvolvimento..."));
        // TODO: Implementar função de manutenção do banco de dados
        // Incluindo limpeza de dados expirados, otimização de desempenho, etc.
    }
    
    private void backupData(Player player) {
        player.sendMessage(ColorUtils.colorize("&eFunção de backup de dados em desenvolvimento..."));
        // TODO: Implementar função de backup de dados
        // Criar arquivo de backup de dados
    }
    
    private void saveSettings(Player player) {
        try {
            plugin.getConfigManager().saveMainConfig();
            player.sendMessage(ColorUtils.colorize("&aConfigurações salvas com sucesso!"));
        } catch (Exception e) {
            player.sendMessage(ColorUtils.colorize("&cFalha ao salvar configurações: " + e.getMessage()));
        }
    }
    
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            
            item.setItemMeta(meta);
        }
        
        return item;
    }
    
    @Override
    public void onClose(Player player) {
        // Processamento ao fechar
    }
    
    @Override
    public void refresh(Player player) {
        if (player.isOnline()) {
            plugin.getGuiManager().refreshGUI(player);
        }
    }
}
