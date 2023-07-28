package utils

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.util.BlockIterator

object BlockDetector : Listener {
    private const val RANGE = 12
    private val passableBlocks = arrayOf(
        Material.AIR, Material.CAVE_AIR, Material.ITEM_FRAME, Material.GRASS,
        Material.TORCH
    )

    /**
     * Return the first Block the vector of view hits. This method ignores air.
     *
     * @param player The player you want to get the view vector from.
     * @return Returns the block the player is looking at, if there is no in range
     * it returns an air block.
     */
    fun getTargetBlock(player: Player?): Block {
        val iter = BlockIterator(player!!, RANGE)
        var lastBlock = iter.next()
        while (iter.hasNext()) {
            lastBlock = iter.next()
            if (isBlockPassable(lastBlock) || lastBlock.type.name.contains("SIGN")
                || lastBlock.type.name.contains("CARPET") || lastBlock.isLiquid
            ) {
                continue
            }
            break
        }
        return lastBlock
    }

    /**
     * Returns the Block at the Location `loc`.
     *
     * @param loc The Location of the block.
     * @return Returns the Block at the Location `loc`.
     */
    fun getBlockByLocation(loc: Location): Block {
        return loc.world.getBlockAt(loc)
    }

    private fun isBlockPassable(block: Block): Boolean {
        for (m in passableBlocks) {
            if (block.type == m) return true
        }
        return false
    }
}
