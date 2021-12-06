package net.potatoesduck.antifreecam.listener;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.util.BlockIterator;

/**
 * <h1>AntiFreeCamListener</h1>
 * This class listens events calls from inventory viewing.
 * Then this class checks if the player view contains the inventory holder.
 * If it does not, it alerts all operator users.
 * <p>
 * Freecam Background: Free cam is hack that allows users to move the client's
 * camera freely. This can be used to view and take items form chests that are not
 * visible. The primary goal of this class is to prevent freecam users to access
 * the chest.
 *
 * @author Potatoes_Duck
 * @version 1.0
 * @since 2021-12-05
 */
public class AntiFreeCamListener implements Listener {

    //maximum distance a player is allowed to access an inventory
    private static final double DISTANCE_THRESHOLD = 8;

    //directions checked which are used for increased accuracy
    private final BlockFace[] directions = new BlockFace[]
            {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.UP, BlockFace.DOWN};

    /**
     * This is the method that is called when an inventory is opened.
     * When an inventory that has a block holder is opened, it will call
     * validatePlayerBlockAction()
     * @param event Event provided by bukkit
     */
    @EventHandler
    public void onInventoryView(InventoryOpenEvent event) {
        if(event.getInventory().getHolder() instanceof BlockInventoryHolder blockInventoryHolder) {
            Player player = (Player) event.getPlayer();

            if(!validatePlayerBlockAction(player, blockInventoryHolder.getBlock())) {
                //alerts the operators of illegal action and prevents the chest being opened.
                fail(player);
                event.setCancelled(true);
            }
        }
    }

    /**
     * A method used to announce a player's freecam failure to all the operators
     * @param player The player that failed
     */
    private void fail(Player player) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.isOp()) {
                p.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "(!) " + ChatColor.GRAY + player.getName() + " has failed the freecam test.");
            }
        }
    }

    /**
     *
     * @param player Player that is committing the action
     * @param block Block that the player is committing the action to
     * @return If the action was legal
     */
    private boolean validatePlayerBlockAction(Player player, Block block) {
        double distance = block.getLocation().distance(player.getLocation());

        //if player is beyond distance threshold, this action is not legal
        if(distance >= DISTANCE_THRESHOLD) {
            return false;
        }

        //get all blocks in players direction up to the distance threshold
        BlockIterator blockIterator = new BlockIterator(player, (int) DISTANCE_THRESHOLD);

        //checkDirectionBlock is used for stairs to check if they are facing the correct way that would yield access
        //to a chest
        Block checkDirectionBlock = null;
        while (blockIterator.hasNext()) {
            Block b = blockIterator.next();

            //this checks if the stair direction is facing the next block, if so the chest is not accessible
            //if checkDirectionBlock is null, this statement will never be true
            if(b.equals(checkDirectionBlock)) {
                return false;
            }

            //clears after it was checked
            checkDirectionBlock = null;

            //reached our desired block! Action legal.
            if(b.equals(block)) {
                return true;
            }
            Material blockType = b.getType();

            //if block has no holes that can allow access to a chest
            if(blockType.isOccluding() && blockType.isSolid()) {
                return false;
            }

            //if stairs, then we have to do an extra check to ensure that the stairs are facing the proper direction to
            //allow the user to open the chest.
            if(b.getType().name().contains("STAIRS") && b.getBlockData() instanceof Directional directional) {
                checkDirectionBlock = b.getRelative(directional.getFacing());
                continue;
            }

            //server and client do not always have the same location information for a client and a server.
            //this will help prevent legal actions with unsync server and client. There is no 100% fix for this issue,
            //but this will help in the majority of the cases. Clients who spin rapidly and try and open a chest may
            //get blocked from opening the inventory.
            for (BlockFace dir : directions) {
                Block relativeBlock = b.getRelative(dir);
                if (relativeBlock.equals(block)) {
                    return true;
                }
            }

        }
        return false;
    }
}
