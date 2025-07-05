package at.petrak.hexcasting.api.addldata;

import at.petrak.hexcasting.api.casting.iota.Iota;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import at.petrak.hexcasting.api.utils.Vector;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ADHexHolder {

    boolean canDrawMediaFromInventory();

    boolean hasHex();

    @Nullable
    Vector<Iota> getHex(ServerLevel level);

    void writeHex(Vector<Iota> patterns, @Nullable FrozenPigment pigment, long media);

    void clearHex();

    @Nullable FrozenPigment getPigment();
}
