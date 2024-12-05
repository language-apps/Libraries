package org.acorns.language;

/**
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2014, all rights reserved
 *
 * This software is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * To receive a copy of the GNU Lesser General Public write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

import org.acorns.data.AudioFeatures;
import org.acorns.data.SoundData;
import org.acorns.phonemes.Component;
import org.acorns.phonemes.PhonemeType;

import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import javax.sound.sampled.AudioSystem;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

public class AudioCompare 
{
	private static ArrayList<Component> centerData;
	
	private int[] sourceClusters, targetClusters;
	
	/** Number of frames considered silence at start and end of the audio */
	public static final int MIN_SPEECH = 20;
	
	private static final int[] VALID_FEATURES =
	{ 
		AudioFeatures.PNCC0,
		AudioFeatures.PNCC0 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC0 +  2 * AudioFeatures.FEATURES_LENGTH,


		AudioFeatures.PNCC1,
		AudioFeatures.PNCC1 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC1 + 2 * AudioFeatures.FEATURES_LENGTH,

		
		AudioFeatures.PNCC2,
		AudioFeatures.PNCC2 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC2 + 2 * AudioFeatures.FEATURES_LENGTH,

		
		AudioFeatures.PNCC3,
		AudioFeatures.PNCC3 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC3 + 2 * AudioFeatures.FEATURES_LENGTH,


		AudioFeatures.PNCC4,
		AudioFeatures.PNCC4 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC4 + 2 * AudioFeatures.FEATURES_LENGTH,
		
		AudioFeatures.PNCC5,
		AudioFeatures.PNCC5 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC5 + 2 * AudioFeatures.FEATURES_LENGTH,

		AudioFeatures.PNCC6,
		AudioFeatures.PNCC6 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC6 + 2 * AudioFeatures.FEATURES_LENGTH,
		
//		AudioFeatures.PNCC7,
		AudioFeatures.PNCC7 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC7 + 2 * AudioFeatures.FEATURES_LENGTH,
		
//		AudioFeatures.PNCC8,
//		AudioFeatures.PNCC8 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC8 + 2 * AudioFeatures.FEATURES_LENGTH,
		
//		AudioFeatures.PNCC9,
//		AudioFeatures.PNCC9 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC9 + 2 * AudioFeatures.FEATURES_LENGTH,
		
//		AudioFeatures.PNCC10,
//		AudioFeatures.PNCC10 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC10 + 2 * AudioFeatures.FEATURES_LENGTH,
		
//		AudioFeatures.PNCC11,
//		AudioFeatures.PNCC11 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC11 + 2 * AudioFeatures.FEATURES_LENGTH,
		
//		AudioFeatures.PNCC12,
//		AudioFeatures.PNCC12 + AudioFeatures.FEATURES_LENGTH,
		AudioFeatures.PNCC12 + 2 * AudioFeatures.FEATURES_LENGTH,
		
		AudioFeatures.ENERGY,
//		AudioFeatures.ENERGY + AudioFeatures.FEATURES_LENGTH,
//		AudioFeatures.ENERGY + 2*AudioFeatures.FEATURES_LENGTH,
	};

	/** Main method to test the audio compare logic */
	public static void main(String[] args)
	{
		SoundData source = getAudio();
		AudioCompare audioTester = new AudioCompare(source);
		
		SoundData target = getAudio();
		audioTester.setTarget(null, target);
		double[] result = audioTester.compare();
		if (result==null)
		{
			System.out.println("Cluster components are not available");
			return;
		}
		double difference = result[2];
		double total = result[0] + result[1];
		double similarity = (total-difference)/total;
		
		for (int i=0; i<result.length; i++)
			System.out.printf("%.5f ", result[i]);
		System.out.println();
		System.out.printf(" Similarity = %.0f / %.0f = %.4f\n", (total-difference), total, similarity);
	}
	
	// Get audio object to test speech recognition algorithm
	private static SoundData getAudio()
	{
		SoundData sound = null;
        JFileChooser fc = new JFileChooser();
        DialogFilter dialogFilter = new DialogFilter();
        fc.setFileFilter(dialogFilter);
           
        fc.setDialogTitle("Select an Audio File");
        fc.addChoosableFileFilter(dialogFilter );
        Frame frame = JOptionPane.getRootFrame();
        int returnVal = fc.showOpenDialog(frame);
          
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
           File file = fc.getSelectedFile();
           if (!file.exists()) 
           {
              	Toolkit.getDefaultToolkit().beep();
                return null;
           }

           try 
           { 
        	  sound = new SoundData();
              sound.readFile(file);
           }
           catch(Exception e)
           {  
              	Toolkit.getDefaultToolkit().beep();
           }
        }
        return sound;
	}
	
	/** Constructor to initialize speech recognition clusters
	 *        and assign clusters to source audio file
	 *        
	 * @param source audio file to which we will compare to
	 * @throws NoSuchElementException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public AudioCompare(SoundData source)
	{
		this(null, source);
	}
	
	/** Constructor to initialize speech recognition clusters
	 *        and assign clusters to source audio file
	 *        
	 * @param bounds start/end offsets in the audio object
	 * @param source audio file to which we will compare to
	 * @throws NoSuchElementException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	public AudioCompare(Point bounds, SoundData source) 
	{
		if (bounds==null) bounds = new Point(-1,-1);
		
		if (source == null || !source.isRecorded())
		{
			sourceClusters = null;
			return;
		}

		/** Only read files for speech recognition components once */
		if (centerData==null)
		{
	    	ObjectInputStream ois = null;
	    	try 
	    	{
	        	ois = createObjectInputStream("phonemeComponents");
	    		if (ois != null)
	    		{
		        	centerData = new ArrayList<Component>();
		        	Component component;
		
	    			Object object;
	                
	    			while (true)
	    			{
	    				object = ois.readObject();
	        			if (!(object instanceof int[]))
	        			{
	        				component = (Component)object;
	        				centerData.add(component);
	        			}
	    			}
	    		}
	    		
	        } catch (EOFException ex) {}
	    	catch (Exception ex) { sourceClusters = null; }
	    	
	    	try { ois.close(); } catch (Exception e) {}
		}
		
		// Return if speech recognition is not possible
		if (centerData==null) return;

		/** Configure cluster assignments for source audio */
		sourceClusters = assignClusters(bounds, source);
	}

	/** Extract audio feature frames and assign each frame to clusters 
	 * 
	 * @param bounds start/end offsets in the audio object
	 * @param audio SoundData audio object
	 * @return Cluster assignments for each frame
	 */
	private int[] assignClusters(Point bounds, SoundData audio)
	{
	   	AudioFeatures features = new AudioFeatures(bounds, audio, true, true, true);
     	double[][] averages = features.computeAverages();
    	double[][] stats = features.getStatisticsSTD(averages);
		double[][] validStats = PhonemeType.getValidFeatures(VALID_FEATURES, stats);

		int len = stats.length;
		int[] clusters = new int[len];
		for (int frame=0; frame<len; frame++)
		{
			clusters[frame] = findClosestCluster(validStats[frame], centerData);
			if ( (bounds == null || bounds.x <= 0) && (frame < MIN_SPEECH || frame > len - MIN_SPEECH) )
				clusters[frame] = SpellCheck.SILENCE;
		}
		if (bounds == null || bounds.x <= 0) markSilentFrames(clusters, stats);
		return clusters;
	}
	
     /** Find cluster closest to the supplied frame of features
     * 
     * @param features Frame of audio feature values
     * @param centers ArrayList of cluster components
     * @return closest cluster number
     */
    private int findClosestCluster(double[] features, ArrayList<Component> components)
    {
    	double distance, minDistance = components.get(0).computeDistance(features);
    	int minCluster = 0;
    	for (int cluster=1; cluster<components.size(); cluster++)
    	{
    		distance = components.get(cluster).computeDistance(features);
    		if (distance<minDistance)
    		{
    			minDistance = distance;
    			minCluster = cluster;
    		}
    	}
    	return minCluster;
    }


    /** Set the target cluster for audio to which to compare to source
     * 
	 * @param bounds start/end offsets in the audio object
     * @param target Audio object
     */
	public void setTarget(Point bounds, SoundData target)
	{
		if (centerData != null)
		   targetClusters = assignClusters(bounds, target);
	}

	/** Compute similarity value for comparing two audio files
	 * 
	 * @return {source compressed length, target compressed length, difference}
	 */
	public double[] compare()
	{
		if (centerData==null) return null;
		return SpellCheck.audioDistance(sourceClusters, targetClusters);
	}
	
	/** Mark those frames that are silent or pauses
	 * 
	 * @param frame of audio features
	 */
	private void markSilentFrames(int[] clusters, double[][] stats)
	{
		for (int f=0; f<clusters.length; f++)
		{
			if (PhonemeType.isSilentFrame(stats[f]))
			{
				clusters[f] = SpellCheck.SILENCE;
			}
		}
	}
	
   // Create input stream for reading cluster data
   private ObjectInputStream createObjectInputStream(String name)
   					throws IOException
   {
	   String fileName = "/data/" + name + ".dat";
	   URL url = getClass().getResource(fileName);
	   URLConnection connection = url.openConnection();
	   ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
	   return in;
   }	
   
   private static class DialogFilter extends FileFilter
   {
       public DialogFilter()    {}

   	
      /* Determine if a file passes the audio filter
       *
       * @param file File object to filter
       *
       * @return true if this file passes the filter check
       */
       public boolean accept(File file)
       {
          if (file.isDirectory()) return true;

          // Determine if this is a readable audio file.
          try
          {  
        	  AudioSystem.getAudioFileFormat(file);
              return true;
          }
          catch (Exception e) {}
          return false;
      }

       @Override
       public String getDescription() 
   		{
   			return "Audio file filtenr";
   		}
   }	// End of Nested DialogFilter class


}	// End of AudioCompare class
