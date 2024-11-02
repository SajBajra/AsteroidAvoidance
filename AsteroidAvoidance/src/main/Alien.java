package main;

import java.awt.Rectangle;


class Alien {
    int x, y, width, height;
    int speed;

    public Alien(int x, int y, int speed) {
        this.x = x;
        this.y = y;
        this.width = 40;
        this.height = 40;
        this.speed = speed;
    }

    public void move() {
        y += speed;
    }

    public void shootBeam() {
        // Implement alien shooting logic here if needed
    }
}
