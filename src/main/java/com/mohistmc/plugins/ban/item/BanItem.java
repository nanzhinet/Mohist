package com.mohistmc.plugins.ban.item;

import com.mohistmc.MohistConfig;
import com.mohistmc.api.ItemAPI;
import com.mohistmc.util.ListUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.event.inventory.InventoryCloseEvent;

import java.util.List;

/**
 * @author Mgazul by MohistMC
 * @date 2023/7/27 2:54:23
 */
public class BanItem {

    public static boolean check(UseOnContext use) {
        return check(use.getPlayer(), use.getItemInHand());
    }

    public static boolean check(net.minecraft.world.entity.player.Player player, ItemStack itemStack) {
        if (!MohistConfig.ban_item_enable) return false;
        if (player == null) return false;
        if (ItemAPI.isBan(CraftItemStack.asCraftMirror(itemStack))) {
            player.containerMenu.sendAllDataToRemote();
            return true;
        }
        return false;
    }

    public static boolean check(ItemStack itemStack) {
        if (!MohistConfig.ban_item_enable) return false;
        return ItemAPI.isBan(CraftItemStack.asCraftMirror(itemStack));
    }

    public static void saveItems(InventoryCloseEvent event) {
        if (event.getView().getTitle().equals("§4Add bans item")) {

            List<String> old = MohistConfig.ban_item_materials;
            for (org.bukkit.inventory.ItemStack itemStack : event.getInventory().getContents()) {
                if (itemStack != null) {
                    ListUtils.isDuplicate(old, itemStack.getType().name());
                }
            }
            MohistConfig.yml.set("ban.item.materials", old);
            MohistConfig.save();
        }
    }
}
