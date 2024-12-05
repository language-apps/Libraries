/**
 * AnnotationNodeArray.java
 * @author HarveyD
 * @version 4.00 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.data;

import java.io.*;
import javax.swing.*;
import java.awt.*;

public class AnnotationNodeArray implements Serializable, Cloneable
{   
    private static final long serialVersionUID=2763054609667420999L;
   
    private AnnotationNode[] data;    // Table of annotation data
    private int annotations;          // Number of annotations stored

    /** threshold is a leeway for compares, also use to whether
     *  text should be left justified (if JUSTIFY set);
     */
    private int threshold;            // Leeway for compares

    private static final int JUSTIFY = 2<<30;

    /** Standard constructor to initialize the array of annotations  */
    public AnnotationNodeArray()
    {
        data = new AnnotationNode[10]; // Initial annotation array size
        annotations = 0;
        threshold   = 0;
    }
    
    /** Constructor for converting from ACORNS version 3.1
     * 
     * @param data The annotation data array.
     * @param annotations The number of annotations stored
     */
    public AnnotationNodeArray(AnnotationNode[] data, int annotations)
    {
        if (data==null) 
        {
           this.data = new AnnotationNode[10]; 
           annotations = 0;
           threshold   = 0;
        }
        else
        {   this.data        = data;
            this.annotations = annotations;
            for (int i=0; i<data.length; i++)
            {
                if (data[i]!=null && data[i].getText().equals(""))
                    data[i].setObject(null);
            }
            threshold = 0;
        }
    }
    
    /** Method to set a threshold for matching annotation offsets
     * 
     * @param threshold threshold value
     */
    public void setThreshold(int threshold) { this.threshold = threshold; }
    
    /** Method to set the text justification option
     * 
     * @param justify true if text is to be centered, false for left justify
     */
    public void setCentered(boolean justify)
    {
        if(justify)  threshold |=  JUSTIFY;
        else         threshold &= ~JUSTIFY;
    }

    /** Determine if the text justification is to be centered
     *
     * @return true if yes, false otherwise
     */
    public boolean isCentered()
    {
        return (threshold & JUSTIFY) != 0;
    }

    /** Method to insert a new annotation at the current layer
     *  @param object  annotation object associated with this offset
     *  @param point  selection for this annotation
     *  @return true if successful, false otherwise
     */
    public boolean insert(Object object, Point point)
    {   
        // Some validation.
        if (point.x<0)        return false;
        if (point.y<=point.x) return false;
        
        if (annotations==0) insert(null, 0, 0);
        int xPrime = binSearch(point.x,-1,annotations);
        int yPrime = binSearch(point.y, -1, annotations);
        
        // If same range, just replace the object.
        if (compare(point.x,xPrime) 
                          && compare(point.y,yPrime) && xPrime+1==yPrime)
        {  data[yPrime].setObject(object);
           return true;
        }
        
        // If x point not found, just perform an insert.
        if (xPrime<0)
        {
            if (!(comparePoints(point.x,point.y) || comparePoints(point.x,0)))
            {   insert(null, point.x, xPrime); }
            insert(object, point.y, yPrime);
            return true;
        }

        boolean sameY = compare(point.y, yPrime);
        boolean sameX = compare(point.x, xPrime);
       
        if (comparePoints(point.x, point.y))
        {  if (!sameY) 
           {  insert(object, point.y, yPrime);
              return true;    
           }
           return false;
        }
        
        int entries = yPrime - xPrime;
        if (yPrime < 0) entries = annotations - xPrime;
        if (compare(point.x, xPrime)) entries--;
        
        Object xData = data[xPrime].getObject();
        remove(xPrime, entries);

        yPrime = binSearch(point.y, -1, annotations);
        if (!sameY) insert(object, point.y, yPrime);
        else        modify(object, new Point(point.y, point.y));
        if (!sameX) insert(xData, point.x, xPrime);
        return true;

    }   // End of insert()

    
    /*  Method to modify an annotation
     *  @param object   annotation object attached to sound wave
     *  @param point  selection for this annotation
     *  @return true if modify succeeded, false otherwise
     */
    public boolean modify(Object object, Point point)
    {
        if (point.x != point.y) return false;
        if (point.x <=0) return false;
        if (annotations == 0) return false;
        
        int spot = binSearch(point.y, -1, annotations);
        if (spot < 0) return false;
        
        data[spot].setObject(object);
        return true;
    }

    /** Method to find the object contained in a selected point
     *
     * @param point The point containing the object
     * @return The object or null if not found
     */
    public Object findObject(Point point)
    {
        if (point.x != point.y) return null;
        if (point.x <=0) return null;
        if (annotations == 0) return null;

        int spot = binSearch(point.y, -1, annotations);
        if (spot < 0) return null;

        return  data[spot].getObject();
    }
    
    /** Method to find and retrieve an annotation object
     * 
     * @param point Location of the object in the audio signal
     * @return The annotation object
     */
    public Object getObject(Point point)
    {
        if (annotations == 0) return null;
        if (point.x < 0) point.x = 0;
        if (point.y<point.x) return null;
       
        int first = binSearch(point.x, -1, annotations);
        if (first<0) return null;
        if (first==0) first = 1;
        
        int last  = binSearch(point.y, -1, annotations);
        if (last<0) last = annotations-1;
        
        Object nodeObject = null; 
        for (int i=first; i<=last; i++)
        {
            nodeObject = data[i].getObject();
            if (nodeObject!=null) return nodeObject;            
        }
        return null;
    }
    
    /**
     * 
     * @param object the object to find an delete
     * @return true if successful, false otherwise
     */
    public boolean deleteObject(Object object)
    {
        Object nodeObject = null;
        boolean prev = false, next = false;
        for (int i=1; i<=annotations-1; i++)
        {
            nodeObject = data[i].getObject();
            if (nodeObject != null && nodeObject==object)
            {
                next = i<annotations-1 && data[i+1].getObject()==null;
                prev = i>1 && data[i-1].getObject()==null;
                if (prev && next) remove(i-1, 2);
                else
                {   if (prev)
                    {
                        data[i].setObject(null);
                        remove(i-1, 1);
                    }
                    else remove(i, 1);
                }
                return true;
            }
        }
        return false;
    }
    
    /** Method to shift annotations based for a copy operation
     *  @param point  selection for this annotation
     *  @param direction true if shift right, false otherwise
     */
    public boolean shift(Point point, boolean direction)
    { 
        // Some validation.
        if (point.y<point.x) return false;
        if (point.x<0) point.x = 0;
        
        // Set the applicable layers.
        long offset, shift = point.y-point.x, newOffset;
        for (int j=1; j<annotations; j++)
        {   newOffset = offset = data[j].getOffset();
            if (direction && offset>=point.x)
            {
                newOffset = offset+shift;
            }
            if (!direction && offset>=point.y)
            {
                newOffset = offset - shift;
                if (newOffset<point.x) newOffset = point.x;
            }
            data[j].setOffset(newOffset);
        }
        return true;        
    }
    
    /** Method to remove an annotation
     *  @param point  selection for this annotation
     *  @return true if deletion succeeded, false otherwise
     */
    public synchronized boolean delete(Point point)
    {   
        if (annotations == 0) return false;
        if (point.x < 0) point.x = 0;
        if (point.y<=point.x) return false;
        
        // Remove range of annotations on all appropriate layers.
        int xPrime = binSearch(point.x,-1,annotations);
        if (xPrime<0) return false;
        
        int yPrime = annotations -1;
        if (point.y > 0)
        {
           yPrime = binSearch(point.y, -1, annotations);
           if (yPrime<0) yPrime = annotations - 1; 
        }
    
        if (data[yPrime].getObject()!=null)
        {   if (xPrime!=yPrime)
            { 
        	  if (yPrime!=1) data[yPrime-1].setOffset(point.y); 
              if (data[yPrime].getOffset()>point.y) yPrime-=1;
              if (data[yPrime].getOffset()>point.y) yPrime-=1;
            }
            else if (xPrime!=0) data[xPrime].setOffset(point.y);
        }
        else yPrime--;
        
        if (data[xPrime].getObject()!=null && data[xPrime].getOffset()>0)
        {  
           if (xPrime!=0) data[xPrime].setOffset(point.x); 
           xPrime++; 
        }
        else if (xPrime==0) xPrime++;

        
        int entries = yPrime - xPrime + 1;
        remove(xPrime, entries); 
        return true;
    }

    /** Method to remove a single annotation
     *  @param offset  audio frame offset for this annotation
     *  @return true if deletion succeeded, false otherwise
     */
    public synchronized boolean delete(int offset)
    {   
        if (annotations == 0) return false;
        if (offset < 0) offset = 0;
        int location = binSearch(offset,-1,annotations);
        if (location<0) return false;
        remove(location, 1); 
        return true;
    }
    
    /** Method to return the annotationData for the active layer
     *  @return an array of annotations for this layer, or null if none
     */
    public AnnotationNode[] getAnnotationNodes()  { return data; }
    
    /** Method to return the annotationData  for export
     * 
     *  @return an array of annotations for this level, or null if none
     */
    public AnnotationNode[] getScaledAnnotationNodes(float ratio)  
    {
        AnnotationNode[] newNodes = new AnnotationNode[data.length]; 
        long offset;
        Object object;
        for (int node = 0; node<data.length; node++)
        {  
            if (data[node]!=null) 
            {   
                offset = (long)(data[node].getOffset() * ratio);
                object = data[node].getObject();
                newNodes[node] = new AnnotationNode(object, offset);
            }
        }
        return newNodes; 
    }
 
    /** Method to return the count of annotations at the active layer
     *  @return number of annotations
     */
    public int getAnnotationSize()  { return annotations; }
       
    /***************************  Private Methods *****************************/
    
    /** Method to insert an entry into the Annotation array
     *  @param object data to insert
     *  @param x    offset of annotation into sound wave
     *  @param offset place in annotation array to insert (-1 to append).
     */
    private void insert(Object object, int x, int offset)
    {
        expand();
        int count = annotations;
        if (offset==-1) offset = count;
        
        for (int i=count; i>offset; i--)
        {   data[i] = data[i-1]; }
          
        data[offset] = new AnnotationNode(object, x);
        annotations++;
    }   // End of insert()
    
    /** Remove entries between two values
     *  @param index before the first entry to remove
     *  @param entries = count of entries to remove
     */
    private void remove(int start, int entries)
    {
        int count = annotations;
        if (entries>0)
        {
            for (int i=start; i< count-entries; i++)
            {  data[i] = data[i+entries];  }
            annotations -= entries;
            
            // Clear out the back part of the array
            for (int i=annotations; i<data.length; i++)
                data[i] = null;
        }
    }       // End of remove()
    
    /* Method to compare value at an index to that of argument.
     * Return true if two values are within a threshold, false otherwise
     */
    private boolean compare(long value, int offset)
    {  if (offset<0) return false;
       
       AnnotationNode node = data[offset];
       return comparePoints(value, node.getOffset());
    }

    /* Method to compare two points within a sound signal
     * Return true if the values are within a threshold, false otherwise
     */
    private boolean comparePoints(long first, long second)
    {
       if (first==0 && second!=0) return false;
       if (second==0 && first!=0) return false;
       
       long difference = second - first; 
       if (difference<0) difference = - difference;
       
       if (difference <= (threshold&~JUSTIFY)) return true;
       return false;        
    }
    
    /* Method to expand the annotation array if necessary */
    private void expand()
    {  if (data == null)
       {   data = new AnnotationNode[10];
           return;
       }
       
       if (data.length == annotations)
       {  AnnotationNode[] newData = new AnnotationNode[2*data.length];
          for (int i=0; i<data.length; i++)
          { newData[i] = data[i].clone();
          }
          data = newData;
       }        
       
    }  // End of expand()
       
    /** Method to binary search the data for the place to insert 
     * 
     * @param soundOffset Offset to insert
     * @param startSpot place in array to begin searching
     * @param endSpot place in array to end searching
     * @return index to sound offset
     */
    private int binSearch(long soundOffset, int startSpot, int endSpot)
    {
        int middle = (startSpot + endSpot)/2;
        if (startSpot == endSpot - 1)  
        {   if (endSpot >= annotations) return -1;
            return endSpot;
        }
        long thisOffset = data[middle].getOffset();
        if (comparePoints(soundOffset, thisOffset)) return middle;
        if (soundOffset < thisOffset)  
              return binSearch(soundOffset, startSpot, middle);
        else  return binSearch(soundOffset, middle, endSpot);
    }  // End of binSearch()
   
   // Make an identical copy of this object.
   public @Override AnnotationNodeArray clone()
   {   AnnotationNodeArray newData = null;
       
       try
       {   newData = (AnnotationNodeArray)super.clone();
           int size = data.length;
           newData.data =  new AnnotationNode[size];
           
           if (data!=null)
           {   for (int i=0; i<size; i++)
               {  if (data[i]!=null) { newData.data[i] = data[i].clone(); } }
           }   // End if.
       }
       catch (Exception e)   
       { JOptionPane.showMessageDialog
                 (null, "Couldn't clone AnnotationNodeArray"); }
       return newData;
   }   
    
}   // End of AnnotationNodeArray class
