package com.brackeen.javagamebook.tilegame;

import java.awt.Image;
import java.util.LinkedList;
import java.util.Iterator;
import com.brackeen.javagamebook.tilegame.sprites.Bullet;
import com.brackeen.javagamebook.tilegame.sprites.Creature;
import com.brackeen.javagamebook.graphics.Sprite;

/**
    The TileMap class contains the data for a tile-based
    map, including Sprites. Each tile is a reference to an
    Image. Of course, Images are used multiple times in the tile
    map.
*/
public class TileMap {

    private Image[][] tiles;
    private LinkedList sprites;
    private Sprite player;

    private LinkedList bullets;
    private LinkedList enemyBullets;
    private int gameScore;

    /**
        Creates a new TileMap with the specified width and
        height (in number of tiles) of the map.
    */
    public TileMap(int width, int height) {
        tiles = new Image[width][height];
        sprites = new LinkedList();
	bullets = new LinkedList();
	enemyBullets = new LinkedList();
	gameScore = 0;
    }

    // Game Score setters and getters
    public void updateScore(int update) {
	gameScore += update;
    }

    public int getScore() {
	return this.gameScore;
    }

    /**
        Gets the width of this TileMap (number of tiles across).
    */
    public int getWidth() {
        return tiles.length;
    }


    /**
        Gets the height of this TileMap (number of tiles down).
    */
    public int getHeight() {
        return tiles[0].length;
    }


    /**
        Gets the tile at the specified location. Returns null if
        no tile is at the location or if the location is out of
        bounds.
    */
    public Image getTile(int x, int y) {
        if (x < 0 || x >= getWidth() ||
            y < 0 || y >= getHeight())
        {
            return null;
        }
        else {
            return tiles[x][y];
        }
    }


    /**
        Sets the tile at the specified location.
    */
    public void setTile(int x, int y, Image tile) {
        tiles[x][y] = tile;
    }


    /**
        Gets the player Sprite.
    */
    public Sprite getPlayer() {
        return player;
    }


    /**
        Sets the player Sprite.
    */
    public void setPlayer(Sprite player) {
        this.player = player;
	((Creature)player).setHealth(20);
    }


    /**
        Adds a Sprite object to this map.
    */
    public void addSprite(Sprite sprite) {
        sprites.add(sprite);
    }

    public void addBullet(Bullet sprite) {
        bullets.add(sprite);
    }

    public void addEnemyBullet(Bullet sprite) {
	enemyBullets.add(sprite);
    }

    /**
        Removes a Sprite object from this map.
    */
    public void removeSprite(Sprite sprite) {
        sprites.remove(sprite);
    }

    public void removeBullet(Bullet sprite) {
	bullets.remove(sprite);
    }

    public void removeEnemyBullet(Bullet sprite) {
	enemyBullets.remove(sprite);
    }

    /**
        Gets an Iterator of all the Sprites in this map,
        excluding the player Sprite.
    */
    public Iterator getSprites() {
        return sprites.iterator();
    }

    public Iterator getBullets() {
        return bullets.iterator();
    }

    public Iterator getEnemyBullets() {
	return enemyBullets.iterator();
    }
}
