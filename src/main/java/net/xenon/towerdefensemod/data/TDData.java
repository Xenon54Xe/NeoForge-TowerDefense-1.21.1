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

    public static void removeCore(BlockPos position){
        for (int i = 0; i < positionCoreList.size(); i++){
            BlockPos curPos = positionCoreList.get(i);
            if (position.equals(curPos)){
                idCoreList.remove(i);
                positionCoreList.remove(i);
                return;
            }
        }
    }

    public static BlockPos getCore(int index){
        return positionCoreList.get(index);
    }

    public static int getCoreId(int index){
        return idCoreList.get(index);
    }

    public static BlockPos getFirstCore(){
        return positionCoreList.getFirst();
    }

    public static int getCoreListSize(){
        return idCoreList.size();
    }

    public static List<BlockPos> getCorePosList(){
        return positionCoreList;
    }

    public static List<Integer> getCoreIDList(){
        return idCoreList;
    }

    public static boolean coreListContains(BlockPos position){
        return positionCoreList.contains(position);
    }

    public static boolean coreListContainsID(int id){
        return idCoreList.contains(id);
    }

    public static boolean isCoreListEmpty(){
        return idCoreList.isEmpty();
    }
}
