import java.math.BigInteger;
import java.util.ArrayList;

public class Var implements Factor {
    private final String var;
    private final BigInteger exp;
    
    public Var(String var, BigInteger exp) {
        this.var = var;
        this.exp = exp;
    }
    
    @Override
    public Poly toPoly() {
        ArrayList<Mono> monos = new ArrayList<>();
        if (var.equals("x")) {
            monos.add(new Mono(BigInteger.ONE, exp, BigInteger.ZERO));
        } else {
            monos.add(new Mono(BigInteger.ONE, BigInteger.ZERO, exp));
        }
        return new Poly(monos);
    }
    
    @Override
    public Factor deepCopy() {
        return new Var(var, exp);
    }
    
    @Override
    public Factor substitute(Factor arg) {
        if (!var.equals("x")) {
            return deepCopy();
        }
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
            return var;
        } else {
            return var + "^" + exp;
        }
    }
}