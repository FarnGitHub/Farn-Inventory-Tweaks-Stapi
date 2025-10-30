package net.invtweaks;

import net.fabricmc.api.ClientModInitializer;
import net.ornithemc.osl.lifecycle.api.MinecraftEvents;
import net.ornithemc.osl.lifecycle.api.WorldEvents;
import org.lwjgl.input.Keyboard;

public class OrnitheInit implements ClientModInitializer {

	boolean pressed = false;

	@Override
	public void onInitializeClient() {
		MinecraftEvents.READY.register(minecraft -> {
			new InvTweaks(minecraft);
		});
		MinecraftEvents.TICK_END.register(minecraft -> {
			if(minecraft.screen != null && InvTweaks.getInstance() != null) {
				InvTweaks.getInstance().onTickInGUI(minecraft.screen);
			}
		});
		WorldEvents.TICK_END.register(world -> {
			if(InvTweaks.getInstance() != null) {
				InvTweaks.getInstance().onTickInGame();
				if(Keyboard.getEventKeyState() && Keyboard.getEventKey() == Const.SORT_KEY_BINDING.keyCode) {
					if (!pressed) {
						pressed = true;
						InvTweaks.getInstance().onSortingKeyPressed();
					}
				} else {
					pressed = false;
				}
			}
		});
	}
}
