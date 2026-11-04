#!/system/bin/sh
# Usage (as root): sh /data/adb/ldac.sh [1000|1001|1002|1003]   (default 1000)
# 1000 = Quality (990 kbps), 1001 = Normal (660), 1002 = Connection (330), 1003 = Best effort (adaptive)
DEX="${DEX:-/data/adb/ldac990.dex}"
MODE="${1:-1000}"
case "$MODE" in
  1000|1001|1002|1003) ;;
  *) echo "invalid mode '$MODE' (use 1000-1003)" >&2; exit 64 ;;
esac
CLASSPATH="$DEX" exec app_process /system/bin Ldac990 "$MODE"
