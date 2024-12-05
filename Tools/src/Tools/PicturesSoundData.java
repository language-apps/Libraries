/*
 * PicturesSoundData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package Tools;

import java.util.*;
import java.io.*;

/** Class to hold a vector of sound data objects or a text object */
public class PicturesSoundData implements Serializable
{
   private  static final long serialVersionUID = 1;
   public static final int GLOSS_SIZE = 30, IPA_SIZE = 30;
   public static final int MAX_SOUNDS = 999;
   
   private Vector<SoundData> soundVector;	
	  private boolean soundFlag;
	  private String  text;
  
   
   /** Method to convert from this object to the new version **/
   public org.acorns.data.PicturesSoundData convert(float version)
   {
       org.acorns.data.PicturesSoundData newData 
               = new org.acorns.data.PicturesSoundData(text, soundFlag);
       if (soundVector==null) return newData;
       
       Vector<org.acorns.data.SoundData> newVector = newData.getVector();
       
       Tools.SoundData oldSoundData;
       org.acorns.data.SoundData newSoundData;
       
       for (int i=0; i<soundVector.size(); i++)
       {  oldSoundData = soundVector.get(i);
          newSoundData = oldSoundData.convert(version);
          newVector.add(newSoundData);           
       }
       return newData;
	  
   }
   
 }        // End of PicturesSoundData class
