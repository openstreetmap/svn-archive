/**
 * 
 */
package org.openstreetmap.josm.plugins.slippymap;

/**
 * <p>
 * Key for map tile. Key have just X and Y value. It have overriden {@link #hashCode()},
 * {@link #equals(Object)} and also {@link #toString()}.
 * </p>
 * 
 * @author LuVar <lubomir.varga@freemap.sk>
 *
 */
public class SlippyMapKey {
	private final int x;
	private final int y;
	
	/**
	 * <p>
	 * Constructs key for hashmaps for some tile describedy by X and Y position. X and Y are tiles
	 * positions on discrete map.
	 * </p>
	 * 
	 * @param x	x position in tiles table
	 * @param y	y position in tiles table
	 */
	public SlippyMapKey(int x, int y) {
		this.x = x;
		this.y = y;
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
		if (obj instanceof SlippyMapKey) {
			SlippyMapKey smk = (SlippyMapKey) obj;
			if((smk.x == this.x) && (smk.y == this.y)) {
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
		return new Integer(this.x + this.y * 10000).hashCode();
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "SlippyMapKey(x=" + this.x + ",y=" + this.y + ")";
	}
	
}
