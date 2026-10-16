package com.patikagunlugu.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.patikagunlugu.app.R
import com.patikagunlugu.app.databinding.FragmentHomeBinding
import com.patikagunlugu.app.ui.adapter.PetHomeAdapter
import com.patikagunlugu.app.viewmodel.PetListViewModel
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PetListViewModel by viewModels()
    private lateinit var petAdapter: PetHomeAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        observePets()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        petAdapter = PetHomeAdapter(
            onItemClick = { pet ->
                val action = HomeFragmentDirections.actionHomeFragmentToTimelineFragment(pet.id, pet.name)
                findNavController().navigate(action)
            },
            onDeleteClick = { pet ->
                showDeleteConfirmationDialog(pet)
            }
        )
        binding.rvPets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPets.adapter = petAdapter
    }

    private fun showDeleteConfirmationDialog(pet: com.patikagunlugu.app.model.Pet) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_pet)
            .setMessage(getString(R.string.delete_pet_confirm, pet.name))
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePet(pet)
                android.widget.Toast.makeText(requireContext(), getString(R.string.pet_deleted), android.widget.Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private var allPetsWithMemories: List<com.patikagunlugu.app.model.PetWithMemories> = emptyList()

    private fun filterPets(query: String) {
        val filteredList = if (query.isEmpty()) {
            allPetsWithMemories
        } else {
            allPetsWithMemories.filter { 
                it.pet.name.lowercase().contains(query) || 
                (it.pet.breed?.lowercase()?.contains(query) ?: false)
            }
        }
        petAdapter.submitList(filteredList)
    }

    private fun observePets() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.petsWithMemories.collect { petsWithMemories ->
                    allPetsWithMemories = petsWithMemories
                    if (petsWithMemories.isEmpty()) {
                        binding.rvPets.visibility = View.GONE
                        binding.searchBarContainer.visibility = View.GONE
                        binding.fabAddPet.visibility = View.GONE
                        binding.cvAddMemberDashed.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvPets.visibility = View.VISIBLE
                        binding.searchBarContainer.visibility = View.VISIBLE
                        binding.fabAddPet.visibility = View.VISIBLE
                        binding.cvAddMemberDashed.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        filterPets(binding.etSearch.text.toString())
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddPet.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToPetEditFragment(0L, false)
            findNavController().navigate(action)
        }
        
        binding.cvAddMemberDashed.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToPetEditFragment(0L, false)
            findNavController().navigate(action)
        }
        
        binding.btnFirstPet.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToPetEditFragment(0L, false)
            findNavController().navigate(action)
        }

        binding.etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().lowercase()
                filterPets(query)
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
