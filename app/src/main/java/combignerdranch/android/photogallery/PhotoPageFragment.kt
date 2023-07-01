package combignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

private const val  ARG_URI = "photo_page_url"

/*класс для WebView*/
class PhotoPageFragment: VisibleFragment() {

    private lateinit var uri: Uri
    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        uri = arguments?.getParcelable(ARG_URI) ?: Uri.EMPTY
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_page, container, false)

        progressBar = view.findViewById(R.id.progress_bar)
        progressBar.max = 100

        webView = view.findViewById(R.id.web_view)

        /*для отображение страницы в WebView вкючаем поддержку Java Script
        * бращаясь к свойству settings для получения экземпляра WebSettings, с последующей установкой
        * WebSettings.javaScriptEnabled = true. Объект WebSettings — первый из трех
        * механизмов настройки WebView. Он содержит различные свойства, которые
        * можно задать в коде, например строку пользовательского агента и размер текста*/
        webView.settings.javaScriptEnabled = true

        /* Информация о прогрессе, получаемая от функции onProgressChanged (WebView,int), представляет
        * собой целое число от 0 до 100. Если результат равен 100, значит, загрузка страницы завершена,
        * поэтому мы скрываем ProgressBar, задавая режим View.GONE*/
        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(webView: WebView, newProgress: Int) {
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                    progressBar.progress = newProgress
                }
            }

            override fun onReceivedTitle(view: WebView?, title: String?) {
                (activity as AppCompatActivity).supportActionBar?.subtitle = title
            }

        }

        /*После этого реализация WebViewClient добавляется к WebView
        * Вместо того чтобы обращаться за помощью к менеджеру activity, виджет
        * обращается к вашей реализации WebViewClient. А реализация WebViewClient
        * по умолчанию говорит: «Загрузи URL самостоятельно!» И страница появится
        * в вашем виджете WebView. */
        webView.webViewClient = WebViewClient()
        webView.loadUrl(uri.toString())

        return view
    }

    companion object {
        fun newInstance(uri: Uri): PhotoPageFragment {
            return PhotoPageFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_URI, uri)
                }
            }
        }
    }

}