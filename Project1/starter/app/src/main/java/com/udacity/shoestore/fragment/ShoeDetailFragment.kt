package com.udacity.shoestore.fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.udacity.shoestore.R
import com.udacity.shoestore.databinding.FragmentShoeDetailBinding
import com.udacity.shoestore.models.Shoe
import com.udacity.shoestore.viewmodels.ShoeStoreViewModel


class ShoeDetailFragment : Fragment() {

    private lateinit var binding: FragmentShoeDetailBinding

    private val shoeStoreViewModel: ShoeStoreViewModel by activityViewModels()

    private var shoe = Shoe("", 0.0, "", "")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_shoe_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initData()

        initEvents()
    }

    private fun initData() {
        binding.shoe = shoe
    }

    private fun initEvents() {
        binding.btnSaveNewShoe.setOnClickListener{
            if (binding.edtInputShoeName.text.toString().isEmpty() ||
                binding.edtInputShoeSize.text.toString().isEmpty() ||
                binding.edtInputShoeCompany.text.toString().isEmpty() ||
                binding.edtInputShoeDescription.text.toString().isEmpty()){
                Toast.makeText(requireContext(), "All of fields must not empty!", Toast.LENGTH_LONG).show()
            }
            else{
                setNewShoe()
                it.findNavController().popBackStack()
            }
        }
        binding.btnCancel.setOnClickListener{
            it.findNavController().popBackStack()
        }
    }

    private fun setNewShoe() {
        shoe.size = binding.edtInputShoeSize.text.toString().toDouble()
        shoeStoreViewModel.addShoeToList(shoe)
    }
}