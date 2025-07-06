package data;

import java.util.LinkedList;
import java.util.List;

public class CircularBlockList {
    private final int capacity;
    private final LinkedList<Block> list;

    public CircularBlockList(int capacity) {
        this.capacity = capacity;
        this.list = new LinkedList<>();
    }

    public void push(Block block) {
        if (list.size() == capacity) {
            int shiftY = list.getFirst().height;
            list.removeFirst(); // Hapus balok paling bawah

            // Geser semua balok yang tersisa ke bawah
            for (Block b : list) {
                b.y += shiftY;
            }
        }

        // Tempatkan balok baru tepat di atas balok terakhir (setelah penggeseran)
        if (!list.isEmpty()) {
            Block top = list.getLast();
            block.y = top.y - block.height;
        }

        // Tambahkan balok ke list setelah semua posisi fix
        list.addLast(block);
    }



    public Block peek() {
        return list.isEmpty() ? null : list.getLast();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public void clear() {
        list.clear();
    }

    public List<Block> getAllBlocks() {
        return new LinkedList<>(list);
    }

    public int size() {
        return list.size();
    }
}
