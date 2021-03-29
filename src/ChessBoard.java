import java.awt.image.BufferedImage;

public abstract class ChessBoard {
    private static ChessPiece[][] chessPieces;
    private static boolean checkBounds(int x, int y){
        if(x>=chessPieces.length||x<0||y>=chessPieces.length||y<0) return false;
        return true;
    }

    public abstract static class ChessPiece {
        protected int x, y;
        protected static final int WHITE_SIDE = 1;
        protected static final int BLACK_SIDE = 2;

        protected int side;
        protected BufferedImage image;

        private boolean move(int x, int y){
            try {
                if (checkKing(x, y) && checkMove(x, y)) {
                    chessPieces[y][x] = this;
                    return true;
                }
            }catch (AttackException e){
                chessPieces[e.getX()][e.getY()] = null;
                return true;
            }catch (FirstPawnMoveException e){

            }
            return false;
        }
        protected abstract boolean checkMove(int x, int y) throws AttackException, FirstPawnMoveException, PromotionException;
        //Проверка для взятия на проходе
        protected boolean isPiece(){return true;}
        //Проверка на поедание (не для пешки)
        protected boolean checkAttack(int x, int y){
            if(chessPieces[y][x]!=null&&chessPieces[y][x].side!=this.side) return true;
            return false;
        }
        //Проверка на союзную фигуру в месте хода
        protected boolean checkAlly(int x, int y){
            if(chessPieces[y][x].isPiece()&&chessPieces[y][x].side==this.side) return true;
            return false;
        }
    }

    //Взятие на проходе
    class EnPassant extends ChessPiece{
        @Override
        protected boolean checkMove(int x, int y) {return false;}
        @Override
        protected boolean isPiece(){return false;}
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
    }
    //Конь
    class Knight extends ChessPiece{
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            if(chessPieces[y][x]==null) return true;
            if(checkAttack(x, y)) throw new AttackException(x, y);
            return !checkAlly(x, y);
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
    }
    //Король
    class King extends ChessPiece{
        @Override
        protected boolean checkMove (int x, int y) throws AttackException{
            for(ChessPiece[] i:chessPieces){
                for(ChessPiece j:i){
                    if(j.side==this.side) continue;
                    try {
                        if(j.checkMove(x, y)&&!chessPieces[y][x].getClass().getName().equals("King")); return false;
                    }catch (ChessMoveException e){
                        if(e.x==this.x&&e.y==this.y) return false;
                    }
                }
            }
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
