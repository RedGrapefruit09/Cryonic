package com.redgrapefruit.cryonic.core

import com.redgrapefruit.itemnbt3.CustomData
import net.minecraft.nbt.NbtCompound

/**
 * Minimal loss of ticks to compensate.
 *
 * Used to avoid packet delays and random glitches
 */
const val MIN_TICK_LOSS: Long = 20L

/**
 * Contains changed-at-runtime values of food type
 */
data class FoodProfile(
    var rotProgress: Int = 0,
    var overdueProgress: Int = 0,
    var previousTick: Long = 0L,
    var isInitialized: Boolean = false,
    var fridgeState: FridgeState = FridgeState.NOT_IN_FRIDGE
) : CustomData {
    override fun getNbtCategory(): String = "FoodProfile"

    override fun readNbt(nbt: NbtCompound) {
        rotProgress = nbt.getInt("Rot Progress")
        overdueProgress = nbt.getInt("Overdue Progress")
        previousTick = nbt.getLong("Previous Tick")
        isInitialized = nbt.getBoolean("Is Initialized")
        fridgeState = FridgeState.readNbt("Fridge State", nbt)
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putInt("Rot Progress", rotProgress)
        nbt.putInt("Overdue Progress", overdueProgress)
        nbt.putLong("Previous Tick", previousTick)
        nbt.putBoolean("Is Initialized", isInitialized)
        FridgeState.writeNbt("Fridge State", fridgeState, nbt)
    }
}

/**
 * Contains changed-at-runtime values of drink type
 */
data class DrinkProfile(
    var rancidProgress: Int = 0,
    var previousTick: Long = 0L,
    var isInitialized: Boolean = false
) : CustomData {
    override fun getNbtCategory(): String = "DrinkProfile"

    override fun readNbt(nbt: NbtCompound) {
        rancidProgress = nbt.getInt("Rancid Progress")
        previousTick = nbt.getLong("Previous Tick")
        isInitialized = nbt.getBoolean("Is Initialized")
    }

    override fun writeNbt(nbt: NbtCompound) {
        nbt.putInt("Rancid Progress", rancidProgress)
        nbt.putLong("Previous Tick", previousTick)
        nbt.putBoolean("Is Initialized", isInitialized)
    }
}
