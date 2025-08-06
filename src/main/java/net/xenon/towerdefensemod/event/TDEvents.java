package net.xenon.towerdefensemod.event;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.xenon.towerdefensemod.TowerDefenseMod;
import net.xenon.towerdefensemod.ai.TDMoveToCoreCreeperGoal;
import net.xenon.towerdefensemod.ai.TDMoveToCoreGoal;
import net.xenon.towerdefensemod.data.TDData;

@EventBusSubscriber(modid = TowerDefenseMod.MODID)
public class TDEvents extends Event {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onEntityJoinWorld(EntityJoinLevelEvent event){
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof net.minecraft.world.entity.PathfinderMob mob)){
            return;
        }
        if (mob instanceof Zombie){
            mob.goalSelector.addGoal(8, new TDMoveToCoreGoal(mob, 1));
        }
        if (mob instanceof Creeper creeper){
            mob.goalSelector.addGoal(6, new TDMoveToCoreCreeperGoal(creeper, 1));
        }
        if (mob instanceof AbstractSkeleton && !(mob instanceof WitherSkeleton)){
            mob.goalSelector.addGoal(6, new TDMoveToCoreGoal(mob, 1));
        }
        if (mob instanceof Spider){
            mob.goalSelector.addGoal(6, new TDMoveToCoreGoal(mob, 1));
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
        if (TDData.coreListContains(position)){
            System.out.println("Déjà là !");
            TDData.removeCore(position);
        }
        else {
            System.out.println("Nouveau !");
            TDData.addCore(position);
        }
        System.out.println(TDData.getCoreIDList());
        System.out.println(TDData.getCorePosList());
    }
}
