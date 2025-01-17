package com.example.gagyeboost.ui.map

import android.content.Context
import android.graphics.ColorSpace
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LiveData
import com.example.gagyeboost.R
import com.example.gagyeboost.common.BitmapUtils
import com.example.gagyeboost.common.formatter
import com.example.gagyeboost.model.data.MyItem
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ui.IconGenerator


class MyClusterRenderer(
    val context: Context,
    map: GoogleMap?,
    clusterManager: ClusterManager<MyItem?>,
    private val isExpense: LiveData<Int>
) : DefaultClusterRenderer<MyItem>(context, map, clusterManager) {

    private val clusterIconGenerator = IconGenerator(context)

    override fun onBeforeClusterItemRendered(item: MyItem, markerOptions: MarkerOptions) {
        ResourcesCompat.getDrawable(
            context.resources,
            if (isExpense.value == 0) R.drawable.ic_expense_marker else R.drawable.ic_income_marker,
            null
        )?.let {
            val bitmap = BitmapUtils.createBitmapFromDrawable(it)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        }
    }

    override fun onBeforeClusterRendered(cluster: Cluster<MyItem?>, markerOptions: MarkerOptions) {
        val icon = with(clusterIconGenerator) {
            setContentPadding(30, 20, 30, 20)
            setBackground(
                ResourcesCompat.getDrawable(
                    context.resources,
                    if (isExpense.value == 0) R.drawable.background_white_radius_20dp_stroke_expense else R.drawable.background_white_radius_20dp_stroke_green,
                    null
                )
            )
            val totalMoney = cluster.items.sumOf { it?.snippet?.replace("원", "")?.toInt() ?: 0 }
            makeIcon("${formatter.format(totalMoney)}원")
        }

        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
    }

    override fun onClusterItemRendered(clusterItem: MyItem, marker: Marker) {

        super.onClusterItemRendered(clusterItem, marker.apply {
            title = clusterItem.title
            snippet = clusterItem.snippet
        })
    }

    override fun onClusterUpdated(cluster: Cluster<MyItem>, marker: Marker) = Unit
}
