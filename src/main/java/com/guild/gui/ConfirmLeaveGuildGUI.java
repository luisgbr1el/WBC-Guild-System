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
 * 确认离开工会GUI
 */
public class ConfirmLeaveGuildGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    
    public ConfirmLeaveGuildGUI(GuildPlugin plugin, Guild guild) {
        this.plugin = plugin;
        this.guild = guild;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&cConfirmar Saída da Guilda");
    }
    
    @Override
    public int getSize() {
        return 27;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 显示确认信息
        displayConfirmInfo(inventory);
        
        // 添加确认和取消按钮
        setupButtons(inventory);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        switch (slot) {
            case 11: // 确认离开
                handleConfirmLeave(player);
                break;
            case 15: // 取消
                handleCancel(player);
                break;
        }
    }
    
    /**
     * 填充边框
     */
    private void fillBorder(Inventory inventory) {
        ItemStack border = createItem(Material.BLACK_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 18, border);
        }
        for (int i = 9; i < 18; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
    
    /**
     * 显示确认信息
     */
    private void displayConfirmInfo(Inventory inventory) {
        ItemStack info = createItem(
            Material.BOOK,
            ColorUtils.colorize("&cConfirmar Saída da Guilda"),
            ColorUtils.colorize("&7Guilda: &e" + guild.getName()),
            ColorUtils.colorize("&7Tem certeza que deseja sair desta guilda?"),
            ColorUtils.colorize("&cEsta operação é irreversível!")
        );
        inventory.setItem(13, info);
    }
    
    /**
     * 设置按钮
     */
    private void setupButtons(Inventory inventory) {
        // 确认离开按钮
        ItemStack confirm = createItem(
            Material.REDSTONE_BLOCK,
            ColorUtils.colorize("&cConfirmar Saída"),
            ColorUtils.colorize("&7Clique para confirmar a saída")
        );
        inventory.setItem(11, confirm);
        
        // 取消按钮
        ItemStack cancel = createItem(
            Material.EMERALD_BLOCK,
            ColorUtils.colorize("&aCancelar"),
            ColorUtils.colorize("&7Cancelar saída da guilda")
        );
        inventory.setItem(15, cancel);
    }
    
    /**
     * 处理确认离开
     */
    private void handleConfirmLeave(Player player) {
        // 检查是否是会长
        if (player.getUniqueId().equals(guild.getLeaderUuid())) {
            String message = plugin.getConfigManager().getMessagesConfig().getString("leave.leader-cannot-leave", "&cO líder da guilda não pode sair! Transfira a liderança ou exclua a guilda primeiro.");
            player.sendMessage(ColorUtils.colorize(message));
            return;
        }
        
        // 离开工会
        plugin.getGuildService().removeGuildMemberAsync(player.getUniqueId(), player.getUniqueId()).thenAccept(success -> {
            if (success) {
                String message = plugin.getConfigManager().getMessagesConfig().getString("leave.success", "&aVocê saiu da guilda &e{guild}&a com sucesso!")
                    .replace("{guild}", guild.getName());
                player.sendMessage(ColorUtils.colorize(message));
                
                // 关闭GUI
                player.closeInventory();
            } else {
                String message = plugin.getConfigManager().getMessagesConfig().getString("leave.failed", "&cFalha ao sair da guilda!");
                player.sendMessage(ColorUtils.colorize(message));
            }
        });
    }
    
    /**
     * 处理取消
     */
    private void handleCancel(Player player) {
        // 返回工会设置GUI
        plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
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
