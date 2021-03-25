import java.awt.image.BufferedImage;

public abstract class ChessBoard {
    private static ChessPiece[][] chessPieces;
    private static boolean checkBounds(int x, int y){
        if(x>=chessPieces.length||x<0||y>=chessPieces.length||y<0) return false;
        return true;
    }

    public static ChessPiece[][] getChessPieces () {
        return chessPieces;
    }
    public static void setChessPieces (ChessPiece[][] chessPieces) {
        ChessBoard.chessPieces = chessPieces;
    }

    public abstract static class ChessPiece {
        protected int x, y;
        protected static final int WHITE_SIDE = 1;
        protected static final int BLACK_SIDE = 2;

        protected int side;
        protected BufferedImage image;

        private boolean move(int x, int y){
            if(checkKing(x, y)&&checkMove(x, y)){
                getChessPieces()[x][y] = this;
                return true;
            }
            return false;
        }
        protected abstract boolean checkMove(int x, int y);
        //Проверка для взятия на проходе
        protected boolean isPiece(){return true;}
    }

    class EnPassant extends ChessPiece {
        //Взятие на проходе(проверка, была ли там пешка)
        @Override
        protected boolean isPiece(){
            return false;
        }
        protected boolean checkMove(int x, int y){return false;}
    }

    //Пешка
    class Pawn extends ChessPiece {
        private boolean isFirstPos = true;

        @Override
        protected boolean checkMove (int x, int y) {
            //Простой ход на клетку вперед
            if ((y == this.y + 1 && this.x == x) && (chessPieces[x][y] == null)) return true;
            //Ход на клетку по диагонали с поеданием фигуры
            if ((y == this.y + 1 && x == this.x + 1 || y == this.y + 1 && x == this.x - 1) && (chessPieces[x][y] != null && chessPieces[x][y].side != this.side))
                return true;
            return false;
        }
    }
    //Ладья
    class Rook extends ChessPiece implements StraightMove{
        private boolean isFirstPos = true;
        @Override
        protected boolean checkMove (int x, int y) {
            return checkStraightMove(x, y, this.x, this.y);
        }
    }
    //Слон
    class Bishop extends ChessPiece implements DiagonalMove{
        @Override
        protected boolean checkMove (int x, int y) {
            return checkDiagonalMove(x, y, this.x, this.y);
        }
    }

    //Ходы для слонов, ладей, ферзей
    interface StraightMove{
        default boolean checkStraightMove(int x, int y, int this_X, int this_Y){
            //Проверка на препятствие
            if(x==this_X){
                int c = y<this_Y?-1:1;
                for(ChessPiece[] i:chessPieces){
                    for(int j = this_Y; j<y; j+=c){
                        if(i[j]!=null||i[j].isPiece()) return false;
                    }
                }
            }else{
                int c = x<this_X?-1:1;
                for(ChessPiece[] i:chessPieces){
                    for(int j = this_X; j<x; j+=c){
                        if(i[j]!=null||i[j].isPiece()) return false;
                    }
                }
            }
            return true;
        }
    }
    interface DiagonalMove{
        default boolean checkDiagonalMove(int x, int y, int this_X, int this_Y){
            while (this_X!=x){
                this_X++;
                this_Y++;
                if(chessPieces[this_X][this_Y]!=null||chessPieces[this_X][this_Y].isPiece()) return false;
            }
            return true;
        }
    }
}
