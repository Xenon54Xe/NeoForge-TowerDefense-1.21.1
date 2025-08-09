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
    protected double minDistanceFromCore;
    protected boolean canMove = true;
    protected int timeMinDistanceNotImproved;



    public TDMoveToCoreGoal(PathfinderMob entity, double speedModifier){
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    protected int findNearestCore(){
        // Return the index in the TDData positionCoreList of the nearest core
        Vec3 entityPos = this.entity.position();
        double minDistance = entityPos.distanceTo(TDData.getFirstCorePosition().getCenter());
        int index = 0;
        for (int i = 0; i < TDData.getCoreNumber(); i++){
            BlockPos corePos = TDData.getCorePositionFromIndex(i);
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
        this.coreID = TDData.getCoreIdFromIndex(indexCore);
        this.corePos = TDData.getCorePositionFromIndex(indexCore);
        this.path = this.entity.getNavigation().createPath(this.corePos, 1);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return TDData.coreListContains(this.coreID) && this.entity.position().distanceTo(this.corePos.getCenter()) > 2D &&
                this.entity.position().distanceTo(this.corePos.getCenter()) > this.minDistanceFromCore - 1;
    }

    @Override
    public void start() {
        this.entity.getNavigation().moveTo(this.path, this.speedModifier);
        this.minDistanceFromCore = this.entity.position().distanceTo(this.corePos.getCenter());
        this.tickUntilNextPathRecalculation = 0;
        this.timeMinDistanceNotImproved = 0;
    }

    @Override
    public void stop() {
        this.entity.getNavigation().stop();
    }

    @Override
    public void tick() {
        if (this.canMove) {
            if (this.entity.getNavigation().isDone()) {
                this.improvedMinDistanceFromCore(this.entity.position().distanceTo(this.corePos.getCenter()));
            }

            if (this.tickUntilNextPathRecalculation >= 30 || this.entity.getNavigation().isDone()) {
                this.path = this.entity.getNavigation().createPath(this.corePos, 1);
                this.entity.getNavigation().moveTo(this.path, this.speedModifier);
                this.tickUntilNextPathRecalculation = 0;
            }
            if (!this.entity.getNavigation().moveTo(path, this.speedModifier)) {
                this.tickUntilNextPathRecalculation += 30;
            }
            this.tickUntilNextPathRecalculation++;
        }
    }

    public void improvedMinDistanceFromCore(double distance){
        if (distance < this.minDistanceFromCore){
            this.minDistanceFromCore = distance;
            this.timeMinDistanceNotImproved = 0;
        }
        else {
            this.timeMinDistanceNotImproved++;
        }
    }

    public void doNotMove(){
        this.entity.getNavigation().stop();
        this.canMove = false;
    }

    public void canMove(){
        this.path = this.entity.getNavigation().createPath(this.corePos, 1);
        this.entity.getNavigation().moveTo(this.path, this.speedModifier);
        this.tickUntilNextPathRecalculation = 0;
        this.canMove = true;
    }
}
