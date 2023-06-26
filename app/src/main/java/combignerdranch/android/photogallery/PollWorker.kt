package combignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters


private const val TAG = "PollWorker"

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        val query =
            QueryPreferences.getStoredQuery(context) // извлечения текущего поискового запроса и
        // последнего идентификатора фотографии

        val lastResultId = QueryPreferences.getLastResultId(context)

        /* Если поискового запроса нет, мы загружаем
        * обычные фотографии. Если поисковый запрос есть, выполняем его*/
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest(1)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } else {
            FlickrFetchr().searchPhotosRequest(query, 1)
                .execute()
                .body()
                ?.photos
                ?.galleryItems
        } ?: emptyList() //В целях безопасности мы будем использовать пустой список,
        // если одному из запросов не удается вернуть какие-либо фотографии

        /* возврат из функции doWork(), если список элементов пуст*/
        if (items.isEmpty()) {
            return Result.success()
        }

        /* если список элементов не пуст  сравним идентификатор первого элемента в списке со свойством lastResultId.
        *  Кроме того, обновим идентификатор последнего результата в QueryPreferences, если нашли новый результат*/
        val resultId = items.first().id
        if (resultId == lastResultId) {
            Log.i(TAG, "Got an old result: $resultId")
        } else {
            Log.i(TAG, "Got a new result: $resultId")
            QueryPreferences.setLastResultId(context, resultId)


            val intent = PhotoGalleryActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources

            /*конструктор уведомления*/
            val notification = NotificationCompat    //NotificationCompat для работы с уведомлениями
                .Builder(context, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.new_pictures_title)) //Текст уведомления
                .setSmallIcon(android.R.drawable.ic_menu_report_image) //Значок уведомления
                .setContentTitle(resources.getString(R.string.new_pictures_title)) //установки заголовка текста
                .setContentText(resources.getString(R.string.new_pictures_text)) //установки текста
                .setContentIntent(pendingIntent) //когда пользователь нажмет на уведомление
                .setAutoCancel(true)
                .build()
            /* NotificationCompat.Builder принимает ID канала и использует его для установки параметра
            * канала уведомления, если пользователь запустил приложение на Oreo или выше.
            * Если у пользователя запущена более ранняя версия Android, NotificationCompat.Builder игнорирует канал*/

            val notificationManager = NotificationManagerCompat.from(context) //мэнеджер запуска уведомления
            notificationManager.notify(0, notification) //для размещения уведомления
            /* Целый параметр, который вы передаете в функцию notify(...), является идентификатором вашего уведомления.
             * Он должен быть уникальным во всем вашем приложении, но может быть использован повторно. Одно уведомление заменит
             * другое тем же самым идентификатором, который все еще находится в ящике уведомлений*/
        }

        return Result.success()
    }
}