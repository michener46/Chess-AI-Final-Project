import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/*
 * Owen Michener
 * April 19th, 2024
 * This code is the chess engine used for my chess ai project.
 * This code will find all legal moves and make moves based on the string given.
 * This uses a pretty standard algebraic notation for chess
 * Like:
 * e4 -> pawn move from e file up to 4th rank
 * Nc3 -> Knight move up to position c3
 * O-O -> King's side castle
 * O-O-O -> Queen's side castle
 * BxD5 -> bishop capturing the piece on D5
 */

//The whole of the chess engine
public class ChessGame {
	chessBoard cb;
	
	ChessGame(){
		cb = new chessBoard();
	}
	
	ChessGame(int[] board, boolean whitesMove){
		cb = new chessBoard(board, whitesMove);
	}
	
	
	public int[] getBoard() {
		int[] a = new int[64];
		for(int i = 0; i < 64; i++) {
			piece p = cb.board[i/8][i%8];
			if(p instanceof pawn) {
				if(p.isWhite()) a[i] = 1;
				else a[i] = 2;
			}else if(p instanceof knight) {
				if(p.isWhite()) a[i] = 3;
				else a[i] = 4;
			}else if(p instanceof bishop) {
				if(p.isWhite()) a[i] = 5;
				else a[i] = 6;
			}else if(p instanceof rook) {
				if(p.isWhite()) a[i] = 7;
				else a[i] = 8;
			}else if(p instanceof queen) {
				if(p.isWhite()) a[i] = 9;
				else a[i] = 10;
			}else if(p instanceof king) {
				if(p.isWhite()) a[i] = 11;
				else a[i] = 12;
			}
		}
		return a;
	}
	
	public boolean getGameOver() {
		return cb.getGameOver();
	}
	
	public boolean whitesMove() {
		return cb.whiteMove;
	}
	
	public Set<String> whiteMoves(){
		return cb.whiteMoves.keySet();
	}
	
	public Set<String> blackMoves(){
		return cb.blackMoves.keySet();
	}
	
	boolean move(String move) {
		return cb.move(move);
	}
	
	public String toString() {
		return cb.toString();
	}
	
	public int inCheck() {
		if(cb.blackInCheck) return 1;
		if(cb.whiteInCheck ) return -1;
		return 0;
	}
	
	public int attackingValue() {
		return cb.attackingValue();
	}
	
	//Pretty abstract class for what a piece does
	abstract class piece{
		protected boolean color;
		protected int row;
		protected int col;
		protected boolean pinned;
		protected boolean defended;
		piece(boolean color, int row, int col){
			this.color = color;
			this.row = row;
			this.col = col;
		}
		abstract int getValue();
		abstract ArrayList<String> legalMoves(piece[][] board);
		void setColor(boolean white) { color = white; }
		boolean isWhite() { return color; }
		void setPinned(boolean pinned) {
			this.pinned = pinned;
		}
		void setDefended(boolean defended) {
			this.defended = defended;
		}
	}
	//Handles all actions that a pawn may do.
	class pawn extends piece{
		boolean enPassant = false;
		boolean left = false;
		pawn(boolean color, int row, int col) {
			super(color, row, col);
		}
		@Override
		int getValue() { return 1; }
		@Override
		public ArrayList<String> legalMoves(piece[][] board) {
	        ArrayList<String> moves = new ArrayList<>();
	        int direction = color ? 1 : -1;
	        int startRow = color ? 1 : 6;
	        int promotionRow = color ? 7 : 0;
	        
	        // Normal move forward
	        if (isValidPosition(row + direction, col) && board[row + direction][col] == null) {
	            addMove(moves, col, row + direction, false, "");
	            if (row == startRow && board[row + (2 * direction)][col] == null) {
	                addMove(moves, col, row + (2 * direction), false, "");
	            }
	        }

	        // Capture moves
	        int[] captureCols = {col - 1, col + 1};
	        for (int captureCol : captureCols) {
	            if (isValidPosition(row + direction, captureCol) && board[row + direction][captureCol] != null && board[row + direction][captureCol].isWhite() != color) {
	                String promotionSuffix = ((row + direction) == promotionRow)? "QNRB" : "";
	                addMove(moves, captureCol, row + direction, true, promotionSuffix);
	            }
	        }

	        // En passant captures
	        if (enPassant && row == (color ? 4 : 3)) {
	            for (int captureCol : captureCols) {
	                if (isValidPosition(row, captureCol) && board[row][captureCol] instanceof pawn &&
	                    board[row][captureCol].isWhite() != color) {
	                    addMove(moves, captureCol, row + direction, true, "");
	                }
	            }
	        }

	        return moves;
	    }

	    private void addMove(ArrayList<String> moves, int col, int newRow, boolean isCapture, String promotionSuffixes) {
	        char startFile = (char) ('a' + this.col);
	        char targetFile = (char) ('a' + col);
	        int targetRow = newRow + 1;
	        String move = "" + (isCapture ? startFile + "x" : "") + targetFile + targetRow;

	        if (promotionSuffixes.isEmpty()) {
	            moves.add(move);
	        } else {
	            for (char suffix : promotionSuffixes.toCharArray()) {
	                moves.add(move + suffix);
	            }
	        }
	    }

	    private boolean isValidPosition(int row, int col) {
	        return row >= 0 && row < 8 && col >= 0 && col < 8;
	    }
		public String toString() {
			if(isWhite()) return "p";
			return "P";
		}
	}
	//Handles all actions that a Knight may do
	class knight extends piece{
		knight(boolean color, int row, int col) {
			super(color, row, col);
		}
		@Override
		int getValue() { return 3; }
		@Override
		ArrayList<String> legalMoves(piece[][] board) {
			ArrayList<String> moves = new ArrayList<String>();
			int[][] directions = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{-1,2},{1,-2},{-1,-2}};
			for(int[] dir : directions) {
				if(row+dir[0] < 0 || row+dir[0] > 7 || col+dir[1] < 0 || col+dir[1] > 7) continue;
				if(board[row+dir[0]][col+dir[1]] == null) moves.add("N" + (char)(col+dir[1] + 'a') + (char)(row+dir[0]+'1'));
				else if(board[row+dir[0]][col+dir[1]].color != color) moves.add("Nx" + (char)(col+dir[1] + 'a') + (char)(row+dir[0]+'1'));
				else board[row+dir[0]][col+dir[1]].setDefended(true);
			}
			return moves;
		}
		public String toString() {
			if(isWhite()) return "n";
			return "N";
		}
	}
	//Handles all actions that a bishop may do
	class bishop extends piece{
		bishop(boolean color, int row, int col) {
			super(color, row, col);
		}
		@Override
		int getValue() { return 3; }
		@Override
		ArrayList<String> legalMoves(piece[][] board) {
			ArrayList<String> moves = new ArrayList<String>();
			boolean upRight = false;
			piece pieceUpRight = null;
			boolean upLeft = false;
			piece pieceUpLeft = null;
			boolean downRight = false;
			piece pieceDownRight = null;
			boolean downLeft = false;
			piece pieceDownLeft = null;
			boolean moveUpRight = false;
			boolean moveUpLeft = false;
			boolean moveDownRight = false;
			boolean moveDownLeft = false;
			for(int i = 1; i < 8; i++) {
				if(row+i < 8 && col+i < 8) {
					if(board[row+i][col+i] != null) {
						moveUpRight = true;
						if(!upRight && pieceUpRight == null) {
							if(board[row+i][col+i].isWhite() != isWhite()) {
								moves.add("Bx" + (char)('a'+col+i) + (char)('1'+row+i));
								pieceUpRight = board[row+i][col+i];
							}else board[row+i][col+i].setDefended(true);
							upRight = true;
						}else if(upRight && pieceUpRight != null)  {
							upRight = false;
							if(board[row+i][col+i] instanceof king) pieceUpRight.setPinned(true);
						}
					} else if(!moveUpRight) moves.add("B" + (char)('a'+col+i) + (char)('1'+row+i));
				}if(row+i < 8 && col-i >= 0) {
					if(board[row+i][col-i] != null) {
						moveUpLeft = true;
						if(!upLeft && pieceUpLeft == null) {
							if(board[row+i][col-i].isWhite() != isWhite()) {
								moves.add("Bx" + (char)('a'+col-i) + (char)('1'+row+i));
								pieceUpLeft = board[row+i][col-1];
							}else board[row+i][col-i].setDefended(true);
							upLeft = true;
						}else if(upLeft && pieceUpLeft != null) {
							upLeft = false;
							if(board[row+i][col-i] instanceof king) pieceUpLeft.setPinned(true);
						}
					} else if(!moveUpLeft) moves.add("B" + (char)('a'+col-i) + (char)('1'+row+i));
				}if(row-i >= 0 && col+i < 8) {
					if(board[row-i][col+i] != null) {
						moveDownRight = true;
						if(!downRight && pieceDownRight == null) {
							if(board[row-i][col+i].isWhite() != isWhite()) {
								moves.add("Bx" + (char)('a'+col+i) + (char)('1'+row-i));
								pieceDownRight = board[row-i][col+1];
							}else board[row-i][col+i].setDefended(true);
							downRight = true;
						}else if(downRight && pieceDownRight != null) {
							downRight = false;
							if(board[row-i][col+i] instanceof king) pieceDownRight.setPinned(true);
						}
					} else if(!moveDownRight) moves.add("B" + (char)('a'+col+i) + (char)('1'+row-i));
				}if(row-i >= 0 && col-i >= 0) {
					if(board[row-i][col-i] != null) {
						moveDownLeft = true;
						if(!downLeft && pieceDownLeft == null) {
							if(board[row-i][col-i].isWhite() != isWhite()) {
								moves.add("Bx" + (char)('a'+col-i) + (char)('1'+row-i));
								pieceDownLeft = board[row-i][col-i];
							}else board[row-i][col-i].setDefended(true);
							downLeft = true;
						} else if(downLeft && pieceDownLeft != null) {
							downLeft = false;
							if(board[row-i][col-i] instanceof king) pieceDownLeft.setPinned(true);
						}
					} else if(!moveDownLeft) moves.add("B" + (char)('a'+col-i) + (char)('1'+row-i));
				}
			}
			return moves;
		}
		public String toString() {
			if(isWhite()) return "b";
			return "B";
		}
	}
	//Handles all actions that a rook may do
	class rook extends piece{
		boolean castle = true;
		rook(boolean color, int row, int col) {
			super(color, row, col);
		}
		void setCastle(boolean castle) {
			this.castle = castle;
		}
		@Override
		int getValue() { return 5; }
		@Override
		ArrayList<String> legalMoves(piece[][] board) {
			ArrayList<String> moves = new ArrayList<String>();
			boolean up = false;
			piece pieceUp = null;
			boolean down = false;
			piece pieceDown = null;
			boolean left = false;
			piece pieceLeft = null;
			boolean right = false;
			piece pieceRight = null;
			boolean moveRight = false;
			boolean moveLeft = false;
			boolean moveUp = false;
			boolean moveDown = false;
			for(int i = 1; i < 8; i++) {
				if(row+i < 8) {
					if(board[row+i][col] != null) {
						moveUp = true;
						if(!up && pieceUp == null) {
							up = true;
							if(board[row+i][col].isWhite() != isWhite()) {
								moves.add("Rx" + (char)('a'+ col) + (char)('1' + row+i));
								pieceUp = board[row+i][col];
							}else board[row+i][col].setDefended(true);
						}else if(up && pieceUp != null) {
							up = false;
							if(board[row+i][col] instanceof king) pieceUp.setPinned(true);
						}
					}else if(!moveUp) moves.add("R" + (char)('a'+ col) + (char)('1' + row+i));
				}if(row-i >= 0) {
					if(board[row-i][col] != null) {
						moveDown = true;
						if(!down && pieceDown == null) {
							down = true;
							if(board[row-i][col].isWhite() != isWhite()) {
								moves.add("Rx" + (char)('a'+ col) + (char)('1' + row-i));
								pieceDown = board[row-i][col];
							}else board[row-i][col].setDefended(true);
						}else if (down && pieceDown != null) {
							down = false;
							if(board[row-i][col] instanceof king) pieceDown.setPinned(true);
						}
					}
					else if(!moveDown) moves.add("R" + (char)('a'+ col) + (char)('1' + row-i));
				}if(col-i >= 0) {
					if(board[row][col-i] != null) {
						moveLeft = true;
						if(!left && pieceLeft == null) {
							left = true;
							if(board[row][col-i].isWhite() != isWhite()) {
								moves.add("Rx" + (char)('a'+ col-i) + (char)('1' + row));
								pieceLeft = board[row][col-i];
							}else board[row][col-i].setDefended(true);
						}else if(left && pieceLeft != null) {
							left = false;
							if(board[row][col-i] instanceof king) pieceLeft.setPinned(true);
						}
					}
					else if(!moveLeft) moves.add("R" + (char)('a'+ col-i) + (char)('1' + row));
				}if(col+i < 8) {
					if(board[row][col+i] != null) {
						moveRight = true;
						if(!right && pieceRight == null) {
							right = true;
							if(board[row][col+i].isWhite() != isWhite()) {
								moves.add("Rx" + (char)('a'+ col+i) + (char)('1' + row));
								pieceRight = board[row][col+i];
							}else board[row][col+i].setDefended(true);
						}else if(right && pieceRight != null) {
							right = false;
							if(board[row][col+i] instanceof king) pieceRight.setPinned(true);
						}
					}
					else if(!moveRight) moves.add("R" + (char)('a'+ col+i) + (char)('1' + row));
				}
			}
			return moves;
		}
		public String toString() {
			if(isWhite()) return "r";
			return "R";
		}
	}
	//Handles all actions that a queen may do
	class queen extends piece{
		queen(boolean color, int row, int col) {
			super(color, row, col);
		}
		@Override
		int getValue() { return 9; }
		@Override
		ArrayList<String> legalMoves(piece[][] board) {
			ArrayList<String> moves = new ArrayList<String>();
			piece p = null;
			int distance = 1;
			int[][] directions = {{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1},{0,1},{0,-1}};
			for(int[] dir : directions) {
				while(row+(dir[0]*distance) >= 0 && row+(dir[0]*distance) <= 7 && col+(dir[1]*distance) >= 0 && col+(dir[1]*distance) <= 7) {
					if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] == null && p != null) {distance++; continue;}
					if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] == null && p == null) {
						moves.add("Q" + (char)(col+(dir[1]*distance)+'a') + (char)(row+(dir[0]*distance)+'1'));
					}else if(board[row+(dir[0]*distance)][col+(dir[1]*distance)].color != color && p == null) {
						moves.add("Qx" + (char)(col+(dir[1]*distance)+'a') + (char)(row+(dir[0]*distance)+'1'));
						p = board[row+(dir[0]*distance)][col+(dir[1]*distance)];
					}else if(board[row+(dir[0]*distance)][col+(dir[1]*distance)].color == color && p == null){
						board[row+(dir[0]*distance)][col+(dir[1]*distance)].setDefended(true);
						break;
					}else {
						if(board[row+(dir[0]*distance)][col+(dir[1]*distance)].color != color && board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof king) {
							p.setPinned(true);
						}else break;
					}
					distance++;
				}
				p = null;
				distance = 1;
			}
			return moves;
		}
		public String toString() {
			if(isWhite()) return "q";
			return "Q";
		}
	}
	//Handles all actions that a king may do
	class king extends piece{
		boolean castle = true;
		king(boolean color, int row, int col) {
			super(color, row, col);
		}
		void setCastle(boolean castle) {
			this.castle = castle;
		}
		@Override
		int getValue() { return 0; }
		@Override
		ArrayList<String> legalMoves(piece[][] board) {
			ArrayList<String> moves = new ArrayList<String>();
			for(int i = 0; i < 64; i++) {
				if(board[i/8][i%8] == null || !(board[i/8][i%8] instanceof king) || board[i/8][i%8].color != color) continue;
				row = i/8;
				col = i%8;
			}
			int[][] directions = {{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1},{0,1},{0,-1}};
			for(int[] dir : directions) {
				if(row+dir[0] < 0 || row+dir[0] > 7 || col+dir[1] < 0 || col+dir[1] > 7) continue;
				if(board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]].color == color) continue;
				else if(board[row+dir[0]][col+dir[1]] != null && !(board[row+dir[0]][col+dir[1]].defended)) {
					moves.add(("Kx" + (char)(col+dir[1] + 'a') + (char)(row+dir[0] + '1')));
				}
				else if(board[row+dir[0]][col+dir[1]] != null) continue;
				else {
					if(legalSpot(board, row+dir[0], col+dir[1])) {
						moves.add(("K" + (char)(col+dir[1] + 'a') + (char)(row+dir[0] + '1')));
					}
				}
			}
			if(castle) {
				if(isWhite()) {
					if(board[0][0] != null && board[0][0] instanceof rook && ((rook)board[0][0]).castle) {
						if(board[0][1] == null && board[0][2] == null && board[0][3] == null) moves.add("O-O-O");
					}if(board[0][7] != null && board[0][7] instanceof rook && ((rook)board[0][7]).castle) {
						if(board[0][6] == null && board[0][5] == null) moves.add("O-O");
					}
				}else {
					if(board[7][0] != null && board[7][0] instanceof rook && ((rook)board[7][0]).castle) {
						if(board[7][1] == null && board[7][2] == null && board[7][3] == null) moves.add("O-O-O");
					}if(board[7][7] != null && board[7][7] instanceof rook && ((rook)board[7][7]).castle) {
						if(board[7][6] == null && board[7][5] == null) moves.add("O-O");
					}
				}
			}
			return moves;
		}
		
		boolean legalSpot(piece[][] board, int row, int col) {
			int[][] directions = {{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1},{0,1},{0,-1}};
			int[][] knightDir = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{-1,2},{1,-2},{-1,-2}};
			for(int[] dir : directions) {
				int distance = 1;
				while(row+(dir[0]*distance) >= 0 && row+(dir[0]*distance) <= 7 && col+(dir[1]*distance) >= 0 && col+(dir[1]*distance) <= 7) {
					if(color) {
						if(dir[0] == 1 && dir[1] != 0 && board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]] instanceof pawn && board[row+dir[0]][col+dir[1]].color != color)return false;
					}else {
						if(dir[0] == -1 && dir[1] != 0 && board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]] instanceof pawn && board[row+dir[0]][col+dir[1]].color != color)return false;
					}
					if(board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]] instanceof king) return false;
					if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] != null && board[row+(dir[0]*distance)][col+(dir[1]*distance)].color != color) {
						if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof queen) return false;
						if(Math.abs(dir[0])+Math.abs(dir[1]) == 2) {
							if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof bishop) return false;
						}
						else {
							if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof rook) return false;
						}
					}
					distance+=1;
				}
				distance = 1;
			}
			for(int[] dir : knightDir) {
				if(row+dir[0] < 0 || row+dir[0] > 7 || col+dir[1] < 0 || col+dir[1] > 7) continue;
				if(board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]].color != color && board[row+dir[0]][col+dir[1]] instanceof knight) return false;
			}
			return true;
		}
		
		public String toString() {
			if(isWhite()) return "k";
			return "K";
		}
	}
	//The representation of the board itself and handles all of the logic
	class chessBoard{
		HashMap<String, piece> blackMoves = new HashMap<String, piece>(); //All legal black moves in chess notation
		HashMap<String, piece> whiteMoves = new HashMap<String, piece>(); //All legal white moves in chess notation
		boolean whiteMove = true; //If it is whites move or blacks
		piece[][] board = new piece[8][8];
		boolean[][] whiteLOS = new boolean[8][8]; //The line of sight of all white pieces. Used for checkmate
		boolean[][] blackLOS = new boolean[8][8]; //The line of sight of all black pieces. Used for checkmate
		boolean gameOver;
		boolean whiteInCheck = false;
		boolean blackInCheck = false;
		chessBoard(){
			resetBoard();
			findLegalMoves();
		}

		chessBoard(String[] moves){
			resetBoard();
			findLegalMoves();
			for(String s : moves) {
				move(s);
			}
		}
		
		chessBoard(int[] a, boolean whitesMove){
			whiteMove = whitesMove;
			for(int i = 0; i < 64; i++) {
				if(a[i] == 0) continue;
				else if(a[i] == 1)board[i/8][i%8] = new pawn(true, i/8, i%8);
				else if(a[i] == 2)board[i/8][i%8] = new pawn(false, i/8, i%8);
				else if(a[i] == 3)board[i/8][i%8] = new knight(true, i/8, i%8);
				else if(a[i] == 4)board[i/8][i%8] = new knight(false, i/8, i%8);
				else if(a[i] == 5)board[i/8][i%8] = new bishop(true, i/8, i%8);
				else if(a[i] == 6)board[i/8][i%8] = new bishop(false, i/8, i%8);
				else if(a[i] == 7)board[i/8][i%8] = new rook(true, i/8, i%8);
				else if(a[i] == 8)board[i/8][i%8] = new rook(false, i/8, i%8);
				else if(a[i] == 9)board[i/8][i%8] = new queen(true, i/8, i%8);
				else if(a[i] == 10)board[i/8][i%8] = new queen(false, i/8, i%8);
				else if(a[i] == 11)board[i/8][i%8] = new king(true, i/8, i%8);
				else if(a[i] == 12)board[i/8][i%8] = new king(false, i/8, i%8);
			}
			findLegalMoves();
		}
		
		//Resets the board to the starting position
		void resetBoard(){
			board = new piece[8][8];
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(i > 1 && i < 6) continue;
					if(i == 0 || i == 7) {
						if(j == 0 || j == 7) board[i][j] = new rook(i == 0, i, j); 
						else if (j == 1 || j == 6) board[i][j] = new knight(i == 0, i, j);
						else if (j == 2 || j == 5) board[i][j] = new bishop(i == 0, i, j);
						else if (j == 3) board[i][j] = new queen(i == 0, i, j);
						else board[i][j] = new king(i == 0, i, j);
					}
					else {
						board[i][j] = new pawn(i == 1, i, j);
					}
				}
			}
		}
		
		//Finds all legal moves by finding every type of move then removing anything that would be illegal like if a piece is pinned or a king capturing a defended piece
		boolean findLegalMoves(){
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(board[i][j] == null) continue;
					board[i][j].setPinned(false);
					board[i][j].setDefended(false);
				}
			}
			whiteMoves = new HashMap<String, piece>();
			blackMoves = new HashMap<String, piece>();
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(board[i][j] == null) continue;
					ArrayList<String> moves = board[i][j].legalMoves(board);
					if(board[i][j].isWhite()) {
						for(String s : moves) {
							if(!whiteMoves.containsKey(s)) whiteMoves.put(s, board[i][j]);
							else { // Ensures if there are 2 rooks or 2 bishops or 2 queens ect... able to move to the same square there is a clarification for which one is moved
								
							}
						}
					}else {
						for(String s : moves) {
							if(!blackMoves.containsKey(s)) blackMoves.put(s, board[i][j]);
							else {
								
							}
						}
					}
				}
			}
			whiteLOS = new boolean[8][8];
			blackLOS = new boolean[8][8];
			whiteInCheck = false;
			blackInCheck = false;
			ArrayList<piece> piecesChecking = new ArrayList<piece>();
			//Finds the line of sight for white
			Iterator<String> iterator = whiteMoves.keySet().iterator();
			while (iterator.hasNext()) {
			    String s = iterator.next();
				piece p = whiteMoves.get(s);
				if(p.pinned) {
					iterator.remove();
					continue;
				}
				if(s.charAt(0) == 'O') continue;
				int col = s.charAt(s.length()-2)-'a';
				int row = s.charAt(s.length()-1)-'1';
				if(row > 7 || row < 0) {
					col = s.charAt(s.length()-3)-'a';
					row = s.charAt(s.length()-2)-'1';
				}
				if(board[row][col] instanceof king) { 
					blackInCheck = true;
					piecesChecking.add(p);
				}
				whiteLOS[row][col] = true;
			}
			//Finds the line of sight for black
			iterator = blackMoves.keySet().iterator();
			while (iterator.hasNext()) {
			    String s = iterator.next();
				piece p = blackMoves.get(s);
				if(p.pinned) {
					iterator.remove();
					continue;
				}
				if(s.charAt(0) == 'O') continue;
				int col = s.charAt(s.length()-2)-'a';
				int row = s.charAt(s.length()-1)-'1';
				if(row > 7 || row < 0) {
					col = s.charAt(s.length()-3)-'a';
					row = s.charAt(s.length()-2)-'1';
				}
				if(row < 0 || row >= 8 || col < 0 || col >= 8) {
					iterator.remove(); 
					continue;
				}
				if(board[row][col] instanceof king) {
					whiteInCheck = true;
					piecesChecking.add(p);
				}
				blackLOS[row][col] = true;
			}
			
			//Checks for castling through check
			if(whiteMoves.containsKey("O-O") && !blackLOS[0][5] && !blackLOS[0][6]) whiteMoves.remove("O-O");
			if(whiteMoves.containsKey("O-O-o") && !blackLOS[0][3] && !blackLOS[0][2]) whiteMoves.remove("O-O-O");
			if(blackMoves.containsKey("O-O") && !whiteLOS[7][5] && !whiteLOS[7][6]) blackMoves.remove("O-O");
			if(blackMoves.containsKey("O-O-O") && !whiteLOS[7][3] && !whiteLOS[7][2]) blackMoves.remove("O-O-O");
			
			//This will find the squares we must move to to protect the king or capture the piece checking
			boolean[][] lineOfCheck = new boolean[8][8];
			if(piecesChecking.size() == 1) {
				piece p = piecesChecking.get(0);
				piece k = null;
				for(int i = 0; i < 64; i++) {
					if(board[i/8][i%8] instanceof king && board[i/8][i%8].isWhite() != p.isWhite()) {
						k = board[i/8][i%8];
						break;
					}
				}
				if(p.row == k.row) {
					if(p.col > k.col) { for(int i = 0; i < p.col-k.col; i++) if(p.col+i < 8)lineOfCheck[p.row][p.col+i] = true;}
					else {for(int i = 0; i < k.col-p.col; i++) if(p.col-i >= 0)lineOfCheck[p.row][p.col-i] = true;}
				}else if(p.col == k.col) {
					if(p.row > k.row) {for(int i = 0; i < p.row-k.row; i++) if(p.row+i < 8)lineOfCheck[p.row+i][p.col] = true;}
					else {for(int i = 0; i < k.row-p.row; i++) if(p.row-i >= 0)lineOfCheck[p.row-i][p.col] = true;}
				}else if(p.col < k.col && p.row < k.row) {
					for(int i = 0; i < k.col-p.col; i++) {if(p.col+i < 8 && p.row+i < 8)lineOfCheck[p.row+i][p.col+i] = true;}
				}else if(p.col > k.col && p.row < k.row) {
					for(int i = 0; i < p.col-k.col; i++) {if(p.row-i >= 0 && p.col+i < 8)lineOfCheck[p.row-i][p.col+i] = true;}
				}else if(p.col < k.col && p.row > k.row) {
					for(int i = 0; i < k.col-p.col; i++) {if(p.col-i >= 0 && p.row+i < 8)lineOfCheck[p.row+i][p.col-i] = true;}
				}else {
					for(int i = 0; i < p.col-k.col; i++) {if(p.row-i >= 0 && p.col-i >= 0)lineOfCheck[p.row-i][p.col-i] = true;}
				}
			}
			setPinned();
			iterator = whiteMoves.keySet().iterator();
			while (iterator.hasNext()) {
			    String s = iterator.next();
			    piece p = whiteMoves.get(s);
			    if(p.pinned) iterator.remove();
			    else if (s.charAt(0) == 'K') {
			        if (whiteLOS[s.charAt(s.length() - 1) - '1'][s.charAt(s.length() - 2) - 'a'] || (board[s.charAt(s.length() - 1) - '1'][s.charAt(s.length() - 2) - 'a'] != null && board[s.charAt(s.length() - 1) - '1'][s.charAt(s.length() - 2) - 'a'].defended)) {
			            iterator.remove();
			            continue;
			        }
			    }
			}
			setDefended();
			//Removes all moves that do not get out of check
			if(whiteInCheck) {
				whiteMoves.remove("O-O-O");
				whiteMoves.remove("O-O");
				iterator = whiteMoves.keySet().iterator();
				while (iterator.hasNext()) {
				    String s = iterator.next();
				    if (piecesChecking.size() > 1) {
				        iterator.remove();
				        continue;
				    } else {
				    	int row = s.charAt(s.length() - 1) - '1';
				    	int col = s.charAt(s.length() - 2) - 'a';
				    	if(row < 0 || row > 7) {
				    		row = s.charAt(s.length() - 2) - '1';
					    	col = s.charAt(s.length() - 3) - 'a';
				    	}if(piecesChecking.get(0).col == col && piecesChecking.get(0).row == row && !(whiteMoves.get(s) instanceof king)) continue;
				        if (!lineOfCheck[row][col] && !(whiteMoves.get(s) instanceof king)) {
				            iterator.remove();
				        }
				    }
				}
				//fixKingLegalMoves(true);
			}
			
			iterator = blackMoves.keySet().iterator();
			while (iterator.hasNext()) {
			    String s = iterator.next();
			    piece p = blackMoves.get(s);
			    if(p.pinned) iterator.remove();
			    else if (s.charAt(0) == 'K') {
			        if (whiteLOS[s.charAt(s.length() - 1) - '1'][s.charAt(s.length() - 2) - 'a'] || (board[s.charAt(s.length() - 1) - '1'][s.charAt(s.length() - 2) - 'a'] != null && board[s.charAt(s.length() - 1) - '1'][s.charAt(s.length() - 2) - 'a'].defended)) {
			            iterator.remove();
			            continue;
			        }
			    }
			}
			
			if(blackInCheck) {
				blackMoves.remove("O-O-O");
				blackMoves.remove("O-O");
				iterator = blackMoves.keySet().iterator();
				while (iterator.hasNext()) {
				    String s = iterator.next();
				    if (piecesChecking.size() > 1) {
				        iterator.remove();
				        continue;
				    } else {
				    	int r = s.charAt(s.length() - 1) - '1';
				    	int c = s.charAt(s.length() - 2) - 'a';
				    	if(r > 7 || r < 0) {
				    		r = s.charAt(s.length() - 2) - '1';
					    	c = s.charAt(s.length() - 3) - 'a';
				    	}if(piecesChecking.get(0).col == c && piecesChecking.get(0).row == r && !(blackMoves.get(s) instanceof king)) continue;
				        if (!lineOfCheck[r][c] && !(blackMoves.get(s) instanceof king)) {
				            iterator.remove();
				        }
				    }
				}
				//fixKingLegalMoves(false);
			}	
			fixKingLegalMoves(true);
			fixKingLegalMoves(false);
			gameOver = (whiteInCheck && whiteMoves.isEmpty()) || (blackInCheck && blackMoves.isEmpty());
			return gameOver;
		}
		
		//Sets the pieces to defended if they are defended. This was used more for just testing if things were being set properly or if some pieces weren't working properly.
		void setDefended() {
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if(board[i][j] == null) continue;
					if(board[i][j] instanceof pawn) {
						if(board[i][j].color) {
							if(i+1 != 8) {
								if(j+1 < 8 && board[i+1][j+1] != null && board[i+1][j+1].color == board[i][j].color) board[i+1][j+1].setDefended(true);
								if(j-1 >= 0 && board[i+1][j-1] != null && board[i+1][j-1].color == board[i][j].color) board[i+1][j-1].setDefended(true);
							}
						}else {
							if(i-1 >= 0) {
								if(j+1 < 8 && board[i-1][j+1] != null && board[i-1][j+1].color == board[i][j].color) board[i-1][j+1].setDefended(true);
								if(j-1 >= 0 && board[i-1][j-1] != null && board[i-1][j-1].color == board[i][j].color) board[i-1][j-1].setDefended(true);
							}
						}
					}
				}
			}
		}
		
		//Does what it sounds like. I had some errors with a king sometimes capturing a defended piece or moving into the line of sight of a piece
		void fixKingLegalMoves(boolean white) {
			int row = 0;
			int col = 0;
			for(int i = 0; i < 64; i++) {
				if(board[i/8][i%8] == null || !(board[i/8][i%8] instanceof king) || board[i/8][i%8].color != white) continue;
				row = i/8;
				col = i%8;
			}
			int[][] directions = {{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1},{0,1},{0,-1}};
			for(int[] dir : directions) {
				if(row+dir[0] < 0 || row+dir[0] > 7 || col+dir[1] < 0 || col+dir[1] > 7) continue;
				if(board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]].color == white) continue;
				else if(board[row+dir[0]][col+dir[1]] != null && !(board[row+dir[0]][col+dir[1]].defended)) {
					if(white) whiteMoves.put(("Kx" + (char)(col+dir[1] + 'a') + (char)(row+dir[0] + '1')), board[row][col]);
					else blackMoves.put(("Kx" + (char)(col+dir[1] + 'a') + (char)(row+dir[0] + '1')), board[row][col]);
				}
				else {
					if(legalSpot(row+dir[0], col+dir[1], white)) {
						if(white) whiteMoves.put(("K" + (char)(col+dir[1] + 'a') + (char)(row+dir[0] + '1')), board[row][col]);
						else blackMoves.put(("K" + (char)(col+dir[1] + 'a') + (char)(row+dir[0] + '1')), board[row][col]);
					}
				}
			}
		}
		//Helper function for fixKingLegalMoves. Checks to see if the square is safe or not.
		boolean legalSpot(int row, int col, boolean white) {
			int[][] directions = {{1,0},{1,1},{1,-1},{-1,0},{-1,1},{-1,-1},{0,1},{0,-1}};
			int[][] knightDir = {{2,1},{2,-1},{-2,1},{-2,-1},{1,2},{-1,2},{1,-2},{-1,-2}};
			for(int[] dir : directions) {
				int distance = 1;
				while(row+(dir[0]*distance) >= 0 && row+(dir[0]*distance) <= 7 && col+(dir[1]*distance) >= 0 && col+(dir[1]*distance) <= 7) {
					if(white) {
						if(dir[0] == 1 && dir[1] != 0 && board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]] instanceof pawn && board[row+dir[0]][col+dir[1]].color != white)return false;
					}else {
						if(dir[0] == -1 && dir[1] != 0 && board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]] instanceof pawn && board[row+dir[0]][col+dir[1]].color != white)return false;
					}
					if(board[row+dir[0]][col+dir[1]] != null && board[row+dir[0]][col+dir[1]] instanceof king) return false;
					if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] != null && board[row+(dir[0]*distance)][col+(dir[1]*distance)].color != white) {
						if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof queen) return false;
						if(Math.abs(dir[0])+Math.abs(dir[1]) == 2) {
							if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof bishop) return false;
						}
						else {
							if(board[row+(dir[0]*distance)][col+(dir[1]*distance)] instanceof rook) return false;
						}
					}
					distance+=1;
				}
				distance = 1;
			}
			for(int[] dir : knightDir) {
				if(row+dir[0] < 0 || row+dir[0] > 7 || col+dir[1] < 0 || col+dir[1] > 7) continue;
				if(board[row+dir[0]][col+dir[1]] == null) continue;
				if(board[row+dir[0]][col+dir[1]].color == white) continue;
				if(board[row+dir[0]][col+dir[1]] instanceof knight) return false;
			}
			return true;
		}
		//Handles the movement based on the move string given
		boolean move(String move) {
			piece p = null;
			//Pawn Movement
			if(whiteMove) p = whiteMoves.get(move);
			else p = blackMoves.get(move);
			if(whiteMove && p == null) {
				//System.out.println(whiteMoves); 
				return false;
			}
			else if(!whiteMove && p == null) {
				//System.out.println(blackMoves); 
				return false;
			}
			
			int col = move.charAt(move.length()-2)-'a';
			int row = move.charAt(move.length()-1)-'1';
			if(p instanceof rook) ((rook)p).setCastle(false);
			if(p instanceof king) ((king)p).setCastle(false);
			if(p instanceof pawn && Math.abs(p.row - row) == 2) {
				if(col != 7 && board[row][col+1] != null && board[row][col+1] instanceof pawn && board[row][col+1].isWhite() != p.isWhite()) {
					((pawn)board[row][col+1]).enPassant = true;
					((pawn)board[row][col+1]).left = true;
				}
				if(col != 0 && board[row][col-1] != null && board[row][col-1] instanceof pawn && board[row][col-1].isWhite() != p.isWhite()) {
					((pawn)board[row][col-1]).enPassant = true;
					((pawn)board[row][col-1]).left = false;
				}
			}
			else {
				for(int i = 0; i < 64; i++) {
					if(board[i%8][i/8] != null && board[i%8][i/8] instanceof pawn) {
						((pawn)board[i%8][i/8]).enPassant = false;
					}
				}
			}
			if(move.charAt(0) == 'O') {
				if(p.isWhite()) {
					if(move.length() > 3) {
						movePiece(board[0][0], 0, 3);
						movePiece(p, 0, 2);
					}else {
						movePiece(board[0][7], 0, 5);
						movePiece(p, 0, 6);
					}
				}else {
					if(move.length() > 3) {
						movePiece(board[7][0], 7, 3);
						movePiece(p, 7, 2);
					}else {
						movePiece(board[7][7], 7, 5);
						movePiece(p, 7, 6);
					}
				}
			}
			else if(p instanceof pawn && move.charAt(move.length()-1) - 'A' > 0) {
				int r = p.row;
				int c = p.col;
				if(move.charAt(move.length()-1) == 'Q') board[p.row][p.col] = new queen(p.color, p.row, p.col);
				else if(move.charAt(move.length()-1) == 'N') board[p.row][p.col] = new knight(p.color, p.row, p.col);
				else if(move.charAt(move.length()-1) == 'B') board[p.row][p.col] = new bishop(p.color, p.row, p.col);
				else if(move.charAt(move.length()-1) == 'R') board[p.row][p.col] = new rook(p.color, p.row, p.col);
				movePiece(board[r][c], move.charAt(move.length()-3) - 'a', move.charAt(move.length()-2) - '1');
			}
			else if(p instanceof pawn && board[row][col] == null && move.charAt(1) == 'x') {
				if(whiteMove) {
					board[row-1][col] = null;
				}else {
					board[row+1][col] = null;
				}
				movePiece(p, row, col);
			}
			else movePiece(p, row, col);
			boolean game = findLegalMoves();
			/*
			if(game) {
				System.out.println("CHECKMATE! " + (whiteMove? "White" : "Black") + " Wins!!!!");
			}
			*/
			whiteMove = !whiteMove;
			return true;
		}
		//Helper function to move the piece to the row and col
		void movePiece(piece p, int row, int col) {
			board[p.row][p.col] = null;
			board[row][col] = p;
			p.row = row;
			p.col = col;
		}
		
		public boolean getGameOver() {
			return gameOver;
		}
		//Meant for the chess AI
		public int attackingValue() {
			int value = 0;
			Set<String> moves = whiteMoves.keySet();
			for(String s : moves) {
				if(s.contains("x")) {
					int row = s.charAt(s.length()-1) - '1';
					int col = s.charAt(s.length()-2) - 'a';
					if(row < 0 || row > 8) {
						row = s.charAt(s.length()-2) - '1';
						col = s.charAt(s.length()-3) - 'a';
					}
					value += board[row][col].getValue();
				}
			}
			moves = blackMoves.keySet();
			for(String s : moves) {
				if(s.contains("x")) {
					int row = s.charAt(s.length()-1) - '1';
					int col = s.charAt(s.length()-2) - 'a';
					if(row < 0 || row > 8) {
						row = s.charAt(s.length()-2) - '1';
						col = s.charAt(s.length()-3) - 'a';
					}
					value -= board[row][col].getValue();
				}
			}
			return value;
		}
		//Just like set defended this was used for testing purposes
		public void setPinned() {
			for(int i = 0; i < 8; i++) {
				for(int j = 0; j < 8; j++) {
					if (board[i][j] != null && board[i][j] instanceof king) propagate(i, j, board[i][j].color);
				}
			}
		}
		//Helper function to setpinned so the nested code was not too large.
		private void propagate(int row, int col, boolean color) {
			piece left = null;
			piece right = null;
			piece up = null;
			piece down = null;
			piece leftUp = null;
			piece leftDown = null;
			piece rightUp = null;
			piece rightDown = null;
			boolean leftSearch = false;
			boolean rightSearch = false;
			boolean upSearch = false;
			boolean downSearch = false;
			boolean leftUpSearch = false;
			boolean leftDownSearch = false;
			boolean rightUpSearch = false;
			boolean rightDownSearch = false;
			for(int i = 1; i < 8; i++) {
				if(!upSearch && row + i < 8) {
					if(board[row+i][col] != null && board[row+i][col].color == color && up != null) upSearch = true;
					if(board[row+i][col] != null && board[row+i][col].color == color) up = board[row+i][col];
					else if(board[row+i][col] != null && up != null && board[row+i][col].color != color) {
						if(board[row+i][col] instanceof queen || board[row+i][col] instanceof rook) up.pinned = true;
					}
				}if(!rightUpSearch && row + i < 8 && col + i < 8) {
					if(board[row+i][col+i] != null && board[row+i][col+i].color == color && rightUp != null) rightUpSearch = true;
					if(board[row+i][col+i] != null && board[row+i][col+i].color == color) rightUp = board[row+i][col+i];
					else if(board[row+i][col+i] != null && rightUp != null && board[row+i][col+i].color != color) {
						if(board[row+i][col+i] instanceof queen || board[row+i][col+i] instanceof bishop) rightUp.pinned = true;
					}
				}if(!leftUpSearch && row + i < 8 && col - i >= 0) {
					if(board[row+i][col-i] != null && board[row+i][col-i].color == color && leftUp != null) leftUpSearch = true;
					if(board[row+i][col-i] != null && board[row+i][col-i].color == color) leftUp = board[row+i][col-i];
					else if(board[row+i][col-i] != null && leftUp != null && board[row+i][col-i].color != color) {
						if(board[row+i][col-i] instanceof queen || board[row+i][col-i] instanceof bishop) leftUp.pinned = true;
					}
				}if(!downSearch && row - i >= 0) {
					if(board[row-i][col] != null && board[row-i][col].color == color && down != null) downSearch = true;
					if(board[row-i][col] != null && board[row-i][col].color == color) down = board[row-i][col];
					else if(board[row-i][col] != null && down != null && board[row-i][col].color != color) {
						if(board[row-i][col] instanceof queen || board[row-i][col] instanceof rook) down.pinned = true;
					}
				}if(!rightDownSearch && row - i >= 0 && col + i < 8) {
					if(board[row-i][col+i] != null && board[row-i][col+i].color == color && rightDown != null) rightDownSearch = true;
					if(board[row-i][col+i] != null && board[row-i][col+i].color == color) rightDown = board[row-i][col+i];
					else if(board[row-i][col+i] != null && rightDown != null && board[row-i][col+i].color != color) {
						if(board[row-i][col+i] instanceof queen || board[row-i][col] instanceof bishop) rightDown.pinned = true;
					}
				}if(!leftDownSearch && row - i >= 0 && col - i >= 0) {
					if(board[row-i][col-i] != null && board[row-i][col-i].color == color && leftDown != null) leftDownSearch = true;
					if(board[row-i][col-i] != null && board[row-i][col-i].color == color) leftDown = board[row-i][col-i];
					else if(board[row-i][col-i] != null && leftDown != null && board[row-i][col-i].color != color) {
						if(board[row-i][col-i] instanceof queen || board[row-i][col-i] instanceof bishop) leftDown.pinned = true;
					}
				}if(!leftSearch && col - i >= 0) {
					if(board[row][col-i] != null && board[row][col-i].color == color && left != null) leftSearch = true;
					if(board[row][col-i] != null && board[row][col-i].color == color) left = board[row][col-i];
					else if(board[row][col-i] != null && left != null && board[row][col-i].color != color) {
						if(board[row][col-i] instanceof queen || board[row][col-i] instanceof rook) left.pinned = true;
					}
				}if(!rightSearch && col + i < 8) {
					if(board[row][col+i] != null && board[row][col+i].color == color && right != null) rightSearch = true;
					if(board[row][col+i] != null && board[row][col+i].color == color) right = board[row][col+i];
					else if(board[row][col+i] != null && right != null && board[row][col+i].color != color) {
						if(board[row][col+i] instanceof queen || board[row][col+i] instanceof rook) right.pinned = true;
					}
				}
			}
		}
		
		public String toString() {
			String s = "";
			for(int i = 0; i < 8; i++) {
				s += (8-i) + "|";
				for(int j = 0; j < 8; j++) {
					if(board[7-i][j] == null) s += " |";
					else s += board[7-i][j] + "|";
				}
				s += '\n';
			}
			s += "  ---------------\n";
			s += "  a b c d e f g h";
			return s;
		}
	}
}
