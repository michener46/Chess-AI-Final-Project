import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.PriorityQueue;

import com.google.gson.Gson;

/*
 * Owen Michener
 * April 19th, 2024
 * This code uses a minimax search to attempt to play chess.
 * It is currently set up to use a built in heuristic function to evaluate board positions.
 * However, there is still the code left from me attempting to use a neural network with this.
 * This code that is commented out will communicate with server.py to send and recieve data requried to run the neural network.
 */

public class chessAI {
	HttpClient client;
	Gson gson;
	
	boolean white;
	int numberOfMoves = 0;
	
	static class InputData {
        private final int[] array;

        public InputData(int[] array) {
            this.array = array;
        }
    }
	
	public class movePair implements Comparable<movePair> {
	    private String move;
	    private double value;
	    private ChessGame game;

	    public movePair(String str, double value, ChessGame game) {
	        this.move = str;
	        this.value = value;
	        this.game = game;
	    }

	    public String getString() {
	        return move;
	    }

	    public double getValue() {
	        return value;
	    }
	    
	    public ChessGame getGame() {
	    	return game;
	    }

	    @Override
	    public int compareTo(movePair other) {
	        return Double.compare(other.value, this.value);
	    }
	}
	
	class pair{
		double alpha;
		double beta;
		String move;
		pair(double a, double b, String s){
			alpha = a;
			beta = b;
			move = s;
		}
	}
	
	public chessAI(boolean white) {
		client = HttpClient.newHttpClient();

        gson = new Gson();
        
        this.white = white;
	}
	
	public String getBestMove(ChessGame currentState) {
		int depth = 8;
		ChessGame game = new ChessGame(currentState.getBoard(), currentState.whitesMove());
		pair p = max(depth, game, new pair(Double.MIN_VALUE, Double.MAX_VALUE, null), 5, numberOfMoves);
		numberOfMoves++;
		return p.move;
	}
	
	pair max(int depth, ChessGame currentState, pair p, int moveCounter, int moveCount) {
		if(depth == 0) return new pair(value(currentState, moveCount), Double.MIN_VALUE, ""); //Terminal state
		else depth -= 1; //Decrease depth for later searching
		if(currentState.getGameOver()) return new pair(currentState.inCheck() * 100000000, p.beta, ""); //Terminal state for checkmate
		pair pa = new pair(p.alpha, p.beta, null);
		String[] moves;
		if(white) moves = currentState.whiteMoves().toArray(new String[0]); //Finds the list of moves depending on black or white
		else moves = currentState.blackMoves().toArray(new String[0]);
		PriorityQueue<movePair> pq = new PriorityQueue<>(); //Used to find the top moveCounter amount of moves
		for(String s : moves) {
			ChessGame nextGame = new ChessGame(currentState.getBoard(), white);
		    nextGame.move(s);
		    double a = value(nextGame, moveCount);
		    pq.add(new movePair(s, a, nextGame));
		}
		int length = pq.size();
		for(int i = 0; i < length; i++) { //Basic implementation of minimax with alpha beta pruning
			movePair s = pq.remove();
			if (pa.move == null) pa.move = s.move;
			if(i == moveCounter) break;
			double a = min(depth, s.getGame(), pa, moveCounter, moveCount+1).beta;
			if(a > pa.alpha) {
				pa.move = s.move;
				pa.alpha = a;
			}
			if(pa.alpha >= pa.beta) return pa;
		}
		return pa;
	}
	//Same as max but flipped to find minimum
	pair min(int depth, ChessGame currentState, pair p, int moveCounter, int moveCount) {
		if(depth == 0) return new pair(p.alpha, value(currentState, moveCount), "");
		else depth -= 1;
		pair pa = new pair(p.alpha, p.beta, null);
		String[] moves;
		if(white) moves = currentState.whiteMoves().toArray(new String[0]);
		else moves = currentState.blackMoves().toArray(new String[0]);
		PriorityQueue<movePair> pq = new PriorityQueue<>(Collections.reverseOrder());
		for(String s : moves) {
			ChessGame nextGame = new ChessGame(currentState.getBoard(), white);
		    nextGame.move(s);
		    double a = value(nextGame, moveCount);
		    pq.add(new movePair(s, a, nextGame));
		}
		int length = pq.size();
		for(int i = 0; i < length; i++) {
			movePair s = pq.remove();
			if (pa.move == null) pa.move = s.move;
			if(i == moveCounter) break;
			double b = max(depth, s.getGame(), pa, moveCounter, moveCount+1).alpha;
			if(b < pa.beta) {
				pa.beta = b;
			}
			if(pa.alpha >= pa.beta) return pa;
		}
		return pa;
	}
	//Will find the value of the board includes the communication between server.py
	public double value(ChessGame currentState, int moveCount) {
		/*
        String json = gson.toJson(new InputData(currentState.getBoard()));

        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:5000/predict"))
            .header("Content-Type", "application/json")
            .POST(BodyPublishers.ofString(json))
            .build();
        HttpResponse<String> response = null;
        try {
            response = client.send(request, BodyHandlers.ofString());
        } catch (Exception e) {
            e.printStackTrace();
        }
		return Double.parseDouble(response.body().substring(9, 20));
		*/
		return evaluateBoard(currentState.getBoard(), currentState, moveCount);
	}
	//Basic heuristic meant to call the other types of evaluation methods to help apply weights and further tweaking to the final value
	 double evaluateBoard(int[] board, ChessGame game, int moveCount){
		double value = 1;
		value += valueOfPieces(board) * 100;
		value += pawnStructure(board, moveCount);
		//value += inCheck(game) * 5;
		value += knightValue(board);
		value += bishopValue(board) * 1;
		value += rookValue(board) * 1;
		value += queenValue(board) * 3;
		value += kingValue(board) * 100;
		//value += attackingValue(game);
		if(white) return value;
		else return value * -1;
	}
	
	/*
	 * Everything below has to do with evaluation and is pretty well defined via the name of the function
	 * I am not going to comment what each does, as I believe it is reasonable to assume anyone can figure it out by the name.
	 */
	 
	static double valueOfPieces(int[] board) {
		double value = 0;
		int[] a = {0, 1, -1, 3, -3, 3, -3, 5, -5, 9, -9, 0, 0}; //Value of the pieces
		for(int i = 0; i < 64; i++) {
			value += a[board[i]];
		}
		return value;
	}
	
	static final int[] pawnMapBeggining = {
			0, 0, 0, 0, 0, 0, 0, 0,
			1, 1, 0, 0, 0, 0, 1, 1,
			-5, 2, 5,10,10, 5, 2, -5,
			0, 0, 2,20,20, 2, 0, 0,
			0, 0, 2,20,20, 2, 0, 0,
			-5, 2, 5,10,10, 5, 2, -5,
			1, 1, 0, 0, 0, 0, 1, 1,
			0, 0, 0, 0, 0, 0, 0, 0
	};
	
	static final int[] pawnMapEnding = {
			0, 0, 0, 0, 0, 0, 0, 0,
			5, 5, 5, 5, 5, 5, 5, 5,
			4, 4, 4, 4, 4, 4, 4, 4,
			3, 3, 3, 3, 3, 3, 3, 3,
			2, 2, 2, 2, 2, 2, 2, 2,
			1, 1, 1, 1, 1, 1, 1, 1,
			-1,-1,-1,-1,-1,-1,-1,-1,
			0, 0, 0, 0, 0, 0, 0, 0
	};
	
	static double pawnStructure(int[] board, int moveCount) {
		double value = 0;
		if(moveCount < 5) {
			for(int i = 0; i < 64; i++) {
				if(board[i] == 1) {
					value += pawnMapBeggining[i];
					if(i+9 >= 64) continue;
					if(board[i+7] == 1) value += 3;
					if(board[i+9] == 1) value += 3;
					if(board[i+7] == 2) value -= 3;
					if(board[i+9] == 2) value -= 3;
				}
				else if(board[i] == 2) {
					value -= pawnMapBeggining[i];
					if(i-9 < 0) continue;
					if(board[i-7] == 2) value -= 3;
					if(board[i-9] == 2) value -= 3;
					if(board[i-7] == 2) value += 3;
					if(board[i-9] == 2) value += 3;
				}
			}
		}
		else if(moveCount > 40) {
			for(int i = 0; i < 64; i++) {
				if(board[i] == 1) {
					value += pawnMapEnding[i];
				}
				else if(board[i] == 2) {
					value -= pawnMapEnding[i];
				}
			}
		}else {
			int chainLeft = 1;
			int chainRight = 1;
			for(int i = 0; i < 64; i++) {
				if(board[i] == 1) {
					for(int j = 7; i-j >= 0; j+=7) {
						if(board[i-j] == 1) chainRight++;
						else break;
					}
					for(int j = 9; i-j >= 0; j+=9) {
						if(board[i-j] == 1) chainLeft++;
						else break;
					}
					value+= chainLeft * chainRight;
				}else if (board[i] == 2) {
					for(int j = 7; i+j < 64; j+=7) {
						if(board[i+j] == 2) chainRight++;
						else break;
					}
					for(int j = 9; i+j < 64; j+=9) {
						if(board[i+j] == 2) chainLeft++;
						else break;
					}
					value+= chainLeft * chainRight;
				}
				chainLeft = 1;
				chainRight = 1;
			}
		}
		return value;
	}
	
	static double inCheck(ChessGame game) {
		double value = 0;
		value = game.inCheck();
		return value;
	}
	
	static final int[] knightMapping = {
			-1,-3,-1,-1,-1,-1,-3,-1,
			-1, 5, 5, 5, 5, 5, 5,-1,
			-1, 5,10,10,10,10, 5,-1,
			-1, 5,10,20,20,10, 5,-1,
			-1, 5,10,20,20,10, 5,-1,
			-1, 5,10,10,10,10, 5,-1,
			-1, 5, 5, 5, 5, 5, 5,-1,
			-1,-3,-1,-1,-1,-1,-3,-1
	};
	
	static double knightValue(int[] board) {
		double value = 0;
		for(int i = 0; i < 64; i++) {
			if(board[i] == 3) value += knightMapping[i];
			else if(board[i] == 4) value -= knightMapping[i];
		}
		return value;
	}
	
	
	
	static double bishopValue(int[] board) {
		double value = 0;
		for(int i = 0; i < 64; i++) {
			if(board[i] == 5) {
				value += lineOfSightBishop(board, i/8, i%8);
			}else if(board[i] == 6) {
				value -= lineOfSightBishop(board, i/8, i%8);
			}
		}
		return value;
	}
	
	static double rookValue(int[] board) {
		double value = 0;
		for(int i = 0; i < 64; i++) {
			if(board[i] == 7) {
				value += lineOfSightRook(board, i/8, i%8);
			}else if(board[i] == 8) {
				value -= lineOfSightRook(board, i/8, i%8);
			}
		}
		return value;
	}
	
	static double queenValue(int[] board) {
		double value = 0;
		for(int i = 0; i < 64; i++) {
			if(board[i] == 9) {
				value += lineOfSightQueen(board, i/8, i%8);
			}else if(board[i] == 10) {
				value -= lineOfSightQueen(board, i/8, i%8);
			}
		}
		return value;
	}
	
	static int lineOfSightQueen(int[] board, int row, int col) {
		int spaces = 0;
		int[][] directions = {{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1},{0,1},{0,-1}};
		for(int[] dir : directions) {
			int dis = 1;
			while((row+dir[0]*dis) >= 0 && (row+dir[0]*dis) <= 7 && (col+dir[1]*dis) >= 0 && (col+dir[1]*dis) <= 7) {
				if(board[(row+dir[0]*dis)*8 + (col+dir[1]*dis)] != 0) break;
				spaces++;
				dis++;
			}
		}
		return spaces;
	}
	
	static int lineOfSightBishop(int[] board, int row, int col) {
		int spaces = 0;
		int[][] directions = {{1,1},{1,-1},{-1,1},{-1,-1}};
		for(int[] dir : directions) {
			int dis = 1;
			while((row+dir[0]*dis) >= 0 && (row+dir[0]*dis) <= 7 && (col+dir[1]*dis) >= 0 && (col+dir[1]*dis) <= 7) {
				if(board[(row+dir[0]*dis)*8 + (col+dir[1]*dis)] != 0) break;
				spaces++;
				dis++;
			}
		}
		return spaces;
	}
	
	static int lineOfSightRook(int[] board, int row, int col) {
		int spaces = 0;
		int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
		for(int[] dir : directions) {
			int dis = 1;
			while((row+dir[0]*dis) >= 0 && (row+dir[0]*dis) <= 7 && (col+dir[1]*dis) >= 0 && (col+dir[1]*dis) <= 7) {
				if(board[(row+dir[0]*dis)*8 + (col+dir[1]*dis)] != 0) break;
				spaces++;
				dis++;
			}
		}
		return spaces;
	}
	
	static final int[] kingMapping = {
			5, 5, 5, 5,-4,-4,-4, 5, 5, 5,
			-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,
			-10,-10,-10,-10,-10,-10,-10,-10,-10,-10,
			-20,-20,-20,-20,-20,-20,-20,-20,-20,-20,
			-20,-20,-20,-20,-20,-20,-20,-20,-20,-20,
			-10,-10,-10,-10,-10,-10,-10,-10,-10,-10,
			-5,-5,-5,-5,-5,-5,-5,-5,-5,-5,
			5, 5, 5, 5,-5,-5,-5, 5, 5, 5
	};
	
	static double kingValue(int[] board) {
		double value = 0;
		for(int i = 0; i < 64; i++) {
			if(board[i] == 11) value += knightMapping[i];
			else if(board[i] == 12) value -= knightMapping[i];
		}
		return value;
	}
	
	//This is the only one that may have an issue with understanding what it does. But just checks to see if you are attacking any peieces and the value of those pieces
	static double attackingValue(ChessGame game) {
		return game.attackingValue();
	}
	
}
