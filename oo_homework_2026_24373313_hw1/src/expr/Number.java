package expr;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;
import java.util.ArrayList;

public class Number implements Factor {
    private final BigInteger num;

    public Number(BigInteger num) {
        this.num = num;
    }

    @Override
    public Poly toPoly() {
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(new Mono(num, BigInteger.ZERO));
        return new Poly(monos);
    }
}
