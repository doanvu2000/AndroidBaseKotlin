package com.example.baseproject.base.ui.flex_box_layout

import androidx.lifecycle.lifecycleScope
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.extension.hide
import com.example.baseproject.databinding.ActivityDemoFlexBoxLayoutBinding
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.JustifyContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DemoFlexBoxLayoutActivity : BaseActivity<ActivityDemoFlexBoxLayoutBinding>() {
    private val dataAdapter by lazy {
        DataAdapter()
    }

    override fun initView() {
        binding.rcvData.apply {
            adapter = dataAdapter
            layoutManager = TwoElementFlexBoxManager(this@DemoFlexBoxLayoutActivity, 3).apply {
                justifyContent = JustifyContent.CENTER
                alignItems = AlignItems.CENTER
                flexDirection = FlexDirection.ROW
                flexWrap = FlexWrap.WRAP
            }
        }
    }

    override fun initData() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                delay(1000)
            }
            binding.loading.hide()
            dataAdapter.setDataList(getDataList2())
        }
    }

    private fun getDataList2() = listOf(
        DataEntity(1, "abc"),
        DataEntity(2, "abxc"),
        DataEntity(3, "aabc"),
        DataEntity(4, "assbc"),
        DataEntity(5, "axcsbc"),
        DataEntity(6, "arwqrbc"),
        DataEntity(7, "asdasdbc"),
        DataEntity(8, "aasdasbc"),
        DataEntity(9, "aasdbc"),
        DataEntity(10, "aasdbc"),
    )

    private fun getDataList(): List<DataEntity> {
        return listOf(
            DataEntity(1, "Vạn vật tồn tại đều có lý do của nó"),
            DataEntity(2, "Ta có năng lực cứu thế nhưng không cứu được nàng thì có ích gì"),
            DataEntity(3, "Ta đi khắp luân hồi chỉ để tìm nàng"),
            DataEntity(4, "Nhất niệm chi gian thành chân tiên, phù dao trực thượng đạp cửu thiên."),
            DataEntity(
                5,
                "Sơn bản vô ưu, nhân tuyết bạch đầu, thủy bản vô sầu, nhân phong khởi trứu"
            ),
            DataEntity(6, "Có đôi khi, cả đời, chỉ vì năm đó một hồi gặp nhau."),
            DataEntity(7, "Năm xưa như gió, một đời người, đã định trước sẽ có rất nhiều nhớ lại."),
            DataEntity(
                8,
                "Mưa sinh ra trên trời, chết rơi về mặt đất, cả quá trình rơi xuống chính là nhân sinh!"
            ),
            DataEntity(9, "Hoa có ngày nở lại, người không còn thiếu niên."),
            DataEntity(
                10,
                "Sống thì thế nào? Chết thì làm sao? Sống chết đều có cơ duyên, muôn vật tự có luân hồi. "
            ),
            DataEntity(
                11,
                "Chờ đợi vốn không phải là điều đáng sợ, đáng sợ là không biết phải chờ đợi đến bao giờ. "
            ),
            DataEntity(12, "Đôi khi một thoáng rung động, cũng kéo theo cả đời cố chấp."),
            DataEntity(13, "Bỏ cả giang sơn vì tri kỷ, đâu ngờ tri kỷ thích giang sơn."),
            DataEntity(14, "Yêu thương như phù dù trôi mãi. Có mấy tình là thật lòng thật tâm?"),
            DataEntity(
                15,
                "Có cả thiên hạ không sở nguyện, Mơ ước phu thê hóa hão huyền."
            ),
            DataEntity(16, "Nguyện đắc nhất nhân tâm, bạch thủ bất tương li."),
            DataEntity(17, "Trăm năm mộng ảo, tỉnh dậy phồn hoa đều hóa tro tàn…"),
            DataEntity(
                18,
                "Bốn mùa trầm lặng, ngôn ngữ mất đi màu sắc. Năm tháng tịch liêu, núi sông cũng quên mất hẹn thề."
            ),
            DataEntity(
                19,
                "Tình yêu không kết quả, chỉ cần nở hoa, màu sắc đã rực rỡ rồi. Được trông thấy màu hoa rực rỡ đó, tuổi trẻ của tôi, không còn gì để hối tiếc...\n"
            )
        )
    }

    override fun initListener() {
        binding.btnBack.clickSafe {
            onBack()
        }
        dataAdapter.setOnClickItem { _, position ->
            dataAdapter.setSelected(position)
        }
    }
}