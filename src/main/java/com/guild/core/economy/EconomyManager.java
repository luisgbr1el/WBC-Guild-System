package com.guild.core.economy;

import com.guild.GuildPlugin;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

/**
 * 经济管理器 - 管理经济系统 (Vault集成已移除)
 */
public class EconomyManager {
    
    private final GuildPlugin plugin;
    private final Logger logger;
    
    public EconomyManager(GuildPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
        logger.info("Sistema de economia (Vault) desativado conforme solicitado.");
    }
    
    /**
     * 检查Vault是否可用
     */
    public boolean isVaultAvailable() {
        return false;
    }
    
    /**
     * 获取玩家余额
     */
    public double getBalance(Player player) {
        return 0.0;
    }
    
    /**
     * 检查玩家是否有足够的余额
     */
    public boolean hasBalance(Player player, double amount) {
        return true; // Always return true to bypass checks if any remain
    }
    
    /**
     * 扣除玩家余额
     */
    public boolean withdraw(Player player, double amount) {
        return true; // Always succeed
    }
    
    /**
     * 增加玩家余额
     */
    public boolean deposit(Player player, double amount) {
        return true; // Always succeed
    }
    
    /**
     * 格式化货币
     */
    public String format(double amount) {
        return String.format("%.2f", amount);
    }
    
    /**
     * 获取货币名称
     */
    public String getCurrencyName() {
        return "Moedas";
    }
    
    /**
     * 获取货币单数名称
     */
    public String getCurrencyNameSingular() {
        return "Moeda";
    }
    
    /**
     * 检查玩家是否有足够的余额（异步）
     */
    public boolean hasBalanceAsync(Player player, double amount) {
        return true;
    }
    
    /**
     * 扣除玩家余额（异步）
     */
    public boolean withdrawAsync(Player player, double amount) {
        return true;
    }
    
    /**
     * 增加玩家余额（异步）
     */
    public boolean depositAsync(Player player, double amount) {
        return true;
    }
}
