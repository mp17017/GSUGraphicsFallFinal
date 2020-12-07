/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Project;

/**
 *
 * @author liamcurtis
 */
import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.geometry.*;
import java.applet.Applet;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.event.*;
import java.awt.*;
import Project.GameWindow;

public class StartMenu extends Applet implements MouseListener {

    public PickCanvas pickCanvas;
    private int lightR = 0;
    private int lightG = 0;
    private int lightB = 0;
    public static float opacity;
    Frame frame;
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    Dimension screenSize = toolkit.getScreenSize();
    SimpleUniverse universe;
    BranchGroup mainMenu = new BranchGroup();
    BranchGroup settingsMenu = settings();
    BranchGroup creditsMenu = credits();

    public StartMenu() {
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        mainMenu.setCapability(BranchGroup.ALLOW_DETACH);
        frame = new Frame("Horror Game");
        universe = new SimpleUniverse(canvas);
        canvas.setSize(800, 400);
        int x = (screenSize.width - frame.getWidth()) / 4;
        int y = (screenSize.height - frame.getHeight()) / 4;
        frame.setLocation(x, y);

        Appearance ap = new Appearance();
        ap.setMaterial(new Material());

        Font3D font = new Font3D(new Font("Herculanum", Font.PLAIN, 1), new FontExtrusion());
        Text3D text = new Text3D(font, "Horror Game");
        Shape3D titleLine1 = new Shape3D(text, ap);
        titleLine1.setName("Title");
        Text3D text2 = new Text3D(font, "For Graphics");
        Shape3D titleLine2 = new Shape3D(text2, ap);
        titleLine2.setName("Other Title");
        Text3D text3 = new Text3D(font, "New Game");
        Shape3D newGame = new Shape3D(text3, ap);
        newGame.setName("New Game");
        /*Text3D text4 = new Text3D(font, "Continue");
        Shape3D continueGame = new Shape3D(text4, ap);
        continueGame.setName("Continue");*/
        Text3D text5 = new Text3D(font, "Settings");
        Shape3D settings = new Shape3D(text5, ap);
        settings.setName("Settings");
        Text3D text6 = new Text3D(font, "Credits");
        Shape3D credits = new Shape3D(text6, ap);
        credits.setName("Credits");

        //Transformations
        //Title line 1
        Transform3D tr = new Transform3D();
        tr.setScale(0.08);
        tr.setTranslation(new Vector3f(-0.25f, 0.3f, 0f));
        TransformGroup tg = new TransformGroup(tr);
        mainMenu.addChild(tg);
        tg.addChild(titleLine1);
        //Title line 2
        Transform3D tr2 = new Transform3D();
        tr2.setScale(0.08);
        tr2.setTranslation(new Vector3f(-0.25f, 0.2f, 0f));
        TransformGroup tg2 = new TransformGroup(tr2);
        mainMenu.addChild(tg2);
        tg2.addChild(titleLine2);
        //New Game
        Transform3D tr3 = new Transform3D();
        tr3.setScale(0.08);
        tr3.setTranslation(new Vector3f(-0.25f, 0f, 0f));
        TransformGroup tg3 = new TransformGroup(tr3);
        mainMenu.addChild(tg3);
        tg3.addChild(newGame);
        //Continue Game
        /*Transform3D tr4 = new Transform3D();
        tr4.setScale(0.08);
        tr4.setTranslation(new Vector3f(-0.25f, -0.1f, 0f));
        TransformGroup tg4 = new TransformGroup(tr4);
        mainMenu.addChild(tg4);
        tg4.addChild(continueGame);*/
        //settings
        Transform3D tr5 = new Transform3D();
        tr5.setScale(0.08);
        tr5.setTranslation(new Vector3f(-0.25f, -0.2f, 0f));
        TransformGroup tg5 = new TransformGroup(tr5);
        mainMenu.addChild(tg5);
        tg5.addChild(settings);
        //Credits
        Transform3D tr6 = new Transform3D();
        tr6.setScale(0.08);
        tr6.setTranslation(new Vector3f(-0.25f, -0.3f, 0f));
        TransformGroup tg6 = new TransformGroup(tr6);
        mainMenu.addChild(tg6);
        tg6.addChild(credits);

        //Background
        BoundingSphere bounds = new BoundingSphere();

        Background background = new Background(1.0f, 1.0f, 1.0f);
        background.setColor(new Color3f(Color.BLACK));
        background.setApplicationBounds(bounds);
        mainMenu.addChild(background);

        // Lighting
        AmbientLight alight = new AmbientLight(true, new Color3f(getLightR(), getLightB(), getLightG()));
        alight.setInfluencingBounds(bounds);
        mainMenu.addChild(alight);

        PointLight light = new PointLight(new Color3f(Color.RED), new Point3f(1f, 1f, 1f),
                new Point3f(-0.5f, -1f, 0f));
        light.setInfluencingBounds(bounds);
        mainMenu.addChild(light);

        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(mainMenu);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent winEvent) {
                System.exit(0);
            }
        });
        frame.add(canvas);
        pickCanvas = new PickCanvas(canvas, mainMenu);
        pickCanvas.setMode(PickCanvas.BOUNDS);
        canvas.addMouseListener((MouseListener) this);
        frame.pack();
        frame.show();
    }

    public static void main(String[] args) {
        System.setProperty("sun.awt.noerasebackground", "true");
        new StartMenu();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        if (result == null) {
            System.out.println("Nothing picked");
        } else {
            Primitive p = (Primitive) result.getNode(PickResult.PRIMITIVE);
            Shape3D s = (Shape3D) result.getNode(PickResult.SHAPE3D);
            if (p != null) {
                System.out.println(p.getName());
            } else if (s != null) {
                if (s.getName().equals("New Game")) {
                    System.out.println("New Game Method Here");
                    frame.dispose();
                    GameWindow.main(new String[0]);
                }
                if (s.getName().equals("Continue")) {
                    System.out.println("Continue Method Here");
                }
                if (s.getName().equals("Settings")) {
                    System.out.println("Settings Method Here");
                    mainMenu.detach();
                    universe.addBranchGraph(settingsMenu);
                    pickCanvas = new PickCanvas(universe.getCanvas(), settingsMenu);
                    pickCanvas.setMode(PickCanvas.BOUNDS);

                }
                if (s.getName().equals("Credits")) {
                    System.out.println("Credits Method Here");
                    mainMenu.detach();
                    universe.addBranchGraph(creditsMenu);
                    pickCanvas = new PickCanvas(universe.getCanvas(), creditsMenu);
                    pickCanvas.setMode(PickCanvas.BOUNDS);
                }
                if (s.getName().equals("Close OK")) {
                    System.out.println("HI!");
                    settingsMenu.detach();
                    creditsMenu.detach();
                    universe.addBranchGraph(mainMenu);
                    pickCanvas = new PickCanvas(universe.getCanvas(), mainMenu);
                    pickCanvas.setMode(PickCanvas.BOUNDS);
                }

                if (s.getName().equals("Very Dark")) {
                    System.out.println("Very Dark Method Clicked");
                    this.opacity = (float) .6;
                }
                if (s.getName().equals("Dark")) {
                    System.out.println("Dark Clicked");
                    this.opacity = (float) .4;
                }
                if (s.getName().equals("Bright")) {
                    System.out.println("Bright Clicked");
                    this.opacity = (float) .2;
                }
                if (s.getName().equals("Very Bright")) {
                    System.out.println("Very Bright Clicked");
                    this.opacity = (float) 0;

                }
            } else {
                    System.out.println("null");
            }

        }

    }

    public int getLightR() {
        return lightR;
    }

    public void setLightR(int lightR) {
        this.lightR = lightR;
    }

    public int getLightG() {
        return lightG;
    }

    public void setLightG(int lightG) {
        this.lightG = lightG;
    }

    public int getLightB() {
        return lightB;
    }

    public void setLightB(int lightB) {
        this.lightB = lightB;
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    public BranchGroup settings() {
        settingsMenu = new BranchGroup();
        settingsMenu.setCapability(BranchGroup.ALLOW_DETACH);
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
        Canvas3D canvas = new Canvas3D(config);
        SimpleUniverse universe = new SimpleUniverse(canvas);
        Appearance ap = new Appearance();
        canvas.setSize(800, 400);
        ap.setMaterial(new Material());

        //Fonts
        Font3D scaryFont = new Font3D(new Font("Herculanum", Font.PLAIN, 1), new FontExtrusion());
        Font3D normFont = new Font3D(new Font("TimesRoman", Font.PLAIN, 1), new FontExtrusion());

        //Title
        Text3D text = new Text3D(scaryFont, "Settings");
        Shape3D titleSettings = new Shape3D(text, ap);
        titleSettings.setName("Title");

        //Brightness Buttons
        Text3D text3 = new Text3D(scaryFont, "Brightness:");
        Shape3D brightness = new Shape3D(text3, ap);
        brightness.setName("Brightness");
        Text3D textVeryBright = new Text3D(scaryFont, "Very Bright");
        Shape3D veryBright = new Shape3D(textVeryBright, ap);
        veryBright.setName("Very Bright");
        Text3D textBright = new Text3D(scaryFont, "Bright");
        Shape3D Bright = new Shape3D(textBright, ap);
        Bright.setName("Bright");
        Text3D textDark = new Text3D(scaryFont, "Dark");
        Shape3D Dark = new Shape3D(textDark, ap);
        Dark.setName("Dark");
        Text3D textVeryDark = new Text3D(scaryFont, " Very Dark");
        Shape3D veryDark = new Shape3D(textVeryDark, ap);
        veryDark.setName("Very Dark");

        /*//Volume Controls
        Text3D titleVol = new Text3D(scaryFont, "Volume:");
        Shape3D volume = new Shape3D(titleVol, ap);
        volume.setName("Volume");*/

        /*//Window Size Controls
        Text3D titleWinSize = new Text3D(scaryFont, "FullScreen Mode");
        Shape3D windowSize = new Shape3D(titleWinSize, ap);
        windowSize.setName("Window Size");
        //Transformations*/

        //Close Button
        Text3D closeOK = new Text3D(normFont, "OK");
        Shape3D closeText = new Shape3D(closeOK, ap);
        closeText.setName("Close OK");

        //Settings
        Transform3D tr = new Transform3D();
        tr.setScale(0.1);
        tr.setTranslation(new Vector3f(-0.20f, 0.3f, 0f));
        TransformGroup tg = new TransformGroup(tr);
        settingsMenu.addChild(tg);
        tg.addChild(titleSettings);
        //Brightness
        Transform3D tr3 = new Transform3D();
        tr3.setScale(0.07);
        tr3.setTranslation(new Vector3f(-0.75f, 0.2f, 0f));
        TransformGroup tg3 = new TransformGroup(tr3);
        settingsMenu.addChild(tg3);
        tg3.addChild(brightness);
        //Brightness: Very Dark
        Transform3D trvd = new Transform3D();
        trvd.setScale(0.05);
        trvd.setTranslation(new Vector3f(-0.75f, 0.1f, 0f));
        TransformGroup tgvd = new TransformGroup(trvd);
        settingsMenu.addChild(tgvd);
        tgvd.addChild(veryDark);
        //Brightness: Dark
        Transform3D trd = new Transform3D();
        trd.setScale(0.05);
        trd.setTranslation(new Vector3f(-0.35f, 0.1f, 0f));
        TransformGroup tgd = new TransformGroup(trd);
        settingsMenu.addChild(tgd);
        tgd.addChild(Dark);
        //Brightness: Bright
        Transform3D trb = new Transform3D();
        trb.setScale(0.05);
        trb.setTranslation(new Vector3f(-0.1f, 0.1f, 0f));
        TransformGroup tgb = new TransformGroup(trb);
        settingsMenu.addChild(tgb);
        tgb.addChild(Bright);
        //Brightness: Very Bright
        Transform3D trvb = new Transform3D();
        trvb.setScale(0.05);
        trvb.setTranslation(new Vector3f(0.2f, 0.1f, 0f));
        TransformGroup tgvb = new TransformGroup(trvb);
        settingsMenu.addChild(tgvb);
        tgvb.addChild(veryBright);

        /*//Volume
        Transform3D tr4 = new Transform3D();
        tr4.setScale(0.07);
        tr4.setTranslation(new Vector3f(-0.75f, -0f, 0f));
        TransformGroup tg4 = new TransformGroup(tr4);
        settingsMenu.addChild(tg4);
        tg4.addChild(volume);
        //Window Size
        Transform3D tr5 = new Transform3D();
        tr5.setScale(0.07);
        tr5.setTranslation(new Vector3f(-0.75f, -0.1f, 0f));
        TransformGroup tg5 = new TransformGroup(tr5);
        settingsMenu.addChild(tg5);
        tg5.addChild(windowSize);*/

        //OK
        Transform3D tr6 = new Transform3D();
        tr6.setScale(0.07);
        tr6.setTranslation(new Vector3f(-0.10f, -0.3f, 0f));
        TransformGroup tg6 = new TransformGroup(tr6);
        settingsMenu.addChild(tg6);
        tg6.addChild(closeText);

        //Background
        BoundingSphere bounds = new BoundingSphere();
        Background background = new Background(1.0f, 1.0f, 1.0f);
        background.setColor(new Color3f(Color.BLACK));
        background.setApplicationBounds(bounds);
        settingsMenu.addChild(background);

        // Lighting
        AmbientLight alight = new AmbientLight(true, new Color3f(getLightR(), getLightB(), getLightG()));
        alight.setInfluencingBounds(bounds);
        settingsMenu.addChild(alight);
        PointLight light = new PointLight(new Color3f(Color.RED), new Point3f(1f, 1f, 1f),
                new Point3f(-0.5f, -1f, 0f));
        light.setInfluencingBounds(bounds);
        settingsMenu.addChild(light);

        universe.getViewingPlatform().setNominalViewingTransform();
        return settingsMenu;
    }

    public BranchGroup credits() {
        creditsMenu = new BranchGroup();
        creditsMenu.setCapability(BranchGroup.ALLOW_DETACH);
        Appearance ap = new Appearance();
        ap.setMaterial(new Material());

        //Fonts
        Font3D scaryFont = new Font3D(new Font("Herculanum", Font.PLAIN, 1), new FontExtrusion());
        Font3D normFont = new Font3D(new Font("TimesRoman", Font.PLAIN, 1), new FontExtrusion());

        //Title
        Text3D text = new Text3D(scaryFont, "Credits");
        Shape3D titleCredits = new Shape3D(text, ap);

        //Michael Perry
        Text3D MichaelPText = new Text3D(scaryFont, "Story and Project Lead: Michael Perry");
        Shape3D MichaelP = new Shape3D(MichaelPText, ap);

        //Marc Newmann
        Text3D MarcNtext = new Text3D(scaryFont, "Camera Development: Marc Newmann");
        Shape3D MarcN = new Shape3D(MarcNtext, ap);

        //Liam Curtis
        Text3D LiamCText = new Text3D(scaryFont, "Graphics and Modelling: Liam Curtis");
        Shape3D LiamC = new Shape3D(LiamCText, ap);

        //Noah Eastwood
        Text3D NoahEText = new Text3D(scaryFont, "Documentation and Lighting: Noah Eastwood");
        Shape3D noahE = new Shape3D(NoahEText, ap);

        //Transformations
        //Close Button
        Text3D closeOK = new Text3D(normFont, "OK");
        Shape3D closeText = new Shape3D(closeOK, ap);
        closeText.setName("Close OK");

        //Credits
        Transform3D tr = new Transform3D();
        tr.setScale(0.1);
        tr.setTranslation(new Vector3f(-0.20f, 0.3f, 0f));
        TransformGroup tg = new TransformGroup(tr);
        creditsMenu.addChild(tg);
        tg.addChild(titleCredits);

        //Michael Perry
        Transform3D tr3 = new Transform3D();
        tr3.setScale(0.07);
        tr3.setTranslation(new Vector3f(-0.75f, 0.1f, 0f));
        TransformGroup tg3 = new TransformGroup(tr3);
        creditsMenu.addChild(tg3);
        tg3.addChild(MichaelP);

        //Marc Transform
        Transform3D tr2 = new Transform3D();
        tr2.setScale(0.07);
        tr2.setTranslation(new Vector3f(-0.75f, 0f, 0f));
        TransformGroup tg2 = new TransformGroup(tr2);
        creditsMenu.addChild(tg2);
        tg2.addChild(MarcN);

        //Liam Transform
        Transform3D tr5 = new Transform3D();
        tr5.setScale(0.07);
        tr5.setTranslation(new Vector3f(-0.75f, -0.1f, 0f));
        TransformGroup tg5 = new TransformGroup(tr5);
        creditsMenu.addChild(tg5);
        tg5.addChild(LiamC);

        //Noah Transform
        Transform3D tr6 = new Transform3D();
        tr6.setScale(0.07);
        tr6.setTranslation(new Vector3f(-0.75f, -0.2f, 0f));
        TransformGroup tg6 = new TransformGroup(tr6);
        creditsMenu.addChild(tg6);
        tg6.addChild(noahE);

        //OK
        Transform3D tr7 = new Transform3D();
        tr7.setScale(0.07);
        tr7.setTranslation(new Vector3f(-0.10f, -0.4f, 0f));
        TransformGroup tg7 = new TransformGroup(tr7);
        creditsMenu.addChild(tg7);
        tg7.addChild(closeText);

        //Background
        BoundingSphere bounds = new BoundingSphere();
        Background background = new Background(1.0f, 1.0f, 1.0f);
        background.setColor(new Color3f(Color.BLACK));
        background.setApplicationBounds(bounds);
        creditsMenu.addChild(background);

        // Lighting
        AmbientLight alight = new AmbientLight(true, new Color3f(getLightR(), getLightB(), getLightG()));
        alight.setInfluencingBounds(bounds);
        creditsMenu.addChild(alight);
        PointLight light = new PointLight(new Color3f(Color.RED), new Point3f(1f, 1f, 1f),
                new Point3f(-0.5f, -1f, 0f));
        light.setInfluencingBounds(bounds);
        creditsMenu.addChild(light);
        return creditsMenu;
    }

}
