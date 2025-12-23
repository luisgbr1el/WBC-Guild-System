package com.guild.core.utils;

/**
 * Utilitários de teste
 */
public class TestUtils {
    
    /**
     * Testa a compatibilidade do servidor
     */
    public static void testCompatibility(java.util.logging.Logger logger) {
        if (logger == null) {
            return;
        }
        logger.info("=== Teste de Compatibilidade do Servidor ===");
        logger.info("Tipo de Servidor: " + ServerUtils.getServerType());
        logger.info("Versão do Servidor: " + ServerUtils.getServerVersion());
        logger.info("Suporta 1.21: " + ServerUtils.supportsApiVersion("1.21"));
        logger.info("Suporta 1.21.8: " + ServerUtils.supportsApiVersion("1.21.8"));
        logger.info("É Folia: " + ServerUtils.isFolia());
        logger.info("É Spigot: " + ServerUtils.isSpigot());
        logger.info("=========================");
    }
    
    /**
     * Testa a compatibilidade do agendador
     */
    public static void testSchedulerCompatibility(java.util.logging.Logger logger) {
        if (logger == null) {
            return;
        }
        logger.info("=== Teste de Compatibilidade do Agendador ===");
        logger.info("Está na Thread Principal: " + CompatibleScheduler.isPrimaryThread());
        logger.info("Tipo de Servidor: " + ServerUtils.getServerType());
        logger.info("=========================");
    }
}
