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
    
    public Term deepCopy() {
        Term res = new Term();
        if (this.sign != null) {
            res.setSign(new Token(Token.Type.ADD, this.sign.getContent()));
        }
        for (Factor factor : factors) {
            res.addFactor(factor.deepCopy());
        }
        return res;
    }
    
    public Term substitute(Factor arg) {
        Term res = new Term();
        if (this.sign != null) {
            res.setSign(new Token(Token.Type.ADD, this.sign.getContent()));
        }
        for (Factor factor : factors) {
            res.addFactor(factor.substitute(arg));
        }
        return res;
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
    
    @Override
    public String toString() {
        if (factors.isEmpty()) {
            return "1";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < factors.size(); i++) {
            sb.append(factors.get(i).toString());
            if (i != factors.size() - 1) {
                sb.append("*");
            }
        }
        return sb.toString();
    }
}