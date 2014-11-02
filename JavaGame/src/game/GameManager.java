package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.LinkedList;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {

    public static void main(String[] args) {
        new GameManager().run();
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;
    private static final long B_COOLDOWN = 200; // 0.2 second cooldown between shots
    private static final long FIRE_COOLDOWN = 1000; // 1 second cooldown after firing MAX B COUNT shots
    private static final long B_LIFESPAN = 400; // bullet survives for 0.4 seconds
    private static final int MAX_B_COUNT = 10;  // Number of bullets that can be fired continuously
    private static final long P_HEALTH_UP_TIMER = 1000; // Every 1 second player is motionless, he gains health


    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private Sound deathSound;
    private Sound shotSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;

    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction jump;
    private GameAction exit;
    private GameAction fire;


    // Counters for timing and counting related functions
    private long bTiming;
    private int numShots;
    private long fireCooldownTiming;
    private long playerHealthTimer;
    private int playerMovePosition;
    private Boolean playerIdle;
    private int playerDir; // 1 for right, -1 for left

    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        map = resourceManager.loadNextMap();

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");
	deathSound = soundManager.getSound("sounds/death_sound.wav");
	shotSound = soundManager.getSound("sounds/shot_sound.wav");

        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.midi");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();

	// Initialize counters
	((Creature)map.getPlayer()).setHealth(20);
	bTiming = 0;
	numShots = 0;
	fireCooldownTiming = 0;
	playerHealthTimer = 0;
	playerMovePosition = TileMapRenderer.pixelsToTiles(map.getPlayer().getX());
    	playerIdle = false;
	playerDir = 1;
     }


    /**
        Closes any resurces used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
	fire = new GameAction("fire");	

        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(jump, KeyEvent.VK_UP);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
	inputManager.mapToKey(fire, KeyEvent.VK_SPACE);
    }


    private void checkInput(long elapsedTime) {

        if (exit.isPressed()) {
            stop();
        }

        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
	    playerIdle = true;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
		playerIdle = false;
		playerDir = -1;
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
		playerIdle = false;	
		playerDir = 1;
            }
            if (jump.isPressed()) {
                player.jump(false);
		playerIdle = false;
            }
            player.setVelocityX(velocityX);
        }

	// Create new bullet
	if(fire.isPressed()) {
	    playerIdle = false;
	    if(bTiming >= B_COOLDOWN && numShots <= MAX_B_COUNT) {
		Bullet newBullet = new Bullet(player.getX(), player.getY()+30, playerDir);
	    	map.addBullet(newBullet);
		bTiming = 0;
		numShots++;
		soundManager.play(shotSound);
	    }
	}

    }


    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }

    /** 
	Check if any of the given bullets collides with the given sprite 
    **/
    public Bullet getHitSprite(Sprite sprite, Iterator b) {
	while (b.hasNext()) {
	    Bullet bullet = (Bullet)b.next();
	    // check if the bullet lies within the boudaries
	    float bulletX = bullet.getX();
	    float bulletY = bullet.getY();
	    float sX = sprite.getX();
	    float sY = sprite.getY();
	    if(bulletX > sX && bulletX < sX + sprite.getWidth() &&
	       bulletY > sY && bulletY < sY + sprite.getHeight()) {
		return bullet;
	    }
	}
	return null;
    }

    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();

        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
	    playerMovePosition = 3;
            return;
        }

	// Update timers
	bTiming += elapsedTime;
	if(playerIdle) {
		playerHealthTimer += elapsedTime;
	}
	else {
		playerHealthTimer = 0;
	}

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);

	// check player health
	if(player.getHealth() <= 0 && player.isAlive()) {
		player.setState(Creature.STATE_DYING);
		soundManager.play(deathSound);
	}


	// Update health of player
	if(playerHealthTimer >= P_HEALTH_UP_TIMER) {
		player.updateHealth(5);
		playerHealthTimer -= P_HEALTH_UP_TIMER;
	}
	int new_player_position = TileMapRenderer.pixelsToTiles(player.getX());
	if(new_player_position != playerMovePosition) {
		player.updateHealth(Math.abs(new_player_position - playerMovePosition));
		playerMovePosition = new_player_position;
	}


        // update other sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Grub) {
                Grub creature = (Grub)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                    updateCreature(creature, elapsedTime);
                }
            }
            // normal update
            sprite.update(elapsedTime);
        }

	// Update bullets
	Iterator b = map.getBullets();
	LinkedList BulletToRemove = new LinkedList();
	while (b.hasNext()) {
		Bullet bullet = (Bullet)b.next();
		bullet.update(elapsedTime);		
		
		// Remove bullets that go past the bullet life span
		long life = bullet.getLife();
		if(life > B_LIFESPAN) BulletToRemove.add(bullet);

		// TODO: Detect bullet collision with tiles
		else if(map.getTile(TileMapRenderer.pixelsToTiles(bullet.getX()), TileMapRenderer.pixelsToTiles(bullet.getY())) != null) BulletToRemove.add(bullet);

	}
	Iterator toRemove = BulletToRemove.iterator();
	while (toRemove.hasNext()) {
		Bullet remove = (Bullet)toRemove.next();
		map.removeBullet(remove);
	}
	    
	// update enemy bullets
	Iterator eb = map.getEnemyBullets();
	BulletToRemove = new LinkedList();
	while (eb.hasNext()) {
		Bullet bullet = (Bullet)eb.next();
		bullet.update(elapsedTime);		
		
		// Remove bullets that go past the bullet life span
		long life = bullet.getLife();
		if(life > B_LIFESPAN) BulletToRemove.add(bullet);

		// TODO: Detect bullet collision with tiles
		else if(map.getTile(TileMapRenderer.pixelsToTiles(bullet.getX()), TileMapRenderer.pixelsToTiles(bullet.getY())) != null) BulletToRemove.add(bullet);

	}
	toRemove = BulletToRemove.iterator();
	while (toRemove.hasNext()) {
		Bullet remove = (Bullet)toRemove.next();
		map.removeEnemyBullet(remove);
	}

	Iterator s = map.getSprites();
	while(s.hasNext()) {
	    Sprite newSprite = (Sprite)s.next();
	    if(newSprite instanceof Grub) {
		Grub sprite = (Grub)newSprite;
		if(sprite.toFire()) {
		    int direction = (sprite.getVelocityX() < 0) ? -1:1;
		    Bullet newBullet = new Bullet(sprite.getX(), sprite.getY()+30, direction);
	    	    map.addEnemyBullet(newBullet);
		    //soundManager.play(shotSound);
		    sprite.bulletFired();
		}
	    }
	}

	// Collision detection for player and bullets
	eb = map.getEnemyBullets();
	Bullet hitPlayer = getHitSprite(player, eb);
	if(hitPlayer != null) {
		map.removeEnemyBullet(hitPlayer);
		player.updateHealth(-5);
	}

	// Collision detection for enemies and player bullets
	i = map.getSprites();
	while(i.hasNext()) {
		Sprite sprite = (Sprite)i.next();
		if(sprite instanceof Grub) {
			Grub enemy = (Grub) sprite;
			b = map.getBullets();
			Bullet hitEnemy = getHitSprite(enemy, b);
			if(hitEnemy != null && enemy.isAlive()) {
				map.removeBullet(hitEnemy);
                		enemy.setState(Creature.STATE_DYING);
				map.updateScore(10);
				player.updateHealth(10);
			}
		}
	}

	// Update cooldowns
	// bullet firing cooldown
	if(numShots >= MAX_B_COUNT) {
		fireCooldownTiming += elapsedTime;
		if(fireCooldownTiming >= FIRE_COOLDOWN) {
			fireCooldownTiming = 0;
			numShots = 0;
		}
	}
    }


    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */
    private void updateCreature(Creature creature,
        long elapsedTime)
    {

        // apply gravity
        if (!creature.isFlying()) {
            creature.setVelocityY(creature.getVelocityY() +
                GRAVITY * elapsedTime);
        }

        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }

        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }

    }


    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill) {
                // kill the badguy and make player bounce
                soundManager.play(boopSound);
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                player.jump(true);
		map.updateScore(10);
		player.updateHealth(10);
            }
            else {
                // player dies!
		soundManager.play(deathSound);
                player.setState(Creature.STATE_DYING);
            }
        }
    }


    /**
        Gives the player the speicifed power up and removes it
        from the map.
    */
    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);

        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
	else if (powerUp instanceof PowerUp.Mushroom) {
	    // Played health increases
	    ((Creature)map.getPlayer()).updateHealth(5);
	    soundManager.play(prizeSound);
	}
    }

}
