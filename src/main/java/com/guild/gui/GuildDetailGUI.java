package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.models.Guild;
import com.guild.models.GuildMember;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GUI de Detalhes da Guilda
 */
public class GuildDetailGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private final Player viewer;
    private List<GuildMember> members = new ArrayList<>();
    
    public GuildDetailGUI(GuildPlugin plugin, Guild guild, Player viewer) {
        this.plugin = plugin;
        this.guild = guild;
        this.viewer = viewer;
        loadMembers();
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize("&6Detalhes da Guilda - " + guild.getName());
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // Preencher borda
        fillBorder(inventory);
        
        // Configurar informações básicas da guilda
        setupGuildInfo(inventory);
        
        // Configurar lista de membros da guilda
        setupMembersList(inventory);
        
        // Configurar botões de ação
        setupActionButtons(inventory);
    }
    
    private void setupGuildInfo(Inventory inventory) {
        // Nome e tag da guilda - Centralizado no topo
        List<String> guildLore = new ArrayList<>();
        guildLore.add(ColorUtils.colorize("&7ID: " + guild.getId()));
        guildLore.add(ColorUtils.colorize("&7Tag: [" + (guild.getTag() != null ? guild.getTag() : "Nenhuma") + "]"));
        guildLore.add(ColorUtils.colorize("&7Criada em: " + formatTime(guild.getCreatedAt())));
        guildLore.add(ColorUtils.colorize("&7Status: " + (guild.isFrozen() ? "&cCongelada" : "&aNormal")));
        
        inventory.setItem(4, createItem(Material.SHIELD, ColorUtils.colorize("&6" + guild.getName()), guildLore.toArray(new String[0])));
        
        // Nível e fundos da guilda - Na segunda linha
        List<String> economyLore = new ArrayList<>();
        economyLore.add(ColorUtils.colorize("&7Nível Atual: &e" + guild.getLevel()));
        economyLore.add(ColorUtils.colorize("&7Máx Membros: &e" + guild.getMaxMembers()));
        economyLore.add(ColorUtils.colorize("&7Membros Atuais: &e" + members.size()));
        
        inventory.setItem(19, createItem(Material.EXPERIENCE_BOTTLE, ColorUtils.colorize("&eInformações da Guilda"), economyLore.toArray(new String[0])));
        
        // Líder da guilda - Na segunda linha
        List<String> leaderLore = new ArrayList<>();
        leaderLore.add(ColorUtils.colorize("&7Líder: &e" + guild.getLeaderName()));
        leaderLore.add(ColorUtils.colorize("&7UUID: &7" + guild.getLeaderUuid()));
        
        inventory.setItem(21, createItem(Material.GOLDEN_HELMET, ColorUtils.colorize("&6Líder da Guilda"), leaderLore.toArray(new String[0])));
        
        // Descrição da guilda - Na segunda linha
        List<String> descLore = new ArrayList<>();
        String description = guild.getDescription();
        if (description != null && !description.isEmpty()) {
            descLore.add(ColorUtils.colorize("&7" + description));
        } else {
            descLore.add(ColorUtils.colorize("&7Sem descrição"));
        }
        
        inventory.setItem(23, createItem(Material.BOOK, ColorUtils.colorize("&eDescrição da Guilda"), descLore.toArray(new String[0])));
    }
    
    private void setupMembersList(Inventory inventory) {
        // Título da lista de membros - Centralizado na terceira linha
        inventory.setItem(27, createItem(Material.PLAYER_HEAD, ColorUtils.colorize("&aMembros da Guilda"), 
            ColorUtils.colorize("&7Total " + members.size() + " membros")));
        
        // Exibir os primeiros 6 membros - Na terceira e quarta linha
        int maxDisplay = Math.min(6, members.size());
        for (int i = 0; i < maxDisplay; i++) {
            GuildMember member = members.get(i);
            int slot = 28 + i;
            
            List<String> memberLore = new ArrayList<>();
            memberLore.add(ColorUtils.colorize("&7Cargo: " + getRoleDisplayName(member.getRole())));
            memberLore.add(ColorUtils.colorize("&7Entrou em: " + formatTime(member.getJoinedAt())));
            memberLore.add(ColorUtils.colorize("&7Status Online: " + (isPlayerOnline(member.getPlayerUuid()) ? "&aOnline" : "&7Offline")));
            
            inventory.setItem(slot, createPlayerHead(member.getPlayerName(), memberLore.toArray(new String[0])));
        }
        
        // Se houver mais de 6 membros, exibir mais informações
        if (members.size() > 6) {
            inventory.setItem(34, createItem(Material.PAPER, ColorUtils.colorize("&eMais Membros"), 
                ColorUtils.colorize("&7Mais " + (members.size() - 6) + " membros não exibidos")));
        }
    }
    
    private void setupActionButtons(Inventory inventory) {
        // Botão de voltar - No canto inferior esquerdo
        inventory.setItem(45, createItem(Material.ARROW, ColorUtils.colorize("&cVoltar")));
        
        // Botões de gerenciamento - Centralizado na parte inferior
        if (viewer.hasPermission("guild.admin")) {
            // Botão de congelar/descongelar
            String freezeText = guild.isFrozen() ? "&aDescongelar Guilda" : "&cCongelar Guilda";
            String freezeLore = guild.isFrozen() ? "&7Clique para descongelar a guilda" : "&7Clique para congelar a guilda";
            inventory.setItem(47, createItem(Material.ICE, ColorUtils.colorize(freezeText), ColorUtils.colorize(freezeLore)));
            
            // Botão de excluir guilda
            inventory.setItem(49, createItem(Material.TNT, ColorUtils.colorize("&4Excluir Guilda"), 
                ColorUtils.colorize("&7Clique para excluir a guilda")));
            
            // Botão de gerenciamento de fundos
            inventory.setItem(51, createItem(Material.GOLD_BLOCK, ColorUtils.colorize("&eGerenciamento de Fundos"), 
                ColorUtils.colorize("&7Gerenciar fundos da guilda")));
        }
        
        // Botão de atualizar - No canto inferior direito
        inventory.setItem(53, createItem(Material.EMERALD, ColorUtils.colorize("&aAtualizar Informações")));
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
    
    private void loadMembers() {
        plugin.getGuildService().getGuildMembersAsync(guild.getId()).thenAccept(membersList -> {
            this.members = membersList != null ? membersList : new ArrayList<>();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (viewer.isOnline()) {
                    refresh(viewer);
                }
            });
        });
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        if (slot == 45) {
            // Voltar
            plugin.getGuiManager().openGUI(player, new GuildListManagementGUI(plugin, player));
        } else if (slot == 53) {
            // Atualizar
            loadMembers();
        } else if (slot == 47 && player.hasPermission("guild.admin")) {
            // Congelar/Descongelar guilda
            toggleGuildFreeze(player);
        } else if (slot == 49 && player.hasPermission("guild.admin")) {
            // Excluir guilda
            deleteGuild(player);
        }
    }
    
    private void toggleGuildFreeze(Player player) {
        boolean newStatus = !guild.isFrozen();
        plugin.getGuildService().updateGuildFrozenStatusAsync(guild.getId(), newStatus).thenAccept(success -> {
            if (success) {
                String message = newStatus ? "&aGuilda " + guild.getName() + " foi congelada!" : "&aGuilda " + guild.getName() + " foi descongelada!";
                player.sendMessage(ColorUtils.colorize(message));
                // Atualizar objeto guild local
                guild.setFrozen(newStatus);
                refresh(player);
            } else {
                player.sendMessage(ColorUtils.colorize("&cOperação falhou!"));
            }
        });
    }
    
    private void deleteGuild(Player player) {
        // Confirmar exclusão
        player.sendMessage(ColorUtils.colorize("&cTem certeza que deseja excluir a guilda " + guild.getName() + "?"));
        player.sendMessage(ColorUtils.colorize("&cDigite &f/guildadmin delete " + guild.getName() + " confirm &cpara confirmar exclusão"));
        player.closeInventory();
    }
    
    private String formatTime(java.time.LocalDateTime dateTime) {
        if (dateTime == null) return "Desconhecido";
        return dateTime.format(com.guild.core.time.TimeProvider.FULL_FORMATTER);
    }
    
    private String getRoleDisplayName(GuildMember.Role role) {
        switch (role) {
            case LEADER: return "&6Líder";
            case OFFICER: return "&eOficial";
            case MEMBER: return "&7Membro";
            default: return "&7Desconhecido";
        }
    }
    
    private boolean isPlayerOnline(java.util.UUID uuid) {
        return Bukkit.getPlayer(uuid) != null;
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
    
    private ItemStack createPlayerHead(String playerName, String... lore) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(ColorUtils.colorize("&e" + playerName));
            
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            
            // Tentar definir cabeça do jogador
            try {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(playerName));
            } catch (Exception e) {
                // Se falhar, usar cabeça padrão
            }
            
            head.setItemMeta(meta);
        }
        
        return head;
    }
    
    @Override
    public void onClose(Player player) {
        // Processamento ao fechar
    }
    
    @Override
    public void refresh(Player player) {
        if (player.isOnline()) {
            plugin.getGuiManager().refreshGUI(player);
        }
    }
}
