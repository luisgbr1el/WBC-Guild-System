package com.guild.models;

import java.time.LocalDateTime;

/**
 * Modelo de log da guilda
 * Usado para registrar o histórico de várias operações da guilda
 */
public class GuildLog {
    private int id;
    private int guildId;
    private String guildName;
    private String playerUuid;
    private String playerName;
    private LogType logType;
    private String description;
    private String details;
    private LocalDateTime createdAt;

    public GuildLog() {
    }

    public GuildLog(int guildId, String guildName, String playerUuid, String playerName, 
                   LogType logType, String description, String details) {
        this.guildId = guildId;
        this.guildName = guildName;
        this.playerUuid = playerUuid;
        this.playerName = playerName;
        this.logType = logType;
        this.description = description;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getGuildId() {
        return guildId;
    }

    public void setGuildId(int guildId) {
        this.guildId = guildId;
    }

    public String getGuildName() {
        return guildName;
    }

    public void setGuildName(String guildName) {
        this.guildName = guildName;
    }

    public String getPlayerUuid() {
        return playerUuid;
    }

    public void setPlayerUuid(String playerUuid) {
        this.playerUuid = playerUuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public LogType getLogType() {
        return logType;
    }

    public void setLogType(LogType logType) {
        this.logType = logType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Enumeração de tipos de log
     */
    public enum LogType {
        GUILD_CREATED("Guilda Criada"),
        GUILD_DISSOLVED("Guilda Dissolvida"),
        GUILD_RENAMED("Guilda Renomeada"),
        MEMBER_JOINED("Membro Entrou"),
        MEMBER_LEFT("Membro Saiu"),
        MEMBER_KICKED("Membro Expulso"),
        MEMBER_PROMOTED("Membro Promovido"),
        MEMBER_DEMOTED("Membro Rebaixado"),
        LEADER_TRANSFERRED("Líder Transferido"),
        FUND_DEPOSITED("Fundo Depositado"),
        FUND_WITHDRAWN("Fundo Retirado"),
        FUND_TRANSFERRED("Fundo Transferido"),
        RELATION_CREATED("Relação Criada"),
        RELATION_DELETED("Relação Excluída"),
        RELATION_ACCEPTED("Relação Aceita"),
        RELATION_REJECTED("Relação Rejeitada"),
        GUILD_FROZEN("Guilda Congelada"),
        GUILD_UNFROZEN("Guilda Descongelada"),
        GUILD_LEVEL_UP("Guilda Atualizada"),
        APPLICATION_SUBMITTED("Inscrição Enviada"),
        APPLICATION_ACCEPTED("Inscrição Aceita"),
        APPLICATION_REJECTED("Inscrição Rejeitada"),
        INVITATION_SENT("Convite Enviado"),
        INVITATION_ACCEPTED("Convite Aceito"),
        INVITATION_REJECTED("Convite Rejeitado");

        private final String displayName;

        LogType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Obtém a string de tempo formatada
     */
    public String getFormattedTime() {
        if (createdAt == null) return "Desconhecido";
        return createdAt.format(com.guild.core.time.TimeProvider.FULL_FORMATTER);
    }

    /**
     * Obtém a string de tempo simplificada (para exibição)
     */
    public String getSimpleTime() {
        if (createdAt == null) return "Desconhecido";
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(createdAt, now);
        
        long days = duration.toDays();
        long hours = duration.toHours() % 24;
        long minutes = duration.toMinutes() % 60;
        
        if (days > 0) {
            return days + " dias atrás";
        } else if (hours > 0) {
            return hours + " horas atrás";
        } else if (minutes > 0) {
            return minutes + " minutos atrás";
        } else {
            return "Agora mesmo";
        }
    }
}
