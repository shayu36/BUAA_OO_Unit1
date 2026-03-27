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
    
    @Override
    public Factor deepCopy() {
        return new ExprFactor(expr.deepCopy(), exp);
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return new ExprFactor(expr.substitute(arg), exp);
    }
    
    @Override
    public String toString() {
        if (exp.equals(BigInteger.ONE)) {
            return "(" + expr.toString() + ")";
        } else {
            return "(" + expr.toString() + ")^" + exp;
        }
    }
}