package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.models.Guild;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

/**
 * 工会权限设置GUI
 */
public class GuildPermissionsGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    
    public GuildPermissionsGUI(GuildPlugin plugin, Guild guild) {
        this.plugin = plugin;
        this.guild = guild;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&6Configurações de Permissão da Guilda");
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 显示权限信息
        displayPermissions(inventory);
        
        // 添加返回按钮
        setupButtons(inventory);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        if (slot == 49) {
            // 返回
            plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
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
     * 显示权限信息
     */
    private void displayPermissions(Inventory inventory) {
        // 会长权限
        ItemStack leaderPerms = createItem(
            Material.GOLDEN_HELMET,
            ColorUtils.colorize("&6Permissões do Líder"),
            ColorUtils.colorize("&7• Todas as permissões"),
            ColorUtils.colorize("&7• Gerenciar membros"),
            ColorUtils.colorize("&7• Modificar configurações"),
            ColorUtils.colorize("&7• Excluir guilda")
        );
        inventory.setItem(10, leaderPerms);
        
        // 官员权限
        ItemStack officerPerms = createItem(
            Material.IRON_HELMET,
            ColorUtils.colorize("&ePermissões de Oficial"),
            ColorUtils.colorize("&7• Convidar membros"),
            ColorUtils.colorize("&7• Expulsar membros"),
            ColorUtils.colorize("&7• Processar solicitações")
        );
        inventory.setItem(12, officerPerms);
        
        // 成员权限
        ItemStack memberPerms = createItem(
            Material.LEATHER_HELMET,
            ColorUtils.colorize("&7Permissões de Membro"),
            ColorUtils.colorize("&7• Ver informações da guilda"),
            ColorUtils.colorize("&7• Solicitar entrada em outras guildas")
        );
        inventory.setItem(14, memberPerms);
        
        // 权限说明
        ItemStack info = createItem(
            Material.BOOK,
            ColorUtils.colorize("&eInformações de Permissão"),
            ColorUtils.colorize("&7Sistema de permissão baseado em cargos"),
            ColorUtils.colorize("&7Líder pode promover/rebaixar membros"),
            ColorUtils.colorize("&7Oficiais podem gerenciar membros"),
            ColorUtils.colorize("&7Membros têm permissões básicas")
        );
        inventory.setItem(16, info);
        
        // 当前权限状态
        ItemStack currentStatus = createItem(
            Material.SHIELD,
            ColorUtils.colorize("&aStatus Atual de Permissão"),
            ColorUtils.colorize("&7Guilda: &e" + guild.getName()),
            ColorUtils.colorize("&7Sistema de Permissão: &aFuncionando"),
            ColorUtils.colorize("&7Verificação de Permissão: &aAtivada")
        );
        inventory.setItem(22, currentStatus);
    }
    
    /**
     * 设置按钮
     */
    private void setupButtons(Inventory inventory) {
        // 返回按钮
        ItemStack back = createItem(
            Material.ARROW,
            ColorUtils.colorize("&7Voltar"),
            ColorUtils.colorize("&7Voltar para Configurações da Guilda")
        );
        inventory.setItem(49, back);
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
