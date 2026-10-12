#!/bin/bash
# clean compile: mede o TOTAL real de erros (compileJava incremental esconde o total)
cd "$(dirname "$0")"
export GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
G=/c/Users/lucas/AppData/Local/hermes/tools/gradle-8.10.2/bin/gradle
$G clean compileJava --console=plain -x test > _build_last.log 2>&1
echo "exit=$?"
grep -c 'error:' _build_last.log
tail -5 _build_last.log
