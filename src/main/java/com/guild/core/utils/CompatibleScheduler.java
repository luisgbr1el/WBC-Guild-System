package com.guild.core.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/**
 * Agendador de Compatibilidade - Suporta Spigot e Folia
 */
public class CompatibleScheduler {
    
    /**
     * Executa tarefa na thread principal
     */
    public static void runTask(Plugin plugin, Runnable task) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador de região global do Folia
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                globalScheduler.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class)
                    .invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run());
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTask(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Executa tarefa em um local específico
     */
    public static void runTask(Plugin plugin, Location location, Runnable task) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador de região do Folia
                Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
                regionScheduler.getClass().getMethod("run", Plugin.class, Location.class, java.util.function.Consumer.class)
                    .invoke(regionScheduler, plugin, location, (java.util.function.Consumer<Object>) scheduledTask -> task.run());
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTask(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Executa tarefa na região da entidade especificada
     */
    public static void runTask(Plugin plugin, Entity entity, Runnable task) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador de entidade do Folia
                Object entityScheduler = entity.getClass().getMethod("getScheduler").invoke(entity);
                entityScheduler.getClass().getMethod("run", Plugin.class, java.util.function.Consumer.class, Runnable.class)
                    .invoke(entityScheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), (Runnable) () -> {});
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTask(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTask(plugin, task);
        }
    }
    
    /**
     * Executa tarefa com atraso
     */
    public static void runTaskLater(Plugin plugin, Runnable task, long delay) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador de região global do Folia
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                globalScheduler.getClass().getMethod("runDelayed", Plugin.class, java.util.function.Consumer.class, long.class)
                    .invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), delay);
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Executa tarefa com atraso em um local específico
     */
    public static void runTaskLater(Plugin plugin, Location location, Runnable task, long delay) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador de região do Folia
                Object regionScheduler = Bukkit.class.getMethod("getRegionScheduler").invoke(null);
                regionScheduler.getClass().getMethod("runDelayed", Plugin.class, Location.class, java.util.function.Consumer.class, long.class)
                    .invoke(regionScheduler, plugin, location, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), delay);
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTaskLater(plugin, task, delay);
            }
        } else {
            Bukkit.getScheduler().runTaskLater(plugin, task, delay);
        }
    }
    
    /**
     * Executa tarefa assincronamente
     */
    public static void runTaskAsync(Plugin plugin, Runnable task) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador assíncrono do Folia
                Object asyncScheduler = Bukkit.class.getMethod("getAsyncScheduler").invoke(null);
                asyncScheduler.getClass().getMethod("runNow", Plugin.class, java.util.function.Consumer.class)
                    .invoke(asyncScheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run());
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
            }
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
    
    /**
     * Executa tarefa repetidamente
     */
    public static void runTaskTimer(Plugin plugin, Runnable task, long delay, long period) {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar o agendador de região global do Folia
                Object globalScheduler = Bukkit.class.getMethod("getGlobalRegionScheduler").invoke(null);
                globalScheduler.getClass().getMethod("runAtFixedRate", Plugin.class, java.util.function.Consumer.class, long.class, long.class)
                    .invoke(globalScheduler, plugin, (java.util.function.Consumer<Object>) scheduledTask -> task.run(), delay, period);
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para o agendador tradicional
                Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
            }
        } else {
            Bukkit.getScheduler().runTaskTimer(plugin, task, delay, period);
        }
    }
    
    /**
     * Verifica se está na thread principal
     */
    public static boolean isPrimaryThread() {
        if (ServerUtils.isFolia()) {
            try {
                // Usa reflexão para chamar a verificação de thread global do Folia
                return (Boolean) Bukkit.class.getMethod("isGlobalTickThread").invoke(null);
            } catch (Exception e) {
                // Se a API do Folia não estiver disponível, volta para a verificação tradicional
                return Bukkit.isPrimaryThread();
            }
        } else {
            return Bukkit.isPrimaryThread();
        }
    }
}
