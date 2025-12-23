package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.core.utils.PlaceholderUtils;
import com.guild.models.Guild;
import com.guild.models.GuildMember;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 成员详情GUI
 */
public class MemberDetailsGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private final GuildMember member;
    private final Player viewer;
    
    public MemberDetailsGUI(GuildPlugin plugin, Guild guild, GuildMember member, Player viewer) {
        this.plugin = plugin;
        this.guild = guild;
        this.member = member;
        this.viewer = viewer;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("member-details.title", "&6Detalhes do Membro")
            .replace("{member_name}", member.getPlayerName()));
    }
    
    @Override
    public int getSize() {
        return plugin.getConfigManager().getGuiConfig().getInt("member-details.size", 54);
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 设置成员头像
        setupMemberHead(inventory);
        
        // 设置基本信息
        setupBasicInfo(inventory);
        
        // 设置权限信息
        setupPermissionInfo(inventory);
        
        // 设置操作按钮
        setupActionButtons(inventory);
        
        // 设置返回按钮
        setupBackButton(inventory);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        // 检查是否是操作按钮
        if (isActionButton(slot)) {
            handleActionButton(player, slot);
            return;
        }
        
        // 检查是否是返回按钮
        if (slot == 49) {
            plugin.getGuiManager().openGUI(player, new MemberManagementGUI(plugin, guild));
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
     * 设置成员头像
     */
    private void setupMemberHead(Inventory inventory) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            // 根据角色设置不同的显示名称
            String displayName;
            switch (member.getRole()) {
                case LEADER:
                    displayName = ColorUtils.colorize("&c" + member.getPlayerName() + " &7(Líder)");
                    break;
                case OFFICER:
                    displayName = ColorUtils.colorize("&6" + member.getPlayerName() + " &7(Oficial)");
                    break;
                default:
                    displayName = ColorUtils.colorize("&f" + member.getPlayerName() + " &7(Membro)");
                    break;
            }
            
            meta.setDisplayName(displayName);
            
            List<String> lore = new ArrayList<>();
            lore.add(ColorUtils.colorize("&7UUID: &f" + member.getPlayerUuid()));
            lore.add(ColorUtils.colorize("&7Cargo: &f" + member.getRole().getDisplayName()));
            
            // 格式化加入时间
            if (member.getJoinedAt() != null) {
                String joinTime = member.getJoinedAt().format(com.guild.core.time.TimeProvider.FULL_FORMATTER);
                lore.add(ColorUtils.colorize("&7Entrou em: &f" + joinTime));
            } else {
                lore.add(ColorUtils.colorize("&7Entrou em: &fDesconhecido"));
            }
            
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        
        inventory.setItem(13, head);
    }
    
    /**
     * 设置基本信息
     */
    private void setupBasicInfo(Inventory inventory) {
        // 基本信息标题
        ItemStack infoTitle = createItem(
            Material.BOOK,
            ColorUtils.colorize("&6Informações Básicas"),
            ColorUtils.colorize("&7Informações detalhadas do membro")
        );
        inventory.setItem(20, infoTitle);
        
        // 角色信息
        ItemStack roleInfo = createItem(
            Material.GOLDEN_HELMET,
            ColorUtils.colorize("&eInformações do Cargo"),
            ColorUtils.colorize("&7Cargo Atual: &f" + member.getRole().getDisplayName()),
            ColorUtils.colorize("&7Nível do Cargo: &f" + getRoleLevel(member.getRole())),
            ColorUtils.colorize("&7Online: &f" + (isPlayerOnline(member.getPlayerUuid()) ? "&aSim" : "&cNão"))
        );
        inventory.setItem(21, roleInfo);
        
        // 时间信息
        ItemStack timeInfo = createItem(
            Material.CLOCK,
            ColorUtils.colorize("&eInformações de Tempo"),
            ColorUtils.colorize("&7Entrou em: &f" + formatTime(member.getJoinedAt())),
            ColorUtils.colorize("&7Tempo na Guilda: &f" + getGuildDuration(member.getJoinedAt()))
        );
        inventory.setItem(22, timeInfo);
        
        // 贡献信息
        ItemStack contributionInfo = createItem(
            Material.EMERALD,
            ColorUtils.colorize("&eInformações de Contribuição"),
            ColorUtils.colorize("&7Contribuição: &f" + getMemberContribution()),
            ColorUtils.colorize("&7Atividade: &f" + getMemberActivity())
        );
        inventory.setItem(23, contributionInfo);
    }
    
    /**
     * 设置权限信息
     */
    private void setupPermissionInfo(Inventory inventory) {
        // 权限信息标题
        ItemStack permissionTitle = createItem(
            Material.SHIELD,
            ColorUtils.colorize("&6Informações de Permissão"),
            ColorUtils.colorize("&7Permissões atuais")
        );
        inventory.setItem(29, permissionTitle);
        
        // 具体权限列表
        List<String> permissions = getRolePermissions(member.getRole());
        ItemStack permissionList = createItem(
            Material.PAPER,
            ColorUtils.colorize("&eLista de Permissões"),
            permissions.toArray(new String[0])
        );
        inventory.setItem(30, permissionList);
        
        // 权限等级
        ItemStack permissionLevel = createItem(
            Material.EXPERIENCE_BOTTLE,
            ColorUtils.colorize("&eNível de Permissão"),
            ColorUtils.colorize("&7Nível Atual: &f" + getPermissionLevel(member.getRole())),
            ColorUtils.colorize("&7Ações Executáveis: &f" + getExecutableActions(member.getRole()))
        );
        inventory.setItem(31, permissionLevel);
    }
    
    /**
     * 设置操作按钮
     */
    private void setupActionButtons(Inventory inventory) {
        // 检查当前玩家是否有权限执行操作
        plugin.getGuildService().getGuildMemberAsync(guild.getId(), viewer.getUniqueId()).thenAccept(viewerMember -> {
            if (viewerMember == null) return;
            
            // 不能操作自己
            if (member.getPlayerUuid().equals(viewer.getUniqueId())) {
                return;
            }
            
            // 不能操作会长
            if (member.getRole() == GuildMember.Role.LEADER) {
                return;
            }
            
            // 踢出按钮（需要踢出权限）
            if (viewerMember.getRole().canKick()) {
                ItemStack kickButton = createItem(
                    Material.REDSTONE_BLOCK,
                    ColorUtils.colorize("&cExpulsar Membro"),
                    ColorUtils.colorize("&7Expulsar membro da guilda"),
                    ColorUtils.colorize("&7Clique para confirmar expulsão")
                );
                inventory.setItem(37, kickButton);
            }
            
            // 提升/降级按钮（只有会长可以）
            if (viewerMember.getRole() == GuildMember.Role.LEADER) {
                if (member.getRole() == GuildMember.Role.OFFICER) {
                    // 降级按钮
                    ItemStack demoteButton = createItem(
                        Material.IRON_INGOT,
                        ColorUtils.colorize("&7Rebaixar Membro"),
                        ColorUtils.colorize("&7Rebaixar Oficial para Membro"),
                        ColorUtils.colorize("&7Clique para confirmar rebaixamento")
                    );
                    inventory.setItem(39, demoteButton);
                } else {
                    // 提升按钮
                    ItemStack promoteButton = createItem(
                        Material.GOLD_INGOT,
                        ColorUtils.colorize("&6Promover Membro"),
                        ColorUtils.colorize("&7Promover Membro para Oficial"),
                        ColorUtils.colorize("&7Clique para confirmar promoção")
                    );
                    inventory.setItem(39, promoteButton);
                }
            }
            
            // 发送消息按钮
            ItemStack messageButton = createItem(
                Material.PAPER,
                ColorUtils.colorize("&eEnviar Mensagem"),
                ColorUtils.colorize("&7Enviar mensagem privada para este membro"),
                ColorUtils.colorize("&7Clique para abrir o chat")
            );
            inventory.setItem(41, messageButton);
        });
    }
    
    /**
     * 设置返回按钮
     */
    private void setupBackButton(Inventory inventory) {
        ItemStack back = createItem(
            Material.ARROW,
            ColorUtils.colorize("&7Voltar"),
            ColorUtils.colorize("&7Voltar ao gerenciamento de membros")
        );
        inventory.setItem(49, back);
    }
    
    /**
     * 检查是否是操作按钮
     */
    private boolean isActionButton(int slot) {
        return slot == 37 || slot == 39 || slot == 41;
    }
    
    /**
     * 处理操作按钮点击
     */
    private void handleActionButton(Player player, int slot) {
        switch (slot) {
            case 37: // 踢出成员
                handleKickMember(player);
                break;
            case 39: // 提升/降级成员
                handlePromoteDemoteMember(player);
                break;
            case 41: // 发送消息
                handleSendMessage(player);
                break;
        }
    }
    
    /**
     * 处理踢出成员
     */
    private void handleKickMember(Player player) {
        // 检查权限
        plugin.getGuildService().getGuildMemberAsync(guild.getId(), player.getUniqueId()).thenAccept(executor -> {
            if (executor == null || !executor.getRole().canKick()) {
                String message = plugin.getConfigManager().getMessagesConfig().getString("gui.no-permission", "&cPermissão insuficiente");
                player.sendMessage(ColorUtils.colorize(message));
                return;
            }
            
            // 确认踢出
            String message = plugin.getConfigManager().getMessagesConfig().getString("gui.confirm-kick", "&cTem certeza que deseja expulsar {member}? Digite &f/guild kick {member} confirm &cpara confirmar")
                .replace("{member}", member.getPlayerName());
            player.sendMessage(ColorUtils.colorize(message));
            player.closeInventory();
        });
    }
    
    /**
     * 处理提升/降级成员
     */
    private void handlePromoteDemoteMember(Player player) {
        // 检查权限
        plugin.getGuildService().getGuildMemberAsync(guild.getId(), player.getUniqueId()).thenAccept(executor -> {
            if (executor == null || executor.getRole() != GuildMember.Role.LEADER) {
                String message = plugin.getConfigManager().getMessagesConfig().getString("gui.leader-only", "&cApenas o líder da guilda pode realizar esta ação");
                player.sendMessage(ColorUtils.colorize(message));
                return;
            }
            
            if (member.getRole() == GuildMember.Role.OFFICER) {
                // 降级
                String message = plugin.getConfigManager().getMessagesConfig().getString("gui.confirm-demote", "&cTem certeza que deseja rebaixar {member}? Digite &f/guild demote {member} confirm &cpara confirmar")
                    .replace("{member}", member.getPlayerName());
                player.sendMessage(ColorUtils.colorize(message));
            } else {
                // 提升
                String message = plugin.getConfigManager().getMessagesConfig().getString("gui.confirm-promote", "&aTem certeza que deseja promover {member} a Oficial? Digite &f/guild promote {member} confirm &apara confirmar")
                    .replace("{member}", member.getPlayerName());
                player.sendMessage(ColorUtils.colorize(message));
            }
            player.closeInventory();
        });
    }
    
    /**
     * 处理发送消息
     */
    private void handleSendMessage(Player player) {
        String message = plugin.getConfigManager().getMessagesConfig().getString("gui.open-chat", "&eDigite a mensagem para enviar para {member}:")
            .replace("{member}", member.getPlayerName());
        player.sendMessage(ColorUtils.colorize(message));
        player.closeInventory();
        
        // 这里可以集成聊天系统，暂时只是提示
        // TODO: 实现私信系统
    }
    
    /**
     * 获取角色等级
     */
    private String getRoleLevel(GuildMember.Role role) {
        switch (role) {
            case LEADER:
                return "Nível Máximo";
            case OFFICER:
                return "Avançado";
            default:
                return "Normal";
        }
    }
    
    /**
     * 检查玩家是否在线
     */
    private boolean isPlayerOnline(java.util.UUID playerUuid) {
        Player player = plugin.getServer().getPlayer(playerUuid);
        return player != null && player.isOnline();
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Desconhecido";
        return dateTime.format(com.guild.core.time.TimeProvider.FULL_FORMATTER);
    }
    
    /**
     * 获取在工会时长
     */
    private String getGuildDuration(java.time.LocalDateTime joinDateTime) {
        if (joinDateTime == null) return "Desconhecido";
        
        java.time.LocalDateTime currentTime = java.time.LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(joinDateTime, currentTime);
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        if (days > 0) {
            return days + " dias " + hours + " horas";
        } else if (hours > 0) {
            return hours + " horas " + minutes + " minutos";
        } else {
            return minutes + " minutos";
        }
    }
    
    /**
     * 获取成员贡献
     */
    private String getMemberContribution() {
        // TODO: 实现贡献统计系统
        return "Pendente";
    }
    
    /**
     * 获取成员活跃度
     */
    private String getMemberActivity() {
        // TODO: 实现活跃度统计系统
        return "Pendente";
    }
    
    /**
     * 获取角色权限列表
     */
    private List<String> getRolePermissions(GuildMember.Role role) {
        List<String> permissions = new ArrayList<>();
        
        switch (role) {
            case LEADER:
                permissions.add(ColorUtils.colorize("&7✓ Todas as permissões"));
                permissions.add(ColorUtils.colorize("&7✓ Convidar Membros"));
                permissions.add(ColorUtils.colorize("&7✓ Expulsar Membros"));
                permissions.add(ColorUtils.colorize("&7✓ Promover/Rebaixar"));
                permissions.add(ColorUtils.colorize("&7✓ Gerenciar Guilda"));
                permissions.add(ColorUtils.colorize("&7✓ Dissolver Guilda"));
                break;
            case OFFICER:
                permissions.add(ColorUtils.colorize("&7✓ Convidar Membros"));
                permissions.add(ColorUtils.colorize("&7✓ Expulsar Membros"));
                permissions.add(ColorUtils.colorize("&7✗ Promover/Rebaixar"));
                permissions.add(ColorUtils.colorize("&7✗ Gerenciar Guilda"));
                permissions.add(ColorUtils.colorize("&7✗ Dissolver Guilda"));
                break;
            default:
                permissions.add(ColorUtils.colorize("&7✗ Convidar Membros"));
                permissions.add(ColorUtils.colorize("&7✗ Expulsar Membros"));
                permissions.add(ColorUtils.colorize("&7✗ Promover/Rebaixar"));
                permissions.add(ColorUtils.colorize("&7✗ Gerenciar Guilda"));
                permissions.add(ColorUtils.colorize("&7✗ Dissolver Guilda"));
                break;
        }
        
        return permissions;
    }
    
    /**
     * 获取权限等级
     */
    private String getPermissionLevel(GuildMember.Role role) {
        switch (role) {
            case LEADER:
                return "Nível Máximo (Nível 3)";
            case OFFICER:
                return "Avançado (Nível 2)";
            default:
                return "Normal (Nível 1)";
        }
    }
    
    /**
     * 获取可执行操作
     */
    private String getExecutableActions(GuildMember.Role role) {
        switch (role) {
            case LEADER:
                return "Todas as ações";
            case OFFICER:
                return "Convidar, Expulsar";
            default:
                return "Ações básicas";
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
