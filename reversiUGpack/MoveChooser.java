import java.util.ArrayList;  
import java.lang.Math;

public class MoveChooser {
  
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

	    //System.out.println("total vals: " +boardVal(boardState));

	    if(moves.isEmpty()){
	        return null;
		}

		return minimax(boardState, searchDepth);
    }
	
    public static Move minimax(BoardState bs, int sd) {
        ArrayList<Move> moveList = bs.getLegalMoves();
        int numMoves = moveList.size();
        Move bestMove = null;
        if (numMoves == 0) {
            return null;
        }
        else {
            bestMove = moveList.get(0);
            BoardState bestBoard = bs.deepCopy();
            bestBoard.makeLegalMove(bestMove.x, bestMove.y);
            int bestEval = boardVal(bestBoard);
            for (int i = 1; i < numMoves; i++) {
                Move currentMove = bs.getLegalMoves().get(i);
                BoardState currentState = bs.deepCopy();
                currentState.makeLegalMove(currentMove.x, currentMove.y);
                if (bestEval < minimax_val(currentState, sd, -2147483648, 2147483647)) {
                    bestMove = currentMove;                }
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
        else if (bs.colour == -1) {
            a = -2147483648;
            int nodeNum = 0;
            while ((b>a) && ( nodeNum < bs.getLegalMoves().size() )){
                Move nextEdge = bs.getLegalMoves().get(nodeNum);
                nodeNum++;
                BoardState nextNode = bs.deepCopy();
                nextNode.makeLegalMove(nextEdge.x, nextEdge.y);
                a = Math.max(a, minimax_val(nextNode, sd-1, a, b));
            }
            return a;
        }
    	else {
    		b = 2147483647;
    		int nodeNum = 0;
    		while ( (b<a) && (nodeNum < bs.getLegalMoves().size()) ) {
    			Move nextEdge = bs.getLegalMoves().get(nodeNum);
                nodeNum++;
                BoardState nextNode = bs.deepCopy();
                nextNode.makeLegalMove(nextEdge.x, nextEdge.y);
    			b = Math.min(b, minimax_val(nextNode, sd-1, a, b));
    		}
            return b;
    	}
    	
    }

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
