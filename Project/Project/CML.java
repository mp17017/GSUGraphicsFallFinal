package Project;

import javax.swing.*;
import java.awt.event.*;

public class CML implements MouseListener, MouseMotionListener, KeyListener {
    private int positionX = 0, positionY = 0;
    private int mouseClickX, mouseClickY;
    private int button;

    @Override
    public void mousePressed(MouseEvent evt) {

        if(SwingUtilities.isLeftMouseButton(evt)) {

            mouseClickX = evt.getX();
            mouseClickY = evt.getY();
        }
    }
    @Override
    public void mouseDragged(MouseEvent evt) {
        if(SwingUtilities.isLeftMouseButton(evt)) {
            positionX = mouseClickX - evt.getX();
            positionY = mouseClickY - evt.getY();
        }
    }
    @Override
    public void mouseMoved(MouseEvent arg0) {}
    @Override
    public void mouseClicked(MouseEvent arg0) { }
    @Override
    public void mouseEntered(MouseEvent arg0) { }
    @Override
    public void mouseExited(MouseEvent arg0) {}

    @Override
    public void mouseReleased(MouseEvent arg0) {}
    public int getX(){
        return positionX;
    }
    public int getY(){
        return positionY;
    }

    @Override
    public void keyTyped(KeyEvent e) {
            }

    @Override
    public void keyPressed(KeyEvent e) {
        button = e.getKeyCode();
        //System.out.println(button);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (button == -1) button = 0;
    }
    public int getB() { return button; }
    public void setB(int button) { this.button = button; }
}
