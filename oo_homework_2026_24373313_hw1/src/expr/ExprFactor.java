package expr;

import poly.Poly;

import java.math.BigInteger;

public class ExprFactor implements Factor {
    private final Expr expr;
    private final BigInteger exp;

    public ExprFactor(Expr expr, BigInteger exp) {
        this.expr = expr;
        this.exp = exp;
    }

    @Override
    public Poly toPoly() {
        return expr.toPoly().powPoly(exp);
    }
}
