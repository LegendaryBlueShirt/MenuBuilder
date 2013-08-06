package util;

public class Coord {
	public int x,y;
	public Coord(int x, int y) {
		this.x = x;
		this.y = y;
	}
	public String toString() {
		return String.format("%d, %d", x,y);
	}
	
	public boolean equals(Object o) {
		if(o instanceof Coord) {
			return ((Coord)o).x == x && ((Coord)o).y == y;
		}
		return false;
	}
}
