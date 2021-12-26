package com.weberchen.tcp_demo.TCP

import android.content.Context
import android.content.Intent
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

class TCPServer(private val port: Int, private val context: Context) : Runnable {

    companion object {
        const val TAG: String = "TCP"
        const val RECEIVE_ACTION: String = "GetTCPReceive"
        const val RECEIVE_STRING: String = "ReceiveString"
        const val RECEIVE_BYTES: String = "ReceiveBytes"

    }

    private var isOpen: Boolean = true
    val SST: MutableList<ServerSocketThread> = mutableListOf()

    fun getStatus(): Boolean {
        return isOpen
    }

    fun closeServer() {
        isOpen = false
        for (s in SST) {
            s.isRun = false
        }
        SST.clear()
    }

    /** Socket handshake */
    fun getSocket(socketSocket: ServerSocket): Socket? {
        try {
            return socketSocket.accept()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("TAG", "update server")
            return null
        }
    }

    override fun run() {
        try {
            /** start server in this device port */
            val serverSocket = ServerSocket(port)
            serverSocket.soTimeout = 2000
            while (isOpen) {
                Log.e(TAG, "Listening input")
                if (!isOpen) break
                val socket = getSocket(serverSocket)
                if (socket != null) {
                    // A device connect to this device if socket is not null
                    ServerSocketThread(socket, context)
                }
            }
            serverSocket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    inner class ServerSocketThread(val socket: Socket, val context: Context) : Thread() {
        private var printWriter: PrintWriter? = null
        private var inputStream: InputStream? = null
        var isRun: Boolean = true

        init {
            try {
                socket.soTimeout = 2000
                val outputStream = socket.getOutputStream()
                inputStream = socket.getInputStream()
                printWriter = PrintWriter(outputStream, true)
                start()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        fun sendData(msg: String) {
            printWriter?.print(msg)
            printWriter?.flush()
        }

        override fun run() {
            val buff = ByteArray(1024)
            SST.add(this)
            while (isRun && !socket.isClosed && !socket.isInputShutdown()) {
                try {
                    // listening message
                    val rcvLen = inputStream?.read(buff) ?: -1
                    if (rcvLen != -1) {
                        val string = String(buff, 0, rcvLen)
                        Log.d(TAG, "receive message : $string")
                        // after receive message, send to Activity by broadcast
                        val intent = Intent()
                        intent.action = RECEIVE_ACTION;
                        intent.putExtra(RECEIVE_STRING, string);
                        intent.putExtra(RECEIVE_BYTES, buff);
                        context.sendBroadcast(intent);
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            // when the while stop, the connection is lost.
            try {
                socket.close()
                SST.clear()
                Log.e(TAG, "close server")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}