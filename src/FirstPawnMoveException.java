public class FirstPawnMoveException extends ChessMoveException{
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public FirstPawnMoveException(int x, int y){
        this.x = x;
        this.y = y;
    }
}
