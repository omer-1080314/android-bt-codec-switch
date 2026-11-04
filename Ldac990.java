import java.lang.reflect.*;
import java.util.List;
public class Ldac990 {
  static long q = 1000;
  public static void main(String[] a) throws Throwable {
    if (a.length > 0) q = Long.parseLong(a[0]);
    Class<?> L = Class.forName("android.os.Looper");
    L.getMethod("prepareMainLooper").invoke(null);
    Class<?> AT = Class.forName("android.app.ActivityThread");
    Object at = AT.getMethod("systemMain").invoke(null);
    final Object ctx = AT.getMethod("getSystemContext").invoke(at);
    try { Class<?> BSM = Class.forName("android.os.BluetoothServiceManager"); Object bsm = BSM.getConstructor().newInstance();
      Class.forName("android.bluetooth.BluetoothFrameworkInitializer").getMethod("setBluetoothServiceManager", BSM).invoke(null, bsm);
    } catch (Throwable t) { System.err.println("init: "+t); }
    Class<?> BA = Class.forName("android.bluetooth.BluetoothAdapter");
    Object ad0 = BA.getMethod("getDefaultAdapter").invoke(null);
    if (ad0 == null) { Object bm = Class.forName("android.content.Context").getMethod("getSystemService", String.class).invoke(ctx, "bluetooth"); ad0 = bm.getClass().getMethod("getAdapter").invoke(bm); }
    if (ad0 == null) { try { Method cm = BA.getDeclaredMethod("createAdapter", Class.forName("android.content.Context")); cm.setAccessible(true); ad0 = cm.invoke(null, ctx);} catch (Throwable t) { t.printStackTrace(); } }
    if (ad0 == null) { try {
      Object b = Class.forName("android.os.ServiceManager").getMethod("getService", String.class).invoke(null, "bluetooth_manager");
      System.err.println("binder="+b);
      Class<?> IB = Class.forName("android.os.IBinder");
      Object ibm = Class.forName("android.bluetooth.IBluetoothManager$Stub").getMethod("asInterface", IB).invoke(null, b);
      Constructor<?> k = BA.getDeclaredConstructor(Class.forName("android.bluetooth.IBluetoothManager"), Class.forName("android.content.Context"));
      k.setAccessible(true); ad0 = k.newInstance(ibm, ctx);
    } catch (Throwable t) { t.printStackTrace(); } }
    final Object ad = ad0; if (ad == null) { System.err.println("no adapter"); System.exit(5); }
    Class<?> SL = Class.forName("android.bluetooth.BluetoothProfile$ServiceListener");
    Object listener = Proxy.newProxyInstance(SL.getClassLoader(), new Class<?>[]{SL}, new InvocationHandler() {
      public Object invoke(Object p, Method m, Object[] args) {
        if (m.getName().equals("onServiceConnected")) {
          int rc = 0;
          try { rc = run(args[1]); } catch (Throwable t) { t.printStackTrace(); rc = 2; }
          System.exit(rc);
        }
        return null;
      }
    });
    Class<?> CTX = Class.forName("android.content.Context");
    boolean ok = (Boolean) BA.getMethod("getProfileProxy", CTX, SL, int.class).invoke(ad, ctx, listener, 2);
    if (!ok) { System.err.println("getProfileProxy failed"); System.exit(3); }
    new Thread(() -> { try { Thread.sleep(10000);} catch (Exception e) {} System.err.println("timeout"); System.exit(4);}).start();
    L.getMethod("loop").invoke(null);
  }
  static int run(Object a2dp) throws Throwable {
    Class<?> A = a2dp.getClass();
    Class<?> DEV = Class.forName("android.bluetooth.BluetoothDevice");
    Class<?> CFG = Class.forName("android.bluetooth.BluetoothCodecConfig");
    List<?> devs = (List<?>) A.getMethod("getConnectedDevices").invoke(a2dp);
    int changed = 0;
    for (Object d : devs) {
      Object st = A.getMethod("getCodecStatus", DEV).invoke(a2dp, d);
      if (st == null) continue;
      Object c = st.getClass().getMethod("getCodecConfig").invoke(st);
      int type = (Integer) CFG.getMethod("getCodecType").invoke(c);
      System.out.println(d + " before: " + c);
      if (type != 4) continue;
      Class<?> B = Class.forName("android.bluetooth.BluetoothCodecConfig$Builder");
      Object b = B.getConstructor().newInstance();
      B.getMethod("setCodecType", int.class).invoke(b, 4);
      B.getMethod("setCodecPriority", int.class).invoke(b, 1000000);
      B.getMethod("setSampleRate", int.class).invoke(b, CFG.getMethod("getSampleRate").invoke(c));
      B.getMethod("setBitsPerSample", int.class).invoke(b, CFG.getMethod("getBitsPerSample").invoke(c));
      B.getMethod("setChannelMode", int.class).invoke(b, CFG.getMethod("getChannelMode").invoke(c));
      B.getMethod("setCodecSpecific1", long.class).invoke(b, q);
      B.getMethod("setCodecSpecific2", long.class).invoke(b, CFG.getMethod("getCodecSpecific2").invoke(c));
      B.getMethod("setCodecSpecific3", long.class).invoke(b, CFG.getMethod("getCodecSpecific3").invoke(c));
      B.getMethod("setCodecSpecific4", long.class).invoke(b, CFG.getMethod("getCodecSpecific4").invoke(c));
      Object nc = B.getMethod("build").invoke(b);
      A.getMethod("setCodecConfigPreference", DEV, CFG).invoke(a2dp, d, nc);
      changed++;
      Thread.sleep(1500);
      Object st2 = A.getMethod("getCodecStatus", DEV).invoke(a2dp, d);
      Object c2 = st2.getClass().getMethod("getCodecConfig").invoke(st2);
      System.out.println("after: codecSpecific1=" + CFG.getMethod("getCodecSpecific1").invoke(c2));
    }
    if (changed == 0) { System.out.println("no LDAC device connected"); return 1; }
    return 0;
  }
}
