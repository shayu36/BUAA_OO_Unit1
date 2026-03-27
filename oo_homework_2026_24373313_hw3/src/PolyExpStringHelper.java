import java.math.BigInteger;
import java.util.ArrayList;

public final class PolyExpStringHelper {
    private static final int EXP_DP_TERM_LIMIT = 16;
    
    private PolyExpStringHelper() {
    }
    
    public static String shortestExpString(Poly poly) {
        int n = poly.getMonoList().size();
        if (n <= EXP_DP_TERM_LIMIT) {
            return shortestExpStringByDp(poly);
        } else {
            return shortestSingleExpStringHeuristic(poly);
        }
    }
    
    private static boolean canBePrintedAsExpFactorDirectly(Poly poly) {
        ArrayList<Mono> list = poly.getMonoList();
        if (list.size() != 1) {
            return false;
        }
        
        Mono m = list.get(0);
        
        if (m.getxexp().equals(BigInteger.ZERO)
            && m.getyexp().equals(BigInteger.ZERO)
            && m.getExpPoly().isZero()) {
            return true;
        }
        
        if (m.getCoe().equals(BigInteger.ONE)
            && m.getxexp().compareTo(BigInteger.ZERO) > 0
            && m.getyexp().equals(BigInteger.ZERO)
            && m.getExpPoly().isZero()) {
            return true;
        }
        
        if (m.getCoe().equals(BigInteger.ONE)
            && m.getxexp().equals(BigInteger.ZERO)
            && m.getyexp().compareTo(BigInteger.ZERO) > 0
            && m.getExpPoly().isZero()) {
            return true;
        }
        
        if (m.getCoe().equals(BigInteger.ONE)
            && m.getxexp().equals(BigInteger.ZERO)
            && m.getyexp().equals(BigInteger.ZERO)
            && !m.getExpPoly().isZero()) {
            return !m.getExpPoly().toString().contains("*");
        }
        
        return false;
    }
    
    private static int digitLength(BigInteger x) {
        return x.toString().length();
    }
    
    private static int toStringLength(Poly poly) {
        return poly.toString().length();
    }
    
    private static String expArgToStringForExp(Poly poly) {
        String inner = poly.toString();
        return canBePrintedAsExpFactorDirectly(poly) ? inner : "(" + inner + ")";
    }
    
    private static int expArgLengthForExp(Poly poly) {
        int innerLen = toStringLength(poly);
        return canBePrintedAsExpFactorDirectly(poly) ? innerLen : innerLen + 2;
    }
    
    private static String bestSingleExpStringUsingGoodFactors(Poly poly) {
        int bestLen = 5 + expArgLengthForExp(poly);
        BigInteger bestFactor = BigInteger.ONE;
        Poly bestQ = null;
        
        BigInteger g = poly.coefficientGcd();
        if (g.compareTo(BigInteger.ONE) > 0) {
            for (BigInteger t : poly.goodExpFactors()) {
                if (t.compareTo(BigInteger.ONE) <= 0) {
                    continue;
                }
                Poly q = poly.divideCoefficientsBy(t);
                int candLen = 6 + expArgLengthForExp(q) + digitLength(t);
                if (candLen < bestLen) {
                    bestLen = candLen;
                    bestFactor = t;
                    bestQ = q;
                }
            }
        }
        
        if (bestFactor.equals(BigInteger.ONE)) {
            return "exp(" + expArgToStringForExp(poly) + ")";
        } else {
            return "exp(" + expArgToStringForExp(bestQ) + ")^" + bestFactor;
        }
    }
    
    private static String shortestSingleExpStringDpCandidate(Poly poly) {
        return bestSingleExpStringUsingGoodFactors(poly);
    }
    
    private static String shortestSingleExpStringHeuristic(Poly poly) {
        return bestSingleExpStringUsingGoodFactors(poly);
    }
    
    private static Poly subsetPoly(ArrayList<Mono> terms, int mask) {
        ArrayList<Mono> picked = new ArrayList<>();
        for (int i = 0; i < terms.size(); i++) {
            if ((mask & (1 << i)) != 0) {
                picked.add(terms.get(i));
            }
        }
        return new Poly(picked);
    }
    
    private static String buildDpString(
        int mask,
        boolean[] useSingle,
        int[] split,
        String[] singleBest
    ) {
        if (useSingle[mask]) {
            return singleBest[mask];
        }
        int left = split[mask];
        int right = mask ^ left;
        return buildDpString(left, useSingle, split, singleBest)
            + "*"
            + buildDpString(right, useSingle, split, singleBest);
    }
    
    private static String shortestExpStringByDp(Poly poly) {
        ArrayList<Mono> terms = poly.getMonoList();
        int n = terms.size();
        int totalMask = 1 << n;
        
        Poly[] subsetCache = new Poly[totalMask];
        String[] singleBest = new String[totalMask];
        int[] dpLen = new int[totalMask];
        boolean[] useSingle = new boolean[totalMask];
        int[] split = new int[totalMask];
        
        for (int mask = 1; mask < totalMask; mask++) {
            subsetCache[mask] = subsetPoly(terms, mask);
            singleBest[mask] = shortestSingleExpStringDpCandidate(subsetCache[mask]);
            dpLen[mask] = singleBest[mask].length();
            useSingle[mask] = true;
            split[mask] = 0;
        }
        
        for (int mask = 1; mask < totalMask; mask++) {
            if ((mask & (mask - 1)) == 0) {
                continue;
            }
            
            int bestLen = dpLen[mask];
            int pivot = Integer.lowestOneBit(mask);
            
            for (int sub = (mask - 1) & mask; sub > 0; sub = (sub - 1) & mask) {
                if (sub == mask) {
                    continue;
                }
                if ((sub & pivot) == 0) {
                    continue;
                }
                
                int other = mask ^ sub;
                int candLen = dpLen[sub] + 1 + dpLen[other];
                if (candLen < bestLen) {
                    bestLen = candLen;
                    dpLen[mask] = candLen;
                    useSingle[mask] = false;
                    split[mask] = sub;
                }
            }
        }
        
        return buildDpString(totalMask - 1, useSingle, split, singleBest);
    }
}