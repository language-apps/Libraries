/*
 * AnnotationData.java
 *
 *   @author  HarveyD
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package Tools;

import java.io.*;

public class AnnotationData extends SoundData implements Serializable, Cloneable
{   public static final int   MAX_ANNOTATIONS = 10;   // Annotation levels.
    private static final long serialVersionUID = 1;
    
    private AnnotationNode[][] data;  // Table of annotation data
    private int annotations[];        // Number of Annotations/level
    private String[] keyboards;       // Keyboard map for each level.
    private int level;                // Active annotation level
    
   
   /** Convert the structure to the current version */
   public @Override org.acorns.data.AnnotationData convert(float version)
   {
      org.acorns.data.AnnotationNode[][] newData 
        = new org.acorns.data.AnnotationNode[MAX_ANNOTATIONS][];

      for (int r=0; r<data.length; r++)
      {   if (data[r] != null)
          {
              newData[r] = new org.acorns.data.AnnotationNode[data[r].length];
              for (int c=0; c<data[r].length; c++)
              {
                 if (data[r][c] !=null)
                      newData[r][c] = data[r][c].convert(version);
              }
          }
      }
      org.acorns.data.AnnotationData newAnnotation
        = new org.acorns.data.AnnotationData
           (getSoundText(), getAudioFormat(), audioData, newData
                                            , annotations, keyboards, level);
      return newAnnotation;		
  }
    
}   // End of AnnotationData class

