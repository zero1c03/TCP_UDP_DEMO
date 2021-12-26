package com.weberchen.tcp_udp_demo.UDP

import NetWorkUtil
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.weberchen.tcp_udp_demo.R
import java.io.IOException
import java.util.concurrent.Executors

class UDPActivity : AppCompatActivity() {
    var edRemoteIp: EditText? = null
    var edLocalPort: EditText? = null
    var edReceiveMessage: EditText? = null
    var edInputBox: EditText? = null
    var edRemotePort: EditText? = null

    val broadcast = UDPBroadcast()
    val stringBuffer = StringBuffer()
    val exec = Executors.newCachedThreadPool()
    var udpServer: UDP? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_udp)
        // init UI
        initUI()
        // init UDP listener
        setReceiveSwitch()
        // init send data function
        setSendFunction()
        // register broadcast
        val intentFilter = IntentFilter(UDP.RECEIVE_ACTION)
        registerReceiver(broadcast, intentFilter)
    }

    private fun initUI() {
        val tvLocalUp: TextView = findViewById(R.id.textView_IP)
        tvLocalUp.text = "Device Ip : ${NetWorkUtil.getLocalIP(this)}"

        val btClear: Button = findViewById(R.id.button_clear)
        btClear.setOnClickListener {
            stringBuffer.delete(0, stringBuffer.length)
            edReceiveMessage?.setText(stringBuffer)
        }
        edRemoteIp = findViewById(R.id.editText_RemoteIp);
        edRemotePort = findViewById(R.id.editText_RemotePort);
        edLocalPort = findViewById(R.id.editText_Port);
        edReceiveMessage = findViewById(R.id.editText_ReceiveMessage);
        edInputBox = findViewById(R.id.editText_Input);
    }

    private fun setReceiveSwitch() {
        val btSwitch: ToggleButton = findViewById(R.id.toggleButton_ReceiveSwitch)
        // init UDP server
        udpServer = UDP(NetWorkUtil.getLocalIP(this), this)
        btSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val port = edLocalPort?.text.toString().toInt()
                udpServer?.setPort(port)
                udpServer?.checkServerStatus(true)
                exec.execute(udpServer)
                edLocalPort?.isEnabled = false
            } else {
                /** Close UDP listening */
                udpServer?.checkServerStatus(false)
                edLocalPort?.isEnabled = true
            }

        }
    }

    private fun setSendFunction() {
        val btSend: Button = findViewById(R.id.button_Send)
        /** Send UDP message to certain IP */
        btSend.setOnClickListener {
            val msg = edInputBox?.text.toString()
            val remoteIp = edRemoteIp?.text.toString()
            val port = edRemotePort?.text.toString().toInt()
            if (msg.isEmpty()) return@setOnClickListener
            stringBuffer.append("Send : ").append(msg).append("\n")
            edReceiveMessage?.setText(stringBuffer)
            exec.execute {
                try {
                    udpServer?.send(msg, remoteIp, port)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcast)
    }

    inner class UDPBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action as String) {
                UDP.RECEIVE_ACTION -> {
                    val msg = intent.getStringExtra(UDP.RECEIVE_STRING)
                    val bytes = intent.getByteArrayExtra(UDP.RECEIVE_BYTES)
                    stringBuffer.append("get : ").append(msg).append("\n")
                    edReceiveMessage?.setText(stringBuffer)
                }
            }
        }
    }
}