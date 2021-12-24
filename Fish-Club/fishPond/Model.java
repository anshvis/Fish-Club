package fishPond;

import java.util.*;

import com.sun.org.apache.xerces.internal.impl.dv.ValidatedInfo;

import cmsc131Utilities.Random131;

/**
 * Model for the Fish Pond Simulation.   The model consists of a List of Fish,   
 * a List of Plants, and a two dimensional array of boolean values representing 
 * the pond (each element in the array is either ROCK, or WATER.)               
 * <p>                                                               
 * Each time the simulation is re-started a new Model object is created.        
 * <p>                                                                             
 * STUDENTS MAY NOT ADD ANY FIELDS.  ALSO, STUDENTS MAY NOT ADD ANY PUBLIC      
 * METHODS.  (PRIVATE METHODS OF YOUR OWN ARE OKAY.)                            
 *                                                                           
 * @author Fawzi Emad, Ansh Viswanathan             
 */
public class Model {
	
	/* State of this Model.  STUDENTS MAY NOT ADD ANY FIELDS! */
	private ArrayList<Fish> fish;
	private ArrayList<Plant> plants;
	private boolean[][] landscape;
	
	/** Value stored in landscape array to represent water */
	public static final boolean WATER = false;
	
	/** Value stored in landscape array to represent rock */
	public static final boolean ROCK = true;
	
	/* Minimum pond dimensions */
	private static final int MIN_POND_ROWS = 5;
	private static final int MIN_POND_COLS = 5;
	
	/** THIS METHOD HAS BEEN WRITTEN FOR YOU!
	 * <p>
	 * If numRows is smaller than MIN_POND_ROWS, or if numCols is smaller than 
	 * MIN_POND_COLS, then this method will throw an IllegalPondSizeException.
	 * <p>
	 * The fields "rows" and "cols" are initialized with the values of 
	 * parameters numRows and numCols.
	 * <p>
	 * The field "landscape" is initialized as a 2-dimensional array of booleans.  
	 * The size is determined by rows and cols.  Every entry in the landscape array 
	 * is filled with WATER.  The border around the perimeter of the landscape array 
	 * (top, bottom, left, right) is then overwritten with ROCK.
	 * <p>
	 * Random rocks are placed in the pond until the number of rocks (in addition to 
	 * those in the border) reaches numRocks.
	 * <p>
	 * The "plants" ArrayList is instantiated.  Randomly placed Plant objects are put 
	 * into the List.  Their positions are chosen so that they are never above rocks or
	 * in the same position as another plant.  Plants are generated in this way until
	 * the list reaches size numPlants.
	 * <p>
	 * The "fish" ArrayList is instantiated.  Now randomly placed Fish objects are put 
	 * into the List.  Their directions are also randomly selected.  The positions are 
	 * chosen so that they are never above rocks, plants, or other fish.  Fish are 
	 * generated in this way until the list reaches size numFish.
	 * 
	 * @param numRows number of rows for pond
	 * @param numCols number of columns for pond
	 * @param numRocks number of rocks to be drawn in addition to rocks around border 
	 * of pond
	 * @param numFish number of fish to start with
	 * @param numPlants number of plants to start with
	 */
	public Model(int numRows, int numCols, int numRocks, int numFish, int numPlants) {
		
		if (numRows < MIN_POND_ROWS || numCols < MIN_POND_COLS)
			throw new IllegalPondSizeException(numRows, numCols);
		
		landscape = new boolean[numRows][numCols];
		plants = new ArrayList<Plant>();
		fish = new ArrayList<Fish>();
		
		/* Fill landscape with water and a border of rocks around the perimeter */
		for (int i = 0; i < numRows; i++) {
			for (int j = 0; j < numCols; j++)
				landscape[i][j] = WATER;
			landscape[i][0] = ROCK;
			landscape[i][numCols - 1] = ROCK;
		}
		for (int j = 1; j < numCols - 1; j++) {
			landscape[0][j] = ROCK;
			landscape[numRows - 1][j] = ROCK;
		}
		
		/* Place random rocks */
		int rocksPlaced = 0;
		while (rocksPlaced < numRocks) {
			
			int row = Random131.getRandomInteger(numRows);
			int col = Random131.getRandomInteger(numCols);
			if (landscape[row][col] == WATER) {
				landscape[row][col] = ROCK;
				rocksPlaced++;
			}
		}
		
		/* Place random plants */
		for (int i = 0; i < numPlants; i++) {
			int row = Random131.getRandomInteger(numRows);
			int col = Random131.getRandomInteger(numCols);
			try{
				addPlant(new Plant(row, col, Plant.PLANT_STARTING_SIZE));
			}
			catch(IllegalPlantPositionException e) {
				i--;
			}
		}
		
		/* Place random fish */
		for (int i = 0; i < numFish; i++) {
			int row = Random131.getRandomInteger(numRows);
			int col = Random131.getRandomInteger(numCols);
			int r = Random131.getRandomInteger(4);
			int dir;
			if (r == 0)                 
				dir = Fish.UP;
			else if (r == 1)
				dir = Fish.DOWN;
			else if (r == 2)
				dir = Fish.LEFT;
			else
				dir = Fish.RIGHT;
			
			Fish f = new Fish(row, col, Fish.FISH_STARTING_SIZE, dir);
			try {	
				addFish(f);
			}
			catch(IllegalFishPositionException e) {
				i--;
			}
		}
	}
	
	/** THIS METHOD HAS BEEN WRITTEN FOR YOU.
	 * <p>
	 * When a plant gets bigger than Plant.MAX_PLANT_SIZE, it will explode into
	 * 2 to 9 smaller plants, whose sizes add up to the size of the original plant.
	 * The smaller plants will be placed in the 9 regions of the landscape array
	 * that surround the original plant.  If there are rocks, fish, or other plants 
	 * already occupying these adjacent regions, then fewer than 9 plants are created.  
	 * If there are no available regions nearby, the plant will not explode.  */
	public void plantExplosions() {
		
		Iterator<Plant> i = plants.iterator();
		while(i.hasNext()) {
			Plant curr = i.next();
			if (curr.getSize() > Plant.MAX_PLANT_SIZE) {
				int count = 0;    // number of available slots for little plants
				boolean[] freeSpace = new boolean[9];  // true if just water in that region
				
				/* locations of 8 little plants are determined by these offsets to
				 * the coordinates of the plant that is exploding. */
				int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
				int[] dy = {1, 1, 0, -1, -1, -1, 0, 1};
				
				int r = curr.getRow();
				int c = curr.getCol();
				
				/* Look to see if space is available in all eight directions */
				for (int j = 0; j < 8; j++) {
					freeSpace[j] = isSpaceAvailable(r + dy[j], c + dx[j]);
					if (freeSpace[j])
						count++;
				}
				
				/* We'll split only if 1 or more spaces are available */
				if (count > 0) {
					i.remove();    // kill off original plant
					int newSize = curr.getSize() / (count + 1);   // truncates!
					
					/* Add little plants to the list -- iterator is now broken! */
					for (int j = 0; j < 8; j++)
						if (freeSpace[j])
							plants.add(new Plant(r + dy[j], c + dx[j], newSize));
					
					plants.add(new Plant(r, c, newSize));  // replace original
					
					/* Since we've modified the List, the original iterator
					 * is no longer useful.  Start iterating from the beginning. */
					i = plants.iterator();
				}	
			}
		}
	}
	
	/** THIS METHOD HAS BEEN WRITTEN FOR YOU!
	 * <p>
	 * When a fish gets bigger than Fish.MAX_FISH_SIZE, it will explode into
	 * 4 to 8 smaller fish, whose sizes add up to the size of the original fish.
	 * The smaller fish will be placed in the eight regions of the landscape array
	 * surrounding the original fish.  The little fish will be begin moving in
	 * directions that point away from the original location.  (Note that no little 
	 * fish is placed into the original location of the landscape array where the
	 * exploding fish was -- just in the surrounding squares.)  If there are rocks, 
	 * fish, or plants already occupying these adjacent squares, then fewer than 
	 * eight little fish are created. If there are not at least four available 
	 * surrounding squares, then the fish will not explode.*/
	public void fishExplosions() {
		
		Iterator<Fish> i = fish.iterator();
		while(i.hasNext()) {
			Fish curr = i.next();
			if (curr.getSize() > Fish.MAX_FISH_SIZE) {
				int count = 0;  // number of available squares for little fish
				boolean[] freeSpace = new boolean[8];  // true if just water in that region
				
				/* locations of the 8 little fish are determined by these offsets
				 * to the coordinates of the original fish that is exploding */
				int[] dx = {0, 1, 1, 1, 0, -1, -1, -1};
				int[] dy = {-1, -1, 0, 1, 1, 1, 0, -1};
				
				/* directions for the 8 little fish */
				int[] newDir = {Fish.UP, Fish.UP, Fish.RIGHT, Fish.RIGHT, Fish.DOWN, 
						Fish.DOWN, Fish.LEFT, Fish.LEFT};
				
				int r = curr.getRow();
				int c = curr.getCol();
				
				/* Look to see if space is available in all directions */
				for (int j = 0; j < 8; j++) {
					freeSpace[j] = isSpaceAvailable(r + dy[j], c + dx[j]);
					if (freeSpace[j])
						count++;
				}
				
				/* We'll split only if 4 or more spaces are available */
				if (count > 3) {
					i.remove();  // remove original fish from List
					int newSize = curr.getSize() / count;
					
					/* Add little fish to the list -- iterator is now broken! */
					for (int j = 0; j < 8; j++)
						if (freeSpace[j])
							fish.add(new Fish(r + dy[j], c + dx[j], newSize, newDir[j]));
					
					/* Since we have modified the List, the original Iterator
					 * is no longer valid.  We'll start iterating again from the
					 * beginning. */
					i = fish.iterator();
				}	
			}
		}
	}
	
	/**
	 * Checks the specified location to see if it has a rock, fish, or plant in it.
	 * If so, returns false; if it is just water, returns true.
	 * @param r is the int representing the row
	 * @param c is the int representing the column
	 * @return true if the location has just water, false if it has a fish, 
	 * plant, or rock
	 */
	public boolean isSpaceAvailable(int r, int c) {
		for (Fish arrFish : fish) {
			if (arrFish.getCol() == c && arrFish.getRow() == r) {
				return false;
			}
		}
		for (Plant arrPlant : plants) {
			if (arrPlant.getCol() == c && arrPlant.getRow() == r) {
				return false;
			}
		}
		if (landscape[r][c] == ROCK) {
			return false;
		}
		return true;
	}
	
	/** Copy Constructor.
	 * <p>
	 * Since landscape is immutable, it is be copied with just a shallow copy.
	 * Fish and Plants are mutable, so they must be copied with a DEEP copy!
	 * (WARNING:  Each fish and each plant must be copied.)
	 */
	public Model(Model other) {
		landscape = new boolean 
				[other.landscape.length][other.landscape[0].length];
		fish = new ArrayList<Fish>();
		plants = new ArrayList<Plant>();
		for (int row = 0; row<other.getRows(); row++) {
			for (int col = 0; col<other.getCols(); col++) {
				landscape[row][col] = other.landscape[row][col];
			}
		}
		for (Fish otherFish : other.fish) {
			fish.add(new Fish(otherFish));
		}
		for (Plant otherPlant : other.plants) {
			plants.add(new Plant(otherPlant));
		}
		
	}
	
	/**
	 * Fish f eats a portion of plant p.  The amount eaten is either 
	 * Plant.PLANT_BITE_SIZE or the current size of the plant, whichever is smaller.  
	 * The fish's size is increased by this amount and the plant's size is decreased 
	 * by this amount.
	 * @param f represents the Fish eating the plant
	 * @param p represents the Plant that gets eaten
	 */
	public static void fishEatPlant(Fish f, Plant p) {
		int bite = Plant.PLANT_BITE_SIZE<p.getSize() ? Plant.PLANT_BITE_SIZE : 
			p.getSize();
		f.eat(bite);
		p.removeBite(bite);
	}
	
	/** returns number of rows in landscape array */
	public int getRows() {
		return landscape.length;
	}
	
	/** returns number of columns in landscape array */
	public int getCols() {
		return landscape[0].length;
	}
	
	/** Iterates through fish list.  For each fish that isAlive, shrinks the fish by
	 * invoking it's "shrink" method. */
	public void shrinkFish() {
		for (Fish arrFish : fish) {
			if (arrFish.isAlive()) {
				arrFish.shrink();
			}
		}
	}
	
	/** Iterates through the plants list, growing each plant by invoking it's "grow"
	 * method. */
	public void growPlants() {
		for (Plant arrPlant : plants) {
			arrPlant.grow();
		}
	}
	
	/** Iterates through the list of Fish.  Any fish that is no longer alive is removed
	 *  from the list. */
	public void removeDeadFish() {
		for (int index = 0; index<fish.size(); index++) {
			if (!fish.get(index).isAlive()) {
				fish.remove(index);
				index--;
			}
		}
	}
	
	/** Iterates through the list of Plants.  Any plant that is no longer alive is removed
	 * from the list. */
	public void removeDeadPlants() {
		for (int index = 0; index<plants.size(); index++) {
			if (!plants.get(index).isAlive()) {
				plants.remove(index);
				index--;
			}
		}
	}
	
	/**
	 * @param f is the Fish that is being checked if its surrounded by rocks
	 * @return true if the Fish is surrounded by rocks, false if not
	 */
	private boolean fishIsSurroundedByRocks(Fish f) {
		boolean rockAbove = landscape[f.getRow()-1][f.getCol()] == ROCK;
		boolean rockBelow = landscape[f.getRow()+1][f.getCol()] == ROCK;
		boolean rockLeft = landscape[f.getRow()][f.getCol()-1] == ROCK;
		boolean rockRight = landscape[f.getRow()][f.getCol()+1] == ROCK;
		if (rockAbove && rockBelow && rockLeft && rockRight) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Iterate through list of Fish.  FOR EACH FISH THAT IS ALIVE, do the following:
	 * <p>
	 * 1. If this fishIsSurroundedByRocks, DO NOTHING, and move on to the next fish.
	 *     (This fish will not turn.)
	 * <p>
	 * 2.  If this fish's direction is not equal to one of the codes UP, DOWN, LEFT, or
	 *     RIGHT, then throw an IllegalFishDirectionException, passing this fish's 
	 *     direction to the constructor of the IllegalFishDirectionException.
	 * <p>    
	 * 3.  Check whether or not this fish is about to hit a rock if it moves in it's 
	 *     current direction.  If it is about to hit a rock, call the fish's 
	 *     setRandomDirection method.  Repeat this step until the fish is no longer
	 *     about to hit a rock.  Do not make any EXTRA calls to setRandomDirection or 
	 *     you will fail our tests!
	 */
	public void turnFish() {
		for (Fish arrFish : fish) {
			if (!fishIsSurroundedByRocks(arrFish)) {
				if (arrFish.getDirection() != Fish.UP && arrFish.getDirection() 
						!= Fish.DOWN && arrFish.getDirection() != Fish.LEFT && 
						arrFish.getDirection() != Fish.RIGHT) {
					throw new 
					IllegalFishDirectionException(arrFish.getDirection());
				} else {
					while (!validNextMove(arrFish)) {
						arrFish.setRandomDirection();
					}
				}
			}
		}
	}
	
	/**
	 * Helper method for turnFish. Checks if the location that would be the 
	 * potential next move (using the direction that the Fish is facing) is a 
	 * valid move.
	 * @param arrFish is the Fish that is being checked if its next move is 
	 * valid.
	 * @return true if the next move is possible, false if not.
	 */
	private boolean validNextMove(Fish fish) {
		int moveRow = fish.getRow();
		int moveCol = fish.getCol();
		switch (fish.getDirection()) {
		case (Fish.UP):
			moveRow--;
			break;
		case (Fish.DOWN):
			moveRow++;
			break;
		case (Fish.LEFT):
			moveCol--;
			break;
		case (Fish.RIGHT):
			moveCol++;
			break;
		}
		if (landscape[moveRow][moveCol] == ROCK) {
			return false;
		} else {
			return true;
		}
	}
	
	
	
	/**
	 * Note:  This method assumes that each live fish that is not surrounded by
	 * rocks is already facing a direction where there is no rock!  (Typically the
	 * call to this method should immediately follow a call to "turnFish", which
	 * ensures that these conditions are satisfied.)
	 * <p>
	 * This method iterates through the list of fish.  FOR EACH FISH THAT IS ALIVE,
	 * do the following:
	 * <p>
	 * 1.  Check to see if this fishIsSurroundedByRocks.  If so, DO NOTHING and move
	 *     along to the next fish in the list.  (This fish does not move, does not
	 *     eat, does not fight.)
	 * <p>        
	 * 2.  Move this fish by calling it's "move" method.
	 * <p>
	 * 3.  Check if there is a plant that isAlive and is located in the same position
	 *     as this fish.  If so, have the fish eat part of the plant by calling
	 *     fishEatPlant.
	 * <p>    
	 * 4.  Check if there is another fish (distinct from this fish) that is in the same
	 *     location as this fish.  If so, have the two fish fight each other by calling
	 *     the fight method.  IMPORTANT -- the fight method is not symmetrical.  You 
	 *     must use THIS fish as the current object, and pass the OTHER fish as the 
	 *     parameter (otherwise you will not pass our tests.)
	 */
	public void moveFish() {
		for (Fish arrFish : fish) {
			if (arrFish.isAlive()) {
				if (!fishIsSurroundedByRocks(arrFish)) {
					arrFish.move();
					for (Plant arrPlant : plants) {
						if (arrPlant.getCol() == arrFish.getCol() && 
								arrPlant.getRow() == arrFish.getRow() && 
								arrPlant.isAlive()) {
							fishEatPlant(arrFish, arrPlant);
						}
					}
					for (Fish compFish : fish) {
						if (arrFish.getCol() == compFish.getCol() && 
								arrFish.getRow() == compFish.getRow() && 
								fish.indexOf(arrFish) != fish.indexOf(compFish))
						{
							arrFish.fight(compFish);
						}
					}
				}
			}
		}
	}
	
	/** Attempts to add the plant p to plant list, if possible.
	 * <p>
	 * First checks if the landscape in the plant's location is equal to ROCK.  If it 
	 * is, then does not add the plant to the list.  Instead throws an 
	 * IllegalPlantPositionException, passing 
	 * IllegalPlantPositionException.PLANT_OVER_ROCK to the constructor.
	 * <p>
	 * Now checks for another plant (distinct from the parameter) that is in the 
	 * same location as the parameter.  If one is found, then does not add the plant
	 * to the list.  Instead throws an IllegalPlantPositionException,
	 * passing IllegalPlantPositionException.TWO_PLANTS_IN_ONE_PLACE to the 
	 * constructor.
	 * <p>
	 * Otherwise, adds the plant to the list "plants".
	 */
	public void addPlant(Plant p) {
		if (landscape[p.getRow()][p.getCol()] == ROCK) {
			throw new IllegalPlantPositionException
			(IllegalPlantPositionException.PLANT_OVER_ROCK);
		} else {
			for (Plant arrPlant : plants) {
				if (arrPlant.getRow() == p.getRow() && arrPlant.getCol() == 
						p.getCol()) {
					throw new IllegalPlantPositionException
					(IllegalPlantPositionException.TWO_PLANTS_IN_ONE_PLACE);
				}
			}
			plants.add(p);
		}
	}
	
	/**Adds the fish f to the fish list, if possible.
	 * <p>
	 * First checks if the landscape in the fish's location is equal to ROCK.  
	 * If it is, then the fish is not added to the list.  Instead, throws an 
	 * IllegalFishPositionException, passing 
	 * IllegalFishPositionException.FISH_OVER_ROCK to the constructor.
	 * <p>
	 * Next checks for another fish (distinct from the parameter) that is in the 
	 * same location as the parameter.  If one is found, then the fish is not
	 * added to the list.  Instead throws an IllegalFishPositionException,
	 * passing IllegalFishPositionException.TWO_FISH_IN_ONE_PLACE to the constructor.
	 * <p>
	 * Otherwise, adds the parameter to the fish list.
	 */
	public void addFish(Fish f){
		if (landscape[f.getRow()][f.getCol()] == ROCK) {
			throw new IllegalFishPositionException
			(IllegalFishPositionException.FISH_OVER_ROCK);
		} else {
			for (Fish arrFish : fish) {
				if (arrFish.getRow() == f.getRow() && arrFish.getCol() == 
						f.getCol()) {
					throw new IllegalFishPositionException
					(IllegalFishPositionException.TWO_FISH_IN_ONE_PLACE);
				}
			}
			fish.add(f);
		}
	}
	
	/** Returns a COPY of the fish list.  Hint:  Use the ArrayList<Fish> copy 
	 * constructor, otherwise you will fail our tests! */
	public ArrayList<Fish> getFish() {
		return new ArrayList<Fish>(fish);
	}
	
	/** Returns a COPY of the plants list.  Hint:  Use the ArrayList<Plant> 
	 * copy constructor, otherwise you will fail our tests! */ 
	public ArrayList<Plant> getPlants() {
		return new ArrayList<Plant>(plants);
	}
	
	/** Returns the specified entry of the landscape array (either WATER or ROCK). */
	public boolean getShape(int row, int col) {
		return landscape[row][col];
	}
}
