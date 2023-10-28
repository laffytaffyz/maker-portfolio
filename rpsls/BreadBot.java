/* BreadBot.java        By: Tiffany Zhang
 * 
 * My rock paper scissors lizard spock robot
 */

package com.mycompany.rpsls;

import java.util.*;

public class BreadBot implements RPSLSStrategy
{
  //instance variables
  private int turn;
  private int[] oppThrows; //stores the opponent's throws with weight
  private Map<Integer, List<Integer>> counterThrows; //stores values that counter the key
  //variables for copy bot
  private int myPrevThrow, myPrevPrevThrow, oppPrevThrow, copyFlag, copyThreshold;
  
  //variables for cycling
  private int pos, cycleFlag, cycleThreshold;
  private List<Integer> lastMoves;
  
  //variables for weight
  private int weightFlag, weightThreshold;
  private int[] myThrows; //stores my throws with weight
  
  //constructor
  public BreadBot()
  {
    this.reset();
  }
  
  //returns the name of the robot
  public String strategyName() {return "BreadBot";}
  
  //returns the name of the creator
  public String coderName() {return "Tiffany Zhang";}
  
  //returns the throw of the bot
  public int getThrow()
  {
    //********FIRST THROW STRATEGY********
    //throws random for first throw
    if (turn == 0) 
    {
      turn++;
      myPrevThrow = new Random().nextInt(5);
      return myPrevThrow;
    }
    
    //Makes turn increment regardless first throw or not
    else
    {
      turn++;
    }
    
    //Stores new previous previous throw so that this throw, 
    //which will become the previous throw for the next throw, 
    //can be stored in previous throw
    int tempMyPrevPrevThrow = myPrevPrevThrow; //temporarily stored to be used, but don't need for next throw
    myPrevPrevThrow = myPrevThrow;
    if (tempMyPrevPrevThrow >= 0)
    {
      myLastThrow(tempMyPrevPrevThrow);
    }
    
    //********WEIGHTED STRATEGY********
    //checks if the opponent's last throw is the weighted throw
    int oppWeightThrow = oppWeightThrow();
    if (oppWeightThrow == oppPrevThrow)
    {
      weightFlag++;
    }
    else
    {
      weightFlag = 0;
    }
    
    //if the weight flag exceeds the threshold
    //throw a counter against the opponent's weighted throw
    if (weightFlag >= weightThreshold)
    { 
      myLastThrow(myPrevPrevThrow);
      myPrevThrow = counterThrows.get(oppWeightThrow()).get(new Random().nextInt(1));
      removeMyLastThrow(myPrevPrevThrow);
      return myPrevThrow;
    }
    
    //********COPYBOT STRATEGY********
    //compares throws to see if opponent is copying
    if (oppPrevThrow == tempMyPrevPrevThrow)
    {
      copyFlag++;
    }
    else
    {
      copyFlag = 0;
    }
    
    //If the copy flag exceeds the threshold
    //the opponent is copying
    //so it will throw a number that beats the previous throw
    if (copyFlag > copyThreshold)
    {
      myPrevThrow = counterThrows.get(myPrevPrevThrow).get(new Random().nextInt(1));
      return myPrevThrow;
    }
    
    //********CYCLING STRATEGY********
    if (cycleFlag >= cycleThreshold)
    {
      myPrevThrow = counterThrows.get(lastMoves.get(0)).get(new Random().nextInt(1));
      return myPrevThrow;
    }
    
    //********REGULAR STRATEGY********
    //find the preferred throw
    int preferredThrow = 0;
    for (int i = 0; i < 5; i++)
    {
      if (oppThrows[i] > oppThrows[preferredThrow]) {preferredThrow = i;}
    }
    
    //get the counters of the preferred throw
    int counter1 = counterThrows.get(preferredThrow).get(0);
    int counter2 = counterThrows.get(preferredThrow).get(1);
    
    int counterWeight1 = 0;
    int counterWeight2 = 0;
    
    //add weights in which counter beats the more preferred throw
    for (int i = 0; i < 5 && i != preferredThrow; i++)
    {
      int firstCounter = counterThrows.get(i).get(0);
      int secondCounter = counterThrows.get(i).get(1);
      if (counter1 == firstCounter || counter1 == secondCounter) {counterWeight1 += oppThrows[i];}
      if (counter2 == firstCounter || counter2 == secondCounter) {counterWeight2 += oppThrows[i];}
    }
    
    //subtract weights in which counter loses the more preferred throw
    counterWeight1 -= counterThrows.get(counter1).get(0);
    counterWeight1 -= counterThrows.get(counter1).get(1);
    counterWeight2 -= counterThrows.get(counter2).get(0);
    counterWeight2 -= counterThrows.get(counter2).get(1);
    
    //compares all of the weights and the highest one is thrown
    if (counterWeight1 > counterWeight2)
    {
      myPrevThrow = counter1;
    }
    else if (counterWeight1 < counterWeight2)
    {
      myPrevThrow = counter2;
    }
    else 
    {
      myPrevThrow = counter1;
    }
    return myPrevThrow;
  }
  
  //receives the opponent's last throw and stores it with a stronger weight correlating to the more recent turns
  public void opponentsLastThrow(int lastThrow) 
  {
    if(lastThrow <= 4 && lastThrow >= 0)
    {
      oppPrevThrow = lastThrow;
      oppThrows[lastThrow] += turn/100 + 1;
      
      try
      {
      if (lastThrow == lastMoves.get(0))
      {
        lastMoves.remove(0);
        cycleFlag++;
      }
      else
      {
        cycleFlag = 0;
      }
      }
      catch (IndexOutOfBoundsException e)
      {
        
      }
      lastMoves.add(lastThrow);
    }
  }
  
  //receives my last throw and stores it with a stronger weight correlating to the more recent turns
  public void myLastThrow(int lastThrow) 
  {
    if(lastThrow <= 4 && lastThrow >= 0)
      {
    myThrows[lastThrow] += turn/100 + 1;
    }
  }
  
  //removes my most recent throw
  //when comparing if the opponent is using weights
  //the previous previous throw must be used, but not the previous one
  //but when making choices, the previous one is needed
  //but needs to be removed later on
  public void removeMyLastThrow(int lastThrow)
  {
    if(lastThrow <= 4 && lastThrow >= 0)
      {
    myThrows[lastThrow] -= turn/100 + 1;
    }
  }
  
  //finds the possible weighted throw by the opponent
  public int oppWeightThrow()
  {
    //find my preferred throw
    int myPreferredThrow = 0;
    for (int i = 0; i < 5; i++)
    {
      if (myThrows[i] > myThrows[myPreferredThrow]) {myPreferredThrow = i;}
    }
    
    //get the opponent's counters of my preferred throw
    int oppCounter1 = counterThrows.get(myPreferredThrow).get(0);
    int oppCounter2 = counterThrows.get(myPreferredThrow).get(1);
    
    int oppCounterWeight1 = 0;
    int oppCounterWeight2 = 0;
    
    //add weights in which counter beats my preferred throw
    for (int i = 0; i < 5 && i != myPreferredThrow; i++)
    {
      int oppFirstCounter = counterThrows.get(i).get(0);
      int oppSecondCounter = counterThrows.get(i).get(1);
      if (oppCounter1 == oppFirstCounter || oppCounter1 == oppSecondCounter) {oppCounterWeight1 += myThrows[i];}
      if (oppCounter2 == oppFirstCounter || oppCounter2 == oppSecondCounter) {oppCounterWeight2 += myThrows[i];}
    }
    
    //subtract weights in which counter loses my preferred throw
    oppCounterWeight1 -= counterThrows.get(oppCounter1).get(0);
    oppCounterWeight1 -= counterThrows.get(oppCounter1).get(1);
    oppCounterWeight2 -= counterThrows.get(oppCounter2).get(0);
    oppCounterWeight2 -= counterThrows.get(oppCounter2).get(1); 
    
    //compares all of the weights and the highest one is the opponent's most possible throw
    int oppWeightThrow;
    if (oppCounterWeight1 > oppCounterWeight2)
    {
      oppWeightThrow = oppCounter1;
    }
    else if (oppCounterWeight1 < oppCounterWeight2)
    {
      oppWeightThrow = oppCounter2;
    }
    else 
    {
      oppWeightThrow = oppCounter1;
    }
    
    return oppWeightThrow;
  }
  
  //resets everything in the bot for the next round
  public void reset()
  {
    turn = 0;
    oppThrows = new int[5];
    
    counterThrows = new HashMap<>();
    
    //Adds values for rock counters
    List<Integer> counterRock = new ArrayList<>();
    counterRock.add(1);
    counterRock.add(4);
    counterThrows.put(0, counterRock);
    
    //Adds values for paper counters
    List<Integer> counterPaper = new ArrayList<>();
    counterPaper.add(2);
    counterPaper.add(3);
    counterThrows.put(1, counterPaper);
    
    //Adds values for scissors counters
    List<Integer> counterScissors = new ArrayList<>();
    counterScissors.add(0);
    counterScissors.add(4);
    counterThrows.put(2, counterScissors);
    
    //Adds values for lizard counters
    List<Integer> counterLizard = new ArrayList<>();
    counterLizard.add(0);
    counterLizard.add(2);
    counterThrows.put(3, counterLizard);
    
    //Adds values for spock counters
    List<Integer> counterSpock = new ArrayList<>();
    counterSpock.add(1);
    counterSpock.add(3);
    counterThrows.put(4, counterSpock);
    
    //Against copy bot
    myPrevThrow = -1;
    oppPrevThrow = -1;
    copyFlag = 0;
    copyThreshold = 5;
    
    //Against cycling
    lastMoves = new ArrayList<Integer>();
    cycleFlag = 0;
    cycleThreshold = 5;
    
    //Against weighted throw strategy
    myThrows = new int[5];
    weightFlag = 0;
    weightThreshold = 5;
  }
}