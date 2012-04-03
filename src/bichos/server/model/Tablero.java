package bichos.server.model;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Random;

import bichos.common.model.DigResult;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

public class Tablero {
    private boolean[][] casillas;

    /** cuantas minas tiene el tablero */
    private int minas;

    /** dimensiones del tablero */
    private int size;

    public Tablero(int size, int minas) {
        if (minas > size * size) {
            throw new IllegalArgumentException("Muchas minas");
        }

        this.size = size;
        this.minas = minas;

        reset();
    }

    public void reset() {
        Random rnd = new SecureRandom();

        casillas = new boolean[size][size];

        // pongo minas
        for (int i = 0; i < minas; i++) {
            int f, c;
            do {
                f = rnd.nextInt(size);
                c = rnd.nextInt(size);
            }
            while (casillas[f][c]);

            // casillas[f][c] == false, la marco true
            casillas[f][c] = true;
        }
    }

    public int getNum(int i, int j) {
        if (hayMinaEnCasilla(i, j)) {
            // mina
            return -1;
        }
        else {
            int ret = 0;

            if (hayMinaEnCasilla(i - 1, j - 1)) {
                ret++;
            }
            if (hayMinaEnCasilla(i, j - 1)) {
                ret++;
            }
            if (hayMinaEnCasilla(i + 1, j - 1)) {
                ret++;
            }
            if (hayMinaEnCasilla(i - 1, j)) {
                ret++;
            }
            if (hayMinaEnCasilla(i + 1, j)) {
                ret++;
            }
            if (hayMinaEnCasilla(i - 1, j + 1)) {
                ret++;
            }
            if (hayMinaEnCasilla(i, j + 1)) {
                ret++;
            }
            if (hayMinaEnCasilla(i + 1, j + 1)) {
                ret++;
            }

            return ret;
        }
    }

    private boolean hayMinaEnCasilla(int i, int j) {
        if (i >= 0 && i < size && j >= 0 && j < size) {
            return casillas[i][j];
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (int i = 0; i < size; i++) {
            ret += "|";
            for (int j = 0; j < size; j++) {
                ret += (casillas[i][j] ? "X|" : (getNum(i, j)) + "|");
            }
            ret += "\n";
        }

        return ret;
    }

    public void fill(final int i, final int j, Collection<DigResult> ret) {
        if (i >= 0 && i < size && j >= 0 && j < size) {
            // en rango

            boolean visitada = Iterables.any(ret, new Predicate<DigResult>() {
                public boolean apply(DigResult dr) {
                    return dr.i == i && dr.j == j;
                }
            });

            if (visitada) {
                // casilla ya visitada
                return;
            }

            int val = getNum(i, j);
            if (val == 0) {
                // vacio
                ret.add(new DigResult(i, j, val));

                // expando
                fill(i - 1, j - 1, ret);
                fill(i, j - 1, ret);
                fill(i + 1, j - 1, ret);
                fill(i - 1, j, ret);
                fill(i + 1, j, ret);
                fill(i - 1, j + 1, ret);
                fill(i, j + 1, ret);
                fill(i + 1, j + 1, ret);
            }
            else if (val > 0) {
                // numero
                ret.add(new DigResult(i, j, val));
            }
        }
    }
    // public static void main(String[] args) {
    // // Tablero t = new Tablero(16, 51);
    // Tablero t = new Tablero(5, 2);
    //
    // t.casillas[0][0] = true;
    //
    // System.out.println(t);
    //
    // Collection<DigResult> r = Sets.newTreeSet();
    // t.fill(0, 0, r);
    //
    // System.out.println(Iterables.toString(r));
    // }
}