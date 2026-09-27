import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.Random;

public class Tests {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
            System.out.println("PASS: " + name);
        } else {
            failed++;
            System.out.println("FAIL: " + name);
        }
    }

    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        testIndexBoundariesAndInvalidMutations();
        testAgainstStandardCollections();
        System.out.println();
        System.out.println("Total passed: " + passed);
        System.out.println("Total failed: " + failed);
    }


    private static void checkIndexOutOfBounds(String name, Runnable operation) {
        boolean threw = false;
        try {
            operation.run();
        } catch (IndexOutOfBoundsException e) {
            threw = true;
        }
        check(name, threw);
    }

    private static void testIndexBoundariesAndInvalidMutations() {
        System.out.println("-- Index boundaries and invalid mutations --");

        DynamicArray array = new DynamicArray();
        array.add(0, 2);
        array.add(array.size(), 4);
        array.add(1, 3);
        check("array insertion at first, middle, and end", array.get(0) == 2 && array.get(1) == 3 && array.get(2) == 4);
        check("array removal at first and last", array.remove(0) == 2 && array.remove(array.size() - 1) == 4);
        checkIndexOutOfBounds("array add rejects negative index", () -> array.add(-1, 1));
        checkIndexOutOfBounds("array add rejects index above size", () -> array.add(array.size() + 1, 1));
        checkIndexOutOfBounds("array remove rejects negative index", () -> array.remove(-1));
        checkIndexOutOfBounds("array remove rejects index equal to size", () -> array.remove(array.size()));

        LinkedList list = new LinkedList();
        list.add(0, 2);
        list.add(list.size(), 4);
        list.add(1, 3);
        check("list insertion at first, middle, and end", list.get(0) == 2 && list.get(1) == 3 && list.get(2) == 4);
        check("list removal at first and last", list.remove(0) == 2 && list.remove(list.size() - 1) == 4);
        checkIndexOutOfBounds("list add rejects negative index", () -> list.add(-1, 1));
        checkIndexOutOfBounds("list add rejects index above size", () -> list.add(list.size() + 1, 1));
        checkIndexOutOfBounds("list remove rejects negative index", () -> list.remove(-1));
        checkIndexOutOfBounds("list remove rejects index equal to size", () -> list.remove(list.size()));
    }

    private static void testAgainstStandardCollections() {
        System.out.println("-- Comparison with Java collections --");

        Random random = new Random(12345);
        DynamicArray array = new DynamicArray();
        ArrayList<Integer> arrayReference = new ArrayList<>();
        LinkedList list = new LinkedList();
        java.util.LinkedList<Integer> listReference = new java.util.LinkedList<>();
        boolean sequenceOperationsMatch = true;

        for (int step = 0; step < 1000; step++) {
            int value = random.nextInt(200);
            if (arrayReference.isEmpty() || random.nextBoolean()) {
                int index = random.nextInt(arrayReference.size() + 1);
                array.add(index, value);
                arrayReference.add(index, value);
                list.add(index, value);
                listReference.add(index, value);
            } else {
                int index = random.nextInt(arrayReference.size());
                if (array.remove(index) != arrayReference.remove(index)
                        || list.remove(index) != listReference.remove(index)) {
                    sequenceOperationsMatch = false;
                }
            }

            if (step % 50 == 0) {
                if (array.size() != arrayReference.size() || list.size() != listReference.size()) {
                    sequenceOperationsMatch = false;
                } else {
                    for (int index = 0; index < arrayReference.size(); index++) {
                        if (array.get(index) != arrayReference.get(index)
                                || list.get(index) != listReference.get(index)) {
                            sequenceOperationsMatch = false;
                            break;
                        }
                    }
                }
                int searchValue = random.nextInt(200);
                if (array.contains(searchValue) != arrayReference.contains(searchValue)
                        || list.contains(searchValue) != listReference.contains(searchValue)) {
                    sequenceOperationsMatch = false;
                }
            }
        }
        check("randomized sequence operations match Java collections", sequenceOperationsMatch);

        MinHeap heap = new MinHeap();
        PriorityQueue<Integer> heapReference = new PriorityQueue<>();
        boolean heapOperationsMatch = true;
        for (int count = 0; count < 1000; count++) {
            int value = random.nextInt(200);
            heap.insert(value);
            heapReference.add(value);
            if (count % 100 == 0) {
                if (heap.peekMin() != heapReference.peek() || !heap.isValidHeap()) {
                    heapOperationsMatch = false;
                }
            }
        }
        while (!heapReference.isEmpty()) {
            if (heap.extractMin() != heapReference.remove()) {
                heapOperationsMatch = false;
            }
            if (!heap.isValidHeap()) {
                heapOperationsMatch = false;
            }
        }
        check("heap operations match PriorityQueue", heapOperationsMatch && heap.isEmpty());
    }

    private static void testDynamicArray() {
        System.out.println("-- DynamicArray --");

        DynamicArray empty = new DynamicArray();
        check("empty size is 0", empty.size() == 0);
        check("empty contains returns false", !empty.contains(5));

        DynamicArray one = new DynamicArray();
        one.add(42);
        check("one element size", one.size() == 1);
        check("one element get", one.get(0) == 42);
        check("one element contains", one.contains(42));

        DynamicArray multi = new DynamicArray();
        for (int i = 0; i < 10; i++) {
            multi.add(i);
        }
        check("multi size", multi.size() == 10);
        multi.add(5, 99);
        check("insert middle shifts size", multi.size() == 11);
        check("insert middle value", multi.get(5) == 99);
        check("insert middle shifted old value", multi.get(6) == 5);

        int removed = multi.remove(5);
        check("remove returns inserted value", removed == 99);
        check("remove restores size", multi.size() == 10);
        check("remove restores order", multi.get(5) == 5);

        DynamicArray dup = new DynamicArray();
        dup.add(7);
        dup.add(7);
        dup.add(7);
        check("duplicate values contains", dup.contains(7));
        check("duplicate values size", dup.size() == 3);

        boolean threwLow = false;
        try {
            multi.get(-1);
        } catch (IndexOutOfBoundsException e) {
            threwLow = true;
        }
        check("invalid low index throws", threwLow);

        boolean threwHigh = false;
        try {
            multi.get(1000);
        } catch (IndexOutOfBoundsException e) {
            threwHigh = true;
        }
        check("invalid high index throws", threwHigh);

        DynamicArray large = new DynamicArray();
        Random random = new Random(42);
        for (int i = 0; i < 50000; i++) {
            large.add(random.nextInt(1000000));
        }
        check("large input size", large.size() == 50000);
        check("large input first element accessible", large.get(0) >= 0);
        check("large input last element accessible", large.get(49999) >= 0);
    }

    private static void testLinkedList() {
        System.out.println("-- LinkedList --");

        LinkedList empty = new LinkedList();
        check("empty size is 0", empty.size() == 0);
        check("empty contains returns false", !empty.contains(5));

        LinkedList one = new LinkedList();
        one.add(42);
        check("one element size", one.size() == 1);
        check("one element get", one.get(0) == 42);
        check("one element contains", one.contains(42));

        LinkedList multi = new LinkedList();
        for (int i = 0; i < 10; i++) {
            multi.add(i);
        }
        check("multi size", multi.size() == 10);
        multi.add(5, 99);
        check("insert middle shifts size", multi.size() == 11);
        check("insert middle value", multi.get(5) == 99);
        check("insert middle shifted old value", multi.get(6) == 5);

        int removed = multi.remove(5);
        check("remove returns inserted value", removed == 99);
        check("remove restores size", multi.size() == 10);
        check("remove restores order", multi.get(5) == 5);

        LinkedList dup = new LinkedList();
        dup.add(7);
        dup.add(7);
        dup.add(7);
        check("duplicate values contains", dup.contains(7));
        check("duplicate values size", dup.size() == 3);

        boolean threwLow = false;
        try {
            multi.get(-1);
        } catch (IndexOutOfBoundsException e) {
            threwLow = true;
        }
        check("invalid low index throws", threwLow);

        boolean threwHigh = false;
        try {
            multi.get(1000);
        } catch (IndexOutOfBoundsException e) {
            threwHigh = true;
        }
        check("invalid high index throws", threwHigh);

        LinkedList large = new LinkedList();
        Random random = new Random(42);
        for (int i = 0; i < 50000; i++) {
            large.add(random.nextInt(1000000));
        }
        check("large input size", large.size() == 50000);
        check("large input last element accessible", large.get(49999) >= 0);
    }

    private static void testMinHeap() {
        System.out.println("-- MinHeap --");

        MinHeap empty = new MinHeap();
        check("empty size is 0", empty.isEmpty());

        boolean threwOnEmptyPeek = false;
        try {
            empty.peekMin();
        } catch (IllegalStateException e) {
            threwOnEmptyPeek = true;
        }
        check("peekMin on empty throws", threwOnEmptyPeek);

        boolean threwOnEmptyExtract = false;
        try {
            empty.extractMin();
        } catch (IllegalStateException e) {
            threwOnEmptyExtract = true;
        }
        check("extractMin on empty throws", threwOnEmptyExtract);

        MinHeap one = new MinHeap();
        one.insert(42);
        check("one element peekMin", one.peekMin() == 42);
        check("one element extractMin", one.extractMin() == 42);
        check("after extract heap is empty", one.isEmpty());

        MinHeap multi = new MinHeap();
        int[] values = {5, 3, 8, 1, 9, 2, 7};
        for (int v : values) {
            multi.insert(v);
            check("heap property after insert " + v, multi.isValidHeap());
        }

        MinHeap dup = new MinHeap();
        dup.insert(4);
        dup.insert(4);
        dup.insert(4);
        check("duplicate values heap valid", dup.isValidHeap());
        check("duplicate extractMin", dup.extractMin() == 4);

        boolean nonDecreasing = true;
        int previous = Integer.MIN_VALUE;
        while (!multi.isEmpty()) {
            int extracted = multi.extractMin();
            if (extracted < previous) {
                nonDecreasing = false;
            }
            previous = extracted;
            if (!multi.isEmpty() && !multi.isValidHeap()) {
                nonDecreasing = false;
            }
        }
        check("extractMin sequence is non-decreasing", nonDecreasing);

        MinHeap large = new MinHeap();
        Random random = new Random(42);
        for (int i = 0; i < 50000; i++) {
            large.insert(random.nextInt(1000000));
        }
        check("large heap valid", large.isValidHeap());
        int last = Integer.MIN_VALUE;
        boolean largeSorted = true;
        for (int i = 0; i < 50000; i++) {
            int val = large.extractMin();
            if (val < last) {
                largeSorted = false;
            }
            last = val;
        }
        check("large heap extraction order", largeSorted);
    }
}
