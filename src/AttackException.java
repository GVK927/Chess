public class AttackException extends ChessMoveException{
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }

    public AttackException(int x, int y){
        this.x = x;
        this.y = y;
    }
}
