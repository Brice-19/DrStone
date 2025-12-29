package fr.dragone.drstone.block.entity;

import fr.dragone.drstone.block.ArchetBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ArchetBlockEntity extends BlockEntity {

    private int burnTime = 20 * 4; // 4 secondes
    private boolean holding = false;

    public ArchetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ARCHET.get(), pos, state);
    }

    /* ================= FIRE ================= */

    public void startFire() {
        burnTime = 20 * 4;
        setChanged();
        sync();
    }

    public void setHolding(boolean value) {
        this.holding = value;
    }

    /* ================= TICK ================= */

    public static void tick(Level level, BlockPos pos, BlockState state, ArchetBlockEntity be) {

        if (state.getValue(ArchetBlock.STAGE) != 3) return;

        boolean isHolding = be.holding;
        be.holding = false;

        // ❌ pas d’appui → rien
        if (!isHolding) return;

        // 💨 particules pendant l’appui (serveur → client)
        if (!level.isClientSide) {
            ((ServerLevel) level).sendParticles(
                    ParticleTypes.SMOKE,
                    pos.getX() + 0.5,
                    pos.getY() + 0.3,
                    pos.getZ() + 0.5,
                    1,
                    0, 0.02, 0,
                    0.0
            );
        }

        be.burnTime--;

        // 🔥 feu terminé → STAGE 4
        if (be.burnTime <= 0 && !level.isClientSide) {
            level.setBlock(pos, state.setValue(ArchetBlock.STAGE, 4), 3);
            be.sync();
        }
    }

    /* ================= SAVE / LOAD ================= */

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putInt("BurnTime", burnTime);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        burnTime = tag.getInt("BurnTime");
    }

    /* ================= SYNC ================= */

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return saveWithoutMetadata(provider);
    }

    private void sync() {
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
