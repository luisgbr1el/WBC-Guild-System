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
import java.util.concurrent.CompletableFuture;

/**
 * 工会标签输入GUI
 */
public class GuildTagInputGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private String currentTag;
    
    public GuildTagInputGUI(GuildPlugin plugin, Guild guild) {
        this.plugin = plugin;
        this.guild = guild;
        this.currentTag = guild.getTag() != null ? guild.getTag() : "";
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&6Modificar Tag da Guilda");
    }
    
    @Override
    public int getSize() {
        return 27;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // 填充边框
        fillBorder(inventory);
        
        // 显示当前标签
        displayCurrentTag(inventory);
        
        // 添加操作按钮
        setupButtons(inventory);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        switch (slot) {
            case 11: // 输入标签
                handleInputTag(player);
                break;
            case 15: // 确认
                handleConfirm(player);
                break;
            case 13: // 取消
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
     * 显示当前标签
     */
    private void displayCurrentTag(Inventory inventory) {
        ItemStack currentTagItem = createItem(
            Material.OAK_SIGN,
            ColorUtils.colorize("&eTag Atual"),
            ColorUtils.colorize("&7" + (currentTag.isEmpty() ? "Sem Tag" : "[" + currentTag + "]"))
        );
        inventory.setItem(11, currentTagItem);
    }
    
    /**
     * 设置按钮
     */
    private void setupButtons(Inventory inventory) {
        // 确认按钮
        ItemStack confirm = createItem(
            Material.EMERALD_BLOCK,
            ColorUtils.colorize("&aConfirmar Modificação"),
            ColorUtils.colorize("&7Confirmar modificação da tag da guilda")
        );
        inventory.setItem(15, confirm);
        
        // 取消按钮
        ItemStack cancel = createItem(
            Material.REDSTONE_BLOCK,
            ColorUtils.colorize("&cCancelar"),
            ColorUtils.colorize("&7Cancelar modificação")
        );
        inventory.setItem(13, cancel);
    }
    
    /**
     * 处理输入标签
     */
    private void handleInputTag(Player player) {
        // 关闭GUI
        player.closeInventory();
        
        // 发送消息提示输入
        String message = plugin.getConfigManager().getMessagesConfig().getString("gui.input-tag", "&aPor favor, digite a nova tag da guilda no chat (máximo 10 caracteres):");
        player.sendMessage(ColorUtils.colorize(message));
        
        // 设置玩家为输入模式
        plugin.getGuiManager().setInputMode(player, input -> {
            if (input.length() > 10) {
                String errorMessage = plugin.getConfigManager().getMessagesConfig().getString("gui.tag-too-long", "&cTag muito longa, máximo 10 caracteres!");
                player.sendMessage(ColorUtils.colorize(errorMessage));
                return false;
            }
            
            // 更新标签
            currentTag = input;
            
            // 保存到数据库
            plugin.getGuildService().updateGuildAsync(guild.getId(), guild.getName(), input, guild.getDescription(), player.getUniqueId()).thenAccept(success -> {
                if (success) {
                    String successMessage = plugin.getConfigManager().getMessagesConfig().getString("gui.tag-updated", "&aTag da guilda atualizada!");
                    player.sendMessage(ColorUtils.colorize(successMessage));
                    
                    // 安全刷新GUI
                    plugin.getGuiManager().refreshGUI(player);
                } else {
                    String errorMessage = plugin.getConfigManager().getMessagesConfig().getString("gui.tag-update-failed", "&cFalha ao atualizar tag da guilda!");
                    player.sendMessage(ColorUtils.colorize(errorMessage));
                }
            });
            
            return true;
        });
    }
    
    /**
     * 处理确认
     */
    private void handleConfirm(Player player) {
        // 返回工会设置GUI
        plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
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
