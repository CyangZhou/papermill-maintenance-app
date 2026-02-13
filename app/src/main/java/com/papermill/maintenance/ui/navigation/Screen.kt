package com.papermill.maintenance.ui.navigation

sealed class Screen(val route: String) {
    object RecordList : Screen("record_list")
    object RecordDetail : Screen("record_detail/{recordId}") {
        fun createRoute(recordId: Long) = "record_detail/$recordId"
    }
    object AddRecord : Screen("record_detail/0")
}
