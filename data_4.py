import random
BIGINT_MIN = -2147483649
BIGINT_MAX = 2147483648
FACTORS_MIN = 1
FACTORS_MAX = 4
TERMS_MIN = 1
TERMS_MAX = 2
EXP_MIN = 0
EXP_MAX = 7
MAX_DEPTH = 2

HAS_FUNC_DEF = 0


def getExpr(depth, isFactor, allowFunc=True, allowChoose=True):
    exponent = random.randint(EXP_MIN, EXP_MAX)
    termNum = random.randint(TERMS_MIN, TERMS_MAX)
    orbit = ""

    for i in range(termNum):
        orbit += getTerm(depth, allowFunc)
        if i != termNum - 1:
            orbit += random.choice(['+', '-'])

    performAsFactor = random.randint(0, 1)
    if isFactor == 1:
        performAsFactor = 1  # 作为 factor 时必须加括号

    if performAsFactor == 1:
        orbit = '(' + orbit + ')' + '^' + str(exponent)

    return orbit


def getTerm(depth, allowFunc=True, allowChoose=True):
    factorNum = random.randint(FACTORS_MIN, FACTORS_MAX)
    orbit = ""

    choice = random.randint(0, 2)   # 第一个 factor 的符号可有可无
    if choice == 0:
        orbit += '-'
    elif choice == 1:
        orbit += '+'

    for i in range(factorNum):
        orbit += getFactor(depth - 1, allowFunc)
        if i != factorNum - 1:
            orbit += '*'

    return orbit


def getFactor(depth, allowFunc=True, allowChoose=True):
    orbit = ""

    if depth > 0:
        if HAS_FUNC_DEF == 1 and allowFunc:
            choice = random.randint(0, 5)
        else:
            choice = random.randint(0, 3)
    else:
        choice = random.randint(0, 1)

    if choice == 0:
        orbit = getNum()
    elif choice == 1:
        orbit = getPower(depth)
    elif choice == 2:
        orbit = getExpr(depth - 1, 1, allowFunc)
    elif choice == 3:
        orbit = getExp(depth)
    elif choice == 4:
        orbit = getChoose(depth, allowFunc)
    else:
        orbit = getFunc(depth)

    return orbit


def getNum():
    orbit = ""
    choice = random.randint(0, 1)
    if choice == 0:
        orbit += '-'
    else:
        orbit += '+'

    zeros = random.randint(0, 2)
    for _ in range(zeros):
        orbit += '0'

    orbit += str(random.randint(0, BIGINT_MAX))
    return orbit


def getPower(depth):
    exponent = random.randint(EXP_MIN, EXP_MAX)
    orbit = ""

    choice = random.randint(0, 1)
    if choice == 0 or depth <= 0:
        orbit += 'x'
    else:
        orbit += '(' + getExpr(depth - 1, 0, True) + ')'

    orbit += '^'
    orbit += str(exponent)
    return orbit


def getExp(depth):
    orbit = "exp("
    orbit += getFactor(depth - 1, False)
    orbit += ')'
    return orbit


def getFunc(depth):
    orbit = "f("
    orbit += getExpr(depth - 1, 1, True)
    orbit += ')'
    return orbit


def getChoose(depth, allowFunc=True):
    orbit = "[("
    orbit += getFactor(depth - 1, allowFunc)
    orbit += "=="
    orbit += getFactor(depth - 1, allowFunc)
    orbit += ")?"
    orbit += getFactor(depth - 1, allowFunc)
    orbit += ":"
    orbit += getFactor(depth - 1, allowFunc)
    orbit += "]"
    return orbit


def getFuncDef():
    orbit = "f(x)="
    orbit += getExpr(MAX_DEPTH, 0, False)
    return orbit


if __name__ == '__main__':
    # random.seed(4052)
    with open('data.txt', 'w', encoding='utf-8') as f:
        hasFunc = random.randint(0, 1)
        # hasFunc = 0
        HAS_FUNC_DEF = hasFunc

        f.write(str(hasFunc) + '\n')
        print(str(hasFunc))

        if hasFunc == 1:
            funcDef = getFuncDef()
            f.write(funcDef + '\n')
            print(funcDef)

        res = getExpr(MAX_DEPTH, 0, True)
        f.write(res)
        print(res, end="")
