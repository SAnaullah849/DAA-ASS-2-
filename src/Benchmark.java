import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class Benchmark {

    private static final int[] SIZES = {100, 1000, 10000, 100000};
    private static final int REPETITIONS = 5;
    private static final long SEED = 42;
    private static final long INDEX_SEED = 43;
    private static final long VALUE_SEED = 44;

    public static void main(String[] args) throws IOException {
        warmUp();
        workload1RandomAccess();
        workload2Search();
        workload3InsertRemove();
        workload4PriorityProcessing();
        System.out.println("Benchmarking complete.");
    }

    private static void warmUp() {
        Random random = new Random(1);
        DynamicArray array = new DynamicArray();
        LinkedList list = new LinkedList();
        MinHeap heap = new MinHeap();
        for (int i = 0; i < 20000; i++) {
            int value = random.nextInt(1000000);
            array.add(value);
            list.add(value);
            heap.insert(value);
        }
        for (int i = 0; i < 20000; i++) {
            array.get(i);
            list.get(i);
            array.contains(i);
            list.contains(i);
        }
        for (int i = 0; i < 20000; i++) {
            array.add(0, i);
            array.remove(0);
            list.add(0, i);
            list.remove(0);
        }
        while (!heap.isEmpty()) {
            heap.extractMin();
        }
        array.resetCounters();
        list.resetCounters();
    }

    private static DynamicArray buildArray(int n, Random random) {
        DynamicArray array = new DynamicArray(n);
        for (int i = 0; i < n; i++) {
            array.add(random.nextInt(1000000));
        }
        return array;
    }

    private static LinkedList buildLinkedList(int n, Random random) {
        LinkedList list = new LinkedList();
        for (int i = 0; i < n; i++) {
            list.add(random.nextInt(1000000));
        }
        return list;
    }

    private static void workload1RandomAccess() throws IOException {
        FileWriter writer = new FileWriter("results/tables/workload1_random_access.csv");
        writer.write("structure,n,avg_time_ns,total_accesses,theoretical\n");

        for (int n : SIZES) {
            int[] indices = new int[10000];
            Random indexRandom = new Random(INDEX_SEED);
            for (int i = 0; i < indices.length; i++) {
                indices[i] = indexRandom.nextInt(n);
            }

            long totalTimeArray = 0;
            long accessesArray = 0;
            for (int rep = 0; rep < REPETITIONS; rep++) {
                Random dataRandom = new Random(SEED);
                DynamicArray array = buildArray(n, dataRandom);
                array.resetCounters();
                long start = System.nanoTime();
                for (int idx : indices) {
                    array.get(idx);
                }
                long end = System.nanoTime();
                totalTimeArray += (end - start);
                accessesArray = array.getAccessCount();
            }
            writer.write("DynamicArray," + n + "," + (totalTimeArray / REPETITIONS) + "," + accessesArray + ",O(1)\n");

            long totalTimeList = 0;
            long accessesList = 0;
            for (int rep = 0; rep < REPETITIONS; rep++) {
                Random dataRandom = new Random(SEED);
                LinkedList list = buildLinkedList(n, dataRandom);
                list.resetCounters();
                long start = System.nanoTime();
                for (int idx : indices) {
                    list.get(idx);
                }
                long end = System.nanoTime();
                totalTimeList += (end - start);
                accessesList = list.getAccessCount();
            }
            writer.write("LinkedList," + n + "," + (totalTimeList / REPETITIONS) + "," + accessesList + ",O(n)\n");

            System.out.println("Workload1 n=" + n + " done");
        }
        writer.close();
    }

    private static void workload2Search() throws IOException {
        FileWriter writer = new FileWriter("results/tables/workload2_search.csv");
        writer.write("structure,n,avg_time_ns,total_comparisons,theoretical\n");

        for (int n : SIZES) {
            int[] searchValues = new int[1000];
            Random valueRandom = new Random(VALUE_SEED);
            for (int i = 0; i < searchValues.length; i++) {
                searchValues[i] = valueRandom.nextInt(1000000);
            }

            long totalTimeArray = 0;
            long comparisonsArray = 0;
            for (int rep = 0; rep < REPETITIONS; rep++) {
                Random dataRandom = new Random(SEED);
                DynamicArray array = buildArray(n, dataRandom);
                array.resetCounters();
                long start = System.nanoTime();
                for (int value : searchValues) {
                    array.contains(value);
                }
                long end = System.nanoTime();
                totalTimeArray += (end - start);
                comparisonsArray = array.getComparisonCount();
            }
            writer.write("DynamicArray," + n + "," + (totalTimeArray / REPETITIONS) + "," + comparisonsArray + ",O(n)\n");

            long totalTimeList = 0;
            long comparisonsList = 0;
            for (int rep = 0; rep < REPETITIONS; rep++) {
                Random dataRandom = new Random(SEED);
                LinkedList list = buildLinkedList(n, dataRandom);
                list.resetCounters();
                long start = System.nanoTime();
                for (int value : searchValues) {
                    list.contains(value);
                }
                long end = System.nanoTime();
                totalTimeList += (end - start);
                comparisonsList = list.getComparisonCount();
            }
            writer.write("LinkedList," + n + "," + (totalTimeList / REPETITIONS) + "," + comparisonsList + ",O(n)\n");

            System.out.println("Workload2 n=" + n + " done");
        }
        writer.close();
    }

    private static void workload3InsertRemove() throws IOException {
        FileWriter writer = new FileWriter("results/tables/workload3_insert_remove.csv");
        writer.write("structure,operation,position,n,avg_time_ns,total_movements,theoretical\n");

        for (int n : SIZES) {
            runInsertRemove(writer, n, 0, "beginning");
            runInsertRemove(writer, n, n / 2, "middle");
            System.out.println("Workload3 n=" + n + " done");
        }
        writer.close();
    }

    private static void runInsertRemove(FileWriter writer, int n, int position, String label) throws IOException {
        long totalTimeArrayInsert = 0;
        long movementsArrayInsert = 0;
        long totalTimeArrayRemove = 0;
        long movementsArrayRemove = 0;

        for (int rep = 0; rep < REPETITIONS; rep++) {
            Random dataRandom = new Random(SEED);
            DynamicArray array = buildArray(n, dataRandom);
            array.resetCounters();
            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                array.add(position, i);
            }
            long end = System.nanoTime();
            totalTimeArrayInsert += (end - start);
            movementsArrayInsert = array.getMovementCount();

            array.resetCounters();
            start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                array.remove(position);
            }
            end = System.nanoTime();
            totalTimeArrayRemove += (end - start);
            movementsArrayRemove = array.getMovementCount();
        }
        writer.write("DynamicArray,insert," + label + "," + n + "," + (totalTimeArrayInsert / REPETITIONS) + "," + movementsArrayInsert + ",O(n)\n");
        writer.write("DynamicArray,remove," + label + "," + n + "," + (totalTimeArrayRemove / REPETITIONS) + "," + movementsArrayRemove + ",O(n)\n");

        long totalTimeListInsert = 0;
        long movementsListInsert = 0;
        long totalTimeListRemove = 0;
        long movementsListRemove = 0;

        for (int rep = 0; rep < REPETITIONS; rep++) {
            Random dataRandom = new Random(SEED);
            LinkedList list = buildLinkedList(n, dataRandom);
            list.resetCounters();
            long start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                list.add(position, i);
            }
            long end = System.nanoTime();
            totalTimeListInsert += (end - start);
            movementsListInsert = list.getMovementCount();

            list.resetCounters();
            start = System.nanoTime();
            for (int i = 0; i < 1000; i++) {
                list.remove(position);
            }
            end = System.nanoTime();
            totalTimeListRemove += (end - start);
            movementsListRemove = list.getMovementCount();
        }
        String theoretical = label.equals("beginning") ? "O(1)" : "O(n)";
        writer.write("LinkedList,insert," + label + "," + n + "," + (totalTimeListInsert / REPETITIONS) + "," + movementsListInsert + "," + theoretical + "\n");
        writer.write("LinkedList,remove," + label + "," + n + "," + (totalTimeListRemove / REPETITIONS) + "," + movementsListRemove + "," + theoretical + "\n");
    }

    private static void workload4PriorityProcessing() throws IOException {
        FileWriter writer = new FileWriter("results/tables/workload4_priority_processing.csv");
        writer.write("n,avg_insert_time_ns,avg_extract_time_ns,total_comparisons,theoretical_batch_complexity,order_valid\n");

        for (int n : SIZES) {
            int[] values = new int[n];
            Random dataRandom = new Random(SEED);
            for (int i = 0; i < n; i++) {
                values[i] = dataRandom.nextInt(1000000);
            }

            long totalInsertTime = 0;
            long totalExtractTime = 0;
            long comparisons = 0;
            boolean orderValid = true;

            for (int rep = 0; rep < REPETITIONS; rep++) {
                MinHeap heap = new MinHeap(n);
                heap.resetCounters();

                long start = System.nanoTime();
                for (int i = 0; i < n; i++) {
                    heap.insert(values[i]);
                }
                long end = System.nanoTime();
                totalInsertTime += (end - start);

                start = System.nanoTime();
                int previous = Integer.MIN_VALUE;
                for (int i = 0; i < n; i++) {
                    int extracted = heap.extractMin();
                    if (extracted < previous) {
                        orderValid = false;
                    }
                    previous = extracted;
                }
                end = System.nanoTime();
                totalExtractTime += (end - start);

                comparisons = heap.getComparisonCount();
            }

            writer.write(n + "," + (totalInsertTime / REPETITIONS) + "," + (totalExtractTime / REPETITIONS) + "," + comparisons + ",O(n log n)," + orderValid + "\n");
            System.out.println("Workload4 n=" + n + " done");
        }
        writer.close();
    }
}
