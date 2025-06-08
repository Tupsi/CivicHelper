package org.tesira.civic

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Für die gemeinsame ViewModel-Instanz
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
// Importiere deine CardWithDetails-Klasse
import org.tesira.civic.db.CardWithDetails // Ersetze mit deinem Paketpfad
// Importiere deinen RecyclerView-Adapter (den wir als Nächstes erstellen müssten)
import org.tesira.civic.AllCardsAdapter // Beispielpfad
import org.tesira.civic.databinding.FragmentAllCardsBinding // Generierte ViewBinding-Klasse
import org.tesira.civic.db.CivicViewModel

class AllCardsFragment : Fragment() {

    // View Binding
    private var _binding: FragmentAllCardsBinding? = null
    private val binding get() = _binding!!
    private val civicViewModel: CivicViewModel by activityViewModels()
    private lateinit var allCardsAdapter: AllCardsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllCardsBinding.inflate(inflater, container, false)
        val rootView: View = binding.getRoot()
        val initialPaddingLeft = rootView.paddingLeft
        val initialPaddingTop = rootView.paddingTop
        val initialPaddingRight = rootView.paddingRight
        val initialPaddingBottom = rootView.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(
            rootView,
            OnApplyWindowInsetsListener { v: View?, windowInsets: WindowInsetsCompat? ->
                val systemBarInsets = windowInsets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(
                    initialPaddingLeft + systemBarInsets.left,
                    initialPaddingTop,
                    initialPaddingRight + systemBarInsets.right,
                    initialPaddingBottom + systemBarInsets.bottom
                )
                windowInsets
            })

        ViewCompat.requestApplyInsets(rootView)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
    }


    private fun setupRecyclerView() {
        // Initialisiere den Adapter
        allCardsAdapter = AllCardsAdapter()

        binding.recyclerViewAllCards.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = allCardsAdapter
            // Optional: ItemDecoration für Abstände zwischen Elementen
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun observeViewModel() {
        civicViewModel.allCardsWithDetails.observe(viewLifecycleOwner) { cardsWithDetailsList ->
            if (cardsWithDetailsList.isNotEmpty()) {
                binding.textViewPlaceholder.visibility = View.GONE
                binding.recyclerViewAllCards.visibility = View.VISIBLE

                // Aktualisiere den Adapter mit den neuen Daten
                allCardsAdapter.submitList(cardsWithDetailsList) // ListAdapter verwendet submitList()

                Log.d("AllCardsFragment", "Karten mit Details geladen und an Adapter übergeben: ${cardsWithDetailsList.size}")
            } else {
                binding.textViewPlaceholder.visibility = View.VISIBLE
                binding.recyclerViewAllCards.visibility = View.GONE
                binding.textViewPlaceholder.text = "Keine Karten gefunden."
                Log.d("AllCardsFragment", "Keine Karten mit Details gefunden.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // View Binding im Speicher freigeben
    }
}