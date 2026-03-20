import glob
import subprocess
import sys
import time
import sympy

JAVA_CMD = "java"
TIMEOUT = 10
OUTPUT_FILE = "output.txt"


def split_cases(data: str):
    """
    按题目格式把整份输入拆成多组 case。

    每组格式：
    - n=0: 共 2 行
    - n=1: 共 3 行
    """
    lines = data.splitlines()
    cases = []
    i = 0

    while i < len(lines):
        n = lines[i].strip()

        if n == "0":
            if i + 1 >= len(lines):
                raise ValueError(f"第 {i+1} 行是 0，但后面缺少待展开表达式")
            case = lines[i] + "\n" + lines[i + 1] + "\n"
            cases.append(case)
            i += 2

        elif n == "1":
            if i + 2 >= len(lines):
                raise ValueError(f"第 {i+1} 行是 1，但后面缺少函数定义或待展开表达式")
            case = lines[i] + "\n" + lines[i + 1] + "\n" + lines[i + 2] + "\n"
            cases.append(case)
            i += 3

        else:
            raise ValueError(f"第 {i+1} 行必须是 0 或 1，实际读到：{lines[i]!r}")

    return cases


def read_input_from_stdin():
    print("请输入多组测试数据，结束输入后：")
    print("Windows: Ctrl+Z 再回车")
    print("Linux / macOS: Ctrl+D")
    print()

    data = sys.stdin.read()

    if not data.strip():
        print("没有读到任何输入。")
        sys.exit(1)

    try:
        cases = split_cases(data)
    except ValueError as e:
        print("输入格式不符合要求。")
        print(e)
        print("\n实际输入为：")
        print(repr(data))
        sys.exit(1)

    return cases


def parse_expr(expr: str):
    """
    把 jar 输出表达式转成 sympy 表达式。
    支持：
    - 整数
    - x
    - + - *
    - ^
    - exp(.)
    - 括号
    """
    s = expr.strip()
    if not s:
        raise ValueError("空输出，无法做数学比较")

    s = s.replace("^", "**")

    x = sympy.Symbol("x")
    return sympy.sympify(
        s,
        locals={
            "x": x,
            "exp": sympy.exp,
        },
    )


def expr_equivalent(expr1: str, expr2: str) -> bool:
    """
    只代入 x=2 检验两个表达式是否相等。
    """
    try:
        x = sympy.Symbol("x")
        e1 = parse_expr(expr1).subs(x, 2)
        e2 = parse_expr(expr2).subs(x, 2)
        return sympy.simplify(e1 - e2) == 0
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
    """
    以第一个未 TLE、返回码为 0 且有输出的程序作为基准，
    比较其他程序在 x=2 时是否数学等价。
    """
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


def print_case_summary(results) -> None:
    print("本次结果：")
    for item in results:
        if item["timeout"]:
            print(f"  {item['jar']}: TLE, time={item['time']:.6f}s")
        else:
            print(
                f"  {item['jar']}: "
                f"returncode={item['returncode']}, time={item['time']:.6f}s, "
                f"math_ok={item['math_ok']}"
            )
    print()


def print_failure(user_input: str, results, reason: str) -> None:
    print("\n" + "=" * 80)
    print(f"发现异常。原因：{reason}")
    print("=" * 80)

    print("\n触发样例输入：")
    print("-" * 80)
    print(user_input, end="" if user_input.endswith("\n") else "\n")
    print("-" * 80)

    print("\n7 个程序的输出信息：")
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
        print(f"数学等价: {item['math_ok']}")

        print("\n标准输出：")
        if item["stdout"]:
            print(item["stdout"])
        else:
            print("[无输出]")

        if item["stderr"]:
            print("\n错误输出：")
            print(item["stderr"], end="" if item["stderr"].endswith("\n") else "\n")


def write_failure_to_file(user_input: str, results, reason: str) -> None:
    with open(OUTPUT_FILE, "a", encoding="utf-8") as f:
        f.write("\n" + "=" * 80 + "\n")
        f.write(f"发现异常。原因：{reason}\n")
        f.write("=" * 80 + "\n")

        f.write("\n触发样例输入：\n")
        f.write("-" * 80 + "\n")
        f.write(user_input)
        if not user_input.endswith("\n"):
            f.write("\n")
        f.write("-" * 80 + "\n")

        f.write("\n7 个程序的输出信息：\n")
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

    if len(jars) != 7:
        print(f"当前目录下找到 {len(jars)} 个 jar 文件，不是 7 个。")
        print("检测到的 jar：")
        for jar in jars:
            print("  ", jar)
        return

    print("检测到以下 7 个 jar：")
    for i, jar in enumerate(jars, 1):
        print(f"{i}. {jar}")
    print()

    all_cases = read_input_from_stdin()
    print(f"共读取到 {len(all_cases)} 组测试数据。")

    for case_id, user_input in enumerate(all_cases, 1):
        print(f"\n########## 第 {case_id} 组 ##########")
        print("触发样例输入：")
        print(user_input, end="" if user_input.endswith("\n") else "\n")

        results = [run_one_jar(jar, user_input) for jar in jars]

        if all(item["timeout"] for item in results):
            print("7 个程序全部 TLE。")
            for item in results:
                print(f"  {item['jar']}: time={item['time']:.6f}s")
            continue

        math_all_ok = check_math_equivalence(results)

        print_case_summary(results)

        if not math_all_ok:
            reason = "输出在 x=2 时不等价"
            print_failure(user_input, results, reason)
            write_failure_to_file(user_input, results, reason)
        else:
            print("7 个 jar 在 x=2 时数学等价。")


if __name__ == "__main__":
    main()
