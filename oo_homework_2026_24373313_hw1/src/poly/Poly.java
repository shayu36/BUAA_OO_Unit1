package poly;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public class Poly {
    private ArrayList<Mono> monoList = new ArrayList<>();

    public Poly(ArrayList<Mono> monoList) {
        this.monoList = compactList(monoList);
    }

    public ArrayList<Mono> getMonoList() {
        return monoList;
    }

    private ArrayList<Mono> compactList(ArrayList<Mono> monos) {
        TreeMap<BigInteger, BigInteger> map = new TreeMap<>();

        for (Mono mono : monos) {
            BigInteger exp = mono.getExp();
            BigInteger coe = mono.getCoe();
            map.put(exp, map.getOrDefault(exp, BigInteger.ZERO).add(coe));
        }

        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            if (!entry.getValue().equals(BigInteger.ZERO)) {
                res.add(new Mono(entry.getValue(), entry.getKey()));
            }
        }
        return res;
    }

    public Poly addPoly(Poly b) {
        TreeMap<BigInteger, BigInteger> map = new TreeMap<>();

        for (Mono mono : this.monoList) {
            map.put(
                    mono.getExp(),
                    map.getOrDefault(mono.getExp(), BigInteger.ZERO).add(mono.getCoe())
            );
        }

        for (Mono mono : b.getMonoList()) {
            map.put(
                    mono.getExp(),
                    map.getOrDefault(mono.getExp(), BigInteger.ZERO).add(mono.getCoe())
            );
        }

        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            if (!entry.getValue().equals(BigInteger.ZERO)) {
                res.add(new Mono(entry.getValue(), entry.getKey()));
            }
        }
        return new Poly(res);
    }

    public Poly subPoly(Poly b) {
        TreeMap<BigInteger, BigInteger> map = new TreeMap<>();

        for (Mono mono : this.monoList) {
            map.put(
                    mono.getExp(),
                    map.getOrDefault(mono.getExp(), BigInteger.ZERO).add(mono.getCoe())
            );
        }

        for (Mono mono : b.getMonoList()) {
            map.put(
                    mono.getExp(),
                    map.getOrDefault(mono.getExp(), BigInteger.ZERO).subtract(mono.getCoe())
            );
        }

        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            if (!entry.getValue().equals(BigInteger.ZERO)) {
                res.add(new Mono(entry.getValue(), entry.getKey()));
            }
        }
        return new Poly(res);
    }

    public Poly mulPoly(Poly b) {
        TreeMap<BigInteger, BigInteger> map = new TreeMap<>();

        for (Mono monoA : this.monoList) {
            for (Mono monoB : b.getMonoList()) {
                BigInteger newCoe = monoA.getCoe().multiply(monoB.getCoe());
                BigInteger newExp = monoA.getExp().add(monoB.getExp());
                map.put(newExp, map.getOrDefault(newExp, BigInteger.ZERO).add(newCoe));
            }
        }

        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            if (!entry.getValue().equals(BigInteger.ZERO)) {
                res.add(new Mono(entry.getValue(), entry.getKey()));
            }
        }
        return new Poly(res);
    }

    public Poly powPoly(BigInteger exp) {
        ArrayList<Mono> init = new ArrayList<>();
        init.add(new Mono(BigInteger.ONE, BigInteger.ZERO));
        Poly ans = new Poly(init);

        BigInteger zero = BigInteger.ZERO;
        BigInteger one = BigInteger.ONE;
        BigInteger two = new BigInteger("2");

        if (exp.equals(zero)) {
            return ans;
        }

        Poly base = new Poly(new ArrayList<>(this.monoList));
        BigInteger n = exp;

        while (n.compareTo(zero) > 0) {
            if (n.mod(two).equals(one)) {
                ans = ans.mulPoly(base);
            }
            if (n.compareTo(one) > 0) {
                base = base.mulPoly(base);
            }
            n = n.divide(two);
        }
        return ans;
    }

    public String mergeMono(ArrayList<Mono> monos) {
        TreeMap<BigInteger, BigInteger> map = new TreeMap<>(Comparator.reverseOrder());
        for (Mono mono : monos) {
            BigInteger exp = mono.getExp();
            BigInteger coe = mono.getCoe();
            map.put(exp, map.getOrDefault(exp, BigInteger.ZERO).add(coe));
        }

        ArrayList<Mono> res = new ArrayList<>();
        for (Map.Entry<BigInteger, BigInteger> entry : map.entrySet()) {
            if (!entry.getValue().equals(BigInteger.ZERO)) {
                res.add(new Mono(entry.getValue(), entry.getKey()));
            }
        }

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
            BigInteger exp = mono.getExp();

            if (coe.equals(BigInteger.ZERO)) {
                continue;
            }

            boolean negative = coe.compareTo(BigInteger.ZERO) < 0;
            BigInteger absCoe = coe.abs();

            if (sb.length() == 0) {
                if (negative) {
                    sb.append("-");
                }
            } else {
                sb.append(negative ? "-" : "+");
            }

            if (exp.equals(BigInteger.ZERO)) {
                sb.append(absCoe);
            } else if (exp.equals(BigInteger.ONE)) {
                if (absCoe.equals(BigInteger.ONE)) {
                    sb.append("x");
                } else {
                    sb.append(absCoe);
                    sb.append("*x");
                }
            } else {
                if (absCoe.equals(BigInteger.ONE)) {
                    sb.append("x^");
                    sb.append(exp);
                } else {
                    sb.append(absCoe);
                    sb.append("*x^");
                    sb.append(exp);
                }
            }
        }
        return sb.length() == 0 ? "0" : sb.toString();
    }
}
