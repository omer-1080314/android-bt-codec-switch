# android-bt-codec-switch

Switch the **LDAC bitrate** of a connected Bluetooth headset (e.g. Sony WF-1000XM5) from *Best effort (ABR)* to *Best quality (990 kbps)* on a rooted Android phone, without paid apps or opening Developer options. Driven by the free **Automate** app.

Tested on OnePlus (OxygenOS / ColorOS, Android 15/16, KernelSU) with Sony WF-1000XM5.

## How it works
`Ldac990.java` is a small headless program run through `app_process` as root. It:
1. binds to the A2DP profile via `BluetoothAdapter.getProfileProxy`,
2. reads the current codec config of every connected device,
3. for devices using LDAC (codec type 4) calls `setCodecConfigPreference` with the same sample rate/bits/channel mode, but `codecSpecific1` = requested LDAC quality.

Exit codes: `0` changed, `1` no LDAC device connected, `2` error, `3` profile proxy failed, `4` timeout, `5` no adapter.

| Argument | LDAC mode |
|---|---|
| `1000` (default) | Optimized for audio quality (990 kbps) |
| `1001` | Normal (660 kbps) |
| `1002` | Optimized for connection (330 kbps) |
| `1003` | Best effort (adaptive) |

## Requirements
- Root (KernelSU / Magisk / APatch). Automate needs to be granted superuser.
- Automate (LlamaLab) from Google Play.
- Headset connected over LDAC (enable LDAC in the headset's companion app, e.g. Sony Sound Connect → "Prioritize sound quality").

## Install
```sh
adb push ldac990.dex /data/local/tmp/
adb push ldac.sh /data/local/tmp/
adb shell su -c 'chmod 644 /data/local/tmp/ldac990.dex; chmod 755 /data/local/tmp/ldac.sh'
# test (headset connected):
adb shell su -c 'sh /data/local/tmp/ldac.sh 1000'
```
Then check Developer options → *Bluetooth audio LDAC codec*: it should show *Optimized for audio quality*.

### Build the dex yourself (optional)
```sh
javac --release 8 -cp android.jar -d out Ldac990.java   # android.jar from the Android SDK (platform 34+)
d8 --min-api 30 --output . out/Ldac990.class && mv classes.dex ldac990.dex
```

## Automate setup
Import the community flow: **[Bluetooth Codec Changer](https://llamalab.com/automate/community/flows/54031)** (Automate → Community → search "Bluetooth Codec Changer", or open the link on the phone). Some fields are intentionally left empty; fill them in:

1. **Flow beginning** – no settings.
2. **Bluetooth device connected?** (trigger block) – set *Device* to your headset (pick it from the list, e.g. "WF-1000XM5"). Leave empty to react to any device.
3. **Delay** (if present) – ~3–5 s, so A2DP has finished negotiating the codec before switching.
4. **Shell command superuser** – set *Command* to:
   ```
   sh /data/local/tmp/ldac.sh 1000
   ```
   (or `CLASSPATH=/data/local/tmp/ldac990.dex app_process /system/bin Ldac990 1000`). Optionally store *Exit status* in a variable to log/notify on failure (non‑zero = see table above).
5. Loop back to the trigger so it fires on every reconnect.

Start the flow, grant Automate the requested privileges (Bluetooth, superuser), and enable *Run on system startup* in Automate settings for set‑and‑forget.

> Note: Android may reset the LDAC mode when the headset reconnects; that's why the flow runs on every connection.

## Disclaimer
Use at your own risk. This only changes a runtime Bluetooth codec preference; nothing is written to system partitions.
