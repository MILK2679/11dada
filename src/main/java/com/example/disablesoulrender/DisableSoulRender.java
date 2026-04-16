package com.example.disablesoulrender;

import org.bukkit.plugin.java.JavaPlugin;

public class DisableSoulRender extends JavaPlugin {

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new SoulRenderListener(this), this);

        getLogger().info("DisableSoulRender 插件已启用！");
        getLogger().info("已阻止玩家在鞘翅飞行时使用 cataclysm:soul_render");
    }

    @Override
    public void onDisable() {
        getLogger().info("DisableSoulRender 插件已禁用！");
    }
}
