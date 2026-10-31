package com.example.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.*
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.keyboard.engine.KeyboardPreferences
import com.example.keyboard.engine.PredictionEngine
import com.example.keyboard.engine.RewardsManager
import com.example.keyboard.model.KeyboardTheme
import com.example.keyboard.ui.KeyboardActionListener
import com.example.keyboard.ui.MinimaKeyboardView

class MinimaInputMethodService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    private lateinit var preferences: KeyboardPreferences
    private lateinit var rewardsManager: RewardsManager
    private val predictionEngine = PredictionEngine()

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        preferences = KeyboardPreferences.getInstance(this)
        rewardsManager = RewardsManager.getInstance(this)
        predictionEngine.switchLanguage(preferences.languageCode.value)
    }

    override fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        predictionEngine.setIncognito(preferences.incognitoMode.value)
        predictionEngine.switchLanguage(preferences.languageCode.value)
        updatePrediction()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onCreateInputView(): View {
        return ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@MinimaInputMethodService)
            setViewTreeViewModelStoreOwner(this@MinimaInputMethodService)
            setViewTreeSavedStateRegistryOwner(this@MinimaInputMethodService)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnLifecycleDestroyed(lifecycleRegistry))

            setContent {
                val themeId by preferences.themeId.collectAsState()
                val theme = KeyboardTheme.findById(themeId)
                val prediction by predictionEngine.predictionState.collectAsState()
                val isIncognito by preferences.incognitoMode.collectAsState()
                val enableHaptic by preferences.hapticFeedback.collectAsState()
                val enableSwipeDelete by preferences.swipeLeftDelete.collectAsState()
                val enableSpaceGlide by preferences.spaceCursorGlide.collectAsState()
                val enableSwipeUpSymbols by preferences.swipeUpSymbols.collectAsState()
                val showSymbolHints by preferences.showSymbolHints.collectAsState()
                val keyHeightScale by preferences.keyHeightScale.collectAsState()

                val wallpaperType by preferences.wallpaperType.collectAsState()
                val customWallpaperPath by preferences.customWallpaperPath.collectAsState()
                val wallpaperDim by preferences.wallpaperDim.collectAsState()
                val keyOpacity by preferences.keyOpacity.collectAsState()
                val isLowEndMode by preferences.lowEndOptimization.collectAsState()
                val currentLangCode by preferences.languageCode.collectAsState()
                val coins by rewardsManager.coins.collectAsState()

                MinimaKeyboardView(
                    theme = theme,
                    prediction = prediction,
                    isIncognito = isIncognito,
                    enableHaptic = enableHaptic,
                    enableSwipeDelete = enableSwipeDelete,
                    enableSpaceGlide = enableSpaceGlide,
                    enableSwipeUpSymbols = enableSwipeUpSymbols,
                    showSymbolHints = showSymbolHints,
                    keyHeightScale = keyHeightScale,
                    wallpaperType = wallpaperType,
                    customWallpaperPath = customWallpaperPath,
                    wallpaperDim = wallpaperDim,
                    keyOpacity = keyOpacity,
                    isLowEndMode = isLowEndMode,
                    currentLanguageCode = currentLangCode,
                    coinBalance = coins,
                    listener = object : KeyboardActionListener {
                        override fun onText(text: String) {
                            currentInputConnection?.commitText(text, 1)
                            if (text.endsWith(" ")) {
                                predictionEngine.onWordCommitted(text.trim())
                                rewardsManager.onWordsTyped(1)
                            }
                            updatePrediction()
                        }

                        override fun onAcceptSuggestion(word: String, prefixLength: Int) {
                            val ic = currentInputConnection ?: return
                            if (prefixLength > 0) {
                                ic.deleteSurroundingText(prefixLength, 0)
                            }
                            ic.commitText(word + " ", 1)
                            predictionEngine.onWordCommitted(word)
                            rewardsManager.onWordsTyped(1)
                            updatePrediction()
                        }

                        override fun onBackspace() {
                            val ic = currentInputConnection ?: return
                            val selectedText = ic.getSelectedText(0)
                            if (selectedText.isNullOrEmpty()) {
                                ic.deleteSurroundingText(1, 0)
                            } else {
                                ic.commitText("", 1)
                            }
                            updatePrediction()
                        }

                        override fun onDeleteWord() {
                            val ic = currentInputConnection ?: return
                            val textBefore = ic.getTextBeforeCursor(50, 0)
                            if (textBefore.isNullOrEmpty()) {
                                ic.deleteSurroundingText(1, 0)
                                return
                            }
                            var i = textBefore.length - 1
                            while (i >= 0 && Character.isWhitespace(textBefore[i])) {
                                i--
                            }
                            while (i >= 0 && !Character.isWhitespace(textBefore[i])) {
                                i--
                            }
                            val deleteCount = textBefore.length - (i + 1)
                            ic.deleteSurroundingText(deleteCount.coerceAtLeast(1), 0)
                            updatePrediction()
                        }

                        override fun onEnter() {
                            val ic = currentInputConnection ?: return
                            val editorInfo = currentInputEditorInfo
                            if (editorInfo != null && (editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION) != EditorInfo.IME_ACTION_NONE) {
                                ic.performEditorAction(editorInfo.imeOptions and EditorInfo.IME_MASK_ACTION)
                            } else {
                                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                            }
                            rewardsManager.onWordsTyped(1)
                            updatePrediction()
                        }

                        override fun onMoveCursor(delta: Int) {
                            val keycode = if (delta > 0) KeyEvent.KEYCODE_DPAD_RIGHT else KeyEvent.KEYCODE_DPAD_LEFT
                            val times = kotlin.math.abs(delta).coerceAtMost(5)
                            for (step in 0 until times) {
                                sendDownUpKeyEvents(keycode)
                            }
                            updatePrediction()
                        }

                        override fun onOpenRewardsAd() {
                            val intent = Intent(this@MinimaInputMethodService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                                putExtra("open_tab", "rewards")
                            }
                            startActivity(intent)
                        }

                        override fun onSwitchLanguage(code: String) {
                            preferences.setLanguage(code)
                            predictionEngine.switchLanguage(code)
                            updatePrediction()
                        }

                        override fun onOpenSettings() {
                            val intent = Intent(this@MinimaInputMethodService, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            }
                            startActivity(intent)
                        }

                        override fun onHideKeyboard() {
                            requestHideSelf(0)
                        }
                    }
                )
            }
        }
    }

    private fun updatePrediction() {
        val textBefore = currentInputConnection?.getTextBeforeCursor(50, 0)
        predictionEngine.onTextContextChanged(textBefore)
    }
}
