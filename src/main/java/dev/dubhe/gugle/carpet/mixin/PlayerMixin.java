package dev.dubhe.gugle.carpet.mixin;

import carpet.patches.EntityPlayerMPFake;
import dev.dubhe.gugle.carpet.GcaExtension;
import dev.dubhe.gugle.carpet.GcaSetting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerMixin {

    Player self = (Player) (Object) this;

    @Inject(method = "tick", at = @At("RETURN"))
    private void tick(CallbackInfo ci) {
        if (GcaSetting.openFakePlayerInventory && self instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof EntityPlayerMPFake && serverPlayer.isAlive()) {
                GcaExtension.fakePlayerInventoryContainerMap.get(self).tick();
            }
        }
    }

    @Inject(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",ordinal = 0), cancellable = true)
    private void interactOn(Entity entityToInteractOn, InteractionHand hand,
            CallbackInfoReturnable<InteractionResult> cir) {
        if (self instanceof ServerPlayer serverPlayer) {
            if (entityToInteractOn instanceof EntityPlayerMPFake fakePlayer) {
                SimpleMenuProvider provider = null;
                if (GcaSetting.openFakePlayerInventory) {
                    provider = new SimpleMenuProvider((i, inventory, p) -> ChestMenu.sixRows(i, inventory,
                            GcaExtension.fakePlayerInventoryContainerMap.get(fakePlayer)), fakePlayer.getDisplayName());
                }
                if (GcaSetting.openFakePlayerEnderChest && serverPlayer.isSneaking()) {
                    provider = new SimpleMenuProvider(
                            (i, inventory, p) -> ChestMenu.threeRows(i, inventory,
                                    fakePlayer.getEnderChestInventory()),
                            fakePlayer.getDisplayName());
                }
                if (provider != null) {
                    serverPlayer.openMenu(provider);
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}
