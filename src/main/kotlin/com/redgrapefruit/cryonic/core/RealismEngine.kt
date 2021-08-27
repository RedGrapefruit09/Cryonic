package com.redgrapefruit.cryonic.core

import com.redgrapefruit.cryonic.item.OverdueFoodItem
import com.redgrapefruit.cryonic.item.RancidDrinkItem
import com.redgrapefruit.cryonic.item.RottenFoodItem
import com.redgrapefruit.cryonic.util.*
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.world.World

object RealismEngine {
    /**
     * Updates food parameters
     */
    fun updateFood(
        config: FoodConfig,
        profile: FoodProfile,
        player: PlayerEntity,
        slot: Int,
        world: World,
        rottenVariant: RottenFoodItem?,
        overdueVariant: OverdueFoodItem?,
        isSalt: Boolean
    ) {

        // 1. Initialization of world time
        if (!profile.isInitialized) {
            profile.previousTick = world.time
            profile.isInitialized = true
        }

        // 2. Compensation of lost ticks for glitches and putting the item into a fridge/locker etc.
        // Calculate the difference between current world time and last saved world time
        val currentTick = world.time
        val difference = currentTick - profile.previousTick
        // If the difference exceeds minimal tick loss, compensate the lost ticks
        if (difference >= MIN_TICK_LOSS) {
            if (config.category.canRot) {
                profile.rotProgress += (difference * calculateRot(profile, config)).toInt()
            }
            if (config.category.canOverdue) {
                profile.overdueProgress += (difference * calculateOverdue(profile, config)).toInt()
            }
        }
        // Now save the world time for further use
        profile.previousTick = currentTick

        // 3. Updating rot and overdue
        if (config.category.canRot) {
            profile.rotProgress += calculateRot(profile, config)
        }
        if (config.category.canOverdue) {
            profile.overdueProgress += calculateOverdue(profile, config)
        }

        // 4. Salt logic
        if (isSalt) profile.rotProgress -= config.saltEfficiency

        // 5. Bound-check rot and overdue to see if the limit has been exceeded
        if (config.category.canRot && profile.rotProgress >= config.rotState) {
            // Swap the item with its rotten variant and reset counter for the stack
            player.inventory.getStack(slot).decrement(1)
            player.inventory.offerOrDrop(ItemStack(rottenVariant))
            profile.rotProgress = 0
        }
        if (config.category.canOverdue && profile.overdueProgress >= config.overdueState) {
            // Swap the item with its overdue variant and reset counter for the stack
            player.inventory.getStack(slot).decrement(1)
            player.inventory.offerOrDrop(ItemStack(overdueVariant))
            profile.overdueProgress = 0
        }
    }

    /**
     * Updates drink parameters
     */
    fun updateDrink(
        profile: DrinkProfile,
        rancidSpeed: Int,
        rancidState: Int,
        slot: Int,
        world: World,
        player: PlayerEntity,
        rancidVariant: RancidDrinkItem
    ) {
        if (!profile.isInitialized) {
            profile.previousTick = world.time
            profile.isInitialized = true
        }

        val currentTick = world.time
        val difference = currentTick - profile.previousTick
        if (difference > MIN_TICK_LOSS) {
            profile.rancidProgress += (difference * rancidSpeed).toInt()
        }
        profile.previousTick = currentTick

        profile.rancidProgress += rancidSpeed

        if (profile.rancidProgress >= rancidState) {
            player.inventory.getStack(slot).decrement(1)
            player.inventory.offerOrDrop(ItemStack(rancidVariant))
            profile.rancidProgress = 0
        }
    }

    /**
     * Renders a tooltip for food
     */
    fun renderFoodTooltip(tooltip: MutableList<Text>, config: FoodConfig, profile: FoodProfile, state: FoodState) {
        // State
        breakLine(tooltip)
        tooltip += LiteralText(AQUA + "State: " + state.displayName)
        // Category and its description
        breakLine(tooltip)
        tooltip += LiteralText(DARK_RED + "Category: ")
        tooltip += LiteralText(RED + config.category.displayName)
        tooltip += LiteralText(YELLOW + config.category.details)
        // Rot
        if (config.category.canRot && state.displayRot) {
            breakLine(tooltip)
            tooltip += LiteralText(DARK_GREEN + "Rot: " + profile.rotProgress + "/" + config.rotState)
        }
        // Overdue
        if (config.category.canOverdue && state.displayOverdue) {
            breakLine(tooltip)
            tooltip += LiteralText(DARK_PURPLE + "Overdue: " + profile.overdueProgress + "/" + config.overdueState)
        }
        // Fridge
        if (config.category.canBePutInFridge && state.displayFridgeProperties) {
            breakLine(tooltip)
            tooltip += LiteralText(BLUE + "Is in fridge: " + profile.fridgeState.boolValue)
            tooltip += LiteralText(DARK_BLUE + "Fridge efficiency: " + config.fridgeEfficiency)
        }
    }

    fun renderDrinkTooltip(tooltip: MutableList<Text>, profile: DrinkProfile, rancidState: Int, isRancid: Boolean = false) {
        // State
        breakLine(tooltip)
        tooltip += LiteralText(AQUA + "State: ${if (isRancid) "Rancid" else "Fresh"}")
        // Rancid
        if (!isRancid) {
            breakLine(tooltip)
            tooltip += LiteralText(GREEN + "Rancid: ${profile.rancidProgress}/$rancidState")
        }
    }
}