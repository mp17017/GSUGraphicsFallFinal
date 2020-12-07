package Project;

import com.sun.j3d.loaders.IncorrectFormatException;
import com.sun.j3d.loaders.ParsingErrorException;
import com.sun.j3d.loaders.Scene;
import com.sun.j3d.loaders.objectfile.ObjectFile;
import com.sun.j3d.utils.applet.MainFrame;
import com.sun.j3d.utils.universe.SimpleUniverse;
import javax.media.j3d.*;
import javax.swing.*;
import javax.vecmath.*;
import java.applet.Applet;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;

//Marc Newman, for Graphics
public class GameWindow extends Applet {

    private final CML Listener;
    GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
    Canvas3D cv = new Canvas3D(gc);
    private int DELAY = 10;
    private float cameraScale = 0.01f;
    private boolean noClipMode = false;
    //variables for event node 3: code lock
    private final String code = "1234";
    private StringBuffer inputCode = new StringBuffer("");
    //Point3d array to hold points the camera can look at
    private int numCircles = 5;
    private int numPoints = 16;
    private final Point3d[][] lookAtLocations = new Point3d[numCircles][numPoints];
    //Indices for event tree
    //Inventory array: key, missing child, weapon
    private boolean[] inventory = {false, false, false};
    private String[] buttonText = {"", ""};
    private boolean decisionsDisabled = false;
    private boolean lookDisabled = false;
    private boolean consoleInitialized = false;
    private int decisionIndex = 0;
    private float opacity = (float) .6;
    private String[] console = {"", "", "", "", "", ""};
    private Map<Integer, ArrayList<Integer>> decisionTreeIndex = new HashMap<>();
    private Map<Integer, String> decisionTreeButtonTitles = new HashMap<>();
    private ArrayList<ArrayList<String>> dialogTree = new ArrayList<>();
    private ArrayList<ArrayList<Point3d>> movementTree = new ArrayList<>();
    //Point3d map ArrayList to determine which points to look at given a lookDirection
    //[0] look counter clockwise/left, [1] look clockwise/right, [2] look up/yaw up, [3] look down/yaw down
    private ArrayList<Map<Point3d, Point3d>> viewLookAtMaps = new ArrayList<>();

    //Point3d array to hold variables associated with the camera's position
    //[0] current position, [1] eye position, [2] relative eye position (will always have a unit value to offset from [0])
    //[0] == camera position relative to world, [1] == position the camera is looking at relative to world, [2] == position camera is looking at relative to current position
    private final Point3d eyeOrigin = new Point3d(0.525f, -1.050f, 2.425f);
    private final Point3d centerOffset = new Point3d(0.000f * cameraScale, 0.000f * cameraScale, -1.000f * cameraScale); //Must be located in the lookAtLocations
    private final Point3d centerOrigin = new Point3d(eyeOrigin.getX() + centerOffset.getX(), eyeOrigin.getY() + centerOffset.getY(), eyeOrigin.getZ() + centerOffset.getZ());
    private Point3d[] viewPosition = {eyeOrigin, centerOrigin, centerOffset};
    private boolean dead = false;
    private int textDelay = 0;

    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        new MainFrame(new GameWindow(), 800, 600);
    }
    
    public GameWindow(){
        this((float)0);
    }

    //@Override
    public GameWindow(float opacity) {

        //initialize universe
        this.setLayout(new GridLayout(1, 2));

        SimpleUniverse su = new SimpleUniverse(cv);
        //Image imports from objects folder, images are PLACEHOLDERS!
        BufferedImage wepin = null;
        BufferedImage keyin = null;
        BufferedImage childin = null;

        try {
            wepin = ImageIO.read(new File("Objects/item1.png"));
            keyin = ImageIO.read(new File("Objects/item2.png"));
            childin = ImageIO.read(new File("Objects/item3.png"));
        } catch (IOException e) {
            System.out.println("Image could not be opened for inventory: " + e);
        }
        //Preperation to pass to class, not sure why this is necesarry, but my IDE wouldn't work without it.
        BufferedImage finalWepin = wepin;
        BufferedImage finalKeyin = keyin;
        BufferedImage finalChildin = childin;

        cv = new Canvas3D(gc) {
            @Override
            public void postRender() {

                //Image import from outside of class to prevent constant loading of images from file.
                BufferedImage wep = finalWepin;
                BufferedImage key = finalKeyin;
                BufferedImage child = finalChildin;

                Graphics2D G2D = this.getGraphics2D();
                G2D.setColor(Color.LIGHT_GRAY);
                G2D.fill3DRect(0, 0, 100, this.getHeight() - 100, true);
                G2D.fill3DRect(0, this.getHeight() - 100, 100, 100, true);
                //Outer text box
                G2D.fill3DRect(100, this.getHeight() - 140, this.getWidth() - 100, 140, true);

                //lighting overlay
                G2D.setColor(new Color(0f,0f,0f,StartMenu.opacity));
                G2D.fill3DRect(100, 0,this.getWidth(), this.getHeight()-140, true);

                //Inner Box's for text
                G2D.setColor(Color.BLACK);
                G2D.fill3DRect(101, this.getHeight() - 32, this.getWidth() - 102, 30, true);
                G2D.fill3DRect(101, this.getHeight() - 138, this.getWidth() - 102, 104, true);
                G2D.setColor(Color.BLACK);
                G2D.setFont(new Font("TimesNewRoman", Font.PLAIN,12));
                DecimalFormat df = new DecimalFormat("##.#####");
                G2D.drawString("X: " + df.format(viewPosition[0].getX()), 5, this.getHeight() - 150);
                G2D.drawString("Y: " + df.format(viewPosition[0].getY()), 5, this.getHeight() - 130);
                G2D.drawString("Z: " + df.format(viewPosition[0].getZ()), 5, this.getHeight() - 110);

                if (!consoleInitialized) {
                    console[2] = "Press the ENTER key to begin playing.";
                    console[1] = "Press the ESCAPE key to exit the game.";
                    console[0] = "(Press 'n' at any time between decisions to activate no-clip movement).";
                }

                G2D.drawString("Inventory", 25, 20);
                G2D.drawString("MENU", 30, this.getHeight() - 45);
                G2D.setColor(Color.GREEN);
                G2D.drawString(" " + console[5], 103, this.getHeight() - 122);
                G2D.drawString(" " + console[4], 103, this.getHeight() - 102);
                G2D.drawString(" " + console[3], 103, this.getHeight() - 82);
                G2D.drawString(" " + console[2], 103, this.getHeight() - 62);
                G2D.drawString(" " + console[1], 103, this.getHeight() - 42);
                G2D.drawString(" " + console[0], 103, this.getHeight() - 12);

                //Shows items if their triggers are true.
                if (inventory[0]) {
                    G2D.drawImage(wep, 18, 38, this);
                }
                if (inventory[1]) {
                    G2D.drawImage(key, 18, 120, this);
                }
                if (inventory[2]) {
                    G2D.drawImage(child, 18, 202, this);
                }
                this.getGraphics2D().flush(false);
                if (dead){
                    G2D.setColor(new Color(0f,0f,0f,0.8f));
                    G2D.fill3DRect(100, 0,this.getWidth(), this.getHeight()-140, true);
                    G2D.setFont(new Font("Herculanum", Font.PLAIN,48));
                    G2D.setColor(Color.RED);
                    G2D.drawString("YOU ARE DEAD", this.getWidth()/2 - 100, this.getHeight()/2 - 50);
                }
            }
        };
        add(cv);
        View view = new View();
        view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
        view.setFieldOfView(0.5 * Math.PI);
        view.setFrontClipDistance(0.001);
        view.setBackClipDistance(1000);
        ViewPlatform vp = new ViewPlatform();
        view.addCanvas3D(cv);
        view.attachViewPlatform(vp);
        view.setPhysicalBody(new PhysicalBody());
        view.setPhysicalEnvironment(new PhysicalEnvironment());
        Transform3D trans = new Transform3D();
        trans.lookAt(eyeOrigin, centerOrigin, new Vector3d(0.0f, 1.0f, 0.0f));
        trans.invert();
        TransformGroup tg = new TransformGroup(trans);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tg.addChild(vp);
        BranchGroup bgView = new BranchGroup();
        bgView.addChild(tg);
        su.addBranchGraph(bgView);

        BranchGroup bg = createObject("Warehouse");

        AmbientLight aLight = new AmbientLight(true, new Color3f(0.1f, 0.1f, 0.1f));

        bg.addChild(aLight);
        bg.compile();
        su.addBranchGraph(bg);
        initializeVariables(tg);
        //Initialize timer
        Timer timer = new Timer(DELAY, arg0 -> {
            validate();
            updateCamera(tg);
        });
        timer.start();

        Listener = new CML();
        cv.addMouseListener(Listener);
        cv.addMouseMotionListener(Listener);
        cv.addKeyListener(Listener);
    }

    private Point3d lookAtMaps(int direction) {
        Point3d newLookAt = new Point3d();
        System.out.println("Direction: " + direction);
        Point3d temp = viewLookAtMaps.get(direction).get(viewPosition[2]);
        viewPosition[2] = temp;
        System.out.println("Current position of eye: ");
        System.out.println("X: " + viewPosition[0].getX() + " Y: " + viewPosition[0].getY() + " Z: " + viewPosition[0].getZ());
        System.out.println("Current position of center relative to eye: ");
        System.out.println("X: " + temp.getX() + " Y: " + temp.getY() + " Z: " + temp.getZ());
        newLookAt.setX(temp.getX() + viewPosition[0].getX());
        newLookAt.setY(temp.getY() + viewPosition[0].getY());
        newLookAt.setZ(temp.getZ() + viewPosition[0].getZ());
        System.out.println("New position of center relative to world: ");
        System.out.println("X: " + newLookAt.getX() + " Y: " + newLookAt.getY() + " Z: " + newLookAt.getZ());

        viewPosition[1] = newLookAt;

        return newLookAt;
    }

    private void initializeVariables(TransformGroup tg) {
        for (int j = 0; j < 1;) {//dummy loop to release j counter when loop is finished
            //populate viewLookAtLocations
            for (float i = -1.0f; i <= 1.0f; i += 1.0f / ((numCircles - 1) / 2.0f)) {
                lookAtLocations[j][0] = new Point3d(1.0f * cameraScale, i * cameraScale, 0.0f * cameraScale);
                lookAtLocations[j][1] = new Point3d(0.75f * cameraScale, i * cameraScale, -0.25f * cameraScale);
                lookAtLocations[j][2] = new Point3d(0.5f * cameraScale, i * cameraScale, -0.5f * cameraScale);
                lookAtLocations[j][3] = new Point3d(0.25f * cameraScale, i * cameraScale, -0.75f * cameraScale);
                //negative x, positive y, negative z
                lookAtLocations[j][4] = new Point3d(0.0f * cameraScale, i * cameraScale, -1.0f * cameraScale);
                lookAtLocations[j][5] = new Point3d(-0.25f * cameraScale, i * cameraScale, -0.75f * cameraScale);
                lookAtLocations[j][6] = new Point3d(-0.5f * cameraScale, i * cameraScale, -0.5f * cameraScale);
                lookAtLocations[j][7] = new Point3d(-0.75f * cameraScale, i * cameraScale, -0.25f * cameraScale);
                //negative x, positive y, positive z
                lookAtLocations[j][8] = new Point3d(-1.0f * cameraScale, i * cameraScale, 0.0f * cameraScale);
                lookAtLocations[j][9] = new Point3d(-0.75f * cameraScale, i * cameraScale, 0.25f * cameraScale);
                lookAtLocations[j][10] = new Point3d(-0.5f * cameraScale, i * cameraScale, 0.5f * cameraScale);
                lookAtLocations[j][11] = new Point3d(-0.25f * cameraScale, i * cameraScale, 0.75f * cameraScale);
                //positive x, positive y, positive z
                lookAtLocations[j][12] = new Point3d(0.0f * cameraScale, i * cameraScale, 1.0f * cameraScale);
                lookAtLocations[j][13] = new Point3d(0.25f * cameraScale, i * cameraScale, 0.75f * cameraScale);
                lookAtLocations[j][14] = new Point3d(0.5f * cameraScale, i * cameraScale, 0.5f * cameraScale);
                lookAtLocations[j][15] = new Point3d(0.75f * cameraScale, i * cameraScale, 0.25f * cameraScale);
                j++;
            }
        }

        //populate viewLookAtMaps
        for (int i = 0; i < 4; i++) {
            viewLookAtMaps.add(new HashMap<>());
        }
        for (int i = 0; i < lookAtLocations.length; i++) {
            //left map
            for (int j = 0; j < lookAtLocations[i].length; j++) {
                if (j == lookAtLocations[i].length - 1) {
                    viewLookAtMaps.get(0).put(lookAtLocations[i][j], lookAtLocations[i][0]);
                } else {
                    viewLookAtMaps.get(0).put(lookAtLocations[i][j], lookAtLocations[i][j + 1]);
                }
            }
            //right map
            for (int j = lookAtLocations[i].length - 1; j >= 0; j--) {
                if (j == 0) {
                    viewLookAtMaps.get(1).put(lookAtLocations[i][j], lookAtLocations[i][lookAtLocations[i].length - 1]);
                } else {
                    viewLookAtMaps.get(1).put(lookAtLocations[i][j], lookAtLocations[i][j - 1]);
                }
            }
        }
        //up/down maps
        for (int i = 0; i < lookAtLocations.length; i++) {
            for (int j = 0; j < lookAtLocations[i].length; j++) {
                //up map
                //if we are at the bottom, set lookDirection to look at, to the point facing the other lookDirection on the same octagon
                if (i == lookAtLocations.length - 1) {
                    viewLookAtMaps.get(2).put(lookAtLocations[i][j], lookAtLocations[i][(j + (lookAtLocations[i].length / 2)) % lookAtLocations[i].length]);
                } else {
                    viewLookAtMaps.get(2).put(lookAtLocations[i][j], lookAtLocations[i + 1][j]);
                }
                //down map
                //if we are at the bottom, set lookDirection to look at, to the point facing the other lookDirection on the same octagon
                if (i == 0) {
                    viewLookAtMaps.get(3).put(lookAtLocations[i][j], lookAtLocations[i][(j + (lookAtLocations[i].length / 2)) % lookAtLocations[i].length]);
                } else {
                    viewLookAtMaps.get(3).put(lookAtLocations[i][j], lookAtLocations[i - 1][j]);
                }
            }
        }
        //Decision Tree Overlay Map
        decisionTreeButtonTitles.put(0, "Start new game");
        decisionTreeButtonTitles.put(1, "Search containers");
        decisionTreeButtonTitles.put(2, "Break lock by firing gun");
        decisionTreeButtonTitles.put(3, "Enter code: ");//If code is incorrect, loop back to 1
        decisionTreeButtonTitles.put(4, "Search body");
        decisionTreeButtonTitles.put(5, "Move on");
        decisionTreeButtonTitles.put(6, "Search cars");
        decisionTreeButtonTitles.put(7, "Break window of car");
        decisionTreeButtonTitles.put(8, "Open door with crowbar");
        decisionTreeButtonTitles.put(9, "Search warehouse");
        decisionTreeButtonTitles.put(10, "Open door quietly");
        decisionTreeButtonTitles.put(11, "Bring child with you");
        decisionTreeButtonTitles.put(12, "Attempt to escape");
        decisionTreeButtonTitles.put(13, "Attempt to hunt monster");
        decisionTreeButtonTitles.put(14, "Tell the child to stay put");
        decisionTreeButtonTitles.put(15, "Attempt to lock the container");
        decisionTreeButtonTitles.put(16, "Attempt to run");
        decisionTreeButtonTitles.put(17, "Knock on the door");
        decisionTreeButtonTitles.put(18, "Search offices");
        decisionTreeButtonTitles.put(19, "Search drawers");
        decisionTreeButtonTitles.put(20, "Continue looking around");
        decisionTreeButtonTitles.put(21, "Attack the monster");
        decisionTreeButtonTitles.put(22, "Hide");
        decisionTreeButtonTitles.put(23, "Search cars");
        decisionTreeButtonTitles.put(24, "Open door with key and hide inside");
        decisionTreeButtonTitles.put(25, "Hide behind container");
        decisionTreeButtonTitles.put(26, "Hide inside container");
        decisionTreeButtonTitles.put(27, "Start car");
        decisionTreeButtonTitles.put(28, "Leave without investigating further");
        decisionTreeButtonTitles.put(29, "Quit Game");

        //Decision Tree Index map
        for (int i = 0; i < 1; i++) { //Dummy loop to release temporary variables
            ArrayList<Integer> temp = new ArrayList<>();
            temp.add(0);
            temp.add(1);
            //Node 0: Start New Game
            temp.set(0, 1);//Search Containers
            temp.set(0, 18);//Search Offices
            decisionTreeIndex.put(0, (ArrayList<Integer>) temp.clone());

            //Node 1: Search Containers
            temp.set(0, 2);//Fire gun (death 1)
            temp.set(1, 3);//Solve puzzle (weapon get)
            decisionTreeIndex.put(1, (ArrayList<Integer>) temp.clone());

            //Node 2: Player Death 1
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(2, (ArrayList<Integer>) temp.clone());

            //Node 3: Solve puzzle (weapon get)
            temp.set(0, 4);//Search body (lore get)
            temp.set(1, 5);//Ignore body (no change)
            decisionTreeIndex.put(3, (ArrayList<Integer>) temp.clone());

            //Node 4: Search body (lore get)
            temp.set(0, 6);//Search cars
            temp.set(1, 9);//Investigate Scream
            decisionTreeIndex.put(4, (ArrayList<Integer>) temp.clone());

            //Node 5: Ignore body (no change)
            temp.set(0, 6);//Search cars
            temp.set(1, 9);//Investigate scream
            decisionTreeIndex.put(5, (ArrayList<Integer>) temp.clone());

            //Node 6: Search Cars
            temp.set(0, 7);//Break window (death 3)
            temp.set(1, 8);//Hide (merge with Node 24)
            decisionTreeIndex.put(6, (ArrayList<Integer>) temp.clone());

            //Node 7: Break window (death 3)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(7, (ArrayList<Integer>) temp.clone());

            //Node 8: Hide (merge with Node 24)
            temp.set(0, 25);//Trap in container (Ending 2: Hunter)
            temp.set(1, 26);//Hide in container (death 6)
            decisionTreeIndex.put(8, (ArrayList<Integer>) temp.clone());

            //Node 9: Investigate scream
            temp.set(0, 10);//Open the door quietly
            temp.set(1, 17);//Knock on the door (death 4)
            decisionTreeIndex.put(9, (ArrayList<Integer>) temp.clone());

            //Node 10: Open the door quietly
            temp.set(0, 11);//Bring child with you (partner get)
            temp.set(1, 14);//Lock child inside container (for safety)
            decisionTreeIndex.put(10, (ArrayList<Integer>) temp.clone());

            //Node 11: Bring them with you (partner get)
            temp.set(0, 12);//Escape (Ending 3)
            temp.set(1, 13);//Hunt monster (death 8)
            decisionTreeIndex.put(11, (ArrayList<Integer>) temp.clone());

            //Node 12: Escape (Ending 3: Job Done)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(12, (ArrayList<Integer>) temp.clone());

            //Node 13: Hunt monster (death 8)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(13, (ArrayList<Integer>) temp.clone());

            //Node 14: Lock child inside container (for safety)
            temp.set(0, 15);//Trap monster in container (Ending 4: Cruel Work)
            temp.set(1, 16);//Run for it (death 7)
            decisionTreeIndex.put(14, (ArrayList<Integer>) temp.clone());

            //Node 15: Trap monster in container (Ending 4: Cruel Work)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(15, (ArrayList<Integer>) temp.clone());

            //Node 16: Run for it (death 7)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(16, (ArrayList<Integer>) temp.clone());

            //Node 17: Knock on the door (death 4)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(17, (ArrayList<Integer>) temp.clone());

            //Node 18: Search offices
            temp.set(0, 19);//Opens drawers (key get)
            temp.set(1, 20);//Ignore drawers (no change)
            decisionTreeIndex.put(18, (ArrayList<Integer>) temp.clone());

            //Node 19: Search offices
            temp.set(0, 21);//Confront entity (death 2)
            temp.set(1, 22);//Hide (no change)
            decisionTreeIndex.put(19, (ArrayList<Integer>) temp.clone());

            //Node 20: Ignore drawers (no change)
            temp.set(0, 21);//Confront entity (death 2)
            temp.set(1, 22);//Hide (no change)
            decisionTreeIndex.put(20, (ArrayList<Integer>) temp.clone());

            //Node 21: Confront entity (death 2)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(21, (ArrayList<Integer>) temp.clone());

            //Node 22: Hide (no change)
            temp.set(0, 23);//Search cars
            temp.set(1, 28);//Leave (Ending 1: Coward)
            decisionTreeIndex.put(22, (ArrayList<Integer>) temp.clone());

            //Node 23: Search cars
            temp.set(0, 24);//Hide
            temp.set(1, 27);//Start the car (death 5)
            decisionTreeIndex.put(23, (ArrayList<Integer>) temp.clone());

            //Node 24: Hide
            temp.set(0, 25);//Trap in container (Ending 2: Hunter)
            temp.set(1, 26);//Hide in container (death 6)
            decisionTreeIndex.put(24, (ArrayList<Integer>) temp.clone());

            //Node 25: Trap in container (Ending 2: Hunter)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(25, (ArrayList<Integer>) temp.clone());

            //Node 26: Hide in container (death 6)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(26, (ArrayList<Integer>) temp.clone());

            //Node 27: Start the car (death 5)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(27, (ArrayList<Integer>) temp.clone());

            //Node 28: Leave (Ending 1: Coward)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(28, (ArrayList<Integer>) temp.clone());

        }
        //Movement tree map
        for (int i = 0; i < 1; i++) {
            ArrayList<Point3d> temp = new ArrayList<>();
            temp.add(new Point3d());
            temp.add(new Point3d());
            //Node 0:
            temp.set(0, new Point3d(0.525f, -1.050f, 2.425f));
            temp.set(1, new Point3d(0.000f * cameraScale, 0.000f * cameraScale, -1.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 1:
            temp.set(0, new Point3d(-0.050f, -1.050f, 1.100f));
            temp.set(1, new Point3d(-0.500f * cameraScale, 0.000f * cameraScale, -0.500f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 2:
            temp.set(0, new Point3d(0.050f, -1.100f, 1.100f));
            temp.set(1, new Point3d(-0.500f * cameraScale, 1.000f * cameraScale, -0.500f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 3:
            temp.set(0, new Point3d(-0.075f, -1.050f, 1.025f));
            temp.set(1, new Point3d(-0.500f * cameraScale, -0.500f * cameraScale, -0.500f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 4:
            temp.set(0, new Point3d(0.600f, -1.000f, -1.200f));
            temp.set(1, new Point3d(0.500f * cameraScale, -0.500f * cameraScale, -0.500f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 5:
            temp.set(0, new Point3d(0.600f, -1.000f, -1.200f));
            temp.set(1, new Point3d(0.500f * cameraScale, 0.000f * cameraScale, -0.500f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 6:
            temp.set(0, new Point3d(0.400f, -1.00f, 0.250f));
            temp.set(1, new Point3d(-0.250f * cameraScale, 0.000f * cameraScale, -0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 7:
            temp.set(0, new Point3d(-0.425, -1.050f, 0.225f));
            temp.set(1, new Point3d(0.750f * cameraScale, 1.000f * cameraScale, 0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 8:
            temp.set(0, new Point3d(-0.425f, -1.050f, -0.025f));
            temp.set(1, new Point3d(1.000f * cameraScale, 0.000f * cameraScale, 0.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 9:
            temp.set(0, new Point3d(-0.125f, -1.050f, 0.275f));
            temp.set(1, new Point3d(-1.000f * cameraScale, 0.000f * cameraScale, 0.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 10:
            temp.set(0, new Point3d(-0.125f, -1.000f, 0.275f));
            temp.set(1, new Point3d(1.000f * cameraScale, 0.000f * cameraScale, 0.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 11:
            temp.set(0, new Point3d(-0.375f, -1.000f, 0.175f));
            temp.set(1, new Point3d(-0.750f * cameraScale, -0.500f * cameraScale, -0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 12:
            temp.set(0, new Point3d(-0.400f, -1.000f, 0.900f));
            temp.set(1, new Point3d(-1.000f * cameraScale, 0.000f * cameraScale, 0.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 13:
            temp.set(0, new Point3d(0.625f, -1.100f, 0.825f));
            temp.set(1, new Point3d(0.250f * cameraScale, 1.000f * cameraScale, -0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 14:
            temp.set(0, new Point3d(-1.250f, -1.050f, 0.275f));
            temp.set(1, new Point3d(0.750f * cameraScale, 0.000f * cameraScale, 0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 15:
            temp.set(0, new Point3d(0.400f, -1.050f, 0.750f));
            temp.set(1, new Point3d(-0.750f * cameraScale, 0.000f * cameraScale, +0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 16:
            temp.set(0, new Point3d(0.675f, -1.100f, -0.725f));
            temp.set(1, new Point3d(0.250f * cameraScale, 0.500f * cameraScale, 0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 17:
            temp.set(0, new Point3d(-0.125f, -1.100f, 0.175f));
            temp.set(1, new Point3d(0.250f * cameraScale, 1.000f * cameraScale, -0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 18:
            temp.set(0, new Point3d(1.400f, -1.000f, 1.800f));
            temp.set(1, new Point3d(1.000f * cameraScale, 0.000f * cameraScale, 0.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 19:
            temp.set(0, new Point3d(1.325f, -1.000f, 1.925f));
            temp.set(1, new Point3d(-0.500f * cameraScale, -0.500f * cameraScale, -0.5000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 20:
            temp.set(0, new Point3d(1.125f, -1.000f, 1.825f));
            temp.set(1, new Point3d(0.750f * cameraScale, 0.000f * cameraScale, -0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 21:
            temp.set(0, new Point3d(1.150f, -1.100f, 1.750f));
            temp.set(1, new Point3d(0.750f * cameraScale, 1.000f * cameraScale, 0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 22:
            temp.set(0, new Point3d(1.200f, -1.100f, 1.850f));
            temp.set(1, new Point3d(0.750f * cameraScale, 0.000f * cameraScale, -0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 23:
            temp.set(0, new Point3d(0.950f, -1.00f, -0.600f));
            temp.set(1, new Point3d(0.250f * cameraScale, 0.000f * cameraScale, -0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 24:
            temp.set(0, new Point3d(1.175f, -1.050f, -0.725f));
            temp.set(1, new Point3d(-0.250f * cameraScale, 0.000f * cameraScale, 0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 25:
            temp.set(0, new Point3d(0.400f, -1.050f, 0.750f));
            temp.set(1, new Point3d(-0.750f * cameraScale, 0.000f * cameraScale, 0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 26:
            temp.set(0, new Point3d(0.300f, -1.100f, 0.850f));
            temp.set(1, new Point3d(-0.250f * cameraScale, 1.000f * cameraScale, -0.750f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 27:
            temp.set(0, new Point3d(1.000f, -1.100f, -0.600f));
            temp.set(1, new Point3d(-0.750f * cameraScale, 0.500f * cameraScale, 0.250f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
            //Node 28:
            temp.set(0, new Point3d(0.550f, -1.050f, 2.400f));
            temp.set(1, new Point3d(0.000f * cameraScale, 0.500f * cameraScale, 1.000f * cameraScale));
            movementTree.add((ArrayList<Point3d>) temp.clone());
        }

        //File input for dialog
        for (int i = 0; i < decisionTreeIndex.size(); i++) {
            //initialize dialogTree
            ArrayList<String> temp = new ArrayList<>();
            //fetch dialog for each event
            BufferedReader inputStream = null;
            try {
                inputStream = new BufferedReader(new FileReader("Scripts/dialogDecision" + i + ".txt"));
                String line;
                while ((line = inputStream.readLine()) != null) {
                    if (!line.isEmpty()) {
                        temp.add(line);
                    }
                }
                if (!temp.isEmpty() && temp.get(0).compareTo("") != 0) {
                    dialogTree.add((ArrayList<String>) temp.clone());
                }
                temp.clear();
            } catch (IOException e) {
                System.out.println("File not found: " + e);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    System.out.println("Could not close fileStream: " + e);
                }
            }
        }
    }

    /**
     * Documentation for UpdateCamera ***************************** The camera
     * updates simple universe su using a customized mouse listener, the
     * listener is itself only meant to retrieve the X and Y of the mouse where
     * it is on the screen.
     *
     * You must click and hold the left mouse button in order for it to track
     * currently.
     *
     * WASD will move through the world as expected. W will move forward S will
     * move back A will strafe left D will strafe right
     *
     * The lookAt transform is the meat of the method.
     *
     */
    private void updateCamera(TransformGroup tg) {
        if (Listener != null) {
            int temp = Listener.getB();
            Listener.setB(-1);
            if (temp != -1) {
                int lookDirection = -1;
                int moveDirection = -1;
                if (temp == 27) {
                    System.exit(0);
                }
                if (temp != 0) {
                    System.out.println("key: " + temp);
                }
                if (consoleInitialized) {
                    if (decisionIndex == 3 && !inventory[0]) {
                        switch (temp) {
                            case 96:
                            case 48:
                                codePuzzle("0", tg);
                                break;
                            case 97:
                            case 49:
                                codePuzzle("1", tg);
                                break;
                            case 98:
                            case 50:
                                codePuzzle("2", tg);
                                break;
                            case 99:
                            case 51:
                                codePuzzle("3", tg);
                                break;
                            case 100:
                            case 52:
                                codePuzzle("4", tg);
                                break;
                            case 101:
                            case 53:
                                codePuzzle("5", tg);
                                break;
                            case 102:
                            case 54:
                                codePuzzle("6", tg);
                                break;
                            case 103:
                            case 55:
                                codePuzzle("7", tg);
                                break;
                            case 104:
                            case 56:
                                codePuzzle("8", tg);
                                break;
                            case 105:
                            case 57:
                                codePuzzle("9", tg);
                                break;
                            case 8:
                                codePuzzle("-", tg);
                                break;
                            case 10:
                                submitPuzzle(tg);
                                break;
                            default:
                                break;
                        }
                    }
                    if (!decisionsDisabled) {
                        switch (temp) {
                            case 65:
                                eventDecision(decisionTreeIndex.get(decisionIndex).get(0), tg);
                                break;
                            case 68:
                                eventDecision(decisionTreeIndex.get(decisionIndex).get(1), tg);
                                break;
                            default:
                                break;
                        }
                    }
                    switch (temp) {
                        case 83:
                            eventDecision(decisionIndex, tg);
                            break;
                        case 87:
                            moveDirection = 3;
                            break;
                        case 78:
                            if (!noClipMode) {
                                noClipMode = true;
                                updateConsole("No clip mode activated", textDelay, tg);
                            } else {
                                noClipMode = false;
                                updateConsole("No clip mode deactivated", textDelay, tg);
                            }
                            break;
                        default:
                            break;
                    }
                    if (!lookDisabled) {
                        switch (temp) {
                            case 37:
                                lookDirection = 0;
                                break;
                            case 39:
                                lookDirection = 1;
                                break;
                            case 38:
                                lookDirection = 2;
                                break;
                            case 40:
                                lookDirection = 3;
                                break;
                            default:
                                break;
                        }
                    }
                }
                if (!consoleInitialized && (temp == 10)) {
                    consoleInitialized = true;
                    //Initialize console
                    eventDecision(0, tg);
                }
                if (moveDirection != -1) {
                    if (noClipMode && moveDirection == 3) {
                        Transform3D lookAt = new Transform3D();
                        viewPosition[0].setX(viewPosition[0].getX() + viewPosition[2].getX() / 0.10f);
                        viewPosition[0].setY(viewPosition[0].getY() + viewPosition[2].getY() / 0.10f);
                        viewPosition[0].setZ(viewPosition[0].getZ() + viewPosition[2].getZ() / 0.10f);
                        viewPosition[1].setX(viewPosition[0].getX() + viewPosition[2].getX());
                        viewPosition[1].setY(viewPosition[0].getY() + viewPosition[2].getY());
                        viewPosition[1].setZ(viewPosition[0].getZ() + viewPosition[2].getZ());
                        lookAt.lookAt(viewPosition[0], viewPosition[1], new Vector3d(0.0f, 1.0f, 0.0f));
                        lookAt.invert();
                        tg.setTransform(lookAt);
                        System.out.println("New position of center relative to world: ");
                        System.out.println("X: " + viewPosition[0].getX() + " Y: " + viewPosition[0].getY() + " Z: " + viewPosition[0].getZ());
                    }
                }
                if (lookDirection != -1) {
                    Transform3D lookAt = new Transform3D();
                    lookAt.lookAt(viewPosition[0], lookAtMaps(lookDirection), new Vector3d(0.0f, 1.0f, 0.0f));
                    lookAt.invert();
                    tg.setTransform(lookAt);
                }

                Listener.setB(0);
            }
        }
    }

    private void eventDecision(Integer moveToNode, TransformGroup tg) {
        decisionIndex = moveToNode;
        switch (decisionIndex) {
            case 0:
                lookDisabled = false;
                dead = false;
                for (int i = 0; i < inventory.length; i++) {
                    inventory[i] = false;
                }
                if (inputCode.length() > 0) inputCode.delete(0, inputCode.length() - 1);
                break;
            case 2:
                lookDisabled = true;
                dead = true;
                break;
            case 7:
                lookDisabled = true;
                dead = true;
                break;
            case 11:
                inventory[2] = true;
                break;
            case 13:
                lookDisabled = true;
                dead = true;
                break;
            case 16:
                lookDisabled = true;
                dead = true;
                break;
            case 17:
                lookDisabled = true;
                dead = true;
                break;
            case 19:
                inventory[1] = true;
                break;
            case 21:
                lookDisabled = true;
                dead = true;
                break;
            case 26:
                lookDisabled = true;
                dead = true;
                break;
            case 28:
                lookDisabled = true;
                dead = true;
                break;
            case 29:
                System.exit(0);
            default:
                break;
        }
        ArrayList<Integer> nextChoices = decisionTreeIndex.get(decisionIndex);
        if (decisionIndex != 29) {
            buttonText[0] = decisionTreeButtonTitles.get(nextChoices.get(0));
            buttonText[1] = decisionTreeButtonTitles.get(nextChoices.get(1));
        }
        ArrayList<Point3d> currentPosition = movementTree.get(decisionIndex);
        Transform3D lookAt = new Transform3D();
        if (decisionIndex == 0) {
            viewPosition[0] = new Point3d(0.525f, -1.050f, 2.425f);
            viewPosition[2] = new Point3d(0.000f * cameraScale, 0.000f * cameraScale, -1.000f * cameraScale); //Must be located in the lookAtLocations
            viewPosition[1].setX(viewPosition[0].getX() + viewPosition[2].getX());
            viewPosition[1].setY(viewPosition[0].getY() + viewPosition[2].getY());
            viewPosition[1].setZ(viewPosition[0].getZ() + viewPosition[2].getZ());
        } else {
            //Move to new location
            viewPosition[0] = currentPosition.get(0);
            viewPosition[2] = currentPosition.get(1);
            viewPosition[1].setX(viewPosition[0].getX() + viewPosition[2].getX());
            viewPosition[1].setY(viewPosition[0].getY() + viewPosition[2].getY());
            viewPosition[1].setZ(viewPosition[0].getZ() + viewPosition[2].getZ());
        }

        lookAt.lookAt(viewPosition[0], viewPosition[1], new Vector3d(0.0f, 1.0f, 0.0f));
        lookAt.invert();
        tg.setTransform(lookAt);

        if (decisionIndex == 3 && !inventory[0]) {
            decisionsDisabled = true;
            updateConsole("Enter numbers on the numpad or number keys, press backspace to delete a number, press Enter to submit code.", 1, tg);
        } else if(decisionIndex == 3 && inventory[0]) {
            viewPosition[0] = new Point3d(0.600f, -1.000f, -1.200f);
            viewPosition[2] = new Point3d(0.500f * cameraScale, -0.500f * cameraScale, -0.500f * cameraScale);
            viewPosition[1].setX(viewPosition[0].getX() + viewPosition[2].getX());
            viewPosition[1].setY(viewPosition[0].getY() + viewPosition[2].getY());
            viewPosition[1].setZ(viewPosition[0].getZ() + viewPosition[2].getZ());
        }
        if (!decisionsDisabled || inventory[0]) {
            //Update console for current dialog
            ArrayList<String> currentDialog = dialogTree.get(decisionIndex);
            for (int i = 0; i < currentDialog.size(); i++) {
                updateConsole(currentDialog.get(i), textDelay, tg);
                if ((currentDialog.get(i).trim().startsWith("CHOOSE"))
                        || (currentDialog.get(i).trim().startsWith("GAME OVER"))
                        || (currentDialog.get(i).trim().startsWith("VICTORY"))) {
                    updateConsole("Press 'a' to: " + buttonText[0] + " | press 'd' to: " + buttonText[1] + " | press 's' to read the dialogue again.", textDelay, tg);
                    /*                    updateConsole("OR", 1, tg);
                    updateConsole("Press 'd' to: " + buttonText[1], 1, tg);
                    updateConsole("OR", 1, tg);
                    updateConsole("Press 's' to: Read dialogue again.", 1, tg);*/
                }
            }
        }
    }

    private void updateConsole(String s, int lineDelay, TransformGroup tg) {
        console[5] = console[4];
        console[4] = console[3];
        console[3] = console[2];
        console[2] = console[1];
        console[1] = console[0];
        console[0] = s;
        Transform3D lookAt = new Transform3D();
        lookAt.lookAt(viewPosition[0], viewPosition[1], new Vector3d(0.0f, 1.0f, 0.0f));
        lookAt.invert();
        tg.setTransform(lookAt);
        int nextLineDelay = lineDelay;
        if (s.isEmpty()) {
            nextLineDelay = 3;
        }
        try {
            TimeUnit.SECONDS.sleep(nextLineDelay);
        } catch (InterruptedException e) {
            System.out.println("An issue occured while attempting to wait: " + e);
        }
    }

    private void codePuzzle(String number, TransformGroup tg) {
        if (number.compareTo("-") == 0 && inputCode.length() > 0) {
            //Delete last entered number
            inputCode.deleteCharAt(inputCode.length() - 1);
        } else if (number.compareTo("-") != 0) {
            inputCode.append(number);
        }
        updateConsole("John presses the '" + number + "' key", 0, tg);
        updateConsole("Current input code: " + inputCode.toString(), 0, tg);
        if (inputCode.length() > code.length()) {
            submitPuzzle(tg);
        }
    }

    private void submitPuzzle(TransformGroup tg) {
        System.out.println("Code lengths are equal? " + (inputCode.length() == code.length()));
        if (inputCode.length() == code.length()) {
            System.out.println("Code and input code are the same? " + (inputCode.toString().compareTo(code) == 0));
            if (inputCode.toString().compareTo(code) == 0) {
                updateConsole("The electronic lock rings out an electronic beep, with an audible click as it unlocks.", textDelay, tg);
                decisionsDisabled = false;
                inventory[0] = true;
                updateConsole("*Crowbar obtained*", textDelay, tg);
                updateConsole("John: \"Not what I was looking for, but this could come in handy.\"", textDelay, tg);
                //updateConsole("John walks out into the parking lot.", textDelay, tg);
                eventDecision(decisionIndex, tg);
            } else {
                inputCode.delete(0, inputCode.length() - 1);
                updateConsole("John: That code wasn't right...", textDelay, tg);
                decisionsDisabled = false;
                eventDecision(1, tg);
            }
        }
    }

    private BranchGroup createObject(String fileName) {

        BranchGroup objRoot = new BranchGroup();
        TransformGroup tg = new TransformGroup();
        Transform3D t3d = new Transform3D();

        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        t3d.setScale(5.0);

        tg.setTransform(t3d);

        ObjectFile loader = new ObjectFile(ObjectFile.RESIZE);
        Scene s = null;

        File file = new java.io.File("Objects/" + fileName + ".obj");

        try {
            s = loader.load(file.toURI().toURL());
        } catch (IncorrectFormatException | ParsingErrorException | FileNotFoundException | MalformedURLException e) {
            System.err.println(e);
            System.exit(1);
        }
        tg.addChild(s.getSceneGroup());

        objRoot.addChild(tg);

        return objRoot;

    }

    public  void setOpacity(float opacity){
        this.opacity = opacity;
    }

    public static double clamp(double input, double min, double max) {
        return (input < min) ? min : (input > max) ? max : input;
    }

}
