package com.carrot.carrotshop.listener;

import java.util.Optional;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.gamemode.GameModes;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import com.carrot.carrotshop.CarrotShop;
import com.carrot.carrotshop.ShopsData;
import com.carrot.carrotshop.shop.Shop;


public class PlayerClickListener {
	@Listener(order=Order.AFTER_PRE, beforeModifications = true)
	public void onPlayerRightClick(InteractBlockEvent.Secondary.MainHand event, @First Player player)
	{
		Optional<Location<World>> optLoc = event.getTargetBlock().getLocation();
		if (!optLoc.isPresent())
			return;

		Optional<Shop> shop = ShopsData.getShop(optLoc.get());
		if (shop.isPresent()) {
			if (optLoc.get().getBlockType() == BlockTypes.STANDING_SIGN || optLoc.get().getBlockType() == BlockTypes.WALL_SIGN) {
				shop.get().trigger(player);
				Sponge.getScheduler().createTaskBuilder().delayTicks(4).execute(
						task -> {
							shop.get().update();
							task.cancel();
						}).submit(CarrotShop.getInstance());
			}
		}
	}

	@Listener(order=Order.AFTER_PRE, beforeModifications = true)
	public void onPlayerLeftClickMaster(InteractBlockEvent.Primary.MainHand event, @First Player player)
	{
		Optional<Location<World>> optLoc = event.getTargetBlock().getLocation();
		if (!optLoc.isPresent())
			return;

		Optional<Shop> shop = ShopsData.getShop(optLoc.get());
		if (shop.isPresent())
			shop.get().info(player);
	}

	@Listener(order=Order.FIRST, beforeModifications = true)
	public void onPlayerLeftClickProtect(InteractBlockEvent.Primary.MainHand event, @First Player player)
	{
		if (!player.gameMode().get().equals(GameModes.CREATIVE))
			return;
		
		Optional<Location<World>> optLoc = event.getTargetBlock().getLocation();
		if (!optLoc.isPresent())
			return;

		Optional<Shop> shop = ShopsData.getShop(optLoc.get());
		if (shop.isPresent()) {
			Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
				if (!optItem.isPresent() || !optItem.get().getItem().equals(ItemTypes.BEDROCK))
					event.setCancelled(true);
		}
	}

	@Listener(beforeModifications = true)
	public void onPlayerLeftClickNormal(InteractBlockEvent.Primary.MainHand event, @First Player player)
	{
		Optional<Location<World>> optLoc = event.getTargetBlock().getLocation();
		if (!optLoc.isPresent())
			return;

		Optional<ItemStack> optItem = player.getItemInHand(HandTypes.MAIN_HAND);
		if (optItem.isPresent() && optItem.get().getItem().equals(ItemTypes.REDSTONE)) {
			if (optLoc.get().getBlockType() == BlockTypes.CHEST || optLoc.get().getBlockType() == BlockTypes.TRAPPED_CHEST) {
				event.setCancelled(true);
				ShopsData.storeItemLocation(player, optLoc.get());
			} else if (optLoc.get().getBlockType() == BlockTypes.STANDING_SIGN || optLoc.get().getBlockType() == BlockTypes.WALL_SIGN) {
				event.setCancelled(true);
				Shop.build(player, optLoc.get());
			}
		}
	}
}
