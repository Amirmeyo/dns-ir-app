package com.dnsir.app.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dnsir.app.data.DnsEntry
import com.dnsir.app.data.DnsRepository
import com.dnsir.app.data.fallbackDnsList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

class MainViewModel(
    private val repository: DnsRepository = DnsRepository()
) : ViewModel() {

    private val _dnsList = MutableStateFlow<List<DnsEntry>>(fallbackDnsList)
    val dnsList: StateFlow<List<DnsEntry>> = _dnsList

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _fetchError = MutableStateFlow(false)
    val fetchError: StateFlow<Boolean> = _fetchError

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    val selectedDns = mutableStateOf<DnsEntry?>(null)

    init {
        loadDnsList()
    }

    fun loadDnsList() {
        viewModelScope.launch {
            _isLoading.value = true
            _fetchError.value = false
            repository.fetchDnsList()
                .onSuccess {
                    _dnsList.value = it
                    if (selectedDns.value == null) selectedDns.value = it.firstOrNull()
                }
                .onFailure {
                    _dnsList.value = fallbackDnsList
                    _fetchError.value = true
                    if (selectedDns.value == null) selectedDns.value = fallbackDnsList.firstOrNull()
                }
            _isLoading.value = false
        }
    }

    fun selectDns(entry: DnsEntry) {
        selectedDns.value = entry
    }

    fun setConnectionState(state: ConnectionState) {
        _connectionState.value = state
    }
}
