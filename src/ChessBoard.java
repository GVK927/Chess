import java.awt.image.BufferedImage;
import java.util.Arrays;

public abstract class ChessBoard {
    private static ChessPiece[][] chessPieces;

    //Заполнение доски
    {
        //Генерация белых фигур
        chessPieces = new ChessPiece[8][8];
        chessPieces[0] = new ChessPiece[]{
                new Rook(0, 0, ChessPiece.WHITE_SIDE),
                new Knight(1,0, ChessPiece.WHITE_SIDE),
                new Bishop(2,0, ChessPiece.WHITE_SIDE),
                new Queen(3,0, ChessPiece.WHITE_SIDE),
                new King(4,0, ChessPiece.WHITE_SIDE),
                new Bishop(5,0, ChessPiece.WHITE_SIDE),
                new Knight(6,0, ChessPiece.WHITE_SIDE),
                new Rook(7,0, ChessPiece.WHITE_SIDE)
        };
        int c = 0;
        for(ChessPiece i:chessPieces[1]){
            i = new Pawn(c,1, ChessPiece.WHITE_SIDE);
            c++;
        }
        //Генерация черных
        chessPieces = new ChessPiece[8][8];
        chessPieces[7] = new ChessPiece[]{
                new Rook(0, 7, ChessPiece.BLACK_SIDE),
                new Knight(1, 7, ChessPiece.BLACK_SIDE),
                new Bishop(2, 7, ChessPiece.BLACK_SIDE),
                new Queen(3, 7, ChessPiece.BLACK_SIDE),
                new King(4, 7, ChessPiece.BLACK_SIDE),
                new Bishop(5, 7, ChessPiece.BLACK_SIDE),
                new Knight(6, 7, ChessPiece.BLACK_SIDE),
                new Rook(7, 7, ChessPiece.BLACK_SIDE)
        };
        int d = 0;
        for(ChessPiece i:chessPieces[6]){
            i = new Pawn(d,6, ChessPiece.BLACK_SIDE);
            c++;
        }
    }

    public abstract class ChessPiece {
        protected int x, y;
        protected static final int WHITE_SIDE = 1;
        protected static final int BLACK_SIDE = 2;

        protected int side;
        protected BufferedImage image;

        protected boolean move(int piece_x, int piece_y, int x, int y) {
            try {
                if (checkKing(this, x, y) && chessPieces[piece_y][piece_x].checkMove(x, y)){
                    chessPieces[y][x] = this;
                    return true;
                }
            } catch (AttackException e) {
                chessPieces[e.getY()][e.getX()] = this;
                chessPieces[y][x] = null;
                return true;
            }
            catch (FirstPawnMoveException e) {
                chessPieces[e.getY()][e.getX()] = this;
                chessPieces[e.getY()-1][e.getX()] = new EnPassant(e.getX(), e.getY(), chessPieces[piece_y][piece_x].side);
            }
            catch (PromotionException e){
                //Выбор фигуры
            }
            return false;
        }

        protected ChessPiece(int x, int y, int side) {
            this.x = x;
            this.y = y;
            this.side = side;
        }
        protected abstract boolean checkMove(int x, int y) throws AttackException, FirstPawnMoveException, PromotionException;

        //Проверка для взятия на проходе
        protected boolean isPiece() {
            return true;
        }

        //Проверка на поедание (не для пешки)
        protected boolean checkAttack(int x, int y) {
            if (chessPieces[y][x] != null && chessPieces[y][x].side != this.side) return true;
            return false;
        }
        //Проверка на союзную фигуру в месте хода
        protected boolean checkAlly(int x, int y) {
            if (chessPieces[y][x].isPiece() && chessPieces[y][x].side == this.side) return true;
            return false;
        }

        //Проверка на мат после хода
        private boolean checkKing(ChessPiece piece, int x, int y) {
            for (ChessPiece[] i : chessPieces) {
                for (ChessPiece j : i) {
                    if (j.getClass().getName().equals("King")&&j.side==this.side) {
                        ChessPiece[][] chessPieces_ = Arrays.copyOf(chessPieces, chessPieces.length);
                        chessPieces[piece.y][piece.x]=null;
                        chessPieces[y][x]=piece;
                        if(checkMoves(j.x, j.y)){
                            chessPieces=Arrays.copyOf(chessPieces_, chessPieces_.length);
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        private boolean checkMoves(int king_x, int king_y) {
            for (ChessPiece[] i : chessPieces) {
                for (ChessPiece j : i) {
                    try {
                        if (!j.checkMove(king_x, king_y)) return false;
                    } catch (AttackException e) {
                    } catch (FirstPawnMoveException e) {
                    } catch (PromotionException e) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    //Взятие на проходе
    class EnPassant extends ChessPiece{
        @Override
        protected boolean checkMove(int x, int y) {return false;}
        @Override
        protected boolean isPiece(){return false;}
        EnPassant(int x, int y, int side){
            super(x, y, side);
        }
    }

    //Пешка
    class Pawn extends ChessPiece {
        private boolean isFirstPos = true;

        @Override
        protected boolean checkMove (int x, int y) throws AttackException, FirstPawnMoveException, PromotionException{
            //Превращение пешки
            if((y == chessPieces.length && this.x == x) && (chessPieces[y][x] == null)) throw new PromotionException(x, y);
            //Простой ход на клетку вперед
            if ((y == this.y + 1 && this.x == x) && (chessPieces[y][x] == null)) return true;
            //Ход на две клетки
            if ((y == this.y + 2) && this.isFirstPos && (chessPieces[y][x] == null&&chessPieces[y-1][x] == null)) throw new FirstPawnMoveException(x, y);
            //Ход на клетку по диагонали с поеданием фигуры
            if ((y == this.y + 1 && x == this.x + 1 || y == this.y + 1 && x == this.x - 1) && (chessPieces[x][y] != null && chessPieces[x][y].side != this.side)) throw new AttackException(x, y);
            return false;
        }

        Pawn(int x, int y, int side){
            super(x, y, side);
        }
    }
    //Ладья
    class Rook extends ChessPiece implements StraightMove{
        private boolean isFirstPos = true;
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            //Проверка на препятствие
            if(!checkStraightMove(x, y, this.x, this.y)) return false;
            //Проверка на поедание
            if(checkAttack(x, y)) throw new AttackException(x, y);
            //Проверка на союзную фигуру в месте хода
            return !checkAlly(x, y);
        }

        Rook(int x, int y, int side){
            super(x, y, side);
        }
    }
    //Слон
    class Bishop extends ChessPiece implements DiagonalMove{
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            //Проверка на препятствие
            if(!checkDiagonalMove(x, y, this.x, this.y)) return false;
            //Проверка на поедание
            if(checkAttack(x, y)) throw new AttackException(x, y);
            //Проверка на союзную фигуру в месте хода
            return !checkAlly(x, y);
        }

        Bishop(int x, int y, int side){
            super(x, y, side);
        }
    }
    //Конь
    class Knight extends ChessPiece{
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            if(chessPieces[y][x]==null) return true;
            if(checkAttack(x, y)) throw new AttackException(x, y);
            return !checkAlly(x, y);
        }

        Knight(int x, int y, int side){
            super(x, y, side);
        }
    }
    //Ферзь
    class Queen extends ChessPiece implements StraightMove, DiagonalMove{
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            if(!(checkDiagonalMove(x, y, this.x, this.y)&&checkStraightMove(x, y, this.x, this.y))) return false;
            if(checkAttack(x, y)) throw new AttackException(x, y);
            return !checkAlly(x, y);
        }

        Queen(int x, int y, int side){
            super(x, y, side);
        }
    }
    //Король
    class King extends ChessPiece{
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            for(ChessPiece[] i:chessPieces){
                for(ChessPiece j:i){
                    if(j.side==this.side) continue;
                    //Проверка ходов всех фигур, кроме короля
                    try {
                        if(j.checkMove(x, y)&&!chessPieces[y][x].getClass().getName().equals("King")); return false;
                    }catch (AttackException e){}
                    catch (FirstPawnMoveException e){}
                    catch (PromotionException e) {
                        if (e.x == this.x && e.y == this.y) return false;
                    }
                }
            }
            //Проверка для вражеского короля
            for(int i = 0; i<3; i++){
                for(int j = 0; j<3; j++){
                    if(i==1&&j==1) continue;
                    try {
                        if(chessPieces[i][j].getClass().getName().equals("King")) return false;
                    }catch (ArrayIndexOutOfBoundsException e){}
                }
            }
            return true;
        }

        King(int x, int y, int side){
            super(x, y, side);
        }
    }

    //Ходы для слонов, ладей, ферзей
    interface StraightMove{
        default boolean checkStraightMove(int x, int y, int this_X, int this_Y){
            //Проверка на препятствие
            if(x==this_X){
                int c = y<this_Y?-1:1;
                for(ChessPiece[] i:chessPieces){
                    for(int j = this_Y+c; j<y; j+=c){
                        if(i[j]!=null&&i[j].isPiece()) return false;
                    }
                }
            }else{
                int c = x<this_X?-1:1;
                for(ChessPiece[] i:chessPieces){
                    for(int j = this_X+c; j<x; j+=c){
                        if(i[j]!=null||i[j].isPiece()) return false;
                    }
                }
            }
            return true;
        }
    }
    interface DiagonalMove{
        default boolean checkDiagonalMove(int x, int y, int this_X, int this_Y){
            //Проверки на препяствие
            //При движении вправо вверх
            if(x>this_X&&y>this_Y){
                while (this_X<x){
                    this_X++;
                    this_Y++;
                    if(chessPieces[this_X][this_Y]!=null&&chessPieces[this_X][this_X].isPiece()) return false;
                }
            }
            //При движении влево вверх
            if(x<this_X&&y>this_Y){
                while (this_X>x){
                    this_X--;
                    this_Y++;
                    if(chessPieces[this_X][this_Y]!=null&&chessPieces[this_X][this_X].isPiece()) return false;
                }
            }
            //При движении вправо вниз
            if(x>this_X&&y<this_Y){
                while (this_X<x){
                    this_X++;
                    this_Y--;
                    if(chessPieces[this_X][this_Y]!=null&&chessPieces[this_X][this_X].isPiece()) return false;
                }
            }
            //При движении влево вниз
            if(x<this_X&&y<this_Y){
                while (this_X>x){
                    this_X--;
                    this_Y--;
                    if(chessPieces[this_X][this_Y]!=null&&chessPieces[this_X][this_X].isPiece()) return false;
                }
            }
            return true;
        }
    }
}
