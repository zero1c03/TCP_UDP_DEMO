package com.weberchen.tcp_udp_demo.UDP

import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.IOException
import java.net.*

class UDP(private val serverIP: String, private val context: Context) : Runnable {

    companion object {
        const val TAG = "UDP"
        const val RECEIVE_ACTION = "GetUDPReceive"
        const val RECEIVE_STRING = "ReceiveString"
        const val RECEIVE_BYTES = "ReceiveBytes"
        var datagramSocket: DatagramSocket? = null
    }

    private var port = 8888
    private var isOpen = false

    fun checkServerStatus(isOpen: Boolean) {
        this.isOpen = isOpen
        if (!isOpen) {
            datagramSocket?.close()
            Log.e("TAG", "UDP-server closed")
        }
    }

    fun setPort(port: Int) {
        this.port = port
    }

    @Throws(IOException::class)
    fun send(string: String, remoteIp: String, remotePort: Int) {
        Log.d(TAG, "Client Ip : $remoteIp : $remotePort")
        val inetAddress = InetAddress.getByName(remoteIp)
        val datagramSocket = DatagramSocket()
        val dpSend = DatagramPacket(
            string.toByteArray(),
            string.toByteArray().size,
            inetAddress,
            remotePort
        )
        datagramSocket.send(dpSend)
    }

    /** start listening thread */
    override fun run() {
        /** Start server to listen */
        val inetSocketAddress = InetSocketAddress(serverIP, port)
        try {
            datagramSocket = DatagramSocket(inetSocketAddress)
            Log.e(TAG, "UDP-Server start.")
        } catch (e: SocketException) {
            Log.e(TAG, "error message : ${e.message}")
            e.printStackTrace()
        }

        val msgRcv = ByteArray(1024)
        val dpRcv = DatagramPacket(msgRcv, msgRcv.size)

        while (isOpen) {
            Log.e(TAG, "UDP-Server listening")
            try {
                datagramSocket?.receive(dpRcv)
                val string = String(dpRcv.data, dpRcv.offset, dpRcv.length)
                Log.d(TAG, "UDP-Server get data $string")

                /** Send data to Activity */
                val intent = Intent()
                intent.action = RECEIVE_ACTION
                intent.putExtra(RECEIVE_STRING, string)
                intent.putExtra(RECEIVE_BYTES, dpRcv.data)
                context.sendBroadcast(intent)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

}