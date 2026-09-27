public class MinHeap {

    private int[] data;
    private int size;

    private long comparisonCount;

    public MinHeap() {
        data = new int[16];
        size = 0;
    }

    public MinHeap(int initialCapacity) {
        data = new int[Math.max(16, initialCapacity)];
        size = 0;
    }

    public void resetCounters() {
        comparisonCount = 0;
    }

    public long getComparisonCount() {
        return comparisonCount;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    private void grow() {
        int[] newData = new int[data.length * 2];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
        }
        data = newData;
    }

    public void insert(int x) {
        if (size == data.length) {
            grow();
        }
        data[size] = x;
        size++;
        siftUp(size - 1);
    }

    private void siftUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            comparisonCount++;
            if (data[index] < data[parent]) {
                int temp = data[index];
                data[index] = data[parent];
                data[parent] = temp;
                index = parent;
            } else {
                break;
            }
        }
    }

    public int peekMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        return data[0];
    }

    public int extractMin() {
        if (size == 0) {
            throw new IllegalStateException("heap is empty");
        }
        int min = data[0];
        size--;
        data[0] = data[size];
        siftDown(0);
        return min;
    }

    private void siftDown(int index) {
        while (true) {
            int left = 2 * index + 1;
            int right = 2 * index + 2;
            int smallest = index;
            if (left < size) {
                comparisonCount++;
                if (data[left] < data[smallest]) {
                    smallest = left;
                }
            }
            if (right < size) {
                comparisonCount++;
                if (data[right] < data[smallest]) {
                    smallest = right;
                }
            }
            if (smallest == index) {
                break;
            }
            int temp = data[index];
            data[index] = data[smallest];
            data[smallest] = temp;
            index = smallest;
        }
    }

    public boolean isValidHeap() {
        for (int i = 0; i < size; i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            if (left < size && data[left] < data[i]) {
                return false;
            }
            if (right < size && data[right] < data[i]) {
                return false;
            }
        }
        return true;
    }
}
