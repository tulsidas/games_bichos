package bichos.common.model;

import org.apache.mina.common.ByteBuffer;

public class DigResult implements Comparable<DigResult> {

    public int i, j, res;

    public DigResult(int i, int j, int res) {
        this.i = i;
        this.j = j;
        this.res = res;
    }

    @Override
    public String toString() {
        return "(" + i + "," + j + ") = " + res;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + i;
        result = prime * result + j;
        result = prime * result + res;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DigResult other = (DigResult) obj;
        if (i != other.i) {
            return false;
        }
        if (j != other.j) {
            return false;
        }
        if (res != other.res) {
            return false;
        }
        return true;
    }

    public int compareTo(DigResult otro) {
        if (i == otro.i) {
            return j - otro.j;
        }
        else {
            return i - otro.i;
        }
    }

    public static void writeTo(DigResult dr, ByteBuffer buff) {
        buff.put((byte) dr.i);
        buff.put((byte) dr.j);
        buff.put((byte) dr.res);
    }

    public static DigResult readFrom(ByteBuffer buff) {
        int i = buff.get();
        int j = buff.get();
        int res = buff.get();

        return new DigResult(i, j, res);
    }

}