package be.noki_senpai.NKhome.data;

public class Home
{
	private int cpt;
	private int id;
	private String server;
	private String name;
	private double x;
	private double y;
	private double z;
	private double facing;
	private double rotation;
	public Home(int cpt, int id, String server, String name, double x, double y, double z, double facing, double rotation)
	{
		setCpt(cpt);
		setId(id);
		setServer(server);
		setName(name);
		setX(x);
		setY(y);
		setZ(z);
		setFacing(facing);
		setRotation(rotation);
	}
	
	
	
	//######################################
	// Getters & Setters
	//######################################
	
	// Getter & Setter 'cpt'
	public int getCpt()
	{
		return cpt;
	}
	public void setCpt(int cpt)
	{
		this.cpt = cpt;
	}
	
	// Getter & Setter 'id'
	public int getId()
	{
		return id;
	}
	public void setId(int id)
	{
		this.id = id;
	}
	
	// Getter & Setter 'server'
	public String getServer()
	{
		return server;
	}
	public void setServer(String server)
	{
		this.server = server;
	}
	
	// Getter & Setter 'name'
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	
	// Getter & Setter 'x'
	public double getX()
	{
		return x;
	}
	public void setX(double x)
	{
		this.x = x;
	}
	
	// Getter & Setter 'y'
	public double getY()
	{
		return y;
	}
	public void setY(double y)
	{
		this.y = y;
	}
	
	// Getter & Setter 'z'
	public double getZ()
	{
		return z;
	}
	public void setZ(double z)
	{
		this.z = z;
	}
	
	// Getter & Setter 'facing'
	public double getFacing()
	{
		return facing;
	}
	public void setFacing(double facing)
	{
		this.facing = facing;
	}
	
	// Getter & Setter 'rotation'
	public double getRotation()
	{
		return rotation;
	}
	public void setRotation(double rotation)
	{
		this.rotation = rotation;
	}
	
	
}
