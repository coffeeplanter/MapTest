package ru.coffeeplanter.maptest.data.model

class ServerPointsAnswer(@Suppress("unused") var pageSize: Int, var data: List<Point>?, private var hasMorePages: Boolean) {

    fun hasMorePages(): Boolean {
        return hasMorePages
    }

    @Suppress("unused")
    fun setHasMorePages(hasMorePages: Boolean) {
        this.hasMorePages = hasMorePages
    }

}
