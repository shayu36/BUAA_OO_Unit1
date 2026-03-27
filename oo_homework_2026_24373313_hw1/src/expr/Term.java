package expr;

import parser.Token;
import poly.Mono;
import poly.Poly;

import java.math.BigInteger;
import java.util.ArrayList;

public class Term {
    private final ArrayList<Factor> factors;
    private Token sign;

    public Term() {
        this.factors = new ArrayList<>();
    }

    public void addFactor(Factor factor) {
        this.factors.add(factor);
    }

    public void setSign(Token token) {
        this.sign = token;
    }

    public Poly toPoly() {
        ArrayList<Mono> init = new ArrayList<>();
        init.add(new Mono(BigInteger.ONE, BigInteger.ZERO));
        Poly res = new Poly(init);

        for (Factor factor : factors) {
            res = res.mulPoly(factor.toPoly());
        }

        if (sign != null && sign.getContent().equals("-")) {
            ArrayList<Mono> minus = new ArrayList<>();
            minus.add(new Mono(BigInteger.valueOf(-1), BigInteger.ZERO));
            res = res.mulPoly(new Poly(minus));
        }
        return res;
    }
}
