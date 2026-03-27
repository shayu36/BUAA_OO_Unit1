import java.math.BigInteger;
import java.util.ArrayList;

public class ExpFactor implements Factor {
    private final Factor factor;
    private final BigInteger exp;
    
    public ExpFactor(Factor factor, BigInteger exp) {
        this.factor = factor;
        this.exp = exp;
    }
    
    @Override
    public Poly toPoly() {
        Poly inner = factor.toPoly().normalize();
        Poly total = inner.mulConst(exp).normalize();
        
        ArrayList<Mono> monos = new ArrayList<>();
        monos.add(new Mono(BigInteger.ONE, BigInteger.ZERO, total));
        return new Poly(monos);
    }
    
    @Override
    public Factor deepCopy() {
        return new ExpFactor(factor.deepCopy(), exp);
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return new ExpFactor(factor.substitute(arg), exp);
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("exp(").append(factor.toString()).append(")");
        if (!exp.equals(BigInteger.ONE)) {
            sb.append("^").append(exp);
        }
        return sb.toString();
    }
}