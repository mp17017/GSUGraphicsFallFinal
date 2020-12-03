package FinalProject;

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
import java.io.File;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

//Marc Newman, for Graphics
public class finalProjectTestv2 extends Applet {

    private CML Listener;
    private int DELAY = 10;
    private float posX = 0.0f;
    private float posY = 0.0f;
    private float posZ = 0.0f;
    private double Strafe = 0.0;
    private float cameraScale = 0.01f;
    //Point3d array to hold points the camera can look at
    private int numCircles = 5;
    private int numPoints = 16;
    private final Point3d[][] lookAtLocations = new Point3d[numCircles][numPoints];
    //Indices for event tree
    private boolean[] inventory = {false, false, false};
    private int decisionIndex = 0;
    private final Map<Integer, ArrayList<Integer>> decisionTreeIndex = new HashMap<Integer, ArrayList<Integer>>();
    private final Map<Integer, String> decisionTreeButtonTitles = new HashMap<Integer, String>();
    private final ArrayList<ArrayList<String>> dialogTree = new ArrayList<ArrayList<String>>();
    private final ArrayList<ArrayList<Point3d>> movementTree = new ArrayList<ArrayList<Point3d>>();
    //Point3d map ArrayList to determine which points to look at given a lookDirection
    //[0] look counter clockwise/left, [1] look clockwise/right, [2] look up/yaw up, [3] look down/yaw down
    private ArrayList<Map<Point3d, Point3d>> viewLookAtMaps = new ArrayList<Map<Point3d, Point3d>>();

    //Point3d array to hold variables associated with the camera's position
    //[0] current position, [1] eye position, [2] relative eye position (will always have a unit value to offset from [0])
    //[0] == camera position relative to world, [1] == position the camera is looking at relative to world, [2] == position camera is looking at relative to current position
    private final Point3d eyeOrigin = new Point3d(0.0f, -0.3f, 0.0f);
    private final Point3d centerOffset = new Point3d(0.0f * cameraScale, 0.0f * cameraScale, -1.0f * cameraScale); //Must be located in the lookAtLocations
    private final Point3d centerOrigin = new Point3d(eyeOrigin.getX() + centerOffset.getX(), eyeOrigin.getY() + centerOffset.getY(), eyeOrigin.getZ() + centerOffset.getZ());
    private Point3d[] viewPosition = {eyeOrigin, centerOrigin, centerOffset};

    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        new MainFrame(new finalProjectTestv2(), 800, 600);
    }

    //@Override
    public finalProjectTestv2() {
        initializeVariables();
        if (dialogTree.isEmpty()) System.exit(0);
        //initialize universe

        GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
        this.setLayout(new GridLayout(1, 2));
        Canvas3D cv = new Canvas3D(gc);

        SimpleUniverse su = new SimpleUniverse(cv);
        cv = new Canvas3D(gc){
            public void postRender() {
                Graphics2D G2D = this.getGraphics2D();
                G2D.setColor(Color.LIGHT_GRAY);
                G2D.fill3DRect(0, 0, 100, this.getHeight()-100, true);
                G2D.fill3DRect(100, this.getHeight()-200, this.getWidth(), 200, true);
                G2D.fill3DRect(0, this.getHeight()-100, 100, 100, true);
                G2D.setColor(Color.BLACK);
                G2D.drawString("FPS: ", 100, 100);
                G2D.drawString("X: "+ viewPosition[0].getX(), 5, 80);
                G2D.drawString("Y: "+ viewPosition[0].getY(), 5, 60);
                G2D.drawString("Z: "+ viewPosition[0].getZ(), 5, 40);
                G2D.drawString("MENU",30 , this.getHeight()-45);
                this.getGraphics2D().flush(false);
            }
        };
        add(cv);
        View view = new View();
        view.setProjectionPolicy(View.PERSPECTIVE_PROJECTION);
        view.setFieldOfView(0.5*Math.PI);
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
        bg.compile();
        su.addBranchGraph(bg);

        Timer timer = new Timer(DELAY, arg0 -> {
            validate();
            updateCamera(tg, vp);
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

    private void initializeVariables() {
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
            viewLookAtMaps.add(new HashMap<Point3d, Point3d>());
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
        decisionTreeButtonTitles.put(5, "Ignore body");
        decisionTreeButtonTitles.put(6, "Search cars");
        decisionTreeButtonTitles.put(7, "Break window of car");
        decisionTreeButtonTitles.put(8, "Hide");
        decisionTreeButtonTitles.put(9, "Investigate scream");
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
        decisionTreeButtonTitles.put(21, "Investigate entity");
        decisionTreeButtonTitles.put(22, "Hide");
        decisionTreeButtonTitles.put(23, "Search cars");
        decisionTreeButtonTitles.put(24, "Hide");
        decisionTreeButtonTitles.put(25, "Hide behind container");
        decisionTreeButtonTitles.put(26, "Hide inside container");
        decisionTreeButtonTitles.put(27, "Start car");
        decisionTreeButtonTitles.put(28, "Leave without investigating further");
        decisionTreeButtonTitles.put(29, "Quit Game");
        
        //Decision Tree Index map
        for (int i = 0; i < 1; i++) { //Dummy loop to release temporary variables
            ArrayList<Integer> temp = new ArrayList<Integer>();
            temp.add(0);
            temp.add(1);
            //Node 0: Start New Game
            temp.set(0, 1);//Search Containers
            temp.set(1, 18);//Search Offices
            decisionTreeIndex.put(0, temp);
            
            //Node 1: Search Containers
            temp.set(0, 2);//Fire gun (death 1)
            temp.set(1, 3);//Solve puzzle (weapon get)
            decisionTreeIndex.put(1, temp);
            
            //Node 2: Player Death 1
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(2, temp);
            
            //Node 3: Solve puzzle (weapon get)
            temp.set(0, 4);//Search body (lore get)
            temp.set(1, 5);//Ignore body (no change)
            decisionTreeIndex.put(3, temp);
            
            //Node 4: Search body (lore get)
            temp.set(0, 6);//Search cars
            temp.set(1, 9);//Investigate Scream
            decisionTreeIndex.put(4, temp);
            
            //Node 5: Ignore body (no change)
            temp.set(0, 6);//Search cars
            temp.set(1, 9);//Investigate scream
            decisionTreeIndex.put(5, temp);
            
            //Node 6: Search Cars
            temp.set(0, 7);//Break window (death 3)
            temp.set(1, 8);//Hide (merge with Node 24)
            decisionTreeIndex.put(6, temp);
            
            //Node 7: Break window (death 3)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(7, temp);
            
            //Node 8: Hide (merge with Node 24)
            temp.set(0, 26);//Trap in container (Ending 2: Hunter)
            temp.set(1, 27);//Hide in container (death 6)
            decisionTreeIndex.put(8, temp);
            
            //Node 9: Investigate scream
            temp.set(0, 10);//Open the door quietly
            temp.set(1, 17);//Knock on the door (death 4)
            decisionTreeIndex.put(9, temp);
            
            //Node 10: Open the door quietly
            temp.set(0, 11);//Bring child with you (partner get)
            temp.set(1, 14);//Lock child inside container (for safety)
            decisionTreeIndex.put(10, temp);
            
            //Node 11: Bring them with you (partner get)
            temp.set(0, 12);//Escape (Ending 3)
            temp.set(1, 13);//Hunt monster (death 8)
            decisionTreeIndex.put(11, temp);
            
            //Node 12: Escape (Ending 3: Job Done)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(12, temp);
            
            //Node 13: Hunt monster (death 8)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(13, temp);
            
            //Node 14: Lock child inside container (for safety)
            temp.set(0, 15);//Trap monster in container (Ending 4: Cruel Work)
            temp.set(1, 16);//Run for it (death 7)
            decisionTreeIndex.put(14, temp);
            
            //Node 15: Trap monster in container (Ending 4: Cruel Work)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(15, temp);
            
            //Node 16: Run for it (death 7)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(16, temp);
            
            //Node 17: Knock on the door (death 4)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(17, temp);
            
            //Node 18: Search offices
            temp.set(0, 19);//Opens drawers (key get)
            temp.set(1, 20);//Ignore drawers (no change)
            decisionTreeIndex.put(18, temp);
            
            //Node 19: Search offices
            temp.set(0, 21);//Confront entity (death 2)
            temp.set(1, 22);//Hide (no change)
            decisionTreeIndex.put(19, temp);
            
            //Node 20: Ignore drawers (no change)
            temp.set(0, 21);//Confront entity (death 2)
            temp.set(1, 22);//Hide (no change)
            decisionTreeIndex.put(20, temp);
            
            //Node 21: Confront entity (death 2)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(21, temp);
            
            //Node 22: Hide (no change)
            temp.set(0, 23);//Search cars
            temp.set(1, 28);//Leave (Ending 1: Coward)
            decisionTreeIndex.put(22, temp);
            
            //Node 23: Search cars
            temp.set(0, 24);//Hide
            temp.set(1, 27);//Start the car (death 5)
            decisionTreeIndex.put(23, temp);
            
            //Node 24: Hide
            temp.set(0, 25);//Trap in container (Ending 2: Hunter)
            temp.set(1, 26);//Hide in container (death 6)
            decisionTreeIndex.put(24, temp);
            
            //Node 25: Trap in container (Ending 2: Hunter)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(25, temp);
            
            //Node 26: Hide in container (death 6)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(26, temp);
            
            //Node 27: Start the car (death 5)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(27, temp);
            
            //Node 28: Leave (Ending 1: Coward)
            temp.set(0, 0);//Restart prompt YES
            temp.set(1, 29);//Restart prompt NO
            decisionTreeIndex.put(28, temp);
            
        }
        //Movement tree map
        /*for (int i = 0; i < 1; i++) {
            ArrayList<Point3d> temp = new ArrayList<Point3d>();
            //Node 0:
            temp.set(0, cameraPosition);
            temp.set(1, eyePosition);
            movementTree.add(temp);
            
            //Node :
            temp.set(0, );
            temp.set(1, );
            movementTree.add(temp);
            
        }
        */
        //File input for dialog
        for (int i = 0; i < decisionTreeIndex.size(); i++) {
            //initialize dialogTree
            ArrayList<String> temp = new ArrayList<String>();
            //fetch dialog for each event
            BufferedReader inputStream = null;
            try {
               inputStream = new BufferedReader(new FileReader("dialogDecision" +i +".txt"));
               String line;
               while ((line = inputStream.readLine()) != null) {
                   temp.add(line);
               }
               dialogTree.add(temp);
               temp.clear();
            }catch (IOException e) {
                System.out.println("File not found: " +e);
            }finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    System.out.println("Could not close fileStream: " +e);
                }
            }
            dialogTree.add(temp);
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

    private void updateCamera(TransformGroup tg, ViewPlatform vp) {
        if (Listener != null) {
            int temp = Listener.getB();
            Listener.setB(-1);
            if (temp != -1) {
                int lookDirection = -1;
                int moveDirection = -1;
                float prevPosX = posX;
                posX = Listener.getX();
                float prevPosY = posY;
                posY = Listener.getY();
                //System.out.println(Listener.getB());
                if (temp != 0) {
                    System.out.println("key: " + temp);
                }
                switch (temp) {
                    case 65:
                        moveDirection = 0;
                        break;
                    case 68:
                        moveDirection = 1;
                        break;
                    case 83:
                        moveDirection = 2;
                        break;
                    case 87:
                        moveDirection = 3;
                        break;
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
                //System.out.println("POS:" + posX + " " + posY);
                if (moveDirection != -1) {
                    if (moveDirection == 3) {
                        Transform3D lookAt = new Transform3D();
                        viewPosition[0].setX(viewPosition[0].getX() + viewPosition[2].getX()/0.10f);
                        viewPosition[0].setY(viewPosition[0].getY() + viewPosition[2].getY()/0.10f);
                        viewPosition[0].setZ(viewPosition[0].getZ() + viewPosition[2].getZ()/0.10f);
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

    private BranchGroup createObject(String fileName) {

        BranchGroup objRoot = new BranchGroup();
        TransformGroup tg = new TransformGroup();
        Transform3D t3d = new Transform3D();

        tg.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        t3d.setScale(5.0);

        tg.setTransform(t3d);

        ObjectFile loader = new ObjectFile(ObjectFile.RESIZE);
        Scene s = null;

        File file = new java.io.File("src/Project Files/Objects/" + fileName + ".obj");

        try {
            s = loader.load(file.toURI().toURL());
        } catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }

        tg.addChild(s.getSceneGroup());

        objRoot.addChild(tg);

        return objRoot;

    }

    private Light createLight() {
        DirectionalLight light = new DirectionalLight(true, new Color3f(1.0f, 1.0f, 1.0f),
                new Vector3f(-0.3f, 0.2f, -1.0f));

        light.setInfluencingBounds(new BoundingSphere(new Point3d(), 10000.0));

        return light;
    }

    public static double clamp(double input, double min, double max) {
        return (input < min) ? min : (input > max) ? max : input;
    }

}