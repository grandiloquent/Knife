package euphoria.psycho.common;

import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.DownloadManager;
import android.app.KeyguardManager;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.app.UiModeManager;
import android.app.WallpaperManager;
import android.app.usage.NetworkStatsManager;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStatsManager;
import android.appwidget.AppWidgetManager;
import android.bluetooth.BluetoothManager;
import android.companion.CompanionDeviceManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.pm.ShortcutManager;
import android.content.res.AssetManager;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraManager;
import android.hardware.display.DisplayManager;
import android.hardware.fingerprint.FingerprintManager;
import android.hardware.input.InputManager;
import android.hardware.usb.UsbManager;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.midi.MidiManager;
import android.media.tv.TvInputManager;
import android.net.ConnectivityManager;
import android.net.IpSecManager;
import android.net.nsd.NsdManager;
import android.net.wifi.WifiManager;
import android.net.wifi.aware.WifiAwareManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.rtt.WifiRttManager;
import android.nfc.NfcManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.DropBoxManager;
import android.os.HardwarePropertiesManager;
import android.os.PowerManager;
import android.os.TestLooperManager;
import android.os.UserManager;
import android.os.health.SystemHealthManager;
import android.os.storage.StorageManager;
import android.print.PrintManager;
import android.telecom.TelecomManager;
import android.telephony.CarrierConfigManager;
import android.telephony.TelephonyManager;
import android.telephony.TelephonyScanManager;
import android.telephony.euicc.EuiccManager;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.CaptioningManager;
import android.view.inputmethod.InputMethodManager;
import android.view.textclassifier.TextClassificationManager;

public class ManagerUtils {
    public static AccessibilityManager provideAccessibilityManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AccessibilityManager.class);
        } else {
            return (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        }
    }

    public static AccountManager provideAccountManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AccountManager.class);
        } else {
            return (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        }
    }


    public static ActivityManager provideActivityManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(ActivityManager.class);
        } else {
            return (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        }
    }

    public static AlarmManager provideAlarmManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AlarmManager.class);
        } else {
            return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
    }


    public static AppOpsManager provideAppOpsManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AppOpsManager.class);
        } else {
            return (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        }
    }


    public static AppWidgetManager provideAppWidgetManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AppWidgetManager.class);
        } else {
            return (AppWidgetManager) context.getSystemService(Context.APPWIDGET_SERVICE);
        }
    }


    public static AssetManager provideAssetManager(Context context) {

        return context.getAssets();
    }


    public static AudioManager provideAudioManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(AudioManager.class);
        } else {
            return (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
    }


    public static BatteryManager provideBatteryManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(BatteryManager.class);
        } else {
            return (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        }
    }


    public static BluetoothManager provideBluetoothManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(BluetoothManager.class);
        } else {
            return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
    }


    public static CameraManager provideCameraManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(CameraManager.class);
        } else {
            return (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        }
    }

    public static CaptioningManager provideCaptioningManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(CaptioningManager.class);
        } else {
            return (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        }
    }


    public static CarrierConfigManager provideCarrierConfigManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(CarrierConfigManager.class);
        } else {
            return (CarrierConfigManager) context.getSystemService(Context.CARRIER_CONFIG_SERVICE);
        }
    }


    public static ClipboardManager provideClipboardManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(ClipboardManager.class);
        } else {
            return (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static CompanionDeviceManager provideCompanionDeviceManager(Context context) {

        return context.getSystemService(CompanionDeviceManager.class);
    }


    public static ConnectivityManager provideConnectivityManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(ConnectivityManager.class);
        } else {
            return (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        }
    }


    public static DisplayManager provideDisplayManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(DisplayManager.class);
        } else {
            return (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
        }
    }

    public static DownloadManager provideDownloadManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(DownloadManager.class);
        } else {
            return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
    }


    public static DropBoxManager provideDropBoxManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(DropBoxManager.class);
        } else {
            return (DropBoxManager) context.getSystemService(Context.DROPBOX_SERVICE);
        }
    }


    @TargetApi(Build.VERSION_CODES.P)
    public static EuiccManager provideEuiccManager(Context context) {


        return context.getSystemService(EuiccManager.class);

    }


    public static FingerprintManager provideFingerprintManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(FingerprintManager.class);
        } else {
            return (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        }
    }


    @TargetApi(Build.VERSION_CODES.N)
    public static HardwarePropertiesManager provideHardwarePropertiesManager(Context context) {


        return context.getSystemService(HardwarePropertiesManager.class);

    }


    public static InputManager provideInputManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(InputManager.class);
        } else {
            return (InputManager) context.getSystemService(Context.INPUT_SERVICE);
        }
    }

    public static InputMethodManager provideInputMethodManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(InputMethodManager.class);
        } else {
            return (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        }
    }


    @TargetApi(Build.VERSION_CODES.P)
    public static IpSecManager provideIpSecManager(Context context) {


        return context.getSystemService(IpSecManager.class);

    }

    public static KeyguardManager provideKeyguardManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(KeyguardManager.class);
        } else {
            return (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        }
    }


    public static LocationManager provideLocationManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(LocationManager.class);
        } else {
            return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }


    public static MidiManager provideMidiManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(MidiManager.class);
        } else {
            return (MidiManager) context.getSystemService(Context.MIDI_SERVICE);
        }
    }


    public static NetworkStatsManager provideNetworkStatsManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(NetworkStatsManager.class);
        } else {
            return (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
        }
    }


    public static NfcManager provideNfcManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(NfcManager.class);
        } else {
            return (NfcManager) context.getSystemService(Context.NFC_SERVICE);
        }
    }


    public static NotificationManager provideNotificationManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(NotificationManager.class);
        } else {
            return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }


    public static NsdManager provideNsdManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(NsdManager.class);
        } else {
            return (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        }
    }


    public static PowerManager providePowerManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(PowerManager.class);
        } else {
            return (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        }
    }


    public static PrintManager providePrintManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(PrintManager.class);
        } else {
            return (PrintManager) context.getSystemService(Context.PRINT_SERVICE);
        }
    }


    public static RestrictionsManager provideRestrictionsManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(RestrictionsManager.class);
        } else {
            return (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
        }
    }


    public static SearchManager provideSearchManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(SearchManager.class);
        } else {
            return (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        }
    }


    public static SensorManager provideSensorManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(SensorManager.class);
        } else {
            return (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
    }


    @TargetApi(Build.VERSION_CODES.N_MR1)
    public static ShortcutManager provideShortcutManager(Context context) {

        return context.getSystemService(ShortcutManager.class);

    }


    public static StorageManager provideStorageManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(StorageManager.class);
        } else {
            return (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static StorageStatsManager provideStorageStatsManager(Context context) {

        return context.getSystemService(StorageStatsManager.class);

    }


    @TargetApi(Build.VERSION_CODES.N)
    public static SystemHealthManager provideSystemHealthManager(Context context) {


        return context.getSystemService(SystemHealthManager.class);

    }


    public static TelecomManager provideTelecomManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(TelecomManager.class);
        } else {
            return (TelecomManager) context.getSystemService(Context.TELECOM_SERVICE);
        }
    }

    public static TelephonyManager provideTelephonyManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(TelephonyManager.class);
        } else {
            return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    public static TelephonyScanManager provideTelephonyScanManager(Context context) {

        return context.getSystemService(TelephonyScanManager.class);

    }

    @TargetApi(Build.VERSION_CODES.O)
    public static TestLooperManager provideTestLooperManager(Context context) {

        return context.getSystemService(TestLooperManager.class);

    }

    @TargetApi(Build.VERSION_CODES.O)
    public static TextClassificationManager provideTextClassificationManager(Context context) {

        return context.getSystemService(TextClassificationManager.class);

    }


    public static TvInputManager provideTvInputManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(TvInputManager.class);
        } else {
            return (TvInputManager) context.getSystemService(Context.TV_INPUT_SERVICE);
        }
    }


    public static UiModeManager provideUiModeManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(UiModeManager.class);
        } else {
            return (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
        }
    }


    public static UsageStatsManager provideUsageStatsManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(UsageStatsManager.class);
        } else {
            return (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }


    public static UsbManager provideUsbManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(UsbManager.class);
        } else {
            return (UsbManager) context.getSystemService(Context.USB_SERVICE);
        }
    }


    public static UserManager provideUserManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(UserManager.class);
        } else {
            return (UserManager) context.getSystemService(Context.USER_SERVICE);
        }
    }


    public static WallpaperManager provideWallpaperManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(WallpaperManager.class);
        } else {
            return (WallpaperManager) context.getSystemService(Context.WALLPAPER_SERVICE);
        }
    }


    @TargetApi(Build.VERSION_CODES.O)
    public static WifiAwareManager provideWifiAwareManager(Context context) {

        return context.getSystemService(WifiAwareManager.class);
    }


    public static WifiManager provideWifiManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getApplicationContext().getSystemService(WifiManager.class);
        } else {
            return (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }
    }


    public static WifiP2pManager provideWifiP2pManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(WifiP2pManager.class);
        } else {
            return (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        }
    }

    @TargetApi(Build.VERSION_CODES.P)
    public static WifiRttManager provideWifiRttManager(Context context) {

        return context.getSystemService(WifiRttManager.class);
    }

    public static WindowManager provideWindowManager(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getSystemService(WindowManager.class);
        } else {
            return (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
    }


}
