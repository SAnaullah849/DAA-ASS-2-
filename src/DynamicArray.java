public class DynamicArray {

    private int[] data;
    private int size;

    private long accessCount;
    private long movementCount;
    private long comparisonCount;

    public DynamicArray() {
        data = new int[4];
        size = 0;
    }

    public DynamicArray(int initialCapacity) {
        data = new int[Math.max(4, initialCapacity)];
        size = 0;
    }

    public void resetCounters() {
        accessCount = 0;
        movementCount = 0;
        comparisonCount = 0;
    }

    public long getAccessCount() {
        return accessCount;
    }

    public long getMovementCount() {
        return movementCount;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public int size() {
        return size;
    }

    private void grow() {
        int[] newData = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
            movementCount++;
        }
        data = newData;
    }

    public void add(int x) {
        if (size == data.length) {
            grow();
        }
        data[size] = x;
        size++;
        movementCount++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        if (size == data.length) {
            grow();
        }
        for (int i = size; i > index; i--) {
            data[i] = data[i - 1];
            movementCount++;
        }
        data[index] = x;
        size++;
        movementCount++;
    }

    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        int removed = data[index];
        for (int i = index; i < size - 1; i++) {
            data[i] = data[i + 1];
            movementCount++;
        }
        size--;
        return removed;
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        accessCount++;
        return data[index];
    }

    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            comparisonCount++;
            if (data[i] == x) {
                return true;
            }
        }
        return false;
    }
}
