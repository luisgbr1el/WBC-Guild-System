package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.models.Guild;
import com.guild.gui.SystemSettingsGUI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GUI de Administração de Guildas
 */
public class AdminGuildGUI implements GUI {
    
    private final GuildPlugin plugin;
    
    public AdminGuildGUI(GuildPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.title", "&4Administração de Guildas"));
    }
    
    @Override
    public int getSize() {
        return plugin.getConfigManager().getGuiConfig().getInt("admin-gui.size", 54);
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // Preencher borda
        fillBorder(inventory);
        
        // Gerenciamento de lista de guildas
        ItemStack guildList = createItem(
            Material.BOOKSHELF,
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.guild-list.name", "&eGerenciar Lista de Guildas")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.guild-list.lore.1", "&7Ver e gerenciar todas as guildas")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.guild-list.lore.2", "&7Inclui exclusão, congelamento, etc."))
        );
        inventory.setItem(20, guildList);
        

        // Gerenciamento de relações
        ItemStack relations = createItem(
            Material.RED_WOOL,
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.relations.name", "&eGerenciar Relações")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.relations.lore.1", "&7Gerenciar relações entre guildas")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.relations.lore.2", "&7Aliados, Inimigos, Guerra, etc."))
        );
        inventory.setItem(24, relations);
        
        // Estatísticas
        ItemStack statistics = createItem(
            Material.PAPER,
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.statistics.name", "&eEstatísticas")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.statistics.lore.1", "&7Ver estatísticas das guildas")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.statistics.lore.2", "&7Membros, economia, etc."))
        );
        inventory.setItem(29, statistics);
        
        // Configurações do sistema
        ItemStack settings = createItem(
            Material.COMPASS,
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.settings.name", "&eConfigurações do Sistema")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.settings.lore.1", "&7Gerenciar configurações do sistema")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.settings.lore.2", "&7Recarregar config, permissões, etc."))
        );
        inventory.setItem(31, settings);
        
        // Botão de voltar
        ItemStack back = createItem(
            Material.ARROW,
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.back.name", "&cVoltar")),
            ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("admin-gui.items.back.lore.1", "&7Voltar ao menu principal"))
        );
        inventory.setItem(49, back);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        switch (slot) {
            case 20: // Gerenciamento de lista de guildas
                openGuildListManagement(player);
                break;
            case 24: // Gerenciamento de relações
                openRelationManagement(player);
                break;
            case 29: // Estatísticas
                openStatistics(player);
                break;
            case 31: // Configurações do sistema
                openSystemSettings(player);
                break;
            case 49:
                plugin.getGuiManager().openGUI(player, new MainGuildGUI(plugin));
                break;
        }
    }
    
    private void openGuildListManagement(Player player) {
        // Abrir GUI de gerenciamento de lista de guildas
        GuildListManagementGUI guildListGUI = new GuildListManagementGUI(plugin, player);
        plugin.getGuiManager().openGUI(player, guildListGUI);
    }
    

    private void openRelationManagement(Player player) {
        // Abrir GUI de gerenciamento de relações
        RelationManagementGUI relationGUI = new RelationManagementGUI(plugin, player);
        plugin.getGuiManager().openGUI(player, relationGUI);
    }
    
    private void openStatistics(Player player) {
        // Exibir estatísticas
        plugin.getGuildService().getAllGuildsAsync().thenAccept(guilds -> {
            player.sendMessage(ColorUtils.colorize("&6=== Estatísticas das Guildas ==="));
            player.sendMessage(ColorUtils.colorize("&eTotal de Guildas: &f" + guilds.size()));
            
            if (!guilds.isEmpty()) {
                final int[] frozenCount = {0};
                
                for (Guild guild : guilds) {
                    if (guild.isFrozen()) {
                        frozenCount[0]++;
                    }
                }
                
                // Obter número total de membros
                CompletableFuture<Integer>[] memberCountFutures = new CompletableFuture[guilds.size()];
                for (int i = 0; i < guilds.size(); i++) {
                    memberCountFutures[i] = plugin.getGuildService().getGuildMemberCountAsync(guilds.get(i).getId());
                }
                
                CompletableFuture.allOf(memberCountFutures).thenRun(() -> {
                    final int[] totalMembers = {0};
                    for (CompletableFuture<Integer> future : memberCountFutures) {
                        try {
                            totalMembers[0] += future.get();
                        } catch (Exception e) {
                            plugin.getLogger().severe("Erro ao obter número de membros: " + e.getMessage());
                        }
                    }
                    
                    player.sendMessage(ColorUtils.colorize("&eTotal de Membros: &f" + totalMembers[0]));
                    player.sendMessage(ColorUtils.colorize("&eGuildas Congeladas: &f" + frozenCount[0]));
                    player.sendMessage(ColorUtils.colorize("&eGuildas Ativas: &f" + (guilds.size() - frozenCount[0])));
                });
            }
        });
    }
    
    private void openSystemSettings(Player player) {
        // Abrir GUI de configurações do sistema
        plugin.getGuiManager().openGUI(player, new SystemSettingsGUI(plugin, player));
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
        // Atualizar GUI
    }
}
