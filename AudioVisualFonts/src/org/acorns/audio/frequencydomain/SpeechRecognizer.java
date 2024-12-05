/*
 * SpeechRecognizer.java 
 *
 *   @author  HarveyD
 *   Dan Harvey - Professor of Computer Science
 *   Southern Oregon University, 1250 Siskiyou Blvd., Ashland, OR 97520-5028
 *   harveyd@sou.edu
 *   @version 1.00
 *
 *   Copyright 2010, all rights reserved
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

package org.acorns.audio.frequencydomain;

public class SpeechRecognizer 
{
	
	public SpeechRecognizer()
	{
		
	}
	
	/** Compute the square of the EUCLIDEAN distance between two frames
	 * 
	 * @param source The source array of features for the frame
	 * @param destination The destination array of features for the frame
	 * @param number of values to check in the frame
	 * @return sum of squares of the distance
	 */
	public double frameDistance(double[] source, double[] destination, int len)
	{
		int length = Math.max(source.length, destination.length);
		length = Math.min(length, len);
		
		double distance = 0, difference;
		for (int i=0; i<length; i++)
		{
			if (i>=source.length) difference = destination[i];
			else if (i>=destination.length) difference = source[i];
			else difference = source[i] - destination[i];
			
			distance += difference * difference;
		}
		return Math.sqrt(distance);		
	}
}
