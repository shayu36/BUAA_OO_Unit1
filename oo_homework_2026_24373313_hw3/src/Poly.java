import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Poly {
    private static final int MERGE_THRESHOLD = 64;
    
    private ArrayList<Mono> monoList = new ArrayList<>();
    
    private String cachedCanonicalKey = null;
    private String cachedToString = null;
    private String cachedShortestExpString = null;
    
    private Integer cachedToStringLen = null;
    
    private BigInteger cachedCoefficientGcd = null;
    private ArrayList<BigInteger> cachedGoodExpFactors = null;
    
    public Poly(ArrayList<Mono> monoList) {
        this.monoList = compactList(monoList);
        clearCaches();
    }
    
    public static Poly zero() {
        return new Poly(new ArrayList<>());
    }
    
    private void clearCaches() {
        cachedCanonicalKey = null;
        cachedToString = null;
        cachedShortestExpString = null;
        cachedToStringLen = null;
        cachedCoefficientGcd = null;
        cachedGoodExpFactors = null;
    }
    
    public ArrayList<Mono> getMonoList() {
        return new ArrayList<>(monoList);
    }
    
    public Poly copy() {
        return new Poly(new ArrayList<>(this.monoList));
    }
    
    public boolean isZero() {
        return this.monoList.isEmpty();
    }
    
    public Poly normalize() {
        this.monoList = compactList(this.monoList);
        clearCaches();
        return this;
    }
    
    public String canonicalKey() {
        if (cachedCanonicalKey != null) {
            return cachedCanonicalKey;
        }
        if (monoList.isEmpty()) {
            cachedCanonicalKey = "0";
            return cachedCanonicalKey;
        }
        
        ArrayList<String> parts = new ArrayList<>();
        for (Mono mono : monoList) {
            if (!mono.isZero()) {
                parts.add(mono.getCoe().toString() + "@" + mono.getCombineKey());
            }
        }
        Collections.sort(parts);
        cachedCanonicalKey = String.join("|", parts);
        return cachedCanonicalKey;
    }
    
    public Poly mulConst(BigInteger k) {
        if (k.equals(BigInteger.ZERO) || this.isZero()) {
            return Poly.zero();
        }
        ArrayList<Mono> res = new ArrayList<>();
        for (Mono mono : this.monoList) {
            res.add(new Mono(
                mono.getCoe().multiply(k),
                mono.getxexp(),
                mono.getyexp(),
                mono.getExpPoly()
            ));
        }
        return new Poly(res);
    }
    
    private void addMonoMergedLinear(ArrayList<Mono> res, Mono mono) {
        if (mono == null || mono.isZero()) {
            return;
        }
        for (int i = 0; i < res.size(); i++) {
            if (res.get(i).canCombine(mono)) {
                Mono sum = res.get(i).monAddMon(mono);
                if (sum.isZero()) {
                    res.remove(i);
                } else {
                    res.set(i, sum);
                }
                return;
            }
        }
        res.add(mono);
    }
    
    private ArrayList<Mono> compactWithMap(ArrayList<Mono> monos) {
        LinkedHashMap<String, BigInteger> coeffMap = new LinkedHashMap<>();
        LinkedHashMap<String, Mono> repMap = new LinkedHashMap<>();
        
        for (Mono mono : monos) {
            if (mono == null || mono.isZero()) {
                continue;
            }
            String key = mono.getCombineKey();
            coeffMap.put(key, coeffMap.getOrDefault(key, BigInteger.ZERO).add(mono.getCoe()));
            repMap.putIfAbsent(key, mono);
        }
        
        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : coeffMap.entrySet()) {
            BigInteger sum = entry.getValue();
            if (sum.equals(BigInteger.ZERO)) {
                continue;
            }
            Mono rep = repMap.get(entry.getKey());
            res.add(new Mono(sum, rep.getxexp(), rep.getyexp(), rep.getExpPoly()));
        }
        return res;
    }
    
    private ArrayList<Mono> compactLinear(ArrayList<Mono> monos) {
        ArrayList<Mono> res = new ArrayList<>();
        for (Mono mono : monos) {
            addMonoMergedLinear(res, mono);
        }
        return res;
    }
    
    public ArrayList<Mono> compactList(ArrayList<Mono> monos) {
        if (monos == null || monos.isEmpty()) {
            return new ArrayList<>();
        }
        if (monos.size() < MERGE_THRESHOLD) {
            return compactLinear(monos);
        }
        return compactWithMap(monos);
    }
    
    public Poly addPoly(Poly b) {
        ArrayList<Mono> res = new ArrayList<>(this.monoList);
        res.addAll(b.getMonoList());
        return new Poly(res);
    }
    
    public Poly subPoly(Poly b) {
        ArrayList<Mono> res = new ArrayList<>(this.monoList);
        for (Mono mono : b.getMonoList()) {
            res.add(new Mono(
                mono.getCoe().negate(),
                mono.getxexp(),
                mono.getyexp(),
                mono.getExpPoly()
            ));
        }
        return new Poly(res);
    }
    
    public Poly mulPoly(Poly b) {
        if (this.isZero() || b.isZero()) {
            return Poly.zero();
        }
        
        ArrayList<Mono> blist = b.getMonoList();
        int estimatedProducts = this.monoList.size() * blist.size();
        
        if (estimatedProducts < MERGE_THRESHOLD) {
            ArrayList<Mono> res = new ArrayList<>();
            for (Mono monoA : this.monoList) {
                for (Mono monoB : blist) {
                    addMonoMergedLinear(res, monoA.monMulMon(monoB));
                }
            }
            return new Poly(res);
        }
        
        LinkedHashMap<String, BigInteger> coeffMap = new LinkedHashMap<>();
        LinkedHashMap<String, Mono> repMap = new LinkedHashMap<>();
        
        for (Mono monoA : this.monoList) {
            for (Mono monoB : blist) {
                Mono prod = monoA.monMulMon(monoB);
                if (prod.isZero()) {
                    continue;
                }
                String key = prod.getCombineKey();
                coeffMap.put(key, coeffMap.getOrDefault(key, BigInteger.ZERO).add(prod.getCoe()));
                repMap.putIfAbsent(key, prod);
            }
        }
        
        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<String, BigInteger> entry : coeffMap.entrySet()) {
            BigInteger sum = entry.getValue();
            if (sum.equals(BigInteger.ZERO)) {
                continue;
            }
            Mono rep = repMap.get(entry.getKey());
            res.add(new Mono(sum, rep.getxexp(), rep.getyexp(), rep.getExpPoly()));
        }
        return new Poly(res);
    }
    
    public Poly powPoly(BigInteger exp) {
        ArrayList<Mono> init = new ArrayList<>();
        init.add(new Mono(BigInteger.ONE, BigInteger.ZERO, BigInteger.ZERO));
        Poly ans = new Poly(init);
        
        BigInteger zero = BigInteger.ZERO;
        BigInteger one = BigInteger.ONE;
        BigInteger two = BigInteger.valueOf(2);
        
        if (exp.equals(zero)) {
            return ans;
        }
        
        Poly base = this;
        BigInteger n = exp;
        
        while (n.compareTo(zero) > 0) {
            if (n.mod(two).equals(one)) {
                ans = ans.mulPoly(base);
            }
            base = base.mulPoly(base);
            n = n.divide(two);
        }
        return ans;
    }
    
    public BigInteger coefficientGcd() {
        if (cachedCoefficientGcd != null) {
            return cachedCoefficientGcd;
        }
        
        BigInteger g = BigInteger.ZERO;
        for (Mono mono : this.monoList) {
            BigInteger c = mono.getCoe().abs();
            if (c.equals(BigInteger.ZERO)) {
                continue;
            }
            if (g.equals(BigInteger.ZERO)) {
                g = c;
            } else {
                g = g.gcd(c);
            }
        }
        
        cachedCoefficientGcd = g.equals(BigInteger.ZERO) ? BigInteger.ONE : g;
        return cachedCoefficientGcd;
    }
    
    public Poly divideCoefficientsBy(BigInteger t) {
        ArrayList<Mono> res = new ArrayList<>();
        for (Mono mono : this.monoList) {
            if (!mono.getCoe().equals(BigInteger.ZERO)) {
                res.add(new Mono(
                    mono.getCoe().divide(t),
                    mono.getxexp(),
                    mono.getyexp(),
                    mono.getExpPoly()
                ));
            }
        }
        return new Poly(res);
    }
    
    public ArrayList<BigInteger> goodExpFactors() {
        if (cachedGoodExpFactors != null) {
            return new ArrayList<>(cachedGoodExpFactors);
        }
        
        BigInteger g = coefficientGcd();
        ArrayList<BigInteger> ans = new ArrayList<>();
        
        if (g.compareTo(BigInteger.ONE) <= 0) {
            cachedGoodExpFactors = ans;
            return new ArrayList<>(cachedGoodExpFactors);
        }
        
        BigInteger hundred = BigInteger.valueOf(100);
        
        if (g.compareTo(hundred) < 0) {
            for (BigInteger t = BigInteger.valueOf(2);
                 t.compareTo(g) <= 0;
                 t = t.add(BigInteger.ONE)) {
                if (g.mod(t).equals(BigInteger.ZERO)) {
                    ans.add(t);
                }
            }
            cachedGoodExpFactors = ans;
            return new ArrayList<>(cachedGoodExpFactors);
        }
        
        ans.add(g);
        for (int k = 2; k <= 99; k++) {
            BigInteger bk = BigInteger.valueOf(k);
            if (g.mod(bk).equals(BigInteger.ZERO)) {
                BigInteger t = g.divide(bk);
                if (t.compareTo(BigInteger.ONE) > 0 && !ans.contains(t)) {
                    ans.add(t);
                }
            }
        }
        
        cachedGoodExpFactors = ans;
        return new ArrayList<>(cachedGoodExpFactors);
    }
    
    public String shortestExpString() {
        if (cachedShortestExpString == null) {
            cachedShortestExpString = PolyExpStringHelper.shortestExpString(this);
        }
        return cachedShortestExpString;
    }
    
    public Poly derivative(char var) {
        if (var != 'x' && var != 'y') {
            throw new IllegalArgumentException("var must be x or y");
        }
        
        ArrayList<Mono> res = new ArrayList<>();
        for (Mono mono : this.monoList) {
            res.addAll(mono.derivative(var).getMonoList());
        }
        return new Poly(res);
    }
    
    public Poly derivativeX() {
        return derivative('x');
    }
    
    public Poly derivativeY() {
        return derivative('y');
    }
    
    public Poly grad() {
        return derivative('x').addPoly(derivative('y'));
    }
    
    @Override
    public String toString() {
        if (cachedToString != null) {
            return cachedToString;
        }
        
        ArrayList<Mono> res = new ArrayList<>(this.monoList);
        if (!res.isEmpty() && res.get(0).getCoe().compareTo(BigInteger.ZERO) < 0) {
            for (int i = 1; i < res.size(); i++) {
                if (res.get(i).getCoe().compareTo(BigInteger.ZERO) > 0) {
                    Mono firstPositive = res.remove(i);
                    res.add(0, firstPositive);
                    break;
                }
            }
        }
        
        StringBuilder sb = new StringBuilder();
        for (Mono mono : res) {
            BigInteger coe = mono.getCoe();
            if (coe.equals(BigInteger.ZERO)) {
                continue;
            }
            
            String term = mono.toString();
            if (sb.length() == 0) {
                sb.append(term);
            } else {
                if (coe.compareTo(BigInteger.ZERO) > 0) {
                    sb.append("+").append(term);
                } else {
                    sb.append(term);
                }
            }
        }
        
        cachedToString = sb.length() == 0 ? "0" : sb.toString();
        if (cachedToStringLen == null) {
            cachedToStringLen = cachedToString.length();
        }
        return cachedToString;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Poly)) {
            return false;
        }
        Poly other = (Poly) obj;
        return this.canonicalKey().equals(other.canonicalKey());
    }
    
    @Override
    public int hashCode() {
        return canonicalKey().hashCode();
    }
}