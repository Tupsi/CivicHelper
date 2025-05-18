package org.tesira.civic

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.tesira.civic.databinding.FragmentAboutBinding

class AboutFragment : Fragment() {

    private var _binding: FragmentAboutBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAboutBinding.inflate(inflater, container, false)

        val versionName = try {
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "n/a"
        }

        // Versionsnummer anzeigen (falls in strings.xml ein %s steht, z.B. "Version: %s")
        binding.tvAboutVersion.text = getString(R.string.about_version, versionName)

        // About Text anzeigen
        binding.tvAboutText.text = getString(R.string.about_text)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
