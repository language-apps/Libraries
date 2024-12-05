/*
 * AcornsProperties.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.properties;

import java.awt.*;
import java.beans.*;
import java.lang.reflect.Method;

/**
 *  Wrapper properties class to integrate with the ACORNS application
 */
public class AcornsProperties
{
   /** Minimum layer number */
   public final static int MIN_LAYERS = 1;
   /** Maximum layer number */
   public final static int MAX_LAYERS = 10;

   private static Object properties;
   
   /** Get the current Acorns property listener
    * 
    * @return AcornsProperties object, or null if not found.
    */
   public static AcornsProperties getAcornsProperties()
   {      PropertyChangeListener[] pcl = Toolkit.getDefaultToolkit()
                                 .getPropertyChangeListeners("Acorns");
          for (int i=0; i<pcl.length; i++)
          {   try
              {  
        	  	 properties = pcl[i];
        	  	 return new AcornsProperties();
              }
              catch (ClassCastException ex) {}
          }
          return null;
   }      // End of getAcornsProperties   
     
  /**  Forward the set file dirty request if the properties object supports it
   *   @return false if no file is open.
   */
   public boolean setFileDirty()
   {
	   if (properties==null) return true;
	   try
	   {
		   Class<?> className = properties.getClass();
		   Method method = className.getMethod("setFileDirty", new Class[] {});
		   method.invoke(properties);
		   return true;
	   }
	   catch (Throwable t)
	   {
		   return false;
	   }
	   
   } 

   
   /** Enable or disable menu buttons appropriately, if properties object supports it.
    */
   public void activateMenuItems()
   {
	   try
	   {
		   Class<?> className = properties.getClass();
		   Method method = className.getMethod("activateMenuItems", new Class[] {});
		   method.invoke(properties);
	   }
	   catch (Throwable t)
	   {
	   }
	      
   }

}
