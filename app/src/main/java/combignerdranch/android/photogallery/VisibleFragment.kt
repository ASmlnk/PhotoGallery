package combignerdranch.android.photogallery

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment

/*Этот класс будет представлять обобщенный фрагмент,
скрывающий оповещения переднего плана*/
private const val TAG ="VisibleFragment"
abstract class VisibleFragment : Fragment() {

    /*приемник*/
    private val onShowNotification = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i(TAG, "canceling notification")

            /* отменить оповещение; эта информация передается в виде простого целочисленного кода
             * результата путем присвоения resultCode значения Activity.RESULT_CANCELED*/
            resultCode = Activity.RESULT_CANCELED
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(PollWorker.ACTION_SHOW_NOTIFICATION)

        /*регистрация приемника*/
        requireActivity().registerReceiver(
            onShowNotification,
            filter,
            PollWorker.PREM_PRIVATE,
            null
        )
    }

    override fun onStop() {
        super.onStop()
        /*отмена приемника*/
        requireActivity().unregisterReceiver(onShowNotification)
    }
}