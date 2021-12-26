import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.wifi.WifiManager
import android.text.format.Formatter

class NetWorkUtil {
    companion object {
        fun getLocalIP(context: Context): String {
            val wifiManager =
                context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ip = wifiInfo.ipAddress
            return Formatter.formatIpAddress(ip)
        }
    }
}