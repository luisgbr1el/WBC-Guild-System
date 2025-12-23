package com.guild.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Server;

/**
 * Utilitário de detecção de tipo de servidor
 */
public class ServerUtils {
    
    public enum ServerType {
        SPIGOT,
        FOLIA,
        UNKNOWN
    }
    
    private static ServerType serverType = null;
    
    /**
     * Detecta o tipo de servidor
     */
    public static ServerType getServerType() {
        if (serverType == null) {
            serverType = detectServerType();
        }
        return serverType;
    }
    
    /**
     * Verifica se é um servidor Folia
     */
    public static boolean isFolia() {
        return getServerType() == ServerType.FOLIA;
    }
    
    /**
     * Verifica se é um servidor Spigot
     */
    public static boolean isSpigot() {
        return getServerType() == ServerType.SPIGOT;
    }
    
    /**
     * Obtém a versão do servidor
     */
    public static String getServerVersion() {
        return Bukkit.getServer().getBukkitVersion();
    }
    
    /**
     * Implementação específica da detecção do tipo de servidor
     */
    private static ServerType detectServerType() {
        try {
            // Tenta carregar classes específicas do Folia
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return ServerType.FOLIA;
        } catch (ClassNotFoundException e) {
            // Verifica se é Spigot
            try {
                Class.forName("org.spigotmc.SpigotConfig");
                return ServerType.SPIGOT;
            } catch (ClassNotFoundException e2) {
                return ServerType.UNKNOWN;
            }
        }
    }
    
    /**
     * Verifica se suporta a versão da API especificada
     */
    public static boolean supportsApiVersion(String requiredVersion) {
        String serverVersion = getServerVersion();
        return compareVersions(serverVersion, requiredVersion) >= 0;
    }
    
    /**
     * Utilitário de comparação de versões
     */
    private static int compareVersions(String version1, String version2) {
        String[] v1Parts = version1.split("-")[0].split("\\.");
        String[] v2Parts = version2.split("-")[0].split("\\.");
        
        int maxLength = Math.max(v1Parts.length, v2Parts.length);
        
        for (int i = 0; i < maxLength; i++) {
            int v1Part = i < v1Parts.length ? Integer.parseInt(v1Parts[i]) : 0;
            int v2Part = i < v2Parts.length ? Integer.parseInt(v2Parts[i]) : 0;
            
            if (v1Part != v2Part) {
                return Integer.compare(v1Part, v2Part);
            }
        }
        
        return 0;
    }
}
