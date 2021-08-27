package com.redgrapefruit.cryonic.client.screen

import com.redgrapefruit.cryonic.MOD_ID
import com.redgrapefruit.redmenu.redmenu.MenuScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * A fridge rendering screen implementing the Container API
 */
class FridgeScreen(handler: ScreenHandler, inventory: PlayerInventory, title: Text) :
    MenuScreen(handler, inventory, title) {

    override val texture: Identifier = Identifier(MOD_ID, "textures/gui/fridge.png")
}