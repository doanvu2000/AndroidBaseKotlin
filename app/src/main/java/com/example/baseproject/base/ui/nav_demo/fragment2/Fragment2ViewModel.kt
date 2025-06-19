package com.example.baseproject.base.ui.nav_demo.fragment2

import com.example.baseproject.base.viewmodel.BaseViewModel
import com.example.baseproject.base.viewmodel.sendEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class Fragment2ViewModel : BaseViewModel() {
    sealed class EventScreen2 {
        data object OnBack : EventScreen2()
        data object ClickGotoFragment3 : EventScreen2()
    }

    private val eventChannel = Channel<EventScreen2>()
    val event = eventChannel.receiveAsFlow()

    fun clickGotoFragment3() {
        sendEvent(eventChannel, EventScreen2.ClickGotoFragment3)
    }

    fun onBack() {
        sendEvent(eventChannel, EventScreen2.OnBack)
    }
}