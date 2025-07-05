package at.petrak.hexcasting.api.casting.iota;

import at.petrak.hexcasting.api.utils.HexUtils;
import at.petrak.hexcasting.api.utils.Vector;
import at.petrak.hexcasting.common.lib.hex.HexIotaTypes;
import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Function;

import static java.lang.Math.max;

/**
 * This is a <i>wrapper</i> for {@link Vector<Iota>}.
 */
public class ListIota extends Iota {
    private Vector<Iota> list;
    private final int depth;
    private final int size;

    public ListIota(@NotNull Vector<Iota> list) {
        super(() -> HexIotaTypes.LIST);
        this.list = list;
        int maxChildDepth = 0;
        int totalSize = 1;
        for (Iota iota : list) {
            totalSize += iota.size();
            maxChildDepth = max(maxChildDepth, iota.depth());
        }
        depth = maxChildDepth + 1;
        size = totalSize;
    }

    public Vector<Iota> getList() {
        return this.list;
    }

    @Override
    public boolean isTruthy() {
        return !this.getList().isEmpty();
    }

    @Override
    public boolean toleratesOther(Iota that) {
        if (!typesMatch(this, that)) {
            return false;
        }
        var a = this.getList();
        if (!(that instanceof ListIota list)) {
            return false;
        }
        var b = list.getList();

        Iterator<Iota> aIter = a.iterator(), bIter = b.iterator();
        for (; ; ) {
            if (!aIter.hasNext() && !bIter.hasNext()) {
                // we ran out together!
                return true;
            }
            if (aIter.hasNext() != bIter.hasNext()) {
                // one remains full before the other
                return false;
            }
            Iota x = aIter.next(), y = bIter.next();
            if (!Iota.tolerates(x, y)) {
                return false;
            }
        }
    }

    @Override
    public @Nullable Iterable<Iota> subIotas() {
        return this.getList();
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public int depth() {
        return depth;
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public Component display() {
        var out = Component.empty();

        for (int i = 0; i < list.size(); i++) {
            var sub = list.get(i);

            out.append(sub.display());

            // only add a comma between 2 non-patterns (commas don't look good with Inline patterns)
            // TODO: maybe add a config? maybe add a method on IotaType to allow it to opt out of commas
            if (i < list.size() - 1 && (sub.type != PatternIota.TYPE
                    || list.get(i + 1).type != PatternIota.TYPE)) {
                out.append(", ");
            }
        }
        return Component.translatable("hexcasting.tooltip.list_contents", out).withStyle(ChatFormatting.DARK_PURPLE);
    }

    public static IotaType<ListIota> TYPE = new IotaType<>() {
        public static final MapCodec<ListIota> CODEC = IotaType.TYPED_CODEC
                .listOf()
                .xmap(Vector::from, Function.identity())
                .xmap(ListIota::new, ListIota::getList)
                .fieldOf("list");
        public static final StreamCodec<RegistryFriendlyByteBuf, ListIota> STREAM_CODEC =
                IotaType.TYPED_STREAM_CODEC.apply(ByteBufCodecs.list()).map(Vector::from, Function.identity()).map(ListIota::new, ListIota::getList);

        @Override
        public MapCodec<ListIota> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ListIota> streamCodec() {
            return STREAM_CODEC;
        }

        @Override
        public int color() {
            return 0xff_aa00aa;
        }
    };
}
