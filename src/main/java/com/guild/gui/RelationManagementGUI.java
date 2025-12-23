package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.models.Guild;
import com.guild.models.GuildRelation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * 关系管理GUI - 管理员专用
 */
public class RelationManagementGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Player player;
    private int currentPage = 0;
    private final int itemsPerPage = 28; // 7列 × 4行
    private List<GuildRelation> allRelations = new ArrayList<>();
    private boolean isLoading = false;
    
    // 确认删除机制
    private static final Map<UUID, GuildRelation> pendingDeletions = new HashMap<>();
    private static final Map<UUID, Long> deletionTimers = new HashMap<>();
    private static final long CONFIRMATION_TIMEOUT = 10000; // 10秒确认超时
    
    public RelationManagementGUI(GuildPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        // 检查管理员权限
        if (!player.hasPermission("guild.admin")) {
            player.sendMessage(ColorUtils.colorize("&cVocê não tem permissão de administrador!"));
            return;
        }
        loadRelations();
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&4Gerenciamento de Relações - Admin");
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 设置关系列表
        setupRelationList(inventory);
        
        // 设置分页按钮
        setupPaginationButtons(inventory);
        
        // 设置操作按钮
        setupActionButtons(inventory);
    }
    
    private void setupRelationList(Inventory inventory) {
        if (isLoading) {
            // 显示加载中
            ItemStack loadingItem = createItem(Material.SAND, ColorUtils.colorize("&eCarregando..."), 
                ColorUtils.colorize("&7Carregando dados de relação..."));
            inventory.setItem(22, loadingItem);
            return;
        }
        
        if (allRelations.isEmpty()) {
            // 显示无数据
            ItemStack emptyItem = createItem(Material.BARRIER, ColorUtils.colorize("&cSem dados de relação"), 
                ColorUtils.colorize("&7Nenhuma relação de guilda encontrada"));
            inventory.setItem(22, emptyItem);
            return;
        }
        
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, allRelations.size());
        
        for (int i = 0; i < itemsPerPage; i++) {
            if (startIndex + i < endIndex) {
                GuildRelation relation = allRelations.get(startIndex + i);
                
                // 计算在2-8列，2-5行的位置 (slots 10-43)
                int row = (i / 7) + 1; // 2-5行
                int col = (i % 7) + 1; // 2-8列
                int slot = row * 9 + col;
                
                inventory.setItem(slot, createRelationItem(relation));
            }
        }
    }
    
    private ItemStack createRelationItem(GuildRelation relation) {
        Material material = getRelationMaterial(relation.getType());
        String status = getRelationStatus(relation.getStatus());
        
        // 检查是否在待删除状态
        boolean isPendingDeletion = pendingDeletions.containsKey(player.getUniqueId()) && 
                                  pendingDeletions.get(player.getUniqueId()).getId() == relation.getId();
        
        List<String> lore = new ArrayList<>();
        lore.add(ColorUtils.colorize("&7Tipo de Relação: " + getRelationTypeName(relation.getType())));
        lore.add(ColorUtils.colorize("&7Status: " + status));
        lore.add(ColorUtils.colorize("&7Guilda 1: " + relation.getGuild1Name()));
        lore.add(ColorUtils.colorize("&7Guilda 2: " + relation.getGuild2Name()));
        lore.add(ColorUtils.colorize("&7Iniciador: " + relation.getInitiatorName()));
        lore.add(ColorUtils.colorize("&7Criado em: " + formatDateTime(relation.getCreatedAt())));
        lore.add("");
        
        if (isPendingDeletion) {
            lore.add(ColorUtils.colorize("&4⚠ Exclusão Pendente"));
            lore.add(ColorUtils.colorize("&cBotão Esquerdo: Confirmar Exclusão"));
            lore.add(ColorUtils.colorize("&eBotão Direito: Cancelar Exclusão"));
        } else {
            lore.add(ColorUtils.colorize("&cBotão Esquerdo: Excluir Relação"));
            lore.add(ColorUtils.colorize("&eBotão Direito: Ver Detalhes"));
        }
        
        String displayName = ColorUtils.colorize("&6" + relation.getGuild1Name() + " ↔ " + relation.getGuild2Name());
        if (isPendingDeletion) {
            displayName = ColorUtils.colorize("&4" + relation.getGuild1Name() + " ↔ " + relation.getGuild2Name());
        }
        
        return createItem(material, displayName, lore.toArray(new String[0]));
    }
    
    private Material getRelationMaterial(GuildRelation.RelationType type) {
        switch (type) {
            case ALLY: return Material.GREEN_WOOL;
            case ENEMY: return Material.RED_WOOL;
            case WAR: return Material.NETHERITE_SWORD;
            case TRUCE: return Material.YELLOW_WOOL;
            case NEUTRAL: return Material.GRAY_WOOL;
            default: return Material.STONE;
        }
    }
    
    private String getRelationTypeName(GuildRelation.RelationType type) {
        switch (type) {
            case ALLY: return "Aliado";
            case ENEMY: return "Inimigo";
            case WAR: return "Guerra";
            case TRUCE: return "Trégua";
            case NEUTRAL: return "Neutro";
            default: return "Desconhecido";
        }
    }
    
    private String getRelationStatus(GuildRelation.RelationStatus status) {
        switch (status) {
            case PENDING: return "Pendente";
            case ACTIVE: return "Ativo";
            case EXPIRED: return "Expirado";
            case CANCELLED: return "Cancelado";
            default: return "Desconhecido";
        }
    }
    
    private String formatDateTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Desconhecido";
        return dateTime.format(com.guild.core.time.TimeProvider.FULL_FORMATTER);
    }
    
    private void setupPaginationButtons(Inventory inventory) {
        int totalPages = (int) Math.ceil((double) allRelations.size() / itemsPerPage);
        
        // 上一页按钮
        if (currentPage > 0) {
            inventory.setItem(45, createItem(Material.ARROW, ColorUtils.colorize("&aPágina Anterior"), 
                ColorUtils.colorize("&7Página " + (currentPage) + "")));
        }
        
        // 页码信息
        inventory.setItem(49, createItem(Material.PAPER, ColorUtils.colorize("&ePágina " + (currentPage + 1) + " de " + totalPages + ""),
            ColorUtils.colorize("&7Total de " + allRelations.size() + " relações")));
        
        // 下一页按钮
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createItem(Material.ARROW, ColorUtils.colorize("&aPróxima Página"), 
                ColorUtils.colorize("&7Página " + (currentPage + 2) + "")));
        }
    }
    
    private void setupActionButtons(Inventory inventory) {
        // 返回按钮
        inventory.setItem(46, createItem(Material.BARRIER, ColorUtils.colorize("&cVoltar"),
            ColorUtils.colorize("&7Voltar ao menu de admin")));
        
        // 刷新按钮
        inventory.setItem(52, createItem(Material.EMERALD, ColorUtils.colorize("&aAtualizar Lista"),
            ColorUtils.colorize("&7Recarregar dados de relação")));
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
    
    private void loadRelations() {
        if (isLoading) return; // 防止重复加载
        
        isLoading = true;
        
        // 获取所有工会的关系
        plugin.getGuildService().getAllGuildsAsync().thenCompose(guilds -> {
            List<CompletableFuture<List<GuildRelation>>> relationFutures = new ArrayList<>();
            
            for (Guild guild : guilds) {
                relationFutures.add(plugin.getGuildService().getGuildRelationsAsync(guild.getId()));
            }
            
            return CompletableFuture.allOf(relationFutures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    List<GuildRelation> allRelationsList = new ArrayList<>();
                    for (CompletableFuture<List<GuildRelation>> future : relationFutures) {
                        try {
                            allRelationsList.addAll(future.get());
                        } catch (Exception e) {
                            plugin.getLogger().warning("Erro ao carregar relações da guilda: " + e.getMessage());
                        }
                    }
                    return allRelationsList;
                });
        }).thenAccept(relations -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                allRelations.clear();
                allRelations.addAll(relations);
                isLoading = false;
                
                if (player.isOnline()) {
                    // 使用安全的刷新方法
                    plugin.getGuiManager().refreshGUI(player);
                }
            });
        }).exceptionally(throwable -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                isLoading = false;
                if (player.isOnline()) {
                    player.sendMessage(ColorUtils.colorize("&cErro ao carregar dados de relação: " + throwable.getMessage()));
                    plugin.getGuiManager().refreshGUI(player);
                }
            });
            return null;
        });
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        // 检查管理员权限
        if (!player.hasPermission("guild.admin")) {
            player.sendMessage(ColorUtils.colorize("&cVocê não tem permissão de administrador!"));
            return;
        }
        
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        String itemName = clickedItem.getItemMeta().getDisplayName();
        
        if (slot == 46) {
            // 返回
            plugin.getGuiManager().openGUI(player, new AdminGuildGUI(plugin));
        } else if (slot == 52) {
            // 刷新
            if (!isLoading) {
                loadRelations();
                player.sendMessage(ColorUtils.colorize("&aAtualizando lista de relações..."));
            }
        } else if (slot == 45 && currentPage > 0) {
            // 上一页
            currentPage--;
            plugin.getGuiManager().refreshGUI(player);
        } else if (slot == 53 && currentPage < (int) Math.ceil((double) allRelations.size() / itemsPerPage) - 1) {
            // 下一页
            currentPage++;
            plugin.getGuiManager().refreshGUI(player);
        } else if (slot >= 10 && slot <= 43) {
            // 关系项目 - 检查是否在2-8列，2-5行范围内
            int row = slot / 9;
            int col = slot % 9;
            if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
                int relativeIndex = (row - 1) * 7 + (col - 1);
                int relationIndex = (currentPage * itemsPerPage) + relativeIndex;
                if (relationIndex < allRelations.size()) {
                    GuildRelation relation = allRelations.get(relationIndex);
                    handleRelationClick(player, relation, clickType);
                }
            }
        }
    }
    
    private void handleRelationClick(Player player, GuildRelation relation, ClickType clickType) {
        if (clickType == ClickType.LEFT) {
            // 左键处理
            if (pendingDeletions.containsKey(player.getUniqueId()) && 
                pendingDeletions.get(player.getUniqueId()).getId() == relation.getId()) {
                // 确认删除
                confirmDeleteRelation(player, relation);
            } else {
                // 开始删除流程
                startDeleteRelation(player, relation);
            }
        } else if (clickType == ClickType.RIGHT) {
            // 右键处理
            if (pendingDeletions.containsKey(player.getUniqueId()) && 
                pendingDeletions.get(player.getUniqueId()).getId() == relation.getId()) {
                // 取消删除
                cancelDeleteRelation(player);
            } else {
                // 查看详情
                showRelationDetails(player, relation);
            }
        }
    }
    
    private void startDeleteRelation(Player player, GuildRelation relation) {
        // 设置待删除状态
        pendingDeletions.put(player.getUniqueId(), relation);
        deletionTimers.put(player.getUniqueId(), System.currentTimeMillis());
        
        player.sendMessage(ColorUtils.colorize("&cTem certeza que deseja excluir a relação: " + relation.getGuild1Name() + " ↔ " + relation.getGuild2Name() + "?"));
        player.sendMessage(ColorUtils.colorize("&cBotão Esquerdo: Confirmar | Botão Direito: Cancelar"));
        player.sendMessage(ColorUtils.colorize("&eCancelamento automático em 10 segundos"));
        
        // 刷新GUI显示待删除状态
        plugin.getGuiManager().refreshGUI(player);
        
        // 设置超时任务
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingDeletions.containsKey(player.getUniqueId()) && 
                pendingDeletions.get(player.getUniqueId()).getId() == relation.getId()) {
                cancelDeleteRelation(player);
            }
        }, 200L); // 10秒 = 200 ticks
    }
    
    private void confirmDeleteRelation(Player player, GuildRelation relation) {
        // 清除待删除状态
        pendingDeletions.remove(player.getUniqueId());
        deletionTimers.remove(player.getUniqueId());
        
        // 执行删除
        plugin.getGuildService().deleteGuildRelationAsync(relation.getId()).thenAccept(success -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (success) {
                    player.sendMessage(ColorUtils.colorize("&aRelação excluída: " + relation.getGuild1Name() + " ↔ " + relation.getGuild2Name()));
                    // 从列表中移除
                    allRelations.remove(relation);
                    // 刷新GUI
                    plugin.getGuiManager().refreshGUI(player);
                } else {
                    player.sendMessage(ColorUtils.colorize("&cFalha ao excluir relação!"));
                }
            });
        }).exceptionally(throwable -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(ColorUtils.colorize("&cErro ao excluir relação: " + throwable.getMessage()));
            });
            return null;
        });
    }
    
    private void cancelDeleteRelation(Player player) {
        GuildRelation relation = pendingDeletions.remove(player.getUniqueId());
        deletionTimers.remove(player.getUniqueId());
        
        if (relation != null) {
            player.sendMessage(ColorUtils.colorize("&eExclusão de relação cancelada: " + relation.getGuild1Name() + " ↔ " + relation.getGuild2Name()));
            // 刷新GUI
            plugin.getGuiManager().refreshGUI(player);
        }
    }
    
    private void showRelationDetails(Player player, GuildRelation relation) {
        player.sendMessage(ColorUtils.colorize("&6=== Detalhes da Relação ==="));
        player.sendMessage(ColorUtils.colorize("&eTipo de Relação: " + getRelationTypeName(relation.getType())));
        player.sendMessage(ColorUtils.colorize("&eStatus: " + getRelationStatus(relation.getStatus())));
        player.sendMessage(ColorUtils.colorize("&eGuilda 1: " + relation.getGuild1Name() + " (ID: " + relation.getGuild1Id() + ")"));
        player.sendMessage(ColorUtils.colorize("&eGuilda 2: " + relation.getGuild2Name() + " (ID: " + relation.getGuild2Id() + ")"));
        player.sendMessage(ColorUtils.colorize("&eIniciador: " + relation.getInitiatorName()));
        player.sendMessage(ColorUtils.colorize("&eCriado em: " + formatDateTime(relation.getCreatedAt())));
        if (relation.getUpdatedAt() != null) {
            player.sendMessage(ColorUtils.colorize("&eAtualizado em: " + formatDateTime(relation.getUpdatedAt())));
        }
        if (relation.getExpiresAt() != null) {
            player.sendMessage(ColorUtils.colorize("&eExpira em: " + formatDateTime(relation.getExpiresAt())));
        }
        player.sendMessage(ColorUtils.colorize("&6=================="));
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
        // 清理资源
        allRelations.clear();
        // 清除待删除状态
        pendingDeletions.remove(player.getUniqueId());
        deletionTimers.remove(player.getUniqueId());
    }
    
    @Override
    public void refresh(Player player) {
        // 使用GUIManager的安全刷新方法
        if (player.isOnline()) {
            plugin.getGuiManager().refreshGUI(player);
        }
    }
}
