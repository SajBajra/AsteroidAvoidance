package main;

import java.awt.Polygon;

class Asteroid {
    int x, y;

    public Asteroid(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move(int speed) {
        y += speed;
    }

    public Polygon getPolygon() {
        return new Polygon(
                new int[]{x, x + 20, x + 40, x + 20},
                new int[]{y, y + 40, y + 20, y},
                4
        );
    }
}
