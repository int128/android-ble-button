package org.hidetake.blebutton

import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class ConnectedFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d("ConnectedFragment", "onCreateView()")
        val view = inflater!!.inflate(R.layout.fragment_connected, container, false)

        if (activity is DeviceActivity) {
            val service = (activity as DeviceActivity).serviceConnection.service

            val buttonAlert = view.findViewById(R.id.button_alert) as ImageButton
            buttonAlert.setOnClickListener { view ->
                service?.immediateAlert()
            }

            val textAddress = view.findViewById(R.id.text_address) as TextView
            textAddress.text = service?.bleDeviceAddress
        }

        return view
    }

}
