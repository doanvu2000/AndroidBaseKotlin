package com.example.baseproject.base.ui.nav_demo.fragment3

import com.example.baseproject.base.viewmodel.BaseViewModel
import com.example.baseproject.base.viewmodel.sendEvent
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class Fragment3ViewModel : BaseViewModel() {
    sealed class EventScreen3 {
        data object OnBack : EventScreen3()
    }

    private val eventChannel = Channel<EventScreen3>(Channel.BUFFERED)
    val event = eventChannel.receiveAsFlow()

    fun onBack() {
        sendEvent(eventChannel, EventScreen3.OnBack)
    }
}