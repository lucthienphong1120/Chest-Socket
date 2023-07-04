package caro;

public class StatusBoard {

    int height;
    private int width;
    public int[][] statusBoard;
    private int[][] saveBoard;

    public StatusBoard(int height, int width) {
        this.height = height;
        this.width = width;
        statusBoard = new int[height][width];
        saveBoard = new int[height][width];
    }

    public void initilizeStatus() { // khởi tạo ban đầu
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                statusBoard[i][j] = 0;
            }
        }
    }

    public void setStatus(int row, int col, int player) { // thiết lập một trạng thái
        statusBoard[row][col] = player;
    }

    public void saveStatus() {
        for (int row = 0; row < height; row++) {
            System.arraycopy(statusBoard[row], 0, saveBoard[row], 0, width);
        }
    }

    public void loadStatus() {
        for (int row = 0; row < height; row++) {
            System.arraycopy(saveBoard[row], 0, statusBoard[row], 0, width);
        }
    }
}
