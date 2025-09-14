import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;

public class Frog extends SuperSmoothMover
{
    private static GreenfootImage frogRight;
    private static GreenfootImage frogLeft;
    private Bug targetBug;
    private ArrayList<Bug> bugs;
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
            targetClosestBug();
        }

        if (targetBug != null) {
            moveTowardBug();
        }
    }

    /** Works like Bug.targetClosestFlower() but for Bugs */
    private void targetClosestBug() {
        double closestTargetDistance = Double.MAX_VALUE;
        double distanceToActor;

        // look for bugs in closer ranges first
        bugs = (ArrayList<Bug>)getObjectsInRange(40, Bug.class);
        if (bugs.size() == 0) {
            bugs = (ArrayList<Bug>)getObjectsInRange(150, Bug.class);
        }
        if (bugs.size() == 0) {
            bugs = (ArrayList<Bug>)getObjectsInRange(300, Bug.class);
        }

        if (bugs.size() > 0) {
            // set the first one as my target
            targetBug = bugs.get(0);
            closestTargetDistance = getDistance(this, targetBug);

            // loop through others to find the closest
            for (Bug b : bugs) {
                distanceToActor = getDistance(this, b);
                if (distanceToActor < closestTargetDistance) {
                    targetBug = b;
                    closestTargetDistance = distanceToActor;
                }
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

    // calculates distance
    private double getDistance(Actor a, Actor b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        return Math.sqrt(dx*dx + dy*dy);
    }
}
