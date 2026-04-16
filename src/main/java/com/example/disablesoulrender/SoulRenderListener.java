package com.example.disablesoulrender;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class SoulRenderListener implements Listener {

    private final DisableSoulRender plugin;
    private final NamespacedKey soulRenderKey;

    // 防止消息刷屏的冷却 (ms)
    private static final long MESSAGE_COOLDOWN = 2000L;
    private final java.util.Map<java.util.UUID, Long> lastMessageTime = new java.util.HashMap<>();

    public SoulRenderListener(DisableSoulRender plugin) {
        this.plugin = plugin;
        // cataclysm:soul_render 的 NamespacedKey
        this.soulRenderKey = new NamespacedKey("cataclysm", "soul_render");
    }

    /**
     * 监听玩家右键点击（空气/方块）事件
     * 这是 soul_render 加速鞘翅的触发方式
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // 只处理右键动作
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();

        // 只在鞘翅飞行时拦截
        if (!player.isGliding()) {
            return;
        }

        ItemStack item = event.getItem();
        if (item == null) {
            return;
        }

        // 判断是否为 soul_render
        if (!isSoulRender(item)) {
            return;
        }

        // 取消事件
        event.setCancelled(true);
        event.setUseItemInHand(org.bukkit.event.Event.Result.DENY);
        event.setUseInteractedBlock(org.bukkit.event.Event.Result.DENY);

        // 提示玩家
        sendCooldownMessage(player);
    }

    /**
     * 监听玩家右键点击实体事件（防止对实体使用）
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

        // 方式1：通过物品的 Key 判断（最可靠）
        try {
            NamespacedKey itemKey = item.getType().getKey();
            if (soulRenderKey.equals(itemKey)) {
                return true;
            }
        } catch (Exception ignored) {
            // 部分模组物品可能无法通过此方式获取，继续尝试其他方式
        }

        // 方式2：通过物品的字符串表示判断（兜底）
        String itemString = item.getType().toString().toLowerCase();
        if (itemString.contains("soul_render")) {
            return true;
        }

        // 方式3：通过 translation key 判断
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            try {
                String translationKey = item.translationKey();
                if (translationKey != null && translationKey.contains("soul_render")) {
                    return true;
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    /**
     * 发送冷却提示，避免刷屏
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

        Component message = Component.text(msgText).color(NamedTextColor.RED);
        player.sendActionBar(message);
    }
}
