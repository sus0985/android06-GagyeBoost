package com.example.gagyeboost.ui.address

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.example.gagyeboost.R
import com.example.gagyeboost.common.GPSUtils
import com.example.gagyeboost.common.INTENT_EXTRA_PLACE_DETAIL
import com.example.gagyeboost.databinding.ActivityAddressResultBinding
import com.example.gagyeboost.model.data.ResultType
import com.example.gagyeboost.ui.base.BaseActivity
import com.example.gagyeboost.ui.home.selectPosition.AddressAdapter
import com.example.gagyeboost.ui.home.selectPosition.LoadStateAdapter
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class AddressResultActivity :
    BaseActivity<ActivityAddressResultBinding>(R.layout.activity_address_result) {

    private val viewModel by viewModel<AddressResultViewModel>()
    private val gpsUtils by lazy { GPSUtils(this) }
    private val adapter by lazy {
        AddressAdapter(viewModel) {
            val intent = Intent().apply {
                putExtra(INTENT_EXTRA_PLACE_DETAIL, it)
            }
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private lateinit var disposable: Disposable
    private val observable = BehaviorSubject.create<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initView()
        initObserve()
    }

    private fun initView() {
        binding.rvAddress.adapter = adapter.withLoadStateFooter(footer = LoadStateAdapter())
        binding.viewModel = viewModel

        binding.etSearch.doAfterTextChanged {
            observable.onNext(it?.toString())
        }

        binding.rvAddress.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                when (newState) {
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
                    }
                }
            }
        })

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initObserve() {
        disposable = observable.debounce(400, TimeUnit.MILLISECONDS).subscribe {
            lifecycleScope.launch {
                viewModel.fetchPlaceListData(it, gpsUtils.getUserLatLng(), setProgressBarVisible)
                    .collectLatest { data ->
                        adapter.submitData(data)
                    }
            }
        }
    }

    private val setProgressBarVisible: (ResultType) -> Unit = { isVisible ->

    }

    override fun onResume() {
        super.onResume()

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(
            InputMethodManager.SHOW_FORCED,
            InputMethodManager.HIDE_IMPLICIT_ONLY
        )
    }

    override fun onPause() {
        super.onPause()

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.etSearch.windowToken, 0)
    }

    override fun onDestroy() {
        disposable.dispose()
        super.onDestroy()
    }
}
