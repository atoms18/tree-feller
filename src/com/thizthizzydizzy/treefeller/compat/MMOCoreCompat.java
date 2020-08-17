package com.thizthizzydizzy.treefeller.compat;
import com.thizthizzydizzy.treefeller.Modifier;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
public class MMOCoreCompat extends InternalCompatibility{
    
    public static Option<HashMap<String, Double>> MMOCORE_TRUNK_XP = new Option<HashMap<String, Double>>("MMOCore Trunk XP", true, false, true, new HashMap<>(), "\n   - global: 1"){
        @Override
        public String getDesc(){
            return "EXP will be provided to these professions when a tree is felled\n"
                    + "EXP is provided per-block (a value of 1 means 1 EXP per block of trunk)\n"
                    + "use \"global\" to add global experience\n"
                    + "ex:\n"
                    + "- global: 3\n"
                    + "- woodcutting: 8";
        }
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof Map){
                HashMap<String, Double> professions = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String profession = null;
                    if(obj instanceof String){
                        profession = (String)obj;
                    }
                    if(profession==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(professions.containsKey(profession)){
                        professions.put(profession, professions.get(profession)+xp);
                    }else{
                        professions.put(profession, xp);
                    }
                }
                return professions;
            }
            return null;
        }
    };
    public static Option<HashMap<String, Double>> MMOCORE_LEAVES_XP = new Option<HashMap<String, Double>>("MMOCore Leaves XP", true, false, true, new HashMap<>(), "\n   - global: 0"){
        @Override
        public String getDesc(){
            return "EXP will be provided to these professions when a tree is felled\n"
                    + "EXP is provided per-block (a value of 1 means 1 EXP per block of leaves)\n"
                    + "use \"global\" to add global experience\n"
                    + "ex:\n"
                    + "- global: 3\n"
                    + "- woodcutting: 8";
        }
        @Override
        public HashMap<String, Double> load(Object o){
            if(o instanceof Map){
                HashMap<String, Double> professions = new HashMap<>();
                Map m = (Map)o;
                for(Object obj : m.keySet()){
                    String profession = null;
                    if(obj instanceof String){
                        profession = (String)obj;
                    }
                    if(profession==null)continue;
                    Double xp = Option.loadDouble(m.get(obj));
                    if(xp==null)continue;
                    if(professions.containsKey(profession)){
                        professions.put(profession, professions.get(profession)+xp);
                    }else{
                        professions.put(profession, xp);
                    }
                }
                return professions;
            }
            return null;
        }
    };
    @Override
    public String getPluginName(){
        return "MMOCore";
    }
    @Override
    public void breakBlock(Tree tree, Tool tool, Player player, ItemStack axe, Block block, List<Modifier> modifiers){
        if(player==null)return;
        HashMap<String, Double> xp = null;
        if(tree.trunk.contains(block.getType())){
            xp = MMOCORE_TRUNK_XP.get(tool, tree);
        }else if(tree.leaves.contains(block.getType())){
            xp = MMOCORE_LEAVES_XP.get(tool, tree);
        }
        if(xp==null||xp.isEmpty())return;
        net.Indyuce.mmocore.api.player.PlayerData data = net.Indyuce.mmocore.api.player.PlayerData.get(player);
        player.sendMessage(data.getCollectionSkills().getClass().getName());
        player.sendMessage(net.Indyuce.mmocore.MMOCore.plugin.professionManager.getClass().getName());
        if(xp.containsKey("global"))data.giveExperience(convert(xp.get("global")), net.Indyuce.mmocore.api.experience.EXPSource.SOURCE);
        for(String profession : xp.keySet()){
            int exp = convert(xp.get(profession));
            if(profession.equals("global")){
                data.giveExperience(exp, net.Indyuce.mmocore.api.experience.EXPSource.SOURCE);
            }else{
                data.getCollectionSkills().giveExperience(net.Indyuce.mmocore.MMOCore.plugin.professionManager.get(profession), exp, net.Indyuce.mmocore.api.experience.EXPSource.SOURCE);
            }
        }
    }
    private int convert(double d){
        int i = (int)d;
        double remainder = d-i;
        if(new Random().nextDouble()<remainder)i++;
        return i;
    }
}