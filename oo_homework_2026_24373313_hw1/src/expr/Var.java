package expr;

import poly.Mono;
import poly.Poly;

import java.math.BigInteger;
import java.util.ArrayList;

public class Var implements Factor {
    private final BigInteger exp;

    public Var(String var, BigInteger exp) {
        this.exp = exp;
    }

    @Override
    public Poly toPoly() {
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(new Mono(BigInteger.ONE, exp));
        return new Poly(monos);
    }
}
