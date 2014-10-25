public class Char extends Item{
	private int speed; // in px/sec
	private int health;
	private int movement_count;
	private final static int UNIT = 20;

	public Char(int xcoord, int ycoord, int speed, int health){
		this.x_map = xcoord;
		this.y_map = ycoord;
		this.speed = speed;
		this.health = health;
	}
	public int get_xcoord(){
		return x_coord;
	}
	public int get_ycoord(){
		return y_coord;
	}
	public int get_speed(){
		return speed;
	}
	public void set_speed(int speed){
		this.speed = speed;
	}
	public int get_health(){
		return health;
	}
	public void set_health(int health){
		this.health = health;
	}
	// InputHandler will set direction and alert frame class
	// Frame will call this to update movements
	public int move_char(long elapsed_time, int leftright){
		// calculate displacement
		int x_displace = this.speed * elapsed_time / 1000;
		// set x map coor 
		if(leftright == 0)  {
			x_displace *= -1;
		}
		this.x_map += x_displace;

		// update health
		this.movement_count += x_displace;
        this.health += (this.movement_count/UNIT);
        this.movement_count %= UNIT;
        
        // return x_displace for screen to update position on map
        return x_displace;
	}
	// InputHandler will detect idleness and call inc_health for every second of idle
	public void inc_health() {
		this.health += 5;
	}


}
