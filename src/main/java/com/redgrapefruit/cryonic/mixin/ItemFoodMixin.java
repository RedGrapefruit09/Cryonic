package com.redgrapefruit.cryonic.mixin;

import com.redgrapefruit.cryonic.core.*;
import com.redgrapefruit.cryonic.item.OverdueFoodItem;
import com.redgrapefruit.cryonic.item.RottenFoodItem;
import com.redgrapefruit.cryonic.util.*;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Mixin(Item.class)
public class ItemFoodMixin implements ItemFoodMixinAccess {
    @Shadow @Final @Nullable private FoodComponent foodComponent;
    @Unique
    private String cryonic$name = "";
    @Unique
    private final Supplier<FoodConfig> cryonic$supplierConfig = () -> ConfigDataKt.storedConfig(cryonic$name);
    @Unique
    private boolean cryonic$isComponentInitialized = false;
    @Unique
    private boolean cryonic$isActivated = false;
    @Unique
    @Nullable
    private OverdueFoodItem cryonic$overdueVariant = null;
    @Unique
    @Nullable
    private RottenFoodItem cryonic$rottenVariant = null;

    // <---- IMPL ---->

    @Inject(method = "inventoryTick", at = @At("TAIL"))
    private void cryonic$inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!cryonic$isActivated || !(entity instanceof PlayerEntity) || cryonic$supplierConfig.get() == FoodConfig.Companion.getDefault()) return;

        //noinspection ConstantConditions
        if (entity instanceof PlayerEntity) {
            RealismEngine.INSTANCE.updateFood(cryonic$supplierConfig.get(), FoodProfile.Companion.get(stack), (PlayerEntity) entity, slot, world, cryonic$rottenVariant, cryonic$overdueVariant, false);
        }
    }

    @Inject(method = "appendTooltip", at = @At("TAIL"))
    private void cryonic$appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context, CallbackInfo ci) {
        if (!cryonic$isActivated || cryonic$supplierConfig.get() == FoodConfig.Companion.getDefault()) return;

        RealismEngine.INSTANCE.renderFoodTooltip(tooltip, cryonic$supplierConfig.get(), FoodProfile.Companion.get(stack), FoodState.FRESH);
    }

    /**
     * Initializes a component, but for the mixin impl
     */
    private void mixinInitComponent() {
        if (cryonic$isComponentInitialized) return;

        if (cryonic$supplierConfig.get() == FoodConfig.Companion.getDefault())
            throw new RuntimeException("Late-load system failed. Config not loaded at moment of execution");

        Objects.requireNonNull(foodComponent, "Late-load system failed. No FoodComponent assigned in " + cryonic$name);
        MutableFoodComponent mutable = MiscUtil.asMutable(foodComponent);
        FoodConfig config = cryonic$supplierConfig.get();

        mutable.setHunger(config.getCategory().getBaseHunger() + config.getHunger());
        if (config.getCategory() == FoodCategory.MEAT) mutable.setMeat(true);
        if (config.getCategory().getBaseHunger() + config.getHunger() < 2) mutable.setSnack(true);
        mutable.setSaturationModifier(config.getCategory().getBaseSaturationModifier() + config.getSaturationModifier());

        ItemAccessor access = (ItemAccessor) ((Item) (Object) this);
        access.setFoodComponent(MiscUtil.asImmutable(mutable));

        cryonic$isComponentInitialized = true;
    }

    // <---- API ---->

    @Override
    public void activateFood() {
        cryonic$isActivated = true;
    }

    @Override
    public void named(@NotNull String name) {
        cryonic$name = name;

        ComponentInitializeCallback.Companion.getEvent().register((loadedName, config) -> {
            if (loadedName.equals(cryonic$name)) mixinInitComponent();
        });
    }

    @Override
    public void setOverdueVariant(@NotNull OverdueFoodItem overdueVariant) {
        this.cryonic$overdueVariant = overdueVariant;
    }

    @Override
    public void setRottenVariant(@NotNull RottenFoodItem rottenVariant) {
        this.cryonic$rottenVariant = rottenVariant;
    }

    @Override
    public boolean isFoodActivated() {
        return cryonic$isActivated;
    }
}
