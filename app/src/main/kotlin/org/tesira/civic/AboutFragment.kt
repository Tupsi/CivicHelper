package org.tesira.civic

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
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
        super.onViewCreated(view, savedInstanceState)
//        binding.root.applyHorizontalSystemBarInsetsAsPadding()

        val versionName = try {
            requireActivity().packageManager.getPackageInfo(
                requireActivity().packageName,
                0
            ).versionName
        } catch (_: PackageManager.NameNotFoundException) {
            "n/a"
        }

        binding.tvAboutVersion.text = getString(R.string.about_version, versionName)
        binding.tvAboutText.text =
            HtmlCompat.fromHtml(getString(R.string.about_text), HtmlCompat.FROM_HTML_MODE_LEGACY)
        binding.tvAboutText.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}