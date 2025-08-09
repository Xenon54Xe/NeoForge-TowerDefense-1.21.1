package net.xenon.towerdefensemod.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.xenon.towerdefensemod.TowerDefenseMod;
import net.xenon.towerdefensemod.ai.TDMoveToCoreCreeperGoal;
import net.xenon.towerdefensemod.ai.TDMoveToCoreGoal;
import net.xenon.towerdefensemod.ai.TDMoveToCoreZombieMinerGoal;
import net.xenon.towerdefensemod.data.TDData;

@EventBusSubscriber(modid = TowerDefenseMod.MODID)
public class TDEvents extends Event {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinLevelEvent event){
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof net.minecraft.world.entity.PathfinderMob mob)){
            return;
        }
        if (mob instanceof Zombie){
            // Donner une pioche à un zombie et donner le goal zombie
            mob.goalSelector.addGoal(7, new TDMoveToCoreZombieMinerGoal(mob, 1));
            mob.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WOODEN_PICKAXE));
        }
        if (mob instanceof Creeper creeper){
            mob.goalSelector.addGoal(5, new TDMoveToCoreCreeperGoal(creeper, 1));
        }
        if (mob instanceof AbstractSkeleton && !(mob instanceof WitherSkeleton)){
            mob.goalSelector.addGoal(5, new TDMoveToCoreGoal(mob, 1));
        }
        if (mob instanceof Spider){
            mob.goalSelector.addGoal(5, new TDMoveToCoreGoal(mob, 1));
        }
    }

    @SubscribeEvent
    public static void onPlayerChat(ServerChatEvent event){
        Component message =  event.getMessage();
        String text = message.getString();
        String[] textList = text.split(" ");
        float x = Float.parseFloat(textList[0]);
        float y = Float.parseFloat(textList[1]);
        float z = Float.parseFloat(textList[2]);
        Vec3 position = new Vec3(x, y, z);
        BlockPos blockPos = new BlockPos((int)position.x, (int)position.y, (int)position.z);
        if (TDData.coreListContains(blockPos)){
            TDData.removeCoreFromPosition(blockPos);
        }
        else {
            TDData.addCore(blockPos);
        }
        System.out.println(TDData.getCoreIDList());
        System.out.println(TDData.getCorePositionList());
    }

    @SubscribeEvent
    public static void onPlayerPlaceBlock(BlockEvent.EntityPlaceEvent event){
        if (!event.getLevel().isClientSide() && event.getPlacedBlock().is(Blocks.DIAMOND_BLOCK)){
            TDData.addCore(event.getPos());
        }
        System.out.println(TDData.getCoreIDList());
        System.out.println(TDData.getCorePositionList());
    }

    @SubscribeEvent
    public static void onPlayerRemoveBlock(BlockEvent.BreakEvent event){
        if (!event.getLevel().isClientSide() && TDData.coreListContains(event.getPos())){
            TDData.removeCoreFromPosition(event.getPos());
        }
        System.out.println(TDData.getCoreIDList());
        System.out.println(TDData.getCorePositionList());
    }

    @SubscribeEvent
    public static void onExplosionGrief(ExplosionEvent.Detonate event){
        for (BlockPos blockPos: event.getAffectedBlocks()){
            if (TDData.coreListContains(blockPos)){
                TDData.removeCoreFromPosition(blockPos);
            }
        }
        System.out.println(TDData.getCoreIDList());
        System.out.println(TDData.getCorePositionList());
    }
}
