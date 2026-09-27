# Assignment 2 — Algorithmic Analysis, Correctness and Performance Trade-offs

## 1. Overview

This project implements three data structures in Java: a Dynamic Array, a singly Linked List,
and a Min-Heap. Each structure supports the required operations (`add`, `add(index, x)`,
`remove(index)`, `get(index)`, `contains(x)` for the array and list; `insert`, `peekMin`,
`extractMin` for the heap). The goal of the assignment is to prove correctness of selected
operations using loop invariants, derive the asymptotic complexity of every operation, and then
compare that theory against measured performance across four workloads and four input sizes
(n = 100, 1,000, 10,000, 100,000).

All benchmarks in this report were actually executed (OpenJDK 25.0.1, `System.nanoTime()`, 5
repetitions averaged per data point, `Random(seed)` with fixed seeds for reproducibility). The
raw CSV output is in `results/tables/` and the plots are in `results/plots/`.

## 2. Complexity Analysis

### Dynamic Array

| Operation | Best | Average | Worst | Auxiliary Space |
|---|---|---|---|---|
| add(x) | Θ(1) | Θ(1) amortized | O(n) (on resize) | O(n) temporarily on resize; O(1) otherwise |
| add(index, x) | Ω(1) (index = end) | Θ(n) | O(n) (index = 0 or resize) | O(n) temporarily on resize; O(1) otherwise |
| remove(index) | Ω(1) (index = end) | Θ(n) | O(n) (index = 0) | O(1) |
| get(index) | Θ(1) | Θ(1) | Θ(1) | O(1) |
| contains(x) | Ω(1) (first element) | Θ(n) | O(n) | O(1) |

`get(index)` is a direct array offset calculation, so it is Θ(1) in every case regardless of
where the index falls. `add(x)` is Θ(1) amortized because the underlying array doubles in size
whenever it is full; the occasional O(n) copy is spread evenly across n insertions. A resize temporarily allocates a second array, requiring O(n) extra space. `add(index, x)`
and `remove(index)` both require shifting every element after the target position, so their cost
is proportional to how many elements sit to the right of the index — best when the index is at
the end (no shifting), worst when the index is 0 (the whole array shifts).

### Linked List (singly linked, with tail pointer)

| Operation | Best | Average | Worst | Auxiliary Space |
|---|---|---|---|---|
| add(x) | Θ(1) | Θ(1) | Θ(1) | O(1) |
| add(index, x) | Ω(1) (index = 0) | Θ(n) | O(n) (index = size) | O(1) |
| remove(index) | Ω(1) (index = 0) | Θ(n) | O(n) (index = size - 1) | O(1) |
| get(index) | Ω(1) (index = 0) | Θ(n) | O(n) (index = size - 1) | O(1) |
| contains(x) | Ω(1) (first element) | Θ(n) | O(n) | O(1) |

Because the list keeps a `tail` pointer, appending with `add(x)` is Θ(1) — there is no traversal.
Every operation that needs to reach an arbitrary index, however, must walk the list one node at a
time from `head`, so `get`, `add(index, x)`, and `remove(index)` are all Θ(n) on average and in
the worst case, with the best case only occurring when the target is at (or very near) the head.

### Min-Heap (array-backed binary heap)

| Operation | Best | Average | Worst | Auxiliary Space |
|---|---|---|---|---|
| insert(x) | Ω(1) (new value is largest, no sift) | Θ(log n) | O(n) (on resize) | O(n) temporarily on resize; O(1) otherwise |
| peekMin() | Θ(1) | Θ(1) | Θ(1) | O(1) |
| extractMin() | Ω(1) (heap size 0 or 1) | Θ(log n) | O(log n) | O(1) |

`insert` appends at the end of the backing array and sifts the new value up toward the root; in
the best case the value is already larger than its parent and no swaps occur. The sift takes
O(log n); geometric growth makes a single insertion O(n) in the worst case when the backing array
resizes, while the average remains Θ(log n). `extractMin` swaps the root with the last element and
sifts it down, taking O(log n) on average and in the worst case. `peekMin` only reads the root, so
it is always Θ(1).

### Operations that look similar but differ in practice

`get(index)` on the Dynamic Array and `get(index)` on the Linked List share the same *signature*
but not the same complexity: Θ(1) versus Θ(n). This is the clearest example in this assignment of
why the underlying physical layout (contiguous memory vs. pointer-chained nodes) matters more than
the operation's name. Similarly, `add(index, x)` at `index = 0` is O(n) for the Dynamic Array
(everything shifts right) but Θ(1) for the Linked List (only the head pointer changes) — the same
call, on the same conceptual operation, has opposite costs depending on structure.

## 3. Correctness — Loop Invariant Proofs

### Proof 1: `DynamicArray.add(index, x)` (insertion via right-shift)

```java
for (int i = size; i > index; i--) {
    data[i] = data[i - 1];
}
data[index] = x;
```

**Loop invariant:** At the start of each iteration of the loop, for every position `k` with
`i < k <= size`, `data[k]` holds the value that was originally in `data[k - 1]` before the loop
began. In other words, the suffix of the array strictly to the right of `i` has already been
shifted one position to the right, and the region at or below `i` is still untouched.

**Initialization:** Before the first iteration, `i = size`. The claimed range `i < k <= size` is
empty, so the invariant holds vacuously.

**Maintenance:** Assume the invariant holds at the start of an iteration with some value of `i`
(where `i > index`). The loop body executes `data[i] = data[i - 1]`, which places the original
value of `data[i - 1]` into position `i`. Combined with the inductive hypothesis (positions
`i+1 .. size` are already correctly shifted), after this assignment positions `i .. size` all hold
the correctly shifted values. The loop then decrements `i`, so at the start of the next iteration
the invariant again holds for the new, larger range `i < k <= size` (with the new, smaller `i`).

**Termination:** The loop terminates when `i = index`. By the invariant, every position
`k` with `index < k <= size` now holds the value originally at `data[k - 1]`.

**Correctness:** After the loop exits, the statement `data[index] = x` places the new value at
position `index`. Combined with the invariant established at termination, the array now satisfies:
position `index` holds `x`, and every position after it holds what used to be one slot to its
left. This is exactly the definition of "insert `x` at `index`, shifting the rest right by one" —
so the loop, together with the final assignment, correctly implements insertion.

### Proof 2: `MinHeap.siftDown(index)` (restoring the heap property after `extractMin`)

```java
while (true) {
    left = 2*index+1; right = 2*index+2; smallest = index;
    if (left < size && data[left] < data[smallest]) smallest = left;
    if (right < size && data[right] < data[smallest]) smallest = right;
    if (smallest == index) break;
    swap(data[index], data[smallest]);
    index = smallest;
}
```

**Loop invariant:** At the start of each iteration, every parent-child relation in the active heap
satisfies the min-heap property except possibly the relations from the current `index` to its
children. The subtrees rooted at those children are valid min-heaps, and the relation from `index`
to its parent, if it has one, is valid. Thus the current node is the only possible source of a
heap-property violation.

**Initialization:** `extractMin` moves the last element into the root, reduces the active size, and
calls `siftDown(0)`. All descendant subtrees are unchanged valid min-heaps; only the root's
relations to its children may be invalid. The root has no parent, so the invariant holds.

**Maintenance:** In each iteration, the loop computes `smallest`, the index holding the minimum of
`data[index]` and its (up to two) children, using one or two key comparisons. If `smallest`
already equals `index`, the node satisfies the heap property relative to its children and, by the
invariant, so does every other subtree — the heap is fully valid, and the loop is about to exit.
Otherwise, the values at `index` and `smallest` are swapped. The old `index` now holds the smaller
child's value, so its relations to its children are valid. The smaller child's old position becomes
the new `index`; its parent now holds a value no greater than the value moved down, so the parent
relation remains valid. Other subtrees are unchanged. Therefore only the new current node may
violate the heap property, and the invariant is preserved one level lower.

**Termination:** Each iteration either exits (when `smallest == index`) or moves `index` strictly
one level deeper in the tree (`index = smallest`, and `smallest` is always a child of the old
`index`). Since the tree has finite height (⌊log₂ n⌋ + 1), the loop must exit after at most
O(log n) iterations, either because `smallest == index` or because `index` reaches a leaf (no
children exist, so `smallest` can never differ from `index`).

**Correctness at termination:** When the loop exits, `smallest == index`, so the current node is
no greater than either child. The invariant guarantees the parent relation and all other heap
relations are valid. Therefore the entire active array satisfies the min-heap property, proving
`siftDown` restores the heap after `extractMin` replaces the root.

The `Tests` runner also covers empty and singleton structures, duplicates, boundary and invalid
indices, and 50,000-element inputs. Seeded operation sequences are compared with `ArrayList`,
`java.util.LinkedList`, and `PriorityQueue`; the current run passes all 69 checks.

## 4. Experimental Setup

- **n** (elements initially stored): 100, 1,000, 10,000, 100,000
- **m** (operations per workload): 10,000 gets (Workload 1), 1,000 searches (Workload 2),
  1,000 insertions + 1,000 removals at two positions (Workload 3), n inserts + n extracts
  (Workload 4)
- **Repetitions:** every timed measurement is the average of 5 runs
- **Timing:** `System.nanoTime()`, wrapped tightly around only the operations being measured
  (input generation happens before the timer starts)
- **Random seed:** `Random(42)` for building the data structures' contents; separate fixed seeds
  (43 for access indices, 44 for search values) so that index/search generation does not
  accidentally correlate with the data itself
- **JVM warm-up:** a short warm-up pass exercises every operation once before any measurement is
  taken, to reduce JIT compilation noise in the results

To compile, test, and rerun the experiments from the project root in PowerShell:

```powershell
New-Item -ItemType Directory -Force out
javac -d out src\*.java
java -cp out Tests
java -cp out Benchmark
python -m pip install matplotlib
python plot_results.py
```

The benchmark overwrites the CSV files in `results/tables/`; the plotting script regenerates the
images in `results/plots/`.

## 5. Results

### Workload 1 — Random Access (`get`)

| Structure | n | avg time (ns) | total accesses | theoretical per get |
|---|---|---|---|---|
| DynamicArray | 100 | 404,420 | 10,000 | O(1) |
| LinkedList | 100 | 1,773,780 | 502,068 | O(n) |
| DynamicArray | 1,000 | 109,580 | 10,000 | O(1) |
| LinkedList | 1,000 | 12,070,700 | 4,953,268 | O(n) |
| DynamicArray | 10,000 | 313,300 | 10,000 | O(1) |
| LinkedList | 10,000 | 109,870,640 | 49,618,268 | O(n) |
| DynamicArray | 100,000 | 11,900 | 10,000 | O(1) |
| LinkedList | 100,000 | 1,295,374,820 | 493,288,268 | O(n) |

See `results/plots/workload1_time.png`.

### Workload 2 — Search (`contains`)

| Structure | n | avg time (ns) | total comparisons | theoretical per search |
|---|---|---|---|---|
| DynamicArray | 100 | 111,300 | 100,000 | O(n) |
| LinkedList | 100 | 449,940 | 100,000 | O(n) |
| DynamicArray | 1,000 | 959,960 | 999,496 | O(n) |
| LinkedList | 1,000 | 3,130,120 | 999,496 | O(n) |
| DynamicArray | 10,000 | 5,946,660 | 9,950,295 | O(n) |
| LinkedList | 10,000 | 30,991,160 | 9,950,295 | O(n) |
| DynamicArray | 100,000 | 64,557,140 | 95,206,221 | O(n) |
| LinkedList | 100,000 | 457,906,660 | 95,206,221 | O(n) |

See `results/plots/workload2_time_and_comparisons.png`.

### Workload 3 — Insertion and Removal

Full table in `results/tables/workload3_insert_remove.csv`. Representative rows at the two
extremes:

| Structure | Operation | Position | n | avg time (ns) | movements |
|---|---|---|---|---|---|
| DynamicArray | insert | beginning | 100,000 | 26,659,440 | 100,600,500 |
| LinkedList | insert | beginning | 100,000 | 46,020 | 1,000 |
| DynamicArray | insert | middle | 100,000 | 12,965,320 | 50,600,500 |
| LinkedList | insert | middle | 100,000 | 165,602,900 | 50,000,000 |
| DynamicArray | remove | beginning | 100,000 | 19,459,320 | 100,499,500 |
| LinkedList | remove | beginning | 100,000 | 46,740 | 0 |

See `results/plots/workload3_time.png`.

### Workload 4 — Priority Processing (Min-Heap)

| n | avg insert time (ns) | avg extract time (ns) | total comparisons | theoretical batch complexity | order valid |
|---|---|---|---|---|
| 100 | 11,760 | 18,720 | 1,035 | O(n log n) worst case | true |
| 1,000 | 97,900 | 237,360 | 17,226 | O(n log n) worst case | true |
| 10,000 | 901,920 | 2,152,960 | 239,329 | O(n log n) worst case | true |
| 100,000 | 3,621,620 | 21,612,300 | 3,059,125 | O(n log n) worst case | true |

The n insertions and n extractions each have O(n log n) total worst-case time; their combined
comparison count is O(n log n). `extractMin()` returned elements in non-decreasing order at every
n. See `results/plots/workload4_time_and_comparisons.png`.

## 6. Discussion

**Workload 1.** Linked List accesses increase from about 502,000 to 493 million as n increases
from 100 to 100,000, consistent with Θ(n) work per randomly selected `get`. Dynamic Array records
exactly 10,000 accesses at every size, consistent with Θ(1) work per `get`. Its measured times are
highly variable and non-monotonic, so these timings do not establish a smooth growth trend; the
fixed access count is the stronger evidence for the theoretical result.

**Workload 2.** Both structures scan close to n elements per search on average (`contains` rarely
finds a match since values are drawn from a range of one million), so total comparisons scale
almost exactly with n for both structures, and so does execution time. The Dynamic Array is
consistently 3–7× faster than the Linked List at the same n despite doing the *same number* of
comparisons — this is a textbook example of question 4/5 from Section 9: identical Big-O does not
mean identical wall-clock time. Sequential array access benefits from CPU cache locality (elements
sit next to each other in memory), while linked-list traversal follows pointers scattered across
the heap, causing more cache misses per comparison.

**Workload 3.** At the beginning of the structure, the Linked List is dramatically faster for both
insert and remove (Θ(1) — only the head pointer moves) while the Dynamic Array must shift up to
100,000 elements (visible as tens of millions of recorded movements and tens of milliseconds of
runtime at n = 100,000). At the middle position, the comparison flips: the Linked List must first
*traverse* to the middle (Θ(n)), which costs about as much as the Dynamic Array's shift, but the
Dynamic Array's shifting is a tight loop over contiguous memory while the Linked List's traversal
chases pointers — so the Dynamic Array is often faster even though both are O(n) here too.

**Workload 4.** The combined comparison count grows by 12.8× when n grows from 10,000 to 100,000,
close to the roughly 12.5× growth predicted by n log n. Over the same step, insertion time grows
about 4.0× and extraction time about 10.0×. These timing ratios are noisy and do not establish a
clean n log n curve by themselves; the comparison counts give clearer evidence of the expected
asymptotic work.

**Where results agree with theory:** Workload 1 (Dynamic Array access count constant, Linked List
access count and time increasing with n),
Workload 2 (both linear in n), and Workload 4 (n log n growth) all match the predicted asymptotic
behavior well.

**Where results diverge from a naive reading of Big-O:** Workload 3's "beginning" case is the
best illustration — Dynamic Array insert/remove at index 0 is technically the *same* O(n) class as
Linked List insert/remove at the middle, yet the Linked List at index 0 is 30–40× faster because
it is actually O(1), not O(n), at that specific position. This is a reminder that Big-O describes
worst-case *growth*, not a fixed real-world speed, and that the specific index matters as much as
the structure.

**Why identical Big-O classes run at different speeds (constant factors):** cache locality
(contiguous array vs. scattered nodes), per-element overhead (a linked list node carries an extra
pointer and a separate heap allocation per element, while the array's elements are packed edge to
edge), and JIT/branch-prediction effects all contribute constant-factor differences that Big-O
notation deliberately ignores.

**When each structure is the right choice:**
- **Dynamic Array** is preferable whenever indexed access (`get`) or scanning is the dominant
  operation, or when appends happen mostly at the end — its cache-friendly layout wins by a wide
  margin (Workload 1).
- **Linked List** is useful when insertions/removals happen mostly at the front (or via an
  existing reference to a node), where it needs no shifting at all (Workload 3, beginning case).
- **Min-Heap** is the right structure whenever the workload is "always process the current
  minimum next" — it never needs to fully sort the data, only maintain enough order to answer
  "what's smallest" in O(1) and update in O(log n), which is far cheaper than keeping a fully
  sorted structure.
- **Workload shape drives the choice** more than raw data volume: the same data, processed with a
  different *access pattern* (front vs. middle vs. random vs. priority order), favors a different
  structure each time, as directly shown by Workloads 1–4 above.

## 7. Design Recommendations

| Workload pattern | Recommended structure | Why |
|---|---|---|
| Frequent random indexed reads | Dynamic Array | O(1) get, cache-friendly |
| Frequent search by value, rare structural changes | Dynamic Array | Same O(n) as list, lower constants |
| Frequent insert/remove at the front | Linked List | O(1) vs. O(n) shifting |
| Frequent insert/remove at arbitrary/middle positions | Either (workload-dependent) | Both O(n); array usually faster due to locality |
| Always need the current minimum | Min-Heap | O(log n) insert/extract vs. O(n) scan-for-min |

## 8. Conclusion

The measured results largely confirm the theoretical complexity of every operation: Dynamic Array
`get` performs a constant number of counted accesses (its wall-clock samples are noisy), Linked List `get`/`contains`/middle insert-remove grow linearly,
and Min-Heap operations grow at an n log n rate. At the same time, the experiments show that Big-O
class alone does not predict real-world speed — constant factors driven by memory layout (cache
locality) repeatedly separated the Dynamic Array and Linked List even when both were in the same
asymptotic class, and the exact index used for an O(n) operation could silently turn it into an
O(1) operation instead. Choosing a data structure in practice therefore requires matching the
*workload's access pattern* to the structure's cheap operations, not just checking which structure
has the "better" Big-O for a single named operation.
