package com.guild.gui;

import com.guild.GuildPlugin;
import com.guild.core.gui.GUI;
import com.guild.core.utils.ColorUtils;
import com.guild.core.utils.CompatibleScheduler;
import com.guild.models.Guild;
import com.guild.models.GuildLog;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * GUI de Visualização de Logs da Guilda
 */
public class GuildLogsGUI implements GUI {
    
    private final GuildPlugin plugin;
    private final Guild guild;
    private final Player player;
    private final int page;
    private final int itemsPerPage = 28; // 2-8列，2-5行
    private List<GuildLog> logs;
    private int totalLogs;
    
    public GuildLogsGUI(GuildPlugin plugin, Guild guild, Player player) {
        this(plugin, guild, player, 0);
    }
    
    public GuildLogsGUI(GuildPlugin plugin, Guild guild, Player player, int page) {
        this.plugin = plugin;
        this.guild = guild;
        this.player = player;
        this.page = page;
    }
    
    @Override
    public String getTitle() {
        return ColorUtils.colorize(plugin.getConfigManager().getGuiConfig().getString("guild-logs.title", "&6Logs da Guilda - {guild_name}")
            .replace("{guild_name}", guild.getName()));
    }
    
    @Override
    public int getSize() {
        return 54;
    }
    
    @Override
    public void setupInventory(Inventory inventory) {
        // Preencher borda
        fillBorder(inventory);
        
        // Carregar dados de log assincronamente
        loadLogsAsync().thenAccept(success -> {
            if (success) {
                // Configurar itens e botões de navegação completos na thread principal
                CompatibleScheduler.runTask(plugin, () -> {
                    setupLogItems(inventory);
                    setupBasicNavigationButtons(inventory);
                    setupFullNavigationButtons(inventory);
                });
            } else {
                // Se o carregamento falhar, exibir mensagem de erro na thread principal
                CompatibleScheduler.runTask(plugin, () -> {
                    ItemStack errorItem = createItem(
                        Material.BARRIER,
                        ColorUtils.colorize("&cFalha ao Carregar"),
                        ColorUtils.colorize("&7Não foi possível carregar os logs, tente novamente")
                    );
                    inventory.setItem(22, errorItem);
                    setupBasicNavigationButtons(inventory);
                });
            }
        });
    }
    
    /**
     * 异步加载日志数据
     */
    private CompletableFuture<Boolean> loadLogsAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                plugin.getLogger().info("Iniciando carregamento de dados de log da guilda " + guild.getName() + " ...");
                
                // Verificar se o ID da guilda é válido
                if (guild.getId() <= 0) {
                    plugin.getLogger().warning("ID da guilda inválido: " + guild.getId());
                    return false;
                }
                
                // Obter número total de logs
                totalLogs = plugin.getGuildService().getGuildLogsCountAsync(guild.getId()).get();
                plugin.getLogger().info("Guilda " + guild.getName() + " tem um total de " + totalLogs + " registros de log");
                
                // Obter logs da página atual
                int offset = page * itemsPerPage;
                logs = plugin.getGuildService().getGuildLogsAsync(guild.getId(), itemsPerPage, offset).get();
                plugin.getLogger().info("Sucesso ao carregar a página " + (page + 1) + " com " + logs.size() + " registros de log");
                
                return true;
            } catch (Exception e) {
                plugin.getLogger().severe("Erro ao carregar logs da guilda: " + e.getMessage());
                e.printStackTrace();
                
                // Definir valores padrão
                totalLogs = 0;
                logs = new java.util.ArrayList<>();
                
                return false;
            }
        });
    }
    
    /**
     * 设置日志物品
     */
    private void setupLogItems(Inventory inventory) {
        plugin.getLogger().info("Configurando itens de log, tamanho dos logs: " + (logs != null ? logs.size() : "null"));
        
        if (logs == null) {
            logs = new java.util.ArrayList<>(); // Garantir que logs não seja null
        }
        
        if (logs.isEmpty()) {
            plugin.getLogger().info("Lista de logs vazia, exibindo mensagem de sem logs");
            // Exibir mensagem de sem logs
            ItemStack noLogs = createItem(
                Material.BARRIER,
                ColorUtils.colorize("&cSem Logs"),
                ColorUtils.colorize("&7Esta guilda não tem registros de operação"),
                ColorUtils.colorize("&7Aguarde atividades da guilda para gerar logs")
            );
            inventory.setItem(22, noLogs);
            return;
        }
        
        plugin.getLogger().info("Iniciando exibição de " + logs.size() + " registros de log");
        
        // Exibir lista de logs
        for (int i = 0; i < Math.min(logs.size(), itemsPerPage); i++) {
            GuildLog log = logs.get(i);
            int slot = getLogSlot(i);
            
            plugin.getLogger().info("Configurando item de log " + i + " no slot " + slot + ": " + log.getLogType().getDisplayName());
            
            ItemStack logItem = createLogItem(log);
            inventory.setItem(slot, logItem);
        }
    }
    
    /**
     * 创建日志物品
     */
    private ItemStack createLogItem(GuildLog log) {
        Material material = getLogMaterial(log.getLogType());
        String name = ColorUtils.colorize("&e" + log.getLogType().getDisplayName());
        
        List<String> lore = new java.util.ArrayList<>();
        lore.add(ColorUtils.colorize("&7Operador: &f" + log.getPlayerName()));
        lore.add(ColorUtils.colorize("&7Tempo: &f" + log.getSimpleTime()));
        lore.add(ColorUtils.colorize("&7Descrição: &f" + log.getDescription()));
        
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            lore.add(ColorUtils.colorize("&7Detalhes: &f" + log.getDetails()));
        }
        
        return createItem(material, name, lore.toArray(new String[0]));
    }
    
    /**
     * 根据日志类型获取物品材质
     */
    private Material getLogMaterial(GuildLog.LogType logType) {
        switch (logType) {
            case GUILD_CREATED:
                return Material.GREEN_WOOL;
            case GUILD_DISSOLVED:
                return Material.RED_WOOL;
            case MEMBER_JOINED:
                return Material.EMERALD;
            case MEMBER_LEFT:
                return Material.REDSTONE;
            case MEMBER_KICKED:
                return Material.REDSTONE;
            case MEMBER_PROMOTED:
                return Material.GOLD_INGOT;
            case MEMBER_DEMOTED:
                return Material.IRON_INGOT;
            case LEADER_TRANSFERRED:
                return Material.DIAMOND;
            case RELATION_CREATED:
            case RELATION_ACCEPTED:
                return Material.BLUE_WOOL;
            case RELATION_DELETED:
            case RELATION_REJECTED:
                return Material.ORANGE_WOOL;
            case GUILD_FROZEN:
                return Material.ICE;
            case GUILD_UNFROZEN:
                return Material.WATER_BUCKET;
            case GUILD_LEVEL_UP:
                return Material.EXPERIENCE_BOTTLE;
            case APPLICATION_SUBMITTED:
            case APPLICATION_ACCEPTED:
            case APPLICATION_REJECTED:
                return Material.PAPER;
            case INVITATION_SENT:
            case INVITATION_ACCEPTED:
            case INVITATION_REJECTED:
                return Material.BOOK;
            default:
                return Material.GRAY_WOOL;
        }
    }
    
    /**
     * 获取日志物品的槽位 - 修复后的计算逻辑
     */
    private int getLogSlot(int index) {
        int row = index / 7; // 7 colunas
        int col = index % 7;
        return (row + 1) * 9 + (col + 1); // Começando da linha 1, coluna 1 (slots 10-43)
    }
    
    /**
     * 设置基本的导航按钮（不依赖日志数据）
     */
    private void setupBasicNavigationButtons(Inventory inventory) {
        // Botão de voltar - Movido para o slot 49, consistente com outras GUIs
        ItemStack backButton = createItem(
            Material.ARROW,
            ColorUtils.colorize("&cVoltar"),
            ColorUtils.colorize("&7Voltar ao menu anterior")
        );
        inventory.setItem(49, backButton);
    }
    
    /**
     * 设置完整的导航按钮（依赖日志数据）
     */
    private void setupFullNavigationButtons(Inventory inventory) {
        // Botões de paginação
        if (page > 0) {
            ItemStack prevButton = createItem(
                Material.ARROW,
                ColorUtils.colorize("&ePágina Anterior"),
                ColorUtils.colorize("&7Ver logs anteriores")
            );
            inventory.setItem(45, prevButton);
        }
        
        if ((page + 1) * itemsPerPage < totalLogs) {
            ItemStack nextButton = createItem(
                Material.ARROW,
                ColorUtils.colorize("&ePróxima Página"),
                ColorUtils.colorize("&7Ver próximos logs")
            );
            inventory.setItem(53, nextButton);
        }
        
        // Informações da página
        ItemStack pageInfo = createItem(
            Material.PAPER,
            ColorUtils.colorize("&6Informações da Página"),
            ColorUtils.colorize("&7Página Atual: &f" + (page + 1)),
            ColorUtils.colorize("&7Total de Páginas: &f" + ((totalLogs - 1) / itemsPerPage + 1)),
            ColorUtils.colorize("&7Total de Registros: &f" + totalLogs)
        );
        inventory.setItem(47, pageInfo);
        
        // Botão de atualizar
        ItemStack refreshButton = createItem(
            Material.EMERALD,
            ColorUtils.colorize("&aAtualizar"),
            ColorUtils.colorize("&7Atualizar lista de logs")
        );
        inventory.setItem(51, refreshButton);
    }
    
    @Override
    public void onClick(Player player, int slot, ItemStack clickedItem, ClickType clickType) {
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;
        
        String itemName = clickedItem.getItemMeta().getDisplayName();
        
        // Botão de voltar
        if (itemName.contains("Voltar")) {
            // Voltar para a GUI de informações da guilda
            GuildInfoGUI guildInfoGUI = new GuildInfoGUI(plugin, player, guild);
            plugin.getGuiManager().openGUI(player, guildInfoGUI);
            return;
        }
        
        // Botão de página anterior
        if (itemName.contains("Página Anterior")) {
            if (page > 0) {
                GuildLogsGUI prevPageGUI = new GuildLogsGUI(plugin, guild, player, page - 1);
                plugin.getGuiManager().openGUI(player, prevPageGUI);
            }
            return;
        }
        
        // Botão de próxima página
        if (itemName.contains("Próxima Página")) {
            if ((page + 1) * itemsPerPage < totalLogs) {
                GuildLogsGUI nextPageGUI = new GuildLogsGUI(plugin, guild, player, page + 1);
                plugin.getGuiManager().openGUI(player, nextPageGUI);
            }
            return;
        }
        
        // Botão de atualizar
        if (itemName.contains("Atualizar")) {
            GuildLogsGUI refreshGUI = new GuildLogsGUI(plugin, guild, player, page);
            plugin.getGuiManager().openGUI(player, refreshGUI);
            return;
        }
        
        // Clique no item de log - Verificar se está na área de exibição de log
        if (slot >= 10 && slot <= 43) {
            int row = slot / 9;
            int col = slot % 9;
            if (row >= 1 && row <= 4 && col >= 1 && col <= 7) {
                int relativeIndex = (row - 1) * 7 + (col - 1);
                int logIndex = (page * itemsPerPage) + relativeIndex;
                if (logIndex < logs.size()) {
                    GuildLog log = logs.get(logIndex);
                    handleLogClick(player, log);
                }
            }
        }
    }
    
    /**
     * 处理日志点击
     */
    private void handleLogClick(Player player, GuildLog log) {
        // Exibir detalhes do log
        String message = ColorUtils.colorize("&6=== Detalhes do Log ===");
        player.sendMessage(message);
        player.sendMessage(ColorUtils.colorize("&7Tipo: &f" + log.getLogType().getDisplayName()));
        player.sendMessage(ColorUtils.colorize("&7Operador: &f" + log.getPlayerName()));
        player.sendMessage(ColorUtils.colorize("&7Tempo: &f" + log.getSimpleTime()));
        player.sendMessage(ColorUtils.colorize("&7Descrição: &f" + log.getDescription()));
        if (log.getDetails() != null && !log.getDetails().isEmpty()) {
            player.sendMessage(ColorUtils.colorize("&7Detalhes: &f" + log.getDetails()));
        }
        player.sendMessage(ColorUtils.colorize("&6=================="));
    }
    
    @Override
    public void onClose(Player player) {
        // Processamento ao fechar
    }
    
    @Override
    public void refresh(Player player) {
        // Atualizar GUI
        GuildLogsGUI refreshGUI = new GuildLogsGUI(plugin, guild, player, page);
        plugin.getGuiManager().openGUI(player, refreshGUI);
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
        org.bukkit.inventory.meta.ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            meta.setDisplayName(name);
            if (lore.length > 0) {
                meta.setLore(java.util.Arrays.asList(lore));
            }
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
