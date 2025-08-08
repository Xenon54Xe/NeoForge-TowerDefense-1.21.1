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
    protected double lastDistanceToCore;
    protected boolean canMove = true;


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
        if (TDData.isCoreListEmpty()){
            return false;
        }

        // Initialisation
        int indexCore = this.findNearestCore();
        this.coreID = TDData.getCoreId(indexCore);
        this.corePos = TDData.getCore(indexCore);
        this.path = this.entity.getNavigation().createPath(this.corePos, 1);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return TDData.coreListContainsID(this.coreID) && this.entity.position().distanceTo(this.corePos.getCenter()) > 2D &&
                this.entity.position().distanceTo(this.corePos.getCenter()) > this.lastDistanceToCore - 1;
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.path, this.speedModifier);
        this.tickUntilNextPathRecalculation = 0;
        this.lastDistanceToCore = this.entity.position().distanceTo(this.corePos.getCenter());
    }

    @Override
    public void stop() {
        this.entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.canMove) {
            if (this.entity.getNavigation().isDone()) {
                this.lastDistanceToCore = this.entity.position().distanceTo(this.corePos.getCenter());
            }

            if (!this.entity.getNavigation().moveTo(path, this.speedModifier)) {
                this.tickUntilNextPathRecalculation += 30;
            }
            if (this.tickUntilNextPathRecalculation >= 30 || this.entity.getNavigation().isDone()) {
                this.path = this.entity.getNavigation().createPath(this.corePos, 1);
                this.entity.getNavigation().moveTo(this.path, this.speedModifier);
                this.tickUntilNextPathRecalculation = 0;
            }
            this.tickUntilNextPathRecalculation++;
        }
    }

    public void doNotMove(){
        this.canMove = false;
        this.entity.getNavigation().stop();
    }

    public void canMove(){
        this.canMove = true;
        this.entity.getNavigation().moveTo(this.path, this.speedModifier);
        this.tickUntilNextPathRecalculation = 0;
    }
}
