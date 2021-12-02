package massspringcloth.cloth;

/**
 * @author Mirco Werner
 */
public class MassSpringModel {
    private final int width;
    private final int height;

    private float[] points;
    private float[] tex;
    private int[] indices;

    public MassSpringModel(int width, int height, Point[][] points) {
        this.width = width;
        this.height = height;
        create(points);
    }

    private void create(Point[][] pointInitialization) {
        points = new float[width * height * 12];
        tex = new float[width * height * 2];
        indices = new int[6 * (width - 1) * (height - 1)];
        int vertexPointer = 0;
        for (int h = 0; h < height; h++) {
            for (int w = 0; w < width; w++) {
                Point point = pointInitialization[w][h];

                // positions
                points[vertexPointer * 12] = point.x;
                points[vertexPointer * 12 + 1] = point.y;
                points[vertexPointer * 12 + 2] = point.z;
                points[vertexPointer * 12 + 3] = 1;

                // velocities
                points[vertexPointer * 12 + 4] = point.v_x;
                points[vertexPointer * 12 + 5] = point.v_y;
                points[vertexPointer * 12 + 6] = point.v_z;
                points[vertexPointer * 12 + 7] = 0;

                // locked
                points[vertexPointer * 12 + 8] = point.locked;

                // textureUV
                points[vertexPointer * 12 + 9] = w / 4f;
                points[vertexPointer * 12 + 10] = h / 4f;

                // padding
                points[vertexPointer * 12 + 11] = 0;

                // texture
                tex[vertexPointer * 2] = w / 4f;
                tex[vertexPointer * 2 + 1] = h / 4f;

                vertexPointer++;
            }
        }
        vertexPointer = 0;
        for (int h = 0; h < height - 1; h++) {
            for (int w = 0; w < width - 1; w++) {
                int bottomLeft = h * width + w;
                int bottomRight = (h + 1) * width + w;
                int topLeft = bottomLeft + 1;
                int topRight = bottomRight + 1;

                indices[vertexPointer * 6] = bottomLeft;
                indices[vertexPointer * 6 + 1] = topRight;
                indices[vertexPointer * 6 + 2] = topLeft;

                indices[vertexPointer * 6 + 3] = bottomLeft;
                indices[vertexPointer * 6 + 4] = bottomRight;
                indices[vertexPointer * 6 + 5] = topRight;

                vertexPointer++;
            }
        }
    }

    public float[] getPoints() {
        return points;
    }

    public long getVertexBufferLengthInBytes() {
        return width * height * 16L; // 4 vertices times 4 bytes long float
    }

    public long getPointsBufferLengthInBytes() {
        return points.length * 4L; // float is 4 bytes long
    }

    public float[] getTex() {
        return tex;
    }

    public int[] getIndices() {
        return indices;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
