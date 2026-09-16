package farn.invtweaks_babric.compat;

import net.minecraft.item.Item;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.util.Identifier;

import java.util.Optional;

public class StationAPICompat {

    public static int parseItem(String id) {
        Optional<Item> itemOp = ItemRegistry.INSTANCE.getOrEmpty(Identifier.tryParse(id));
        return itemOp.map((item) -> item.id).orElse(-1);
    }
}
