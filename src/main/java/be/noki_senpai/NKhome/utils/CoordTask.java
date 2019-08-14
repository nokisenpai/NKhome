package be.noki_senpai.NKhome.utils;

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
}
