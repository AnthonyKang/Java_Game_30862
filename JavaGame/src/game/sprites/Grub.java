package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;

/**
    A Grub is a Creature that moves slowly on the ground.
*/
public class Grub extends Creature {

    private long wakeupTimer;
    private long bulletTimer;
    private Boolean fireBullet;
    private Boolean awake;

    private static final long BULLET_COOLDOWN = 400;

    public Grub(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
	wakeupTimer = 0;
	bulletTimer = 0;
	awake = false;
	fireBullet = false;
    }

    public float getMaxSpeed() {
        return 0.05f;
    }

    public void wakeUp() {
    }

    public Boolean toFire() {
	return fireBullet;
    }

    public void bulletFired() {
	fireBullet = false;
    }

    public void update(long elapsedTime) {
	super.update(elapsedTime);
	if(isAlive()) {
		if(!awake) wakeupTimer += elapsedTime;
		else bulletTimer += elapsedTime;
		if(wakeupTimer >= 500) {
			awake = true;
			setVelocityX(-getMaxSpeed());
			wakeupTimer = 0;
		}
		if(bulletTimer >= BULLET_COOLDOWN) {
			fireBullet = true;
			bulletTimer -= BULLET_COOLDOWN;
		}
	}
    }
}
