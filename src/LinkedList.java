public class LinkedList {

    private static class Node {
        int value;
        Node next;

        Node(int value) {
            this.value = value;
        }
    }

    private Node head;
    private Node tail;
    private int size;

    private long accessCount;
    private long movementCount;
    private long comparisonCount;

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

    public void add(int x) {
        Node node = new Node(x);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
        movementCount++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        if (index == size) {
            add(x);
            return;
        }
        Node node = new Node(x);
        if (index == 0) {
            node.next = head;
            head = node;
            if (tail == null) {
                tail = node;
            }
        } else {
            Node prev = head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.next;
                movementCount++;
            }
            node.next = prev.next;
            prev.next = node;
        }
        size++;
        movementCount++;
    }

    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        int removed;
        if (index == 0) {
            removed = head.value;
            head = head.next;
            if (head == null) {
                tail = null;
            }
        } else {
            Node prev = head;
            for (int i = 0; i < index - 1; i++) {
                prev = prev.next;
                movementCount++;
            }
            Node target = prev.next;
            removed = target.value;
            prev.next = target.next;
            if (target == tail) {
                tail = prev;
            }
        }
        size--;
        return removed;
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index);
        }
        Node current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
            accessCount++;
        }
        accessCount++;
        return current.value;
    }

    public boolean contains(int x) {
        Node current = head;
        while (current != null) {
            comparisonCount++;
            if (current.value == x) {
                return true;
            }
            current = current.next;
        }
        return false;
    }
}
