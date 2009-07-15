/**
 * 
 */
package org.openstreetmap.josm.plugins.walkingpapers;

/**
 * <p>
 * Key for map tile. Key have just X and Y value. It have overriden {@link #hashCode()},
 * {@link #equals(Object)} and also {@link #toString()}.
 * </p>
 * 
 * @author LuVar <lubomir.varga@freemap.sk>
 * @author Dave Hansen <dave@sr71.net>
 *
 */
public class WalkingPapersKey {
	private final int x;
	private final int y;
	private final int level;
	
	/**
	 * <p>
	 * Constructs key for hashmaps for some tile describedy by X and Y position. X and Y are tiles
	 * positions on discrete map.
	 * </p>
	 * 
	 * @param x	x position in tiles table
	 * @param y	y position in tiles table
	 */
	public final boolean valid;
	public WalkingPapersKey(int level, int x, int y) {
		this.x = x;
		this.y = y;
		this.level = level;
		if (level <= 0 || x < 0 || y < 0) {
			this.valid = false;
			System.err.println("invalid WalkingPapersKey("+level+", "+x+", "+y+")");
		} else {
			this.valid = true;
		}
	}
	
	/**
	 * <p>
	 * Returns true ONLY if x and y are equals.
	 * </p>
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WalkingPapersKey) {
			WalkingPapersKey smk = (WalkingPapersKey) obj;
			if((smk.x == this.x) && (smk.y == this.y) && (smk.level == this.level)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @return	return new Integer(this.x + this.y * 10000).hashCode();
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return new Integer(this.x + this.y * 10000 + this.level * 100000).hashCode();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "WalkingPapersKey(x=" + this.x + ",y=" + this.y + ",level=" + level + ")";
	}
	
}
