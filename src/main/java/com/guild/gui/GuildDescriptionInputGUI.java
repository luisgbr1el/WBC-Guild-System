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
 * GUI de Entrada de Descrição da Guilda
 */
public class GuildDescriptionInputGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private String currentDescription;
    
    public GuildDescriptionInputGUI(GuildPlugin plugin, Guild guild) {
        this.plugin = plugin;
        this.guild = guild;
        this.currentDescription = guild.getDescription() != null ? guild.getDescription() : "";
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&6Modificar Descrição da Guilda");
    }
    
    @Override
    public int getSize() {
        return 27;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // Preencher borda
        fillBorder(inventory);
        
        // Exibir descrição atual
        displayCurrentDescription(inventory);
        
        // Adicionar botões de ação
        setupButtons(inventory);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        switch (slot) {
            case 11: // Inserir descrição
                handleInputDescription(player);
                break;
            case 15: // Confirmar
                handleConfirm(player);
                break;
            case 13: // Cancelar
                handleCancel(player);
                break;
        }
    }
    
    /**
     * Preencher borda
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
     * Exibir descrição atual
     */
    private void displayCurrentDescription(Inventory inventory) {
        ItemStack currentDesc = createItem(
            Material.BOOK,
            ColorUtils.colorize("&eDescrição Atual"),
            ColorUtils.colorize("&7" + (currentDescription.isEmpty() ? "Sem descrição" : currentDescription))
        );
        inventory.setItem(11, currentDesc);
    }
    
    /**
     * Configurar botões
     */
    private void setupButtons(Inventory inventory) {
        // Botão de confirmar
        ItemStack confirm = createItem(
            Material.EMERALD_BLOCK,
            ColorUtils.colorize("&aConfirmar Modificação"),
            ColorUtils.colorize("&7Confirmar modificação da descrição da guilda")
        );
        inventory.setItem(15, confirm);
        
        // Botão de cancelar
        ItemStack cancel = createItem(
            Material.REDSTONE_BLOCK,
            ColorUtils.colorize("&cCancelar"),
            ColorUtils.colorize("&7Cancelar modificação")
        );
        inventory.setItem(13, cancel);
    }
    
    /**
     * Tratar entrada de descrição
     */
    private void handleInputDescription(Player player) {
        // Fechar GUI
        player.closeInventory();
        
        // Enviar mensagem solicitando entrada
        String message = plugin.getConfigManager().getMessagesConfig().getString("gui.input-description", "&aPor favor, digite a nova descrição da guilda no chat (máx 100 caracteres):");
        player.sendMessage(ColorUtils.colorize(message));
        
        // Definir jogador para modo de entrada
        plugin.getGuiManager().setInputMode(player, input -> {
            if (input.length() > 100) {
                String errorMessage = plugin.getConfigManager().getMessagesConfig().getString("gui.description-too-long", "&cDescrição muito longa, máximo de 100 caracteres!");
                player.sendMessage(ColorUtils.colorize(errorMessage));
                return false;
            }
            
            // Atualizar descrição
            currentDescription = input;
            
            // Salvar no banco de dados
            plugin.getGuildService().updateGuildDescriptionAsync(guild.getId(), input).thenAccept(success -> {
                if (success) {
                    String successMessage = plugin.getConfigManager().getMessagesConfig().getString("gui.description-updated", "&aDescrição da guilda atualizada!");
                    player.sendMessage(ColorUtils.colorize(successMessage));
                    
                    // Atualizar GUI com segurança
                    plugin.getGuiManager().refreshGUI(player);
                } else {
                    String errorMessage = plugin.getConfigManager().getMessagesConfig().getString("gui.description-update-failed", "&cFalha ao atualizar descrição da guilda!");
                    player.sendMessage(ColorUtils.colorize(errorMessage));
                }
            });
            
            return true;
        });
    }
    
    /**
     * Tratar confirmação
     */
    private void handleConfirm(Player player) {
        // Voltar para GUI de configurações da guilda
        plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
    }
    
    /**
     * Tratar cancelamento
     */
    private void handleCancel(Player player) {
        // Voltar para GUI de configurações da guilda
        plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
    }
    
    /**
     * Criar item
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
