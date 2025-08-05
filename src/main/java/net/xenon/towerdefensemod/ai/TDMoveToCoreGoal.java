package net.xenon.towerdefensemod.ai;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import net.xenon.towerdefensemod.data.TDData;

import java.util.EnumSet;


public class TDMoveToCoreGoal extends Goal {
    protected final PathfinderMob entity;
    protected final double speedModifier;
    private Vec3 corePos;
    private Path path;
    private int tickUntilNextPathRecalculation;

    public TDMoveToCoreGoal(PathfinderMob entity, double speedModifier){
        this.entity = entity;
        this.speedModifier = speedModifier;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    private Vec3 findNearestCore(){
        Vec3 entityPos = this.entity.position();
        double minDistance = entityPos.distanceTo(TDData.coreList.getFirst());
        int index = 0;
        for (int i = 0; i < TDData.coreList.size(); i++){
            Vec3 corePos = TDData.coreList.get(i);
            double distance = entityPos.distanceTo(corePos);
            if (distance < minDistance){
                minDistance = distance;
                index = i;
            }
        }
        return TDData.coreList.get(index);
    }

    @Override
    public boolean canUse() {
        System.out.println("CanUse !");
        // Time between uses
        if (TDData.coreList.isEmpty()){
            return false;
        }
        this.corePos = this.findNearestCore();
        this.path = this.entity.getNavigation().createPath(this.corePos.x, this.corePos.y, this.corePos.z, 2);
        return path != null;
    }

    @Override
    public boolean canContinueToUse() {
        return !this.entity.getNavigation().isDone() && !TDData.coreList.isEmpty();
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
            this.tickUntilNextPathRecalculation += 25;
        }
        if (this.tickUntilNextPathRecalculation >= 25){
            this.path = this.entity.getNavigation().createPath(this.corePos.x, this.corePos.y, this.corePos.z, 2);
            this.tickUntilNextPathRecalculation = 0;
        }
        this.tickUntilNextPathRecalculation ++;
    }
}
