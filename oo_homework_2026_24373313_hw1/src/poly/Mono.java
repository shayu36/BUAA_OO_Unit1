package poly;

import java.math.BigInteger;
import java.util.ArrayList;

public class Mono {
    private final BigInteger coe;
    private final BigInteger exp;

    public Mono(BigInteger coe, BigInteger exp) {
        this.coe = coe;
        this.exp = exp;
    }

    public Poly monMulPol(Poly polyB) {
        ArrayList<Mono> monos = new ArrayList<>();
        for (Mono mono : polyB.getMonoList()) {
            monos.add(monMulMon(mono));
        }
        return new Poly(monos);
    }

    public Mono monMulMon(Mono monoB) {
        return new Mono(
                this.coe.multiply(monoB.coe),
                this.exp.add(monoB.exp)
        );
    }

    public BigInteger getCoe() {
        return coe;
    }

    public BigInteger getExp() {
        return exp;
    }
}
