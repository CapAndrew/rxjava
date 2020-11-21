package app.com.rxjava

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.main_fragment.*
import java.util.concurrent.TimeUnit

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private var disposable: Disposable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity?.title = "Love has no age"
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        textOfStory.text = createTextFromRaw()
        searchLine.addTextChangedListener(textWatcher)
    }


    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, start: Int, before: Int, count: Int) {
            var countOfFound = 0
            var isTextFound = false

            if (p0 != null) {
                if (p0.isNotEmpty()) {
                    disposable = storyObservable.throttleLatest(1L, TimeUnit.SECONDS, true)
                        .subscribeOn(io())
                        .doOnComplete {
                            if (!isTextFound) matchCount.text = getString(R.string.no_matches_found)
                        }
                        .subscribe({ words ->
                            words.forEach { word ->
                                if (word.toLowerCase().contains(p0.toString().toLowerCase())) {
                                    matchCount.text = (++countOfFound).toString()
                                    isTextFound = true
                                }
                            }
                        }, {

                        })
                } else {
                    matchCount.text = ""
                }
            }
        }
    }

    private val storyObservable = Flowable.fromCallable {
        createTextFromRaw().split(" ")
    }


    private fun createTextFromRaw(): String {
        return resources.openRawResource(R.raw.love_has_no_age).bufferedReader()
            .use { it.readText() }
    }

    override fun onDestroy() {
        disposable?.dispose()
        disposable = null
        super.onDestroy()
    }
}
