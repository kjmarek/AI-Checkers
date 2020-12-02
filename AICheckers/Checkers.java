import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

/**
 * @author Kyle Marek, source of GUI - http://math.hws.edu/eck/cs124/javanotes6/source/Checkers.java
 */

public class Checkers extends JPanel {

    public static void main(String[] args) {
        JFrame window = new JFrame("Checkers");
        Checkers content = new Checkers();
        window.setContentPane(content);
        window.pack();
        Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
        window.setLocation( (screensize.width - window.getWidth())/2,
                (screensize.height - window.getHeight())/2 );
        window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
    }

    private JButton newGameButton;
    private JButton resignButton;

    private JLabel message;

    public Checkers() {
        setLayout(null);
        setPreferredSize( new Dimension(350,250) );
        setBackground(new Color(0,150,0));
        Board board = new Board();
        add(board);
        add(newGameButton);
        add(resignButton);
        add(message);
        board.setBounds(20,20,164,164); // Note:  size MUST be 164-by-164 !
        newGameButton.setBounds(210, 60, 120, 30);
        resignButton.setBounds(210, 120, 120, 30);
        message.setBounds(0, 200, 350, 30);
    }

    private static class CheckersMove {
        int fromRow, fromCol;  // Position of piece to be moved.
        int toRow, toCol;      // Square it is to move to.
        CheckersData ownBoard;
        CheckersMove(int r1, int c1, int r2, int c2) {
            fromRow = r1;
            fromCol = c1;
            toRow = r2;
            toCol = c2;
        }
        CheckersMove(int r1, int c1, int r2, int c2, int[][] board) {
            fromRow = r1;
            fromCol = c1;
            toRow = r2;
            toCol = c2;
            ownBoard = new CheckersData(board);
        }
        boolean isJump() {
            return (fromRow - toRow == 2 || fromRow - toRow == -2);
        }
    }

    private class Board extends JPanel implements ActionListener, MouseListener {

        CheckersData board;
        boolean gameInProgress;
        int currentPlayer;
        int selectedRow, selectedCol;
        CheckersMove[] legalMoves;

        Board() {
            setBackground(Color.BLACK);
            addMouseListener(this);
            resignButton = new JButton("Resign");
            resignButton.addActionListener(this);
            newGameButton = new JButton("New Game");
            newGameButton.addActionListener(this);
            message = new JLabel("",JLabel.CENTER);
            message.setFont(new  Font("Serif", Font.BOLD, 14));
            message.setForeground(Color.green);
            board = new CheckersData();
            doNewGame();
        }

        public void actionPerformed(ActionEvent evt) {
            Object src = evt.getSource();
            if (src == newGameButton)
                doNewGame();
            else if (src == resignButton)
                doResign();
        }

        void doNewGame() {
            if (gameInProgress == true) {
                message.setText("Finish the current game first!");
                return;
            }
            board.setUpGame();
            currentPlayer = CheckersData.RED;
            legalMoves = board.getLegalMovesHuman(CheckersData.RED);
            selectedRow = -1;
            message.setText("Red:  Make your move.");
            gameInProgress = true;
            newGameButton.setEnabled(false);
            resignButton.setEnabled(true);
            repaint();
        }

        void doResign() {
            if (gameInProgress == false) {
                message.setText("There is no game in progress!");
                return;
            }
            if (currentPlayer == CheckersData.RED)
                gameOver("RED resigns.  BLACK wins.");
            else
                gameOver("BLACK resigns.  RED wins.");
        }

        void gameOver(String str) {
            message.setText(str);
            newGameButton.setEnabled(true);
            resignButton.setEnabled(false);
            gameInProgress = false;
        }

        void doClickSquare(int row, int col) {
            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == row && legalMoves[i].fromCol == col) {
                    selectedRow = row;
                    selectedCol = col;
                    if (currentPlayer == CheckersData.RED)
                        message.setText("RED:  Make your move.");
                    else
                        message.setText("BLACK:  Make your move.");
                    repaint();
                    return;
                }

            if (selectedRow < 0) {
                message.setText("Click the piece you want to move.");
                return;
            }

            for (int i = 0; i < legalMoves.length; i++)
                if (legalMoves[i].fromRow == selectedRow && legalMoves[i].fromCol == selectedCol
                        && legalMoves[i].toRow == row && legalMoves[i].toCol == col) {
                    doMakeMove(legalMoves[i]);
                    return;
                }

            message.setText("Click the square you want to move to.");

        }

        void letsPlayBall() {
            CheckersMove move;
            int movesTaken = 0;
            while (gameInProgress == true) {
                movesTaken++;
                if (movesTaken > 400) {
                    gameOver("Draw !");
                    repaint();
                }
                if (currentPlayer == CheckersData.RED) {
                    legalMoves = board.getLegalMovesAI(currentPlayer);
                    if (legalMoves == null) {
                        repaint();
                        gameOver("RED has no moves. BLACK wins.");
                    }

                    if (legalMoves.length == 0) {
                        gameOver("Black wins!");
                        repaint();
                    }

                    move = makeDecision(currentPlayer, board, 5); // use alpha-beta pruning to find best move

                    board = move.ownBoard;
                    Checkers.this.repaint();

                    currentPlayer = CheckersData.BLACK; // AI took turn, flip back to correct player
                }
                else if (currentPlayer == CheckersData.BLACK) {
                    legalMoves = board.getLegalMovesAI(currentPlayer);
                    if (legalMoves == null) {
                        repaint();
                        gameOver("BLACK has no moves.  RED wins.");
                    }

                    if (legalMoves.length == 0) {
                        gameOver("Red wins!");
                        repaint();
                    }

                    move = makeDecision(currentPlayer, board, 5); // use alpha-beta pruning to find best move

                    board = move.ownBoard;
                    Checkers.this.repaint();

                    currentPlayer = CheckersData.RED; // AI took turn, flip back to correct player
                }
            }
        }

        void doMakeMove(CheckersMove move) {
            board.makeMove(move);

            if (move.isJump()) {
                legalMoves = board.getJumpsFromSpot(currentPlayer,move.toRow,move.toCol);
                if (legalMoves != null) {
                    if (currentPlayer == CheckersData.RED)
                        message.setText("RED:  You must continue jumping.");
                    else
                        message.setText("BLACK:  You must continue jumping.");
                    selectedRow = move.toRow;  // Since only one piece can be moved, select it.
                    selectedCol = move.toCol;
                    repaint();
                    return;
                }
            }

            if (currentPlayer == CheckersData.RED) {
                repaint();
                currentPlayer = CheckersData.BLACK;
                legalMoves = board.getLegalMovesAI(currentPlayer);
                if (legalMoves == null) {
                    repaint();
                    gameOver("BLACK has no moves.  RED wins.");
                }
                    message.setText("BLACK:  Make your move.");

                if (legalMoves.length == 0) {
                    gameOver("Red wins!");
                    repaint();
                }

                move = makeDecision(currentPlayer, board, 5); // use alpha-beta pruning to find best move
                System.out.println("AI's move - fromRow: " + move.fromRow + " fromCol: " + move.fromCol + " toRow: " + move.toRow + " toCol: " + move.toCol);
                board = move.ownBoard;

                currentPlayer = CheckersData.RED; // AI took turn, flip back to correct player
                legalMoves = board.getLegalMovesHuman(currentPlayer); // get other players legal moves
                if (legalMoves == null)
                    gameOver("RED has no moves.  BLACK wins.");
                else if (legalMoves[0].isJump())
                    message.setText("RED:  Make your move.  You must jump.");
                else
                    message.setText("RED:  Make your move.");
            }
            else {
                currentPlayer = CheckersData.RED;
                legalMoves = board.getLegalMovesHuman(currentPlayer);
                if (legalMoves == null)
                    gameOver("RED has no moves.  BLACK wins.");
                else if (legalMoves[0].isJump())
                    message.setText("RED:  Make your move.  You must jump.");
                else
                    message.setText("RED:  Make your move.");
            }

            selectedRow = -1;

            if (legalMoves != null) {
                boolean sameStartSquare = true;
                for (int i = 1; i < legalMoves.length; i++)
                    if (legalMoves[i].fromRow != legalMoves[0].fromRow
                            || legalMoves[i].fromCol != legalMoves[0].fromCol) {
                        sameStartSquare = false;
                        break;
                    }
                if (sameStartSquare) {
                    selectedRow = legalMoves[0].fromRow;
                    selectedCol = legalMoves[0].fromCol;
                }
            }

            repaint();
        }


        // Evaluation function for board state from a players point of view
        public double getResult(CheckersData data, int player) {
            int[][] board = data.board;
            double value = 0.0;
            int redCount = 0;
            int redKing = 0;
            int blackCount = 0;
            int blackKing = 0;
            int canGetJumpRed = 0;
            int canGetJumpBlack = 0;
            for(int i = 0; i < board.length; i++) {
                for(int j = 0; j < board[i].length; j++) {
                    if (board[i][j] == CheckersData.RED) {
                        if (data.jumpability(CheckersData.RED, i, j, data.board)) {
                            canGetJumpRed = canGetJumpRed + 1;
                        }
                        redCount++;
                    }
                    if (board[i][j] == CheckersData.RED_KING) {
                        if (data.jumpability(CheckersData.RED, i, j, data.board)) {
                            canGetJumpRed = canGetJumpRed + 1;
                        }
                        redKing++;
                    }
                    if (board[i][j] == CheckersData.BLACK) {
                        if (data.jumpability(CheckersData.BLACK, i, j, data.board)) {
                            canGetJumpBlack = canGetJumpBlack + 1;
                        }
                        blackCount++;
                    }
                    if (board[i][j] == CheckersData.BLACK_KING) {
                        if (data.jumpability(CheckersData.BLACK, i, j, data.board)) {
                            canGetJumpBlack = canGetJumpBlack + 1;
                        }
                        blackKing++;
                    }
                }
            }
            if(player == CheckersData.RED || player == CheckersData.RED_KING){ // evaluation of values for RED player board
                value = (redCount - blackCount) + (1.5 * redKing - 1.5 * blackKing) + (canGetJumpRed - canGetJumpBlack); // better to have less of black, bigger difference between, more kings
                if (blackCount + blackKing == 0) { // if no blacks it will win
                    value = value + 50;
                }
            }
            else { // evaluation of values for BLACK player board
                value = (blackCount - redCount) + (1.5 * blackKing - 1.5 * redKing) + (canGetJumpBlack - canGetJumpRed); // better to have less of red, bigger difference between, more kings
                if (redCount + redKing == 0) { // if no reds it will win
                    value = value + 50;
                }
            }
            return value;
        }

        public CheckersMove makeDecision(int player, CheckersData data, int depth) {
            CheckersMove result = null;
            double resultValue = Double.NEGATIVE_INFINITY;
            for (CheckersMove action : data.getLegalMovesAI(player)) { // for each move black can do
                double value = minValue(action.ownBoard, player, // get minValue of move red can do with new board
                        Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, depth);
                if (value > resultValue) { // find maximum result of the minimums
                    result = action;
                    resultValue = value;
                }
            }
            if (result != null) {
                return result;
            }
            else {
                return data.getLegalMovesAI(player)[0];
            }
        }

        public double maxValue(CheckersData state, int player, double alpha, double beta, int depth) {
            if (player == CheckersData.RED) { // flip which players turn it is
                player = CheckersData.BLACK;
            }
            else {
                player = CheckersData.RED;
            }
            if (depth == 0) // if depth is up
                return getResult(state, player); // return end result
            double value = Double.NEGATIVE_INFINITY;
            CheckersMove[] listOfMoves = state.getLegalMovesAI(player); // get all legalMoves by the player
            if (listOfMoves != null) {
                for (CheckersMove action : listOfMoves) {
                    value = Math.max(value, minValue( // get minValue of move with new board
                            action.ownBoard, player, alpha, beta, depth));
                    if (value >= beta) // find maximum result of the minimums
                        return value;
                    alpha = Math.max(alpha, value);
                }
            }
            return value;
        }

        public double minValue(CheckersData state, int player, double alpha, double beta, int depth) {
            if (player == CheckersData.RED) { // flip which players turn it is
                player = CheckersData.BLACK;
            }
            else {
                player = CheckersData.RED;
            }
            if (depth == 0) // if depth is up
                return getResult(state, player); // return end result of these moves
            double value = Double.POSITIVE_INFINITY;
            CheckersMove[] listOfMoves = state.getLegalMovesAI(player); // get all legalMoves by the player
            if (listOfMoves != null) {
                for (CheckersMove action : listOfMoves) {
                    value = Math.min(value, maxValue( // find maxValue of move with new board
                            action.ownBoard, player, alpha, beta, depth - 1)); // decrease the depth
                    if (value <= alpha) // find minimum result of the maximums
                        return value;
                    beta = Math.min(beta, value);
                }
            }
            return value;
        }

        public void paintComponent(Graphics g) {
            g.setColor(Color.black);
            g.drawRect(0,0,getSize().width-1,getSize().height-1);
            g.drawRect(1,1,getSize().width-3,getSize().height-3);

            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 )
                        g.setColor(Color.LIGHT_GRAY);
                    else
                        g.setColor(Color.GRAY);
                    g.fillRect(2 + col*20, 2 + row*20, 20, 20);
                    switch (board.pieceAt(row,col)) {
                        case CheckersData.RED:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.BLACK:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            break;
                        case CheckersData.RED_KING:
                            g.setColor(Color.RED);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                        case CheckersData.BLACK_KING:
                            g.setColor(Color.BLACK);
                            g.fillOval(4 + col*20, 4 + row*20, 15, 15);
                            g.setColor(Color.WHITE);
                            g.drawString("K", 7 + col*20, 16 + row*20);
                            break;
                    }
                }
            }

            if (gameInProgress) {
                g.setColor(Color.cyan);
                for (int i = 0; i < legalMoves.length; i++) {
                    g.drawRect(2 + legalMoves[i].fromCol*20, 2 + legalMoves[i].fromRow*20, 19, 19);
                    g.drawRect(3 + legalMoves[i].fromCol*20, 3 + legalMoves[i].fromRow*20, 17, 17);
                }

                if (selectedRow >= 0) {
                    g.setColor(Color.white);
                    g.drawRect(2 + selectedCol*20, 2 + selectedRow*20, 19, 19);
                    g.drawRect(3 + selectedCol*20, 3 + selectedRow*20, 17, 17);
                    g.setColor(Color.green);
                    for (int i = 0; i < legalMoves.length; i++) {
                        if (legalMoves[i].fromCol == selectedCol && legalMoves[i].fromRow == selectedRow) {
                            g.drawRect(2 + legalMoves[i].toCol*20, 2 + legalMoves[i].toRow*20, 19, 19);
                            g.drawRect(3 + legalMoves[i].toCol*20, 3 + legalMoves[i].toRow*20, 17, 17);
                        }
                    }
                }
            }

        }

        public void mousePressed(MouseEvent evt) {
            if (gameInProgress == false)
                message.setText("Click \"New Game\" to start a new game.");
            else {
                int col = (evt.getX() - 2) / 20;
                int row = (evt.getY() - 2) / 20;
                if (col >= 0 && col < 8 && row >= 0 && row < 8)
                    doClickSquare(row,col);
                 	//letsPlayBall(); // use letsPlayBall if you want two AI's to fight each other have to click on the board for it to run
            }
        }

        public void mouseReleased(MouseEvent evt) { }
        public void mouseClicked(MouseEvent evt) { }
        public void mouseEntered(MouseEvent evt) { }
        public void mouseExited(MouseEvent evt) { }

    }

    private static class CheckersData {

        static final int
                EMPTY = 0,
                RED = 1,
                RED_KING = 2,
                BLACK = 3,
                BLACK_KING = 4;

        int[][] board;  // board[r][c] is the contents of row r, column c.

        CheckersData() {
            board = new int[8][8];
            setUpGame();
        }

        CheckersData(int[][] oldBoard) {
            board = new int[8][8];
            for (int i = 0; i < oldBoard.length; i++) {
                for (int j = 0; j < oldBoard[i].length; j++) {
                    board[i][j] = oldBoard[i][j];
                }
            }
        }

        void setUpGame() {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if ( row % 2 == col % 2 ) {
                        if (row < 3)
                            board[row][col] = BLACK;
                        else if (row > 4)
                            board[row][col] = RED;
                        else
                            board[row][col] = EMPTY;
                    }
                    else {
                        board[row][col] = EMPTY;
                    }
                }
            }
        }

        int pieceAt(int row, int col) {
            return board[row][col];
        }

        void makeMove(CheckersMove move) {
            makeMove(move.fromRow, move.fromCol, move.toRow, move.toCol);
        }

        void makeMove(int fromRow, int fromCol, int toRow, int toCol) {
            board[toRow][toCol] = board[fromRow][fromCol];
            board[fromRow][fromCol] = EMPTY;
            if (fromRow - toRow == 2 || fromRow - toRow == -2) {
                // The move is a jump.  Remove the jumped piece from the board.
                int jumpRow = (fromRow + toRow) / 2;  // Row of the jumped piece.
                int jumpCol = (fromCol + toCol) / 2;  // Column of the jumped piece.
                board[jumpRow][jumpCol] = EMPTY;
            }
            if (toRow == 0 && board[toRow][toCol] == RED)
                board[toRow][toCol] = RED_KING;
            if (toRow == 7 && board[toRow][toCol] == BLACK)
                board[toRow][toCol] = BLACK_KING;
        }

        CheckersMove[] getLegalMovesHuman(int player) {

            if (player != RED && player != BLACK)
                return null;

            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;

            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();


            // if it can jump make it jump
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        if (canJumpFromSpot(player, row, col, row+1, col+1, row+2, col+2))
                            moves.add(new CheckersMove(row, col, row+2, col+2));
                        if (canJumpFromSpot(player, row, col, row-1, col+1, row-2, col+2))
                            moves.add(new CheckersMove(row, col, row-2, col+2));
                        if (canJumpFromSpot(player, row, col, row+1, col-1, row+2, col-2))
                            moves.add(new CheckersMove(row, col, row+2, col-2));
                        if (canJumpFromSpot(player, row, col, row-1, col-1, row-2, col-2))
                            moves.add(new CheckersMove(row, col, row-2, col-2));
                    }
                }
            }

            // otherwise see other moves
            if (moves.size() == 0) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == player || board[row][col] == playerKing) {
                            if (canMoveFromSpot(player,row,col,row+1,col+1))
                                moves.add(new CheckersMove(row,col,row+1,col+1));
                            if (canMoveFromSpot(player,row,col,row-1,col+1))
                                moves.add(new CheckersMove(row,col,row-1,col+1));
                            if (canMoveFromSpot(player,row,col,row+1,col-1))
                                moves.add(new CheckersMove(row,col,row+1,col-1));
                            if (canMoveFromSpot(player,row,col,row-1,col-1))
                                moves.add(new CheckersMove(row,col,row-1,col-1));
                        }
                    }
                }
            }

            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }
        }

        CheckersMove[] getLegalMovesAI(int player) {

            if (player != RED && player != BLACK)
                return null;

            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;

            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();

            // check for jumps
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    if (board[row][col] == player || board[row][col] == playerKing) {
                        moves = getJumperoni(player, playerKing, row, col, board, moves);
                    }
                }
            }

            // if no jumps check for other moves, update board as moves can be made
            if (moves.size() == 0) {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        if (board[row][col] == player || board[row][col] == playerKing) {
                            if (canMoveFromSpot(player,row,col,row+1,col+1)) {
                                CheckersData jumpBoard = new CheckersData(board); // make new Checkers Data to store in final moves
                                jumpBoard.makeMove(new CheckersMove(row, col, row + 1, col + 1)); // make the jump
                                moves.add(new CheckersMove(row, col, row + 1, col + 1, jumpBoard.board));
                            }
                            if (canMoveFromSpot(player,row,col,row-1,col+1)) {
                                CheckersData jumpBoard = new CheckersData(board); // make new Checkers Data to store in final moves
                                jumpBoard.makeMove(new CheckersMove(row, col, row - 1, col + 1)); // make the jump
                                moves.add(new CheckersMove(row, col, row - 1, col + 1, jumpBoard.board));
                            }
                            if (canMoveFromSpot(player,row,col,row+1,col-1)) {
                                CheckersData jumpBoard = new CheckersData(board); // make new Checkers Data to store in final moves
                                jumpBoard.makeMove(new CheckersMove(row, col, row + 1, col - 1)); // make the jump
                                moves.add(new CheckersMove(row, col, row + 1, col - 1, jumpBoard.board));
                            }
                            if (canMoveFromSpot(player,row,col,row-1,col-1)) {
                                CheckersData jumpBoard = new CheckersData(board); // make new Checkers Data to store in final moves
                                jumpBoard.makeMove(new CheckersMove(row, col, row - 1, col - 1)); // make the jump
                                moves.add(new CheckersMove(row, col, row - 1, col - 1, jumpBoard.board));
                            }
                        }
                    }
                }
            }

            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }

        }

        ArrayList<CheckersMove> getJumperoni(int player, int playerKing, int row, int col, int[][] board, ArrayList<CheckersMove> moves) { // calculates all possible jump moves and returns arraylist of moves
            if (canJumpOwnBoard(player, row, col, row + 1, col + 1, row + 2, col + 2, board)) {
                CheckersData jumpBoard = new CheckersData(board); // make new Checkers Data to store in final moves
                jumpBoard.makeMove(new CheckersMove(row, col, row + 2, col + 2)); // make the jump
                if (jumpability(player, row + 2, col + 2, jumpBoard.board)) { // see if another jump can be made
                    moves = getJumperoni(player, playerKing, row + 2, col + 2, jumpBoard.board, moves); // recursive if another jump can be made
                } else {
                    moves.add(new CheckersMove(row, col, row + 2, col + 2, jumpBoard.board)); // add move with end result board
                }
            }
            if (canJumpOwnBoard(player, row, col, row - 1, col + 1, row - 2, col + 2, board)) {
                CheckersData jumpBoard = new CheckersData(board);
                jumpBoard.makeMove(new CheckersMove(row, col, row - 2, col + 2));
                if (jumpability(player, row - 2, col + 2, jumpBoard.board)) {
                    moves = getJumperoni(player, playerKing, row - 2, col + 2, jumpBoard.board, moves);
                } else {
                    moves.add(new CheckersMove(row, col, row - 2, col + 2, jumpBoard.board));
                }
            }
            if (canJumpOwnBoard(player, row, col, row + 1, col - 1, row + 2, col - 2, board)) {
                CheckersData jumpBoard = new CheckersData(board);
                jumpBoard.makeMove(new CheckersMove(row, col, row + 2, col - 2));
                if (jumpability(player, row + 2, col - 2, jumpBoard.board)) {
                    moves = getJumperoni(player, playerKing, row + 2, col - 2, jumpBoard.board, moves);
                } else {
                    moves.add(new CheckersMove(row, col, row + 2, col - 2, jumpBoard.board));
                }
            }
            if (canJumpOwnBoard(player, row, col, row - 1, col - 1, row - 2, col - 2, board)) {
                CheckersData jumpBoard = new CheckersData(board);
                jumpBoard.makeMove(new CheckersMove(row, col, row - 2, col - 2));
                if (jumpability(player, row - 2, col - 2, jumpBoard.board)) {
                    moves = getJumperoni(player, playerKing, row - 2, col - 2, jumpBoard.board, moves);
                } else {
                    moves.add(new CheckersMove(row, col, row - 2, col - 2, jumpBoard.board));
                }
            }
            return moves;
        }

        Boolean jumpability(int player, int row, int col, int[][] board) { // tests if player can jump from row column on given board
            boolean response = false;
            if (canJumpOwnBoard(player, row, col, row + 1, col + 1, row + 2, col + 2, board)) {
                response = true;
            }
            if (canJumpOwnBoard(player, row, col, row - 1, col + 1, row - 2, col + 2, board)) {
                response = true;
            }
            if (canJumpOwnBoard(player, row, col, row + 1, col - 1, row + 2, col - 2, board)) {
                response = true;
            }
            if (canJumpOwnBoard(player, row, col, row - 1, col - 1, row - 2, col - 2, board)) {
                response = true;
            }
            return response;
        }

        CheckersMove[] getJumpsFromSpot(int player, int row, int col) {
            if (player != RED && player != BLACK)
                return null;
            int playerKing;
            if (player == RED)
                playerKing = RED_KING;
            else
                playerKing = BLACK_KING;
            ArrayList<CheckersMove> moves = new ArrayList<CheckersMove>();
            if (board[row][col] == player || board[row][col] == playerKing) {
                if (canJumpFromSpot(player, row, col, row+1, col+1, row+2, col+2))
                    moves.add(new CheckersMove(row, col, row+2, col+2));
                if (canJumpFromSpot(player, row, col, row-1, col+1, row-2, col+2))
                    moves.add(new CheckersMove(row, col, row-2, col+2));
                if (canJumpFromSpot(player, row, col, row+1, col-1, row+2, col-2))
                    moves.add(new CheckersMove(row, col, row+2, col-2));
                if (canJumpFromSpot(player, row, col, row-1, col-1, row-2, col-2))
                    moves.add(new CheckersMove(row, col, row-2, col-2));
            }
            if (moves.size() == 0)
                return null;
            else {
                CheckersMove[] moveArray = new CheckersMove[moves.size()];
                for (int i = 0; i < moves.size(); i++)
                    moveArray[i] = moves.get(i);
                return moveArray;
            }
        }

        private boolean canJumpFromSpot(int player, int r1, int c1, int r2, int c2, int r3, int c3) {

            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false;

            if (board[r3][c3] != EMPTY)
                return false;

            if (player == RED) {
                if (board[r1][c1] == RED && r3 > r1)
                    return false;
                if (board[r2][c2] != BLACK && board[r2][c2] != BLACK_KING)
                    return false;
                return true;
            }
            else {
                if (board[r1][c1] == BLACK && r3 < r1)
                    return false;
                if (board[r2][c2] != RED && board[r2][c2] != RED_KING)
                    return false;
                return true;
            }

        }

        private boolean canJumpOwnBoard(int player, int r1, int c1, int r2, int c2, int r3, int c3, int[][] ownBoard) {

            if (r3 < 0 || r3 >= 8 || c3 < 0 || c3 >= 8)
                return false;

            if (ownBoard[r3][c3] != EMPTY)
                return false;

            if (player == RED) {
                if (ownBoard[r1][c1] == RED && r3 > r1)
                    return false;
                if (ownBoard[r2][c2] != BLACK && ownBoard[r2][c2] != BLACK_KING)
                    return false;
                return true;
            }
            else {
                if (ownBoard[r1][c1] == BLACK && r3 < r1)
                    return false;
                if (ownBoard[r2][c2] != RED && ownBoard[r2][c2] != RED_KING)
                    return false;
                return true;
            }

        }

        private boolean canMoveFromSpot(int player, int r1, int c1, int r2, int c2) {

            if (r2 < 0 || r2 >= 8 || c2 < 0 || c2 >= 8)
                return false;

            if (board[r2][c2] != EMPTY)
                return false;

            if (player == RED) {
                if (board[r1][c1] == RED && r2 > r1)
                    return false;
                return true;
            }
            else {
                if (board[r1][c1] == BLACK && r2 < r1)
                    return false;
                return true;
            }
        }
    }
}