package com.mycompany.rpsls;

public class CopyBot implements RPSLSStrategy {
  private int oppLast;
  
  public String strategyName() {
    return "CopyBot";
  }
  
  public String coderName() {
    return "Xerox";
  }
  
  public int getThrow() {
    return oppLast;
  }
  
  public void opponentsLastThrow(int lastThrow) {
    oppLast = lastThrow;
  }
  
  public void reset() {
    
  }
}