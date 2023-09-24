package com.udacity.shoestore.fragment

import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeListBinding
import com.udacity.shoestore.databinding.ItemShoeBinding
import com.udacity.shoestore.viewmodels.ShoeStoreViewModel


class ShoeListFragment : Fragment() {

    private lateinit var binding: FragmentShoeListBinding

    private val shoeStoreViewModel: ShoeStoreViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shoe_list, container, false)

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Do custom work here
                    view?.findNavController()?.popBackStack(R.id.loginFragment, false)

                    // if you want onBackPressed() to be called as normal afterwards
//                    if (isEnabled) {
//                        isEnabled = false
//                        requireActivity().onBackPressed()
//                    }
                }
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()

        initData()

        initEvents()


    }

    private fun initView() {
        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.logout_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.logout -> {
                        view?.findNavController()?.popBackStack(R.id.loginFragment, false)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun initData() {
        shoeStoreViewModel.shoes.observe(viewLifecycleOwner) {listShoe ->

            if (listShoe != null){
                for (i in listShoe.indices){
                    val bindingItemShoe: ItemShoeBinding =
                        ItemShoeBinding.inflate(layoutInflater, binding.layoutShoeList, false)
                    bindingItemShoe.shoe = listShoe[i]
                    bindingItemShoe.shoeSizeString = listShoe[i].size.toString()

                    binding.layoutShoeList.addView(bindingItemShoe.root)

                }
            }
        }
    }

    private fun initEvents() {
        binding.floatingBtnAddNewShoe.setOnClickListener{
            it.findNavController().navigate(R.id.action_shoeListFragment_to_shoeDetailFragment)
        }
    }

}