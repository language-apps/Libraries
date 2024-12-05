package org.acorns.language.keyboards.data;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DeadSequences implements Constants
{

	/** The set of dead key sequence states parsed from the keylayout document */
	private Hashtable<String, String[]> deadSequences;
	
    /** An hash table of terminator states and outputs */
    private ArrayList<String[]> terminators;
	
	public DeadSequences(Document document) 
	{
		deadSequences = initializeDeadKeySequences(document);
		terminators = getTerminatorStates(document);
	}
	
	public Hashtable<String, String[]>getDeadSequences() { return deadSequences; }
	
    private Hashtable<String, String[]> initializeDeadKeySequences(Document document)
    {
    	Element tag, actionElement;
    	String state, previous, output, next, key, data[];
 	
	 	Hashtable<String, String[]> deadSequences = new Hashtable<String,String[]>();
	 	
	 	NodeList list = document.getElementsByTagName("action");
	 	NodeList whenList;
	 	  
	 	if (list==null || list.getLength()==0)
	    {  
	 	 return deadSequences;
	    }
	 	  
	    for (int i=0; i<list.getLength(); i++)
	    {
	        tag = (Element)list.item(i);
	        if (!tag.hasAttribute("id")) { continue; }
	         
	        state = tag.getAttribute("id");
	        // Create keyMap and configure state table
	        whenList = tag.getElementsByTagName("when");
	        for (int j=0; j<whenList.getLength(); j++)
	        {
	        	actionElement = (Element)whenList.item(j);
	        	if (!actionElement.hasAttribute("state"))
	        		continue;
	             
	             
	            previous = actionElement.getAttribute("state");
	            output   = actionElement.getAttribute("output");
	            next     = actionElement.getAttribute("next");
	             
	            if (!actionElement.hasAttribute("through"))
	            {  
	            	key = previous + "~~" + state;
	            	data = new String[MAP_LEN];
	            	data[ACTION] = next;
	            	data[OUTPUT] = output;
	            	 
	            	deadSequences.put(key,  data);
	            }
	            else
	            {  
	            	int initial = 0, last = 0, mult = 1, current, number = 0;
	                try { initial = Integer.parseInt(state); }
	                catch (NumberFormatException nfe) {  continue; }
	                
	                try 
	                { 
	                	last = Integer.parseInt(actionElement.getAttribute("through")); 
	                }
	                catch (NumberFormatException nfe) {  continue; }
	                
	                if (actionElement.hasAttribute("multiplier"));
	                {  try
	                   {   mult = Integer.parseInt
	                              (actionElement.getAttribute("multiplier"));
	                   }
	                   catch (NumberFormatException nfe) { continue; }
	                }
	
	                if (actionElement.hasAttribute("next"))
	                {  try { number = Integer.parseInt(next); }
	                   catch (NumberFormatException nfe)
	                   {  continue; }
	                }
	                                
	                if (actionElement.hasAttribute("output"))
	                {  
	                   number = actionElement.getAttribute("output").charAt(0);
	                   // Adjust if numbers are in control character range.
	                   if (number>=0x2400 && number<0x2420)  
	                   { 
	                	   number -= 0x2400; 
	                   }
	                }
	 
	                current = initial;
	                while (current<=last)
	                {  
	                   key = previous + "~~" + (current - initial)*mult;
	                   data = new String[2];
	                   data[ACTION] = next;
	                   data[OUTPUT] = output;
	                   if (output.length()>0)
	                	   
	              	   deadSequences.put(key,  data);
	
	                   current++;
	                   if (actionElement.hasAttribute("next"))
	                   {  
	                	   next = "" + (char)((current-initial)*mult+number);
	                   }
	                   
	                   if (actionElement.hasAttribute("output"))
	                	      output = "" + ((current - initial)*mult + number); 
	                   
	                }  // End while
	             }     // End handling ranges
	          }        // End of for loop through all the when tags
	      }           // End of for loop through all the action tags
	      return deadSequences;
	  }		// end of initializeDeadKeySequences
    
    /** Method to find the termination output for this state
     *  @param termination state
     *  @return output state
     *  
     *  return null if not found
     */
    public String findTerminator(String state)
    {  String[] entry;
       for (int i=0; i<terminators.size(); i++)
       {  entry = terminators.get(i);
          if (entry[0].equals(state)) { return entry[1]; }
       }
       return "";
    }
    
    public @Override String toString()
    {  
    	StringBuffer buffer = new StringBuffer();
        
        buffer.append("\"terminators\": \n[");
    	String value[], key, action, output, jsonItem;
        for (int i=0; i<terminators.size(); i++)
        {
        	value = terminators.get(i);
        	key = value[0];
        	output = value[1];
        	output = output.replace("\\", "\\\\");
     	    jsonItem = "{\"key\": \"" + key 
       	   		+ "\", \"output\": \"" + output 
       	   	    + "\"}";
       	   buffer.append(jsonItem);
    	   if (i<terminators.size()-1)
    		   buffer.append(",");
    	   else
    		   buffer.append("\n],");
    	   buffer.append("\n");
        }
        
        buffer.append("\n");
      	buffer.append("\"deadSequences\": \n[");
        Enumeration<String> keys = deadSequences.keys();
        while(keys.hasMoreElements())
        {
     	   key = keys.nextElement();
       	   value = deadSequences.get(key);
    	   action = value[ACTION];
    	   output = value[OUTPUT];
    	   
    	   if (action.contains("\""))
        	   if (action.contains("\""))
       	        action = action.replace("\"", "\\\"");
       	   else action = action.replace("\\", "\\\\");

    	   if (output.contains("\""))
    	        output = output.replace("\"", "\\\"");
    	   else output = output.replace("\\", "\\\\");

    	   if (key.contains("\""))
          	    key = key.replace("\"", "\\\"");
    	   else key = key.replace("\\", "\\\\");
    	   
    	   jsonItem = "{\"key\": \"" + key 
    			+ "\", \"action\": \"" + action 
    	   		+ "\", \"output\": \"" + output 
    	   	    + "\"}";
    	   buffer.append(jsonItem);
    	   if (keys.hasMoreElements())
    		   buffer.append(",");
    	   else
    		   buffer.append("\n],");
    	   buffer.append("\n");
        }
        return buffer.toString();
    }
    
   /** Method to get the list of action states
     * @param document the XML DOM object
     * @return hash table of terminator actions
     */
    private ArrayList<String[]> getTerminatorStates(Document document) 
    {
       ArrayList<String[]> action = new ArrayList<String[]>();
           
       NodeList nodeList = document.getElementsByTagName("terminators");
       {  if (nodeList.getLength()==0) return action;
          
          Element terms = (Element)nodeList.item(0);
          NodeList whenList = terms.getElementsByTagName("when");
          Element when;
          String[] entry;
      
          for (int i=0; i<whenList.getLength(); i++)
          {
              entry = new String[2];
              when    = (Element)whenList.item(i);
              entry[0]= when.getAttribute("state");
              entry[1]= when.getAttribute("output");
              action.add(entry);
          }
          return action;
       }
    }     // End of getTerminatorStates()

 }			// End of DeadSequences class
