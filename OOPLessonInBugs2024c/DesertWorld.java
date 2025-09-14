import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.List;

/**
 * This scenario serves to demonstrate the core concepts of Object Oriented Programming,
 * especially:
 * 
 * - Constructors
 * - Instance variables
 * - Encapsulation
 * 
 * @author Jordan Cohen
 * @version v1.24, Feb 2024
 * 
 * - (v1.21) This version included a number of massive performance tweeks
 * - (v1.24c) Fixed flower spawn rate math issue
 */
public class DesertWorld extends World
{
    // World constants
    private static final int FLOWER_SPAWN_RATE = 20; // How many new flowers will spawn per second. 
    // Smaller number = more flowers
    public static final boolean SHOW_BARS = true;
    public static final boolean TRACK_PERF = true;
    public static final boolean Z_SORT = true;
    public static final int MAX_FLOWERS = 160;

    // Flower constants
    public static final int NIBBLES_PER_FLOWER = 4000; // Number of times a flower can be "Nibbled"
    public static final int HP_PER_NIBBLE = 20; // HP gained per act while eating

    // Bug constants
    public static final int BUG_MAX_HP = 5000;
    public static final int BUG_DEATH_RATE = 3; // HP lost per act
    public static final int ODDS_OF_BIG = 6; // 1:this chance to spawn big bug. A value of 1 would make all bugs big

    private static long lastAct, thisAct, elapsed;

    // Greenfoot Objects
    private TextButton spawnButton;
    private TextButton clearFlowersButton;
    private SuperDisplayLabel scoreBar; 

    // Performance Tracking
    private static int[] bugCounts;
    private static double[] actTimes;
    private static int actCounter;
    private static int currentBugs;

    // World variables
    private int flowerSpawnCounter;
    private boolean scoreShowing;
    private int maxAliveAtOnce;
    private int averageAge;
    private int ageTotalForDeathAvg;
    private int deaths;

    /**
     * Constructor for objects of class SpaceWorld.
     * 
     */
    public DesertWorld()
    {    
        // Create a new world with 800x560 cells with a cell size of 1x1 pixels.
        super(800, 560, 1); 

        // Sets the greenfoot speed slider back to the middle, in case you increased it
        // while running it last time.
        Greenfoot.setSpeed(50);

        // Calls the static init() method that gets the Bug class ready
        Bug.init();

        if (TRACK_PERF){ // init some (cheap to use) Arrays to track performance if desired
            bugCounts = new int[10000000];
            actTimes = new double [1000000];
        }

        // Set the order in which objects are painted, effectively setting what
        // will show up "on top of" what
        setPaintOrder (TextButton.class, SuperStatBar.class, Bug.class, Flower.class,  SuperDisplayLabel.class, Egg.class, DeadBug.class);

        // Score should not be showing at this point - because the welcome message is displayed
        scoreShowing = false;

        // Create a SuperDisplayLabel called score bar to display important values
        // during the simulation
        scoreBar = new SuperDisplayLabel (Color.BLACK, Color.WHITE, new Font ("Trebuchet", true, false, 24),48, "Welcome to the Desert. Click Run to Start");
        // Set the labels for the values on the score bar
        scoreBar.setLabels(new String[] {"N:", "Max: ", "Dead: ", "Avg. Life: "});
        // Show the starting message on the score bar (note - this version of the update
        // method does not use the labels or values, just whatever text is passed.)
        addObject(scoreBar, 400, 24);

        // Add the spawn button to the world
        spawnButton = new TextButton ("Spawn Bug", 24);
        addObject (spawnButton, 60, 24);

        clearFlowersButton = new TextButton ("Bomb Flowers", 24);
        addObject (clearFlowersButton, 726, 24);

        // Initial variable values
        maxAliveAtOnce = 0;
        actCounter = 0;
        flowerSpawnCounter = FLOWER_SPAWN_RATE;
        lastAct = -1;
        // Spawn a bug
        spawnBug();
        spawnBug();
        spawnBug();
        
        addObject(new Frog(), getWidth() / 2, getHeight() / 2); // place frog in the middle
    }

    public void act ()
    {
        //System.out.println();
        if (TRACK_PERF){
            updateTimer();
        }
        // Increment act counter
        actCounter++;

        // Check if user wants to spawn another bug
        if (Greenfoot.mouseClicked(spawnButton))
        {
            spawnBug();
        }
        // Check if user wants to delete all flowers
        if (Greenfoot.mouseClicked(clearFlowersButton)){
            // Get a list of all Flowers, and remove them using an iterative loop
            for (Flower f : getObjects(Flower.class)){
                removeObject(f);
            }
        }

        // Only perform these world tasks on the 0th and 30th act of each cycle
        if (actCounter % 30 == 0) // on the 30th and 60th act of each cycle (0 and 29);
        {
            // Spawn flowers on the 0th act of each 60 act cycle (0-59)
            if (actCounter % 60 == 0)
            {
                // Only spawn a Flower if there are less than 100 in the world
                int nFlowers = getObjects(Flower.class).size();
                if (nFlowers < MAX_FLOWERS)
                {
                    int spawnCount = FLOWER_SPAWN_RATE;
                    if (nFlowers + spawnCount > MAX_FLOWERS){
                        spawnCount = MAX_FLOWERS - nFlowers;
                    }
                    for (int i = 0; i < spawnCount; i++){
                        spawnFlower();
                    }

                    // The zSort method in the Utility class is called if Z_SORT is enabled

                }

            }

            if (Z_SORT){
                // Create a new list that can hold any Actors
                ArrayList<Actor> tempActors = new ArrayList<Actor>();

                // Add all Flower objects to the list
                tempActors.addAll(getObjects(Flower.class));

                // Add all SuperSmoothMover objects to the list
                tempActors.addAll(getObjects(SuperSmoothMover.class));

                // Now you can sort tempActors as they are all Actors
                Util.zSort(tempActors, this);
            }
            // Update displayed stats
            statUpdates();

            if (scoreShowing)
            {
                scoreBar.update(new int[]{currentBugs, maxAliveAtOnce, deaths, averageAge});
            }        
        } 
        if (actCounter % 3000 == 0){ // redistribute every 3000 acts ( to even out bug pathings per act
            Bug.resetActDistribution();
            for (Bug b : getObjects(Bug.class)){
                b.refreshActNumber();
            }
        }

    }

    public static int getActNumber() {
        // return a number from 0 - 59
        return actCounter % 60;
    }

    /**
     * Method to spawn a single bug at a random location
     */
    private void spawnBug ()
    {
        addObject (new Bug (), Greenfoot.getRandomNumber(800), Greenfoot.getRandomNumber(480)+120);
        scoreShowing = true;
    }

    /**
     * Method to spawn a flower
     */
    private void spawnFlower ()
    {
        addObject (new Flower(), Greenfoot.getRandomNumber(800), Greenfoot.getRandomNumber(480)+50);
    }

    /**
     * Static method that gets the distance between the x,y coordinates of two Actors
     * using Pythagorean Theorum.
     * 
     * @param a     First Actor
     * @param b     Second Actor
     * @return float
     */
    public static double getDistance (Actor a, Actor b)
    {
        return Math.hypot (a.getX() - b.getX(), a.getY() - b.getY());
    }

    private static void updateTimer(){
        // capture current time
        thisAct = System.nanoTime();
        // determine how much time has passed
        elapsed = thisAct - lastAct;
        //System.out.print(currentBugs + " bugs: " + elapsed/1000000000.0);
        bugCounts[actCounter] = currentBugs;
        actTimes[actCounter] = elapsed/1000000000.0;
        // remember when this act was for next time
        lastAct = thisAct;
    }

    public void analyzeMe () {
        analyze();
    }

    public static void analyze (){
        String results = "";
        for (int i = 0; i < actCounter-1; i++){
            if (actTimes[i] > 0.0163){
                System.out.println("Act " + i + " (Bugs = " + bugCounts[i] + "): " + actTimes[i]);
            }
        }

        // return results;
    }

    /**
     * Add to the list of Deaths when a bug dies, to be used for stat updates
     */
    public void addDeath (int age)
    {
        deaths++;
        ageTotalForDeathAvg += age;
        averageAge = (int)((double)ageTotalForDeathAvg / (double)deaths);
    }

    /**
     * Update the statistics
     */
    private void statUpdates ()
    {
        currentBugs = getObjects(Bug.class).size();
        if (currentBugs > maxAliveAtOnce)
            maxAliveAtOnce = currentBugs;
    }
}
