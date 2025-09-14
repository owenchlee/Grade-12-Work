import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

public class Frog extends SuperSmoothMover
{
    private static GreenfootImage frogRight;
    private static GreenfootImage frogLeft;
    private Bug targetBug;
    private double mySpeed = 2.5;
    private boolean facingRight = true;

    public Frog() {
        if (frogRight == null) {
            frogRight = new GreenfootImage("frog.png"); // original facing right
            frogRight.scale(frogRight.getWidth() * 2, frogRight.getHeight() * 2);

            frogLeft = new GreenfootImage(frogRight); // create left-facing by mirroring
            frogLeft.mirrorHorizontally();
        }
        setImage(frogRight);
    }

    public void act() {
        if (targetBug == null || targetBug.getWorld() == null) {
            pickClosestBug();
        }

        if (targetBug != null) {
            moveTowardBug();
        }
    }

    private void pickClosestBug() {
        targetBug = null;
        double closestDistance = Double.MAX_VALUE;

        for (Object obj : getWorld().getObjects(Bug.class)) {
            Bug b = (Bug)obj;
            double dx = b.getX() - getX();
            double dy = b.getY() - getY();
            double distance = Math.sqrt(dx*dx + dy*dy);

            if (distance < closestDistance && distance < 1000) {
                closestDistance = distance;
                targetBug = b;
            }
        }
    }

    private void moveTowardBug() {
        if (targetBug == null) return;

        double dx = targetBug.getX() - getX();
        double dy = targetBug.getY() - getY();
        double distance = Math.sqrt(dx*dx + dy*dy);

        if (distance < 20) { // eat bug
            getWorld().removeObject(targetBug);
            targetBug = null;
        } else {
            double moveX = (dx / distance) * mySpeed;
            double moveY = (dy / distance) * mySpeed;

            // Flip image based on horizontal movement
            if (moveX > 0 && !facingRight) {
                setImage(frogRight);
                facingRight = true;
            } else if (moveX < 0 && facingRight) {
                setImage(frogLeft);
                facingRight = false;
            }

            setLocation(getX() + (int)moveX, getY() + (int)moveY);
        }
    }
}
