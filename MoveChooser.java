import java.util.ArrayList;  
import java.lang.Math;

public class MoveChooser {
    
    // used by the boardval function
	private static int[][] evalFunc = { {120, -20, 20,  5,  5, 20, -20, 120},
								 		{-20, -40, -5, -5, -5, -5, -40, -20},
										{ 20,  -5, 15,  3,  3, 15, -5,   20},
										{  5,  -5,  3,  3,  3,  3, -5,    5},
										{  5,  -5,  3,  3,  3,  3, -5,    5},
										{ 20,  -5, 15,  3,  3, 15, -5,   20},
										{-20, -40, -5, -5, -5, -5, -40, -20},
										{120, -20, 20,  5,  5, 20, -20, 120} };  

    public static Move chooseMove(BoardState boardState) {

		int searchDepth= Othello.searchDepth;

	    ArrayList<Move> moves = boardState.getLegalMoves();

	    if(moves.isEmpty()){
	        return null;
		}

		return minimax(boardState, searchDepth);
    }
	
    /*
     * This is the first function call for the minimax algorithm.
     * This is not really recursive, just a way to make life easier for me.
     * Because the alpha beta pruning gives me an int as a return value with no node/ move associated with it,
     * here I essentially go through each of the daughter nodes of the initial boardstate left to right,
     * and I find the maximum daughter as determined by the minimax_val function.
     */
    public static Move minimax(BoardState bs, int sd) {
        ArrayList<Move> moveList = bs.getLegalMoves();
        int numMoves = moveList.size();
        Move bestMove = null;

        if (numMoves == 0) {
            return null;
        }
        else {
            // The best move will initially be the first move
            bestMove = moveList.get(0);

            // create a copy of the board and make this move
            BoardState bestBoard = bs.deepCopy();
            bestBoard.makeLegalMove(bestMove.x, bestMove.y);
            int bestEval = boardVal(bestBoard);

            // look though the rest of the possible moves.
            for (int i = 1; i < numMoves; i++) {

                // copy the original board and make the next move
                Move currentMove = bs.getLegalMoves().get(i);
                BoardState currentState = bs.deepCopy();
                currentState.makeLegalMove(currentMove.x, currentMove.y);

                // call to recursive function
                int currentEval = minimax_val(currentState, sd-1, -2147483648, 2147483647);

                // update the best move if this move is better
                if (bestEval < currentEval) {
                    bestMove = currentMove;
                }
            }
        }
        return bestMove;

    }
	public static int minimax_val(BoardState bs, int sd, int a, int b)
    {
        if (sd == 0)
    	{
    		return boardVal(bs);
    	}
        else if (bs.colour == 1) { // if maximising node (white wants to maximise)
            a = -2147483648; // -infinity for ints
            int nodeNum = 0; // used to keep track of which daughters have been checked

            // don't need to check if 0 moves are possible, as the while conditional checks this
            // if 0 moves, while is not executed, and the turn is skipped.
            while ((b>a) && ( nodeNum < bs.getLegalMoves().size() )){
                // I interpreted the edges between nodes as moves, so this finds the next edge.
                Move nextEdge = bs.getLegalMoves().get(nodeNum);

                nodeNum++;

                // creates the daughter node by cloning itself and then applying the "edge" move.
                BoardState nextNode = bs.deepCopy();
                nextNode.makeLegalMove(nextEdge.x, nextEdge.y);

                // recursive call w decreminted search depth.
                a = Math.max(a, minimax_val(nextNode, sd-1, a, b));
            }
            return a;
        }
    	else { // if minimizing node
    		b = 2147483647; // inifinity for ints
    		int nodeNum = 0; // track which daughters have been chcked 

            // don't need to check if 0 moves are possible, as the while conditional checks this
            // if 0 moves, while is not executed, and the turn is skipped.
    		while ( (b>a) && (nodeNum < bs.getLegalMoves().size()) ) {
    			// same process as in the above else if statement, but instead you use math.min because this is a minimzing node
                Move nextEdge = bs.getLegalMoves().get(nodeNum);
                nodeNum++;
                BoardState nextNode = bs.deepCopy();
                nextNode.makeLegalMove(nextEdge.x, nextEdge.y);
    			b = Math.min(b, minimax_val(nextNode, sd-1, a, b));
    		}
            return b;
    	}
    	
    }


    // This is a function used to find the value of the board from white's perspective using the 
    // static evaluation function.
    public static int boardVal(BoardState bs) {
	    int total_val_white = 0;
	    int total_val_black = 0;

	    for (int i = 0; i < 8; i++) {
	    	for (int j = 0; j < 8; j++) {
	    		if (bs.getContents(i,j) == 1) {
	    			total_val_white += evalFunc[i][j];
	    		}
	    		else if (bs.getContents(i,j) == -1) {
	    			total_val_black += evalFunc[i][j];
	    		}
	    	}
	    }

	    return total_val_white - total_val_black;
    }
    
}
