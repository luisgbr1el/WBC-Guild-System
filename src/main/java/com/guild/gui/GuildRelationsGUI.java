package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.core.utils.PlaceholderUtils;
import com.guild.core.utils.CompatibleScheduler;
import com.guild.models.Guild;
import com.guild.models.GuildMember;
import com.guild.models.GuildRelation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 工会关系GUI - 管理工会关系
 */
public class GuildRelationsGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private final Player player;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 每页显示28个关系 (7列 × 4行)
    private List<GuildRelation> relations = new ArrayList<>();
    
    public GuildRelationsGUI(GuildPlugin plugin, Guild guild, Player player) {
        this.plugin = plugin;
        this.guild = guild;
        this.player = player;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("guild-relations.title", "&6Relações da Guilda"));
    }
    
    @Override
    public int getSize() {
        return plugin.getConfigManager().getGuiConfig().getInt("guild-relations.size", 54);
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 加载关系数据
        loadRelations().thenAccept(relationsList -> {
            this.relations = relationsList;
            
            // 确保在主线程中执行GUI操作
            CompatibleScheduler.runTask(plugin, () -> {
                // 显示关系列表
                displayRelations(inventory);
                
                // 添加功能按钮
                addFunctionButtons(inventory);
                
                // 添加分页按钮
                addPaginationButtons(inventory);
            });
        });
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        String itemName = clickedItem.getItemMeta().getDisplayName();
        
        // 返回按钮
        if (itemName.contains("Voltar")) {
            MainGuildGUI mainGUI = new MainGuildGUI(plugin);
            plugin.getGuiManager().openGUI(player, mainGUI);
            return;
        }
        
        // 创建关系按钮
        if (itemName.contains("Criar Relação")) {
            openCreateRelationGUI(player);
            return;
        }
        
        // 分页按钮
        if (itemName.contains("Página Anterior")) {
            if (currentPage > 0) {
                currentPage--;
                refreshInventory(player);
            }
            return;
        }
        
        if (itemName.contains("Próxima Página")) {
            int maxPage = (relations.size() - 1) / itemsPerPage;
            if (currentPage < maxPage) {
                currentPage++;
                refreshInventory(player);
            }
            return;
        }
        
        // 关系项目点击 - 检查是否在2-8列，2-5行范围内
        if (slot >= 10 && slot <= 43) {
            int row = slot / 9;
            int col = slot % 9;
            if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
                int relativeIndex = (row - 1) * 7 + (col - 1);
                int relationIndex = (currentPage * itemsPerPage) + relativeIndex;
                if (relationIndex < relations.size()) {
                    GuildRelation relation = relations.get(relationIndex);
                    handleRelationClick(player, relation, clickType);
                }
            }
        }
    }
    
    /**
     * 加载工会关系数据
     */
    private CompletableFuture<List<GuildRelation>> loadRelations() {
        return plugin.getGuildService().getGuildRelationsAsync(guild.getId());
    }
    
    /**
     * 显示关系列表
     */
    private void displayRelations(Inventory inventory) {
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, relations.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            GuildRelation relation = relations.get(i);
            int relativeIndex = i - startIndex;
            
            // 计算在2-8列，2-5行的位置 (slots 10-43)
            int row = (relativeIndex / 7) + 1; // 2-5行
            int col = (relativeIndex % 7) + 1; // 2-8列
            int slot = row * 9 + col;
            
            ItemStack relationItem = createRelationItem(relation);
            inventory.setItem(slot, relationItem);
        }
    }
    
    /**
     * 创建关系显示物品
     */
    private ItemStack createRelationItem(GuildRelation relation) {
        String otherGuildName = relation.getOtherGuildName(guild.getId());
        GuildRelation.RelationType type = relation.getType();
        GuildRelation.RelationStatus status = relation.getStatus();
        
        Material material = getRelationMaterial(type);
        String color = type.getColor();
        String displayName = color + otherGuildName + " - " + type.getDisplayName();
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.colorize("&7Tipo de Relação: " + color + type.getDisplayName()));
        lore.add(ColorUtils.colorize("&7Status: " + getStatusColor(status) + status.getDisplayName()));
        lore.add(ColorUtils.colorize("&7Iniciador: " + relation.getInitiatorName()));
        lore.add(ColorUtils.colorize("&7Criado em: " + formatDateTime(relation.getCreatedAt())));
        
        if (relation.getExpiresAt() != null) {
            lore.add(ColorUtils.colorize("&7Expira em: " + formatDateTime(relation.getExpiresAt())));
        }
        
        lore.add("");
        
        // 根据关系类型和状态添加操作提示
        if (status == GuildRelation.RelationStatus.PENDING) {
            if (relation.getInitiatorUuid().equals(player.getUniqueId())) {
                lore.add(ColorUtils.colorize("&cBotão Direito: Cancelar Relação"));
            } else {
                lore.add(ColorUtils.colorize("&aBotão Esquerdo: Aceitar Relação"));
                lore.add(ColorUtils.colorize("&cBotão Direito: Rejeitar Relação"));
            }
        } else if (status == GuildRelation.RelationStatus.ACTIVE) {
            if (type == GuildRelation.RelationType.TRUCE) {
                lore.add(ColorUtils.colorize("&eBotão Esquerdo: Terminar Trégua"));
            } else if (type == GuildRelation.RelationType.WAR) {
                lore.add(ColorUtils.colorize("&eBotão Esquerdo: Propor Trégua"));
            } else {
                lore.add(ColorUtils.colorize("&cBotão Direito: Excluir Relação"));
            }
        }
        
        return createItem(material, displayName, lore.toArray(new String[0]));
    }
    
    /**
     * 获取关系类型对应的材料
     */
    private Material getRelationMaterial(GuildRelation.RelationType type) {
        switch (type) {
            case ALLY: return Material.GREEN_WOOL;
            case ENEMY: return Material.RED_WOOL;
            case WAR: return Material.NETHERITE_SWORD;
            case TRUCE: return Material.YELLOW_WOOL;
            case NEUTRAL: return Material.GRAY_WOOL;
            default: return Material.WHITE_WOOL;
        }
    }
    
    /**
     * 获取状态颜色
     */
    private String getStatusColor(GuildRelation.RelationStatus status) {
        switch (status) {
            case PENDING: return "&e";
            case ACTIVE: return "&a";
            case EXPIRED: return "&7";
            case CANCELLED: return "&c";
            default: return "&f";
        }
    }
    
    /**
     * 格式化日期时间
     */
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Desconhecido";
        return dateTime.format(com.guild.core.time.TimeProvider.FULL_FORMATTER);
    }
    
    /**
     * 添加功能按钮
     */
    private void addFunctionButtons(Inventory inventory) {
        // 创建关系按钮
        ItemStack createRelation = createItem(
            Material.EMERALD,
            ColorUtils.colorize("&aCriar Relação"),
            ColorUtils.colorize("&7Criar nova relação de guilda"),
            ColorUtils.colorize("&7Aliados, Inimigos, Guerra, etc.")
        );
        inventory.setItem(45, createRelation);
        
        // 关系统计按钮
        ItemStack statistics = createItem(
            Material.BOOK,
            ColorUtils.colorize("&eEstatísticas de Relações"),
            ColorUtils.colorize("&7Ver estatísticas de relações"),
            ColorUtils.colorize("&7Número de aliados, inimigos, etc.")
        );
        inventory.setItem(47, statistics);
    }
    
    /**
     * 添加分页按钮
     */
    private void addPaginationButtons(Inventory inventory) {
        int maxPage = (relations.size() - 1) / itemsPerPage;
        
        // 上一页按钮
        if (currentPage > 0) {
            ItemStack previousPage = createItem(
                Material.ARROW,
                ColorUtils.colorize("&cPágina Anterior"),
                ColorUtils.colorize("&7Ver página anterior")
            );
            inventory.setItem(45, previousPage);
        }
        
        // 下一页按钮
        if (currentPage < maxPage) {
            ItemStack nextPage = createItem(
                Material.ARROW,
                ColorUtils.colorize("&aPróxima Página"),
                ColorUtils.colorize("&7Ver próxima página")
            );
            inventory.setItem(53, nextPage);
        }
        
        // 返回按钮
        ItemStack backButton = createItem(
            Material.BARRIER,
            ColorUtils.colorize("&cVoltar"),
            ColorUtils.colorize("&7Voltar ao menu principal")
        );
        inventory.setItem(49, backButton);
        
        // 页码显示
        ItemStack pageInfo = createItem(
            Material.PAPER,
            ColorUtils.colorize("&ePág " + (currentPage + 1)),
            ColorUtils.colorize("&7Total " + (maxPage + 1) + " págs"),
            ColorUtils.colorize("&7Total " + relations.size() + " relações")
        );
        inventory.setItem(47, pageInfo);
    }
    
    /**
     * 处理关系点击
     */
    private void handleRelationClick(Player player, GuildRelation relation, ClickType clickType) {
        GuildRelation.RelationStatus status = relation.getStatus();
        GuildRelation.RelationType type = relation.getType();
        
        if (status == GuildRelation.RelationStatus.PENDING) {
            if (relation.getInitiatorUuid().equals(player.getUniqueId())) {
                // 发起人取消关系
                if (clickType == ClickType.RIGHT) {
                    cancelRelation(player, relation);
                }
            } else {
                // 对方处理关系
                if (clickType == ClickType.LEFT) {
                    acceptRelation(player, relation);
                } else if (clickType == ClickType.RIGHT) {
                    rejectRelation(player, relation);
                }
            }
        } else if (status == GuildRelation.RelationStatus.ACTIVE) {
            if (type == GuildRelation.RelationType.TRUCE) {
                if (clickType == ClickType.LEFT) {
                    endTruce(player, relation);
                }
            } else if (type == GuildRelation.RelationType.WAR) {
                if (clickType == ClickType.LEFT) {
                    proposeTruce(player, relation);
                }
            } else {
                if (clickType == ClickType.RIGHT) {
                    deleteRelation(player, relation);
                }
            }
        }
    }
    
    /**
     * 接受关系
     */
    private void acceptRelation(Player player, GuildRelation relation) {
        plugin.getGuildService().updateGuildRelationStatusAsync(relation.getId(), GuildRelation.RelationStatus.ACTIVE)
            .thenAccept(success -> {
                CompatibleScheduler.runTask(plugin, () -> {
                    if (success) {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.accept-success", "&aRelação com {guild} aceita!");
                        message = message.replace("{guild}", relation.getOtherGuildName(guild.getId()));
                        player.sendMessage(ColorUtils.colorize(message));
                        refreshInventory(player);
                    } else {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.accept-failed", "&cFalha ao aceitar relação!");
                        player.sendMessage(ColorUtils.colorize(message));
                    }
                });
            });
    }
    
    /**
     * 拒绝关系
     */
    private void rejectRelation(Player player, GuildRelation relation) {
        plugin.getGuildService().updateGuildRelationStatusAsync(relation.getId(), GuildRelation.RelationStatus.CANCELLED)
            .thenAccept(success -> {
                CompatibleScheduler.runTask(plugin, () -> {
                    if (success) {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.reject-success", "&cRelação com {guild} rejeitada!");
                        message = message.replace("{guild}", relation.getOtherGuildName(guild.getId()));
                        player.sendMessage(ColorUtils.colorize(message));
                        refreshInventory(player);
                    } else {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.reject-failed", "&cFalha ao rejeitar relação!");
                        player.sendMessage(ColorUtils.colorize(message));
                    }
                });
            });
    }
    
    /**
     * 取消关系
     */
    private void cancelRelation(Player player, GuildRelation relation) {
        plugin.getGuildService().updateGuildRelationStatusAsync(relation.getId(), GuildRelation.RelationStatus.CANCELLED)
            .thenAccept(success -> {
                CompatibleScheduler.runTask(plugin, () -> {
                    if (success) {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.cancel-success", "&cRelação com {guild} cancelada!");
                        message = message.replace("{guild}", relation.getOtherGuildName(guild.getId()));
                        player.sendMessage(ColorUtils.colorize(message));
                        refreshInventory(player);
                    } else {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.cancel-failed", "&cFalha ao cancelar relação!");
                        player.sendMessage(ColorUtils.colorize(message));
                    }
                });
            });
    }
    
    /**
     * 结束停战
     */
    private void endTruce(Player player, GuildRelation relation) {
        // 结束停战，改为中立关系
        GuildRelation newRelation = new GuildRelation(
            relation.getGuild1Id(), relation.getGuild2Id(),
            relation.getGuild1Name(), relation.getGuild2Name(),
            GuildRelation.RelationType.NEUTRAL, player.getUniqueId(), player.getName()
        );
        
        plugin.getGuildService().createGuildRelationAsync(
            newRelation.getGuild1Id(), newRelation.getGuild2Id(),
            newRelation.getGuild1Name(), newRelation.getGuild2Name(),
            newRelation.getType(), newRelation.getInitiatorUuid(), newRelation.getInitiatorName()
        ).thenAccept(success -> {
            CompatibleScheduler.runTask(plugin, () -> {
                if (success) {
                    // 删除旧的停战关系
                    plugin.getGuildService().deleteGuildRelationAsync(relation.getId());
                    
                    String message = plugin.getConfigManager().getMessagesConfig().getString("relations.truce-end", "&aTrégua com {guild} terminou, relação agora é neutra!");
                    message = message.replace("{guild}", relation.getOtherGuildName(guild.getId()));
                    player.sendMessage(ColorUtils.colorize(message));
                    refreshInventory(player);
                } else {
                    String message = plugin.getConfigManager().getMessagesConfig().getString("relations.truce-end-failed", "&cFalha ao terminar trégua!");
                    player.sendMessage(ColorUtils.colorize(message));
                }
            });
        });
    }
    
    /**
     * 提议停战
     */
    private void proposeTruce(Player player, GuildRelation relation) {
        // 创建停战提议
        GuildRelation truceRelation = new GuildRelation(
            relation.getGuild1Id(), relation.getGuild2Id(),
            relation.getGuild1Name(), relation.getGuild2Name(),
            GuildRelation.RelationType.TRUCE, player.getUniqueId(), player.getName()
        );
        
        plugin.getGuildService().createGuildRelationAsync(
            truceRelation.getGuild1Id(), truceRelation.getGuild2Id(),
            truceRelation.getGuild1Name(), truceRelation.getGuild2Name(),
            truceRelation.getType(), truceRelation.getInitiatorUuid(), truceRelation.getInitiatorName()
        ).thenAccept(success -> {
            CompatibleScheduler.runTask(plugin, () -> {
                if (success) {
                    String message = plugin.getConfigManager().getMessagesConfig().getString("relations.truce-proposed", "&eTrégua proposta para {guild}!");
                    message = message.replace("{guild}", relation.getOtherGuildName(guild.getId()));
                    player.sendMessage(ColorUtils.colorize(message));
                    refreshInventory(player);
                } else {
                    String message = plugin.getConfigManager().getMessagesConfig().getString("relations.truce-propose-failed", "&cFalha ao propor trégua!");
                    player.sendMessage(ColorUtils.colorize(message));
                }
            });
        });
    }
    
    /**
     * 删除关系
     */
    private void deleteRelation(Player player, GuildRelation relation) {
        plugin.getGuildService().deleteGuildRelationAsync(relation.getId())
            .thenAccept(success -> {
                CompatibleScheduler.runTask(plugin, () -> {
                    if (success) {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.delete-success", "&aRelação com {guild} excluída!");
                        message = message.replace("{guild}", relation.getOtherGuildName(guild.getId()));
                        player.sendMessage(ColorUtils.colorize(message));
                        refreshInventory(player);
                    } else {
                        String message = plugin.getConfigManager().getMessagesConfig().getString("relations.delete-failed", "&cFalha ao excluir relação!");
                        player.sendMessage(ColorUtils.colorize(message));
                    }
                });
            });
    }
    
    /**
     * 打开创建关系GUI
     */
    private void openCreateRelationGUI(Player player) {
        CreateRelationGUI createRelationGUI = new CreateRelationGUI(plugin, guild, player);
        plugin.getGuiManager().openGUI(player, createRelationGUI);
    }
    
    /**
     * 刷新库存
     */
    private void refreshInventory(Player player) {
        if (player.isOnline()) {
            plugin.getGuiManager().refreshGUI(player);
        }
    }
    
    /**
     * 填充边框
     */
    private void fillBorder(Inventory inventory) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
    
    /**
     * 创建物品
     */
    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
