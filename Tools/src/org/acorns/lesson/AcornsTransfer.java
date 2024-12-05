/*
 * AcornsTransfer.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */

package org.acorns.lesson;

import org.acorns.data.*;
import java.io.*;
import java.awt.*;
import java.awt.datatransfer.*;

/** Class to facilitate transfer of acorns objects */
public class AcornsTransfer implements Serializable, Transferable 
{
	private static final long serialVersionUID = 1;
	private static final String LESSON = "org.acorns.Lesson";
	private static final String PICTURE = "org.acorns.Choice";
	private static final String ACORN = "org.acorns.Acorn";
	
	public static final int LESSONTYPE = 0, PICTURETYPE = 1, ACORNTYPE = 2;
	
	private static DataFlavor lessonFlavor = new DataFlavor(Lesson.class, LESSON);
	private static DataFlavor pictureFlavor = new DataFlavor(PictureChoice.class, PICTURE);
	private static DataFlavor acornFlavor = new DataFlavor(PicturesSoundData.class, ACORN);
	
	private static DataFlavor[] flavors = 
			{ lessonFlavor, pictureFlavor, acornFlavor };
	
	private Lesson lesson;
	private String[] lessonHeader;
	private PictureChoice pictureChoice;
	private PicturesSoundData picturesSoundData;
	
	/** Constructor to define the new data flavor */
	public AcornsTransfer()	{}
	
	/** Constructor for lesson objects */
	public AcornsTransfer(Lesson lesson)
	{
		this.lesson = lesson;
		this.lessonHeader = lesson.getLessonHeader();
	}
	
	/** Constructor for picture & sound acorns objects */
	public AcornsTransfer(PicturesSoundData picturesSoundData)
	{
		this.picturesSoundData = picturesSoundData;
	}
	
	/** Constructor for multiple picture objects */
	public AcornsTransfer(PictureChoice pictureChoice)
	{
		this();
		this.pictureChoice = pictureChoice;
	}
	
	/** The method for copying */
	public void copyObject()
	{
		Clipboard clipboard 
		   = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(this, null);		
	}
	
	/** Determine if a paste operation is possible
	 * 
	 * @param type LESSON, PICTURE, or ACORN
	 * @return true if possible, false otherwise
	 */
	public static boolean isPastable(int type)
	{
		Clipboard clipboard 
		   = Toolkit.getDefaultToolkit().getSystemClipboard();
	    Transferable data = clipboard.getContents(null);
	    try
	    {  Object object = data.getTransferData(flavors[type]);
	       AcornsTransfer transfer = (AcornsTransfer)object;
     	   switch (type)
	       {
	    	  case LESSONTYPE:
	    		return transfer.getLesson()!=null;
	    	  case PICTURETYPE:
	    		return transfer.getPictureData()!=null;
	    	  case ACORNTYPE:
	    		return transfer.getPicturesSoundData()!=null;
	    	}
	    	
	    } catch (Exception e) 
	      { return false; }
	    
	    return false;
	}

	/** Get the transfer object from the clip board 
	 * 
	 * @param type LESSON, PICTURE, or ACORN
	 * @return true if possible, false otherwise
	 * @throws UnsupportedFlavorException
	 * @throws IOException
	 */
	public static AcornsTransfer getCopiedObject(int type) 
			throws UnsupportedFlavorException, IOException 
	{
		Clipboard clipboard 
		   = Toolkit.getDefaultToolkit().getSystemClipboard();
	    Transferable data = clipboard.getContents(null);
		Object object = data.getTransferData(flavors[type]);
  	   	if (object instanceof AcornsTransfer)
  	   	{   
 	    	return (AcornsTransfer)object;
  	   	}
 	    
 	    return null;
	}
	
	/** Getter methods called during a paste operation */
	public Lesson getLesson() 
	{ 
		if (lesson!=null) lesson.initializeLesson();
		return lesson; 
	}
	public String[] getLessonHeader() { return lessonHeader; }
	public PicturesSoundData getPicturesSoundData() 
	{ return picturesSoundData; }
	public PictureChoice getPictureData() { return pictureChoice; }
	
	/** Methods required for a transferable object */
	public Transferable getTransferable() {	return this; }
	
	/** Get flavors supported by this object */
	public DataFlavor[] getTransferDataFlavors() 
	{ return flavors; }

	/** Check if the flavor is supported by this object */
	public boolean isDataFlavorSupported(DataFlavor flavor) 
	{
		String name = flavor.getHumanPresentableName();
		if (name.equals(LESSON)) return true;
		if (name.equals(PICTURE)) return true;
		if (name.equals(ACORN)) return true;
		return false;
	}

	/** Get a data transfer object */
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException 
	{
		if (isDataFlavorSupported(flavor)) return this;
		else return null;
	}

}	// End of AcornsTransfer class
