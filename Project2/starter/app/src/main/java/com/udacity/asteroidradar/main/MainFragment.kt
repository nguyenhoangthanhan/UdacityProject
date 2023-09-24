package com.udacity.asteroidradar.main

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.udacity.asteroidradar.R
import com.udacity.asteroidradar.databinding.FragmentMainBinding

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this, MainViewModel.Factory(requireActivity().application))[MainViewModel::class.java]
    }

    private val asteroidAdapter = AsteroidAdapter(AsteroidAdapter.OnClickListener { asteroid ->
        viewModel.displayAsteroidDetails(asteroid)
    })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = FragmentMainBinding.inflate(inflater)
        binding.lifecycleOwner = viewLifecycleOwner

        Log.d("MainFragment","onCreateView")
        Log.d("MainFragment_viewModel.toString()", viewModel.toString())
        binding.viewModel = viewModel
        Log.d("MainFragment","onCreateView2")

        binding.asteroidRecycler.adapter = asteroidAdapter

        viewModel.navigateToSelectedProperty.observe(viewLifecycleOwner) {
            if (null != it) {
                this.findNavController().navigate(MainFragmentDirections.actionShowDetail(it))
                viewModel.displayAsteroidDetailsComplete()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("MainFragment","onViewCreated")

//        viewModel.asteroidList.observe(viewLifecycleOwner) { asteroids ->
//            asteroids?.apply {
//                Log.d("asteroidList",asteroids.toString())
//                asteroidAdapter.submitList(this)
//            }
//        }

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_overflow_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.show_today_asteroid -> {
                        // clearCompletedTasks()
                        Log.d("MainFragment_onMenuItemSelected","FilterAsteroid.TODAY")
                        viewModel.onChangeFilter(FilterAsteroid.TODAY)
                        true
                    }
                    R.id.show_week_asteroid -> {
                        // clearCompletedTasks()
                        Log.d("MainFragment_onMenuItemSelected","FilterAsteroid.WEEK")
                        viewModel.onChangeFilter(FilterAsteroid.WEEK)
                        true
                    }
                    R.id.show_saved_asteroid -> {
                        // clearCompletedTasks()
                        Log.d("MainFragment_onMenuItemSelected","FilterAsteroid.ALL")
                        viewModel.onChangeFilter(FilterAsteroid.ALL)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainFragment","onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainFragment","onResume")
    }

}
