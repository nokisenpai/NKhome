package be.noki_senpai.NKhome.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
			// droite
			if(world.getBlockAt(CoordTask.BlockCoord(x) + 1, (int) y, CoordTask.BlockCoord(z)).getType().isTransparent()
					&& world.getBlockAt(CoordTask.BlockCoord(x) + 1, (int) y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location(world, x + 1, y, z, yaw, pitch);
			}
			// gauche
			if(world.getBlockAt(CoordTask.BlockCoord(x) - 1, (int) y, CoordTask.BlockCoord(z)).getType().isTransparent()
					&& world.getBlockAt(CoordTask.BlockCoord(x) - 1, (int) y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location(world, x - 1, y, z, yaw, pitch);
			}
			// haut
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int) y, CoordTask.BlockCoord(z) + 1).getType().isTransparent()
					&& world.getBlockAt(CoordTask.BlockCoord(x), (int) y + 1, CoordTask.BlockCoord(z) + 1).getType().isTransparent())
			{
				return new Location(world, x, y, z + 1, yaw, pitch);
			}
			// bas
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int) y, CoordTask.BlockCoord(z) - 1).getType().isTransparent()
					&& world.getBlockAt(CoordTask.BlockCoord(x), (int) y + 1, CoordTask.BlockCoord(z) - 1).getType().isTransparent())
			{
				return new Location(world, x, y, z - 1, yaw, pitch);
			}
			// Bed location
			if(world.getBlockAt(CoordTask.BlockCoord(x), (int) y + 1, CoordTask.BlockCoord(z)).getType().isTransparent())
			{
				return new Location(world, x, y, z, yaw, pitch);
			}
		}
		return null;
	}
}
