import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

/*
 * Owen Michener
 * chessTest.java
 * April 19th, 2024
 * This program is a simple program to test the ChessGame.java and chessAI.java files
 * Within this code it currently pins two AI against each other, and I believe the game will end about 
 * 25 moves in because white looses all of its pieces.
 */

public class chessTest {
	public static void main(String[] args) {
		Scanner s = new Scanner(System.in);
		ChessGame game = new ChessGame();
		chessAI ai1 = new chessAI(true);
		chessAI ai2 = new chessAI(false);
		String input = "";
		int moves = 0;
		while(!game.getGameOver()) {
			if(game.whitesMove()) input = ai1.getBestMove(game);
			else input = ai2.getBestMove(game);
			if(input == null) break;
			moves++;
			System.out.println(input);
			//else while(s.hasNext()) {input = s.next(); break;}
			game.move(input);
			System.out.println(game);
		}
		System.out.println(game.inCheck());
		System.out.println(game.blackMoves());
		System.out.println(game.whiteMoves());
		System.out.println("White moves: " + (moves/2 + moves%2) + " Black moves: " + (moves/2));
		if(moves%2 == 1) System.out.println("White wins!");
		else System.out.println("Black wins!");
		/*
		Random r = new Random();
		chessAI ai = new chessAI();
		while(!game.getGameOver()) {
			if(game.whitesMove()) {
				game.move(ai.getBestMove(game));
			}
			else {
				Set<String> moves = game.blackMoves();
				int randomIndex = r.nextInt(moves.size());
			    int i = 0;
			    String move = null;
			    for (String e : moves) {
			        if (i == randomIndex) { move = e; break;}
			        i++;
			    }
			    if(move != null) game.move(move);
			}
			System.out.println(game);
		}
		*/
		s.close();
	}
}
