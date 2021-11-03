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

		return minimax_ab(boardState, searchDepth, 2147483647, -2147483648);
    }
	

	public static Move minimax_ab(BoardState bs, int sd, int a, int b)
    {
    	if (sd == 0)
    	{
    		return bs.getLegalMoves().get(0);
    	}
    	else if (bs.colour == -1) {
    		a = -2147483648;
    		int nodeNum = 0;
    		while (b < a && !bs.getLegalMoves().isEmpty()) {
    			BoardState bc = bs.deepCopy();
    			bc.makeLegalMove(bc.getLegalMoves().get(nodeNum).x, bc.getLegalMoves().get(nodeNum).y);
    			nodeNum++;
    			a = Math.max(a, boardVal(minimax_ab(bc, sd-1, a, b)));
    		}
    	}
    	else {
    		b = 2147483647;
    		int nodeNum = 0;
    		while (b < a && !bs.getLegalMoves().isEmpty()) {
    			BoardState bc = bs.deepCopy();
    			bc.makeLegalMove(bc.getLegalMoves().get(nodeNum).x, bc.getLegalMoves().get(nodeNum).y);
    			nodeNum++;
    			b = Math.min(b, boardVal(minimax_ab(bc, sd-1, a, b)));
    		}
    	}
    	return null;
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
