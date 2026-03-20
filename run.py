import glob
import subprocess
import sys
import time
import sympy

JAVA_CMD = "java"
PYTHON_CMD = sys.executable
TIMEOUT = 10
DATA_GEN_TIMEOUT = 10
OUTPUT_FILE = "output.txt"


def check_input_format(data: str) -> bool:
    """
    只接受两种格式：
    1) 0\n一行数据
    2) 1\n一行数据\n一行数据

    允许末尾多一个换行。
    """
    lines = data.splitlines()
    if len(lines) == 2 and lines[0] == "0":
        return True
    if len(lines) == 3 and lines[0] == "1":
        return True
    return False


def generate_input_from_data4() -> str:
    try:
        result = subprocess.run([PYTHON_CMD, "data_4.py"], text=True, capture_output=True, timeout=DATA_GEN_TIMEOUT)
    except subprocess.TimeoutExpired:
        print(f"data_4.py 运行超时（>{DATA_GEN_TIMEOUT} 秒）")
        sys.exit(1)
    except Exception as e:
        print(f"运行 data_4.py 失败：{e}")
        sys.exit(1)

    if result.returncode != 0:
        print("data_4.py 运行失败。")
        if result.stderr:
            print("错误输出：")
            print(result.stderr)
        sys.exit(1)

    data = result.stdout

    if not data.strip():
        print("data_4.py 没有生成任何输出。")
        sys.exit(1)

    if not check_input_format(data):
        print("data_4.py 生成的数据格式不符合要求。")
        print("只允许以下两种格式：")
        print("  0\\n一行数据")
        print("  1\\n一行数据\\n一行数据")
        print("\n实际输出为：")
        print(repr(data))
        sys.exit(1)

    return data


def parse_expr(expr: str):
    """
    把 jar 输出表达式转成 sympy 表达式。
    支持：
    - 整数
    - x
    - + - *
    - ^
    - exp(...)
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
    使用 sympy，避免大整数溢出和浮点误差。
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
            [JAVA_CMD, "-jar", jar], input=user_input, text=True, capture_output=True, timeout=TIMEOUT
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


def print_case_summary(case_id: int, results) -> None:
    print(f"第 {case_id} 组结果：")
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


def print_failure(case_id: int, user_input: str, results, reason: str) -> None:
    print("\n" + "=" * 80)
    print(f"在第 {case_id} 组数据发现异常，程序终止。原因：{reason}")
    print("=" * 80)

    print("\n触发样例输入：")
    print("-" * 80)
    print(user_input, end="" if user_input.endswith("\n") else "\n")
    print("-" * 80)

    print("\n三个程序的输出信息：")
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

        f.write("\n三个程序的输出信息：\n")
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
        print(f"当前目录下找到 {len(jars)} 个 jar 文件，不是 3 个。")
        print("检测到的 jar：")
        for jar in jars:
            print("  ", jar)
        return

    print("检测到以下 3 个 jar：")
    for i, jar in enumerate(jars, 6):
        print(f"{i}. {jar}")
    print()

    case_id = 0

    while True:
        case_id += 1
        print(f"========== 第 {case_id} 组数据 ==========")

        user_input = generate_input_from_data4()
        print("触发样例输入：")
        print(user_input, end="" if user_input.endswith("\n") else "\n")

        results = [run_one_jar(jar, user_input) for jar in jars]

        if all(item["timeout"] for item in results):
            print(f"第 {case_id} 组：三个程序全部 TLE，跳过。")
            for item in results:
                print(f"  {item['jar']}: time={item['time']:.6f}s")
            print()
            continue

        math_all_ok = check_math_equivalence(results)

        print_case_summary(case_id, results)

        if not math_all_ok:
            reason = "输出在 x=2 时不等价"
            print_failure(case_id, user_input, results, reason)
            write_failure_to_file(case_id, user_input, results, reason)
            break


if __name__ == "__main__":
    main()
