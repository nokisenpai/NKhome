package be.noki_senpai.NKhome.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Bed;

import java.util.ArrayList;
import java.util.List;

public class CoordTask
{
	public static double roundFive(double number)
	{
		if(number >= 0)
		{
			return ((int) number) + 0.5;
		}
		else
		{
			return ((int) number) - 0.5;
		}
	}

	public static int BlockCoord(double number)
	{
		if(number < 0)
		{
			return ((int) number) - 1;
		}
		else
		{
			return (int) number;
		}
	}

	public static double BedNegateAdjust(double number)
	{
		if(number < 0)
		{
			return number + 1;
		}
		else
		{
			return number;
		}
	}

	@SuppressWarnings("deprecation")
	public static Location safeLocation(String worldName, double x, double y, double z, float yaw, float pitch)
	{
		World world = Bukkit.getServer().getWorld(worldName);
		if(world != null)
		{
			// Block location
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int) y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location(world, x, y, z, yaw, pitch);
			}
		}
		return null;
	}

	@SuppressWarnings("deprecation")
	public static Location safeBedLocation(String worldName, double x, double y, double z, float yaw, float pitch)
	{

		World world = Bukkit.getServer().getWorld(worldName);
		if(world != null)
		{
			Block headBed = world.getBlockAt(CoordTask.BlockCoord(x), (int) y, CoordTask.BlockCoord(z));
			Block footBed = headBed.getRelative(((Bed) headBed.getBlockData()).getFacing().getOppositeFace());

			// *******************
			// HEAD BED
			// *******************

			// nord
			if(headBed.getRelative(BlockFace.NORTH).getType().isTransparent()
					&& headBed.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.NORTH).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x, y, z - 1, yaw, pitch);
			}
			// sud
			if(headBed.getRelative(BlockFace.SOUTH).getType().isTransparent()
					&& headBed.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.SOUTH).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x , y, z + 1, yaw, pitch);
			}
			// ouest
			if(headBed.getRelative(BlockFace.WEST).getType().isTransparent()
					&& headBed.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.WEST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x - 1, y, z, yaw, pitch);
			}
			// est
			if(headBed.getRelative(BlockFace.EAST).getType().isTransparent()
					&& headBed.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.EAST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x + 1, y, z, yaw, pitch);
			}
			// nord-ouest
			if(headBed.getRelative(BlockFace.NORTH_WEST).getType().isTransparent()
					&& headBed.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.NORTH_WEST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x - 1, y, z - 1, yaw, pitch);
			}
			// nord-est
			if(headBed.getRelative(BlockFace.NORTH_EAST).getType().isTransparent()
					&& headBed.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.NORTH_EAST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x + 1, y, z - 1, yaw, pitch);
			}
			// sud-ouest
			if(headBed.getRelative(BlockFace.SOUTH_WEST).getType().isTransparent()
					&& headBed.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.SOUTH_WEST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x - 1, y, z + 1, yaw, pitch);
			}
			// sud-est
			if(headBed.getRelative(BlockFace.SOUTH_EAST).getType().isTransparent()
					&& headBed.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(headBed.getRelative(BlockFace.SOUTH_EAST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x + 1, y, z + 1, yaw, pitch);
			}


			switch(((Bed) headBed.getBlockData()).getFacing().getOppositeFace())
			{
				case NORTH: z--;
				break;
				case SOUTH: z++;
				break;
				case WEST: x--;
				break;
				case EAST: x++;
				break;
			}

			// *******************
			// FOOT BED
			// *******************

			// nord
			if(footBed.getRelative(BlockFace.NORTH).getType().isTransparent()
					&& footBed.getRelative(BlockFace.NORTH).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.NORTH).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x, y, z - 1, yaw, pitch);
			}
			// sud
			if(footBed.getRelative(BlockFace.SOUTH).getType().isTransparent()
					&& footBed.getRelative(BlockFace.SOUTH).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.SOUTH).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x , y, z + 1, yaw, pitch);
			}
			// ouest
			if(footBed.getRelative(BlockFace.WEST).getType().isTransparent()
					&& footBed.getRelative(BlockFace.WEST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.WEST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x - 1, y, z, yaw, pitch);
			}
			// est
			if(footBed.getRelative(BlockFace.EAST).getType().isTransparent()
					&& footBed.getRelative(BlockFace.EAST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.EAST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x + 1, y, z, yaw, pitch);
			}
			// nord-ouest
			if(footBed.getRelative(BlockFace.NORTH_WEST).getType().isTransparent()
					&& footBed.getRelative(BlockFace.NORTH_WEST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.NORTH_WEST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x - 1, y, z - 1, yaw, pitch);
			}
			// nord-est
			if(footBed.getRelative(BlockFace.NORTH_EAST).getType().isTransparent()
					&& footBed.getRelative(BlockFace.NORTH_EAST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.NORTH_EAST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x + 1, y, z - 1, yaw, pitch);
			}
			// sud-ouest
			if(footBed.getRelative(BlockFace.SOUTH_WEST).getType().isTransparent()
					&& footBed.getRelative(BlockFace.SOUTH_WEST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.SOUTH_WEST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x - 1, y, z + 1, yaw, pitch);
			}
			// sud-est
			if(footBed.getRelative(BlockFace.SOUTH_EAST).getType().isTransparent()
					&& footBed.getRelative(BlockFace.SOUTH_EAST).getRelative(BlockFace.UP).getType().isTransparent())
			{
				if(org.bukkit.Tag.CARPETS.getValues().contains(footBed.getRelative(BlockFace.SOUTH_EAST).getBlockData().getMaterial()))
				{
					y += 0.0625;
				}
				return new Location(world, x + 1, y, z + 1, yaw, pitch);
			}
		}
		return null;
	}
}
