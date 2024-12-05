/*
 * AnnotationNode.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package Tools;

import java.io.*;


public class AnnotationNode implements Serializable, Cloneable
{
    private static final long serialVersionUID = 1;
    
    public String  text;        // The text that annotates part of a sound wave
    public long    soundOffset; // The offset to the sound wave sample
   

 public org.acorns.data.AnnotationNode convert(float version)
	{
    org.acorns.data.AnnotationNode node =
        new org.acorns.data.AnnotationNode(text, soundOffset);
    return node;
	}
    
}   // End of AnnotationNode class