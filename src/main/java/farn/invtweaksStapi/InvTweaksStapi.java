package farn.invtweaksStapi;

import net.fabricmc.loader.api.FabricLoader;
import net.invtweaks.Const;
import net.invtweaks.InvTweaks;
import net.mine_diver.unsafeevents.listener.EventListener;
import net.mine_diver.unsafeevents.listener.ListenerPriority;
import net.minecraft.client.Minecraft;
import net.modificationstation.stationapi.api.client.event.keyboard.KeyStateChangedEvent;
import net.modificationstation.stationapi.api.client.event.option.KeyBindingRegisterEvent;
import net.modificationstation.stationapi.api.event.init.InitFinishedEvent;
import net.modificationstation.stationapi.api.event.tick.GameTickEvent;
import net.modificationstation.stationapi.api.util.Namespace;
import net.modificationstation.stationapi.api.util.Null;
import net.modificationstation.stationapi.api.mod.entrypoint.Entrypoint;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

public class InvTweaksStapi {

    @Entrypoint.Namespace
    public static Namespace NAMESPACE;

    @Entrypoint.Logger
    public static Logger LOGGER = Null.get();

    public static InvTweaks instance;

    private static boolean keyDownSort = false;

    private static Minecraft minecraft;

    @EventListener
    public void pressKey(KeyStateChangedEvent event) {
        if(instance != null && Keyboard.getEventKey() == Const.SORT_KEY_BINDING.code && instance.mc.world != null) {
            if(!keyDownSort) {
                keyDownSort = true;
                instance.onSortingKeyPressed();
            } else {
                keyDownSort = false;
            }
        }
    }

    @EventListener
    public void registerKey(KeyBindingRegisterEvent event) {
        event.keyBindings.add(Const.SORT_KEY_BINDING);
    }

    @EventListener(priority = ListenerPriority.LOW)
    public void init(InitFinishedEvent event) {
        minecraft = (Minecraft) FabricLoader.getInstance().getGameInstance();
        instance = new InvTweaks(minecraft);
        LOGGER.info("TEST");
    }

    @EventListener
    public void gameTickEnd(GameTickEvent.End event) {
        if(minecraft.currentScreen != null) {
            InvTweaksStapi.instance.onTickInGUI(minecraft.currentScreen);
        }

        if(minecraft.world != null) {
            InvTweaksStapi.instance.onTickInGame();
        }
    }

}
