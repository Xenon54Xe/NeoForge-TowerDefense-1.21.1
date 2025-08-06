package net.xenon.towerdefensemod.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.xenon.towerdefensemod.data.TDData;

import java.util.EnumSet;


public class TDMoveToCoreGoal extends Goal {
    protected final PathfinderMob entity;
    private final double speedModifier;
    protected int coreID;
    protected BlockPos corePos;
    private Path path;
    protected int tickUntilNextPathRecalculation;

    public TDMoveToCoreGoal(PathfinderMob entity, double speedModifier){
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected int findNearestCore(){
        // Return the index in the TDData positionCoreList of the nearest core
        Vec3 entityPos = this.entity.position();
        double minDistance = entityPos.distanceTo(TDData.getFirstCore().getCenter());
        int index = 0;
        for (int i = 0; i < TDData.getCoreListSize(); i++){
            BlockPos corePos = TDData.getCore(i);
            double distance = entityPos.distanceTo(corePos.getCenter());
            if (distance < minDistance){
                minDistance = distance;
                index = i;
            }
        }
        return index;
    }

    @Override
    public boolean canUse() {
        // Time between uses
        if (TDData.isCoreListEmpty()){
            return false;
        }

        int indexCore = this.findNearestCore();
        this.coreID = TDData.getCoreId(indexCore);
        this.corePos = TDData.getCore(indexCore);
        this.path = this.entity.getNavigation().createPath(this.corePos, 1);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.entity.getNavigation().isDone() && TDData.coreListContainsID(this.coreID) && this.entity.position().distanceTo(this.corePos.getCenter()) > 2D;
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.path, this.speedModifier);
        this.tickUntilNextPathRecalculation = 0;
    }

    @Override
    public void stop() {
        this.entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (!this.entity.getNavigation().moveTo(path, this.speedModifier)){
            this.tickUntilNextPathRecalculation += 30;
        }
        if (this.tickUntilNextPathRecalculation >= 30){
            this.path = this.entity.getNavigation().createPath(this.corePos, 1);
            this.tickUntilNextPathRecalculation = 0;
        }
        this.tickUntilNextPathRecalculation ++;
    }
}
