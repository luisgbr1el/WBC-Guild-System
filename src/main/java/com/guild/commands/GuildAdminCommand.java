package com.guild.commands;
import com.guild.GuildPlugin;
import com.guild.core.utils.ColorUtils;
import com.guild.gui.AdminGuildGUI;
import com.guild.gui.RelationManagementGUI;
import com.guild.models.Guild;
import com.guild.models.GuildRelation;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
public class GuildAdminCommand implements CommandExecutor, TabCompleter {
    private final GuildPlugin plugin;
    public GuildAdminCommand(GuildPlugin plugin) {
        this.plugin = plugin;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("guild.admin")) {
            sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessagesConfig().getString("general.no-permission", "&cVocê não tem permissão para realizar esta operação!")));
            return true;
        }
        if (args.length == 0) {
            if (sender instanceof Player player) {
                AdminGuildGUI adminGUI = new AdminGuildGUI(plugin);
                plugin.getGuiManager().openGUI(player, adminGUI);
            } else {
                handleHelp(sender);
            }
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "list":
                handleList(sender, args);
                break;
            case "info":
                handleInfo(sender, args);
                break;
            case "delete":
                handleDelete(sender, args);
                break;
            case "freeze":
                handleFreeze(sender, args);
                break;
            case "unfreeze":
                handleUnfreeze(sender, args);
                break;
            case "transfer":
                handleTransfer(sender, args);
                break;
            case "relation":
                handleRelation(sender, args);
                break;
            case "reload":
                handleReload(sender);
                break;
            case "test":
                handleTest(sender, args);
                break;
            case "help":
                handleHelp(sender);
                break;
            default:
                sender.sendMessage(ColorUtils.colorize(plugin.getConfigManager().getMessagesConfig().getString("general.unknown-command", "&cComando desconhecido! Use /guildadmin help para ver a ajuda.")));
                break;
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("guild.admin")) {
            return completions;
        }
        if (args.length == 1) {
            completions.addAll(Arrays.asList("list", "info", "delete", "freeze", "unfreeze", "transfer", "relation", "reload", "help"));
        } else if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "info":
                case "delete":
                case "freeze":
                case "unfreeze":
                case "transfer":
                    plugin.getGuildService().getAllGuildsAsync().thenAccept(guilds -> {
                        for (Guild guild : guilds) {
                            completions.add(guild.getName());
                        }
                    });
                    break;
                case "relation":
                    completions.addAll(Arrays.asList("list", "create", "delete", "gui"));
                    break;
            }
        } else if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "transfer":
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        completions.add(player.getName());
                    }
                    break;
                case "relation":
                    if ("create".equals(args[1])) {
                        plugin.getGuildService().getAllGuildsAsync().thenAccept(guilds -> {
                            for (Guild guild : guilds) {
                                completions.add(guild.getName());
                            }
                        });
                    }
                    break;
            }
        } else if (args.length == 4) {
            switch (args[0].toLowerCase()) {
                case "relation":
                    if ("create".equals(args[1])) {
                        plugin.getGuildService().getAllGuildsAsync().thenAccept(guilds -> {
                            for (Guild guild : guilds) {
                                completions.add(guild.getName());
                            }
                        });
                    }
                    break;
            }
        } else if (args.length == 5) {
            switch (args[0].toLowerCase()) {
                case "relation":
                    if ("create".equals(args[1])) {
                        completions.addAll(Arrays.asList("ally", "enemy", "war", "truce", "neutral"));
                    }
                    break;
            }
        }
        return completions;
    }
    private void handleList(CommandSender sender, String[] args) {
        plugin.getGuildService().getAllGuildsAsync().thenAccept(guilds -> {
            sender.sendMessage(ColorUtils.colorize("&6=== Lista de Guildas ==="));
            if (guilds.isEmpty()) {
                sender.sendMessage(ColorUtils.colorize("&cSem guildas"));
                return;
            }
            for (Guild guild : guilds) {
                String status = guild.isFrozen() ? "&c[Congelado]" : "&a[Normal]";
                sender.sendMessage(ColorUtils.colorize(String.format("&e%s &7- Líder: &f%s &7- Nível: &f%d &7%s", 
                    guild.getName(), guild.getLeaderName(), guild.getLevel(), status)));
            }
        });
    }
    private void handleInfo(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin info <Nome da Guilda>"));
            return;
        }
        String guildName = args[1];
        plugin.getGuildService().getGuildByNameAsync(guildName).thenAccept(guild -> {
            if (guild == null) {
                sender.sendMessage(ColorUtils.colorize("&cGuilda " + guildName + " não existe!"));
                return;
            }
            sender.sendMessage(ColorUtils.colorize("&6=== Informações da Guilda ==="));
            sender.sendMessage(ColorUtils.colorize("&eNome: &f" + guild.getName()));
            sender.sendMessage(ColorUtils.colorize("&eTag: &f" + (guild.getTag() != null ? guild.getTag() : "Nenhum")));
            sender.sendMessage(ColorUtils.colorize("&eLíder: &f" + guild.getLeaderName()));
            sender.sendMessage(ColorUtils.colorize("&eNível: &f" + guild.getLevel()));
            sender.sendMessage(ColorUtils.colorize("&eSaldo: &f" + guild.getBalance()));
            sender.sendMessage(ColorUtils.colorize("&eStatus: &f" + (guild.isFrozen() ? "Congelado" : "Normal")));
            plugin.getGuildService().getGuildMemberCountAsync(guild.getId()).thenAccept(count -> {
                sender.sendMessage(ColorUtils.colorize("&eMembros: &f" + count + "/" + guild.getMaxMembers()));
            });
        });
    }
    private void handleDelete(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin delete <Nome da Guilda>"));
            return;
        }
        String guildName = args[1];
        plugin.getGuildService().getGuildByNameAsync(guildName).thenAccept(guild -> {
            if (guild == null) {
                sender.sendMessage(ColorUtils.colorize("&cGuilda " + guildName + " não existe!"));
                return;
            }
            plugin.getGuildService().deleteGuildAsync(guild.getId(), UUID.randomUUID()).thenAccept(success -> {
                if (success) {
                    sender.sendMessage(ColorUtils.colorize("&aGuilda " + guildName + " foi deletada à força!"));
                } else {
                    sender.sendMessage(ColorUtils.colorize("&cFalha ao deletar guilda!"));
                }
            });
        });
    }
    private void handleFreeze(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin freeze <Nome da Guilda>"));
            return;
        }
        String guildName = args[1];
        plugin.getGuildService().getGuildByNameAsync(guildName).thenAccept(guild -> {
            if (guild == null) {
                sender.sendMessage(ColorUtils.colorize("&cGuilda " + guildName + " não existe!"));
                return;
            }
            sender.sendMessage(ColorUtils.colorize("&aGuilda " + guildName + " foi congelada!"));
        });
    }
    private void handleUnfreeze(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin unfreeze <Nome da Guilda>"));
            return;
        }
        String guildName = args[1];
        plugin.getGuildService().getGuildByNameAsync(guildName).thenAccept(guild -> {
            if (guild == null) {
                sender.sendMessage(ColorUtils.colorize("&cGuilda " + guildName + " não existe!"));
                return;
            }
            sender.sendMessage(ColorUtils.colorize("&aGuilda " + guildName + " foi descongelada!"));
        });
    }
    private void handleTransfer(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin transfer <Nome da Guilda> <Novo Líder>"));
            return;
        }
        String guildName = args[1];
        String newLeaderName = args[2];
        Player newLeader = Bukkit.getPlayer(newLeaderName);
        if (newLeader == null) {
            sender.sendMessage(ColorUtils.colorize("&cJogador " + newLeaderName + " não está online!"));
            return;
        }
        plugin.getGuildService().getGuildByNameAsync(guildName).thenAccept(guild -> {
            if (guild == null) {
                sender.sendMessage(ColorUtils.colorize("&cGuilda " + guildName + " não existe!"));
                return;
            }
            plugin.getGuildService().getGuildMemberAsync(guild.getId(), newLeader.getUniqueId()).thenAccept(member -> {
                if (member == null) {
                    sender.sendMessage(ColorUtils.colorize("&cJogador " + newLeaderName + " não é membro desta guilda!"));
                    return;
                }
                sender.sendMessage(ColorUtils.colorize("&aLiderança da guilda " + guildName + " transferida para " + newLeaderName + "!"));
            });
        });
    }
    private void handleRelation(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin relation <list|create|delete|gui>"));
            return;
        }
        switch (args[1].toLowerCase()) {
            case "gui":
                if (sender instanceof Player player) {
                    RelationManagementGUI relationGUI = new RelationManagementGUI(plugin, player);
                    plugin.getGuiManager().openGUI(player, relationGUI);
                } else {
                    sender.sendMessage(ColorUtils.colorize("&cEste comando só pode ser executado por jogadores!"));
                }
                break;
            case "list":
                sender.sendMessage(ColorUtils.colorize("&6=== Lista de Relações da Guilda ==="));
                plugin.getGuildService().getAllGuildsAsync().thenCompose(guilds -> {
                    List<CompletableFuture<List<GuildRelation>>> relationFutures = new ArrayList<>();
                    for (Guild guild : guilds) {
                        relationFutures.add(plugin.getGuildService().getGuildRelationsAsync(guild.getId()));
                    }
                    return CompletableFuture.allOf(relationFutures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> {
                            List<GuildRelation> allRelations = new ArrayList<>();
                            for (CompletableFuture<List<GuildRelation>> future : relationFutures) {
                                try {
                                    allRelations.addAll(future.get());
                                } catch (Exception e) {
                                    plugin.getLogger().warning("获取工会关系时发生错误: " + e.getMessage());
                                }
                            }
                            return allRelations;
                        });
                }).thenAccept(relations -> {
                    if (relations.isEmpty()) {
                        sender.sendMessage(ColorUtils.colorize("&cSem relações de guilda"));
                        return;
                    }
                    for (GuildRelation relation : relations) {
                        String status = getRelationStatusText(relation.getStatus());
                        String type = getRelationTypeText(relation.getType());
                        sender.sendMessage(ColorUtils.colorize(String.format("&e%s ↔ %s &7- %s &7- %s", 
                            relation.getGuild1Name(), relation.getGuild2Name(), type, status)));
                    }
                });
                break;
            case "create":
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin relation create <Guilda1> <Guilda2> <Tipo>"));
                    sender.sendMessage(ColorUtils.colorize("&7Tipos: ally|enemy|war|truce|neutral"));
                    return;
                }
                handleCreateRelation(sender, args);
                break;
            case "delete":
                if (args.length < 4) {
                    sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin relation delete <Guilda1> <Guilda2>"));
                    return;
                }
                handleDeleteRelation(sender, args);
                break;
            default:
                sender.sendMessage(ColorUtils.colorize("&cOperação inválida! Use list|create|delete|gui"));
                break;
        }
    }
    private void handleCreateRelation(CommandSender sender, String[] args) {
        String guild1Name = args[2];
        String guild2Name = args[3];
        String relationTypeStr = args[4];
        GuildRelation.RelationType relationType;
        try {
            relationType = GuildRelation.RelationType.valueOf(relationTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ColorUtils.colorize("&cTipo inválido! Use: ally, enemy, war, truce, neutral"));
            return;
        }
        CompletableFuture<Guild> guild1Future = plugin.getGuildService().getGuildByNameAsync(guild1Name);
        CompletableFuture<Guild> guild2Future = plugin.getGuildService().getGuildByNameAsync(guild2Name);
        CompletableFuture.allOf(guild1Future, guild2Future).thenAccept(v -> {
            try {
                Guild guild1 = guild1Future.get();
                Guild guild2 = guild2Future.get();
                if (guild1 == null) {
                    sender.sendMessage(ColorUtils.colorize("&c工会 " + guild1Name + " 不存在！"));
                    return;
                }
                if (guild2 == null) {
                    sender.sendMessage(ColorUtils.colorize("&c工会 " + guild2Name + " 不存在！"));
                    return;
                }
                if (guild1.getId() == guild2.getId()) {
                    sender.sendMessage(ColorUtils.colorize("&cNão é possível criar relação consigo mesmo!"));
                    return;
                }
                plugin.getGuildService().createGuildRelationAsync(
                    guild1.getId(), guild2.getId(), 
                    guild1.getName(), guild2.getName(), 
                    relationType, UUID.randomUUID(), "管理员"
                ).thenAccept(success -> {
                    if (success) {
                        sender.sendMessage(ColorUtils.colorize("&aRelação criada: " + guild1Name + " ↔ " + guild2Name + " (" + getRelationTypeText(relationType) + ")"));
                    } else {
                        sender.sendMessage(ColorUtils.colorize("&cFalha ao criar relação!"));
                    }
                });
            } catch (Exception e) {
                sender.sendMessage(ColorUtils.colorize("&cErro ao criar relação: " + e.getMessage()));
            }
        });
    }
    private void handleDeleteRelation(CommandSender sender, String[] args) {
        String guild1Name = args[2];
        String guild2Name = args[3];
        CompletableFuture<Guild> guild1Future = plugin.getGuildService().getGuildByNameAsync(guild1Name);
        CompletableFuture<Guild> guild2Future = plugin.getGuildService().getGuildByNameAsync(guild2Name);
        CompletableFuture.allOf(guild1Future, guild2Future).thenAccept(v -> {
            try {
                Guild guild1 = guild1Future.get();
                Guild guild2 = guild2Future.get();
                if (guild1 == null) {
                    sender.sendMessage(ColorUtils.colorize("&c工会 " + guild1Name + " 不存在！"));
                    return;
                }
                if (guild2 == null) {
                    sender.sendMessage(ColorUtils.colorize("&c工会 " + guild2Name + " 不存在！"));
                    return;
                }
                plugin.getGuildService().getGuildRelationsAsync(guild1.getId()).thenAccept(relations -> {
                    for (GuildRelation relation : relations) {
                        if ((relation.getGuild1Id() == guild1.getId() && relation.getGuild2Id() == guild2.getId()) ||
                            (relation.getGuild1Id() == guild2.getId() && relation.getGuild2Id() == guild1.getId())) {
                            plugin.getGuildService().deleteGuildRelationAsync(relation.getId()).thenAccept(success -> {
                                if (success) {
                                    sender.sendMessage(ColorUtils.colorize("&aRelação deletada: " + guild1Name + " ↔ " + guild2Name));
                                } else {
                                    sender.sendMessage(ColorUtils.colorize("&cFalha ao deletar relação!"));
                                }
                            });
                            return;
                        }
                    }
                    sender.sendMessage(ColorUtils.colorize("&cRelação entre " + guild1Name + " e " + guild2Name + " não encontrada!"));
                });
            } catch (Exception e) {
                sender.sendMessage(ColorUtils.colorize("&cErro ao deletar relação: " + e.getMessage()));
            }
        });
    }
    private String getRelationStatusText(GuildRelation.RelationStatus status) {
        switch (status) {
            case PENDING: return "Pendente";
            case ACTIVE: return "Ativo";
            case EXPIRED: return "Expirado";
            case CANCELLED: return "Cancelado";
            default: return "Desconhecido";
        }
    }
    private String getRelationTypeText(GuildRelation.RelationType type) {
        switch (type) {
            case ALLY: return "Aliado";
            case ENEMY: return "Inimigo";
            case WAR: return "Guerra";
            case TRUCE: return "Trégua";
            case NEUTRAL: return "Neutro";
            default: return "Desconhecido";
        }
    }
    private void handleReload(CommandSender sender) {
        try {
            plugin.getConfigManager().reloadAllConfigs();
            plugin.getPermissionManager().reloadFromConfig();
            sender.sendMessage(ColorUtils.colorize("&aConfiguração recarregada!"));
        } catch (Exception e) {
            sender.sendMessage(ColorUtils.colorize("&cFalha ao recarregar configuração: " + e.getMessage()));
        }
    }
    private void handleTest(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin test <test-type>"));
            sender.sendMessage(ColorUtils.colorize("&7test-type: gui, relation"));
            return;
        }
        String testType = args[1];
        switch (testType.toLowerCase()) {
            case "gui":
                if (sender instanceof Player player) {
                    AdminGuildGUI adminGUI = new AdminGuildGUI(plugin);
                    plugin.getGuiManager().openGUI(player, adminGUI);
                    sender.sendMessage(ColorUtils.colorize("&aGUI de administrador aberta para teste."));
                } else {
                    sender.sendMessage(ColorUtils.colorize("&cEste comando só pode ser executado por jogadores!"));
                }
                break;
            case "relation":
                if (args.length < 5) {
                    sender.sendMessage(ColorUtils.colorize("&cUso: /guildadmin test relation create <Guilda1> <Guilda2> <Tipo>"));
                    sender.sendMessage(ColorUtils.colorize("&7Tipos: ally|enemy|war|truce|neutral"));
                    return;
                }
                String guild1NameTest = args[2];
                String guild2NameTest = args[3];
                String relationTypeStrTest = args[4];
                GuildRelation.RelationType relationTypeTest;
                try {
                    relationTypeTest = GuildRelation.RelationType.valueOf(relationTypeStrTest.toUpperCase());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ColorUtils.colorize("&cTipo inválido! Use: ally, enemy, war, truce, neutral"));
                    return;
                }
                plugin.getGuildService().getGuildByNameAsync(guild1NameTest).thenAccept(guild1 -> {
                    if (guild1 == null) {
                        sender.sendMessage(ColorUtils.colorize("&c工会 " + guild1NameTest + " 不存在！"));
                        return;
                    }
                    plugin.getGuildService().getGuildByNameAsync(guild2NameTest).thenAccept(guild2 -> {
                        if (guild2 == null) {
                            sender.sendMessage(ColorUtils.colorize("&c工会 " + guild2NameTest + " 不存在！"));
                            return;
                        }
                        if (guild1.getId() == guild2.getId()) {
                            sender.sendMessage(ColorUtils.colorize("&cNão é possível criar relação consigo mesmo!"));
                            return;
                        }
                        plugin.getGuildService().createGuildRelationAsync(
                            guild1.getId(), guild2.getId(), 
                            guild1.getName(), guild2.getName(), 
                            relationTypeTest, UUID.randomUUID(), "管理员"
                        ).thenAccept(success -> {
                            if (success) {
                                sender.sendMessage(ColorUtils.colorize("&aRelação criada: " + guild1NameTest + " ↔ " + guild2NameTest + " (" + getRelationTypeText(relationTypeTest) + ")"));
                            } else {
                                sender.sendMessage(ColorUtils.colorize("&cFalha ao criar relação!"));
                            }
                        });
                    });
                });
                break;
            default:
                sender.sendMessage(ColorUtils.colorize("&cTipo de teste inválido! Use gui, economy, relation"));
                break;
        }
    }
    private void handleHelp(CommandSender sender) {
        sender.sendMessage(ColorUtils.colorize("&6=== Comandos de Administrador da Guilda ==="));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin &7- Abrir GUI de administrador"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin list &7- Listar todas as guildas"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin info <Guilda> &7- Ver informações da guilda"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin delete <Guilda> &7- Deletar guilda à força"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin freeze <Guilda> &7- Congelar guilda"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin unfreeze <Guilda> &7- Descongelar guilda"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin transfer <Guilda> <Jogador> &7- Transferir liderança"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin economy <Guilda> <Operação> <Quantia> &7- Gerenciar economia"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin relation <Operação> &7- Gerenciar relações"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin reload &7- Recarregar configuração"));
        sender.sendMessage(ColorUtils.colorize("&e/guildadmin help &7- Mostrar ajuda"));
    }
}