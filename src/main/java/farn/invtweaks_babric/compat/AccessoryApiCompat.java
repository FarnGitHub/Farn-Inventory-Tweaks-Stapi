package farn.invtweaks_babric.compat;

import com.periut.accessoryapi.impl.slot.AccessorySlot;
import com.periut.accessoryapi.impl.slot.AccessorySlotStorage;
import net.invtweaks.library.ContainerManager;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;

public class AccessoryApiCompat {

    public static int fix(PlayerScreenHandler handler, ContainerManager manager) {
        List<Slot> accessorySlots = new ArrayList<>();
        for(int index = 0; index < handler.slots.size(); ++index)
            if(handler.slots.get(index) instanceof AccessorySlot slot)
                accessorySlots.add(slot);
        manager.slotRefs.put(ContainerManager.ContainerSection.UNKNOWN, accessorySlots);
        return accessorySlots.size();
    }
}
