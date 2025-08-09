package net.xenon.towerdefensemod.data;

import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class TDData {
    protected static List<Integer> idCoreList = new ArrayList<>();
    protected static List<BlockPos> positionCoreList = new ArrayList<>();
    private static int idCounter = 1;

    public static void addCore(BlockPos position){
        idCoreList.add(idCounter);
        positionCoreList.add(position);
        idCounter++;
    }

    public static void removeCoreFromPosition(BlockPos position){
        for (int i = 0; i < positionCoreList.size(); i++){
            BlockPos curPos = positionCoreList.get(i);
            if (position.equals(curPos)){
                idCoreList.remove(i);
                positionCoreList.remove(i);
                return;
            }
        }
    }

    public static BlockPos getCorePositionFromIndex(int index){
        return positionCoreList.get(index);
    }

    public static BlockPos getCorePositionFromID(int id){
        for (int i = 0; i < idCoreList.size(); i++){
            int curID = idCoreList.get(i);
            if (curID == id){
                return positionCoreList.get(i);
            }
        }
        return positionCoreList.getFirst();
    }

    public static int getCoreIdFromIndex(int index){
        return idCoreList.get(index);
    }

    public static BlockPos getFirstCorePosition(){
        return positionCoreList.getFirst();
    }

    public static int getCoreNumber(){
        return idCoreList.size();
    }

    public static List<BlockPos> getCorePositionList(){
        return positionCoreList;
    }

    public static List<Integer> getCoreIDList(){
        return idCoreList;
    }

    public static boolean coreListContains(BlockPos position){
        return positionCoreList.contains(position);
    }

    public static boolean coreListContains(int id){
        return idCoreList.contains(id);
    }

    public static boolean isCoreListEmpty(){
        return idCoreList.isEmpty();
    }
}
