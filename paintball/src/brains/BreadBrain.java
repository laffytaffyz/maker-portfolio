/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package brains;

import arena.*;
import java.awt.Color;
import java.util.*;

/**
 *
 * @author tzhan0315
 */
public class BreadBrain implements Brain {
    private int team = 0; // team number
    private int baseCol = -1; // base's column
    private int oppBaseCol = -1; // opponent's base's column
    private boolean shouldAtBase = true; // allows bot to move away from base to shoot the enemy if needed
    private boolean defendShot = false; // allows bot to be shot to defend the base
    private boolean isDefense = true; // allows bot to switch between defense and offense
    
    private final int BASE_ROW = 16; // row of bases
    private final int ENEMY_DISTANCE = 6; // maximum enemy distance before starts shooting it
    private final int DEFENSE_DISTANCE = 7; // maximum distance around base for a brain to be considered as "defense"
    private final int DEFENSE_NUMBER = 3; // minimum number of defense bots
    private final int ROWS = 33; // number of rows on the board
    private final int COLS = 50; // number of columns on the board
    
    // Returns the name of the Paintball strategy ("bot").
    @Override
    public String getName() {
        return "bread brain";
    }

    // Returns the name of the *person* who wrote the code.
    @Override
    public String getCoder() {
        return "Tiffany Zhang";
    }

    // Returns the color of the Player that uses this strategy.
    @Override
    public Color getColor() {
        return new Color(220, 185, 158);
    };

    // Returns the chosen action by the given Player located on the given Board.
    @Override
    public Action getMove(Player p, Board b) {
        // sets up instance variables for the team, base, 
        // and opponent base
        if (team == 0) {
            team = p.getTeam();
            if (team == 2) {
                baseCol = 49;
                oppBaseCol = 0;
            }
            if (team == 1) {
                baseCol = 0;
                oppBaseCol = 49;
            }
        }

//        // determines if this player is defense or offense
//        defenseOrOffense(p, b);
        
        // determines if this player will be shot
        // if it will be shot, it will move in a random direction
        // that is not on the path of the shot
        ArrayList<Shot> shots = (ArrayList<Shot>) b.getAllShots();
        if(!defendShot) {
            for (Shot s : shots) {
                if (willShootPlayer(p, b, s)) {
                    int angle = 0;
                    do {
                        angle = randomDir();
                    } while (angle == s.getDirection() || angle == this.reverseDir(s.getDirection()));
                    return new Action("M", angle);
                }
            }
        }
        
//        // program for defense
//        if (isDefense) {
            
            // if the player should be at the base and is not, 
            // the player starts moving toward the base until it is adjacent to it
            if (shouldAtBase && Direction.moveDistance(p.getRow(), p.getCol(), BASE_ROW, baseCol) != 1) {
                int baseDir = Direction.getDirectionTowards(p.getRow(), p.getCol(), BASE_ROW, baseCol);
                int[] nextSpace = Direction.getLocInDirection(p.getRow(), p.getCol(), baseDir);
                if (b.isEmpty(nextSpace[0], nextSpace[1]))
                    return new Action("M", baseDir);
                else 
                    // will go in random direction if path towards base is blocked
                    return new Action("M", randomDir());
            }

            // determines if the base will be shot and will defend against it
            int pRow = p.getRow();
            int pCol = p.getCol();
            for (Shot s : shots) {
                
                // if the shot will shoot the base, the player is allowed to 
                // move away from the base to defend the shot and move to the
                // closest point to do so
                if (willShootBase(b, s)) {
                    defendShot = true;
                    ArrayList<int[]> t = shotTrajectory(b, s);
                    int[] coor = t.get(t.size() - 1);
                    return new Action("M", Direction.getDirectionTowards(pRow, pCol, coor[0], coor[1]));
                }
                
                // if the player is allowed to defend the shot and the shot 
                // will hit the player, the player will turn to face the shot
                // and shoot it 
                if (defendShot && willShootPlayer(p, b, s)) {
                    ArrayList<int[]> t = shotTrajectory(b, s);
                    int[] coor = t.get(t.size() - 1);
                    int d = Direction.getDirectionTowards(pRow, pCol, coor[0], coor[1]);
                    if (p.getDirection() != d) {
                        return new Action("T", d);
                    } else {
                        return new Action("S");
                    }
                }
                
            }
            // if there is no threatening shot, 
            // then the player does not have to defend a shot
            defendShot = false; 

            // player defense against incoming enemy players
            
            // determining closest enemy to shoot
            ArrayList<Player> enemies = (ArrayList<Player>) b.getAllPlayers(3-team);
            Player closestEnemy = enemies.get(0);
            int enemyDist = Direction.moveDistance(pRow, pCol, closestEnemy.getRow(), closestEnemy.getCol());
            for (Player e : enemies) {
                int tempDist = Direction.moveDistance(pRow, pCol, e.getRow(), e.getCol());
                if (tempDist < enemyDist) {
                    enemyDist = tempDist;
                    closestEnemy = e;
                }
            }

            // targeting the enemy to eliminate it
            int enemyDir = Direction.getDirectionTowards(pRow, pCol, closestEnemy.getRow(), closestEnemy.getCol());
            int currDir = p.getDirection();
            if (enemyDir == currDir) {
                
                // if the player is facing the enemy but the enemy is too 
                // close that it cannot be shot, the player does not have to
                // next to the base and will move backwards, but facing the
                // enemy to eliminate it
                if (enemyDist == 1) {
                    shouldAtBase = false;
                    int[] backSpace = Direction.getLocInDirection(p.getRow(), p.getCol(), reverseDir(currDir));
                    if (b.isEmpty(backSpace[0], backSpace[1])) {
                       return new Action("M", reverseDir(currDir));
                    } else {
                       return new Action("M", randomDir());
                    }

                // if the enemy isn't too close and the player is facing 
                // the enemy, it will shoot
                } else if (enemyDist <= ENEMY_DISTANCE) {
                    shouldAtBase = true; 
                    return new Action("S");
                }   
            
            // the player will turn towards the enemy if it isn't facing
            // the enemy already
            } else {
                return new Action("T", enemyDir);
            }
            
            return new Action("P");
//        }
        
        // program for offense
        // to prevent the enemy from blocking the shots, the player will move
        // until it is adjacent to the base in which it will shoot it
        // this is the most preferable strategy because there are blockers 
        // that prevent shooting the base from a distance
        
        // offense is commented out for second round paintball
//        int distToBase = Direction.moveDistance(p.getRow(), p.getCol(), BASE_ROW, oppBaseCol);
//        int dirToBase = Direction.getDirectionTowards(p.getRow(), p.getCol(), BASE_ROW, oppBaseCol);
//
//        // if the player is next to the base, it will turn and shoot it
//        if (distToBase == 1) {
//            if (dirToBase != p.getDirection())
//                return new Action("T", dirToBase);
//            else
//                return new Action("S");
//
//        // if the player is not next to the base, it will move towards it
//        } else {
//            int[] nextSpace = Direction.getLocInDirection(p.getRow(), p.getCol(), dirToBase);
//            if (b.isEmpty(nextSpace[0], nextSpace[1]))
//                return new Action("M", dirToBase);
//            return new Action("M", randomDir());
//        }
    }
    
    // calculates the reverse direction
    public int reverseDir(int dir) {
        return (dir + 180) % 360;
    }
    
    // randomly generates a direction 
    public int randomDir() {
        return 45* ((int) (Math.random() * 8));
    }
    
    // determines if the player is defense or offense
    public void defenseOrOffense(Player p, Board b) {
        int def = 0;
        
        // checks all positions to count the number of "defense" players
        // to be considered as a "defense" player, they must be on the same
        // team and is in the region of a specific range around the base
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                if (Direction.moveDistance(BASE_ROW, baseCol, r, c) <= DEFENSE_DISTANCE && !b.isEmpty(r,c) &&
                        b.get(r,c).getTeam() == team) {
                    def++;
                }
            }
        }
        
        // if the number of defense players are less than or equal to the 
        // number of needed defense players, this player is defense
        // if the player is in the region of a specific range around the base
        // (indicating that they are a defense player), the player will remain
        // as defense
        isDefense = def <= DEFENSE_NUMBER || Direction.moveDistance(BASE_ROW, baseCol, p.getRow(), p.getCol()) <= DEFENSE_DISTANCE;
    }
    
    // determines if a shot will shoot the base
    public boolean willShootBase(Board b, Shot s) {
        
        // if the coordinate after the last coordinate of the shot's trajectory
        // is the base, the base will be shot
        ArrayList<int[]> t = shotTrajectory(b, s);
        if (!t.isEmpty()) {
            int[] shotCoor = t.get(t.size()-1);
            int[] coor = Direction.getLocInDirection(shotCoor[0], shotCoor[1], s.getDirection());
            return (coor[0] == BASE_ROW && coor[1] == baseCol);
        }
        return false;
    }
    
    // determines if a shot will shoot this player
    public boolean willShootPlayer(Player p, Board b, Shot s) {
        
        // if the coordinate after the last coordinate of the shot's trajectory
        // is the player, the player will be shot
        int pRow = p.getRow();
        int pCol = p.getCol();
        ArrayList<int[]> t = shotTrajectory(b, s);
        if (!t.isEmpty()) {
            int[] shotCoor = t.get(t.size()-1);
            int[] coor = Direction.getLocInDirection(shotCoor[0], shotCoor[1], s.getDirection());
            return (coor[0] == pRow && coor[1] == pCol);
        }
        return false;
    }
    
    // calculates the coordinates on a shot's trajectory 
    public ArrayList<int[]> shotTrajectory(Board b, Shot s) {
        ArrayList<int[]> trajectory = new ArrayList<>();
        int x = s.getRow();
        int y = s.getCol();
        int d = s.getDirection();
        do {
            trajectory.add(new int[]{x, y});
            int next[] = Direction.getLocInDirection(x, y, d);
            x = next[0];
            y = next[1];
        } while (b.isValid(x, y) && b.isEmpty(x, y));
        return trajectory;
    }
}
