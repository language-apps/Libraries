/**
 * AudioDropTarget.java
 * @author HarveyD
 * @version 6.00
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.audio;

import java.lang.reflect.*;
import javax.swing.*;
import java.awt.dnd.*;
import java.awt.datatransfer.*;

import java.io.*;
import java.util.*;

public class AudioDropTarget implements DropTargetListener
{
   private Method audioDroppedMethod;
   private Object dropObject;


   public AudioDropTarget(JPanel panel, Object dropObject)
   {  
	  new DropTarget(panel, DnDConstants.ACTION_COPY_OR_MOVE, this, true);
   
      this.dropObject = dropObject;
      Class<?> dropClass = dropObject.getClass();

      try
      {  
    	  audioDroppedMethod = dropClass.getMethod
                  ("audioDropped", new Class[]{File.class} );
      }
      catch (NoSuchMethodException ex) {}
   }

   /** Method to process drops into the panel
    *
    * @param dtde The event triggering this method to execute
    */
   public void drop(DropTargetDropEvent dtde)
    {
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        File[] files = getTransferObjects(dtde.getTransferable());
        if (files == null || files.length==0)
        { java.awt.Toolkit.getDefaultToolkit().beep();
          dtde.dropComplete(false);  
          return;
        }

        String arg = files[0].getPath();
        String extension = arg.substring(arg.lastIndexOf(".")+1);
        if (isAudio(extension))
        {  try  {  
        	   audioDroppedMethod.invoke(dropObject, files[0]); }
           catch (Exception ex)   {  dtde.dropComplete(false);  }
        }
        else { java.awt.Toolkit.getDefaultToolkit().beep();
               dtde.dropComplete(false);
               return;
             }
        dtde.dropComplete(true);
    }

    /** Method to determine if drags to this object are okay
     *
     * @param dtde The triggering event
     */
    public void dragEnter (DropTargetDragEvent dtde)
    {
    }

    public void dragExit (DropTargetEvent dte) 
    {
    }

    public void dragOver (DropTargetDragEvent dtde)
    {  
    }

    public void dropActionChanged (DropTargetDragEvent dtde)
    {   
    }

    /** Method to get the transferable list of files
     *
     * @param transfer The transferable object
     * @return An array of file objects or (null if none)
     */
    private File[] getTransferObjects(Transferable transfer)
    {
        DataFlavor[] flavors = transfer.getTransferDataFlavors();
        File[] file = new File[1];

        DataFlavor listFlavor = null;
        AbstractList<?> list = null;

        for (int i=0; i<flavors.length; i++)
        {  if (flavors[i].getRepresentationClass() == List.class)
                listFlavor = flavors[i];
           if (flavors[i].getRepresentationClass() == AbstractList.class)
                listFlavor = flavors[i];
        }

        try
        {  if (listFlavor!=null)
           {
               list = (AbstractList<?>)transfer.getTransferData(listFlavor);

               int size = list.size();
               file = new File[size];
               String extension;

               for (int i=0; i<size; i++)
               {
                  file[i] = (File)list.get(i);
                  extension = file[i].getName();
                  extension = extension.substring(extension.lastIndexOf(".")+1);

                 if (!isAudio(extension)) return null;
               }  // End for
           }
        }
        catch (Throwable e) { return null; }
        return file;
    }   // End acceptIt()

    /** Determine if an extension goes with a valid audio type
     *
     * @param name of file to check
     * @return true if an audio file, false otherwise.
     */
    private boolean isAudio(String extension)
    {
    	extension = extension.toLowerCase();
        String[] soundArray = SoundDefaults.getSoundsSupported();
        if (extension.equalsIgnoreCase("mp3")) return true;
        for (int i=0; i<soundArray.length; i++)
            if (extension.equals(soundArray[i])) return true;

        return false;
    }
}        // End of AudioDropTarget class
