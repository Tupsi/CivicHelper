package org.tesira.civic

import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.fragment.app.Fragment
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
        super.onViewCreated(view, savedInstanceState)

        val rootViewToApplyInsets = binding.root

        val initialPaddingLeft = rootViewToApplyInsets.paddingLeft
        val initialPaddingTop = rootViewToApplyInsets.paddingTop
        val initialPaddingRight = rootViewToApplyInsets.paddingRight
        val initialPaddingBottom = rootViewToApplyInsets.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(rootViewToApplyInsets) { v, windowInsets ->
            val systemBarInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.updatePadding(
                left = initialPaddingLeft + systemBarInsets.left,
                top = initialPaddingTop,
                right = initialPaddingRight + systemBarInsets.right,
                bottom = initialPaddingBottom + systemBarInsets.bottom
            )

            WindowInsetsCompat.CONSUMED
            windowInsets
        }

        val versionName = try {
            requireActivity().packageManager.getPackageInfo(requireActivity().packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "n/a"
        }

        binding.tvAboutVersion.text = getString(R.string.about_version, versionName)
        binding.tvAboutText.text = getString(R.string.about_text)
        binding.tvAboutText.movementMethod = ScrollingMovementMethod()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}