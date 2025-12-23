package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.core.utils.CompatibleScheduler;
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
 * GUI de Entrada de Nome da Guilda
 */
public class GuildNameInputGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private final Player player;
    private String currentName;
    
    public GuildNameInputGUI(GuildPlugin plugin, Guild guild, Player player) {
        this.plugin = plugin;
        this.guild = guild;
        this.player = player;
        this.currentName = guild.getName() != null ? guild.getName() : "";
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&6Modificar Nome da Guilda");
    }
    
    @Override
    public int getSize() {
        return 27;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // Preencher borda
        fillBorder(inventory);
        
        // Exibir nome atual
        displayCurrentName(inventory);
        
        // Adicionar botões de ação
        setupButtons(inventory);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        switch (slot) {
            case 11: // Inserir nome
                handleInputName(player);
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
     * Exibir nome atual
     */
    private void displayCurrentName(Inventory inventory) {
        ItemStack currentNameItem = createItem(
            Material.NAME_TAG,
            ColorUtils.colorize("&eNome Atual da Guilda"),
            ColorUtils.colorize("&7" + (currentName.isEmpty() ? "Sem nome" : currentName))
        );
        inventory.setItem(11, currentNameItem);
    }
    
    /**
     * Configurar botões
     */
    private void setupButtons(Inventory inventory) {
        // Botão de confirmar
        ItemStack confirmButton = createItem(
            Material.EMERALD,
            ColorUtils.colorize("&aConfirmar Modificação"),
            ColorUtils.colorize("&7Clique para confirmar a modificação do nome"),
            ColorUtils.colorize("&7Nota: O nome da guilda será atualizado após relogar")
        );
        inventory.setItem(15, confirmButton);
        
        // Botão de cancelar
        ItemStack cancelButton = createItem(
            Material.REDSTONE,
            ColorUtils.colorize("&cCancelar"),
            ColorUtils.colorize("&7Voltar ao menu anterior")
        );
        inventory.setItem(13, cancelButton);
    }
    
    /**
     * Processar entrada de nome
     */
    private void handleInputName(Player player) {
        // Fechar GUI e entrar no modo de entrada
        plugin.getGuiManager().closeGUI(player);
        plugin.getGuiManager().setInputMode(player, "guild_name_input", this);
        
        // Enviar dica de entrada
        player.sendMessage(ColorUtils.colorize("&6Por favor, digite o novo nome da guilda:"));
        player.sendMessage(ColorUtils.colorize("&7Nome Atual: &f" + currentName));
        player.sendMessage(ColorUtils.colorize("&7Digite &cCancelar &7para cancelar"));
        player.sendMessage(ColorUtils.colorize("&7Suporta códigos de cor, ex: &a&lVerde Negrito &7ou &c&oVermelho Itálico"));
        player.sendMessage(ColorUtils.colorize("&7Nota: O nome da guilda deve ser único"));
    }
    
    /**
     * Processar confirmação
     */
    private void handleConfirm(Player player) {
        // Verificar permissões (apenas o líder pode modificar o nome da guilda)
        if (!plugin.getGuildService().isGuildLeader(player.getUniqueId(), guild.getId())) {
            String message = plugin.getConfigManager().getMessagesConfig().getString("gui.leader-only", "&cApenas o líder da guilda pode fazer isso");
            player.sendMessage(ColorUtils.colorize(message));
            return;
        }
        
        // Se o nome atual estiver vazio, solicitar entrada
        if (currentName.isEmpty()) {
            handleInputName(player);
            return;
        }
        
        // Executar operação de renomeação
        executeNameChange(player, currentName);
    }
    
    /**
     * Processar cancelamento
     */
    public void handleCancel(Player player) {
        // Voltar para a GUI de configurações da guilda
        plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
    }
    
    /**
     * Processar conclusão da entrada
     */
    public void handleInputComplete(Player player, String input) {
        if (input == null || input.trim().isEmpty()) {
            player.sendMessage(ColorUtils.colorize("&cO nome da guilda não pode ser vazio!"));
            plugin.getGuiManager().openGUI(player, this);
            return;
        }
        
        String newName = input.trim();
        
        // Verificar comprimento do nome (baseado no nome limpo, sem códigos de cor)
        String cleanName = newName.replaceAll("§[0-9a-fk-or]", "").replaceAll("&[0-9a-fk-or]", "");
        if (cleanName.length() < 2) {
            player.sendMessage(ColorUtils.colorize("&cO nome da guilda deve ter pelo menos 2 caracteres (sem cores)!"));
            plugin.getGuiManager().openGUI(player, this);
            return;
        }
        
        if (cleanName.length() > 16) {
            player.sendMessage(ColorUtils.colorize("&cO nome da guilda não pode exceder 16 caracteres (sem cores)!"));
            plugin.getGuiManager().openGUI(player, this);
            return;
        }
        
        // Verificar se é igual ao nome atual
        if (newName.equalsIgnoreCase(currentName)) {
            player.sendMessage(ColorUtils.colorize("&cO novo nome é igual ao atual!"));
            plugin.getGuiManager().openGUI(player, this);
            return;
        }
        
        // Verificar formato do nome (permite letras, números e códigos de cor)
        if (!cleanName.matches("^[\\u4e00-\\u9fa5a-zA-Z0-9]+$")) {
            player.sendMessage(ColorUtils.colorize("&cO nome da guilda deve conter apenas letras e números!"));
            plugin.getGuiManager().openGUI(player, this);
            return;
        }
        
        // Executar operação de renomeação
        executeNameChange(player, newName);
    }
    
    /**
     * Executar operação de renomeação
     */
    private void executeNameChange(Player player, String newName) {
        // Verificar assincronamente se o nome está disponível
        plugin.getGuildService().getGuildByNameAsync(newName).thenAccept(existingGuild -> {
            if (existingGuild != null) {
                // Nome já existe
                CompatibleScheduler.runTask(plugin, () -> {
                    player.sendMessage(ColorUtils.colorize("&cNome da guilda &f" + newName + " &cjá está em uso!"));
                    plugin.getGuiManager().openGUI(player, this);
                });
                return;
            }
            
            // Nome disponível, executar atualização
            plugin.getGuildService().updateGuildAsync(guild.getId(), newName, guild.getTag(), guild.getDescription(), player.getUniqueId())
                .thenAccept(success -> {
                    CompatibleScheduler.runTask(plugin, () -> {
                        if (success) {
                            // Atualização bem-sucedida
                            player.sendMessage(ColorUtils.colorize("&aNome da guilda modificado com sucesso!"));
                            player.sendMessage(ColorUtils.colorize("&7Novo Nome: &f" + newName));
                            
                            // Registrar log
                            plugin.getGuildService().logGuildActionAsync(
                                guild.getId(),
                                guild.getName(),
                                player.getUniqueId().toString(),
                                player.getName(),
                                com.guild.models.GuildLog.LogType.GUILD_RENAMED,
                                "Nome da guilda alterado de " + currentName + " para " + newName,
                                "Nome original: " + currentName + ", Novo nome: " + newName
                            );
                            
                            // Obter novamente as informações mais recentes da guilda
                            plugin.getGuildService().getGuildByIdAsync(guild.getId()).thenAccept(updatedGuild -> {
                                if (updatedGuild != null) {
                                    // Voltar para a GUI de configurações da guilda (usando informações atualizadas)
                                    plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, updatedGuild));
                                } else {
                                    // Se falhar ao obter, usar objeto atualizado localmente
                                    guild.setName(newName);
                                    plugin.getGuiManager().openGUI(player, new GuildSettingsGUI(plugin, guild));
                                }
                            });
                        } else {
                            // Falha na atualização
                            player.sendMessage(ColorUtils.colorize("&cFalha ao modificar nome da guilda! Tente novamente"));
                            plugin.getGuiManager().openGUI(player, this);
                        }
                    });
                });
        });
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
    
    @Override
    public void onClose(Player player) {
        // Processamento ao fechar
    }
    
    @Override
    public void refresh(Player player) {
        // Atualizar GUI
        plugin.getGuiManager().openGUI(player, this);
    }
}
