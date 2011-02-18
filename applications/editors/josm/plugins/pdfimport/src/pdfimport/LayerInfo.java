package pdfimport;

import java.awt.Color;

public class LayerInfo{
	public Color fill;
	public Color stroke;
	public int dash;
	public double width;
	public int divider;
	public boolean isGroup;

	public int nr;

	@Override
	public int hashCode()
	{
		int code =  Double.toString(width).hashCode() ^ this.divider ^ this.dash;

		if (this.fill != null) {
			code ^= this.fill.hashCode();
		}

		if (this.stroke != null) {
			code ^= this.stroke.hashCode();
		}

		return code;
	}

	@Override
	public boolean equals(Object o)
	{
		LayerInfo l = (LayerInfo) o;
		boolean eq = this.width == l.width &&
		this.divider == l.divider &&
		this.dash == l.dash;


		if (this.fill != null){
			eq &= this.fill.equals(l.fill);
		}

		if (this.stroke != null) {
			eq &= this.stroke.equals(l.stroke);
		}

		return eq;
	}

	public LayerInfo copy() {
		LayerInfo result = new LayerInfo();
		result.fill = this.fill;
		result.stroke = this.stroke;
		result.dash = this.dash;
		result.width = this.width;
		result.divider = this.divider;
		return result;
	}

}
