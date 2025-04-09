package drawshapes;

import java.awt.Color;
import java.awt.Point;

/**
 * Abstract shape class.
 * 
 * Lots of the data and methods for a shape are in here
 * and can be inherited by subclass shapes.
 * 
 * 
 * 
 */
public abstract class AbstractShape implements IShape
{
    protected BoundingBox boundingBox;
    protected boolean selected;
    protected Color color;
    protected Point anchorPoint;
    
    protected AbstractShape(Point anchor) {
        this.anchorPoint = anchor;
    }
    
    protected void setBoundingBox(int left, int right, int top, int bottom) {
        this.boundingBox = new BoundingBox(left, right, top, bottom);
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#intersects(drawshapes.sol.Shape)
     */
    @Override
    public boolean intersects(IShape other) {
        if (this == other || other == null){
            return false;
        }
        return this.boundingBox.intersects(other.getBoundingBox());
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#contains(java.awt.Point)
     */
    @Override
    public boolean contains(Point point) {
        return this.boundingBox.contains(point);
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#getBoundingBox()
     */
    @Override
    public BoundingBox getBoundingBox() {
        return this.boundingBox;
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#getColor()
     */
    @Override
    public Color getColor() {
        return this.color;
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#setColor(java.awt.Color)
     */
    @Override
    public void setColor(Color color) {
        this.color = color;
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#isSelected()
     */
    @Override
    public boolean isSelected() {
        return selected;
    }

    /* (non-Javadoc)
     * @see drawshapes.sol.Shape#setSelected(boolean)
     */
    @Override
    public void setSelected(boolean b) {
        this.selected = b;
    }
    
    @Override
    public Point getAnchorPoint() {
        return this.anchorPoint;
    }
    
    static String colorToString(Color color) {
        if (color == Color.RED) {
            return "RED";
        } else if (color == Color.BLUE) {
            return "BLUE";
        }
        throw new UnsupportedOperationException("Unexpected color: "+color);
    }

    @Override
    public void move(int x, int y)
    {
        anchorPoint.translate(x, y);
        boundingBox.move(x, y);

    }

    public void scaleDown()
    {
        throw new UnsupportedOperationException("not yet implemnted"); 
    }

    public void scaleUp()
    {
        throw new UnsupportedOperationException("not yet implemnted");

    }
}
