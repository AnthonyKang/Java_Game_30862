package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

public class Bullet {

    // position (pixels)
    private float x;
    private float y;
    // velocity (pixels per millisecond)
    private float dx;
    private float dy;

    public Bullet(float x, float y, int direction) { // -1 for left, 1 for right
        super();
	this.x = x;
	this.y = y;
	this.dx = direction * this.getMaxSpeed();
	this.dy = 0;
    }

    // position setters and getters
    public float getX() {
	return this.x;
    }

    public float getY() {
	return this.y;
    }

    public void setX(float x) {
	this.x = x;
    }

    public void setY(float y) {
	this.y = y;
    }

    // Overwrite update function to not use animation
    public void update(long elapsedTime) {
	x += dx * elapsedTime;
	y += dy * elapsedTime;
    }

    public float getMaxSpeed() {
        return 1;
    }

    public boolean isFlying() {
        return true;
    }

}
