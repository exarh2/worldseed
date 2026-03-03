package online.worldseed.model.geo.isohypse;

public class ByteArray2 {
    private int width;
    private int height;
    private byte[] data;

    public ByteArray2(int width, int height) {
        this.width = width;
        this.height = height;
        data = new byte[width * height];
    }

    public byte[] getData() {
        return data;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public byte getValue(int x, int y) {
        return data[x + y * width];
    }

    public void setValue(byte value, int x, int y) {
        data[x + y * width] = value;
    }

    public void initialize(byte initialValue) {
        for (int i = 0; i < data.length; i++) {
            data[i] = initialValue;
        }
    }
}

