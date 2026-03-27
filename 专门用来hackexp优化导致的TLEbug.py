import glob
import os
import subprocess
import sys
import time
import sympy

JAVA_CMD = "java"
PYTHON_CMD = sys.executable
TIMEOUT = 0.5
DATA_GEN_TIMEOUT = 10
OUTPUT_FILE = "output.txt"
DATA_FILE = "data.txt"


def parse_data_input(data: str):
    """
    按 data.py 的输出格式解析输入：

    hasFunc
    [f(x)=...]
    hasRec
    [f{0}(x)=... / f{1}(x)=... / f{n}(x)=...]
    expr
    """
    lines = data.splitlines()
    if not lines:
        return None

    idx = 0

    if lines[idx] not in {"0", "1"}:
        return None
    has_func = int(lines[idx])
    idx += 1

    func_def = None
    if has_func == 1:
        if idx >= len(lines) or not lines[idx].startswith("f(x)="):
            return None
        func_def = lines[idx]
        idx += 1

    if idx >= len(lines) or lines[idx] not in {"0", "1"}:
        return None
    has_rec = int(lines[idx])
    idx += 1

    rec_defs = []
    if has_rec == 1:
        if idx + 3 > len(lines):
            return None
        rec_defs = lines[idx : idx + 3]
        idx += 3

        init_set = set()
        has_fn = False
        for line in rec_defs:
            if line.startswith("f{0}(x)="):
                init_set.add(0)
            elif line.startswith("f{1}(x)="):
                init_set.add(1)
            elif line.startswith("f{n}(x)="):
                has_fn = True
            else:
                return None
        if init_set != {0, 1} or not has_fn:
            return None

    if idx != len(lines) - 1:
        return None

    expr = lines[idx]
    if expr.strip() == "":
        return None

    return {
        "has_func": has_func,
        "func_def": func_def,
        "has_rec": has_rec,
        "rec_defs": rec_defs,
        "expr": expr,
    }


def check_input_format(data: str) -> bool:
    return parse_data_input(data) is not None


def generate_input_from_data() -> str:
    try:
        if os.path.exists(DATA_FILE):
            os.remove(DATA_FILE)
    except Exception:
        pass

    try:
        result = subprocess.run(
            [PYTHON_CMD, "data.py"],
            text=True,
            capture_output=True,
            timeout=DATA_GEN_TIMEOUT,
        )
    except subprocess.TimeoutExpired:
        print(f"data.py 运行超时（>{DATA_GEN_TIMEOUT} 秒）")
        sys.exit(1)
    except Exception as e:
        print(f"运行 data.py 失败：{e}")
        sys.exit(1)

    if result.returncode != 0:
        print("data.py 运行失败。")
        if result.stderr:
            print("错误输出：")
            print(result.stderr)
        sys.exit(1)

    data = result.stdout

    if not data.strip() and os.path.exists(DATA_FILE):
        try:
            with open(DATA_FILE, "r", encoding="utf-8") as f:
                data = f.read()
        except Exception as e:
            print(f"读取 {DATA_FILE} 失败：{e}")
            sys.exit(1)

    if not data.strip():
        print("data.py 没有生成任何数据。")
        print("stdout 为：")
        print(repr(result.stdout))
        if os.path.exists(DATA_FILE):
            print(f"{DATA_FILE} 存在，但内容为空。")
        else:
            print(f"{DATA_FILE} 不存在。")
        sys.exit(1)

    parsed = parse_data_input(data)
    if parsed is None:
        print("data.py 生成的数据格式不符合要求。")
        print("合法格式应为：")
        print("  hasFunc")
        print("  [f(x)=...]")
        print("  hasRec")
        print("  [f{0}(x)=...]")
        print("  [f{1}(x)=...]")
        print("  [f{n}(x)=...]")
        print("  expr")
        print("\n实际输出为：")
        print(repr(data))
        sys.exit(1)

    return data


def parse_expr(expr: str):
    s = expr.strip()
    if not s:
        raise ValueError("空输出，无法做数学比较")

    s = s.replace("^", "**")

    x = sympy.Symbol("x")
    y = sympy.Symbol("y")
    return sympy.sympify(
        s,
        locals={
            "x": x,
            "y": y,
            "exp": sympy.exp,
        },
    )


def expr_equivalent(expr1: str, expr2: str) -> bool:
    try:
        e1 = parse_expr(expr1)
        e2 = parse_expr(expr2)
        diff = sympy.simplify(e1 - e2)
        if diff == 0:
            return True

        x = sympy.Symbol("x")
        y = sympy.Symbol("y")
        test_points = [
            {x: 0, y: 0},
            {x: 1, y: 0},
            {x: 2, y: 1},
            {x: -1, y: 2},
            {x: 3, y: -1},
        ]
        for subs in test_points:
            if sympy.simplify(diff.subs(subs)) != 0:
                return False
        return True
    except Exception:
        return False


def run_one_jar(jar: str, user_input: str) -> dict:
    info = {
        "jar": jar,
        "timeout": False,
        "returncode": None,
        "stdout": "",
        "stderr": "",
        "time": None,
        "math_ok": None,
    }

    start = time.perf_counter()
    try:
        result = subprocess.run(
            [JAVA_CMD, "-jar", jar],
            input=user_input,
            text=True,
            capture_output=True,
            timeout=TIMEOUT,
        )
        end = time.perf_counter()

        info["returncode"] = result.returncode
        info["stdout"] = result.stdout.strip()
        info["stderr"] = result.stderr
        info["time"] = end - start

    except subprocess.TimeoutExpired:
        end = time.perf_counter()
        info["timeout"] = True
        info["time"] = end - start

    except Exception as e:
        end = time.perf_counter()
        info["stderr"] = f"运行失败：{e}"
        info["time"] = end - start

    return info


def check_math_equivalence(results):
    base = None
    for item in results:
        if not item["timeout"] and item["returncode"] == 0 and item["stdout"]:
            base = item
            break

    if base is None:
        return False

    base["math_ok"] = True
    all_ok = True

    for item in results:
        if item is base:
            continue

        if item["timeout"] or item["returncode"] != 0 or not item["stdout"]:
            item["math_ok"] = False
            all_ok = False
            continue

        ok = expr_equivalent(base["stdout"], item["stdout"])
        item["math_ok"] = ok
        if not ok:
            all_ok = False

    return all_ok


def print_case_summary(case_id: int, results) -> None:
    print(f"第 {case_id} 组结果：")
    for item in results:
        if item["timeout"]:
            print(f"  {item['jar']}: TLE, time={item['time']:.6f}s")
        else:
            print(
                f"  {item['jar']}: "
                f"returncode={item['returncode']}, "
                f"time={item['time']:.6f}s, "
                f"length={len(item['stdout'])}, "
                f"math_ok={item['math_ok']}"
            )
    print()


def print_failure(case_id: int, user_input: str, results, reason: str) -> None:
    print("\n" + "=" * 80)
    print(f"在第 {case_id} 组数据发现异常，程序终止。原因：{reason}")
    print("=" * 80)

    print("\n触发样例输入：")
    print("-" * 80)
    print(user_input, end="" if user_input.endswith("\n") else "\n")
    print("-" * 80)

    print("\n各 jar 的输出信息：")
    for item in results:
        print("\n" + "=" * 80)
        print(f"程序: {item['jar']}")
        print("=" * 80)

        if item["timeout"]:
            print(f"状态: TLE（>{TIMEOUT} 秒）")
            print(f"运行时间: {item['time']:.6f}s")
            continue

        print(f"返回码: {item['returncode']}")
        print(f"运行时间: {item['time']:.6f}s")
        print(f"输出长度: {len(item['stdout'])}")
        print(f"数学等价: {item['math_ok']}")

        print("\n标准输出：")
        if item["stdout"]:
            print(item["stdout"])
        else:
            print("[无输出]")

        if item["stderr"]:
            print("\n错误输出：")
            print(item["stderr"], end="" if item["stderr"].endswith("\n") else "\n")


def write_failure_to_file(case_id: int, user_input: str, results, reason: str) -> None:
    with open(OUTPUT_FILE, "a", encoding="utf-8") as f:
        f.write("\n" + "=" * 80 + "\n")
        f.write(f"在第 {case_id} 组数据发现异常，程序终止。原因：{reason}\n")
        f.write("=" * 80 + "\n")

        f.write("\n触发样例输入：\n")
        f.write("-" * 80 + "\n")
        f.write(user_input)
        if not user_input.endswith("\n"):
            f.write("\n")
        f.write("-" * 80 + "\n")

        f.write("\n各 jar 的输出信息：\n")
        for item in results:
            f.write("\n" + "=" * 80 + "\n")
            f.write(f"程序: {item['jar']}\n")
            f.write("=" * 80 + "\n")

            if item["timeout"]:
                f.write(f"状态: TLE（>{TIMEOUT} 秒）\n")
                f.write(f"运行时间: {item['time']:.6f}s\n")
                continue

            f.write(f"返回码: {item['returncode']}\n")
            f.write(f"运行时间: {item['time']:.6f}s\n")
            f.write(f"输出长度: {len(item['stdout'])}\n")
            f.write(f"数学等价: {item['math_ok']}\n")

            f.write("\n标准输出：\n")
            if item["stdout"]:
                f.write(item["stdout"])
                if not item["stdout"].endswith("\n"):
                    f.write("\n")
            else:
                f.write("[无输出]\n")

            if item["stderr"]:
                f.write("\n错误输出：\n")
                f.write(item["stderr"])
                if not item["stderr"].endswith("\n"):
                    f.write("\n")


def main():
    jars = sorted(glob.glob("*.jar"))

    if not jars:
        print("当前目录下没有找到任何 jar 文件。")
        return

    print(f"当前目录下共找到 {len(jars)} 个 jar 文件：")
    for i, jar in enumerate(jars, 1):
        print(f"{i}. {jar}")
    print()

    case_id = 0

    while True:
        case_id += 1
        print(f"========== 第 {case_id} 组数据 ==========")

        user_input = generate_input_from_data()
        print("触发样例输入：")
        print(user_input, end="" if user_input.endswith("\n") else "\n")

        results = [run_one_jar(jar, user_input) for jar in jars]

        if all(item["timeout"] for item in results):
            print(f"第 {case_id} 组：所有 jar 全部 TLE，跳过。")
            for item in results:
                print(f"  {item['jar']}: time={item['time']:.6f}s")
            print()

            continue

        math_all_ok = check_math_equivalence(results)
        print_case_summary(case_id, results)

        if not math_all_ok:
            reason = "输出不数学等价"
            print_failure(case_id, user_input, results, reason)
            write_failure_to_file(case_id, user_input, results, reason)
            break


if __name__ == "__main__":
    main()
