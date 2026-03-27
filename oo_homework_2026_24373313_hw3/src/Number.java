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
        monos.add(new Mono(num, BigInteger.ZERO, BigInteger.ZERO));
        return new Poly(monos);
    }
    
    @Override
    public Factor deepCopy() {
        return new Number(num);
    }
    
    @Override
    public Factor substitute(Factor arg) {
        return deepCopy();
    }
    
    @Override
    public String toString() {
        return num.toString();
    }
}