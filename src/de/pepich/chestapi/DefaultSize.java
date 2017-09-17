package de.pepich.chestapi;

public final class DefaultSize
{
	private int size;
	private int max_size = 54;
	private boolean allow_square;
	private final boolean dynamic;
	private final boolean automatic;
	
	// -------------------------------------------------------------- //
	// ------------------------- Variations ------------------------- //
	// -------------------------------------------------------------- //
	
	public static final DefaultSize DYNAMIC_AUTO(final int preferred_size, final int max_size)
	{
		return new DefaultSize(preferred_size, max_size, true, true, true);
	}
	
	public static final DefaultSize DYNAMIC_AUTO_RECTANGLE(final int preferred_size, final int max_size)
	{
		return new DefaultSize(preferred_size, max_size, true, true, false);
	}
	
	public static final DefaultSize DYNAMIC_FIXED(final int preferred_size, final int max_size)
	{
		return new DefaultSize(preferred_size, max_size, true, false, true);
	}
	
	public static final DefaultSize DYNAMIC_FIXED_RECTANGLE(final int preferred_size, final int max_size)
	{
		return new DefaultSize(preferred_size, max_size, true, false, false);
	}
	
	public static final DefaultSize FINAL_FIXED(final int size)
	{
		return new DefaultSize(size, size, false, false, true);
	}
	
	public static final DefaultSize FINAL_FIXED_RECTANGLE(final int size)
	{
		return new DefaultSize(size, size, false, false, false);
	}
	
	// ------------------------------------------------------------- //
	// ------------------------ Constructor ------------------------ //
	// ------------------------------------------------------------- //
	
	private DefaultSize(final int size, final int max_size, final boolean dynamic, final boolean automatic,
			final boolean allow_square)
	{
		this.size = size;
		this.dynamic = dynamic;
		this.max_size = max_size;
		this.automatic = automatic;
		this.allow_square = allow_square;
	}
	
	// ------------------------------------------------------------- //
	// -------------------- GETTERS AND SETTERS -------------------- //
	// ------------------------------------------------------------- //
	
	public int getPreferredSize()
	{
		return size;
	}
	
	public int getMaxSize()
	{
		return max_size;
	}
	
	public boolean allowSquareShape()
	{
		return allow_square;
	}
	
	public boolean doAutoResize()
	{
		return automatic;
	}
	
	public boolean isFinalSize()
	{
		return !dynamic;
	}
	
	public void setPreferredSize(final int size) throws IllegalAccessException
	{
		if (!dynamic)
			throw new IllegalAccessException("Instances of FINAL_SIZE can not be modified");
		this.size = size;
	}
	
	public void setMaxSize(final int size) throws IllegalAccessException
	{
		if (!dynamic)
			throw new IllegalAccessException("Instances of FINAL_SIZE can not be modified");
		this.max_size = size;
	}
}
