package net.xenon.towerdefensemod.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.xenon.towerdefensemod.data.TDData;

public class TDMoveToCoreZombieGoal extends TDMoveToCoreGoal {
    // Ajouter animation lorsque le zombie casse un block
    // Ajouter la possibilité de casser les murs avec sa pioche
    // Ajouter la construction de ponts
    private double minDistanceToCore;
    private final int breakTime = 40; // ticks to break
    private int ticksBreaking = 0;

    public TDMoveToCoreZombieGoal(PathfinderMob entity, double speedModifier) {
        super(entity, speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return ticksBreaking > 0 || (!this.entity.getNavigation().isDone() && TDData.coreListContainsID(this.coreID));
    }

    @Override
    public void start() {
        super.start();
        this.minDistanceToCore = this.entity.position().distanceTo(this.corePos.getCenter());
        this.ticksBreaking = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.entity.position().distanceTo(this.corePos.getCenter()) < 2D) {
            this.breakCore();
        }
        //if ((this.entity.getNavigation().isDone() && this.entity.position().distanceTo(this.corePos) >= this.minDistanceToCore - 1)) {
        // this.makeZombieBuildOrBreak();
        if (this.entity.getNavigation().isDone()) {
            this.minDistanceToCore = this.entity.position().distanceTo(this.corePos.getCenter());
        }
    }

    public void breakCore() {
        System.out.println("Breaking !");
        System.out.println(ticksBreaking);
        Level level = this.entity.level();
        BlockState coreBlockState = level.getBlockState(this.corePos);

        if (!coreBlockState.isAir()) {
            ticksBreaking++;
            if (ticksBreaking >= breakTime) {
                level.destroyBlock(this.corePos, true, entity);
                TDData.removeCore(this.corePos);
                ticksBreaking = 0;
            }
        }
    }
}
