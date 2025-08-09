package net.xenon.towerdefensemod.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.xenon.towerdefensemod.data.TDData;

import java.util.ArrayList;
import java.util.List;

public class TDMoveToCoreZombieMinerGoal extends TDMoveToCoreGoal {
    // Ajouter animation lorsque le zombie casse un block
    // Ajouter la construction de ponts
    private ZombieBehavior zombieBehavior;
    private final int breakTime = 2; // ticks to break
    private int ticksBreaking;
    private final int breakChoiceTime = 20;
    private int tickUntilNextBreakChoice;
    private List<BlockPos> blockPosToDestroy = new ArrayList<>();
    private int airBlockCount;
    private boolean isTargetingFakeCore;

    public TDMoveToCoreZombieMinerGoal(PathfinderMob entity, double speedModifier) {
        super(entity, speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return TDData.coreListContains(this.coreID);
    }

    @Override
    public void start() {
        this.changeBehavior(ZombieBehavior.MOVE);
        this.ticksBreaking = 0;
        this.isTargetingFakeCore = false;
        this.tickUntilNextBreakChoice = this.breakChoiceTime;
        super.start();
    }

    @Override
    public void tick() {
        double entityY = this.entity.getY();
        double coreY = this.corePos.getY();
        if (this.isTargetingFakeCore && entityY <= coreY){
            this.stopFarBreak();
        }

        if (this.zombieBehavior == ZombieBehavior.MOVE){
            if (this.entity.position().distanceTo(this.corePos.getCenter()) < 1.4D) {
                this.breakCoreInit();
            } else if (this.timeMinDistanceNotImproved > 2) {
                System.out.println("Berserk mod !");
            } else if (this.entity.getNavigation().isDone() && this.entity.position().distanceTo(this.corePos.getCenter()) >= this.minDistanceFromCore - 0.2D &&
                    this.tickUntilNextBreakChoice >= this.breakChoiceTime) {
                this.makeZombieBuildOrBreak(entityY, coreY);
            }
        } else if (this.zombieBehavior == ZombieBehavior.CORE_BREAKING){
            this.breakCore();
        } else if (this.zombieBehavior == ZombieBehavior.FORWARD_BREAKING
                || this.zombieBehavior == ZombieBehavior.DOWNWARD_BREAKING) {
            this.breakBlocks();
        }

        if (this.tickUntilNextBreakChoice < this.breakChoiceTime){
            this.tickUntilNextBreakChoice++;
        }
        super.tick();
    }

    public void changeBehavior(ZombieBehavior behavior){
        this.zombieBehavior = behavior;
        switch (behavior){
            case MOVE -> {
                this.canMove();
            }
            case CORE_BREAKING, FORWARD_BREAKING, DOWNWARD_BREAKING -> {
                this.doNotMove();
            }
            case BERSERK_BREAKING -> {

            }
            case CONSTRUCTING -> {
            }
        }
    }

    public void makeZombieBuildOrBreak(double entityY, double coreY) {
        // Choosing between breaking or constructing
        if (coreY < entityY){
            if (this.isTargetingFakeCore) {
                this.breakInit(ZombieBehavior.DOWNWARD_BREAKING);
            }
            else {
                this.farBreakInit(entityY - coreY);
            }
        } else if (coreY - 1 > entityY) {
            //this.zombieBehavior = ZombieBehavior.CONSTRUCTING;
            return;
        }
        else {
            this.breakInit(ZombieBehavior.FORWARD_BREAKING);
        }
    }

    public void farBreakInit(double deltaY){
        // Used when the zombie wants to mins downward and the horizontal distance
        // to travel is not twice as much as the height distance to travel
        // Change the position of the targeted core in order to force the pathfinding to go straight forward
        Vec3 vectorZombieToCore = this.corePos.getCenter().subtract(this.entity.position()).normalize();
        Direction coreDirection = Direction.getNearest(vectorZombieToCore.x, 0, vectorZombieToCore.z);
        Vec3 newPos = this.entity.position().add(coreDirection.getStepX() * deltaY * 5, 0,coreDirection.getStepZ() * deltaY * 5);
        this.corePos = new BlockPos((int)newPos.x, this.corePos.getY(), (int)newPos.z);
        this.isTargetingFakeCore = true;
        // To recalculate a path directly after
        this.changeBehavior(ZombieBehavior.MOVE);
    }

    public void stopFarBreak(){
        this.corePos = TDData.getCorePositionFromID(this.coreID);
        this.isTargetingFakeCore = false;
        this.changeBehavior(ZombieBehavior.MOVE);
    }

    public void breakInit(ZombieBehavior behavior){
        this.changeBehavior(behavior);
        this.ticksBreaking = 0;
        this.blockPosToDestroy.clear();

        // Find blocks to destroy
        // To end properly the downward breaking while targeting fake core
        if (this.zombieBehavior == ZombieBehavior.DOWNWARD_BREAKING &&
                this.isTargetingFakeCore && this.entity.position().y <= this.corePos.getCenter().y + 1.5D){
            BlockPos targetPosBreakStair = this.entity.blockPosition().above(-1);
            this.blockPosToDestroy.add(targetPosBreakStair);
        }
        else {
            Vec3 vectorZombieToCore = this.corePos.getCenter().subtract(this.entity.position()).normalize();
            Direction coreDirection = Direction.getNearest(vectorZombieToCore.x, 0, vectorZombieToCore.z);

            BlockPos targetPosFoot = this.entity.blockPosition().relative(coreDirection);
            BlockPos targetPosEye = this.entity.blockPosition().above(1).relative(coreDirection);
            this.blockPosToDestroy.add(targetPosEye);
            this.blockPosToDestroy.add(targetPosFoot);
            if (behavior == ZombieBehavior.DOWNWARD_BREAKING) {
                BlockPos targetPosUnderFoot = this.entity.blockPosition().above(-1).relative(coreDirection);
                this.blockPosToDestroy.add(targetPosUnderFoot);
            }
        }

        this.airBlockCount = this.blockPosToDestroy.size();
    }

    public void breakBlocks(){
        Level level = this.entity.level();
        BlockPos targetBlockPos = this.blockPosToDestroy.getFirst();
        BlockState targetBlockState = level.getBlockState(targetBlockPos);

        if (targetBlockState.isAir()){
            this.airBlockCount--;
            this.blockPosToDestroy.removeFirst();
            if (this.blockPosToDestroy.isEmpty()){
                this.changeBehavior(ZombieBehavior.MOVE);
                if (this.airBlockCount == 0){
                    this.tickUntilNextBreakChoice = 0;
                    if (this.isTargetingFakeCore){
                        this.stopFarBreak();
                    }
                }
            }
            return;
        }

        if (this.entity.position().distanceTo(targetBlockPos.getCenter()) > 3D){
            this.changeBehavior(ZombieBehavior.MOVE);
            return;
        }

        this.ticksBreaking++;
        if (this.ticksBreaking >= this.breakTime) {
            level.destroyBlock(targetBlockPos, false, this.entity);
            this.blockPosToDestroy.removeFirst();
            if (TDData.coreListContains(targetBlockPos)){
                TDData.removeCoreFromPosition(targetBlockPos);
            }
            this.ticksBreaking = 0;
            if (this.blockPosToDestroy.isEmpty()){
                this.changeBehavior(ZombieBehavior.MOVE);
            }
        }
    }

    public void breakCoreInit(){
        this.changeBehavior(ZombieBehavior.CORE_BREAKING);
        this.ticksBreaking = 0;
    }

    public void breakCore() {
        Level level = this.entity.level();
        BlockState coreBlockState = level.getBlockState(this.corePos);

        if (coreBlockState.isAir()){
            TDData.removeCoreFromPosition(this.corePos);
            return;
        }

        if (this.entity.position().distanceTo(this.corePos.getCenter()) > 2D){
            this.changeBehavior(ZombieBehavior.MOVE);
            return;
        }

        this.ticksBreaking++;
        if (this.ticksBreaking >= this.breakTime) {
            level.destroyBlock(this.corePos, false, this.entity);
            TDData.removeCoreFromPosition(this.corePos);
            this.changeBehavior(ZombieBehavior.MOVE);
            this.ticksBreaking = 0;
        }
    }

    public static enum ZombieBehavior{
        MOVE,
        CORE_BREAKING,
        FORWARD_BREAKING,
        DOWNWARD_BREAKING,
        BERSERK_BREAKING,
        CONSTRUCTING
    }
}
