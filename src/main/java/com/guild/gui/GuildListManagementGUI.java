package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.models.Guild;
import com.guild.gui.GuildDetailGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 工会列表管理GUI
 */
public class GuildListManagementGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Player player;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 7列 × 4行
    private List<Guild> allGuilds = new ArrayList<>();
    
    public GuildListManagementGUI(GuildPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        loadGuilds();
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&4Gerenciamento da Lista de Guildas");
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 设置工会列表
        setupGuildList(inventory);
        
        // 设置分页按钮
        setupPaginationButtons(inventory);
        
        // 设置操作按钮
        setupActionButtons(inventory);
    }
    
    private void setupGuildList(Inventory inventory) {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allGuilds.size());
        
        for (int i = 0; i < itemsPerPage; i++) {
            if (startIndex + i < endIndex) {
                Guild guild = allGuilds.get(startIndex + i);
                
                // 计算在2-8列，2-5行的位置 (slots 10-43)
                int row = (i / 7) + 1; // 2-5行
                int col = (i % 7) + 1; // 2-8列
                int slot = row * 9 + col;
                
                inventory.setItem(slot, createGuildItem(guild));
            }
        }
    }
    
    private ItemStack createGuildItem(Guild guild) {
        Material material = guild.isFrozen() ? Material.RED_WOOL : Material.GREEN_WOOL;
        String status = guild.isFrozen() ? "&cCongelada" : "&aNormal";
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.colorize("&7ID: " + guild.getId()));
        lore.add(ColorUtils.colorize("&7Tag: [" + (guild.getTag() != null ? guild.getTag() : "Nenhuma") + "]"));
        lore.add(ColorUtils.colorize("&7Líder: " + guild.getLeaderName()));
        lore.add(ColorUtils.colorize("&7Nível: " + guild.getLevel()));
        lore.add(ColorUtils.colorize("&7Status: " + status));
        lore.add("");
        lore.add(ColorUtils.colorize("&eBotão Esquerdo: Ver Detalhes"));
        lore.add(ColorUtils.colorize("&cBotão Direito: Excluir Guilda"));
        if (guild.isFrozen()) {
            lore.add(ColorUtils.colorize("&aBotão do Meio: Descongelar Guilda"));
        } else {
            lore.add(ColorUtils.colorize("&6Botão do Meio: Congelar Guilda"));
        }
        
        return createItem(material, ColorUtils.colorize("&6" + guild.getName()), lore.toArray(new String[0]));
    }
    
    private void setupPaginationButtons(Inventory inventory) {
        int totalPages = (int) Math.ceil((double) allGuilds.size() / itemsPerPage);
        
        // 上一页按钮
        if (currentPage > 0) {
            inventory.setItem(45, createItem(Material.ARROW, ColorUtils.colorize("&aPágina Anterior"), 
                ColorUtils.colorize("&7Pág " + (currentPage) + "")));
        }
        
        // 页码信息
        inventory.setItem(49, createItem(Material.PAPER, ColorUtils.colorize("&ePág " + (currentPage + 1) + " de " + totalPages + "")));
        
        // 下一页按钮
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createItem(Material.ARROW, ColorUtils.colorize("&aPróxima Página"), 
                ColorUtils.colorize("&7Pág " + (currentPage + 2) + "")));
        }
    }
    
    private void setupActionButtons(Inventory inventory) {
        // 返回按钮
        inventory.setItem(46, createItem(Material.BARRIER, ColorUtils.colorize("&cVoltar")));
        
        // 刷新按钮
        inventory.setItem(52, createItem(Material.EMERALD, ColorUtils.colorize("&aAtualizar Lista")));
    }
    
    private void fillBorder(Inventory inventory) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        
        // 填充边框
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
    
    private void loadGuilds() {
        plugin.getGuildService().getAllGuildsAsync().thenAccept(guilds -> {
            this.allGuilds = guilds;
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) {
                    refresh(player);
                }
            });
        });
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        if (slot == 46) {
            // 返回
            plugin.getGuiManager().openGUI(player, new AdminGuildGUI(plugin));
        } else if (slot == 52) {
            // 刷新
            loadGuilds();
        } else if (slot == 45 && currentPage > 0) {
            // 上一页
            currentPage--;
            refresh(player);
        } else if (slot == 53 && currentPage < (int) Math.ceil((double) allGuilds.size() / itemsPerPage) - 1) {
            // 下一页
            currentPage++;
            refresh(player);
        } else if (slot >= 10 && slot <= 43) {
            // 工会项目 - 检查是否在2-8列，2-5行范围内
            int row = slot / 9;
            int col = slot % 9;
            if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
                int relativeIndex = (row - 1) * 7 + (col - 1);
                int guildIndex = (currentPage * itemsPerPage) + relativeIndex;
                if (guildIndex < allGuilds.size()) {
                    Guild guild = allGuilds.get(guildIndex);
                    handleGuildClick(player, guild, clickType);
                }
            }
        }
    }
    
    private void handleGuildClick(Player player, Guild guild, ClickType clickType) {
        if (clickType == ClickType.LEFT) {
            // 查看详情
            openGuildDetailGUI(player, guild);
        } else if (clickType == ClickType.RIGHT) {
            // 删除工会
            deleteGuild(player, guild);
        } else if (clickType == ClickType.MIDDLE) {
            // 冻结/解冻工会
            toggleGuildFreeze(player, guild);
        }
    }
    
    private void openGuildDetailGUI(Player player, Guild guild) {
        // 打开工会详情GUI
        plugin.getGuiManager().openGUI(player, new GuildDetailGUI(plugin, guild, player));
    }
    
    private void deleteGuild(Player player, Guild guild) {
        // 确认删除
        player.sendMessage(ColorUtils.colorize("&cTem certeza que deseja excluir a guilda " + guild.getName() + "?"));
        player.sendMessage(ColorUtils.colorize("&cDigite &f/guildadmin delete " + guild.getName() + " confirm &cpara confirmar exclusão"));
    }
    
    private void toggleGuildFreeze(Player player, Guild guild) {
        boolean newStatus = !guild.isFrozen();
        plugin.getGuildService().updateGuildFrozenStatusAsync(guild.getId(), newStatus).thenAccept(success -> {
            if (success) {
                String message = newStatus ? "&aGuilda " + guild.getName() + " foi congelada!" : "&aGuilda " + guild.getName() + " foi descongelada!";
                player.sendMessage(ColorUtils.colorize(message));
                loadGuilds(); // 刷新列表
            } else {
                player.sendMessage(ColorUtils.colorize("&cOperação falhou!"));
            }
        });
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
        // 关闭时的处理
    }
    
    @Override
    public void refresh(Player player) {
        if (player.isOnline()) {
            plugin.getGuiManager().refreshGUI(player);
        }
    }
}
