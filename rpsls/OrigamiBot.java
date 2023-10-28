package com.mycompany.rpsls;

public class OrigamiBot implements RPSLSStrategy {
  public String strategyName() {
    return "OrigamiBot";
  }
  
  public String coderName() {
    return "I.L. Fold";
  }
  
  public int getThrow() {
    return 1;
  }
  
  public void opponentsLastThrow(int lastThrow) {
    
  }
  
  public void reset() {
    
  }
}