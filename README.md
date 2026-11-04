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
Import the community flow **[Bluetooth Codec Changer](https://llamalab.com/automate/community/flows/54031)** (open the link on the phone, or Automate → Community → search "Bluetooth Codec Changer"). The flow ships with two empty fields: the **device in block 2** and the **command in block 4**.

Flow layout: wait for a connection, run the root command once connected, keep looping.

1. **Flow beginning**: nothing to fill in. GO → block 2.
2. **Bluetooth device connected?** *(empty field: Device)*: select your headphones, e.g. `WF-1000XM5`, or leave it at *any* device.
   - **NO** → block 3.
   - **YES** → block 4.
3. **Delay**: 3 s, then back to block 2 (poll again).
4. **Superuser command** *(empty field: Command)*: enter
   ```
   sh /data/local/tmp/ldac.sh 1000
   ```
   or call it directly:
   ```
   CLASSPATH=/data/local/tmp/ldac990.dex app_process /system/bin Ldac990 1000
   ```
   **OK** → loops back to the IN of block 2 (or through the delay).

**Superuser access:** grant Automate root in **KernelSU** (KernelSU Manager → Superuser → enable Automate). Otherwise block 4 fails. Then start the flow. For set-and-forget, enable *Run on system startup* in Automate settings.

> Android may reset the LDAC mode on reconnect, which is why the flow keeps looping.

## Disclaimer
Use at your own risk. This only changes a runtime Bluetooth codec preference; nothing is written to system partitions.
