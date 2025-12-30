package fr.dragone.drstone.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class CampfireSavedData extends SavedData {

    private static final String NAME = "drstone_campfires";
    public static final int MAX_BURN = 60;

    public static class CampfireState {
        public ItemStack fuel = ItemStack.EMPTY;
        public ItemStack result1 = ItemStack.EMPTY;
        public ItemStack result2 = ItemStack.EMPTY;
        public int burnTime = MAX_BURN;
    }

    private final Map<String, CampfireState> campfires = new HashMap<>();

    public static CampfireSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        (Supplier<CampfireSavedData>) CampfireSavedData::new,
                        (BiFunction<CompoundTag, HolderLookup.Provider, CampfireSavedData>) CampfireSavedData::load,
                        DataFixTypes.LEVEL
                ),
                NAME
        );
    }

    public CampfireState getState(BlockPos pos) {
        return campfires.computeIfAbsent(key(pos), k -> new CampfireState());
    }

    public void remove(BlockPos pos) {
        campfires.remove(key(pos));
        setDirty();
    }

    public void forEachCampfire(BiConsumer<BlockPos, CampfireState> action) {
        campfires.forEach((k, v) -> action.accept(parsePos(k), v));
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider provider) {
        CompoundTag all = new CompoundTag();

        campfires.forEach((k, s) -> {
            CompoundTag t = new CompoundTag();
            t.put("Fuel", s.fuel.save(provider));
            t.put("R1", s.result1.save(provider));
            t.put("R2", s.result2.save(provider));
            t.putInt("Burn", s.burnTime);
            all.put(k, t);
        });

        tag.put("Campfires", all);
        return tag;
    }

    public static CampfireSavedData load(CompoundTag tag, HolderLookup.Provider provider) {
        CampfireSavedData data = new CampfireSavedData();
        CompoundTag all = tag.getCompound("Campfires");

        for (String k : all.getAllKeys()) {
            CompoundTag t = all.getCompound(k);
            CampfireState s = new CampfireState();
            s.fuel = ItemStack.parseOptional(provider, t.getCompound("Fuel"));
            s.result1 = ItemStack.parseOptional(provider, t.getCompound("R1"));
            s.result2 = ItemStack.parseOptional(provider, t.getCompound("R2"));
            s.burnTime = t.getInt("Burn");
            data.campfires.put(k, s);
        }

        return data;
    }

    private static String key(BlockPos p) {
        return p.getX() + "," + p.getY() + "," + p.getZ();
    }

    private static BlockPos parsePos(String k) {
        String[] s = k.split(",");
        return new BlockPos(
                Integer.parseInt(s[0]),
                Integer.parseInt(s[1]),
                Integer.parseInt(s[2])
        );
    }
}
