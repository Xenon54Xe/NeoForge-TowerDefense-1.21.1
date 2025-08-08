package net.xenon.towerdefensemod.ai;

import net.minecraft.world.entity.monster.Creeper;
import net.xenon.towerdefensemod.data.TDData;

public class TDMoveToCoreCreeperGoal extends TDMoveToCoreGoal {
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
    }

    @Override
    public void tick() {
        if ((this.entity.getNavigation().isDone() && this.entity.position().distanceTo(this.corePos.getCenter()) >= this.lastDistanceToCore - 1) ||
                this.entity.position().distanceTo(this.corePos.getCenter()) < 2D){
            ((Creeper) this.entity).ignite();
            return;
        }
        super.tick();
    }
}
