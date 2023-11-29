import java.io.*;
import java.util.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("input.txt"));
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            int boardSize = Integer.parseInt(reader.readLine().trim());
            int numInsects = Integer.parseInt(reader.readLine().trim());
            int numFoodPoints = Integer.parseInt(reader.readLine().trim());
            if (numInsects < 1 || numInsects > 16) {
                throw new InvalidNumberOfInsectsException();
            }
            if (numFoodPoints < 1 || numFoodPoints > 200) {
                throw new InvalidNumberOfFoodPointsException();
            }
            if (boardSize < 4 || boardSize > 1000) {
                throw new InvalidBoardSizeException();
            }
            Board board = new Board(boardSize);
            Set<String> createdInsects = new HashSet<>();
            for (int i = 0; i < numInsects; i++) {
                String[] insectData = reader.readLine().split(" ");
                String color = insectData[0];
                String type = insectData[1];
                int x = Integer.parseInt(insectData[2]);
                int y = Integer.parseInt(insectData[3]);
                if (x < 1 || x > boardSize || y < 1 || y > boardSize) {
                    throw new InvalidEntityPositionException();
                }
                Insect insect = createInsect(color, type, new EntityPosition(x, y), createdInsects);
                board.addEntity(insect);
            }
            for (int i = 0; i < numFoodPoints; i++) {
                String[] foodData = reader.readLine().split(" ");
                int value = Integer.parseInt(foodData[0]);
                int x = Integer.parseInt(foodData[1]);
                int y = Integer.parseInt(foodData[2]);
                if (x < 1 || x > boardSize || y < 1 || y > boardSize) {
                    throw new InvalidEntityPositionException();
                }
                FoodPoint foodPoint = new FoodPoint(new EntityPosition(x, y), value, true);
                board.addEntity(foodPoint);
            }
            reader.close();
            StringWriter resultWriter = new StringWriter();
            processInsects(board, resultWriter);
            writer.write(resultWriter.toString());
            writer.newLine();
            writer.close();
        } catch (IOException | NumberFormatException | InvalidInsectColorException | InvalidInsectTypeException |
                 TwoEntitiesOnSamePositionException | InvalidBoardSizeException | InvalidNumberOfFoodPointsException |
                 InvalidNumberOfInsectsException | InvalidEntityPositionException | DuplicateInsectException e) {
            writeToFile("output.txt", e.getMessage());
        }
    }
    private static void writeToFile(String filename, String Message) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(Message);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private static void processInsects(Board board, Writer writer) throws IOException {
        List<Insect> insects = new ArrayList<>();

        for (Map.Entry<String, BoardEntity> entry : board.getBoardData().entrySet()) {
            BoardEntity entity = entry.getValue();
            if (entity instanceof Insect) {
                insects.add((Insect) entity);
            }
        }

        for (int i = 0; i < insects.size(); i++) {
            Insect insect = insects.get(i);
            Direction bestDirection = insect.getBestDirection(board.getBoardData(), board.getSize());
            int foodEaten = insect.travelDirection(bestDirection, board.getBoardData(), board.getSize());
            String colorString = insect.color.name().toLowerCase();
            colorString = Character.toUpperCase(colorString.charAt(0)) + colorString.substring(1);
            String directionString = bestDirection.getTextRepresentation();
            directionString = Character.toUpperCase(directionString.charAt(0)) + directionString.substring(1);
            writer.write(colorString + " " + insect.getClass().getSimpleName() + " " + directionString + " " + foodEaten);
            if (i < insects.size() - 1) {
                writer.write("\n");
            }
        }
    }

    private static Insect createInsect(String color, String type, EntityPosition position, Set<String> createdInsects)
            throws InvalidInsectColorException, InvalidInsectTypeException, DuplicateInsectException {
        InsectColor insectColor = InsectColor.toColor(color);
        String insectKey = color + "_" + type;
        if (createdInsects.contains(insectKey)) {
            throw new DuplicateInsectException();
        }
        createdInsects.add(insectKey);
        switch (type) {
            case "Grasshopper":
                return new Grasshopper(position, insectColor);
            case "Butterfly":
                return new Butterfly(position, insectColor);
            case "Ant":
                return new Ant(position, insectColor);
            case "Spider":
                return new Spider(position, insectColor);
            default:
                throw new InvalidInsectTypeException();
        }
    }
}
enum Direction {
    N("North"), E("East"), S("South"), W("West"), NE("North-East"), SE("South-East"), SW("South-West"), NW("North-West");
    private String textRepresentation;
    Direction(String text) {
        this.textRepresentation = text;
    }
    public String getTextRepresentation() {
        return textRepresentation;
    }
}
class EntityPosition {
    private int x;
    private int y;
    public EntityPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int[] getCoordinates() {
        return new int[]{x, y};
    }
}
enum InsectColor {
    RED, GREEN, BLUE, YELLOW;
    public static InsectColor toColor(String s) throws InvalidInsectColorException {
        for (InsectColor color : InsectColor.values()) {
            if (color.name().equalsIgnoreCase(s)) {
                return color;
            }
        }
        throw new InvalidInsectColorException();
    }
}
class FoodPoint extends BoardEntity {
    private int value;
    private boolean canEat;
    public FoodPoint(EntityPosition position, int value, boolean canEat) {
        super(position);
        this.value = value;
        this.canEat = canEat;
    }
    public int getValue() {
        return value;
    }

    public boolean isCanEat() {
        return canEat;
    }

    public void setCanEat(boolean canEat) {
        this.canEat = canEat;
    }
}
class Board {
    private Map<String, BoardEntity> boardData;
    private int size;
    public Board(int boardSize) {
        this.size = boardSize;
        this.boardData = new LinkedHashMap<>();
    }
    public void addEntity(BoardEntity entity) throws TwoEntitiesOnSamePositionException {
        EntityPosition position = entity.entityPosition;
        int[] coordinates = position.getCoordinates();
        String key = Arrays.toString(coordinates);

        if (boardData.containsKey(key)) {
            throw new TwoEntitiesOnSamePositionException();
        }

        boardData.put(key, entity);
    }
    public BoardEntity getEntity(EntityPosition position) {
        int[] coordinates = position.getCoordinates();
        String key = Arrays.toString(coordinates);
        return boardData.get(key);
    }
    public Direction getDirection(Insect insect) {
        return Direction.N;
    }
    public int getDirectionSum(Insect insect) {
        return 0;
    }
    public Map<String, BoardEntity> getBoardData() {
        return boardData;
    }
    public int getSize() {
        return size;
    }
}
abstract class BoardEntity {
    protected EntityPosition entityPosition;
    public BoardEntity(EntityPosition entityPosition) {
        this.entityPosition = entityPosition;
    }
}
abstract class Insect extends BoardEntity {
    protected InsectColor color;
    private boolean alive;
    public Insect(EntityPosition position, InsectColor color, boolean alive) {
        super(position);
        this.color = color;
        this.alive = alive;
    }
    public abstract Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize);
    public abstract int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize);

    public boolean getAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
class Grasshopper extends Insect {
    public Grasshopper(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color, true);
    }
    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        Direction bestDir = null;
        int maxFood = 0;
        for (Direction dir : new Direction[]{Direction.N, Direction.E, Direction.S, Direction.W}) {
            int newX = x;
            int newY = y;
            int spreadX = 0;
            int spreadY = 0;
            switch (dir) {
                case W:
                    spreadY = -2;
                    break;
                case S:
                    spreadX = 2;
                    break;
                case E:
                    spreadY = 2;
                    break;
                case N:
                    spreadX = -2;
                    break;
            }
            int currentFoodValue = 0;
            while ((newX >= 1 && newX <= boardSize) && (newY >= 1 && newY <= boardSize)) {
                newX += spreadX;
                newY += spreadY;
                EntityPosition newPosition = new EntityPosition(newX, newY);
                int[] newCoordinates = newPosition.getCoordinates();
                String key = Arrays.toString(newCoordinates);
                BoardEntity entityAtNewPosition = boardData.get(key);

                if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                    int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                    currentFoodValue += foodValue;
                }
            }
            if (currentFoodValue > maxFood) {
                maxFood = currentFoodValue;
                bestDir = dir;
            }
        }
        if (bestDir == null){
            return Direction.N;
        } else {
            return bestDir;
        }
    }
    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        int spreadY = 0;
        int spreadX = 0;
        int getFood = 0;
        boolean isAlive = true;
        switch (dir) {
            case W:
                spreadY = -2;
                break;
            case S:
                spreadX = 2;
                break;
            case E:
                spreadY = 2;
                break;
            case N:
                spreadX = -2;
                break;
        }
        while (isAlive){
            x += spreadX;
            y += spreadY;
            InsectColor grasshopperColor = color;
            EntityPosition newPosition = new EntityPosition(x, y);
            int[] newCoordinates = newPosition.getCoordinates();
            String key = Arrays.toString(newCoordinates);
            BoardEntity entityAtNewPosition = boardData.get(key);
            if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                getFood += foodValue;
                ((FoodPoint) entityAtNewPosition).setCanEat(false);
            } else if (entityAtNewPosition instanceof Insect && color != grasshopperColor && getAlive() || x > boardSize || x < 1 || y > boardSize || y < 1){
                isAlive = false;
                setAlive(false);
                break;
            }
        }
        return getFood;
    }
}
class Butterfly extends Insect implements OrthogonalMoving {
    public Butterfly(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color, true);
    }
    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        Direction bestDir = null;
        int maxFood = 0;
        for (Direction dir : new Direction[]{Direction.N, Direction.E, Direction.S, Direction.W}) {
            int newX = x;
            int newY = y;
            int spreadX = 0;
            int spreadY = 0;
            switch (dir) {
                case W:
                    spreadY = -1;
                    break;
                case S:
                    spreadX = 1;
                    break;
                case E:
                    spreadY = 1;
                    break;
                case N:
                    spreadX = -1;
                    break;
            }
            int currentFoodValue = 0;
            while ((newX >= 1 && newX <= boardSize) && (newY >= 1 && newY <= boardSize)) {
                newX += spreadX;
                newY += spreadY;
                EntityPosition newPosition = new EntityPosition(newX, newY);
                int[] newCoordinates = newPosition.getCoordinates();
                String key = Arrays.toString(newCoordinates);
                BoardEntity entityAtNewPosition = boardData.get(key);

                if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                    int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                    currentFoodValue += foodValue;
                }
            }
            if (currentFoodValue > maxFood) {
                maxFood = currentFoodValue;
                bestDir = dir;
            }
        }
        if (bestDir == null){
            return Direction.N;
        } else {
            return bestDir;
        }
    }
    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        int spreadY = 0;
        int spreadX = 0;
        int getFood = 0;
        boolean isAlive = true;
        switch (dir) {
            case W:
                spreadY = -1;
                break;
            case S:
                spreadX = 1;
                break;
            case E:
                spreadY = 1;
                break;
            case N:
                spreadX = -1;
                break;
        }
        while (isAlive){
            x += spreadX;
            y += spreadY;
            InsectColor grasshopperColor = color;
            EntityPosition newPosition = new EntityPosition(x, y);
            int[] newCoordinates = newPosition.getCoordinates();
            String key = Arrays.toString(newCoordinates);
            BoardEntity entityAtNewPosition = boardData.get(key);
            if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                getFood += foodValue;
                ((FoodPoint) entityAtNewPosition).setCanEat(false);
            } else if (entityAtNewPosition instanceof Insect && ((Insect) entityAtNewPosition).color != grasshopperColor && ((Insect) entityAtNewPosition).getAlive() || x > boardSize || x < 1 || y > boardSize || y < 1){
                isAlive = false;
                setAlive(false);
                break;
            }
        }
        return getFood;
    }
    @Override
    public int getOrthogonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
    @Override
    public int travelOrthogonally(Direction dir, EntityPosition entityPosition, InsectColor color, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
}
class Ant extends Insect implements OrthogonalMoving, DiagonalMoving{
    public Ant(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color, true);
    }
    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        Direction bestDir = null;
        int maxFood = 0;
        for (Direction dir : Direction.values()) {
            int newX = x;
            int newY = y;
            int spreadX = 0;
            int spreadY = 0;
            switch (dir) {
                case W:
                    spreadY = -1;
                    break;
                case S:
                    spreadX = 1;
                    break;
                case E:
                    spreadY = 1;
                    break;
                case N:
                    spreadX = -1;
                    break;
                case SW:
                    spreadY = -1;
                    spreadX = 1;
                    break;
                case NW:
                    spreadY = -1;
                    spreadX = -1;
                    break;
                case NE:
                    spreadX = -1;
                    spreadY = 1;
                    break;
                case SE:
                    spreadX = 1;
                    spreadY = 1;
                    break;
            }
            int currentFoodValue = 0;
            while ((newX >= 1 && newX <= boardSize) && (newY >= 1 && newY <= boardSize)) {
                newX += spreadX;
                newY += spreadY;
                EntityPosition newPosition = new EntityPosition(newX, newY);
                int[] newCoordinates = newPosition.getCoordinates();
                String key = Arrays.toString(newCoordinates);
                BoardEntity entityAtNewPosition = boardData.get(key);

                if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                    int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                    currentFoodValue += foodValue;
                }
            }
            if (currentFoodValue > maxFood) {
                maxFood = currentFoodValue;
                bestDir = dir;
            }
        }
        if (bestDir == null){
            return Direction.N;
        } else {
            return bestDir;
        }
    }
    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        int spreadY = 0;
        int spreadX = 0;
        int getFood = 0;
        boolean isAlive = true;
        switch (dir) {
            case W:
                spreadY = -1;
                break;
            case S:
                spreadX = 1;
                break;
            case E:
                spreadY = 1;
                break;
            case N:
                spreadX = -1;
                break;
            case SW:
                spreadY = -1;
                spreadX = 1;
                break;
            case NW:
                spreadY = -1;
                spreadX = -1;
                break;
            case NE:
                spreadX = -1;
                spreadY = 1;
                break;
            case SE:
                spreadX = 1;
                spreadY = 1;
                break;
        }
        while (isAlive){
            x += spreadX;
            y += spreadY;
            InsectColor grasshopperColor = color;
            EntityPosition newPosition = new EntityPosition(x, y);
            int[] newCoordinates = newPosition.getCoordinates();
            String key = Arrays.toString(newCoordinates);
            BoardEntity entityAtNewPosition = boardData.get(key);
            if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                getFood += foodValue;
                ((FoodPoint) entityAtNewPosition).setCanEat(false);
            } else if (entityAtNewPosition instanceof Insect && ((Insect) entityAtNewPosition).color != grasshopperColor && ((Insect) entityAtNewPosition).getAlive() || x > boardSize || x < 1 || y > boardSize || y < 1){
                isAlive = false;
                setAlive(false);

                break;
            }
        }
        return getFood;
    }
    @Override
    public int getOrthogonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
    @Override
    public int travelOrthogonally(Direction dir, EntityPosition entityPosition, InsectColor color, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
    @Override
    public int getDiagonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
    @Override
    public int travelDiagonally(Direction dir, EntityPosition entityPosition, InsectColor color, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
}
class Spider extends Insect implements DiagonalMoving {
    public Spider(EntityPosition entityPosition, InsectColor color) {
        super(entityPosition, color, true);
    }
    @Override
    public Direction getBestDirection(Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        Direction bestDir = null;
        int maxFood = 0;
        for (Direction dir : new Direction[]{Direction.NE, Direction.SE, Direction.SW, Direction.NW}) {
            int newX = x;
            int newY = y;
            int spreadX = 0;
            int spreadY = 0;
            switch (dir) {
                case SW:
                    spreadY = -1;
                    spreadX = 1;
                    break;
                case NW:
                    spreadX = -1;
                    spreadY = -1;
                    break;
                case NE:
                    spreadY = 1;
                    spreadX = -1;
                    break;
                case SE:
                    spreadX = 1;
                    spreadY = 1;
                    break;
            }
            int currentFoodValue = 0;
            while ((newX >= 1 && newX <= boardSize) && (newY >= 1 && newY <= boardSize)) {
                newX += spreadX;
                newY += spreadY;
                EntityPosition newPosition = new EntityPosition(newX, newY);
                int[] newCoordinates = newPosition.getCoordinates();
                String key = Arrays.toString(newCoordinates);
                BoardEntity entityAtNewPosition = boardData.get(key);

                if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                    int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                    currentFoodValue += foodValue;
                }
            }
            if (currentFoodValue > maxFood) {
                maxFood = currentFoodValue;
                bestDir = dir;
            }
        }
        if (bestDir == null){
            return Direction.NE;
        } else {
            return bestDir;
        }
    }
    @Override
    public int travelDirection(Direction dir, Map<String, BoardEntity> boardData, int boardSize) {
        int[] currentPosition = entityPosition.getCoordinates();
        int x = currentPosition[0];
        int y = currentPosition[1];
        int spreadY = 0;
        int spreadX = 0;
        int getFood = 0;
        boolean isAlive = true;
        switch (dir) {
            case SW:
                spreadY = -1;
                spreadX = 1;
                break;
            case NW:
                spreadX = -1;
                spreadY = -1;
                break;
            case NE:
                spreadY = 1;
                spreadX = -1;
                break;
            case SE:
                spreadX = 1;
                spreadY = 1;
                break;
        }
        while (isAlive){
            x += spreadX;
            y += spreadY;
            InsectColor grasshopperColor = color;
            EntityPosition newPosition = new EntityPosition(x, y);
            int[] newCoordinates = newPosition.getCoordinates();
            String key = Arrays.toString(newCoordinates);
            BoardEntity entityAtNewPosition = boardData.get(key);
            if (entityAtNewPosition instanceof FoodPoint && ((FoodPoint) entityAtNewPosition).isCanEat()) {
                int foodValue = ((FoodPoint) entityAtNewPosition).getValue();
                getFood += foodValue;
                ((FoodPoint) entityAtNewPosition).setCanEat(false);
            } else if (entityAtNewPosition instanceof Insect && ((Insect) entityAtNewPosition).color != grasshopperColor && ((Insect) entityAtNewPosition).getAlive() || x > boardSize || x < 1 || y > boardSize || y < 1){
                isAlive = false;
                setAlive(false);
                break;
            }
        }
        return getFood;
    }
    @Override
    public int getDiagonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
    @Override
    public int travelDiagonally(Direction dir, EntityPosition entityPosition, InsectColor color, Map<String, BoardEntity> boardData, int boardSize) {
        return travelDirection(dir, boardData, boardSize);
    }
}
interface OrthogonalMoving {
    int getOrthogonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition, Map<String, BoardEntity> boardData, int boardSize);
    int travelOrthogonally(Direction dir, EntityPosition entityPosition, InsectColor color, Map<String, BoardEntity> boardData, int boardSize);
}
interface DiagonalMoving {
    int getDiagonalDirectionVisibleValue(Direction dir, EntityPosition entityPosition, Map<String, BoardEntity> boardData, int boardSize);
    int travelDiagonally(Direction dir, EntityPosition entityPosition, InsectColor color, Map<String, BoardEntity> boardData, int boardSize);
}
class InvalidBoardSizeException extends Exception {
    public String getMessage(){
        return "Invalid board size";
    }
}
class InvalidNumberOfInsectsException extends Exception {
    public String getMessage(){
        return "Invalid number of insects";
    }
}
class InvalidNumberOfFoodPointsException extends Exception {
    public String getMessage(){
        return "Invalid number of food points";
    }
}
class InvalidInsectColorException extends Exception {
    public String getMessage(){
        return "Invalid insect color";
    }
}
class InvalidInsectTypeException extends Exception {
    public String getMessage(){
        return "Invalid insect type";
    }
}
class InvalidEntityPositionException extends Exception {
    public String getMessage(){
        return "Invalid entity position";
    }
}
class DuplicateInsectException extends Exception {
    public String getMessage(){
        return "Duplicate insects";
    }
}
class TwoEntitiesOnSamePositionException extends Exception {
    public String getMessage(){
        return "Two entities in the same position";
    }
}

