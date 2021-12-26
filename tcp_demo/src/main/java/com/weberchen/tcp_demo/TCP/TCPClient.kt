package com.weberchen.tcp_demo.TCP

import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.DataInputStream
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.net.Socket

class TCPClient(val serverIP: String, val serverPort: Int, val context: Context) : Runnable {

    private val TAG = TCPServer.TAG
    private var printWriter: PrintWriter? = null
    private var inputstring: InputStream? = null
    private var dataInputStream: DataInputStream? = null
    private var isRun = true
    var socket: Socket? = null

    fun getStatus(): Boolean {
        return isRun
    }

    fun closeClient() {
        isRun = false
    }

    fun send(msg: ByteArray) {
        try {
            val outputStream = socket?.getOutputStream()
            outputStream?.write(msg)
            outputStream?.flush()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun send(msg: String) {
        printWriter?.print(msg)
        printWriter?.flush()
    }

    override fun run() {
        val buff = ByteArray(100)
        try {
            /** Set socket IP and Port */
            socket = Socket(serverIP, serverPort)
            socket?.soTimeout = 5000
            printWriter = PrintWriter(socket!!.getOutputStream(), true)
            inputstring = socket?.getInputStream()
            dataInputStream = DataInputStream(inputstring)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        while (isRun) {
            try {
                val rcvLen = dataInputStream?.read(buff)
                if (rcvLen != null) {
                    val rcvMsg = String(buff, 0, rcvLen, Charsets.UTF_8)
                    Log.d(TAG, "Receive message : $rcvMsg")
                    val intent = Intent()
                    intent.action = TCPServer.RECEIVE_ACTION
                    intent.putExtra(TCPServer.RECEIVE_STRING, rcvMsg)
                    context.sendBroadcast(intent)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                printWriter?.close()
                inputstring?.close()
                dataInputStream?.close()
                socket?.close()
                Log.d(TAG, "Close Client")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}