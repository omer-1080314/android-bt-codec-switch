#!/system/bin/sh
# Usage (as root): sh ldac.sh [1000|1001|1002|1003]
# 1000 = Quality (990 kbps), 1001 = Normal (660), 1002 = Connection (330), 1003 = Best effort (ABR)
DEX="${DEX:-/data/local/tmp/ldac990.dex}"
CLASSPATH="$DEX" exec app_process /system/bin Ldac990 "${1:-1000}"
