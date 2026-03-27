import random
from math import gcd
from typing import List


def format_poly(coeffs: List[int], start_power: int) -> str:
    parts = []
    for i, c in enumerate(coeffs):
        p = start_power + i
        if p == 1:
            parts.append(f"{c} * x")
        else:
            parts.append(f"{c} * x ^ {p}")
    return " + ".join(parts)


class GcdHeavyOneCaseGenerator:
    def __init__(self, seed: int | None = None):
        self.rng = random.Random(seed)
        self.primes = [11, 13, 17, 19, 23, 29, 31, 37]

    def make_big_base(self, repeat: int = 5) -> int:
        """
        构造一个很大的公共基底 G
        repeat 越大，生成出来的整数通常越大
        """
        g = 1
        chosen = self.rng.sample(self.primes, k=repeat)
        for p in chosen:
            e = self.rng.randint(2, 4)
            g *= p**e
        return g

    def make_multipliers(self, n: int) -> List[int]:
        """
        构造 n 个 multiplier：
        彼此共享很多质因子，便于形成重 gcd 结构
        """
        res = []
        for _ in range(n):
            picked = self.rng.sample(self.primes, k=self.rng.randint(3, 5))
            m = 1
            for p in picked:
                e = self.rng.randint(1, 3)
                m *= p**e
            m *= self.rng.choice([2, 3, 4, 5, 6, 8, 9, 10])
            res.append(m)
        return res

    def make_segment(self, length: int) -> List[int]:
        """
        一个 exp 段的系数：
        coeff_i = G * m_i
        """
        G = self.make_big_base(repeat=5)
        ms = self.make_multipliers(length)
        return [G * m for m in ms]

    def make_two_chain_case(self) -> str:
        """
        exp((x..x^4)) * exp((x^5..x^8))
        """
        a = self.make_segment(4)
        b = self.make_segment(4)
        pa = format_poly(a, 1)
        pb = format_poly(b, 5)
        return f"0\n0\nexp(({pa})) * exp(({pb}))"

    def make_three_chain_case(self) -> str:
        """
        exp((x..x^3)) * exp((x^4..x^6)) * exp((x^7..x^8))
        """
        a = self.make_segment(3)
        b = self.make_segment(3)
        c = self.make_segment(2)
        pa = format_poly(a, 1)
        pb = format_poly(b, 4)
        pc = format_poly(c, 7)
        return f"0\n0\nexp(({pa})) * exp(({pb})) * exp(({pc}))"

    def generate_one(self, mode: str = "mixed") -> str:
        """
        mode:
          - "two": 只生成两段链
          - "three": 只生成三段链
          - "mixed": 随机二选一
        """
        if mode == "two":
            return self.make_two_chain_case()
        if mode == "three":
            return self.make_three_chain_case()
        if mode == "mixed":
            if self.rng.random() < 0.5:
                return self.make_two_chain_case()
            return self.make_three_chain_case()
        raise ValueError("mode must be one of: two, three, mixed")


if __name__ == "__main__":
    # 改这里：
    # seed 不同，输出不同；mode 可选 two / three / mixed
    gen = GcdHeavyOneCaseGenerator(seed=None)
    print(gen.generate_one(mode="mixed"))
