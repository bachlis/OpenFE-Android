package de.datlag.openfe.fragments

import android.os.Bundle
import android.util.Log
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import de.datlag.openfe.R
import de.datlag.openfe.bottomsheets.ConfirmActionSheet
import de.datlag.openfe.bottomsheets.FileProgressSheet
import de.datlag.openfe.commons.*
import de.datlag.openfe.data.ExplorerFragmentStorageArgs
import de.datlag.openfe.databinding.FragmentOverviewBinding
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.recycler.adapter.ActionRecyclerAdapter
import de.datlag.openfe.recycler.adapter.LocationRecyclerAdapter
import de.datlag.openfe.recycler.data.ActionItem
import de.datlag.openfe.recycler.data.LocationItem
import de.datlag.openfe.util.PermissionChecker
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
class OverviewFragment : Fragment(), FragmentBackPressed {

    lateinit var locationList: List<LocationItem>
    lateinit var actionList: List<ActionItem>

    private lateinit var binding: FragmentOverviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        locationList = getLocationItems()
        actionList = getActionItems()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val contextThemeWrapper = ContextThemeWrapper(saveContext, R.style.OverviewFragmentTheme)
        val clonedLayoutInflater = inflater.cloneInContext(contextThemeWrapper)

        saveContext.theme.applyStyle(R.style.OverviewFragmentTheme, true)
        binding = FragmentOverviewBinding.inflate(clonedLayoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = with(binding) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AdvancedActivity).setSupportActionBar(toolBar)
        val toggle = ActionBarDrawerToggle(requireActivity(), drawer, toolBar, R.string.app_name, R.string.app_name)
        toggle.drawerArrowDrawable.color = getColor(R.color.overviewDrawerToggleColor)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        locationRecycler.isNestedScrollingEnabled = false
        locationRecycler.layoutManager = LinearLayoutManager(saveContext)
        locationRecycler.adapter = LocationRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                checkReadPermission(position)
            }
            submitList(locationList)
        }


        actionRecycler.isNestedScrollingEnabled = false
        actionRecycler.layoutManager = GridLayoutManager(saveContext, if(saveContext.packageManager.isTelevision()) 5 else 3)
        actionRecycler.adapter = ActionRecyclerAdapter().apply {
            setOnClickListener { _, position ->
                when(actionList[position].actionId) {
                    R.id.action_OverviewFragment_to_AppsActionFragment -> {
                        val action = OverviewFragmentDirections.actionOverviewFragmentToAppsActionFragment(locationList[0].usage.file.absolutePath)
                        findNavController().navigate(action)
                    }
                    else -> findNavController().navigate(actionList[position].actionId)
                }
            }
            submitList(actionList)
        }
    }

    private fun getLocationItems(): List<LocationItem> {
        val usageStats = saveContext.getStorageVolumes()
        val locationList = mutableListOf<LocationItem>()

        for (usageStat in usageStats) {
            if(usageStat.max != 0L && usageStat.current != 0L) {
                locationList.add(LocationItem(usageStat.file.getDisplayName(saveContext), usageStat))
            }
        }

        return locationList.toList()
    }

    private fun getActionItems(): List<ActionItem> {
        val actionList = mutableListOf<ActionItem>()

        actionList.add(ActionItem(getDrawable(R.drawable.ic_music_note_24dp), "Music", 0))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_image_24dp), "Images", 1))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_local_movies_24dp), "Videos", 2))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_insert_drive_file_24dp), "Documents", 3))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_archive_24dp), "Archives", 4))
        actionList.add(ActionItem(getDrawable(R.drawable.ic_adb_24dp), "Apps", R.id.action_OverviewFragment_to_AppsActionFragment))

        return actionList
    }

    private fun checkReadPermission(position: Int) {
        PermissionChecker.checkReadStorage(saveContext, object: PermissionListener{
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                val action = OverviewFragmentDirections.actionOverviewFragmentToExplorerFragment(
                    ExplorerFragmentStorageArgs(locationList, position)
                )
                findNavController().navigate(action)
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?,
                p1: PermissionToken?
            ) {
                showBottomSheetFragment(PermissionChecker.storagePermissionSheet(saveContext, p1))
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {

            }

        })
    }

    override fun onResume() {
        super.onResume()
        statusBarColor(getColor(R.color.overviewStatusbarColor))
    }

    override fun onBackPressed(): Boolean = with(binding) {
        return if(drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
            false
        } else {
            true
        }
    }

    companion object {
        fun newInstance() = OverviewFragment()
    }
}
