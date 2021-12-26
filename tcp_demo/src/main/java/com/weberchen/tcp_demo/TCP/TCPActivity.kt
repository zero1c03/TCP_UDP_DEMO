package com.weberchen.tcp_demo.TCP

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.weberchen.tcp_demo.NetWorkUtil
import com.weberchen.tcp_demo.R
import java.util.concurrent.Executors

class TCPActivity : AppCompatActivity() {

    var edRemoteIp: EditText? = null
    var edLocalPort: EditText? = null
    var edReceiveMessage: EditText? = null
    var edInputBox: EditText? = null
    var edRemotePort: EditText? = null

    var btClientConnect: ToggleButton? = null
    var btOpenServer: ToggleButton? = null
    var btServer: ToggleButton? = null
    var swFunction: SwitchCompat? = null

    val exec = Executors.newCachedThreadPool()
    val tcpBroadcast = TCPBroadcast()
    val stringBuffer = StringBuffer()
    var tcpServer: TCPServer? = null
    var tcpClient: TCPClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tcp)
        initUI()
        // set TCP server
        setServerSwitch()
        // set TCP client
        setClientSwitch()
        // set send data function (server/client)
        setSendFunction()
        // set broadcast
        val intentFilter = IntentFilter(TCPServer.RECEIVE_ACTION)
        registerReceiver(tcpBroadcast, intentFilter)
    }

    private fun initUI() {
        val tvLocalIp: TextView = findViewById(R.id.textView_IP)
        tvLocalIp.text = "Local Ip : ${NetWorkUtil.getLocalIP(this)}"
        val btClear: Button = findViewById(R.id.button_clear)
        btClear.setOnClickListener {
            stringBuffer.delete(0, stringBuffer.length)
            edReceiveMessage?.setText(stringBuffer)
        }
        btOpenServer = findViewById(R.id.toggleButton_Server);
        btClientConnect = findViewById(R.id.toggleButton_ClientConnect);
        edRemoteIp = findViewById(R.id.editText_RemoteIp);
        edRemotePort = findViewById(R.id.editText_RemotePort);
        edLocalPort = findViewById(R.id.editText_Port);
        edReceiveMessage = findViewById(R.id.editText_ReceiveMessage);
        edInputBox = findViewById(R.id.editText_Input);
        swFunction = findViewById(R.id.switch_ModeChange);
        swFunction?.isChecked = false;

        swFunction?.setOnCheckedChangeListener { buttonView, isChecked ->
            btOpenServer?.isEnabled = !isChecked;
            btClientConnect?.isEnabled = isChecked;
            if (!isChecked) {
                if (tcpServer != null && tcpClient?.getStatus() == true) {
                    tcpClient?.closeClient()
                    btClientConnect?.isChecked = false
                }
                swFunction?.text = "Server";
            } else {
                if (tcpServer != null && tcpServer?.getStatus() == true) {
                    tcpServer?.closeServer();
                    btServer?.isChecked = false;
                }
                swFunction?.text = "Client";
            }
        }
    }

    /** set TCP server */
    private fun setServerSwitch() {
        btServer = findViewById(R.id.toggleButton_Server)
        val exec = Executors.newCachedThreadPool()
        btServer?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                val port = edLocalPort?.text.toString().toInt()
                tcpServer = TCPServer(port, this)
                exec.execute(tcpServer)
                edLocalPort?.isEnabled = false
            } else {
                tcpServer?.closeServer()
                edLocalPort?.isEnabled = true
            }
        }
    }

    /** set TCP client */
    private fun setClientSwitch() {
        val remoteIp = edRemoteIp?.text.toString()
        val remotePort = edRemotePort?.text.toString().toInt()
        btClientConnect?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                tcpClient = TCPClient(remoteIp, remotePort, this)
                exec.execute(tcpClient)
                edRemoteIp?.isEnabled = false
                edRemotePort?.isEnabled = false
            } else {
                tcpClient?.closeClient()
                edRemoteIp?.isEnabled = true
                edRemotePort?.isEnabled = true
            }
        }
    }

    /** Send data (Server) */
    fun setSendFunction() {
        val btSend: Button = findViewById(R.id.button_Send)
        btSend.setOnClickListener {
            val text = edInputBox?.text.toString()
            if (swFunction?.isChecked == true) {
                // Change Switch in Client mode
                if (tcpClient == null) return@setOnClickListener
                if (text.length == 0 || tcpClient?.getStatus() == false) return@setOnClickListener
                exec.execute { tcpClient?.send(text) }
            } else {
                // Change Switch in Server mode
                if (tcpServer == null) return@setOnClickListener
                if (text.isEmpty() || tcpServer?.getStatus() == false) return@setOnClickListener
                exec.execute { tcpServer!!.SST[0].sendData(text) }
            }
        }
    }

    inner class TCPBroadcast : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                TCPServer.RECEIVE_ACTION -> {
                    val msg = intent.getStringExtra(TCPServer.RECEIVE_STRING)
                    val bytes = intent.getByteArrayExtra(TCPServer.RECEIVE_BYTES)
                    stringBuffer.append("receive : ").append(msg).append("\n")
                    edReceiveMessage?.setText(stringBuffer)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(tcpBroadcast)
    }
}