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
    
    @Override
    public Factor deepCopy() {
        return new Var("x", exp);
    }
    
    @Override
    public Factor substitute(Factor arg) {
        if (exp.equals(BigInteger.ZERO)) {
            return new Number(BigInteger.ONE);
        }
        if (exp.equals(BigInteger.ONE)) {
            return arg.deepCopy();
        }
        return new ExprFactor(Expr.fromFactor(arg.deepCopy()), exp);
    }
    
    @Override
    public String toString() {
        if (exp.equals(BigInteger.ZERO)) {
            return "1";
        } else if (exp.equals(BigInteger.ONE)) {
            return "x";
        } else {
            return "x^" + exp;
        }
    }
}