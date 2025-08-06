package net.xenon.towerdefensemod.ai;

import net.minecraft.world.entity.monster.Creeper;
import net.xenon.towerdefensemod.data.TDData;

public class TDMoveToCoreCreeperGoal extends TDMoveToCoreGoal {
    private double minDistanceToCore;

    public TDMoveToCoreCreeperGoal(Creeper entity, double speedModifier) {
        super(entity, speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return TDData.coreListContainsID(this.coreID);
    }

    @Override
    public void start() {
        super.start();
        this.minDistanceToCore = this.entity.position().distanceTo(this.corePos);
    }

    @Override
    public void tick() {
        super.tick();

        if ((this.entity.getNavigation().isDone() && this.entity.position().distanceTo(this.corePos) >= this.minDistanceToCore - 1) ||
                this.entity.position().distanceTo(this.corePos) < 2D){
            ((Creeper) this.entity).ignite();
        } else if (this.entity.getNavigation().isDone()){
            this.minDistanceToCore = this.entity.position().distanceTo(this.corePos);
        }
    }
}
