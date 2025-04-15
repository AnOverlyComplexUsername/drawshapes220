package drawshapes;



import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;

/**
 * Interface for a shape to be drawn
 * 
 * Each method is documented.
 * 
 * @author jspacco
 *
 */
public interface IShape
{
    /**
     * Draw the shape using the given Graphics object
     * 
     * The Graphics object containing lots of methods for drawing.
     * 
     * @param g the Graphics object from the Java Swing GUI framework
     */
    public void draw(Graphics g);
    /**
     * Does this shape intersect any part of the other shape?
     * 
     * @param other
     * @return
     */
    public boolean intersects(IShape other);
    /**
     * Does this shape contain the given point?
     * 
     * @param point
     * @return
     */
    public boolean contains(Point point);
    /**
     * Return the color of this shape.
     * 
     * @return
     */
    public Color getColor();
    /**
     * Set the color of this shape to the given color.
     * 
     * @param color
     */
    public void setColor(Color color);
    /**
     * Is this shape selected?
     * 
     * Some operations apply to all selected shapes.
     * 
     * @return
     */
    public boolean isSelected();
    /**
     * Set the selected status of this shape to the given value.
     * @param b
     */
    public void setSelected(boolean b);
    
    /**
     * Return the anchor point of this shape.
     * @return
     */
    public Point getAnchorPoint();
    /**
     * Set the anchor point of this shape to the given point.
     * @param p
     */
    public void setAnchorPoint(Point p);
    
    /**
     * Returns the bounding box of this shape. The bounding box
     * is the max/min X and Y coordinates.
     * 
     * The bounding box is used to check for intersections.
     * 
     * @return
     */
    public BoundingBox getBoundingBox();

    public void move(int x, int y);
    public void scaleUp(double scale);
    public void scaleDown(double scale);
    public IShape copy();
}
