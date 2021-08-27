package com.redgrapefruit.cryonic.mixin;

import com.redgrapefruit.cryonic.Constants;
import com.redgrapefruit.cryonic.core.DrinkProfile;
import com.redgrapefruit.cryonic.core.RealismEngine;
import com.redgrapefruit.cryonic.item.RancidDrinkItem;
import com.redgrapefruit.cryonic.util.ItemDrinkMixinAccess;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

/**
 * Like {@link ItemFoodMixin}, but for drinks
 */
@Mixin(Item.class)
public class ItemDrinkMixin implements ItemDrinkMixinAccess {
    @Unique
    private boolean cryonic$isActivated = false;
    @Unique
    private @Nullable FoodComponent cryonic$component = null;
    @Unique
    private @Nullable RancidDrinkItem cryonic$rancidVariant = null;
    @Unique
    private int cryonic$rancidSpeed = Constants.UNUSED_PROPERTY;
    @Unique
    private int cryonic$rancidState = Constants.UNUSED_PROPERTY;

    // <---- IMPL ---->

    @Inject(method = "inventoryTick", at = @At("TAIL"))
    private void cryonic$inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (cryonic$invalid()) return;

        if (entity instanceof PlayerEntity) {
            RealismEngine.INSTANCE.updateDrink(DrinkProfile.Companion.get(stack), cryonic$rancidSpeed, cryonic$rancidState, slot, world, (PlayerEntity) entity, cryonic$rancidVariant);
        }
    }

    @Inject(method = "appendTooltip", at = @At("HEAD"))
    private void cryonic$appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (cryonic$invalid()) return;

        RealismEngine.INSTANCE.renderDrinkTooltip(tooltip, DrinkProfile.Companion.get(stack), cryonic$rancidState, false);
    }

    private boolean cryonic$invalid() {
        return !cryonic$isActivated || cryonic$component == null ||
                cryonic$rancidSpeed == Constants.UNUSED_PROPERTY || cryonic$rancidState == Constants.UNUSED_PROPERTY ||
                cryonic$rancidVariant == null;
    }

    // <---- API ---->

    @Override
    public void activateDrink() {
        cryonic$isActivated = true;
    }

    @Override
    public void setComponent(@NotNull FoodComponent component) {
        cryonic$component = component;
    }

    @Override
    public void setRancidSpeed(int rancidSpeed) {
        cryonic$rancidSpeed = rancidSpeed;
    }

    @Override
    public void setRancidState(int rancidState) {
        cryonic$rancidState = rancidState;
    }

    @Override
    public void setRancidVariant(@NotNull RancidDrinkItem rancidVariant) {
        cryonic$rancidVariant = rancidVariant;
    }

    @Override
    public boolean isDrinkActivated() {
        return cryonic$isActivated;
    }
}
