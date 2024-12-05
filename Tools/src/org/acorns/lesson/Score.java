/**
 * Score.java
 *
 *   @author  HarveyDan Jr.
 *   @version 3.00 Beta
 *
 *   Copyright 2007-2015, all rights reserved
 */
package org.acorns.lesson;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import org.acorns.language.*;

/** Panel to maintain user scores and difficulty levels of lessons
 *  being executed
 */
public class Score
{
   /** Minimum complexity level */
   public static final int MIN_DIFFICULTY = 1;
   /** Maximum complexity level */
   public static final int MAX_DIFFICULTY = 5;
   /** Font for score label */
   public static final int FONT = 16;
   
   public static final int CORRECT = 0;
   public static final int INCORRECT = 1;
   public static final int TOTAL = 2;
   public static final int PERCENT = 3;

   private static int correct = 0, incorrect = 0;
   
   private static JLabel scoreLabel = null;
   private static int    complexityLevel = 2;
   
   /** Reset the scores */
   public static void reset() 
   { correct = incorrect = 0; 
     calculateScore();
   }
  
   /** Record the next score
    *  @param answer true if correct, false otherwise
    */
   public static void nextScore(boolean answer)
   {  if (answer) correct++;
      else        incorrect++;
   }

   /** Return an HTML label string indicating the current user score */
   public static String calculateScore()
   {
       String correctAns = LanguageText.getMessage("commonHelpSets", 6);
       String totalAns = LanguageText.getMessage("commonHelpSets", 7);
       String htmlText = "<html>&nbsp;"+correctAns + correct + "<br>";
       htmlText += "&nbsp;" + totalAns  + (correct+incorrect);
       htmlText += "</html>";
       if (scoreLabel!= null) 
       {
           scoreLabel.setText(htmlText);
           repaintScore();
       }
       return htmlText;
   }  // End calculateScore()
   
   /** Get current difficulty level
    *  @return complexity level
    */
   public static int getDifficultyLevel()  { return complexityLevel; }
      
   /** Set current difficulty level
    *  @param level desired complexity level
    *  @return true if successful, false otherwise
    */
   public static boolean setDifficultyLevel(int level)
   {
       if (level>=MIN_DIFFICULTY && level<=MAX_DIFFICULTY)
       {  complexityLevel = level; 
          return true;
       }
       return false;
   }

   /** Redisplay the score label */
   public static void repaintScore()
   {
       if (scoreLabel!=null) 
       {  scoreLabel.revalidate();
          scoreLabel.repaint();
       }
   }
 
   /** Get the current score totals 
    * 
    * @return double array (correct answers, incorrect answers, 
    * 							total answers, percentage correct
    */
   public static double[] getScores()
   {
	   double total = correct + incorrect;
	   double result = (total>0) ? 100 * correct / total : 0;
	   return new double[] {correct, incorrect, total, result};
   }

   /** Get the score label sized appropriately
    *
    * @param displaySize Desired size
    * @return the score JLabel object
    */
   public static JLabel getScoreLabel(Dimension displaySize)
   {
       if (scoreLabel == null)
       {  
          // Add score label with appropriate font and text.
          Font font = new Font(null, Font.PLAIN, FONT);
          scoreLabel = new JLabel("");
      
          scoreLabel.setFont(font);
          scoreLabel.setForeground(new Color(255, 255, 180));
          scoreLabel.setBackground(new Color(130, 130, 130));
          scoreLabel.setOpaque(true);
          scoreLabel.setBorder(BorderFactory.createEtchedBorder
           (EtchedBorder.LOWERED, new Color(200,200,200), new Color(90,50,50)));
       }
       scoreLabel.setSize(displaySize);
       scoreLabel.setMinimumSize(displaySize);
       scoreLabel.setPreferredSize(displaySize);
       scoreLabel.setMaximumSize(displaySize);  
       return scoreLabel;
       
   }   // End of getScoreLabel
   
}  // End of Score class
