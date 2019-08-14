package be.noki_senpai.NKhome.data;

public class Home
{
	private int cpt;
	private int id;
	private String server;
	private String name;
	private String world;
	private double x;
	private double y;
	private double z;
	private float pitch;
	private float yaw;
	public Home(int cpt, int id, String server, String name, String world, double x, double y, double z, float pitch, float yaw)
	{
		setCpt(cpt);
		setId(id);
		setServer(server);
		setName(name);
		setWorld(world);
		setX(x);
		setY(y);
		setZ(z);
		setPitch(pitch);
		setYaw(yaw);
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

	// Getter & Setter 'world'
	public String getWorld()
	{
		return world;
	}
	public void setWorld(String world)
	{
		this.world = world;
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
	
	// Getter & Setter 'pitch'
	public float getPitch()
	{
		return pitch;
	}
	public void setPitch(float pitch)
	{
		this.pitch = pitch;
	}
	
	// Getter & Setter 'yaw'
	public float getYaw()
	{
		return yaw;
	}
	public void setYaw(float yaw)
	{
		this.yaw = yaw;
	}
	
	
}
