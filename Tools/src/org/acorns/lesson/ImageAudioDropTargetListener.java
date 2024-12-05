/**
 * FrameDropTarget.java
 * @author HarveyD
 * @version 4.10 Beta
 *
 * Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson;

import java.lang.reflect.*;
import java.awt.datatransfer.*;
import java.awt.dnd.*;
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.*;

import org.acorns.audio.*;

public class ImageAudioDropTargetListener implements DropTargetListener
{
    Lesson lesson;
    JComponent component;
    
    public ImageAudioDropTargetListener(Lesson lesson, JComponent panel)
    {
        if (panel==null) return;
        
        new DropTarget(panel, this);
        this.lesson = lesson;
        component = panel;
    }
    
    public void drop(DropTargetDropEvent dtde)
    {   
        dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
        File[] files = getTransferObjects(dtde.getTransferable());
        if (files == null) 
        { dtde.dropComplete(false);
          return;
        }
        String extension;
        String[] args = new String[1];

        for (int i=0; i<files.length; i++)
        {
           args[0] = files[i].getPath();
           extension = args[0].substring(args[0].lastIndexOf(".")+1);
           if (isImage(extension))
           { 
              try
              {
                 URL url = new File(args[0]).toURI().toURL(); 
                 lesson.insertPicture(url, 100,  0);
                 lesson.displayLesson();
              }
              catch (Exception e) {}
           }
           else  if (isAudio(extension))
           {
              Class<?> componentClass = component.getClass();
              Class<?>[] params = new Class[1];
              params[0] = File.class;

              try
              {
                  Method audioDropped 
                      = componentClass.getMethod("audioDropped", params);
                  
                  audioDropped.invoke(component, files[i]);
              }
              catch (Exception  e) 
              {
                  System.out.println(e);
              }
           }
        }
        dtde.dropComplete(true);
    }
    
    public void dragEnter (DropTargetDragEvent dtde) 
    { 
        if (!acceptIt(dtde.getTransferable())) dtde.rejectDrag();
        else dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    public void dragExit (DropTargetEvent dte) {}

    public void dragOver (DropTargetDragEvent dtde)
    {   if (!acceptIt(dtde.getTransferable())) dtde.rejectDrag();
        else dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
    }

    public boolean isDropAcceptable(DropTargetDropEvent dtde)
    {  return acceptIt(dtde.getTransferable());  }

    public boolean isDragAcceptable(DropTargetDragEvent dtde)
    { return acceptIt(dtde.getTransferable());
    }
  
    public void dropActionChanged (DropTargetDragEvent dtde)
    {   if (!acceptIt(dtde.getTransferable())) dtde.rejectDrag(); }

    /** Method to determine if drop type is correct
     *
     * @param transfer The transferable object
     * @return true if acceptable drop type.
     */
    private boolean acceptIt(Transferable transfer)
    {
        DataFlavor[] flavors = transfer.getTransferDataFlavors();
        for (int i=0; i<flavors.length; i++)
        { if (flavors[i].getRepresentationClass() == List.class) return true;
           if (flavors[i].getRepresentationClass() == AbstractList.class)
                return true;
        }
        return false;
    }

    /** Method to get the transferable list of files
     * 
     * @param transfer The transferable object
     * @return An array of file objects or (null if none)
     */
    private File[] getTransferObjects(Transferable transfer)
    {
        DataFlavor[] flavors = transfer.getTransferDataFlavors();
        File[] files = new File[1];

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
               files = new File[size];
               String extension;

               for (int i=0; i<size; i++)
               {
                  files[i] = (File)list.get(i);
                  extension = files[i].getName();
                  extension = extension.substring(extension.lastIndexOf(".")+1);

                 if (!isImage(extension))
                 {
                     if (component!=null && !isAudio(extension)) 
                         return null;
                 }
               }  // End for
           }
        }
        catch (Throwable e)  {  return null;  }
        return files;
    }   // End acceptIt()

    /** Determine if an extension goes with
     *
     * @param name of file to check
     * @return true if an image file, false otherwise.
     */
    private boolean isImage(String extension)
    {
        String[] imageArray = ImageIO.getReaderFormatNames();
        for (int i=0; i<imageArray.length; i++)
            if (extension.equals(imageArray[i])) return true;

        return false;
    }
    
    /** Determine if an extension goes with a valid audio type
     *
     * @param name of file to check
     * @return true if an audio file, false otherwise.
     */
    private boolean isAudio(String extension)
    {
        String[] soundArray = SoundDefaults.getSoundsSupported();
        if (extension.equalsIgnoreCase("mp3")) return true;
        for (int i=0; i<soundArray.length; i++)
            if (extension.equals(soundArray[i])) return true;

        return false;
    }
}        // End of FrameDropTarget class
