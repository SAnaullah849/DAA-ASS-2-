import csv
import matplotlib.pyplot as plt

def read_csv(path):
    with open(path) as f:
        return list(csv.DictReader(f))

def plot_workload1():
    rows = read_csv("results/tables/workload1_random_access.csv")
    sizes = sorted(set(int(r["n"]) for r in rows))
    for structure in ["DynamicArray", "LinkedList"]:
        times = [int(r["avg_time_ns"]) for r in rows if r["structure"] == structure]
        plt.plot(sizes, times, marker="o", label=structure)
    plt.xscale("log")
    plt.yscale("log")
    plt.xlabel("n")
    plt.ylabel("avg time (ns)")
    plt.title("Workload 1: Random Access - Execution Time vs n")
    plt.legend()
    plt.tight_layout()
    plt.savefig("results/plots/workload1_time.png")
    plt.close()

def plot_workload2():
    rows = read_csv("results/tables/workload2_search.csv")
    sizes = sorted(set(int(r["n"]) for r in rows))
    fig, axes = plt.subplots(1, 2, figsize=(11, 4.5))
    for structure in ["DynamicArray", "LinkedList"]:
        times = [int(r["avg_time_ns"]) for r in rows if r["structure"] == structure]
        axes[0].plot(sizes, times, marker="o", label=structure)
        comparisons = [int(r["total_comparisons"]) for r in rows if r["structure"] == structure]
        axes[1].plot(sizes, comparisons, marker="o", label=structure)
    axes[0].set_xscale("log")
    axes[0].set_yscale("log")
    axes[0].set_xlabel("n")
    axes[0].set_ylabel("avg time (ns)")
    axes[0].set_title("Workload 2: Search - Execution Time vs n")
    axes[0].legend()
    axes[1].set_xscale("log")
    axes[1].set_yscale("log")
    axes[1].set_xlabel("n")
    axes[1].set_ylabel("total comparisons")
    axes[1].set_title("Workload 2: Search - Comparisons vs n")
    axes[1].legend()
    plt.tight_layout()
    plt.savefig("results/plots/workload2_time_and_comparisons.png")
    plt.close()

def plot_workload3():
    rows = read_csv("results/tables/workload3_insert_remove.csv")
    sizes = sorted(set(int(r["n"]) for r in rows))
    fig, axes = plt.subplots(2, 2, figsize=(11, 8))
    combos = [
        ("insert", "beginning", axes[0][0], "Insert at beginning"),
        ("remove", "beginning", axes[0][1], "Remove at beginning"),
        ("insert", "middle", axes[1][0], "Insert at middle"),
        ("remove", "middle", axes[1][1], "Remove at middle"),
    ]
    for operation, position, ax, title in combos:
        for structure in ["DynamicArray", "LinkedList"]:
            times = [
                int(r["avg_time_ns"]) for r in rows
                if r["structure"] == structure and r["operation"] == operation and r["position"] == position
            ]
            ax.plot(sizes, times, marker="o", label=structure)
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.set_xlabel("n")
        ax.set_ylabel("avg time (ns)")
        ax.set_title(title)
        ax.legend()
    plt.tight_layout()
    plt.savefig("results/plots/workload3_time.png")
    plt.close()

def plot_workload4():
    rows = read_csv("results/tables/workload4_priority_processing.csv")
    sizes = [int(r["n"]) for r in rows]
    insert_times = [int(r["avg_insert_time_ns"]) for r in rows]
    extract_times = [int(r["avg_extract_time_ns"]) for r in rows]
    comparisons = [int(r["total_comparisons"]) for r in rows]

    fig, axes = plt.subplots(1, 2, figsize=(11, 4.5))
    axes[0].plot(sizes, insert_times, marker="o", label="insert (n times)")
    axes[0].plot(sizes, extract_times, marker="o", label="extractMin (n times)")
    axes[0].set_xscale("log")
    axes[0].set_yscale("log")
    axes[0].set_xlabel("n")
    axes[0].set_ylabel("avg total time (ns)")
    axes[0].set_title("Workload 4: MinHeap - Execution Time vs n")
    axes[0].legend()

    axes[1].plot(sizes, comparisons, marker="o", color="tab:red", label="comparisons")
    axes[1].set_xscale("log")
    axes[1].set_yscale("log")
    axes[1].set_xlabel("n")
    axes[1].set_ylabel("total comparisons")
    axes[1].set_title("Workload 4: MinHeap - Comparisons vs n")
    axes[1].legend()
    plt.tight_layout()
    plt.savefig("results/plots/workload4_time_and_comparisons.png")
    plt.close()

plot_workload1()
plot_workload2()
plot_workload3()
plot_workload4()
print("plots generated")
