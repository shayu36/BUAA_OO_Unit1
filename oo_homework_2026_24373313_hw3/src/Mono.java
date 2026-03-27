import java.math.BigInteger;
import java.util.ArrayList;

public class Mono {
    private final BigInteger coe;
    private final BigInteger xexp;
    private final BigInteger yexp;
    private final Poly expPoly; // 表示 exp(expPoly)，为 0 时表示没有 exp 因子
    private final String combineKey;
    private String cachedString = null;
    
    public Mono(BigInteger coe, BigInteger xexp, BigInteger yexp) {
        this.coe = coe;
        this.xexp = xexp;
        this.yexp = yexp;
        this.expPoly = Poly.zero();
        this.combineKey = buildCombineKey();
    }
    
    public Mono(BigInteger coe, BigInteger xexp, BigInteger yexp, Poly expPoly) {
        this.coe = coe;
        this.xexp = xexp;
        this.yexp = yexp;
        // 不再深拷贝，按逻辑不可变对象使用
        this.expPoly = (expPoly == null) ? Poly.zero() : expPoly;
        this.combineKey = buildCombineKey();
    }
    
    private String buildCombineKey() {
        return xexp.toString() + "#" + yexp.toString() + "#" + expPoly.canonicalKey();
    }
    
    public String getCombineKey() {
        return combineKey;
    }
    
    public Mono monMulMon(Mono monoB) {
        return new Mono(
            this.coe.multiply(monoB.coe),
            this.xexp.add(monoB.xexp),
            this.yexp.add(monoB.yexp),
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
            this.yexp,
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
    
    public BigInteger getyexp() {
        return yexp;
    }
    
    public Poly getExpPoly() {
        return expPoly;
    }
    
    private boolean hasOtherFactor() {
        return !xexp.equals(BigInteger.ZERO)
            || !yexp.equals(BigInteger.ZERO)
            || !expPoly.isZero();
    }
    
    public Poly derivative(char var) {
        if (var != 'x' && var != 'y') {
            throw new IllegalArgumentException("var must be x or y");
        }
        
        if (this.isZero()) {
            return Poly.zero();
        }
        
        ArrayList<Mono> res = new ArrayList<>();
        
        if (var == 'x') {
            if (xexp.compareTo(BigInteger.ZERO) > 0) {
                res.add(new Mono(
                    coe.multiply(xexp),
                    xexp.subtract(BigInteger.ONE),
                    yexp,
                    expPoly
                ));
            }
        } else {
            if (yexp.compareTo(BigInteger.ZERO) > 0) {
                res.add(new Mono(
                    coe.multiply(yexp),
                    xexp,
                    yexp.subtract(BigInteger.ONE),
                    expPoly
                ));
            }
        }
        
        Poly ans = new Poly(res);
        
        if (!expPoly.isZero()) {
            Poly innerDer = expPoly.derivative(var);
            if (!innerDer.isZero()) {
                ArrayList<Mono> baseList = new ArrayList<>();
                baseList.add(new Mono(coe, xexp, yexp, expPoly));
                Poly base = new Poly(baseList);
                ans = ans.addPoly(base.mulPoly(innerDer));
            }
        }
        
        return ans;
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
        if (cachedString != null) {
            return cachedString;
        }
        
        if (coe.equals(BigInteger.ZERO)) {
            cachedString = "0";
            return cachedString;
        }
        
        StringBuilder sb = new StringBuilder();
        
        if (coe.equals(BigInteger.valueOf(-1)) && hasOtherFactor()) {
            sb.append("-");
        } else if (!coe.equals(BigInteger.ONE)
            || (xexp.equals(BigInteger.ZERO) && yexp.equals(BigInteger.ZERO)
            && expPoly.isZero())) {
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
        if (!yexp.equals(BigInteger.ZERO)) {
            if (sb.length() > 0 && !(sb.length() == 1 && sb.charAt(0) == '-')) {
                sb.append("*");
            }
            sb.append("y");
            if (!yexp.equals(BigInteger.ONE)) {
                sb.append("^").append(yexp);
            }
        }
        
        if (!expPoly.isZero()) {
            if (sb.length() > 0 && !(sb.length() == 1 && sb.charAt(0) == '-')) {
                sb.append("*");
            }
            sb.append(expPoly.shortestExpString());
        }
        
        cachedString = sb.toString();
        return cachedString;
    }
}