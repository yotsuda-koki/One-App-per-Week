package io.github.some_example_name;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Random;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    enum GameState {
        MENU,
        PLAYING,
        GAME_OVER,
    }

    GameState gameState = GameState.MENU;

    public static final int ROWS = 20;
    public static final int COLS = 10;
    public static final int CELL_SIZE = 32;

    SpriteBatch batch;
    BitmapFont font;
    BitmapFont subFont;

    ShapeRenderer shapeRenderer;

    int[][] board;

    int[][] currentPiece = {
        {1,1},
        {1,1}
    };

    int[][] nextPiece;

    int offsetX;
    int offsetY;

    int pieceRow = 17;
    int pieceCol = 4;

    long lastFallTime;
    long normalFallInterval = 500;
    long softDropInterval = 50;
    long fallInterval = normalFallInterval;

    int score = 0;
    int linesCleared = 0;
    int level = 1;

    String playerName = "";
    boolean enteringName = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font  = new BitmapFont();
        font.getData().setScale(4);
        subFont = new BitmapFont();
        subFont.getData().setScale(1);
        shapeRenderer = new ShapeRenderer();

        board = new int[ROWS][COLS];

        lastFallTime = TimeUtils.millis();

        offsetX = (Gdx.graphics.getWidth() - COLS * CELL_SIZE) / 2;
        offsetY = 0;

        nextPiece = getRandomPiece();
        spawnNewPiece();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        switch(gameState) {
            case MENU:
                renderMenu();
                break;
            case PLAYING:
                renderGame();
                break;
            case GAME_OVER:
                renderGame();
                renderGameOver();
                break;
        }

        if (gameState == GameState.MENU && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            gameState = GameState.PLAYING;
        }

        if(gameState == GameState.PLAYING){
            handleInput();
            updateFall();
        }

        if(gameState == GameState.GAME_OVER){
            if(!enteringName) enteringName = true;
            handleNameInput();
        }

        if (gameState == GameState.GAME_OVER && !enteringName && Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            restartGame();
        }
    }

    private void renderMenu() {
        batch.begin();

        String text = "Hello tetris!";
        String subText = "press enter to start.";
        GlyphLayout layout = new GlyphLayout(font, text);
        GlyphLayout subLayout = new GlyphLayout(subFont, subText);

        float x = (Gdx.graphics.getWidth() - layout.width) / 2;
        float y = (Gdx.graphics.getHeight() - layout.height) / 4 * 3;

        font.draw(batch, layout, x, y);

        float s = (Gdx.graphics.getWidth() - subLayout.width) / 2;
        float t = (Gdx.graphics.getHeight() - subLayout.height) / 2;
        subFont.draw(batch, subLayout, s, t);

        batch.end();
    }

    private void renderGame() {
        batch.begin();
        drawBoard();
        drawCurrentPiece();
        drawNextPiece();
        drawScore();
        batch.end();
    }

    private void renderGameOver() {
        batch.begin();

        String text = "GAME OVER";
        String subText = enteringName ? "Enter your name: " + playerName : "press enter to restart.";
        GlyphLayout layout = new GlyphLayout(font, text);
        GlyphLayout subLayout = new GlyphLayout(subFont, subText);

        float x = (Gdx.graphics.getWidth() - layout.width) / 2;
        float y = (Gdx.graphics.getHeight() - layout.height) / 4 * 3;

        font.draw(batch, layout, x, y);

        float s = (Gdx.graphics.getWidth() - subLayout.width) / 2;
        float t = (Gdx.graphics.getHeight() - subLayout.height) / 2;
        subFont.draw(batch, subLayout, s, t);

        float sx = offsetX;
        float sy = 100;
        subFont.draw(batch, "High Scores:", sx, sy + 60);
        java.util.ArrayList<String> scores = loadScores();
        for (int i = 0; i < scores.size(); i++) {
            subFont.draw(batch, scores.get(i), sx, sy - i * 20);
        }

        batch.end();
    }

    private void drawBoard() {
        for (int row = 0; row < ROWS; row++){
            for (int col = 0; col < COLS; col++){
                int val = board[row][col];
                float x = offsetX + col * CELL_SIZE;
                float y = offsetY + row * CELL_SIZE;

                if (val != 0) {
                    drawColoredRect(x, y, val, 0.4f);
                } else {
                    drawRect(x, y, CELL_SIZE, CELL_SIZE, 0.2f, 0.2f, 0.2f, 1f);
                    drawGridLine(x, y, CELL_SIZE, CELL_SIZE);
                }
            }
        }
    }

    private void drawColoredRect(float x, float y, int type) {
        drawColoredRect(x, y, type, 1f);
    }

    private void drawColoredRect(float x, float y, int type, float alpha) {
        float r = 1, g = 1, b = 1;

        switch(type){
            case 1: r = 0f; g = 1f; b = 1f; break;
            case 2: r = 1f; g = 1f; b = 0f; break;
            case 3: r = 0.6f; g = 0f; b = 1f; break;
            case 4: r = 1f; g = 0.5f; b = 0f; break;
            case 5: r = 0f; g = 0f; b = 1f; break;
            case 6: r = 0f; g = 1f; b = 0f; break;
            case 7: r = 1f; g = 0f; b = 0f; break;
        }

        drawRect(x, y, CELL_SIZE, CELL_SIZE, r, g, b, alpha);

        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0, 0, 0, 1);
        shapeRenderer.rect(x, y, CELL_SIZE, CELL_SIZE);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
    }

    private void drawRect(float x, float y, float w, float h, float r, float g, float b, float a) {
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(r, g, b, a);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.begin();
    }

    private void drawScore() {
        font.getData().setScale(1.5f);

        float baseX = COLS * CELL_SIZE - 300;
        float baseY = ROWS * CELL_SIZE - 180;

        font.draw(batch, "Score: " + score, baseX, baseY);
        font.draw(batch, "Lines: " + linesCleared, baseX, baseY - 30);
        font.draw(batch, "Level: " + level, baseX, baseY - 60);

        font.getData().setScale(3);
    }

    Random random = new Random();

    int[][][] tetrominoes = {

        {
            {1, 1, 1, 1}
        },

        {
            {2, 2},
            {2, 2}
        },

        {
            {0, 3, 0},
            {3, 3, 3}
        },

        {
            {4, 0},
            {4, 0},
            {4, 4}
        },

        {
            {0, 5},
            {0, 5},
            {5, 5}
        },

        {
            {6, 6, 0},
            {0, 6, 6}
        },

        {
            {0, 7, 7},
            {7, 7, 0}
        }
    };

    private void drawCurrentPiece() {
        for (int row = 0; row < currentPiece.length; row++) {
            for (int col = 0; col < currentPiece[0].length; col++) {
                int val = currentPiece[row][col];
                if (val != 0) {
                    float x = offsetX + (pieceCol + col) * CELL_SIZE;
                    float y = offsetY + (pieceRow + row) * CELL_SIZE;
                    drawColoredRect(x, y, val);
                }
            }
        }
    }

    private void drawNextPiece(){
        float baseX = Gdx.graphics.getWidth() - 150;
        float baseY = Gdx.graphics.getHeight() -100;

        subFont.getData().setScale(1.5f);
        subFont.draw(batch, "Next", baseX, baseY + 80);

        for (int row = 0; row < nextPiece.length; row++) {
            for (int col = 0; col < nextPiece[0].length; col++){
                int val = nextPiece[row][col];
                if(val != 0){
                    float x = baseX + col * (CELL_SIZE / 2f);
                    float y = baseY + row * (CELL_SIZE / 2f);
                    drawRect(x, y , CELL_SIZE / 2f, CELL_SIZE / 2f, getColorR(val), getColorG(val), getColorB(val), 1);
                }
            }
        }

        subFont.getData().setScale(1);
    }

    private float getColorR(int type) {
        switch (type) {
            case 1: return 0f;
            case 2: return 1f;
            case 3: return 0.6f;
            case 4: return 1f;
            case 5: return 0f;
            case 6: return 0f;
            case 7: return 1f;
        }
        return 1f;
    }

    private float getColorG(int type) {
        switch (type) {
            case 1: return 1f;
            case 2: return 1f;
            case 3: return 0f;
            case 4: return 0.5f;
            case 5: return 0f;
            case 6: return 1f;
            case 7: return 0f;
        }
        return 1f;
    }

    private float getColorB(int type) {
        switch (type) {
            case 1: return 1f;
            case 2: return 0f;
            case 3: return 1f;
            case 4: return 0f;
            case 5: return 1f;
            case 6: return 0f;
            case 7: return 0f;
        }
        return 1f;
    }


    private void drawGridLine(float x, float y, float w, float h) {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.1f, 0.1f, 0.1f, 1f);
        shapeRenderer.rect(x, y, w, h);
        shapeRenderer.end();
    }

    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT)) {
            if (canMoveHorizontally(-1)) {
                pieceCol--;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT)) {
            if (canMoveHorizontally(1)) {
                pieceCol++;
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            rotateCurrentPiece();
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            fallInterval = softDropInterval;
        } else {
            fallInterval = normalFallInterval;
        }

        if(Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            hardDrop();
        }
    }

    private void handleNameInput(){
        for(int key = Input.Keys.A; key <= Input.Keys.Z; key++){
            if(Gdx.input.isKeyJustPressed(key)){
                if(playerName.length() < 10){
                    playerName += getCharForKey(key);
                }
            }
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.BACKSPACE) && playerName.length() > 0) {
            playerName = playerName.substring(0, playerName.length() - 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) && !playerName.isEmpty()) {
            saveScore(playerName, score);
            enteringName = false;
        }
    }

    private char getCharForKey(int keycode) {
        if (keycode >= Input.Keys.A && keycode <= Input.Keys.Z) {
            return (char) ('a' + (keycode - Input.Keys.A));
        }
        return '?';
    }

    private void hardDrop(){
        while(canMoveDown()){
            pieceRow--;
        }
        placePiece();
        clearLines();
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
    }

    private void updateFall(){
        if(TimeUtils.timeSinceMillis(lastFallTime) > fallInterval){
            if(canMoveDown()){
                pieceRow--;
            } else {
                placePiece();
                clearLines();
                spawnNewPiece();
            }
            lastFallTime = TimeUtils.millis();
        }
    }

    private boolean canMoveDown(){
        for(int row = 0; row < currentPiece.length; row++){
            for(int col = 0; col < currentPiece[0].length; col++){
                if(currentPiece[row][col] != 0){
                    int boardRow = pieceRow + row - 1;
                    int boardCol = pieceCol + col;

                    if(boardRow < 0 || boardCol < 0 || boardRow >= ROWS || boardCol >= COLS || board[boardRow][boardCol] != 0){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean canMoveHorizontally(int dx){
        for(int row = 0; row < currentPiece.length; row++){
            for(int col = 0; col < currentPiece[0].length; col++){
                if(currentPiece[row][col] != 0){
                    int newCol = pieceCol + col + dx;
                    int newRow = pieceRow + row;

                    if(newCol < 0 || newCol >= COLS) return false;
                    if(board[newRow][newCol] != 0) return false;
                }
            }
        }
        return true;
    }

    private void rotateCurrentPiece(){
        int rows = currentPiece.length;
        int cols = currentPiece[0].length;

        int[][] rotated = new int[cols][rows];

        for(int row = 0; row < rows; row++){
            for(int col = 0; col < cols; col++){
                rotated[col][rows - 1 - row] = currentPiece[row][col];
            }
        }

        currentPiece = rotated;

        if(pieceCol + currentPiece[0].length > COLS){
            pieceCol = COLS - currentPiece[0].length;
        }
        if(pieceRow + currentPiece[0].length > ROWS){
            pieceRow = ROWS - currentPiece.length;
        }
    }

    private int[][] getRandomPiece(){
        int index = random.nextInt(tetrominoes.length);
        int[][] shape = tetrominoes[index];
        int[][] copy = new int[shape.length][shape[0].length];
        for(int i = 0; i < shape.length; i++){
            System.arraycopy(shape[i], 0 , copy[i], 0, shape[0].length);
        }
        return copy;
    }

    private void placePiece(){
        for(int row = 0; row < currentPiece.length; row++){
            for(int col = 0; col < currentPiece[0].length; col++){
                if(currentPiece[row][col] != 0){
                    int boardRow = pieceRow + row;
                    int boardCol = pieceCol + col;
                    if (boardRow >= 0 && boardRow < ROWS && boardCol >= 0 && boardCol < COLS) {
                        board[boardRow][boardCol] = currentPiece[row][col];
                    }
                }
            }
        }
    }

    private void clearLines(){
        int lines = 0;

        for(int row = 0; row < ROWS; row++){
            boolean fullLine = true;
            for(int col = 0; col < COLS; col++){
                if(board[row][col] == 0){
                    fullLine = false;
                    break;
                }
            }

            if(fullLine){
                for(int r = row; r < ROWS - 1; r++){
                    for(int c = 0; c < COLS; c++){
                        board[r][c] = board[r + 1][c];
                    }
                }

                for(int c = 0; c < COLS; c++){
                    board[ROWS - 1][c] = 0;
                }

                row--;
                lines++;
            }
        }

        if(lines > 0){
            linesCleared += lines;
            switch (lines) {
                case 1:
                    score += 100;
                    break;
                case 2:
                    score += 300;
                    break;
                case 3:
                    score += 500;
                    break;
                case 4:
                    score += 800;
                    break;
                default:
                    score += lines * 200;
                    break;
            }

            level = 1 + linesCleared / 5;

            normalFallInterval = Math.max(100, 500 - (level - 1) * 50);
        }
    }

    private void spawnNewPiece(){
        currentPiece = nextPiece;
        nextPiece = getRandomPiece();

        pieceRow = ROWS - currentPiece.length;
        pieceCol = COLS / 2 - currentPiece[0].length / 2;

        if(!canSpawnAtPosition(pieceRow,pieceCol)){
            gameState = GameState.GAME_OVER;
        }
    }

    private boolean canSpawnAtPosition(int rowOffset, int colOffset){
        for(int row = 0; row < currentPiece.length; row++){
            for(int col = 0; col < currentPiece[0].length; col++){
                if(currentPiece[row][col] != 0){
                    int boardRow = rowOffset + row;
                    int boardCol = colOffset + col;

                    if(boardRow < 0 || boardRow >= ROWS || boardCol < 0 || boardCol >= COLS){
                        return false;
                    }

                    if(board[boardRow][boardCol] != 0){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private void saveScore(String name, int score) {
        try {
            com.badlogic.gdx.files.FileHandle file = Gdx.files.local("scoreboard.txt");
            String entry = name + " " + score + "\n";
            file.writeString(entry, true);
        } catch (Exception e) {
            System.out.println("スコア保存失敗: " + e.getMessage());
        }
    }

    private java.util.ArrayList<String> loadScores(){
        java.util.ArrayList<String> list = new java.util.ArrayList<>();
        try{
            com.badlogic.gdx.files.FileHandle file = Gdx.files.local("scoreboard.txt");
            if(file.exists()) {
                String[] lines = file.readString().split("\n");
                java.util.Arrays.sort(lines, (a,b) -> {
                    try{
                        int sa = Integer.parseInt(a.split(" ")[1]);
                        int sb = Integer.parseInt(b.split(" ")[1]);
                        return Integer.compare(sb, sa);
                    } catch (Exception e) {
                        return 0;
                    }
                });
                for (int i = 0;i < Math.min(lines.length, 5); i++) {
                    list.add(lines[i]);
                }
            }
        } catch (Exception e){
            System.out.println("読み込みエラー: " + e.getMessage());
        }
        return list;
    }

    private void restartGame(){
        for(int row = 0; row < ROWS; row++){
            for(int col = 0; col < COLS; col++){
                board[row][col] = 0;
            }
        }

        playerName = "";
        enteringName = false;
        score = 0;
        linesCleared = 0;
        level = 1;

        gameState = GameState.PLAYING;
        spawnNewPiece();
        lastFallTime = TimeUtils.millis();
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        shapeRenderer.dispose();
    }
}
