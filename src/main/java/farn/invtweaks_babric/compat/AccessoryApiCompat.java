package farn.invtweaks_babric.compat;

import com.periut.accessoryapi.impl.slot.AccessorySlot;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.Slot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AccessoryApiCompat {

    public static int fix(PlayerScreenHandler handler, Consumer<List<Slot>> consumer) {
        List<Slot> accessorySlots = new ArrayList<>();
        for(int index = 0; index < handler.slots.size(); ++index)
            if(handler.slots.get(index) instanceof AccessorySlot slot)
                accessorySlots.add(slot);
        consumer.accept(accessorySlots);
        return accessorySlots.size();
    }
}
