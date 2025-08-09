package net.xenon.towerdefensemod.ai;

import net.minecraft.world.entity.monster.Creeper;
import net.xenon.towerdefensemod.data.TDData;

public class TDMoveToCoreCreeperGoal extends TDMoveToCoreGoal {
    public TDMoveToCoreCreeperGoal(Creeper entity, double speedModifier) {
        super(entity, speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return TDData.coreListContains(this.coreID);
    }

    @Override
    public void tick() {
        if ((this.entity.getNavigation().isDone() && this.timeMinDistanceNotImproved > 2) ||
                this.entity.position().distanceTo(this.corePos.getCenter()) < 2D){
            this.doNotMove();
            ((Creeper) this.entity).ignite();
            return;
        }
        super.tick();
    }
}
