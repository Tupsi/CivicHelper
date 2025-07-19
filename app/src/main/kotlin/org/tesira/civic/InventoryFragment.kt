package org.tesira.civic

import android.graphics.Canvas
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.launch
import org.tesira.civic.db.CardWithDetails
import org.tesira.civic.db.CivicViewModel
import org.tesira.civic.utils.BaseCardListFragment

class InventoryFragment : BaseCardListFragment() {

    override fun getCardsLiveData(): LiveData<List<CardWithDetails>> {
        return civicViewModel.allPurchasedCardsWithDetails
    }

    override fun getButton(): View {
        return binding.btnSortAllCards
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSwipeToDelete()
    }

    fun isSpecificCardPresent(cardName: String): Boolean {
        val currentCardList: List<CardWithDetails>? = getCardsLiveData().value
        if (currentCardList != null) {
            return currentCardList.any { cardWithDetails ->
                cardWithDetails.card.name == cardName
            }
        }
        return false
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0, // Wir unterstützen kein Drag & Drop, daher 0
            ItemTouchHelper.LEFT // Swipe nach links zum Löschen
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false // Nicht benötigt für Swipe-to-Delete
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val cardToDeleteDetails = allCardsAdapter.currentList[position]
                    val cardName = cardToDeleteDetails.card.name
                    val bothExtras = isSpecificCardPresent(CivicViewModel.EXTRA_CREDITS_BOTH)
                    viewLifecycleOwner.lifecycleScope.launch {
                        civicViewModel.deletePurchasedCard(cardToDeleteDetails)
                    }

                    // Snackbar-Feedback
                    val snackbar = Snackbar.make(binding.root, "Card '$cardName' removed", Snackbar.LENGTH_LONG)

                    // Undo-Aktion nur für "normale" Karten anbieten
                    if (cardName != CivicViewModel.WRITTEN_RECORD && cardName != CivicViewModel.MONUMENT) {
                        snackbar.setAction("Undo") {
                            viewLifecycleOwner.lifecycleScope.launch {
                                // Logik zum Wiederherstellen aller Karten ausser Monument/Written Record:
                                // 1. Wieder in 'purchases' eintragen
                                civicViewModel.insertPurchase(cardName)
                                // 2. Boni der Hauptkarte wieder hinzufügen
                                civicViewModel.addBonus(cardToDeleteDetails.card)
                                // 3. Änderungen an den Boni speichern
                                civicViewModel.saveBonus()
                            }
                        }
                    } else {
                        snackbar.setText("Card '$cardName' removed (Undo not available for this card)")
                    }

                    if (bothExtras) {
                        snackbar.setText("You bought 'Monument' and 'Written Record' in the same round, removing both. (Undo not possible)")
                    }
                    snackbar.show()
                }
            }

            // Optional: Visuelles Feedback während des Swipens (Hintergrundfarbe und Icon)
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                // Verwende eine Bibliothek wie RecyclerViewSwipeDecorator für einfaches Styling
                // Du musst diese Bibliothek zu deiner build.gradle hinzufügen:
                // implementation 'it.xabaras.android:recyclerview-swipedecorator:1.4'
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(requireContext(), R.color.md_theme_error)) // Deine Fehlerfarbe
                    .addSwipeLeftActionIcon(R.drawable.ic_delete) // Dein Löschen-Icon
                    // .addSwipeLeftLabel("Löschen") // Optionaler Text
                    // .setSwipeLeftLabelColor(ContextCompat.getColor(requireContext(), R.color.md_theme_onError))
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        // binding.list ist der RecyclerView in BaseCardListFragment
        itemTouchHelper.attachToRecyclerView(binding.recyclerViewAllCards)
    }
}