package org.tesira.civic

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import org.tesira.civic.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState) // 'view' ist hier binding.root

        val rootViewToApplyInsets = binding.root // Entspricht 'view' in diesem Kontext

        val initialPaddingLeft = rootViewToApplyInsets.paddingLeft
        val initialPaddingTop = rootViewToApplyInsets.paddingTop
        val initialPaddingRight = rootViewToApplyInsets.paddingRight
        val initialPaddingBottom = rootViewToApplyInsets.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(rootViewToApplyInsets) { v, windowInsets ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Wende die Systemleisten-Insets zusätzlich zum ursprünglichen Padding an.
            // Die KTX-Erweiterungsfunktion updatePadding ist hierfür gut geeignet.
            v.updatePadding(
                left = initialPaddingLeft + systemBarInsets.left,
                top = initialPaddingTop, // Für Edge-to-Edge oben
                right = initialPaddingRight + systemBarInsets.right,
                bottom = initialPaddingBottom + systemBarInsets.bottom // Für Edge-to-Edge unten (Navigationsleiste)
            )

            // Es ist wichtig, die WindowInsets (ggf. modifiziert) zurückzugeben,
            // damit Kind-Views sie auch konsumieren können.
            WindowInsetsCompat.CONSUMED // Oder windowInsets, wenn du sie nicht vollständig konsumieren willst
            // CONSUMED ist oft richtig, wenn du das Padding hier final setzt.
            // Wenn Kindelemente ebenfalls auf die Insets reagieren sollen,
            // gib die originalen `windowInsets` zurück oder spezifisch modifizierte.
            // Für einfaches Padding der Root-View ist CONSUMED oder die
            // unveränderten windowInsets oft das, was man will. Teste das Verhalten.
            // Wenn du nur das Padding der aktuellen View anpasst und die
            // Insets nicht weiterleiten musst, kannst du auch die
            // originalen Insets zurückgeben.
            // Um sicherzugehen, dass Insets nicht mehrfach angewendet werden,
            // wenn du sie hier "verbraucht" hast:
            // return WindowInsetsCompat.CONSUMED (verhindert weitere Propagation)
            // Für viele Layouts ist es aber auch okay, die originalen
            // `windowInsets` zurückzugeben, da Kindelemente sie ggf. ignorieren
            // oder selbst korrekt behandeln.
            windowInsets // Gib die originalen Insets zurück, damit Kind-Views ggf. auch reagieren können,
            // es sei denn, du bist sicher, dass du sie hier komplett konsumiert hast.
        }
        // --- Ende: WindowInsets Handling ---

        // Dein bestehender Code:
        val versionName = try {
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "n/a"
        }

        binding.tvAboutVersion.text = getString(R.string.about_version, versionName)
        binding.tvAboutText.text = getString(R.string.about_text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}