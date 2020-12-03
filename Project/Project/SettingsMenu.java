//Settings Menu
package Project;


//Imports
import com.sun.j3d.utils.picking.*;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.geometry.*;
import javax.media.j3d.*;
import javax.vecmath.*;
import java.awt.event.*;
import java.awt.*;


public class SettingsMenu extends StartMenu implements MouseListener {
   
    //Attributes
    private final PickCanvas pickCanvas;

    
    //Constructor
    public SettingsMenu(){

        //Window Setup

        BranchGroup group = new BranchGroup();
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
        Shape3D veryBright = new Shape3D(textVeryBright,ap);
        veryBright.setName("Very Bright");
        Text3D textBright = new Text3D(scaryFont,"Bright");
        Shape3D Bright = new Shape3D(textBright,ap);
        Bright.setName("Bright");
        Text3D textDark = new Text3D(scaryFont,"Dark");
        Shape3D Dark = new Shape3D(textDark,ap);
        Dark.setName("Dark");
        Text3D textVeryDark = new Text3D(scaryFont," Very Dark");
        Shape3D veryDark = new Shape3D(textVeryDark,ap);
        veryDark.setName("Very Dark");
        
        //Volume Controls
        Text3D titleVol = new Text3D(scaryFont, "Volume:");
        Shape3D volume = new Shape3D(titleVol, ap);
        volume.setName("Volume");
        
        //Window Size Controls
        Text3D titleWinSize = new Text3D(scaryFont, "FullScreen Mode");
        Shape3D windowSize = new Shape3D(titleWinSize, ap);
        windowSize.setName("Window Size");
        //Transformations
        
        //Close Button
        Text3D closeOK = new Text3D(normFont,"OK");
        Shape3D closeText = new Shape3D(closeOK,ap);
        closeText.setName("Close OK");
  
        
        
        //Settings
        Transform3D tr = new Transform3D();
        tr.setScale(0.1);
        tr.setTranslation(new Vector3f(-0.20f, 0.3f, 0f));
        TransformGroup tg = new TransformGroup(tr);
        group.addChild(tg);
        tg.addChild(titleSettings);
        //Brightness
        Transform3D tr3 = new Transform3D();
        tr3.setScale(0.07);
        tr3.setTranslation(new Vector3f(-0.75f, 0.2f,0f));
        TransformGroup tg3 = new TransformGroup(tr3);
        group.addChild(tg3);
        tg3.addChild(brightness);
        //Brightness: Very Dark
        Transform3D trvd = new Transform3D();
        trvd.setScale(0.05);
        trvd.setTranslation(new Vector3f(-0.75f, 0.1f,0f));
        TransformGroup tgvd = new TransformGroup(trvd);
        group.addChild(tgvd);
        tgvd.addChild(veryDark);
        //Brightness: Dark
        Transform3D trd = new Transform3D();
        trd.setScale(0.05);
        trd.setTranslation(new Vector3f(-0.35f, 0.1f,0f));
        TransformGroup tgd = new TransformGroup(trd);
        group.addChild(tgd);
        tgd.addChild(Dark);
        //Brightness: Bright
        Transform3D trb = new Transform3D();
        trb.setScale(0.05);
        trb.setTranslation(new Vector3f(-0.1f, 0.1f,0f));
        TransformGroup tgb = new TransformGroup(trb);
        group.addChild(tgb);
        tgb.addChild(Bright);
        //Brightness: Very Bright
        Transform3D trvb = new Transform3D();
        trvb.setScale(0.05);
        trvb.setTranslation(new Vector3f(0.2f, 0.1f,0f));
        TransformGroup tgvb = new TransformGroup(trvb);
        group.addChild(tgvb);
        tgvb.addChild(veryBright);
            
        
        //Volume
        Transform3D tr4 = new Transform3D();
        tr4.setScale(0.07);
        tr4.setTranslation(new Vector3f(-0.75f, -0f,0f));
        TransformGroup tg4 = new TransformGroup(tr4);
        group.addChild(tg4);
        tg4.addChild(volume);
        //Window Size
        Transform3D tr5 = new Transform3D();
        tr5.setScale(0.07);
        tr5.setTranslation(new Vector3f(-0.75f, -0.1f,0f));
        TransformGroup tg5 = new TransformGroup(tr5);
        group.addChild(tg5);
        tg5.addChild(windowSize);
        
        //OK
        Transform3D tr6 = new Transform3D();
        tr6.setScale(0.07);
        tr6.setTranslation(new Vector3f(-0.10f, -0.3f, 0f));
        TransformGroup tg6 = new TransformGroup(tr6);
        group.addChild(tg6);
        tg6.addChild(closeText);
     
        

        //Background
        BoundingSphere bounds = new BoundingSphere();
        Background background = new Background(1.0f, 1.0f, 1.0f);
        background.setColor(new Color3f(Color.BLACK));
        background.setApplicationBounds(bounds);
        group.addChild(background);

        // Lighting
        AmbientLight alight = new AmbientLight(true, new Color3f(getLightR(),getLightB(),getLightG()));
        alight.setInfluencingBounds(bounds);
        group.addChild(alight); 
        PointLight light = new PointLight(new Color3f(Color.RED), new Point3f(1f,1f,1f),
        new Point3f(-0.5f,-1f,0f));
        light.setInfluencingBounds(bounds);
        group.addChild(light);

        universe.getViewingPlatform().setNominalViewingTransform();
        universe.addBranchGraph(group);
        frame.addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent winEvent) {
                System.exit(0);

           }
        });
        frame.add(canvas);
        pickCanvas = new PickCanvas(canvas, group);
        pickCanvas.setMode(PickCanvas.BOUNDS);
        canvas.addMouseListener((MouseListener) this);
        frame.pack();
        frame.show();
    }


    @Override
    public void mouseClicked(MouseEvent e){
        pickCanvas.setShapeLocation(e);
        PickResult result = pickCanvas.pickClosest();
        if (result == null) {
           System.out.println("Nothing picked");
        } else {
           Primitive p = (Primitive)result.getNode(PickResult.PRIMITIVE);
           Shape3D s = (Shape3D)result.getNode(PickResult.SHAPE3D);
           if (p != null) {
              System.out.println(p.getName());
           } else if (s != null) {
               if(s.getName().equals("Very Dark")){
                   System.out.println("Very Dark Method Here");
                   this.setLightR(0);
                   this.setLightB(0);
                   this.setLightG(0);
               }
               if(s.getName().equals("Dark")){
                   System.out.println("Dark Method Here");
                    this.setLightR(85);
                    this.setLightB(85);
                    this.setLightG(85);
               }
               if(s.getName().equals("Bright")){
                    System.out.println("Bright Method Here");
                    this.setLightR(170);
                    this.setLightB(170);
                    this.setLightG(170);
               }
               if(s.getName().equals("Very Bright")){
                    System.out.println("Very Bright Method Here");
                    this.setLightR(255);
                    this.setLightB(255);
                    this.setLightG(255);
                    
               }
               if(s.getName().equals("Close OK")){
                    frame.dispose();
                    new StartMenu();
               }
           } else{
              System.out.println("null");
           }

        }

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
} 
