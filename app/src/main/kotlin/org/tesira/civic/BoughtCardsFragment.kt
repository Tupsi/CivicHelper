package org.tesira.civic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import org.tesira.civic.databinding.FragmentBoughtCardsBinding

class BoughtCardsFragment : Fragment() {

    private var _binding: FragmentBoughtCardsBinding? = null
    private val binding get() = _binding!!

    private val args: BoughtCardsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBoughtCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val cardNames = args.cardNames.toList().sorted()
        val adapter = BoughtCardsAdapter(cardNames)

        binding.boughtCardsRecyclerView.adapter = adapter
        binding.boughtCardsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        binding.closeButton.setOnClickListener {
            findNavController().navigate(R.id.action_boughtCardsFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}