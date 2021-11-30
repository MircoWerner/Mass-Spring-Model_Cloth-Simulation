package renderengine.engine;

/**
 * @author Mirco Werner
 */
public class Timer {
    private long lastLoopTime;

    private int frames = 0;
    private long second;

    public void init() {
        lastLoopTime = getTime();
    }

    private long getTime() {
        return System.currentTimeMillis();
    }

    public long getElapsedTime() {
        long time = getTime();
        long elapsedTime = time - lastLoopTime;
        lastLoopTime = time;
        frames++;
        long newSecond = System.currentTimeMillis() / 1000;
        if (newSecond > second) {
            second = newSecond;
            System.out.println((1000 / (float) elapsedTime) + "     " + frames);
            frames = 0;
        }
        return elapsedTime;
    }
}
