package com.hsd.contest.spain.view.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.hsd.contest.domain.entities.ListMunicipality
import com.hsd.contest.domain.entities.ListProvinces
import com.hsd.contest.domain.entities.Province
import com.hsd.contest.domain.entities.WeatherInfo
import com.hsd.contest.spain.R
import com.hsd.contest.spain.databinding.WeatherFragmentBinding
import com.hsd.contest.spain.view.MenuActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class WeatherFragment : Fragment() {
    private var binding: WeatherFragmentBinding? = null
    private val viewModel by viewModel<WeatherViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = WeatherFragmentBinding.inflate(inflater, container, false)
        setObserver()
        analytics()
        initListeners()
        return binding?.root
    }

    private fun analytics() {
        val bundle = Bundle()
        bundle.putString("screen_name", "Weather)")
        (activity as MenuActivity).instance?.onEvent("screen_Weather", bundle)
    }

    private fun initListeners() {
        binding?.weatherEditCity?.setOnClickListener {
            binding?.containerWeather?.visibility = View.GONE
            binding?.containerSearchWeather?.visibility = View.VISIBLE
            binding?.weatheSpinner?.visibility = View.VISIBLE
        }
    }

    private fun setObserver() {
        viewModel.provinces.observe(viewLifecycleOwner, { list ->
            initProvinceSpinner(list)
        })
        viewModel.municipalities.observe(viewLifecycleOwner, { list ->
            initMunicipalitySpinner(list)
        })
        viewModel.error.observe(viewLifecycleOwner, {
            binding?.root?.let { root ->
                Snackbar.make(
                    root,
                    R.string.error_alert,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        })
        viewModel.weather.observe(viewLifecycleOwner, { weatherInfo ->
            initUI(weatherInfo)
            if (binding?.weatheSpinnerMunicipality?.visibility == View.VISIBLE) {
                showWeather()
            }
        })
    }

    private fun showWeather() {
        binding?.weatheSpinner?.visibility = View.GONE
        binding?.weatheSpinnerMunicipality?.visibility = View.GONE
        binding?.buttonSearch?.visibility = View.GONE
        binding?.containerSearchWeather?.visibility = View.GONE
        binding?.containerWeather?.visibility = View.VISIBLE
    }

    private fun initUI(weatherInfo: WeatherInfo) {
        binding?.containerWeather?.visibility = View.VISIBLE
        binding?.weatherCity?.text = weatherInfo.nameMunicipality
        binding?.weatherTitle?.text = weatherInfo.date
        binding?.weatherState?.text = weatherInfo.skyState
        binding?.weatherActualTemperature?.text =
            StringBuilder(weatherInfo.actualTemperature.toString()).append("º")
        binding?.weatherTemperatureToday?.text =
            StringBuilder(weatherInfo.temperatureMax.toString()).append("º / ")
                .append(weatherInfo.temperatureMin).append("º")
        binding?.weatherNextDaysList?.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = WeatherAdapter(weatherInfo.weatherNextDays)
        }
    }

    private fun initMunicipalitySpinner(list: ListMunicipality) {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            list.list
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding?.weatheSpinnerMunicipality?.adapter = adapter
        }
        binding?.weatheSpinnerMunicipality?.setSelection(list.list.indexOfFirst { municipality -> municipality.name == "Madrid" })
        binding?.weatheSpinnerMunicipality?.onItemSelectedListener =
            (object : OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val code = (binding?.weatheSpinner?.selectedItem as Province).code
                    if (viewModel.weather.value == null) {
                        viewModel.weather(
                            code,
                            (list.list[position].code).substring(0, 5)
                        )
                    } else {
                        binding?.buttonSearch?.visibility = View.VISIBLE
                        binding?.buttonSearch?.setOnClickListener {
                            viewModel.weather(
                                code,
                                (list.list[position].code).substring(0, 5)
                            )
                            showWeather()
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    //
                }

            })
    }

    private fun initProvinceSpinner(list: ListProvinces) {
        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            list.provinces
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding?.weatheSpinner?.adapter = adapter
        }
        binding?.weatheSpinner?.setSelection(list.provinces.indexOfFirst { province -> province.name == "Madrid" })
        binding?.weatheSpinner?.onItemSelectedListener = (object : OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                viewModel.getListMunicipality((binding?.weatheSpinner?.selectedItem as Province).code)
                if (binding?.weatheSpinner?.visibility == View.VISIBLE) {
                    binding?.weatheSpinnerMunicipality?.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // don't do nothing
            }

        })

    }
}