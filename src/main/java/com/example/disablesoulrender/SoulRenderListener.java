package com.example.disablesoulrender;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SoulRenderListener implements Listener {

    private final DisableSoulRender plugin;
    private final NamespacedKey soulRenderKey;

    // 防止消息刷屏的冷却 (ms)
    private static final long MESSAGE_COOLDOWN = 2000L;
    private final Map<UUID, Long> lastMessageTime = new HashMap<>();

    public SoulRenderListener(DisableSoulRender plugin) {
        this.plugin = plugin;
        this.soulRenderKey = new NamespacedKey("cataclysm", "soul_render");
    }

    /**
     * 监听玩家右键点击（空气/方块）事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        if (!player.isGliding()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        if (!isSoulRender(item)) {
            return;
        }

        // 取消事件
        event.setCancelled(true);
        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

        sendCooldownMessage(player);
    }

    /**
     * 监听玩家右键点击实体事件
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();

        if (!player.isGliding()) {
            return;
        }

        ItemStack item = player.getInventory().getItem(event.getHand());
        if (item == null || !isSoulRender(item)) {
            return;
        }

        event.setCancelled(true);
        sendCooldownMessage(player);
    }

    /**
     * 检查物品是否为 cataclysm:soul_render
     */
    private boolean isSoulRender(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }

        // 方式1：通过 Material 的 Key 判断（最可靠）
        try {
            NamespacedKey itemKey = item.getType().getKey();
            if (soulRenderKey.equals(itemKey)) {
                return true;
            }
        } catch (Exception ignored) {
        }

        // 方式2：通过物品类型字符串兜底
        String itemString = item.getType().toString().toLowerCase();
        if (itemString.contains("soul_render")) {
            return true;
        }

        return false;
    }

    /**
     * 发送提示消息（ActionBar 不可用时降级为聊天栏）
     */
    private void sendCooldownMessage(Player player) {
        long now = System.currentTimeMillis();
        Long last = lastMessageTime.get(player.getUniqueId());

        if (last != null && now - last < MESSAGE_COOLDOWN) {
            return;
        }

        lastMessageTime.put(player.getUniqueId(), now);

        String msgText = plugin.getConfig().getString(
                "message",
                "鞘翅飞行时无法使用 Soul Render！"
        );

        String coloredMsg = ChatColor.translateAlternateColorCodes('&', "&c" + msgText);

        // 用反射调用 sendActionBar，避免直接依赖 Paper / Adventure API
        if (!trySendActionBar(player, coloredMsg)) {
            // 如果 ActionBar 失败，降级为普通聊天消息
            player.sendMessage(coloredMsg);
        }
    }

    /**
     * 通过反射尝试发送 ActionBar 消息，兼容 Spigot / Paper / Arclight 等
     */
    private boolean trySendActionBar(Player player, String message) {
        // 尝试 Spigot API 的 sendMessage(ChatMessageType, BaseComponent...)
        try {
            Class<?> chatMessageTypeClass = Class.forName("net.md_5.bungee.api.ChatMessageType");
            Class<?> baseComponentClass = Class.forName("net.md_5.bungee.api.chat.BaseComponent");
            Class<?> textComponentClass = Class.forName("net.md_5.bungee.api.chat.TextComponent");

            Object actionBarType = chatMessageTypeClass
                    .getField("ACTION_BAR")
                    .get(null);

            Object textComponent = textComponentClass
                    .getConstructor(String.class)
                    .newInstance(message);

            Object componentArray = java.lang.reflect.Array.newInstance(baseComponentClass, 1);
            java.lang.reflect.Array.set(componentArray, 0, textComponent);

            player.spigot()
                    .getClass()
                    .getMethod("sendMessage", chatMessageTypeClass, componentArray.getClass())
                    .invoke(player.spigot(), actionBarType, componentArray);

            return true;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
