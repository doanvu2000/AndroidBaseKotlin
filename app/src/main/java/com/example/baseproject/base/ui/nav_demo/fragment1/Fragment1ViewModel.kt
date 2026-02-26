package com.example.baseproject.base.ui.nav_demo.fragment1

import com.example.baseproject.base.viewmodel.BaseViewModel
import com.example.baseproject.base.viewmodel.sendEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class Fragment1ViewModel : BaseViewModel() {
    sealed class EventScreen1 {
        data class ClickGotoFragment2(val userName: String) : EventScreen1()
    }

    private val eventChannel = Channel<EventScreen1>(Channel.BUFFERED)
    val event = eventChannel.receiveAsFlow()

    fun clickGotoFragment2(userName: String) {
        sendEvent(eventChannel, EventScreen1.ClickGotoFragment2(userName))
    }
}