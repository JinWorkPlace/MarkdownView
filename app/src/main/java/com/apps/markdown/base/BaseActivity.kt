package com.apps.markdown.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.apps.markdown.util.ex.clickAnimation
import com.apps.markdown.util.ex.handleBackPressed
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Base Activity cho tất cả các Activity trong app
 * Cung cấp các utility methods và setup cơ bản
 * copy from <a href="https://github.com/doanvu2000/AndroidBaseKotlin/blob/master/app/src/main/java/com/example/baseproject/base/base_view/screen/BaseActivity.kt"> BaseProject</a>
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    companion object {
        const val TAG = "MarkDownView"
        const val TIME_DELAY_CLICK = 200L
    }

    // View binding instance
    lateinit var binding: VB

    // Click prevention
    private var isAvailableClick = true

    // Screen dimensions
    var screenWidth = 0
    var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable edge-to-edge display
        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        // Initialize view binding
        binding = inflateViewBinding(layoutInflater)
        setContentView(binding.root)

        // Setup window insets
        setupWindowInsets()

        // Setup back press handling
        handleBackPressed { onBack() }

        // Initialize views and data
        initView()
        initData()
        initListener()
    }

    /**
     * Setup window insets for edge-to-edge display
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Handle back press action
     * Override this method to customize back press behavior
     */
    open fun onBack() {
        finish()
    }

    /**
     * Inflate view binding - must be implemented by subclasses
     */
    abstract fun inflateViewBinding(inflater: LayoutInflater): VB

    /**
     * Initialize views - must be implemented by subclasses
     */
    abstract fun initView()

    /**
     * Initialize data - must be implemented by subclasses
     */
    abstract fun initData()

    /**
     * Initialize listeners - must be implemented by subclasses
     */
    abstract fun initListener()

    /**
     * Delay click to prevent multiple rapid clicks
     */
    private fun delayClick() {
        launchCoroutineIO {
            isAvailableClick = false
            delay(TIME_DELAY_CLICK)
            isAvailableClick = true
        }
    }

    /**
     * Safe click extension to prevent multiple rapid clicks
     * @param isAnimationClick enable click animation
     * @param action action to perform on click
     */
    fun View.clickSafe(isAnimationClick: Boolean = false, action: () -> Unit) {
        setOnClickListener {
            if (isAvailableClick) {
                if (isAnimationClick) clickAnimation()
                action()
                delayClick()
            }
        }
    }

    //region Coroutine Management

    /**
     * Global exception handler for coroutines
     */
    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleError(throwable)
    }

    /**
     * Handle coroutine errors
     * Override this method to customize error handling
     */
    open fun handleError(throwable: Throwable) {
        val errorMessage = throwable.message ?: "Unknown error"
        logError(errorMessage)
        throwable.printStackTrace()
    }

    /**
     * Launch coroutine with error handling
     * @param dispatcher coroutine dispatcher (default: EmptyCoroutineContext)
     * @param blockCoroutine coroutine block to execute
     */
    fun launchCoroutine(
        dispatcher: CoroutineContext = EmptyCoroutineContext,
        blockCoroutine: suspend CoroutineScope.() -> Unit
    ) {
        try {
            lifecycleScope.launch(dispatcher + coroutineExceptionHandler) {
                blockCoroutine()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Launch coroutine on Main dispatcher
     */
    fun launchCoroutineMain(blockCoroutine: suspend CoroutineScope.() -> Unit) =
        launchCoroutine(Dispatchers.Main, blockCoroutine)

    /**
     * Launch coroutine on IO dispatcher
     */
    fun launchCoroutineIO(blockCoroutine: suspend CoroutineScope.() -> Unit) =
        launchCoroutine(Dispatchers.IO, blockCoroutine)

    /**
     * Execute action after delay
     * @param delayTime delay in milliseconds (default: 200ms)
     * @param action action to perform after delay
     */
    fun delayToAction(delayTime: Long = 200L, action: () -> Unit) {
        launchCoroutineIO {
            delay(delayTime)
            launchCoroutineMain { action() }
        }
    }

    //endregion

    /**
     * Adjust view insets for bottom navigation compatibility
     * Useful when system actions like sharing might interfere
     * @param viewBottom view to adjust margins for
     */
    protected fun adjustInsetsForBottomNavigation(viewBottom: View) {
        ViewCompat.setOnApplyWindowInsetsListener(viewBottom) { view, insets ->
            try {
                val params = view.layoutParams as ViewGroup.MarginLayoutParams
                val displayCutout =
                    insets.getInsetsIgnoringVisibility(WindowInsetsCompat.Type.systemBars())
                params.topMargin = displayCutout.top
                params.leftMargin = displayCutout.left
                params.rightMargin = displayCutout.right
                params.bottomMargin = displayCutout.bottom
                view.layoutParams = params
            } catch (e: Exception) {
                e.printStackTrace()
            }
            insets
        }
    }

    override fun onPause() {
        super.onPause()
        // Reset click availability when activity is paused
        isAvailableClick = true
    }

    //region Logging Utilities

    /**
     * Log debug message
     */
    fun logDebug(msg: String) = Log.d(TAG, "${this.javaClass.simpleName}: $msg")

    /**
     * Log warning message
     */
    fun logWarning(msg: String) = Log.w(TAG, "${this.javaClass.simpleName}: $msg")

    /**
     * Log error message
     */
    fun logError(msg: String) = Log.e(TAG, "${this.javaClass.simpleName}: $msg")

    /**
     * Log info message
     */
    fun logInfo(msg: String) = Log.i(TAG, "${this.javaClass.simpleName}: $msg")

    //endregion
}