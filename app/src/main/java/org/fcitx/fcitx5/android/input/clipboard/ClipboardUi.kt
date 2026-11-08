/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.clipboard

import android.content.Context
import android.view.View
import android.widget.ViewAnimator
import androidx.transition.Fade
import androidx.transition.TransitionManager
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.data.theme.ThemeManager
import org.fcitx.fcitx5.android.input.bar.ui.ToolButton
import splitties.dimensions.dp
import splitties.views.backgroundColor
import splitties.views.dsl.coordinatorlayout.coordinatorLayout
import splitties.views.dsl.coordinatorlayout.defaultLParams
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.view
import splitties.views.dsl.recyclerview.recyclerView
import timber.log.Timber

class ClipboardUi(override val ctx: Context, private val theme: Theme) : Ui {

    val recyclerView = recyclerView {
        addItemDecoration(SpacesItemDecoration(dp(4)))
    }

    val enableUi = ClipboardInstructionUi.Enable(ctx, theme)

    val emptyUi = ClipboardInstructionUi.Empty(ctx, theme)

    val viewAnimator =  view(::ViewAnimator) {
        add(recyclerView, lParams(matchParent, matchParent))
        add(emptyUi.root, lParams(matchParent, matchParent))
        add(enableUi.root, lParams(matchParent, matchParent))
    }

    private val keyBorder by ThemeManager.prefs.keyBorder
    private val disableAnimation by AppPrefs.getInstance().advanced.disableAnimation

    override val root = coordinatorLayout {
        if (!keyBorder) {
            backgroundColor = theme.barColor
        }
        add(viewAnimator, defaultLParams(matchParent, matchParent))
    }

    val backspaceButton = ToolButton(ctx, R.drawable.ic_baseline_backspace_24, theme).apply {
        contentDescription = ctx.getString(R.string.backspace)
    }

    val enterButton = ToolButton(ctx, R.drawable.ic_baseline_keyboard_return_24, theme).apply {
        contentDescription = ctx.getString(R.string.enter)
    }

    val homeButton = ToolButton(ctx, R.drawable.ic_baseline_first_page_24, theme).apply {
        contentDescription = ctx.getString(R.string.move_cursor_to_start)
    }

    val endButton = ToolButton(ctx, R.drawable.ic_baseline_last_page_24, theme).apply {
        contentDescription = ctx.getString(R.string.move_cursor_to_end)
    }

    val selectAllButton = ToolButton(ctx, R.drawable.ic_baseline_select_all_24, theme).apply {
        contentDescription = ctx.getString(android.R.string.selectAll)
    }

    val deleteAllButton = ToolButton(ctx, R.drawable.ic_baseline_delete_sweep_24, theme).apply {
        contentDescription = ctx.getString(R.string.delete_all)
    }

    val extension = horizontalLayout {
        add(homeButton, lParams(dp(40), dp(40)))
        add(endButton, lParams(dp(40), dp(40)))
        add(selectAllButton, lParams(dp(40), dp(40)))
        add(backspaceButton, lParams(dp(40), dp(40)))
        add(enterButton, lParams(dp(40), dp(40)))
        add(deleteAllButton, lParams(dp(40), dp(40)))
    }

    private fun setToolbarButtonsShown(enabled: Boolean) {
        val visibility = if (enabled) View.VISIBLE else View.INVISIBLE
        backspaceButton.visibility = visibility
        enterButton.visibility = visibility
        deleteAllButton.visibility = visibility
    }

    fun switchUiByState(state: ClipboardStateMachine.State) {
        Timber.d("Switch clipboard to $state")
        if (!disableAnimation)
            TransitionManager.beginDelayedTransition(root, Fade().apply { duration = 100L })
        when (state) {
            ClipboardStateMachine.State.Normal -> {
                viewAnimator.displayedChild = 0
                setToolbarButtonsShown(true)
            }
            ClipboardStateMachine.State.AddMore -> {
                viewAnimator.displayedChild = 1
                setToolbarButtonsShown(false)
            }
            ClipboardStateMachine.State.EnableListening -> {
                viewAnimator.displayedChild = 2
                setToolbarButtonsShown(false)
            }
        }
    }
}