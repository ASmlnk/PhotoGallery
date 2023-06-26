package combignerdranch.android.photogallery

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

const val NOTIFICATION_CHANNEL_ID = "flickr_poll"

/*класс для уведомлений*/
class PhotoGalleryApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        /* добавим возможность создать и добавить канал,
        * если устройство работает под управлением Android Oreo или выше*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = getString(R.string.notification_channel_name)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
                val notificationManager: NotificationManager =
                    getSystemService(NotificationManager::class.java)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }
