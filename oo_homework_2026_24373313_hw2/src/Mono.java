import java.math.BigInteger;
import java.util.ArrayList;

public class Mono {
    private final BigInteger coe;
    private final BigInteger xexp;
    private final Poly expPoly; // 表示 exp(expPoly)，为 0 时表示没有 exp 因子
    private final String combineKey;
    
    public Mono(BigInteger coe, BigInteger xexp) {
        this.coe = coe;
        this.xexp = xexp;
        this.expPoly = Poly.zero();
        this.combineKey = buildCombineKey();
    }
    
    public Mono(BigInteger coe, BigInteger xexp, Poly expPoly) {
        this.coe = coe;
        this.xexp = xexp;
        if (expPoly == null) {
            this.expPoly = Poly.zero();
        } else {
            this.expPoly = expPoly.copy().normalize();
        }
        this.combineKey = buildCombineKey();
    }
    
    private String buildCombineKey() {
        return xexp.toString() + "#" + expPoly.canonicalKey();
    }
    
    public String getCombineKey() {
        return combineKey;
    }
    
    public Mono monMulMon(Mono monoB) {
        return new Mono(
            this.coe.multiply(monoB.coe),
            this.xexp.add(monoB.xexp),
            this.expPoly.addPoly(monoB.expPoly)
        );
    }
    
    public boolean canCombine(Mono monoB) {
        return this.combineKey.equals(monoB.combineKey);
    }
    
    public Mono monAddMon(Mono monoB) {
        return new Mono(
            this.coe.add(monoB.coe),
            this.xexp,
            this.expPoly
        );
    }
    
    public boolean isZero() {
        return this.coe.equals(BigInteger.ZERO);
    }
    
    public BigInteger getCoe() {
        return coe;
    }
    
    public BigInteger getxexp() {
        return xexp;
    }
    
    public Poly getExpPoly() {
        return expPoly.copy();
    }
    
    private boolean hasOtherFactor() {
        return !xexp.equals(BigInteger.ZERO) || !expPoly.isZero();
    }
    
    //用于判断：exp(...) 里面的参数是否可以不加额外括号直接打印。
    private boolean canPolyBePrintedAsFactorDirectly(Poly p) {
        Poly norm = p.copy().normalize();
        ArrayList<Mono> list = norm.getMonoList();
        
        if (list.size() != 1) {
            return false;
        }
        
        Mono m = list.get(0);
        
        // 正常数
        if (m.getxexp().equals(BigInteger.ZERO) && m.getExpPoly().isZero()) {
            return true;
        }
        
        // x 或 x^k
        if (m.getCoe().equals(BigInteger.ONE)
            && m.getxexp().compareTo(BigInteger.ZERO) > 0
            && m.getExpPoly().isZero()) {
            return true;
        }
        
        // exp(...)
        if (m.getCoe().equals(BigInteger.ONE)
            && m.getxexp().equals(BigInteger.ZERO)
            && !m.getExpPoly().isZero()) {
            return true;
        }
        
        return false;
    }
    
    private String expArgToString(Poly p) {
        String inner = p.toString();
        if (canPolyBePrintedAsFactorDirectly(p)) {
            return inner;
        } else {
            return "(" + inner + ")";
        }
    }
    
    private String shortestExpString(Poly p) {
        Poly norm = p.copy().normalize();
        
        // 原始形式
        String best = "exp(" + expArgToString(norm) + ")";
        
        BigInteger g = norm.coefficientGcd();
        if (g.compareTo(BigInteger.ONE) <= 0) {
            return best;
        }
        
        for (BigInteger t : norm.goodExpFactors()) {
            if (t.compareTo(BigInteger.ONE) <= 0) {
                continue;
            }
            Poly q = norm.divideCoefficientsBy(t).normalize();
            String cand = "exp(" + expArgToString(q) + ")^" + t;
            if (cand.length() < best.length()) {
                best = cand;
            }
        }
        
        return best;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Mono)) {
            return false;
        }
        Mono other = (Mono) obj;
        return this.coe.equals(other.coe)
            && this.combineKey.equals(other.combineKey);
    }
    
    @Override
    public String toString() {
        if (coe.equals(BigInteger.ZERO)) {
            return "0";
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (coe.equals(BigInteger.valueOf(-1)) && hasOtherFactor()) {
            sb.append("-");
        } else if (!coe.equals(BigInteger.ONE)
            || (xexp.equals(BigInteger.ZERO) && expPoly.isZero())) {
            sb.append(coe);
        }
        
        if (!xexp.equals(BigInteger.ZERO)) {
            if (sb.length() > 0 && !(sb.length() == 1 && sb.charAt(0) == '-')) {
                sb.append("*");
            }
            sb.append("x");
            if (!xexp.equals(BigInteger.ONE)) {
                sb.append("^").append(xexp);
            }
        }
        
        if (!expPoly.isZero()) {
            if (sb.length() > 0 && !(sb.length() == 1 && sb.charAt(0) == '-')) {
                sb.append("*");
            }
            sb.append(shortestExpString(expPoly));
        }
        
        return sb.toString();
    }
    
}
