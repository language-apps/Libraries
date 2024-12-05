/*
 * Lesson.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package Tools;

import java.io.*;


/** Abstract class extended by all lesson types that integrate into the
 *  ACORNS software.
 */
public abstract class Lesson implements Serializable
{
   private static final long serialVersionUID = 1;	
	
   public   String      name;              // Name for appearing on menus.
   public   int         layer;             // Layer used for this layer.
  
    /** Create a new lesson
    *
    * @param name The descriptive name of this lesson. This name displays on
    * the drop down menu when a user creates, or modifies, lesson header data.
    */
   public Lesson(String name)
   {   this.name = name;
       layer     = 1; 
   }  // End of constructor.
  
   /** Abstract method to convert between versions 
    * 
    * @param version the version to convert from
    * @return true if successful, false otherwise
    */
   public abstract org.acorns.lesson.Lesson convert(float version) throws IOException;
   
}  // End of Lesson class.	
