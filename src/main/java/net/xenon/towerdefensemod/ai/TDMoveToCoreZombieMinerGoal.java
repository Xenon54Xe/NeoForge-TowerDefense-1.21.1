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
    private final int breakTime = 5; // ticks to break
    private int ticksBreaking = 0;
    private List<BlockPos> blockPosToDestroy = new ArrayList<>();
    private int tickUntilNextBlockBreaking;

    public TDMoveToCoreZombieMinerGoal(PathfinderMob entity, double speedModifier) {
        super(entity, speedModifier);
    }

    @Override
    public boolean canContinueToUse() {
        return TDData.coreListContainsID(this.coreID);
    }

    @Override
    public void start() {
        super.start();
        this.changeBehavior(ZombieBehavior.MOVE);
        this.tickUntilNextBlockBreaking = 0;
        this.ticksBreaking = 0;
    }

    @Override
    public void tick() {
        System.out.println(this.zombieBehavior);
        System.out.println(this.lastDistanceToCore);
        if (this.zombieBehavior == ZombieBehavior.MOVE){
            if (this.entity.position().distanceTo(this.corePos.getCenter()) < 2D) {
                this.breakCoreInit();
            }
            else if (this.entity.getNavigation().isDone() && this.entity.position().distanceTo(this.corePos.getCenter()) >= this.lastDistanceToCore - 1
                    && this.tickUntilNextBlockBreaking >= 20) {
                this.makeZombieBuildOrBreak();
            }
        } else if (this.zombieBehavior == ZombieBehavior.CORE_BREAKING){
            this.breakCore();
        } else if (this.zombieBehavior == ZombieBehavior.FORWARD_BREAKING
                || this.zombieBehavior == ZombieBehavior.DOWNWARD_BREAKING) {
            this.breakBlocks();
        }

        if (this.tickUntilNextBlockBreaking < 20){
            this.tickUntilNextBlockBreaking++;
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
            case CONSTRUCTING -> {
            }
        }
    }

    public void makeZombieBuildOrBreak(){
        // Choosing between breaking or constructing
        double entityY = this.entity.getY();
        double coreY = this.corePos.getY();
        System.out.println(entityY);
        System.out.println(coreY);
        if (coreY < entityY){
            System.out.println("Downward !");
            this.breakInit(ZombieBehavior.DOWNWARD_BREAKING);
        } else if (coreY - 1 > entityY) {
            //this.zombieBehavior = ZombieBehavior.CONSTRUCTING;
            return;
        }
        else {
            this.breakInit(ZombieBehavior.FORWARD_BREAKING);
        }
    }

    public void breakInit(ZombieBehavior behavior){
        this.changeBehavior(behavior);
        this.ticksBreaking = 0;
        this.blockPosToDestroy.clear();

        // Find blocks to destroy
        Vec3 vectorZombieToCore = this.corePos.getCenter().subtract(this.entity.position()).normalize();
        Direction coreDirection = Direction.getNearest(vectorZombieToCore.x, 0, vectorZombieToCore.z);
        BlockPos targetPosFoot = this.entity.blockPosition().relative(coreDirection);
        BlockPos targetPosEye = this.entity.blockPosition().above(1).relative(coreDirection);
        this.blockPosToDestroy.add(targetPosEye);
        this.blockPosToDestroy.add(targetPosFoot);
        if (behavior == ZombieBehavior.DOWNWARD_BREAKING){
            BlockPos targetPosUnderFoot = this.entity.blockPosition().above(-1).relative(coreDirection);
            this.blockPosToDestroy.add(targetPosUnderFoot);
        }
    }

    public void breakBlocks(){
        Level level = this.entity.level();
        BlockPos targetBlockPos = this.blockPosToDestroy.getFirst();
        BlockState targetBlockState = level.getBlockState(targetBlockPos);

        if (targetBlockState.isAir()){
            System.out.println("Air !");
            this.blockPosToDestroy.removeFirst();
            if (this.blockPosToDestroy.isEmpty()){
                // Si le zombie arrive dans cette condition c'est que le pathfinding est bloqué sur un block
                // Solution ?: lui donner pour objectif un core qui est situé très loins dans la direction du core réel et situé à la même hauteur
                // et le faire miner pour aller là bas
                this.changeBehavior(ZombieBehavior.MOVE);
                this.tickUntilNextBlockBreaking = 0;
            }
            return;
        }

        if (this.entity.position().distanceTo(targetBlockPos.getCenter()) > 2D){
            System.out.println("Trop loins !");
            this.changeBehavior(ZombieBehavior.MOVE);
            return;
        }

        System.out.println("Breaking !");
        System.out.println(this.ticksBreaking);

        this.ticksBreaking++;
        if (this.ticksBreaking >= this.breakTime) {
            level.destroyBlock(targetBlockPos, false, this.entity);
            this.blockPosToDestroy.removeFirst();
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
            TDData.removeCore(this.corePos);
            return;
        }

        if (this.entity.position().distanceTo(this.corePos.getCenter()) > 2D){
            this.changeBehavior(ZombieBehavior.MOVE);
            return;
        }

        System.out.println("Breaking !");
        System.out.println(this.ticksBreaking);

        this.ticksBreaking++;
        if (this.ticksBreaking >= this.breakTime) {
            level.destroyBlock(this.corePos, false, this.entity);
            TDData.removeCore(this.corePos);
            this.changeBehavior(ZombieBehavior.MOVE);
            this.ticksBreaking = 0;
        }
    }

    public static enum ZombieBehavior{
        MOVE,
        CORE_BREAKING,
        FORWARD_BREAKING,
        DOWNWARD_BREAKING,
        FAR_DOWNWARD_BREAKING,
        CONSTRUCTING
    }
}
