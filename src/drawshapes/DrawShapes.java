package drawshapes;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.LinkedList;
import java.util.List;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

@SuppressWarnings("serial")
public class DrawShapes extends JFrame
{
    public enum ShapeType {
        SQUARE,
        CIRCLE,
        RECTANGLE
    }
    
    private DrawShapesPanel shapePanel;
    private Scene scene;
    private ShapeType shapeType = ShapeType.SQUARE;
    private Color color = Color.RED;
    private Point startDrag;
    private int distance = 20;
    private double scaleUpFactor = 1.5;
    private double scaleDownFactor = 0.5;
    private LinkedList<Scene> undoStack = new LinkedList<>();
    private int undoStackIndex =  0; // current scene in the undo stack; allows for undo/redo
    private Scene cachedCurScene; // used for redoing an undo

    public DrawShapes(int width, int height)
    {
        setTitle("Draw Shapes!");
        scene=new Scene();
        undoStack.push(scene);

        
        // create our canvas, add to this frame's content pane
        shapePanel = new DrawShapesPanel(width,height,scene);
        this.getContentPane().add(shapePanel, BorderLayout.CENTER);
        this.setResizable(false);
        this.pack();
        this.setLocation(100,100);
        
        // Add key and mouse listeners to our canvas
        initializeMouseListener();
        initializeKeyListener();
        
        // initialize the menu options
        initializeMenu();

        // Handle closing the window.
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }


    // undo/redo functions
    /// stores scene for redo/undo operation
    private void push()
    {
        if(undoStackIndex != 0)
        {
            while(undoStackIndex > 0){
                undoStack.removeFirst();
                undoStackIndex--;
            }
        }
            undoStack.push(scene.copy());
    }

    /// caches the current scene as a reference for redo-ing an action
    private void cacheScene()
    {
        cachedCurScene = scene.copy();
    }

    private void undo()
    {
        if(undoStackIndex < undoStack.size() -1 ){
            scene.update(undoStack.get(undoStackIndex));
            undoStackIndex++;
        }
        repaint();
    }

    private void redo()
    {
        if(undoStackIndex > 0){
            undoStackIndex--;
            if (undoStackIndex == 0)
                scene.update(cachedCurScene);
             else 
                scene.update(undoStack.get(undoStackIndex-1));
        }
        repaint();
    }

    private void initializeMouseListener()
    {
        //right click menu
        JPopupMenu rClickMenu = new JPopupMenu();
        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.addActionListener((ActionEvent e) -> {
            System.out.println(e.getActionCommand());
            undo();
        });

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.addActionListener((ActionEvent e) -> {
            System.out.println(e.getActionCommand());
            redo();
        });
        rClickMenu.add(undoItem);
        rClickMenu.add(redoItem);
        
        JMenuItem recolorItem=new JMenuItem("Recolor");
        rClickMenu.add(recolorItem);
        recolorItem.addActionListener((ActionEvent e) -> {
            push();
            String text=e.getActionCommand();
            System.out.println(text);
            scene.recolorSelectedShapes(color);
            repaint();
            cacheScene();
        });

        JMenuItem deselectItem = new JMenuItem("Deselect");
        rClickMenu.add(deselectItem);
        deselectItem.addActionListener((ActionEvent e) -> {
            String text=e.getActionCommand();
            System.out.println(text);
            scene.deselectAll();
            repaint();
        });

        JMenuItem deleteItem = new JMenuItem("Delete");
        rClickMenu.add(deleteItem);
        deleteItem.addActionListener((ActionEvent e) -> {
            String text=e.getActionCommand();
            push();
            System.out.println(text);
            scene.deleteAllSelected();
            repaint();
            cacheScene();
        });





        MouseAdapter a = new MouseAdapter() { //anon class; implements abstract class 
            
            public void mouseClicked(MouseEvent e)
            {
                System.out.printf("Mouse cliked at (%d, %d)\n", e.getX(), e.getY());
                // handles placing shapes when left clicked
                if (e.getButton()==MouseEvent.BUTTON1) { 
                    if (shapeType == ShapeType.SQUARE) {
                        push();
                        scene.addShape(new Square(color, 
                                e.getX(), 
                                e.getY(),
                                100));
                        cacheScene();
                    } else if (shapeType == ShapeType.CIRCLE){
                        push();
                        scene.addShape(new Circle(color,
                                e.getPoint(),
                                100));
                        cacheScene();
                    } else if (shapeType == ShapeType.RECTANGLE) {
                        push();
                        scene.addShape(new Rectangle(
                                e.getPoint(),
                                100, 
                                200,
                                color));
                        cacheScene();
                    }
                    
                } else if (e.getButton()==MouseEvent.BUTTON2) {
                    // apparently this is middle click
                    Point p = e.getPoint();
                    rClickMenu.show(rootPane, p.x, p.y + 20);
                } else if (e.getButton()==MouseEvent.BUTTON3){
                    // right right-click
                    Point p = e.getPoint();
                    System.out.printf("Right click is (%d, %d)\n", p.x, p.y);
                    rClickMenu.show(rootPane, p.x, p.y + 20);


                    List<IShape> selected = scene.select(p);
                    if (selected.size() > 0){
                        for (IShape s : selected){
                            s.setSelected(true);
                        }
                    } else {
                        for (IShape s : scene){
                            s.setSelected(false);
                        }
                    }
                    System.out.printf("Select %d shapes\n", selected.size());
                }
                repaint();
            }
            
            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
             */
            public void mousePressed(MouseEvent e) 
            {
                System.out.printf("mouse pressed at (%d, %d)\n", e.getX(), e.getY());
                scene.startDrag(e.getPoint());
                
            }

            /* (non-Javadoc)
             * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
             */
            public void mouseReleased(MouseEvent e)
            {
                System.out.printf("mouse released at (%d, %d)\n", e.getX(), e.getY());
                scene.stopDrag();
                repaint();
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                System.out.printf("mouse drag! (%d, %d)\n", e.getX(), e.getY());
                scene.updateSelectRect(e.getPoint());
                repaint();
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // TODO use this to grow/shrink shapes
            }
            
        };
        shapePanel.addMouseMotionListener(a);
        shapePanel.addMouseListener(a);
    }
    
    /**
     * Initialize the menu options
     */
    private void initializeMenu()
    {

        // menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // file menu
        JMenu fileMenu=new JMenu("File");
        menuBar.add(fileMenu);
        // load
        JMenuItem loadItem = new JMenuItem("Load");
        fileMenu.add(loadItem);
        loadItem.addActionListener(new ActionListener() {
            @Override
            @SuppressWarnings("UseSpecificCatch")
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");

                int returnValue = jfc.showOpenDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("load from " +selectedFile.getAbsolutePath());
                    try
                    {
                        push();
                        scene.loadFromFile(selectedFile);
                        cacheScene();
                        repaint();
                    } catch (IOException ex) //lazy exception
                    {
                        JOptionPane.showMessageDialog(null, "ERROR: File Not Found");
                    } catch (InputMismatchException ex)
                    {
                        JOptionPane.showMessageDialog(null, "ERROR: File Corrupted");

                    }
                    catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(null, ex);
                    }
                    
                }
            }
        });
        // save
        JMenuItem saveItem = new JMenuItem("Save");
        fileMenu.add(saveItem);
        saveItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println(e.getActionCommand());
                JFileChooser jfc = new JFileChooser(".");

                // int returnValue = jfc.showOpenDialog(null);
                int returnValue = jfc.showSaveDialog(null);

                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = jfc.getSelectedFile();
                    System.out.println("save to " +selectedFile.getAbsolutePath());

                    try (PrintWriter out  = new PrintWriter(selectedFile))
                    {
                        out.write(scene.toString());
                    }  catch (Exception ex)
                    {
                        JOptionPane.showMessageDialog(null, "ERROR: " + ex);
                    }

                   
                    
                }
            }
        });
        fileMenu.addSeparator();
        // edit
        JMenuItem itemExit = new JMenuItem ("Exit");
        fileMenu.add(itemExit);
        itemExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                System.exit(0);
            }
        });

        // color menu
        JMenu colorMenu = new JMenu("Color");
        menuBar.add(colorMenu);

        // red color
        JMenuItem redColorItem= new JMenuItem ("Red");
        colorMenu.add(redColorItem);
        redColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to red
                color = Color.RED;
            }
        });
        
        
        // blue color
        JMenuItem blueColorItem = new JMenuItem ("Blue");
        colorMenu.add(blueColorItem);
        blueColorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                // change the color instance variable to blue
                color = Color.BLUE;
            }
        });

         // yellow color
         JMenuItem yellowColorItem = new JMenuItem ("Yellow");
         colorMenu.add(yellowColorItem);
         yellowColorItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String text=e.getActionCommand();
                 System.out.println(text);
                 // change the color instance variable to blue
                 color = Color.YELLOW;
             }
         });

         // green color
         JMenuItem greenColorItem = new JMenuItem ("Green");
         colorMenu.add(greenColorItem);
         greenColorItem.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 String text=e.getActionCommand();
                 System.out.println(text);
                 // change the color instance variable to blue
                 color = Color.GREEN;
             }
         });
        
        // shape menu
        JMenu shapeMenu = new JMenu("Shape");
        menuBar.add(shapeMenu);
        
        // square
        JMenuItem squareItem = new JMenuItem("Square");
        shapeMenu.add(squareItem);
        squareItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Square");
                shapeType = ShapeType.SQUARE;
            }
        });
        
        // circle
        JMenuItem circleItem = new JMenuItem("Circle");
        shapeMenu.add(circleItem);
        circleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Circle");
                shapeType = ShapeType.CIRCLE;
            }
        });
        
        // rectangle
        JMenuItem rectangleItem = new JMenuItem("Rectangle");
        shapeMenu.add(rectangleItem);
        rectangleItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("Rectangle");
                shapeType = ShapeType.RECTANGLE;
            }
        });
        
        // operation mode menu
        JMenu operationModeMenu=new JMenu("Operation");
        menuBar.add(operationModeMenu);
        
        // draw option
        JMenuItem drawItem=new JMenuItem("Resize");
        operationModeMenu.add(drawItem);
        drawItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
            }
        });
        
        // select option
        JMenuItem selectItem=new JMenuItem("Move");
        operationModeMenu.add(selectItem);
        selectItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
            }
        });
        
        // recolor option
        JMenuItem recolorItem=new JMenuItem("Recolor");
        operationModeMenu.add(recolorItem);
        recolorItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String text=e.getActionCommand();
                System.out.println(text);
                push();
                scene.recolorSelectedShapes(color);
                repaint();
                cacheScene();
            }
        });

        // delete option
        JMenuItem deleteItem = new JMenuItem("Delete");
        operationModeMenu.add(deleteItem);
        deleteItem.addActionListener((ActionEvent e) -> {
            String text=e.getActionCommand();
            push();
            System.out.println(text);
            scene.deleteAllSelected();
            repaint();
            cacheScene();
        });

        // set the menu bar for this frame
        this.setJMenuBar(menuBar);
    }
    
    /**
     * Initialize the keyboard listener.
     */
    private void initializeKeyListener()
    {
        shapePanel.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                System.out.println("key typed: " +e.getKeyChar());
            }
            public void keyReleased(KeyEvent e){
                // TODO: implement this method if you need it
            }
            public void keyTyped(KeyEvent e) {
                char k = e.getKeyChar();
                // TODO: implement this method if you need it
        
                if(k == 'w') {
                    push(); 
                    scene.MoveSelected(0,distance); 
                    cacheScene();
                }
                if(k == 's') 
                {
                    push();
                    scene.MoveSelected(0, -distance);
                    cacheScene();
                }
                if(k == 'a') {
                    push();
                    scene.MoveSelected(-distance, 0); 
                    cacheScene();
                }
                if(k == 'd') {
                    push();
                    scene.MoveSelected(distance, 0);
                    cacheScene();
                }

                if(k == 'p') {
                    push();
                    for (IShape shapes: scene) if (shapes.isSelected()) 
                    {
                        shapes.scaleUp(scaleUpFactor);
                    }
                    cacheScene();
                }
                if(k == 'l') {
                    push();
                    for (IShape shapes: scene) if (shapes.isSelected()) 
                    {
                        shapes.scaleDown(scaleDownFactor);
                    }
                    cacheScene();
                }
                
                if(k == 'z') //undo
                    undo();

                if(k == 'y') //redo
                    redo();
                repaint();
            }
        });
    }
    
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        DrawShapes shapes=new DrawShapes(700, 600);
        shapes.setVisible(true);
    }

}
